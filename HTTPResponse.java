import java.time.*;
import java.text.*;

class HTTPResponse {

  private String statusLine;
  private String date;
  private String server;
  private String contentLength;

  private byte[] body;

  // Date formatters for formats allowed by HTTP 1.1 (RFC 2616-S3) not sure how important the 
  // legacy datetime formats are but I thought I might as well implement them.
  
  // RFC 822/1123 date formatter for encoding/parsing
  private SimpleDateFormat dateFormatOne = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  // RFC 850/1036 date formatter for parsing (if previous fails to parse from input)
  private SimpleDateFormat dateFormatTwo = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'", Locale.US);

  // ANSI C asctime() format
  private SimpleDateFormat dateFormatThree = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
 
  // Response String Constants
  private final String OK = "200 OK";
  private final String NOT_MODIFIED = "304 Not Modified";
  private final String BAD_REQUEST = "400 Bad Request";
  private final String NOT_FOUND = "404 Not Found";
  private final String NOT_IMPLEMENTED = "501 Not Implemented";

  HTTPResponse(HTTPRequest req){
    
    this.server = MyWebServer.SERVER_NAME;
    
    // Set timezone for our date formatters to automatically format other timezone info in GMT 
    this.dateFormatOne.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatTwo.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.dateFormatThree.setTimeZone(TimeZone.getTimeZone("GMT"));

    this.date = this.dateFormatOne.format(LocalDateTime.now());   

    if(!req.getError){



    } else {this.statusLine = this.BAD_REQUEST}

  }

  public void processRequest(String request){

    StringTokenizer tokenizedRequest = new StringTokenizer(request);

    if(tokenizedRequest.countTokens() >= 2){
      
      // Process Method

      String method = tokenizedRequest.nextToken();
      
      // Determine if method abides by HTTP syntax specifications
      // Basically can only consist of uppercase letters
      if(method.matches("[A-Z]+")){
       
         // Determine if method is currently implemented (GET, HEAD)
         if(method.equals("GET") || method.equals("HEAD")){
          
           this.statusLine = processURL(tokenizedRequest.nextToken());
           
         } else {this.statusLine = "501 Not Implemented";}

      } else {this.statusLine = "400 Bad Request";}
      
    } else {this.statusLine = "400 Bad Request";}
  }

  private String processURL(String URL){

    filePath = URL;

    // Handle AbsoluteURI Requests
    if(filePath.startsWith("http")){

      String[] splitPath = filePath.split("/", 4);

      // Not needed, but I extract host info from absURI requests just
      // for the sake of abidingn by the HTTP specifications in the case
      // that this Web Server is ever expanded to full functionality

      host = splitPath[2];
      filePath = splitPath[3];

    // Align filename to current directory
    } else if(filePath.startsWith("/")){
      filePath = filePath.substring(1);
    }
    // Check if filename references a directory (if so request index.html from said directory)
    if(filePath.endsWith("/") || filePath.equals("")){
      filePath = filePath + "index.html";
    }

    String completeResourcePath = this.path + filePath;
    System.out.println("Fetching data from: " + completeResourcePath);

    // Obtain file, throw 404 and display simple html if it does not exist

    try{

      FileInputStream file = new FileInputStream(completeResourcePath);
      this.body = new byte[file.available()];
      file.read(this.body);

      return("200 OK");

    } catch (FileNotFoundException e){
      
      // Simple HTML to display 404 error page
      String errorPage = "<body style=\"text-align:center\"><h1>404</h1><p>Not Found</p></body>";

      // Convert html into byte data
      this.body = errorPage.getBytes(StandardCharsets.ISO_8859_1);

      return("404 Object Not Found");
    }
  }

  // Method to handle each optional request argument. Should only be called AFTER running processRequest 
  // if the optional request arg is to close the connection, return false. Otherwise return true for all.
  public boolean processArgument(String argument){
    
  }

}
