class HTTPRequest {

  private String command;
  private String absolutePath;
  private String version;

  private Map<String, String> headers = new HashMap<>();
  private boolean hasError;

  HTTPRequest(String reqLine, ArrayList<String> argLines, String path){

    this.hasError = false;
    splitReqLine = reqLine.split(" ");

    // Ensure request line is not malformed
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

    System.out.println("Argument Lines: ");

    // Process all request arguments into a hashmap
    for(int i = 0; i<argLines.size(); i++){
      
      System.out.println(argLines.get(i));

      // Divide argument line into argument, value. Ensure non-malformed input if so, skip line 
      String[] currentLineTokens = argLines.get(i).split(":");
      if(currentLineTokens.length != 2){this.hasError = true; continue;}

      // Capitalize argument since args are specified to be case insensitive
      String currentArgument = currentLineTokens[0].toUpperCase();
      // There is an optional space possible following the ':' so we must strip the value of any spaces
      String currentValue = currentLineTokens[1].replaceAll("//s", "");
      
      headers.put(currentArgument, currentValue); 

    }
  }

}
