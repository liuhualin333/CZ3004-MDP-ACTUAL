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
    
    final int ROBOTSIZE = 3;
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
    static int exploredNodeCount;
    static int obstacleCount;
    boolean done;  //means explored 100%
    static boolean thereAreImpossibleNodesLeft = false;
    static boolean leniencyTrigger = false;
    static boolean isUnique = true;
    static List<Node> impossibleNodes = new LinkedList<Node>();
    static List<Node> adjustedImpossibleNodes = new LinkedList<Node>();
    static List<Node> clearedImpossibleNodes = new LinkedList<Node>();
    static int [][] nearestUnexploredExtended = new int[9][2];
    static boolean explorationDone = false;
    static boolean fastestPathDone = false;
    
    //for percentage explore
    static boolean goalReached = false;  //and fullExplore also
    static double nodesToExplore;
    static int nodesToExploreRounded;
    
    //for timed explore
    static int timeLimitAbsolute = 0;
    static int prevX = 0, prevY = 0;
    
    //for fastest path
    static int consecutiveForward = 0;
    
    static int speed;
    static final int sleepTime = 250;
    static boolean turnTwiceFlag;
    static int movementCounter = 0;
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
                try{
                TimeUnit.SECONDS.sleep(10);
                }
                catch (Exception e){}
                con.writeData("bExplore start");
                fullExplore(1);
                while (!explorationDone){}
                con.writeData("bExplore done");
            }
            else if(readInt == 11){
                con.writeData("bFastest Path start");
                fastPath(1);
                while (!fastestPathDone){}
                con.writeData("bFastest Path done");
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
        
        
        //percentageExplore(50, 1);
        //timedExplore(100, 4);
        //fullExplore(1);
        //fastPath(1);
        
        //test for integration
//        try{
//        TimeUnit.SECONDS.sleep(10);
//        }
//        catch (Exception e){}
//        con.readData();
//        fullExplore(1);      
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
        
        Robot.defineRobotPosition(x + 1, y);   
        
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
                
                con.writeData("ad"); //Arduino turn right             
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
                        break;
                    }
                }
                break;
                
            case Direction.TURN_LEFT:
                
                con.writeData("aa"); //Arduino turn left
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
                        break;
                    }
                }
                break;
        }
    }
    //go , stop() is not needed anymore because specify exactly how much to move forward
    public void forward(int tileCount){
        
        int x = Robot.R2X;
        int y = Robot.R2Y;
        
        con.writeData("aw"); //Arduino move forward for tileCount, not specified as of yet cause hardware programming not ready
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
                break;
            }
        }
    }
    
    //scan for obstacles
    public int scan(){
        con.writeData("ac"); //Arduino scan
        while (true){        //Wait till scanning finishes
            int tmp = con.messageRecognition();
            if (tmp== 12){
                System.out.println(12);
                break;
            }
            System.out.println(tmp);
        }
        scanResult = con.sensorDataParse();
        
        int scannedNodes = 0;
        //isObstacle() returns a boolean, setObstacleTile() accepts int, so if true = 1, false = 0 
        if (StateOfMap.isValidTile(Robot.Tile1X, Robot.Tile1Y)){
            if (!StateOfMap.isExploredTile(Robot.Tile1X, Robot.Tile1Y)){
                StateOfMap.setExploredTile(Robot.Tile1X, Robot.Tile1Y, 1);
                if (scanResult[1] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile1X, Robot.Tile1Y, 1);
                //StateOfMap.setObstacleTile(Robot.Tile1X, Robot.Tile1Y, map.getNode(Robot.Tile1X, Robot.Tile1Y).isObstacle() ? 1 : 0 );
                StateOfMap.updateDescriptor(Robot.Tile1X, Robot.Tile1Y, 1);
                scannedNodes++;
            }
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile1EX, Robot.Tile1EY) && StateOfMap.isExploredTile(Robot.Tile1X, Robot.Tile1Y)){
                    if (!StateOfMap.isExploredTile(Robot.Tile1EX, Robot.Tile1EY) && !StateOfMap.isObstacleTile(Robot.Tile1X, Robot.Tile1Y)){
                        StateOfMap.setExploredTile(Robot.Tile1EX, Robot.Tile1EY, 1);
                        if (scanResult[1] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile1EX, Robot.Tile1EY, 1);
                        //StateOfMap.setObstacleTile(Robot.Tile1EX, Robot.Tile1EY, map.getNode(Robot.Tile1EX, Robot.Tile1EY).isObstacle() ? 1 : 0 );
                        StateOfMap.updateDescriptor(Robot.Tile1EX, Robot.Tile1EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile2X, Robot.Tile2Y)){
            if (!StateOfMap.isExploredTile(Robot.Tile2X, Robot.Tile2Y)){
                StateOfMap.setExploredTile(Robot.Tile2X, Robot.Tile2Y, 1);
                if (scanResult[2] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile2X, Robot.Tile2Y, 1);
                //StateOfMap.setObstacleTile(Robot.Tile2X, Robot.Tile2Y, map.getNode(Robot.Tile2X, Robot.Tile2Y).isObstacle() ? 1 : 0 );
                StateOfMap.updateDescriptor(Robot.Tile2X, Robot.Tile2Y, 1);
                scannedNodes++;                
            }
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile2EX, Robot.Tile2EY) && StateOfMap.isExploredTile(Robot.Tile2X, Robot.Tile2Y)){
                    if (!StateOfMap.isExploredTile(Robot.Tile2EX, Robot.Tile2EY) && !StateOfMap.isObstacleTile(Robot.Tile2X, Robot.Tile2Y)){
                        StateOfMap.setExploredTile(Robot.Tile2EX, Robot.Tile2EY, 1);
                        if (scanResult[2] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile2EX, Robot.Tile2EY, 1);
                        //StateOfMap.setObstacleTile(Robot.Tile2EX, Robot.Tile2EY, map.getNode(Robot.Tile2EX, Robot.Tile2EY).isObstacle() ? 1 : 0 );
                        StateOfMap.updateDescriptor(Robot.Tile2EX, Robot.Tile2EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile3X, Robot.Tile3Y)){
            if (!StateOfMap.isExploredTile(Robot.Tile3X, Robot.Tile3Y)){
                StateOfMap.setExploredTile(Robot.Tile3X, Robot.Tile3Y, 1);
                if (scanResult[3] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile3X, Robot.Tile3Y, 1);
                //StateOfMap.setObstacleTile(Robot.Tile3X, Robot.Tile3Y, map.getNode(Robot.Tile3X, Robot.Tile3Y).isObstacle() ? 1 : 0 );
                StateOfMap.updateDescriptor(Robot.Tile3X, Robot.Tile3Y, 1);
                scannedNodes++;                
            }
            
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile3EX, Robot.Tile3EY) && StateOfMap.isExploredTile(Robot.Tile3X, Robot.Tile3Y)){
                    if (!StateOfMap.isExploredTile(Robot.Tile3EX, Robot.Tile3EY) && !StateOfMap.isObstacleTile(Robot.Tile3X, Robot.Tile3Y)){
                        StateOfMap.setExploredTile(Robot.Tile3EX, Robot.Tile3EY, 1);
                        if (scanResult[3] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile3EX, Robot.Tile3EY, 1);
                        //StateOfMap.setObstacleTile(Robot.Tile3EX, Robot.Tile3EY, map.getNode(Robot.Tile3EX, Robot.Tile3EY).isObstacle() ? 1 : 0 );
                        StateOfMap.updateDescriptor(Robot.Tile3EX, Robot.Tile3EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
        if (StateOfMap.isValidTile(Robot.Tile5X, Robot.Tile5Y)){
            if (!StateOfMap.isExploredTile(Robot.Tile5X, Robot.Tile5Y)){ 
                StateOfMap.setExploredTile(Robot.Tile5X, Robot.Tile5Y, 1);
                if (scanResult[0] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile5X, Robot.Tile5Y, 1);
                //StateOfMap.setObstacleTile(Robot.Tile5X, Robot.Tile5Y, map.getNode(Robot.Tile5X, Robot.Tile5Y).isObstacle() ? 1 : 0 );
                StateOfMap.updateDescriptor(Robot.Tile5X, Robot.Tile5Y, 1);
                scannedNodes++;
            }
                
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile5EX, Robot.Tile5EY) && StateOfMap.isExploredTile(Robot.Tile5X, Robot.Tile5Y)){
                    if (!StateOfMap.isExploredTile(Robot.Tile5EX, Robot.Tile5EY) && !StateOfMap.isObstacleTile(Robot.Tile5X, Robot.Tile5Y)){
                        StateOfMap.setExploredTile(Robot.Tile5EX, Robot.Tile5EY, 1);
                        if (scanResult[0] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile5EX, Robot.Tile5EY, 1);
                        //StateOfMap.setObstacleTile(Robot.Tile5EX, Robot.Tile5EY, map.getNode(Robot.Tile5EX, Robot.Tile5EY).isObstacle() ? 1 : 0 );
                        StateOfMap.updateDescriptor(Robot.Tile5EX, Robot.Tile5EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
//        if (StateOfMap.isValidTile(Robot.Tile6X, Robot.Tile6Y)){
//            if (!StateOfMap.isExploredTile(Robot.Tile6X, Robot.Tile6Y)){
//                StateOfMap.setExploredTile(Robot.Tile6X, Robot.Tile6Y, 1);
//                StateOfMap.setObstacleTile(Robot.Tile6X, Robot.Tile6Y, map.getNode(Robot.Tile6X, Robot.Tile6Y).isObstacle() ? 1 : 0 );
//                //StateOfMap.updateDescriptor(Robot.Tile6X, Robot.Tile6Y, 1);
//                scannedNodes++;
//            }
//                
//            if (enableTwoTiles){
//                if (StateOfMap.isValidTile(Robot.Tile6EX, Robot.Tile6EY) && StateOfMap.isExploredTile(Robot.Tile6X, Robot.Tile6Y)){
//                    if (!StateOfMap.isExploredTile(Robot.Tile6EX, Robot.Tile6EY) && !StateOfMap.isObstacleTile(Robot.Tile6X, Robot.Tile6Y)){
//                        StateOfMap.setExploredTile(Robot.Tile6EX, Robot.Tile6EY, 1);
//                        StateOfMap.setObstacleTile(Robot.Tile6EX, Robot.Tile6EY, map.getNode(Robot.Tile6EX, Robot.Tile6EY).isObstacle() ? 1 : 0 );
//                        //StateOfMap.updateDescriptor(Robot.Tile6EX, Robot.Tile6EY, 1);
//                        scannedNodes++;
//                    }
//                }
//            }
//        }
//        if (StateOfMap.isValidTile(Robot.Tile7X, Robot.Tile7Y)){
//            if (!StateOfMap.isExploredTile(Robot.Tile7X, Robot.Tile7Y)){ 
//                StateOfMap.setExploredTile(Robot.Tile7X, Robot.Tile7Y, 1);
//                StateOfMap.setObstacleTile(Robot.Tile7X, Robot.Tile7Y, map.getNode(Robot.Tile7X, Robot.Tile7Y).isObstacle() ? 1 : 0 );
//                //StateOfMap.updateDescriptor(Robot.Tile7X, Robot.Tile7Y, 1);
//                scannedNodes++;
//            }
//                
//            if (enableTwoTiles){
//                if (StateOfMap.isValidTile(Robot.Tile7EX, Robot.Tile7EY) && StateOfMap.isExploredTile(Robot.Tile7X, Robot.Tile7Y)){
//                    if (!StateOfMap.isExploredTile(Robot.Tile7EX, Robot.Tile7EY) && !StateOfMap.isObstacleTile(Robot.Tile7X, Robot.Tile7Y)){
//                        StateOfMap.setExploredTile(Robot.Tile7EX, Robot.Tile7EY, 1);
//                        StateOfMap.setObstacleTile(Robot.Tile7EX, Robot.Tile7EY, map.getNode(Robot.Tile7EX, Robot.Tile7EY).isObstacle() ? 1 : 0 );
//                        //StateOfMap.updateDescriptor(Robot.Tile7EX, Robot.Tile7EY, 1);
//                        scannedNodes++;
//                    }
//                }
//            }
//        }
        if (StateOfMap.isValidTile(Robot.Tile8X, Robot.Tile8Y)){
            if (!StateOfMap.isExploredTile(Robot.Tile8X, Robot.Tile8Y)){ 
                StateOfMap.setExploredTile(Robot.Tile8X, Robot.Tile8Y, 1);
                if (scanResult[4] == 0)
                    StateOfMap.setObstacleTile(Robot.Tile8X, Robot.Tile8Y, 1);
                //StateOfMap.setObstacleTile(Robot.Tile8X, Robot.Tile8Y, map.getNode(Robot.Tile8X, Robot.Tile8Y).isObstacle() ? 1 : 0 );
                StateOfMap.updateDescriptor(Robot.Tile8X, Robot.Tile8Y, 1);
                scannedNodes++;
            }
                
            if (enableTwoTiles){
                if (StateOfMap.isValidTile(Robot.Tile8EX, Robot.Tile8EY) && StateOfMap.isExploredTile(Robot.Tile8X, Robot.Tile8Y)){
                    if (!StateOfMap.isExploredTile(Robot.Tile8EX, Robot.Tile8EY) && !StateOfMap.isObstacleTile(Robot.Tile8X, Robot.Tile8Y)){
                        StateOfMap.setExploredTile(Robot.Tile8EX, Robot.Tile8EY, 1);
                        if (scanResult[4] == 1)
                            StateOfMap.setObstacleTile(Robot.Tile8EX, Robot.Tile8EY, 1);
                        //StateOfMap.setObstacleTile(Robot.Tile8EX, Robot.Tile8EY, map.getNode(Robot.Tile8EX, Robot.Tile8EY).isObstacle() ? 1 : 0 );
                        StateOfMap.updateDescriptor(Robot.Tile8EX, Robot.Tile8EY, 1);
                        scannedNodes++;
                    }
                }
            }
        }
//        if (StateOfMap.isValidTile(Robot.Tile9X, Robot.Tile9Y)){
//            if (!StateOfMap.isExploredTile(Robot.Tile9X, Robot.Tile9Y)){
//                StateOfMap.setExploredTile(Robot.Tile9X, Robot.Tile9Y, 1);
//                StateOfMap.setObstacleTile(Robot.Tile9X, Robot.Tile9Y, map.getNode(Robot.Tile9X, Robot.Tile9Y).isObstacle() ? 1 : 0 );
//                //StateOfMap.updateDescriptor(Robot.Tile9X, Robot.Tile9Y, 1);
//                scannedNodes++;
//            } 
//                
//            if (enableTwoTiles){
//                if (StateOfMap.isValidTile(Robot.Tile9EX, Robot.Tile9EY) && StateOfMap.isExploredTile(Robot.Tile9X, Robot.Tile9Y)){
//                    if (!StateOfMap.isExploredTile(Robot.Tile9EX, Robot.Tile9EY) && !StateOfMap.isObstacleTile(Robot.Tile9X, Robot.Tile9Y)){
//                        StateOfMap.setExploredTile(Robot.Tile9EX, Robot.Tile9EY, 1);
//                        StateOfMap.setObstacleTile(Robot.Tile9EX, Robot.Tile9EY, map.getNode(Robot.Tile9EX, Robot.Tile9EY).isObstacle() ? 1 : 0 );
//                        //StateOfMap.updateDescriptor(Robot.Tile9EX, Robot.Tile9EY, 1);
//                        scannedNodes++;
//                    }
//                }
//            }
//        }
//        if (StateOfMap.isValidTile(Robot.Tile10X, Robot.Tile10Y)){
//            if (!StateOfMap.isExploredTile(Robot.Tile10X, Robot.Tile10Y)){
//                StateOfMap.setExploredTile(Robot.Tile10X, Robot.Tile10Y, 1);
//                StateOfMap.setObstacleTile(Robot.Tile10X, Robot.Tile10Y, map.getNode(Robot.Tile10X, Robot.Tile10Y).isObstacle() ? 1 : 0 );
//                //StateOfMap.updateDescriptor(Robot.Tile10X, Robot.Tile10Y, 1);
//                scannedNodes++;
//            }  
//                
//            if (enableTwoTiles){
//                if (StateOfMap.isValidTile(Robot.Tile10EX, Robot.Tile10EY) && StateOfMap.isExploredTile(Robot.Tile10X, Robot.Tile10Y)){
//                    if (!StateOfMap.isExploredTile(Robot.Tile10EX, Robot.Tile10EY) && !StateOfMap.isObstacleTile(Robot.Tile10X, Robot.Tile10Y)){
//                        StateOfMap.setExploredTile(Robot.Tile10EX, Robot.Tile10EY, 1);
//                        StateOfMap.setObstacleTile(Robot.Tile10EX, Robot.Tile10EY, map.getNode(Robot.Tile10EX, Robot.Tile10EY).isObstacle() ? 1 : 0 );
//                        //StateOfMap.updateDescriptor(Robot.Tile10EX, Robot.Tile10EY, 1);
//                        scannedNodes++;
//                    }
//                }
//            }
//        }
        return scannedNodes; 
    }
    
    //This method used only for determineNearestUnexplored(), but same logic as Node.sethCost()
    private int heuristicFunc(int[] objective){
        for (Node tmp : impossibleNodes ) {
            if ( tmp.getX() == objective[0] && tmp.getY() == objective[1] )
                return 5000;
        }    
        return Math.abs( currentLocation[0] - objective[0] ) + Math.abs( currentLocation[1] - objective[1] );
    }
    
//    private int heuristicFunc2(int[] objective){
//        for (Node tmp : impossibleNodes ) {
//            if ( tmp.getX() == objective[0] && tmp.getY() == objective[1] )
//                return 5000;
//        }
//        int curToObjective = Math.abs( currentLocation[0] - objective[0] ) + Math.abs( currentLocation[1] - objective[1] );
//        int objectiveToGoal = Math.abs( currentLocation[0] - objective[0] ) + Math.abs( currentLocation[1] - objective[1] );
//        
//        return curToObjective + (3 * objectiveToGoal);
//    }
    
    public int[] determineNearestUnexplored(){
        int[] tmp = new int[2]; 
        int[] objective = new int[2];
        int lowestDistance = 9999;  //random initial super high value
        int distance = 0;
        
        //for each unexplored node calculate only h() cost, smallest wins
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                if( !StateOfMap.isExploredTile(i, j) ){
                    tmp[0] = i;
                    tmp[1] = j;
//                    if (goalReached)
                        distance = heuristicFunc(tmp); //use h value of current->tmp only, greedy but fast
//                    else
//                        distance = heuristicFunc2(tmp);
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
                    case "10%": percentageExplore(10, speed);
                        break;
                    case "20%": percentageExplore(20, speed);
                        break;
                    case "30%": percentageExplore(30, speed);
                        break;
                    case "40%": percentageExplore(40, speed);
                        break;
                    case "50%": percentageExplore(50, speed);
                        break;
                    case "60%": percentageExplore(60, speed);
                        break;
                    case "70%": percentageExplore(70, speed);
                        break;
                    case "80%": percentageExplore(80, speed);
                        break;
                    case "90%": percentageExplore(90, speed);
                        break;
                    case "100%": fullExplore(speed);
                        break;
                    default: System.out.println("Error");
                        break;
                }
                break;
            case "30s": timedExplore(30, speed);
                break;
            case "60s": timedExplore(60, speed);
                break;
            case "120s": timedExplore(120, speed);
                break;
            case "180s": timedExplore(180, speed);
                break;
            case "240s": timedExplore(240, speed);
                break;
            case "300s": timedExplore(300, speed);
                break;
            case "360s": timedExplore(360, speed);
                break;
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
        
                for (int i = 0; i < 4; i++){
                    scan();       //detect obstacles
                    executeTurn(Direction.TURN_RIGHT);
                    publishAndSleep();
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
//                        else{
//                            for (Node tmp : impossibleNodes){
//                                if (StateOfMap.isExploredTile(tmp.getX(), tmp.getY())){
//                                    clearedImpossibleNodes.add(tmp); //concurrent modification of same list
//                                }
//                            }
//                            for (Node tmp2 : clearedImpossibleNodes){
//                                if (impossibleNodes.contains(tmp2))
//                                    impossibleNodes.remove(tmp2);                             
//                            }
//                            System.out.println("check");
//                            clearedImpossibleNodes.clear();
//                        }
                        if (!StateOfMap.frontIsTraversable()){ 
                            if (actionSequence.isEmpty())   //skips sleep and publish if robot didn't move
                                break;
                            break;
                        }
                        else{    
                            forward(1);
                            scan();
                            publishAndSleep();
                            updateExploredAndObstacleCount();
                            System.out.println("Explored nodes: " + exploredNodeCount);
                        }
                    }

                    if (exploredNodeCount == 300)
                        done = true;
                    
                    else{
                        //System.out.println("Impossible Nodes: " + impossibleNodes.size());
                        if ( 300 - exploredNodeCount == impossibleNodes.size() ){
                            thereAreImpossibleNodesLeft = true;
                            System.out.println("\nThere are nodes remaining: " + impossibleNodes.size() + "\n");
                            for (Node tmp : impossibleNodes){
                                nearestUnexplored[0][0] = tmp.getX();
                                nearestUnexplored[0][1] = tmp.getY();
                                //now technically neighbout of nearest unexplored
                                //CAN USE ISREACHABLE TILE HERE, ELSE ONLY DO SMTH ELSE
                                //with reference to comment above, what if impossible node is not at the border, do smth
                                
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
                        //leniencyTrigger = true;
                        System.out.println("Explored nodes: " + exploredNodeCount);
                        //for (int lastTries = 0; lastTries < 2; lastTries++){
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
                                //if (!StateOfMap.isExploredTile(impossibleNodes.get(increment).getX(), impossibleNodes.get(increment).getY())){
                                    tmp[0] = remainingNode.getX();
                                    tmp[1] = remainingNode.getY();
//                                    nearestUnexploredExtended = nearestUnexploredNeighbours(tmp);
//                                    for (int i = 0; i < 9; i++){
//                                        nearestUnexplored = nearestUnexploredNeighbours(nearestUnexploredExtended[i]);
//                                        if (nearestUnexplored[0][0] != -1 && nearestUnexplored[0][1] != -1 && 
//                                                !StateOfMap.isObstacleTile(nearestUnexplored[0][0], nearestUnexplored[0][1]))
//                                            moveToObjective(nearestUnexplored[0]);
//                                    }
                                    nearestUnexplored = nearestUnexploredNeighbours(tmp);
                                    //if (!StateOfMap.isObstacleTile(nearestUnexplored[0][0], nearestUnexplored[0][1]))
                                        moveToObjective(nearestUnexplored[0]);
                                    System.out.println("Explored nodes: " + exploredNodeCount);
                                //}
                                if (exploredNodeCount == 300){
                                    done = true;
                                    break;
                                }
                            }
                        //}
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
                             
                for (int i = 1; i < 9; i++){
                    nearestUnexplored[i][0] = -1;
                    nearestUnexplored[i][1] = -1;
                }             
                if(!goalReached)
                    moveToObjective(goalZoneLocation);
                moveToObjective(startZoneLocation);
                System.out.println("Movements: " + movementCounter);
                System.out.println("Turns: " + turnCounter);
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
                    saveFile();
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

                actionSequence = map.findPath(currentLocation, objective); 
                //for (Node tmp : actionSequence)
                    //System.out.println("Action Path: " + tmp.getX() + " " + tmp.getY());
                if (actionSequence == null)
                    System.out.println("null");
                if (actionSequence.isEmpty()){  
                    System.out.println("is empty");
                    bestPathImpossible = true;                       
                    impossibleNodes.add( new Node(objective[0], objective[1]));
                    System.out.println("Impossible Nodes: " + impossibleNodes.size());
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

//                        if (explorationDone){
//                            forward(1);
//                            publishAndSleep(); 
//                        }
//                        else 
                        if (StateOfMap.frontIsTraversable()) {            
                            //setRobotLocationAsExplored();
                            forward(1);
                            scan();
                            publishAndSleep(); 
                            bestPathImpossible = false;
                            updateExploredAndObstacleCount();
                        } 
                        else {
                            bestPathImpossible = true;                       
                            impossibleNodes.add( actionSequence.get(actionSequence.size()-1) );
                            System.out.println("Impossible Nodes: " + impossibleNodes.size());
                            break;//this is where we handle nearest unexplored or neighbours being obstacles
                        }

                    }
                    //if (!explorationDone){
                        //scan();           //redundent, especially if there's no path, no need to scan if didn't move
                        //setRobotLocationAsExplored();
                    //}
                    if(bestPathImpossible != true){
                            //publishAndSleep();
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
    
    //for percentage exploration, there are a lot of variables/logic shared between this and fullExplore()
    public void percentageExplore(int percentage, int speed){
        
        obstacleCount = 0;
        done = false;
        goalReached = false;
        this.speed = speed;
        nodesToExplore = 300 * percentage/100;
        nodesToExploreRounded = (int) nodesToExplore; //round 
        
//        SwingWorker worker = new SwingWorker<Integer, Integer>() {
//            @Override
//            protected Integer doInBackground() {
        
                for (int i = 0; i < 4; i++){
                    scan();       //detect obstacles
                    executeTurn(Direction.TURN_RIGHT);
                    //publishAndSleep();
                }       

                while (!done){
                    while (true){
                        if (exploredNodeCount >= nodesToExploreRounded)  //have to terminate no matter what
                            break;
                        if (!bestPathImpossible || impossibleNodes.size() > 100)
                            impossibleNodes.clear();
                        if (!StateOfMap.frontIsTraversable()){ 
                            if (actionSequence.isEmpty())   //skips sleep and publish if robot didn't move
                                break;
                            //scan();
                            //publishAndSleep();
                            break;
                        }
                        else{    
                            forward(1);
                            scan();
                            //publishAndSleep();
                            updateExploredAndObstacleCount();
                            System.out.println("Explored nodes: " + exploredNodeCount);
                        }
                    }

                    if (exploredNodeCount >= nodesToExploreRounded && goalReached)  //have to terminate no matter what
                        done = true;
                    else if (exploredNodeCount >= nodesToExploreRounded)
                        break;
                    else{
                        System.out.println("Impossible Nodes: " + impossibleNodes.size());
                            nearestUnexplored[0] = determineNearestUnexplored(); 
                            nearestUnexplored = nearestUnexploredNeighbours(nearestUnexplored[0]);
                            System.out.print("Nearest unexplored: " + nearestUnexplored[0][0] + " ");
                            System.out.println(nearestUnexplored[0][1]);
                            moveToObjectivePercentage(nearestUnexplored[0]);
                    }                                       
                }                
                
                //tbh I am not sure if we need to move to goal/start, or just terminate immediately after reaching threshold
                //currently assumes that after reaching it, will try to move to goal then to start
                if (done){                        
                    System.out.println("Exploration complete"); // moving back to start.");
                    System.out.println("Explored nodes: " + exploredNodeCount);
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);   
                    
//                    explorationDone = true;
//                    for (int i = 1; i < 9; i++){
//                        nearestUnexplored[i][0] = -1;
//                        nearestUnexplored[i][1] = -1;
//                    }
//                    moveToObjectivePercentage(startZoneLocation);
                }
                else{
                    System.out.println("Exploration complete"); // but didn't reach goal, moving to goal if possible then to start");
                    System.out.println("Explored nodes: " + exploredNodeCount);
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);
                    
//                    explorationDone = true;
//                    for (int i = 1; i < 9; i++){
//                        nearestUnexplored[i][0] = -1;
//                        nearestUnexplored[i][1] = -1;
//                    }
//                    moveToObjectivePercentage(goalZoneLocation);
//                    moveToObjectivePercentage(startZoneLocation);
                }              
//                return 1;
//            }
//            @Override
//            public void process(java.util.List<Integer> chunks){
//                try{
//                        CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor1.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
//                CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor2.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
//                for (Node node : updateList) {
//                    StateOfMap.updateDescriptor(node.getX(), node.getY(), 0);
//                    updateList.remove(node);
//                }
//                }
//                catch(Exception e){
//                }
//                    
//                    
//            }
//            public void publishAndSleep() {
//                publish();
//                try{
//                    Thread.sleep(sleepTime/speed);
//                }
//                catch(InterruptedException e){
//                    
//                }
//            }
//        };
//        worker.execute();
    }
    
    public void timedExplore(int timeLimit, int speed){  //just use an integer for now
        obstacleCount = 0;
        done = false;   //done is used differently in this context, only set when timelimit reached
        this.speed = speed;
        timeLimitAbsolute = timeLimit * speed;
        
//        SwingWorker worker = new SwingWorker<Integer, Integer>() {
//            @Override
//            protected Integer doInBackground() {
                for (int i = 0; i < 4; i++){
                    scan();       //detect obstacles
                    executeTurn(Direction.TURN_RIGHT);
                    //publishAndSleep();
                }       

                while (!done){
                    while (true){
                        if ( (movementCounter + turnCounter) >= timeLimitAbsolute){  //have to terminate no matter what
                            done = true;
                            break;
                        }
                        if (!bestPathImpossible || impossibleNodes.size() > 100)
                            impossibleNodes.clear();
                        if (!StateOfMap.frontIsTraversable()){ 
                            break;
                        }
                        else{    
                            forward(1);
                            scan();
                            //publishAndSleep();
                            updateExploredAndObstacleCount();
                            System.out.println("Explored nodes: " + exploredNodeCount);
                        }
                    }

                    if (movementCounter + turnCounter >= timeLimitAbsolute)  //have to terminate no matter what
                        done = true;
                    else{
                        System.out.println("Impossible Nodes: " + impossibleNodes.size());
                        if ( 300 - exploredNodeCount == impossibleNodes.size() ){
                            thereAreImpossibleNodesLeft = true;
                            System.out.println("\nThere are nodes remaining: " + impossibleNodes.size() + "\n");
                            for (Node tmp : impossibleNodes){
                                nearestUnexplored[0][0] = tmp.getX();
                                nearestUnexplored[0][1] = tmp.getY();
                                //now technically neighbout of nearest unexplored
                                //CAN USE ISREACHABLE TILE HERE, ELSE ONLY DO SMTH ELSE
                                //with reference to comment above, what if impossible node is not at the border, do smth
                                if (nearestUnexplored[0][0] <= 0 )
                                    nearestUnexplored[0][0]++;
                                if (nearestUnexplored[0][1] <= 0 )
                                    nearestUnexplored[0][1]++;
                                if (nearestUnexplored[0][0] >= width - 1 )
                                    nearestUnexplored[0][0]--;
                                if (nearestUnexplored[0][1] >= height - 1 )
                                    nearestUnexplored[0][1]--;
                                
                                adjustedImpossibleNodes.add(new Node(nearestUnexplored[0][0], nearestUnexplored[0][1]));
                            }
                        }
                        else{
                            nearestUnexplored[0] = determineNearestUnexplored(); 
                            nearestUnexplored = nearestUnexploredNeighbours(nearestUnexplored[0]);
                            System.out.print("Nearest unexplored: " + nearestUnexplored[0][0] + " ");
                            System.out.println(nearestUnexplored[0][1]);
                            moveToObjectiveTimed(nearestUnexplored[0]);
                        }
                    }
                    
                    if (thereAreImpossibleNodesLeft){
                        int[] tmp = new int[2];
                        System.out.println("Explored nodes: " + exploredNodeCount);
                            for (Node remainingNode: adjustedImpossibleNodes){
                                System.out.println("Remaning Node: " + remainingNode.getX() + " " + remainingNode.getY());
                                    tmp[0] = remainingNode.getX();
                                    tmp[1] = remainingNode.getY();
                                    
                                    nearestUnexplored = nearestUnexploredNeighbours(tmp);
                                    if (!StateOfMap.isObstacleTile(nearestUnexplored[0][0], nearestUnexplored[0][1]))
                                        moveToObjectiveTimed(nearestUnexplored[0]);
                                    System.out.println("Explored nodes: " + exploredNodeCount);

                                if ( (movementCounter + turnCounter) >= timeLimitAbsolute){
                                    done = true;
                                    break;
                                }    
                                if (exploredNodeCount == 300)
                                    break;
                            }                            
                        break;
                    }   
                }                
                
                if (done){                        
                    System.out.println("Exploration terminated due to time limit: " + timeLimit + " seconds"); // moving back to start.");
                    System.out.println("Explored nodes: " + exploredNodeCount);
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);                     
                }
                else{
                    System.out.println("Exploration finished before time limit: " + timeLimit + " seconds");
                    System.out.println("Explored nodes: " + exploredNodeCount);
                    System.out.println("Obstacle count: " + obstacleCount);
                    System.out.println("Movements: " + movementCounter);
                    System.out.println("Turns: " + turnCounter);   
                }
                
//                return 1;
//            }
//            @Override
//            public void process(java.util.List<Integer> chunks){
//                try{
//                        CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor1.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
//                CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor2.paintRobotLocation(Controller.currentLocation[0], Controller.currentLocation[1]);
//                for (Node node : updateList) {
//                    StateOfMap.updateDescriptor(node.getX(), node.getY(), 0);
//                    updateList.remove(node);
//                }
//                }
//                catch(Exception e){
//                }
//                    
//                    
//            }
//            public void publishAndSleep() {
//                publish();
//                try{
//                    Thread.sleep(sleepTime/speed);
//                }
//                catch(InterruptedException e){
//                    
//                }
//            }
//        };
//        worker.execute();
    }
    
    public void fastPath(int speed){
        //exploration is done here changed that shit
        
        this.speed = speed;
        //Controller.mapsimulator.contentPanel.refresh();
//        for (int i = 0; i < width; i++){
//            for (int j = 0; j < height; j++){
//                StateOfMap.exploredMap[i][j] = 1;
//                if(Controller.map.getNode(i, j).isObstacle()){
//                    StateOfMap.obstacleMap[i][j] = 1;
//                }
//                else{
//                    StateOfMap.obstacleMap[i][j] = 0;
//                }
//           }
//        }
        for (int i = 1; i < 9; i++){
            nearestUnexplored[i][0] = -1;
            nearestUnexplored[i][1] = -1;
        }
        //explorationDone = true;
        moveToObjectiveDemo(goalZoneLocation);

//        for (int i = 0; i < width; i++){
//            for (int j = 0; j < height; j++){
//                StateOfMap.exploredMap[i][j] = 0;
//                StateOfMap.obstacleMap[i][j] = 0;
//           }
//        }
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

                //COMMENTED OUT CONSECUTIVE FORWARD BLOCKS FOR NOW
//                prevX = Robot.R9X;
//                prevY = Robot.R9Y;
                for (Node s : actionSequence){
                    //System.out.println("Action Path: " + s.getX() + " " + s.getY());
                    directionX = s.getX() - Robot.R9X;  //- prevX;
                    directionY = s.getY() - Robot.R9Y;  //- prevY;
//                    prevX = s.getX();
//                    prevY = s.getY();
                                     
                        if (directionX < 0 && directionY == 0) {
//                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
//                                if (consecutiveForward != 0){
//                                    forward(consecutiveForward);
//                                    consecutiveForward = 0;
//                                }
                                turn(Direction.DIRECTION_LEFT);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
//                            }
//                            else {
//                                consecutiveForward++;
//                            }
                        } else if (directionX > 0 && directionY == 0) {
//                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
//                                if (consecutiveForward != 0){
//                                    forward(consecutiveForward);
//                                    consecutiveForward = 0;
//                                }
                                turn(Direction.DIRECTION_RIGHT);
                                publishAndSleep();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
//                            }
//                            else {
//                                consecutiveForward++;
//                            }
                        } else if (directionY < 0 && directionX == 0) {
//                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
//                                if (consecutiveForward != 0){
//                                    forward(consecutiveForward);
//                                    consecutiveForward = 0;
//                                }
                                turn(Direction.DIRECTION_DOWN);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
//                            }
//                            else {
//                                consecutiveForward++;
//                            }
                        } else if (directionY > 0 && directionX == 0) {
//                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
//                                if (consecutiveForward != 0){
//                                    forward(consecutiveForward);
//                                    consecutiveForward = 0;
//                                }
                                turn(Direction.DIRECTION_UP);
                                publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    publishAndSleep();
                                    turnTwiceFlag = false;
                                }
//                            }
//                            else {
//                                consecutiveForward++;
//                            }
                        }                 
                       
                    if (StateOfMap.frontIsTraversable() ) {//&& consecutiveForward == 0) {            
                        forward(1);
                        publishAndSleep(); 
                    }               
                }
                
                //just in case the objective reached before you had the chance to execute all the delayed forwards
//                if (consecutiveForward != 0){            
//                    forward(consecutiveForward);
//                    publishAndSleep(); 
//                }
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
    
    //THE BELOW IS NOT USED
    
            public void moveToObjectivePercentage(int[] objective){ 

                actionSequence = map.findPath(currentLocation, objective); 
                //for (Node tmp : actionSequence)
                    //System.out.println("Action Path: " + tmp.getX() + " " + tmp.getY());
                if (actionSequence == null)
                    System.out.println("null");
                if (actionSequence.isEmpty()){  
                    System.out.println("is empty");
                    bestPathImpossible = true;                       
                    impossibleNodes.add( new Node(objective[0], objective[1]));
                }
                
                    for (Node s : actionSequence){
                        System.out.println("Action Path: " + s.getX() + " " + s.getY());
                        directionX = s.getX() - Robot.R9X;
                        directionY = s.getY() - Robot.R9Y;

                        if (directionX < 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
                                turn(Direction.DIRECTION_LEFT);
                                //publishAndSleep();
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionX > 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
                                turn(Direction.DIRECTION_RIGHT);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY < 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
                                turn(Direction.DIRECTION_DOWN);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY > 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
                                turn(Direction.DIRECTION_UP);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        }

                        if (explorationDone){
                            forward(1);
                            //publishAndSleep();
                        }
                        else 
                            if (StateOfMap.frontIsTraversable()) {            
                            //setRobotLocationAsExplored();
                            forward(1);
                            scan();
                            //publishAndSleep();
                            bestPathImpossible = false;
                            updateExploredAndObstacleCount();
                            if (exploredNodeCount >= nodesToExploreRounded)
                                return;
                        } 
                        else {
                            bestPathImpossible = true;                       
                            impossibleNodes.add( actionSequence.get(actionSequence.size()-1) );
                            break;//this is where we handle nearest unexplored or neighbours being obstacles
                        }

                    }
                    if (!explorationDone){
                        scan();
                        setRobotLocationAsExplored();
                        //publishAndSleep();
                    }
              }
            
            public void moveToObjectiveTimed(int[] objective){ 

                actionSequence = map.findPath(currentLocation, objective); 
                //for (Node tmp : actionSequence)
                    //System.out.println("Action Path: " + tmp.getX() + " " + tmp.getY());
                if (actionSequence == null)
                    System.out.println("null");
                if (actionSequence.isEmpty()){  
                    System.out.println("is empty");
                    bestPathImpossible = true;                       
                    impossibleNodes.add( new Node(objective[0], objective[1]));
                }
                
                    for (Node s : actionSequence){
                        System.out.println("Action Path: " + s.getX() + " " + s.getY());
                        directionX = s.getX() - Robot.R9X;
                        directionY = s.getY() - Robot.R9Y;
                        
                        if (directionX < 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_LEFT){
                                turn(Direction.DIRECTION_LEFT);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_LEFT);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionX > 0 && directionY == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_RIGHT){
                                turn(Direction.DIRECTION_RIGHT);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_RIGHT);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY < 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_DOWN){
                                turn(Direction.DIRECTION_DOWN);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_DOWN);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        } else if (directionY > 0 && directionX == 0) {
                            if (Direction.CUR_DIRECTION != Direction.DIRECTION_UP){
                                turn(Direction.DIRECTION_UP);
                                //publishAndSleep(); 
                                if (turnTwiceFlag){
                                    turn(Direction.DIRECTION_UP);
                                    //publishAndSleep();
                                    turnTwiceFlag = false;
                                }
                            }
                        }
                        if ( (movementCounter + turnCounter) >= timeLimitAbsolute){  //check after a turn
                            //done = true;
                            break;
                        }

                        if (StateOfMap.frontIsTraversable()) {            
                            forward(1);
                            scan();
                            //publishAndSleep();
                            bestPathImpossible = false;
                            updateExploredAndObstacleCount();
                        } 
                        else {
                            bestPathImpossible = true;                       
                            impossibleNodes.add( actionSequence.get(actionSequence.size()-1) );
                            break;
                        }
                        if ( (movementCounter + turnCounter) >= timeLimitAbsolute){  //check after sucessful forward movement
                            //done = true;
                            break;
                        }
                        scan();
                        setRobotLocationAsExplored();
                    }
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
                        
//                while(con.messageRecognition() != 6){
//
//                }

            }
    
}
