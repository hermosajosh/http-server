import java.io.*;
import java.util.*;
import java.net.*;

class Connection extends Thread {

  Socket client;

  Connection(Socket client) throws SocketException{

    this.client = client;  
    
    // Set thread priority level to one below the default to ensure the main thread and RequestParser thread
    // are not interrupted or slowed down by too many concurrent connections

    setPriority(NORM_PRIORITY - 1);
    
    start();

  }

  public void run(){

    System.out.println("Connection Established");

    try{
      
      // Read in data from the socket, specify ISO8859-1 character encoding to abide by 
      // HTTP RFC specifications
      BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream(), "8859_1") ); 

      // Create a channel to write data to the socket
      OutputStream out = client.getOutputStream();

      // Wrap the output channel in an an encoder to abide by HTTP RFC specifications
      PrintWriter pout = new PrintWriter( new OutputStreamWriter(out, "8859_1"), true );
      
      // Save the current request for processing, print it to standard output for debugging purposes
      String request = in.readLine();
      System.out.println( "Request: " + request);

      // Process the request
      StringTokenizer st = new StringTokenizer( request );

    } catch (IOException e) {
      
      System.err.println( "I/O Error " + e);

    }


  }

}
