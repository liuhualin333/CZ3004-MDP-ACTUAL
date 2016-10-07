/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;

/**
 *
 * @author Jo
 */
public class Node {
    protected static final int BASICMOVEMENTCOST = 10;
    protected static final int DIAGONALMOVEMENTCOST = 14; // approx (10**2 x 2) ** 0.5
    
    private int x;      
    private int y;
    private boolean isObstacle; 
    private boolean isExplored; 
    
    private Node previous;
    private int direction;
    private boolean notPrefered;
    private boolean isDiagonally; //determines if movement from previous node is diag
    private int gCost;
    private int hCost;
    
    public Node(int x, int y){
        this.x = x;
        this.y = y;
        this.isObstacle = false;
        this.isExplored = true;   //remember to set it to false after finishing testing
        this.previous = null;
        this.notPrefered = false;
        this.isDiagonally = false;
        this.gCost = 0;
        this.hCost = 0;
    }
    
    public int getX(){ return x; }
    public int getY(){ return y; }
    public void setCoordinates(int x, int y){
        this.x = x;
        this.y = y;
    }
    public static boolean isEqual(Node n1, Node n2){ //return if 2 nodes are same
        if (n1.x == n2.x && n1.y == n2.y)
            return true;
        else
            return false;
    }
    
    public boolean isObstacle(){
        return isObstacle;
    }
    public void setIsObstacle(boolean isObstacle){
        this.isObstacle = isObstacle;
    }
    public boolean isInStartZone(){
        if((x <= Controller.startZoneLocation[0] + 1) && (x >= Controller.startZoneLocation[0] - 1) && (y <= Controller.startZoneLocation[0] + 1) && (y >= Controller.startZoneLocation[0] - 1)){
            return true;
        }
        else{
            return false;
        }
        
        
    }
    public boolean isInEndZone(){
        if((x <= Controller.goalZoneLocation[0] + 1) && (x >= Controller.goalZoneLocation[0] - 1) && (y <= Controller.goalZoneLocation[0] + 1) && (y > Controller.goalZoneLocation[0] - 1)){
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean isExplored(){
        return isExplored;
    }
    public void setIsExplored(boolean isExplored){
        this.isExplored = isExplored;
    }
    
    public Node getPrevious(){
        return previous;
    }
    public void setPrevious(Node previous){
        this.previous = previous;
    }
    
    public int getDirection(){
        return direction;
    }
    public void setDirection(){
        int directionX = this.getX() - previous.getX();
        int directionY = this.getY() - previous.getY();
        
        if (directionX < 0 && directionY == 0){
            setDirection(Direction.DIRECTION_LEFT);
        } else if (directionX > 0 && directionY == 0) {
            setDirection(Direction.DIRECTION_RIGHT);
        } else if (directionY < 0 && directionX == 0) {
            setDirection(Direction.DIRECTION_DOWN);
        } else if (directionY > 0 && directionX == 0) {
            setDirection(Direction.DIRECTION_UP);
        }
    }
    public void setDirection(int direction){
        this.direction = direction;
    }
    
    public boolean isNotPrefered(){
        return notPrefered;
    }
    public void setIsNotPrefered(boolean value){
        this.notPrefered = value;
    }
    
    public boolean isDiagonally(){
        return isDiagonally;
    }
    public void setIsDiagonally(boolean isDiagonally){
        this.isDiagonally = isDiagonally;
    }
    
    //G COST RELATED
    public int getgCost(){
        return gCost;
    }
    public void setgCost(int g){
        this.gCost = g;
    }
    public void setgCost(Node previousNode, int cost){ //used for the method below
        setgCost(previousNode.getgCost() + cost);
    }
    public void setgCost(Node previousNode){  //assume constant cost as defined
        if (isDiagonally){
            setgCost(previousNode, DIAGONALMOVEMENTCOST);
        }
        else{
            if (this.direction != previous.direction)
                setgCost(previousNode, BASICMOVEMENTCOST + 1);
            else
                setgCost(previousNode, BASICMOVEMENTCOST);
        }
    }
    /* Initially a node has no gcost, use this to calculate it, then after setting
       with above method, you can get it with getgCost once you reach the next node
    */
    public int calcgCost(Node previousNode){
        if (isDiagonally){
            return (previousNode.getgCost() + DIAGONALMOVEMENTCOST);
        }
        else{
            return (previousNode.getgCost() + BASICMOVEMENTCOST);
        }
    }
    
    //H COST RELATED
    public int gethCost(){
        return hCost;
    }
    public void sethCost(int h){
        this.hCost = h;
    }
    public void sethCost(Node objective){
        
//        if (this.direction != previous.direction){
//            sethCost(   //manhattan distance
//                ( Math.abs(this.getX() - objective.getX()) + Math.abs(this.getY() - objective.getY()) )
//                * BASICMOVEMENTCOST
//            );
//        }
//        else{
            sethCost(   //manhattan distance
                ( Math.abs(this.getX() - objective.getX()) + Math.abs(this.getY() - objective.getY()) )
                * BASICMOVEMENTCOST 
            );
        //}
    }
  
    //F COST RELATED
    public int getfCost(){
        return gCost + hCost;
    }
     
}
