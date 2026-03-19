import java.time.*;

class HTTPResponse {

  private String statusLine;
  private String date;
  private String server;
  private String lastModified;
  private String contentLength;

  private byte[] body;

  HTTPResponse(){
    
    this.server = MyWebServer.SERVER_NAME;
    this.date = LocalDateTime.now()

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
