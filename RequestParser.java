import java.net.*;

Class RequestParser extends Thread {

  private String host;
  private String path;
  private int port;

  private bool isRunning;
  private int activeConnections;
  
  // Constructor
  RequestParser(String rootPath, int port){
    
    this.host = hostname;
    this.path = rootPath;
    this.port = port;
    private Socket requestSocket = Socket(null, this.port);

    start();

  }

  // Thread task
  private void run(){
    
    //Wait for incoming requests
    while(this.isRunning){



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
