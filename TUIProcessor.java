class TUIProcessor {

  private boolean isRunning;
  private ConnectionHandler handler;
  private enum State{
    INITIAL,
    CONNECTION,
  }
  private State currentState;
  private Connection currentConnection;

  TUIProcessor(ConnectionHandler handler){

    isRunning = true;
    this.handler = handler;
    currentState = State.INITIAL;
    currentConnection = null;

  }

  public void parse(String input){

    String output;
    // Shut down program if command is received
    if(input.toUpperCase().equals("SHUTDOWN")){

      handler.endProgram();
      this.isRunning = false;

    }

    // Determine how to process commands based on state
    switch(currentState){

      case INITIAL:

        output = processInputInitial(input.toUpperCase());
        break;

      case CONNECTION:

        output = processInputConnection(input.toUpperCase());
        break;

      default:

    }

    System.out.print(output);

  }

  // Process commands within initial state
  private String processInputInitial(String input){

    String splitInput = input.split(" ");

    String output;

    switch(splitInput[0]){

      case "STATUS":

        output = "\n---CURRENT SERVER STATUS---\n"; 
        if(this.handler.isRunning()){
          output += "\nONLINE";
        } else{
          output += "\nSHUTTING DOWN";
        }

        output += ("\nCurrently Connected: " + this.handler.getActiveConnections());
        output += ("\nConnections serviced in aggregate: " + this.handler.getTotalConnections());

        break;


      case "SEE":

        try{

          this.currentConnection = handler.getConnection(Integer.parseInt(splitInput[1]));
          if(this.currentConnection != null){

            this.currentStatus = Status.CONNECTION;
            output = "\n Context Switched to Connection " + splitInput[1];

          } else{
            output = "\n Invalid Connection Identifier - Retype Command";
            break;
          }

        } catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
          output = "\n Malformed Input - Retype Command";
          break;
        }

      default:

        output = "\n Invalid Command";

    }

    return(output);

  }

  private String processInputConnection(String input){

    String output;

    switch(input){
      
      case "STATUS":

        output = "\n---CURRENT CONNECTION STATUS---\n";

    } 

    return(output);

  }

  public boolean isRunning(){

    return(this.isRunning);

  }
}
