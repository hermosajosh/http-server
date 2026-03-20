import java.util.*;

class HTTPRequest {

  private String command;
  private String absolutePath;
  private String version;

  private Map<String, String> headers = new HashMap<>();
  private boolean hasError;

  HTTPRequest(String reqLine, ArrayList<String> argLines, String path){

    System.out.println("\n---REQUEST INTERPRETER---\n");
    this.hasError = false;
    String[] splitReqLine = reqLine.split(" ");

    // Ensure request line is not malformed
    System.out.println("Interpret First Request Line Has: " + splitReqLine.length + " tokens.");
    if (splitReqLine.length == 3){

      // First token is http command (no checks if it is a legit command here)

      this.command = splitReqLine[0];
      this.version = splitReqLine[2];

      // If request line is malformed, we toggle the error flag, which will notify response builder to 
      // throw a 400 Bad Request

    } else {this.hasError = true;}

    // Path/URL processing

    this.absolutePath = path;
    String filePath = splitReqLine[1];

    // Process AbsoluteURI Requests. Note first part of URI is case-insensitive as per HTTP 1.1 specs
    if(filePath.toUpperCase().startsWith("HTTP")){

      String[] splitPath = filePath.split("/", 4);

      // Not needed, but I extract host info from absURI requests just
      // for the sake of abiding by the HTTP specifications in the case
      // that this Web Server is ever expanded to full functionality

      String host = splitPath[2];
      filePath = splitPath[3];

      // Align filename to current directory
    } else if(filePath.startsWith("/")){
      filePath = filePath.substring(1);
    }
    // Check if filename references a directory (if so request index.html from said directory)
    if(filePath.endsWith("/") || filePath.equals("")){
      filePath = filePath + "index.html";
    }

    this.absolutePath += filePath;
    System.out.println("Searching for file with path: " + this.absolutePath);

    System.out.println("Argument Lines: ");

    // Process all request arguments into a hashmap
    for(int i = 0; i<argLines.size(); i++){
      
      System.out.println(argLines.get(i));

      String[] currentLineTokens = argLines.get(i).split(":", 2);

      // Capitalize argument since args are specified to be case insensitive
      String currentArgument = currentLineTokens[0].toUpperCase();
      // There is an optional space possible following the ':' so we must strip the value of any spaces
      String currentValue = currentLineTokens[1].replaceAll("//s", "");
      
      headers.put(currentArgument, currentValue); 

    }
  }

  //Getter methods

  public String getCommand(){

    return(this.command);

  } 

  public String getAbsolutePath(){

    return(this.absolutePath);

  } 

  public String getVersion(){

    return(this.version);

  }

  public Map<String, String> getHeaders(){
    
    return(this.headers);
  }

  public boolean getError(){

    return(this.hasError);

  }

  public boolean keepAlive(){

    boolean keepAlive = true;
    String status = this.headers.get("CONNECTION");

    if(status != null){

      if(status.toUpperCase().equals("CLOSE")){

        keepAlive = false;

      }

    }

    return(keepAlive);

  }

}
