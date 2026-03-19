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
          if(line.equals("")){
            
            HTTPRequest req = new HTTPRequest(request, args, this.path);
            HTTPResponse res = new HTTPResponse(req);
            closeAfterMsg = res.getConnectionStatus();
            res.process();
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

  private boolean processRequest(StringTokenizer tokenizedRequest, PrintWriter pout, OutputStream out){
    
    //Process Method
    String method = tokenizedRequest.nextToken(); 
    boolean provideFile;

    switch(method){

      case "GET":

       provideFile = true; 
       break;

      case "HEAD":

       provideFile = false;
       break;

      default:
        
        // Check if request abides by HTTP syntax specifications, in which case throw a 501
        // If request does not (special characters or lowercase letters) throw a 400

        if(method.matches("[A-Z]+")){
          
          pout.println( "501 Not Implemented");

        } else {pout.println( "400 Bad Request" );}

        return(true);

    }

    // Process URL
    
    String filePath;
    String host;

    // Handle AbsoluteURI Request
    if((filePath = tokenizedRequest.nextToken()).startsWith("http")){
      
      String[] splitPath = filePath.split("/", 4);
      
      // Not needed, but I extract host information from absolute URI requests just for
      // the sake of abiding by the HTTP specifications in the case this Web Server is ever
      // expanded to full functionality
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

    byte[] data;
    boolean wasFound;

    try{
        
      FileInputStream file = new FileInputStream(completeResourcePath);
      data = new byte[file.available()];
      file.read(data);
      
      wasFound = true;

    } catch (FileNotFoundException e){
      pout.println( "404 Object Not Found" );

      // Simple HTML to display
      String errorPage = "<body style="text-align:center"><h1>404</h1><p>Not Found</p></body>";
      //Convert html into byte data
      data = errorPage.getBytes(StandardCharsets.ISO_8859_1);
      
      wasFound = false;

    } catch (IOException e){System.err.println(e);}

    

    return(false);

  }

}
