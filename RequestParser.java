import java.net.*;
import java.io.*;

class RequestParser extends Thread {

  private String host;
  private String path;
  private int port;

  private boolean isRunning;
  private int activeConnections;

  private ServerSocket requestSocket;

  // Constructor
  RequestParser(int port, String rootPath) throws IOException{

    this.path = rootPath;
    this.port = port;
    this.requestSocket = new ServerSocket(this.port);

    this.isRunning = true;
    start();

  }

  // Thread task
  public void run(){

    System.out.println("Listener Active.");
    //Wait for incoming requests
    try {

      while(this.isRunning){

        new Connection(requestSocket.accept());

      }


    } catch (IOException e){
      System.err.println(e);
    } 

  }

  public void endProgram(){

    this.isRunning = false;

  }

  public int getActiveConnections(){

    return (this.activeConnections);

  }

  public void decrementActiveConnections(){

    this.activeConnections--;

  }

}
