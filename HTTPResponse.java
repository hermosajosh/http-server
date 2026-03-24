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

  private byte[] body;

  // Date formatters for formats allowed by HTTP 1.1 (RFC 2616-S3) not sure how important the 
  // legacy datetime formats are but I thought I might as well implement them.

  // RFC 822/1123 date formatter for encoding/parsing
  private SimpleDateFormat dateFormatOne = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  // RFC 850/1036 date formatter for parsing (if previous fails to parse from input)
  private SimpleDateFormat dateFormatTwo = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'", Locale.US);

  // ANSI C asctime() format
  private SimpleDateFormat dateFormatThree = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);

  // Format specified by assignment specs
  private SimpleDateFormat dateFormatFour = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);

  // Response String Constants, ordered by desc precedence (higher overwrites lower)
  private final String BAD_REQUEST = "400 Bad Request";
  private final String NOT_IMPLEMENTED = "501 Not Implemented";
  private final String NOT_FOUND = "404 Not Found";
  private final String NOT_MODIFIED = "304 Not Modified";
  private final String OK = "200 OK";

  HTTPResponse(HTTPRequest req){

    System.out.println("\n---RESPONSE BUILDER---\n");

    // Initially set body to null
    this.body = null;
    this.contentLength = null;
    this.lastModified = null;

    this.server = MyWebServer.SERVER_NAME;
    this.version = "HTTP/1.1 ";

    // Set timezone for our date formatters to automatically format other timezone info in GMT 
    this.dateFormatOne.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatTwo.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatThree.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Format date using the class-specified form. I believe this is not the actual specified HTTP 1.1 format though.
    this.date = this.dateFormatFour.format(new Date());   
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
        setErrorBody("400", "Bad Request");

        // If neither HEAD nor GET, but legal request, then throw 501 not implemented
      } else {

        this.statusLine = this.NOT_IMPLEMENTED;
        setErrorBody("501", "Not Implemented");

      }

    } else {

      this.statusLine = this.BAD_REQUEST;
      setErrorBody("400", "Bad Request");

    }

  }

  private void processBody(HTTPRequest req, boolean attachBody){

    String filePath = req.getAbsolutePath();
    File f = new File(filePath);

    // Get the date of the last file modification
    Date lastModifiedDate = null;
    if(f.exists()){

      lastModifiedDate = new Date(f.lastModified());
      // Again, we format this output in the specified instructions' form, but actual HTTP 1.1 is different
      this.lastModified = dateFormatFour.format(lastModifiedDate);

    }

    // Check if a 304 Not Modified is needed to be checked for
    String ifModifiedSinceField = req.getHeaders().get("IF-MODIFIED-SINCE");
    if(ifModifiedSinceField != null && lastModifiedDate != null){


      Date IMSDate;
      // Parse the allowable DateTime formats
      try {
        IMSDate = dateFormatOne.parse(ifModifiedSinceField);
      } catch(ParseException p){
        try {
          IMSDate = dateFormatTwo.parse(ifModifiedSinceField);
        } catch(ParseException r){
          try {
            IMSDate = dateFormatThree.parse(ifModifiedSinceField);
          } catch(ParseException s){
            try {
              IMSDate = dateFormatFour.parse(ifModifiedSinceField);
            } catch(ParseException t){
              IMSDate = null;
            }
          }
        }
      }

      // This is against HTTP 1.1 specification, however project instructions state to throw a 400
      // when encountering an unparsable if-modified-since date. Actual HTTP 1.1 specification requires
      // ignoring malformed dates and only throwing a 400 if the structure itself is incorrect
      if(IMSDate == null){

        this.statusLine = this.BAD_REQUEST;
        setErrorBody("400", "Bad Request");
        return;

      }

      // Determine if we need to throw a 304 by checking that NONE of the following cases are true
      // 1) The If-Modified-Since value could not be parsed to a date
      // 2) The last time the file was modified occured after the If-Modified-Since date
      // 3) The If-Modified-Since date is set in the future (after current date)
      if(!( lastModifiedDate.after(IMSDate)||IMSDate.after(new Date()) )){

        this.statusLine = this.NOT_MODIFIED;
        return;

      }

    }

    // Obtain file, throw 404 and display simple html if it does not exist

    try{

      FileInputStream file = new FileInputStream(filePath);
      // Assign data buffer based on size of body
      this.body = new byte[(int)f.length()];
      file.read(this.body);
      file.close();

      this.statusLine = this.OK;

    } catch (FileNotFoundException e){
      setErrorBody("404", "Not Found");
      this.lastModified = null;
      this.statusLine = this.NOT_FOUND;
      return;
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
    if(!attachBody){this.body = null;}


  }

  // Send data to the Socket
  public void process(OutputStream out){

    try{

      PrintWriter pout = new PrintWriter( new OutputStreamWriter(out, "8859_1"), true );

      pout.println(this.version + this.statusLine);
      pout.println("Date: " + this.date);
      pout.println("Server: " + this.server);
      if(!(this.lastModified == null))
        pout.println("Last-Modified: " + this.lastModified);
      if(!(this.contentLength == null))
        pout.println("Content-Length: " + this.contentLength);

      pout.println();

      if(!(this.body == null)){

        out.write(this.body);
        out.flush();

      }
    } catch (IOException e){
      System.err.print("I/O error encountered: " + e);
    }

    System.out.print("Full Response:\n"+ this.version + this.statusLine + "\nServer: "+ 
        this.server + "\nDate: "+ this.date+"\nContent-Length: "+
        this.contentLength + "\nLast-Modified: "+this.lastModified + "\n");


  }

  // Method to determine if connection should be terminated from error
  public boolean shouldClose() {

    return (this.statusLine.equals(this.NOT_IMPLEMENTED) || this.statusLine.equals(this.BAD_REQUEST));

  }

  // Method to generate body for error codes
  private void setErrorBody(String statusCode, String message){

    // Simple html error message to display error page
    String errorPage = "<body style=\"text-align:center\"><h1>" + statusCode + "</h1><p>" + message + "</p></body>";
    // Convert html into byte data
    this.body = errorPage.getBytes(StandardCharsets.ISO_8859_1);
    this.contentLength = "" + this.body.length;
  }

}
