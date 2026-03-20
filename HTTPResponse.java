import java.time.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.nio.charset.StandardCharsets;

class HTTPResponse {

  private String version;
  private String statusLine;
  private String date;
  private String server;
  private String contentLength;
  private String lastModified;
  private String contentType;

  private byte[] body;

  // Date formatters for formats allowed by HTTP 1.1 (RFC 2616-S3) not sure how important the 
  // legacy datetime formats are but I thought I might as well implement them.

  // RFC 822/1123 date formatter for encoding/parsing
  private SimpleDateFormat dateFormatOne = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  // RFC 850/1036 date formatter for parsing (if previous fails to parse from input)
  private SimpleDateFormat dateFormatTwo = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'", Locale.US);

  // ANSI C asctime() format
  private SimpleDateFormat dateFormatThree = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);

  // Response String Constants, ordered by desc precedence (higher overwrites lower)
  private final String BAD_REQUEST = "400 Bad Request";
  private final String NOT_IMPLEMENTED = "501 Not Implemented";
  private final String NOT_FOUND = "404 Not Found";
  private final String NOT_MODIFIED = "304 Not Modified";
  private final String OK = "200 OK";

  HTTPResponse(HTTPRequest req){

    System.out.println("\n---RESPONSE BUILDER---\n");

    //Initially set body to null
    this.body = null;
    this.contentType = null;
    this.contentLength = null;
    this.lastModified = null;

    this.server = MyWebServer.SERVER_NAME;
    this.version = "HTTP/1.1 ";

    // Set timezone for our date formatters to automatically format other timezone info in GMT 
    this.dateFormatOne.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatTwo.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatThree.setTimeZone(TimeZone.getTimeZone("GMT"));

    this.date = this.dateFormatOne.format(new Date());   
    System.out.println("Current Date: " + this.date);

    System.out.println("Request Has Error? " + req.getError());
    if(!req.getError()){

      String command = req.getCommand();
      System.out.println("Processed Command: " + command);
      boolean attachBody;

      // Determine how to process request based on HTTP command

      // If either of the implemented HTTP commands, then continue processing
      if(command.equals("GET")){

        System.out.println("Received GET, processing.");
        attachBody = true;
        processBody(req, attachBody);

      } else if(command.equals("HEAD")){

        System.out.println("Received HEAD, processing.");
        attachBody = false;
        processBody(req, attachBody);

        // If illegal format (containing anything other than caps letters), set bad req status
      } else if(!command.matches("[A-Z]+")){

        this.statusLine = this.BAD_REQUEST; 

        // If neither HEAD nor GET, but legal request, then throw 501 not implemented
      } else {this.statusLine = this.NOT_IMPLEMENTED;}

    } else {this.statusLine = this.BAD_REQUEST;}

  }

  private void processBody(HTTPRequest req, boolean attachBody){

    String filePath = req.getAbsolutePath();
    File f = new File(filePath);

    // Get the date of the last file modification
    Date lastModifiedDate = null;
    if(f.exists()){

      lastModifiedDate = new Date(f.lastModified());
      this.lastModified = dateFormatOne.format(lastModifiedDate);

    }

    // Check if a 304 Not Modified is needed to be checked for
    String ifModifiedSinceField = req.getHeaders().get("IF-MODIFIED-SINCE");
    if(ifModifiedSinceField != null && lastModifiedDate != null){


      Date IMSDate;
      // Parse the allowable DateTime formats
      try{
        IMSDate = dateFormatOne.parse(ifModifiedSinceField);
      } catch(ParseException p){
        try{
          IMSDate = dateFormatTwo.parse(ifModifiedSinceField);
        } catch(ParseException r){
          try{
            IMSDate = dateFormatThree.parse(ifModifiedSinceField);
          } catch(ParseException s){
            IMSDate = null;
          }
        }
      }

      // Determine if we need to throw a 304 by checking that NONE of the following cases are true
      // 1) The If-Modified-Since value could not be parsed to a date
      // 2) The last time the file was modified occured after the If-Modified-Since date
      // 3) The If-Modified-Since date is set in the future (after current date)
      if(!( IMSDate == null||lastModifiedDate.after(IMSDate)||IMSDate.after(new Date()) )){

        this.statusLine = this.NOT_MODIFIED;
        return;

      }

    }

    // Obtain file, throw 404 and display simple html if it does not exist

    try{

      FileInputStream file = new FileInputStream(filePath);
      this.body = new byte[file.available()];
      file.read(this.body);

      this.statusLine = this.OK;

    } catch (FileNotFoundException e){

      // Simple HTML to display 404 error page
      String errorPage = "<body style=\"text-align:center\"><h1>404</h1><p>Not Found</p></body>";

      // Convert html into byte data
      this.body = errorPage.getBytes(StandardCharsets.ISO_8859_1);
      this.lastModified = null;
      this.statusLine = this.NOT_FOUND;
    } catch (IOException e){
      System.err.print("I/O error encountered: " + e);
      this.body = null;
      this.lastModified = null;
      this.statusLine = "500 Internal Server Error";
      return;
    }

    // Determine if we want to attach the body or not
    // Find Content-Length field
    this.contentLength = "" + this.body.length;
    this.contentType = "text/html";
    if(!attachBody){this.body = null;}


  }

  // Send data to the Socket
  public void process(OutputStream out){

    try{

      PrintWriter pout = new PrintWriter( new OutputStreamWriter(out, "8859_1"), true );

      pout.println(this.version + this.statusLine);
      pout.println("Server: " + this.server);
      pout.println("Date: " + this.date);
      if(!(this.contentType == null))
        pout.println("Content-Type: " + this.contentType);
      if(!(this.contentLength == null))
        pout.println("Content-Length: " + this.contentLength);
      if(!(this.lastModified == null))
        pout.println("Last-Modified: " + this.lastModified);

      pout.println();

      if(!(this.body == null)){

        out.write(this.body);
        out.flush();

      }
    } catch (IOException e){
      System.err.print("I/O error encountered: " + e);
    }

    System.out.print("Full Response:\n"+ this.version + this.statusLine + "\nServer: "+ 
        this.server + "\nDate: "+ this.date+"\nContent-Type: "+this.contentType + "\nContent-Length: "+
        this.contentLength + "\nLast-Modified: "+this.lastModified);


  }

}
