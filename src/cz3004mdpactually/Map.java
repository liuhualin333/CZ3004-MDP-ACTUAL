/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.util.LinkedList;
import java.util.List;
/**
 *
 * @author Jo
 */
public class Map {
    public static boolean CANMOVEDIAGONALLY = false; //CANNOT SET TRUE YET
    
    private int width;  // x
    private int height; // y
    private Node[][] nodeMap;
    public int TOTALNODES = width * height;
    
    public Map(int width, int height){
        this.width = width;
        this.height = height; 
        nodeMap = new Node[width][height];
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++)
                nodeMap[i][j] = new Node(i, j); 
                //all nodes current set to explored=true, during actual runs explored and obstacle set to false
        }
    }
    
    public void setIsObstacle(int x, int y, boolean z){
        nodeMap[x][y].setIsObstacle(z);
    }
    public void setIsExplored(int x, int y, boolean z){
        nodeMap[x][y].setIsExplored(z);
    }
    public Node getNode(int x, int y){
        return nodeMap[x][y];
    }
    
    private List<Node> openList;
    private List<Node> closedList;
    
    // adapted from http://software-talk.org/blog/2012/01/a-star-java/
    // http://www.policyalmanac.org/games/aStarTutorial.htm for easy to understand tutorial
    public List<Node> findPath(int[] start, int[] objective){
        openList = new LinkedList<Node>();      //to be processed
        closedList = new LinkedList<Node>();    //done
        openList.add(nodeMap[start[0]][start[1]]);
        openList.get(0).setDirection(Direction.CUR_DIRECTION);
        
        boolean done = false;
        Node current;
        while (!done) {
            current = lowestFInOpen();  //get node with lowest f() cost, core of Astar
            closedList.add(current);
            openList.remove(current);
//            System.out.print("current to process: " + current.getX());
//            System.out.print(" ");
//            System.out.println(current.getY());
            
            if ( (current.getX() == objective[0]) && (current.getY() == objective[1]) ){
                //System.out.println("start: " + start[0] + " " + start[1]);
                //System.out.println("current: " + current.getX() + " " + current.getY());
                return calcPath(nodeMap[start[0]][start[1]], current);    
            }
            
            List<Node> adjacentNodes = getAdjacent(current, objective); //get nodes adjacent to current, for each of them:
            for (int i = 0; i < adjacentNodes.size(); i++){
                Node currentAdj = adjacentNodes.get(i);
                //System.out.print("cur Adjacent: ");
                //System.out.print(currentAdj.getX() + " ");
                //System.out.println(currentAdj.getY());
                if (!openList.contains(currentAdj)){     //if the openlist doesn't already contain current adjacent
                    currentAdj.setPrevious(current);
                    currentAdj.setDirection();
                    currentAdj.sethCost(nodeMap[objective[0]][objective[1]]);
                    currentAdj.setgCost(current);
                    //System.out.println(currentAdj.getgCost());
                    //System.out.println(currentAdj.gethCost());
                    openList.add(currentAdj);
                }
                else {      //else if in open list but suboptimal cost, update to new current and new cost
                    if (currentAdj.getgCost() > currentAdj.calcgCost(current)){
                        if (!current.isNotPrefered()){
                            currentAdj.setPrevious(current);
                            currentAdj.setDirection();
                            currentAdj.setgCost(current);
                        }
                    }
                }
            }
            
            if (openList.isEmpty())
                return new LinkedList<Node>(); //return empty list if no path exists
                                               //could just return path to where you stopped
        }
        return null; //literally unreachable
    }
    
    //found goal, calc exact path from current location to it, which is a list of coordinates
    private List<Node> calcPath(Node start, Node objective){
        LinkedList<Node> path = new LinkedList<Node>();
        boolean done = false;
        Node cur = objective;
        
        if (start.getX() == cur.getX() && start.getY() == cur.getY()){
            done = true;
            path.addFirst(cur);
            System.out.println("Already at objective.");
        }
        while (!done) {
            //path should not contain 2 of the same node, indicates an error so return empty 
            if (path.contains(cur)) 
                return new LinkedList<Node>();
            path.addFirst(cur);
            cur = cur.getPrevious();

            if ( Node.isEqual(cur, start)) {
                done = true;
            }
        }
        return path;
    }
    
    private Node lowestFInOpen(){
        // currently, this is done by going through the whole openList, can optimize with priority queue!
        Node lowestCost = openList.get(0);
        for (int i = 0; i < openList.size(); i++) {
            if (openList.get(i).getfCost() < lowestCost.getfCost()) {
                lowestCost = openList.get(i);
            }
        }
        return lowestCost;
    }
    
    //because fullExplore uses Astar on the map when its not completely explored
    //need to add condition not to get adjacent nodes that have not been explored
    //unless it is the objective given by determineNearestUnexplored()
    private List<Node> getAdjacent(Node n, int[] objective){
        int x = n.getX();
        int y = n.getY();
        int nearestUnexploredCounter = 0;
        List<Node> adjac = new LinkedList<Node>();     
        
        //Diagonal movement flags during horizontal/vertical checking, not implemented yet
        boolean _0 = false;
        boolean _3 = false;
        boolean _8 = false;
        boolean _11 = false;
        
        Node tmp;
        Node tmp1;
        Node tmp2;
        Node tmp3;
        
        // 0  1  2  3  4
        // 5  x  x  x  6   
        // 7  x  X  x  8   middle X = reference point
        // 9  x  x  x  10
        // 11 12 13 14 15
        //  <--- 
        if (x >= 2 && y < height - 1 && y > 0){
            tmp  = this.getNode((x - 1),  y);       //Tile between 7 and X 
            tmp1 = this.getNode((x - 2),  y);       //7
            tmp2 = this.getNode((x - 2), (y + 1));  //5
            tmp3 = this.getNode((x - 2), (y - 1));  //9
                
            //if it is not obstacle, not processed yet, and explored
            //and there is space for robot to move through, then add to the list
            if ( StateOfMap.NotObstacleIsExplored(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                if ( StateOfMap.NotObstacleIsExplored(tmp2.getX(), tmp2.getY()) && StateOfMap.NotObstacleIsExplored(tmp3.getX(), tmp3.getY()) ){
                    tmp.setIsDiagonally(false);
                    adjac.add(tmp);
                }
            }
            else{
                for (int i = 0; i < 9; i++){
                    if (Controller.nearestUnexplored[i][0] == tmp1.getX() && Controller.nearestUnexplored[i][1] == tmp1.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp2.getX() && Controller.nearestUnexplored[i][1] == tmp2.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp3.getX() && Controller.nearestUnexplored[i][1] == tmp3.getY())
                        nearestUnexploredCounter++;
                }
                if (nearestUnexploredCounter >= 3){
                    tmp.setIsDiagonally(false);
                    tmp.setIsNotPrefered(true);
                    adjac.add(tmp);
                }
                nearestUnexploredCounter = 0;
            }
            
            if (Controller.thereAreImpossibleNodesLeft){
                if ( !StateOfMap.isObstacleTile(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                    if ( !StateOfMap.isObstacleTile(tmp2.getX(), tmp2.getY()) && !StateOfMap.isObstacleTile(tmp3.getX(), tmp3.getY()) ){
                        tmp.setIsDiagonally(false);
                        adjac.add(tmp);
                    }
                }
            }
        }
        
        // 0  1  2  3  4
        // 5  x  x  x  6  
        // 7  x  X  x  8   middle X = reference point
        // 9  x  x  x  10
        // 11 12 13 14 15        
        // --->
        if (x < width - 2 && y < height - 1 && y > 0) {
            tmp  = this.getNode((x + 1),  y);       //Tile between 8 and X
            tmp1 = this.getNode((x + 2),  y);       //8
            tmp2 = this.getNode((x + 2), (y + 1));  //6
            tmp3 = this.getNode((x + 2), (y - 1));  //10
            
            //if (objective[0] == x+2 && objective[1] == y){
                //tmp.setIsDiagonally(false);
                //adjac.add(tmp);
                //adjac.add(tmp1);
            //}
            if ( StateOfMap.NotObstacleIsExplored(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                if ( StateOfMap.NotObstacleIsExplored(tmp2.getX(), tmp2.getY()) && StateOfMap.NotObstacleIsExplored(tmp3.getX(), tmp3.getY()) ){
                    tmp.setIsDiagonally(false);
                    adjac.add(tmp);
                }
            }
            else{
                for (int i = 0; i < 9; i++){
                    if (Controller.nearestUnexplored[i][0] == tmp1.getX() && Controller.nearestUnexplored[i][1] == tmp1.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp2.getX() && Controller.nearestUnexplored[i][1] == tmp2.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp3.getX() && Controller.nearestUnexplored[i][1] == tmp3.getY())
                        nearestUnexploredCounter++;
                }
                if (nearestUnexploredCounter >= 3){
                    tmp.setIsDiagonally(false);
                    tmp.setIsNotPrefered(true);
                    adjac.add(tmp);
                }
                nearestUnexploredCounter = 0;
            }
            
            if (Controller.thereAreImpossibleNodesLeft){
                if ( !StateOfMap.isObstacleTile(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                    if ( !StateOfMap.isObstacleTile(tmp2.getX(), tmp2.getY()) && !StateOfMap.isObstacleTile(tmp3.getX(), tmp3.getY()) ){
                        tmp.setIsDiagonally(false);
                        adjac.add(tmp);
                    }
                }
            }
        }

        // 0  1  2  3  4
        // 5  x  x  x  6   
        // 7  x  X  x  8   middle X = reference point
        // 9  x  x  x  10
        // 11 12 13 14 15        
        // down
        if (y >= 2 && x < width - 1 && x > 0) {
            tmp  = this.getNode( x, (y - 1));       //Tile between 13 and X
            tmp1 = this.getNode( x, (y - 2));       //13
            tmp2 = this.getNode((x - 1), (y - 2));  //12
            tmp3 = this.getNode((x + 1), (y - 2));  //14
            
            //if (objective[0] == x && objective[1] == y-2){
                //tmp.setIsDiagonally(false);
                //adjac.add(tmp);
                //adjac.add(tmp1);
            //}
            if ( StateOfMap.NotObstacleIsExplored(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                if ( StateOfMap.NotObstacleIsExplored(tmp2.getX(), tmp2.getY()) && StateOfMap.NotObstacleIsExplored(tmp3.getX(), tmp3.getY()) ){
                    tmp.setIsDiagonally(false);
                    adjac.add(tmp);
                }
            }
            else{
                for (int i = 0; i < 9; i++){
                    if (Controller.nearestUnexplored[i][0] == tmp1.getX() && Controller.nearestUnexplored[i][1] == tmp1.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp2.getX() && Controller.nearestUnexplored[i][1] == tmp2.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp3.getX() && Controller.nearestUnexplored[i][1] == tmp3.getY())
                        nearestUnexploredCounter++;
                }
                if (nearestUnexploredCounter >= 3){
                    tmp.setIsDiagonally(false);
                    tmp.setIsNotPrefered(true);
                    adjac.add(tmp);
                }
                nearestUnexploredCounter = 0;
            }
            
            if (Controller.thereAreImpossibleNodesLeft){
                if ( !StateOfMap.isObstacleTile(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                    if ( !StateOfMap.isObstacleTile(tmp2.getX(), tmp2.getY()) && !StateOfMap.isObstacleTile(tmp3.getX(), tmp3.getY()) ){
                        tmp.setIsDiagonally(false);
                        adjac.add(tmp);
                    }
                }
            }
        }
        
        // 0  1  2  3  4
        // 5  x  x  x  6   
        // 7  x  X  x  8    middle X = reference point  
        // 9  x  x  x  10
        // 11 12 13 14 15        
        // up
        if (y < height - 2 && x < width - 1 && x > 0) {
            tmp  = this.getNode( x, (y + 1));       //Tile between 2 and X
            tmp1 = this.getNode( x, (y + 2));       //2
            tmp2 = this.getNode((x - 1), (y + 2));  //1
            tmp3 = this.getNode((x + 1), (y + 2));  //3
            
            //if (objective[0] == x && objective[1] == y+2){
                //tmp.setIsDiagonally(false);
                //adjac.add(tmp);
                //adjac.add(tmp1);
            //}
            if ( StateOfMap.NotObstacleIsExplored(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                if ( StateOfMap.NotObstacleIsExplored(tmp2.getX(), tmp2.getY()) && StateOfMap.NotObstacleIsExplored(tmp3.getX(), tmp3.getY()) ){
                    tmp.setIsDiagonally(false);
                    adjac.add(tmp);
                }
            }
            else{
                for (int i = 0; i < 9; i++){
                    if (Controller.nearestUnexplored[i][0] == tmp1.getX() && Controller.nearestUnexplored[i][1] == tmp1.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp2.getX() && Controller.nearestUnexplored[i][1] == tmp2.getY())
                        nearestUnexploredCounter++;
                    if (Controller.nearestUnexplored[i][0] == tmp3.getX() && Controller.nearestUnexplored[i][1] == tmp3.getY())
                        nearestUnexploredCounter++;
                }
                if (nearestUnexploredCounter >= 3){
                    tmp.setIsDiagonally(false);
                    tmp.setIsNotPrefered(true);
                    adjac.add(tmp);
                }
                nearestUnexploredCounter = 0;
            }
            
            if (Controller.thereAreImpossibleNodesLeft){
                if ( !StateOfMap.isObstacleTile(tmp1.getX(), tmp1.getY()) && !closedList.contains(tmp) ){
                    if ( !StateOfMap.isObstacleTile(tmp2.getX(), tmp2.getY()) && !StateOfMap.isObstacleTile(tmp3.getX(), tmp3.getY()) ){
                        tmp.setIsDiagonally(false);
                        adjac.add(tmp);
                    }
                }
            }
        }
        
        //complicated implementation, not done yet just save for later if nothing else to do, NOT SAFE TO USE!!
        if (CANMOVEDIAGONALLY) {
            if (x < width && y < height) {
                tmp = this.getNode((x + 1), (y + 1));
                if ( (!tmp.isObstacle() && !closedList.contains(tmp)) && 
                        (tmp.isExplored() || (objective[0] == x && objective[1] == y )) ) {
                        tmp.setIsDiagonally(true);
                        adjac.add(tmp);
                    }
            }

            if (x > 0 && y > 0) {
                tmp = this.getNode((x - 1), (y - 1));
                if ( (!tmp.isObstacle() && !closedList.contains(tmp)) && 
                        (tmp.isExplored() || (objective[0] == x && objective[1] == y )) ) {
                        tmp.setIsDiagonally(true);
                        adjac.add(tmp);
                    }
            }

            if (x > 0 && y < height) {
                tmp = this.getNode((x - 1), (y + 1));
                if ( (!tmp.isObstacle() && !closedList.contains(tmp)) && 
                        (tmp.isExplored() || (objective[0] == x && objective[1] == y )) ) {
                        tmp.setIsDiagonally(true);
                        adjac.add(tmp);
                    }
            }

            if (x < width && y > 0) {
                tmp = this.getNode((x + 1), (y - 1));
                if ( (!tmp.isObstacle() && !closedList.contains(tmp)) && 
                        (tmp.isExplored() || (objective[0] == x && objective[1] == y )) ) {
                        tmp.setIsDiagonally(true);
                        adjac.add(tmp);
                    }
            }
        }
        //END of diagonal portion
        
        return adjac;
    }
    
    //the old 100% working method assuming robot is 1x1
    private List<Node> oldGetAdjacent(Node n, int[] objective){
        int x = n.getX();
        int y = n.getY();
        List<Node> adjac = new LinkedList<Node>();
        
        Node tmp;
        if (x > 0) {
            tmp = this.getNode((x - 1), y);
            if (!tmp.isObstacle() && !closedList.contains(tmp) && tmp.isExplored()) {
                tmp.setIsDiagonally(false);
                adjac.add(tmp);
            }
        }

        if (x < width - 1) {
            tmp = this.getNode((x + 1), y);
            if (!tmp.isObstacle() && !closedList.contains(tmp) && tmp.isExplored()) {
                tmp.setIsDiagonally(false);
                adjac.add(tmp);
            }
        }

        if (y > 0) {
            tmp = this.getNode(x, (y - 1));
            if (!tmp.isObstacle() && !closedList.contains(tmp) && tmp.isExplored()) {
                tmp.setIsDiagonally(false);
                adjac.add(tmp);
            }
        }

        if (y < height - 1) {
            tmp = this.getNode(x, (y + 1));
            if (!tmp.isObstacle() && !closedList.contains(tmp) && tmp.isExplored()) {
                 tmp.setIsDiagonally(false);
                adjac.add(tmp);
            }
        } 
        return adjac;
    }
    
}

