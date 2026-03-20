import java.io.*;
public class MyWebServer {

  public static final String SERVER_NAME = "MyWebServer"; 
  
  public static void main(String[] args) throws IOException{
    
    // Initiate Web Server
    int port = Integer.parseInt(args[0]);
    String rootPath = args[1];

    boolean isRunning = true;

    // Create a RequestParser
    ConnectionHandler handler = new ConnectionHandler(port, rootPath);

    System.out.println("Server running on port " + port);
    // Loop to allow user input for info/debugging
    while(isRunning){
      

      


    }
    // Shut Down Server
    
  }

}
