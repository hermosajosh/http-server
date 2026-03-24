import java.net.*;
import java.util.*;
import java.io.*;

class ConnectionHandler extends Thread {

  private String host;
  private String path;
  private int port;

  private boolean isRunning;
  private int activeConnections;
  private int totalConnections;

  private ServerSocket requestSocket;
  private ArrayList<Connection> activeConnections; 

  // Constructor
  ConnectionHandler(int port, String rootPath) throws IOException{

    this.path = rootPath;
    this.port = port;
    this.requestSocket = new ServerSocket(port);

    // This is a relatively low-traffic web server, so I will set the initial Connection
    // list size to 5, which reduces memory usage when at low capacity
    // but increases overhead if the ArrayList needs to be expanded
    this.activeConnections = new ArrayList<>(5);

    this.isRunning = true;
    start();

  }

  // Thread task
  public void run(){

    System.out.println("Listener Active.");

    //Wait for incoming requests
    while(this.isRunning){

      try {

        activeConnections.add(new Connection(requestSocket.accept(), this, this.path));
        totalConnections++;

      } catch (IOException e){

        System.err.println("Connection attempt failed: " + e);

      }

    }


  }

  public void terminateConnection(Connection conn){

    this.activeConnections.remove(conn);

  }

  public Connection getConnection(int num){

    if(num >= this.activeConnections.size()){

      return(null);

    }

    return(this.activeConnections.get(num));

  }

  public void endProgram(){

    this.isRunning = false;

  }

  public int getActiveConnections(){

    return (this.activeConnections.size());

  }

  public boolean isRunning(){

    return(this.isRunning());

  }

  public int getTotalConnections(){

    return(this.totalConnections);
  }

}
