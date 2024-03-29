/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author HuaBa
 */
public class Controller {
    
    static final int width = 15;
    static final int height = 20;
    static Mapsimulator mapsimulator;

    static Map map = new Map(width, height);
    static boolean enableTwoTiles = true;
    
    //used in moveToObjective()
    static List<Node> actionSequence = new LinkedList<Node>();
    static int directionX; 
    static int directionY;   
    static boolean bestPathImpossible;
      
    //for full explore
    static boolean justTurnedRight = false;
    static boolean movedAlready = false;
    static int exploredNodeCount;
    static int obstacleCount;
    boolean done;
    static boolean thereAreImpossibleNodesLeft = false;
    static List<Node> priorityNodes = new LinkedList<Node>();
    static boolean leniencyTrigger = false;
    static boolean isUnique = true;
    static List<Node> impossibleNodes = new LinkedList<Node>();
    static List<Node> adjustedImpossibleNodes = new LinkedList<Node>();
    static volatile boolean explorationDone = false;
    static boolean goalReached = false;
 
    //for fastest path
    static int prevX = 0, prevY = 0;
    static int consecutiveForward = 0;
    static volatile boolean fastestPathDone = false;
    
    static int speed;
    static final int sleepTime = 100;
    static boolean turnTwiceFlag;
    static int movementCounter = 0;
    static int lastCaliMovementCounter = 0;
    static int lastCaliLeftMovementCounter = 0;
    static int lastCaliRightMovementCounter = 0;
    static int turnCounter = 0;
    static int [] currentLocation = new int [2];
    static int [] startZoneLocation = new int [2]; //start and end zone are 3x3 tiles
    static int [] goalZoneLocation = new int [2];   //these indicate the middle tile
    static int [] robotStartLocation = new int [2];
    static int [][] nearestUnexplored = new int[9][2];  // [0] indicates the unexplored, rest are neighbours
    static Vector<Node> updateList = new Vector(1); 
    
    //For connection related
    Connection con = new Connection();
    int[] scanResult = new int[5];
    
    public void start(){
        int[] tmp = new int[2];
        int readInt;
        
        initialize();  
        mapsimulator = new Mapsimulator();
             
        con.readData();     //always read the empty string first
        while (true){
            readInt = con.messageRecognition();
            if(readInt == 10){                            
                setRobotLocationAsExplored();
                //setPriorityNodes();
                con.writeData("bExplore start");
                while (con.messageRecognition() != 6){}
                fullExplore(1);
                while (!explorationDone){}
                System.out.println("Out of fullExplore code");
                //while (Connection.writingToAndroid || Connection.writingToArduino) {}
                //con.writeData("bExplore done");
                //while (con.messageRecognition() != 6){}
                while(true){
                    readInt = con.messageRecognition();
                    if (readInt == 11)
                        break;
                }
                System.out.println("Time for fastest Path");
                fastPath(1);
                while (!fastestPathDone){}
                System.out.println("Fastest Path Done");
            }
            else if(readInt == 11){
                //while (Connection.writingToAndroid || Connection.writingToArduino) {}
                //con.writeData("bFastest Path start");
                //while (con.messageRecognition() != 6){}
                fastPath(1);
                while (!fastestPathDone){}
                while (Connection.writingToAndroid || Connection.writingToArduino) {}
                con.writeData("bFastest Path done");
                while (con.messageRecognition() != 6){}
            }         
            //changing the following 3 after initialize() and mapsimulator instantiation might cause problems
            else if(readInt == 7){
                tmp = con.zoneParse();
                setStartZone(tmp[0], tmp[1]);
                con.writeData("bSet start done");
            }
            else if(readInt == 8){
                tmp = con.zoneParse();
                setGoalZone(tmp[0], tmp[1]);
                con.writeData("bSet goal done");
            }
            else if(readInt == 9){
                tmp = con.zoneParse();
                setRobotStartLocation(tmp[0], tmp[1]);
                con.writeData("bSet robot done");
            }

        }
    }
    
    //check if this is the goal zone
    public boolean isGoalState(){
        return (currentLocation[0] == goalZoneLocation[0] && 
                currentLocation[1] == goalZoneLocation[1]);
    }
    
    //initialization
    public void initialize(){          
        int robotX = 1;   //temporarily use these variables
        int robotY = 1;   //robot might not start at start zone, just coincidence
        
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                StateOfMap.exploredMap[i][j] = 0;
                StateOfMap.obstacleMap[i][j] = 0;
           }
        }
        setRobotStartLocation(robotX, robotY);
        setStartZone(1, 1);
        setGoalZone(13, 18);
        
    }
    public void setStartZone(int x, int y){       
        //first reset
        if (startZoneLocation[0] != 0 && startZoneLocation[1] != 0){
            for (int i = startZoneLocation[0] - 1; i <= startZoneLocation[0] + 1; i++){
                for(int j = startZoneLocation[1] - 1; j <= startZoneLocation[1] + 1; j++){
                    Node node = map.getNode(i,j);
                    node.setIsExplored(false);  
                    StateOfMap.setExploredTile(i, j, 0);
                }
            }
        }
  
        startZoneLocation[0] = x;
        startZoneLocation[1] = y;
        for (int i = x-1; i <= x+1; i++){
            for(int j = y-1; j <= y+1; j++){
                Node node = map.getNode(i,j);
                node.setIsExplored(true);  
                StateOfMap.setExploredTile(i, j, 1);
            }
        }
    }
    public void setGoalZone(int x, int y){
        //first reset
        if (goalZoneLocation[0] != 0 && goalZoneLocation[1] != 0){
            for (int i = goalZoneLocation[0] - 1; i <= goalZoneLocation[0] + 1; i++){
                for(int j = goalZoneLocation[1] - 1; j <= goalZoneLocation[1] + 1; j++){
                    Node node = map.getNode(i,j);
                    node.setIsExplored(false);  
                    StateOfMap.setExploredTile(i, j, 0);
                }
            }
        }
        
        goalZoneLocation[0] = x;
        goalZoneLocation[1] = y;
        for (int i = x-1; i <= x+1; i++){
            for(int j = y-1; j <= y+1; j++){
                Node node = map.getNode(i,j);
                node.setIsExplored(true);
                StateOfMap.setExploredTile(i, j, 1);
            }
        }
    }
    public void setRobotStartLocation(int x, int y){
        robotStartLocation[0] = x;
        robotStartLocation[1] = y;
        
        Robot.defineRobotPosition(x, y + 1);   
        
        currentLocation[0] = robotStartLocation[0];
        currentLocation[1] = robotStartLocation[1];
    }
    public void setRobotLocationAsExplored(){
        StateOfMap.setExploredTile(Robot.R1X, Robot.R1Y, 1);
        StateOfMap.setExploredTile(Robot.R2X, Robot.R2Y, 1);
        StateOfMap.setExploredTile(Robot.R3X, Robot.R3Y, 1);
        StateOfMap.setExploredTile(Robot.R4X, Robot.R4Y, 1);
        StateOfMap.setExploredTile(Robot.R5X, Robot.R5Y, 1);
        StateOfMap.setExploredTile(Robot.R6X, Robot.R6Y, 1);
        StateOfMap.setExploredTile(Robot.R7X, Robot.R7Y, 1);
        StateOfMap.setExploredTile(Robot.R8X, Robot.R8Y, 1);
        StateOfMap.setExploredTile(Robot.R9X, Robot.R9Y, 1);  
        
        StateOfMap.updateDescriptor(Robot.R1X, Robot.R1Y, 1);
        StateOfMap.updateDescriptor(Robot.R2X, Robot.R2Y, 1);
        StateOfMap.updateDescriptor(Robot.R3X, Robot.R3Y, 1);
        StateOfMap.updateDescriptor(Robot.R4X, Robot.R4Y, 1);
        StateOfMap.updateDescriptor(Robot.R5X, Robot.R5Y, 1);
        StateOfMap.updateDescriptor(Robot.R6X, Robot.R6Y, 1);
        StateOfMap.updateDescriptor(Robot.R7X, Robot.R7Y, 1);
        StateOfMap.updateDescriptor(Robot.R8X, Robot.R8Y, 1);
        StateOfMap.updateDescriptor(Robot.R9X, Robot.R9Y, 1);
    }
    public void setPriorityNodes(){
        
        for (int i = 2; i < 14; i++)
            priorityNodes.add(new Node(i,1));
        for (int i = 2; i < 19; i++)
            priorityNodes.add(new Node(13,i));
        for (int i = 12; i > 0; i--)
            priorityNodes.add(new Node(i,18));
        for (int i = 17; i > 3; i--)
            priorityNodes.add(new Node(1,i));
        
        
//        for (int i = 2; i < 19; i++)
//            priorityNodes.add(new Node(1,i));
//        for (int i = 2; i < 14; i++)
//            priorityNodes.add(new Node(i,18));
//        for (int i = 17; i > 0; i--)
//            priorityNodes.add(new Node(13,i));
//        for (int i = 12; i > 3; i--)
//            priorityNodes.add(new Node(i,1));
    }
    //elementary behaviours
    //determines where the robot should turn based on current direction and objective direction, then execute
    public void turn(int HEADEDTOWARDS) {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                switch (HEADEDTOWARDS) {
                    case Direction.DIRECTION_RIGHT:
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_DOWN:
                        turnTwiceFlag = true;
                        executeTurn(Direction.TURN_RIGHT);
                        //executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_LEFT:
                        executeTurn(Direction.TURN_LEFT);
                        break;
                }
                break;
            case Direction.DIRECTION_DOWN:
                switch (HEADEDTOWARDS) {
                    case Direction.DIRECTION_UP:
                        turnTwiceFlag = true;
                        executeTurn(Direction.TURN_RIGHT);
                        //executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_RIGHT:
                        executeTurn(Direction.TURN_LEFT);
                        break;
                    case Direction.DIRECTION_LEFT:
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                switch (HEADEDTOWARDS) {
                    case Direction.DIRECTION_UP:
                        executeTurn(Direction.TURN_LEFT);
                        break;
                    case Direction.DIRECTION_DOWN:
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_LEFT:
                        turnTwiceFlag = true;
                        //executeTurn(Direction.TURN_RIGHT);
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                }
                break;
            case Direction.DIRECTION_LEFT:
                switch (HEADEDTOWARDS) {
                    case Direction.DIRECTION_UP:
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_RIGHT:
                        turnTwiceFlag = true;
                        //executeTurn(Direction.TURN_RIGHT);
                        executeTurn(Direction.TURN_RIGHT);
                        break;
                    case Direction.DIRECTION_DOWN:
                        executeTurn(Direction.TURN_LEFT);
                        break;
                }
                break;
        }
    }
    
    //an absolute turn towards the direction you want relative to the robot
    //if you're facing down, turn(right) means you'll face left, even if you meant to be headed right
    public void executeTurn(int direction){
        System.out.println("TURN " + (direction == Direction.TURN_LEFT ? "LEFT" : "RIGHT"));
        turnCounter++;
        switch (direction) {
            case Direction.TURN_RIGHT:
                
                while (Connection.writingToAndroid) {}
                Connection.writingToArduino = true;
                con.writeData("ad|"); //Arduino turn right                         
                switch (Direction.CUR_DIRECTION) {
                    case Direction.DIRECTION_UP:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_RIGHT;
                        Robot.defineRobotPosition(Robot.R2X + 1, Robot.R2Y - 1); //after turning, the robots position needs to be redefined
                        break; 
                    case Direction.DIRECTION_DOWN:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_LEFT;
                        Robot.defineRobotPosition(Robot.R2X - 1, Robot.R2Y + 1);
                        break;
                    case Direction.DIRECTION_RIGHT:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_DOWN;
                        Robot.defineRobotPosition(Robot.R2X - 1, Robot.R2Y - 1);
                        break;
                    case Direction.DIRECTION_LEFT:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_UP;
                        Robot.defineRobotPosition(Robot.R2X + 1, Robot.R2Y + 1);
                        break;
                }              
                while (true){
                    //after writing put switch statement in between, before reading
                    //need to wait for response from Arduino anyway, might as well process while reading
                    if ( con.messageRecognition() == 2 ){
                        Connection.writingToArduino = false;
                        break;
                    }
                }
                break;
                
            case Direction.TURN_LEFT:
                
                while (Connection.writingToAndroid) {}
                Connection.writingToArduino = true;
                con.writeData("aa|"); //Arduino turn left
                switch (Direction.CUR_DIRECTION) {
                    case Direction.DIRECTION_UP:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_LEFT;
                        Robot.defineRobotPosition(Robot.R2X - 1, Robot.R2Y - 1);
                        break;
                    case Direction.DIRECTION_DOWN:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_RIGHT;
                        Robot.defineRobotPosition(Robot.R2X + 1, Robot.R2Y + 1);
                        break;
                    case Direction.DIRECTION_RIGHT:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_UP;
                        Robot.defineRobotPosition(Robot.R2X - 1, Robot.R2Y + 1);
                        break;
                    case Direction.DIRECTION_LEFT:
                        Direction.CUR_DIRECTION = Direction.DIRECTION_DOWN;
                        Robot.defineRobotPosition(Robot.R2X + 1, Robot.R2Y - 1);
                        break;
                }              
                while (true){
                    if ( con.messageRecognition() == 3 ){
                        Connection.writingToArduino = false;
                        break;
                    }
                }
                break;
        }
    }
    //go , stop() is not needed anymore because specify exactly how much to move forward
    public void forward(int tileCount){
        boolean notOne = false;
        String howManySteps = "default";
        int x = Robot.R2X;
        int y = Robot.R2Y;
        
        if (tileCount != 1){
            notOne = true;            
            if (tileCount > 9){
                switch(tileCount){
                    case 10:
                        howManySteps = "e";
                        break;
                    case 11:
                        howManySteps = "f";
                        break;
                    case 12:
                        howManySteps = "g";
                        break;
                    case 13:
                        howManySteps = "h";
                        break;
                    case 14:
                        howManySteps = "i";
                        break;
                    case 15:
                        howManySteps = "j";
                        break;
                    case 16:
                        howManySteps = "k";
                        break;
                    case 17:
                        howManySteps = "l";
                        break;
                    default:
                        howManySteps = "e";
                        break;
                }          
            }
            else
                howManySteps = String.valueOf(tileCount);
        }
        
        while (Connection.writingToAndroid) {}
        Connection.writingToArduino = true;
        if (notOne)
            con.writeData("aw"+ howManySteps + "|"); //Arduino move forward for tileCount, not specified as of yet cause hardware programming not ready
        else
            con.writeData("aw|");
        for (int i = 0; i < tileCount; i++) {
            switch (Direction.CUR_DIRECTION) {
                case Direction.DIRECTION_UP:
                    y++;
                    break;
                case Direction.DIRECTION_DOWN:
                    y--;
                    break;
                case Direction.DIRECTION_RIGHT:
                    x++;
                    break;
                case Direction.DIRECTION_LEFT:
                    x--;
                    break;
            }
            Robot.defineRobotPosition(x, y);
            currentLocation[0] = Robot.R9X;
            currentLocation[1] = Robot.R9Y;
            System.out.println(currentLocation[0] + " " + currentLocation[1]);
            movementCounter++;   
            if (isGoalState()){      //for percentage explore, doesn't affect fullExplore
                goalReached = true;
                System.out.println("Is GOAL!");
            }
        }
        //just like turn, process then busy wait till Arduino response
        while (true){
            if ( con.messageRecognition() == 1 ){
                Connection.writingToArduino = false;
                break;
            }
        }
    }
    
    public void calibrate(){
        
        if (StateOfMap.canCalibrateRight()){      
               
            while (Connection.writingToAndroid) {}
            Connection.writingToArduino = true;
            con.writeData("ap|");
            while (true){
                if (con.messageRecognition() == 5){
                    Connection.writingToArduino = false;
                    break;
                }
            }
            lastCaliMovementCounter = movementCounter;
            
            if (movementCounter - lastCaliRightMovementCounter > 4 ){
                executeTurn(Direction.TURN_RIGHT);
                while (Connection.writingToAndroid) {}
                Connection.writingToArduino = true;
                con.writeData("ap|");
                while (true){
                    if (con.messageRecognition() == 5){
                        Connection.writingToArduino = false;
                        break;
                    }
                }
                executeTurn(Direction.TURN_LEFT);  
                lastCaliRightMovementCounter = movementCounter;
                lastCaliMovementCounter = movementCounter;
            }
        }
        else if(StateOfMap.canCalibrateFront()){
            while (Connection.writingToAndroid) {}
            Connection.writingToArduino = true;
            con.writeData("ap|");
            while (true){
                if (con.messageRecognition() == 5){
                    Connection.writingToArduino = false;
                    break;
                }
            }
            lastCaliMovementCounter = movementCounter;
        }
        if (StateOfMap.canCalibrateLeft()){
            if (movementCounter - lastCaliLeftMovementCounter > 4 || done == true){
                executeTurn(Direction.TURN_LEFT);

                while (Connection.writingToAndroid) {}
                Connection.writingToArduino = true;
                con.writeData("ap|");
                while (true){
                    if (con.messageRecognition() == 5){
                        Connection.writingToArduino = false;
                        break;
                    }
                }
                lastCaliMovementCounter = movementCounter;
                lastCaliLeftMovementCounter = movementCounter;
                
                executeTurn(Direction.TURN_RIGHT);
            }
        }
    }
    
    //scan for obstacles
    public int scan(){
        while (Connection.writingToAndroid) {}
        Connection.writingToArduino = true;
        con.writeData("ac|"); //Arduino scan
        while (true){        //Wait till scanning finishes
            int tmp = con.messageRecognition();
            if (tmp== 12){
                System.out.println(12);
                Connection.writingToArduino = false;
                break;
            }
            System.out.println(tmp);
        }
        scanResult = con.sensorDataParse();
        
        int scannedNodes = 0;
        //isObstacle() returns a boolean, setObstacleTile() accepts int, so if true = 1, false = 0 
        if (StateOfMap.isValidTile(Robot.Tile1X, Robot.Tile1Y)){
            //if (!StateOfMap.isExploredTile(Robot.Tile1X, Robot.Tile1Y)){
                StateOfMap.setExploredTile(Robot.Tile1X, Robot.Tile1Y, 1);
                if (scanResult[1] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile1X, Robot.Tile1Y, 1);
                else
                    StateOfMap.setObstacleTile(Robot.Tile1X, Robot.Tile1Y, 0);
                StateOfMap.updateDescriptor(Robot.Tile1X, Robot.Tile1Y, 1);
                scannedNodes++;
            //}
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile1EX, Robot.Tile1EY) && StateOfMap.isExploredTile(Robot.Tile1X, Robot.Tile1Y)){
                    //if (!StateOfMap.isExploredTile(Robot.Tile1EX, Robot.Tile1EY) && !StateOfMap.isObstacleTile(Robot.Tile1X, Robot.Tile1Y)){
                    if (!StateOfMap.isObstacleTile(Robot.Tile1X, Robot.Tile1Y)){
                        StateOfMap.setExploredTile(Robot.Tile1EX, Robot.Tile1EY, 1);
                        if (scanResult[1] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile1EX, Robot.Tile1EY, 1);
                        else
                            StateOfMap.setObstacleTile(Robot.Tile1EX, Robot.Tile1EY, 0);
                        StateOfMap.updateDescriptor(Robot.Tile1EX, Robot.Tile1EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile2X, Robot.Tile2Y)){
            //if (!StateOfMap.isExploredTile(Robot.Tile2X, Robot.Tile2Y)){
                StateOfMap.setExploredTile(Robot.Tile2X, Robot.Tile2Y, 1);
                if (scanResult[2] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile2X, Robot.Tile2Y, 1);
                else
                    StateOfMap.setObstacleTile(Robot.Tile2X, Robot.Tile2Y, 0);
                StateOfMap.updateDescriptor(Robot.Tile2X, Robot.Tile2Y, 1);
                scannedNodes++;                
            //}
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile2EX, Robot.Tile2EY) && StateOfMap.isExploredTile(Robot.Tile2X, Robot.Tile2Y)){
                    //if (!StateOfMap.isExploredTile(Robot.Tile2EX, Robot.Tile2EY) && !StateOfMap.isObstacleTile(Robot.Tile2X, Robot.Tile2Y)){
                    if (!StateOfMap.isObstacleTile(Robot.Tile2X, Robot.Tile2Y)){
                        StateOfMap.setExploredTile(Robot.Tile2EX, Robot.Tile2EY, 1);
                        if (scanResult[2] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile2EX, Robot.Tile2EY, 1);
                        else
                            StateOfMap.setObstacleTile(Robot.Tile2EX, Robot.Tile2EY, 0);
                        StateOfMap.updateDescriptor(Robot.Tile2EX, Robot.Tile2EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile3X, Robot.Tile3Y)){
            //if (!StateOfMap.isExploredTile(Robot.Tile3X, Robot.Tile3Y)){
                StateOfMap.setExploredTile(Robot.Tile3X, Robot.Tile3Y, 1);
                if (scanResult[3] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile3X, Robot.Tile3Y, 1);
                else
                    StateOfMap.setObstacleTile(Robot.Tile3X, Robot.Tile3Y, 0);
                StateOfMap.updateDescriptor(Robot.Tile3X, Robot.Tile3Y, 1);
                scannedNodes++;                
            //}
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile3EX, Robot.Tile3EY) && StateOfMap.isExploredTile(Robot.Tile3X, Robot.Tile3Y)){
                    //if (!StateOfMap.isExploredTile(Robot.Tile3EX, Robot.Tile3EY) && !StateOfMap.isObstacleTile(Robot.Tile3X, Robot.Tile3Y)){
                    if (!StateOfMap.isObstacleTile(Robot.Tile3X, Robot.Tile3Y)){
                        StateOfMap.setExploredTile(Robot.Tile3EX, Robot.Tile3EY, 1);
                        if (scanResult[3] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile3EX, Robot.Tile3EY, 1);
                        else
                            StateOfMap.setObstacleTile(Robot.Tile3EX, Robot.Tile3EY, 0);
                        StateOfMap.updateDescriptor(Robot.Tile3EX, Robot.Tile3EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile5X, Robot.Tile5Y)){
            //if (!StateOfMap.isExploredTile(Robot.Tile5X, Robot.Tile5Y)){ 
                StateOfMap.setExploredTile(Robot.Tile5X, Robot.Tile5Y, 1);
                if (scanResult[0] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile5X, Robot.Tile5Y, 1);
                else
                    StateOfMap.setObstacleTile(Robot.Tile5X, Robot.Tile5Y, 0);
                StateOfMap.updateDescriptor(Robot.Tile5X, Robot.Tile5Y, 1);
                scannedNodes++;
            //}
                
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile5EX, Robot.Tile5EY) && StateOfMap.isExploredTile(Robot.Tile5X, Robot.Tile5Y)){
                    //if (!StateOfMap.isExploredTile(Robot.Tile5EX, Robot.Tile5EY) && !StateOfMap.isObstacleTile(Robot.Tile5X, Robot.Tile5Y)){
                    if (!StateOfMap.isObstacleTile(Robot.Tile5X, Robot.Tile5Y)){
                        StateOfMap.setExploredTile(Robot.Tile5EX, Robot.Tile5EY, 1);
                        if (scanResult[0] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile5EX, Robot.Tile5EY, 1);
                        else
                            StateOfMap.setObstacleTile(Robot.Tile5EX, Robot.Tile5EY, 0);
                        StateOfMap.updateDescriptor(Robot.Tile5EX, Robot.Tile5EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile8X, Robot.Tile8Y)){
            //if (!StateOfMap.isExploredTile(Robot.Tile8X, Robot.Tile8Y)){ 
                StateOfMap.setExploredTile(Robot.Tile8X, Robot.Tile8Y, 1);
                if (scanResult[4] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile8X, Robot.Tile8Y, 1);
                else
                    StateOfMap.setObstacleTile(Robot.Tile8X, Robot.Tile8Y, 0);
                StateOfMap.updateDescriptor(Robot.Tile8X, Robot.Tile8Y, 1);
                scannedNodes++;
            //}
                
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile8EX, Robot.Tile8EY) && StateOfMap.isExploredTile(Robot.Tile8X, Robot.Tile8Y)){
                    //if (!StateOfMap.isExploredTile(Robot.Tile8EX, Robot.Tile8EY) && !StateOfMap.isObstacleTile(Robot.Tile8X, Robot.Tile8Y)){
                    if (!StateOfMap.isObstacleTile(Robot.Tile8X, Robot.Tile8Y)){
                        StateOfMap.setExploredTile(Robot.Tile8EX, Robot.Tile8EY, 1);
                        if (scanResult[4] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile8EX, Robot.Tile8EY, 1);
                        else
                            StateOfMap.setObstacleTile(Robot.Tile8EX, Robot.Tile8EY, 0);
                        StateOfMap.updateDescriptor(Robot.Tile8EX, Robot.Tile8EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (movementCounter - lastCaliMovementCounter >= 1){  //will change condition later
            if (!explorationDone)
                calibrate();
        }
        return scannedNodes; 
    }
    
    //This method used only for determineNearestUnexplored(), but same logic as Node.sethCost()
    private int heuristicFunc(int[] objective){
        for (Node tmp : impossibleNodes ) {
            if ( tmp.getX() == objective[0] && tmp.getY() == objective[1] )
                return 5000;
        }    
        if (!goalReached)
            return Math.abs( goalZoneLocation[0] - objective[0] ) + Math.abs( goalZoneLocation[1] - objective[1] );
        else
            return Math.abs( currentLocation[0] - objective[0] ) + Math.abs( currentLocation[1] - objective[1] );
    }
       
    public int[] determineNearestUnexplored(){
        int[] tmp = new int[2]; 
        int[] objective = new int[2];
        int lowestDistance = 9999;  //random initial super high value
        int distance = 0;
        
        Node priority;
        if (!priorityNodes.isEmpty()){
            priority = priorityNodes.get(0);
            priorityNodes.remove(0);
            objective[0] = priority.getX();
            objective[1] = priority.getY();
            return objective;
        }
        
        //for each unexplored node calculate only h() cost, smallest wins
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                if( !StateOfMap.isExploredTile(i, j) ){
                    tmp[0] = i;
                    tmp[1] = j;
                    distance = heuristicFunc(tmp); //use just h value to determine, greedy but fast
                    if (distance < lowestDistance){
                        lowestDistance = distance;
                        objective[0] = tmp[0];
                        objective[1] = tmp[1];
                    }
                }
            }
        }
        return objective;
    }
    
    public int[][] nearestUnexploredNeighbours(int[] nearestUnexplored){
        int[][] tmp = new int[9][2];
        int x = nearestUnexplored[0];  //the center
        int y = nearestUnexplored[1];
        tmp[0][0] = x;
        tmp[0][1] = y;
        
        for (int i = 1; i < 9; i++){
            tmp[i][0] = -1;
            tmp[i][1] = -1;
        }
        if (StateOfMap.isValidTile(x + 1, y)){  //right neighbour
            tmp[1][0] = x + 1;
            tmp[1][1] = y;
        }
        if (StateOfMap.isValidTile(x - 1, y)){  //left neighbour
            tmp[2][0] = x - 1;
            tmp[2][1] = y;
        }
        if (StateOfMap.isValidTile(x, y + 1)){  //top neighbour
            tmp[3][0] = x;
            tmp[3][1] = y + 1;
        }
        if (StateOfMap.isValidTile(x, y - 1)){  //bottom neighbour
            tmp[4][0] = x;
            tmp[4][1] = y - 1;
        }
        if (StateOfMap.isValidTile(x + 1, y + 1)){  //top right neighbour
            tmp[5][0] = x + 1;
            tmp[5][1] = y + 1;
        }
        if (StateOfMap.isValidTile(x + 1, y - 1)){  //bottom right neighbour
            tmp[6][0] = x + 1;
            tmp[6][1] = y - 1;
        }
        if (StateOfMap.isValidTile(x - 1, y + 1)){  //top left neighbour
            tmp[7][0] = x - 1;
            tmp[7][1] = y + 1;
        }
        if (StateOfMap.isValidTile(x - 1, y - 1)){  //bottom left neighbour
            tmp[8][0] = x - 1;
            tmp[8][1] = y - 1;
        }
        return tmp;
    }
    
    public void explore (String percentage, int speed, String time){
        switch(time){
            case "-":
                switch(percentage){
//                    case "10%": percentageExplore(10, speed);
//                        break;
//                    case "20%": percentageExplore(20, speed);
//                        break;
//                    case "30%": percentageExplore(30, speed);
//                        break;
//                    case "40%": percentageExplore(40, speed);
//                        break;
//                    case "50%": percentageExplore(50, speed);
//                        break;
//                    case "60%": percentageExplore(60, speed);
//                        break;
//                    case "70%": percentageExplore(70, speed);
//                        break;
//                    case "80%": percentageExplore(80, speed);
//                        break;
//                    case "90%": percentageExplore(90, speed);
//                        break;
                    case "100%": fullExplore(speed);
                        break;
                    default: System.out.println("Error");
                        break;
                }
                break;
//            case "30s": timedExplore(30, speed);
//                break;
//            case "60s": timedExplore(60, speed);
//                break;
//            case "120s": timedExplore(120, speed);
//                break;
//            case "180s": timedExplore(180, speed);
//                break;
//            case "240s": timedExplore(240, speed);
//                break;
//            case "300s": timedExplore(300, speed);
//                break;
//            case "360s": timedExplore(360, speed);
//                break;
            default: System.out.println("Error");
                break;            
                
        }
    } 
    //Algorithm designed to guarantee 100% exploration to fulfil checklist
    public void fullExplore(int speed){
        obstacleCount = 0;
        done = false;
        this.speed = speed;
        SwingWorker worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() {                            
                scan();
                executeTurn(Direction.TURN_LEFT);
                publishAndSleep();
                calibrate();
                executeTurn(Direction.TURN_RIGHT);
                publishAndSleep();
                executeTurn(Direction.TURN_RIGHT);
                publishAndSleep();
                scan();
                updateExploredAndObstacleCount();
                publishAndSleep();

                calibrate();
                publishAndSleep();
                
                if (!priorityNodes.isEmpty()){
                    while(!priorityNodes.isEmpty()){
                        if (!bestPathImpossible || impossibleNodes.size() > 60){
                            if (impossibleNodes.size() > 60)
                                leniencyTrigger = true;
                            else
                                leniencyTrigger = false;
                            impossibleNodes.clear();
                        }
                        moveToObjective(determineNearestUnexplored());
                    }
                }
                else{
                    while(true){
                        if (StateOfMap.rightIsTraversable() && !justTurnedRight){
                            executeTurn(Direction.TURN_RIGHT);
                            justTurnedRight = true;
                        }      
                        else if (!StateOfMap.frontIsTraversable()){
                            executeTurn(Direction.TURN_LEFT);
                        }
                        else{
                            forward(1);
                            justTurnedRight = false;
                            movedAlready = true;
                        }
                        publishAndSleep();
                        scan();
                        updateExploredAndObstacleCount();
                        publishAndSleep();
                        if (movedAlready && currentLocation[0] == startZoneLocation[0] && currentLocation[1] == startZoneLocation[1]){
                            if ( exploredNodeCount >= 250)
                                break;
                        }
                    }
                    if ( exploredNodeCount >= 250)
                        done = true;
                }

                while (!done){
                    while (true){
                        if (Connection.STOP){ //stop exploration and return to start zone
                            for (int i = 1; i < 9; i++){
                                nearestUnexplored[i][0] = -1;
                                nearestUnexplored[i][1] = -1;
                            }
                            Connection.STOP = false;
                            moveToObjective(startZoneLocation);
                            return 1;
                        }
                            
                        if (!bestPathImpossible || impossibleNodes.size() > 60){
                            if (impossibleNodes.size() > 60)
                                leniencyTrigger = true;
                            else
                                leniencyTrigger = false;
                            impossibleNodes.clear();
                        }

                        if (!StateOfMap.frontIsTraversable()){ 
                            break;
                        }
                        else{
                            if (exploredNodeCount == 300){
                                done = true;
                                break;
                            }
                            forward(1);
                            publishAndSleep();
                            scan();
                            publishAndSleep();
                            updateExploredAndObstacleCount();
                            System.out.println("Explored nodes: " + exploredNodeCount);
                        }
                    }

                    if (exploredNodeCount == 300)
                        done = true;
                    
                    else{
                        if ( 300 - exploredNodeCount == impossibleNodes.size() ){
                            thereAreImpossibleNodesLeft = true;
                            impossibleNodes.clear();
                            for (int i = 0; i < 15; i++){
                                for (int j = 0; j < 20; j++){
                                    if(!StateOfMap.isExploredTile(i, j))
                                        impossibleNodes.add(new Node(i,j));
                                }
                            }
                            System.out.println("\nThere are nodes remaining: " + impossibleNodes.size() + "\n");
                            for (Node tmp : impossibleNodes){
                                nearestUnexplored[0][0] = tmp.getX();
                                nearestUnexplored[0][1] = tmp.getY();
                                
                                if (nearestUnexplored[0][0] <= 0 ){
                                    nearestUnexplored[0][0]++;
                                }
                                if (nearestUnexplored[0][1] <= 0 ){
                                    nearestUnexplored[0][1]++;
                                }
                                if (nearestUnexplored[0][0] >= width - 1 ){
                                    nearestUnexplored[0][0]--;
                                }
                                if (nearestUnexplored[0][1] >= height - 1 ){
                                    nearestUnexplored[0][1]--;
                                }
                                
                                for (Node unique: adjustedImpossibleNodes){
                                    if (unique.getX() == nearestUnexplored[0][0] && unique.getY() == nearestUnexplored[0][1])
                                        isUnique = false;
                                }
                                if (isUnique)
                                    adjustedImpossibleNodes.add(new Node(nearestUnexplored[0][0], nearestUnexplored[0][1]));
                                isUnique = true;
                            }
                        }
                        else {
                            if (!goalReached){
                                for (int i = 1; i < 9; i++){
                                    nearestUnexplored[i][0] = -1;
                                    nearestUnexplored[i][1] = -1;
                                }
                                moveToObjective(goalZoneLocation);
                            }
                            
                            nearestUnexplored[0] = determineNearestUnexplored(); 
                            nearestUnexplored = nearestUnexploredNeighbours(nearestUnexplored[0]);
                            System.out.print("Nearest unexplored: " + nearestUnexplored[0][0] + " ");
                            System.out.println(nearestUnexplored[0][1]);
                            moveToObjective(nearestUnexplored[0]);
                        }
                    }
                    
                    if (thereAreImpossibleNodesLeft){
                        int[] tmp = new int[2];
                        
                        System.out.println("Explored nodes: " + exploredNodeCount);
                        for (Node remainingNode: adjustedImpossibleNodes){
                            if (Connection.STOP){ //stop exploration and return to start zone
                                for (int i = 1; i < 9; i++){
                                    nearestUnexplored[i][0] = -1;
                                    nearestUnexplored[i][1] = -1;
                                }
                                Connection.STOP = false;
                                moveToObjective(startZoneLocation);
                                return 1;
                            }
                            System.out.println("Remaning Node: " + remainingNode.getX() + " " + remainingNode.getY());
                            tmp[0] = remainingNode.getX();
                            tmp[1] = remainingNode.getY();
                            nearestUnexplored = nearestUnexploredNeighbours(tmp);
                            moveToObjective(nearestUnexplored[0]);
                            System.out.println("Explored nodes: " + exploredNodeCount);

                            if (exploredNodeCount == 300){
                                done = true;
                                break;
                            }
                        }
                            
                        if (exploredNodeCount == 300){
                            done = true;
                            break;
                        }
                        else {
                            for (int i = 0; i < width; i++){
                                for (int j = 0; j < height; j++){
                                    if (!StateOfMap.isExploredTile(i, j)){
                                        System.out.println("STILL HAVE: " + i + " " + j);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    
                }                
                
                if (done){                        
                    System.out.println("Exploration complete, moving to goal then back to start.");
                    System.out.println("Ignored Nodes: " + (300 - exploredNodeCount));
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);                   
                }
                else{
                    System.out.println("Exploration complete but unreachable nodes present.");
                    System.out.println("Unreachable Nodes: " + (300 - exploredNodeCount));
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);
                }
                
                thereAreImpossibleNodesLeft = false;            
                for (int i = 1; i < 9; i++){
                    nearestUnexplored[i][0] = -1;
                    nearestUnexplored[i][1] = -1;
                }             
                if(!goalReached)
                    moveToObjective(goalZoneLocation);
                if(currentLocation[0] != startZoneLocation[0] && currentLocation[1] != startZoneLocation[1])
                    moveToObjective(startZoneLocation);
                System.out.println("Movements: " + movementCounter);
                System.out.println("Turns: " + turnCounter);
                System.out.println("Done exploration, sleep now");
                
                try{
                TimeUnit.SECONDS.sleep(20);
                }
                catch (Exception e){}
                System.out.println("Calibrate in 10..");
                try{
                TimeUnit.SECONDS.sleep(10);
                }
                catch (Exception e){}
                
                //intense calibration
                calibrate();
//                for (int i = 0; i < 4; i++){
//                    executeTurn(Direction.TURN_RIGHT);
//                    calibrate();
//                }
                
                //determine where to face using the first node in fastest path actionSequence
                    actionSequence = map.findPath(currentLocation, goalZoneLocation); 
                    for (Node s : actionSequence){
                        System.out.println("Action Path: " + s.getX() + " " + s.getY());
                        directionX = s.getX() - Robot.R9X;
                        directionY = s.getY() - Robot.R9Y;

                        if (directionX < 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
                                turn(Direction.DIRECTION_LEFT);
                                publishAndSleep();
                                calibrate();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                    calibrate();
                                }
                            }
                        } else if (directionX > 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
                                turn(Direction.DIRECTION_RIGHT);
                                publishAndSleep(); 
                                calibrate();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                    calibrate();
                                }
                            }
                        } else if (directionY < 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
                                turn(Direction.DIRECTION_DOWN);
                                publishAndSleep();
                                calibrate();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                    calibrate();
                                }
                            }
                        } else if (directionY > 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
                                turn(Direction.DIRECTION_UP);
                                publishAndSleep(); 
                                calibrate();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                    calibrate();
                                }
                            }
                        }
                        break;
                    }
                //end determine which direction to face

                System.out.println("Calibration is done.");
                explorationDone = true;
                return 1;
            }
            @Override
            public void process(java.util.List<Integer> chunks){
                try{
                    CZ3004MDPACTUALLY.controller.mapsimulator.contentPanel.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
                    for (Node node : updateList) {
                        StateOfMap.updateDescriptor(node.getX(), node.getY(), 0);
                        updateList.remove(node);
                    }
                    if(Connection.writingToArduino == false){  //might skip writing sometimes which is okay
                        Connection.writingToAndroid = true;
                        saveFile();
                        Connection.writingToAndroid = false;
                    }
                }
                catch(Exception e){
                }
                    
                    
            }
            public void publishAndSleep() {
                publish();
                try{
                    Thread.sleep(sleepTime/speed);
                }
                catch(InterruptedException e){
                    
                }
            }
            public void moveToObjective(int[] objective){ 
                if (currentLocation[0] == objective[0] && currentLocation[1] == objective[1]){
                    System.out.println("Already at objective!");
                    return;
                }
                actionSequence = map.findPath(currentLocation, objective); 
                if (actionSequence == null)
                    System.out.println("null");
                if (actionSequence.isEmpty()){  
                    System.out.println("is empty");
                    if(!thereAreImpossibleNodesLeft){
                        bestPathImpossible = true;                       
                        impossibleNodes.add( new Node(objective[0], objective[1]));
                        System.out.println("Impossible Nodes: " + impossibleNodes.size());
                    }
                }

                    for (Node s : actionSequence){
                        if (Connection.STOP)
                            return;
                        System.out.println("Action Path: " + s.getX() + " " + s.getY());
                        directionX = s.getX() - Robot.R9X;
                        directionY = s.getY() - Robot.R9Y;

                        if (directionX < 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
                                turn(Direction.DIRECTION_LEFT);
                                publishAndSleep();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionX > 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
                                turn(Direction.DIRECTION_RIGHT);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY < 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
                                turn(Direction.DIRECTION_DOWN);
                                publishAndSleep();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY > 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
                                turn(Direction.DIRECTION_UP);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        }

                        if (StateOfMap.frontIsTraversable()) {            
                            forward(1);
                            publishAndSleep();
                            if (exploredNodeCount != 300){
                                scan();
                                publishAndSleep(); 
                            }
                            bestPathImpossible = false;
                            updateExploredAndObstacleCount();
                        } 
                        else {
                            if (thereAreImpossibleNodesLeft)
                                break;
                            bestPathImpossible = true;                       
                            impossibleNodes.add( actionSequence.get(actionSequence.size()-1) );
                            System.out.println("Impossible Nodes: " + impossibleNodes.size());
                            break;
                        }

                    }
                }
                       
        };
        worker.execute();
    
    }
    public void updateExploredAndObstacleCount(){
        int explore = 0;
        int obstacle = 0;
        
        for (int k = 0; k < width; k++){
            for (int l = 0; l < height; l++){
                if (StateOfMap.isExploredTile(k, l))
                    explore++;
                if (StateOfMap.isObstacleTile(k, l))
                    obstacle++;
            }
        }
        exploredNodeCount = explore;
        obstacleCount = obstacle;
    }
             
    public void fastPath(int speed){
        //exploration is done here changed that shit
        
        this.speed = speed;
        explorationDone = true;

        for (int i = 1; i < 9; i++){
            nearestUnexplored[i][0] = -1;
            nearestUnexplored[i][1] = -1;
        }
        moveToObjectiveDemo(goalZoneLocation);
    }
    
    //move to a node assuming there is a path to it in the currently explored space
    public void moveToObjectiveDemo(int[] objective){ 
                             
        actionSequence = map.findPath(currentLocation, objective); 
        for (Node tmp : actionSequence)
            System.out.println("Action Path: " + tmp.getX() + " " + tmp.getY());
        if (actionSequence == null)
            System.out.println("null");
        if (actionSequence.isEmpty()){  
            System.out.println("Path is empty");
        }

        SwingWorker worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() {
                mapsimulator.contentPanel.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);

                prevX = Robot.R9X;
                prevY = Robot.R9Y;
                for (Node s : actionSequence){
                    //System.out.println("Action Path: " + s.getX() + " " + s.getY());
                    directionX = s.getX() - prevX;
                    directionY = s.getY() - prevY;
                    prevX = s.getX();
                    prevY = s.getY();
                                     
                        if (directionX < 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
                                if (consecutiveForward != 0){
                                    forward(consecutiveForward);
                                    consecutiveForward = 1;
                                }
                                turn(Direction.DIRECTION_LEFT);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                            else {
                                consecutiveForward++;
                            }
                        } else if (directionX > 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
                                if (consecutiveForward != 0){
                                    forward(consecutiveForward);
                                    consecutiveForward = 1;
                                }
                                turn(Direction.DIRECTION_RIGHT);
                                publishAndSleep();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                            else {
                                consecutiveForward++;
                            }
                        } else if (directionY < 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
                                if (consecutiveForward != 0){
                                    forward(consecutiveForward);
                                    consecutiveForward = 1;
                                }
                                turn(Direction.DIRECTION_DOWN);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                            else {
                                consecutiveForward++;
                            }
                        } else if (directionY > 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
                                if (consecutiveForward != 0){
                                    forward(consecutiveForward);
                                    consecutiveForward = 1;
                                }
                                turn(Direction.DIRECTION_UP);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                            else {
                                consecutiveForward++;
                            }
                        }                 
                       
                    if (StateOfMap.frontIsTraversable() && consecutiveForward == 0) {            
                        forward(1);
                        publishAndSleep(); 
                    }               
                }
                
                //just in case the objective reached before you had the chance to execute all the delayed forwards
                if (consecutiveForward != 0){            
                    forward(consecutiveForward);
                    publishAndSleep(); 
                }
                fastestPathDone = true;
                return 1;

             }

            @Override
            protected void process(java.util.List<Integer> chunks) {               
                mapsimulator.contentPanel.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
                
            }

            @Override
            protected void done() {
                System.out.println("SHORTEST PATH COMPLETED");
            }

            public void publishAndSleep() {
                publish();
                try{
                    Thread.sleep(sleepTime/speed);
                }
                catch(InterruptedException e){
                    
                }
            }
        };
        worker.execute();
        
    }
               
    public void saveFile() throws IOException{
        String part2String = "";
        String tmpString = "";
        String tmpPart2Str = "";
        String part1String = "";        
        String tmpPart1Str = "";
        for(int j = 0; j< height; j++){
            for(int i = 0; i< width; i++){
                if(StateOfMap.exploredMap[i][j] == 1){
                    if(StateOfMap.obstacleMap[i][j] == 1){
                        part2String+="1";
                    }
                    else{
                        part2String+="0";
                    }//no need of part1String
                }
            }
        }
        for (int i = 0; i < part2String.length()%4; i++){
            part2String = part2String+"0";
        }

        for(int j = 0; j < height; j++){
            for(int i = 0; i< width; i++){                       
                if(StateOfMap.exploredMap[i][j] == 1){
                    part1String+="1";
                }
                else{
                    part1String+="0";
                }
            }
        }
        //padding part
        part1String = "11"+part1String+"11";
        for (int i = 0; i < part2String.length()%4; i++){
            part2String = part2String+"0";
        }
        tmpString = "";
        for (int i = 0 ; i < part2String.length(); i++){
            tmpPart2Str += part2String.charAt(i);
            if(tmpPart2Str.length() == 4){
                int number = Integer.parseInt(tmpPart2Str,2);
                String hexStr = Integer.toString(number,16);
                tmpString += hexStr;
                tmpPart2Str = "";
            }
        }
        part2String = tmpString;

        tmpString = "";
        for (int i = 0 ; i < part1String.length(); i++){
            tmpPart1Str += part1String.charAt(i);
            if(tmpPart1Str.length() == 4){
                int number = Integer.parseInt(tmpPart1Str,2);
                String hexStr = Integer.toString(number,16);
                tmpString += hexStr;
                tmpPart1Str = "";
            }

        }
        part1String = tmpString;

        con.writeData( "bGRID "
                +"15" + " " + "20" + " "
                +Robot.R9Y+" "
                +Robot.R9X+" "
                +Direction.CUR_DIRECTION+" " 
                +part1String+" "
                +part2String);

        while(con.messageRecognition() != 6){}

    }
    
}
