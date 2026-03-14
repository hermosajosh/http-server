public Class MyWebServer {

  private static final String SERVER_NAME = "MyWebServer"; 
  
  public static void main(String[] args) throws IOException{
    
    // Initiate Web Server
    int port = Integer.parseInt(args[0]);
    String rootPath = args[1];

    //Create a RequestParser
    parser = RequestParser(port, rootPath);


    // Shut Down Server
    
  }

}
