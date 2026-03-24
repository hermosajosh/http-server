import java.io.*;
import java.util.*;
import java.net.*;

class Connection extends Thread {

  Socket client;
  String path;

  Connection(Socket client, String path) throws SocketException{

    this.client = client;  
    this.path = path;

    // Set thread priority level to one below the default to ensure the main thread and RequestParser thread
    // are not interrupted or slowed down by too many concurrent connections

    setPriority(NORM_PRIORITY - 1);

    start();

  }

  public void run(){

    System.out.println("Connection Established");

    try{

      boolean closeAfterMsg = false;

      // Read in data from the socket, specify ISO8859-1 character encoding to abide by 
      // HTTP RFC specifications
      BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream(), "8859_1") ); 

      // Create a channel to write data to the socket
      OutputStream out = client.getOutputStream();

      // Wrap the output channel in an an encoder to abide by HTTP RFC specifications
      PrintWriter pout = new PrintWriter( new OutputStreamWriter(out, "8859_1"), true );

      while(!closeAfterMsg){

        // Save the current request for processing, print it to standard output for debugging purposes
        String request = in.readLine();
        // Prepare an ArrayList to store header arguments from the request
        ArrayList<String> args = new ArrayList<>(10);

        System.out.println( "Request: " + request);

        while(true){

          String line = in.readLine();
          if(line == null){
            System.out.println("Client Timeout, Closing Connection.");
            closeAfterMsg = true;
            break;
          }
          if(line.equals("")){
            
            HTTPRequest req = new HTTPRequest(request, args, this.path);
            HTTPResponse res = new HTTPResponse(req);
            closeAfterMsg = (!req.keepAlive() || res.shouldClose());
            res.process(out);
            break;

          } else{ args.add(line); }
        }
      }
      
      System.out.println("Connection Closed");
      client.close();
      
    } catch (IOException e) {

      System.err.println( "I/O Error " + e);

    }


  }

}
