import java.io.*;
import java.util.*;


// TO-DO list:
// Figure out parsing issue with trailing '/' being optional for URLs
// Determine content-type adaptively depending on file
// Fix date parsing and 304 errors
// Create a server-side TUI

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

    // Create scanner to listen for command line input from user
    Scanner inputScanner = new scanner(System.in);

    // Create TUI object to parse command line input
 //   TUIProcessor tui = new TUIProcessor(handler);
    // Loop to allow user input for info/debugging
    while(isRunning){

 //     tui.parse(inputScanner.nextLine());

      isRunning = tui.isRunning();

    }
    
    // Gracefully shut down server (close sockets)
    handler.closeConnections();

  }

}
