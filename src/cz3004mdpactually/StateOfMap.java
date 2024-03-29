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
public class StateOfMap {
    //obstacle map is initialized to all false, and will be marked as exploration goes along
    public static int[][] obstacleMap = new int[Controller.width][Controller.height];
    public static int[][] exploredMap = new int[Controller.width][Controller.height];
    
    public static boolean isValidTile(int x, int y){
        return ( (x >= 0 && x < Controller.width) && (y >= 0 && y < Controller.height) );
    }
    
    public static boolean isReachableTile(int x, int y){
        return ( (x > 0 && x < Controller.width - 1) && (y > 0 && y < Controller.height - 1) );
    }
    
    public static boolean isObstacleTile(int x, int y){
        return (obstacleMap[x][y] == 1);
    }
    
    public static boolean isExploredTile(int x, int y){
        return (exploredMap[x][y] == 1);
    }
    public static boolean validNotObstacle (int x, int y) {
        return isValidTile(x, y) && !isObstacleTile(x, y);
    }
    public static boolean NotObstacleIsExplored(int x, int y) {
        return isValidTile(x, y) && isExploredTile(x, y) && !isObstacleTile(x, y);
    }
    
    public static boolean goalOrStart (int x, int y) {
        
        switch(x){
            case 0:
                if (y == 0 || y == 1 || y == 2)
                    return true;
                break;
            case 1:
                if (y == 0 || y == 1 || y == 2)
                    return true;
                break;
            case 2:
                if (y == 0 || y == 1 || y == 2)
                    return true;
                break;
            case 12:
                if (y == 17 || y == 18 || y == 19)
                    return true;
                break;
            case 13:
                if (y == 17 || y == 18 || y == 19)
                    return true;
                break;
            case 14:
                if (y == 17 || y == 18 || y == 19)
                    return true;
                break;
            default:
                return false;
        }
        return false;
    }
    
    public static void setObstacleTile(int x, int y, int value){
        if ( isValidTile(x,y) ){
            if ( !goalOrStart(x,y) )
                obstacleMap[x][y] = value;
        }
//        if ( !Controller.updateList.contains(Controller.map.getNode(x, y)))
//            Controller.updateList.add(Controller.map.getNode(x, y));
    }
    
    public static void setExploredTile(int x, int y, int value){
        if ( isValidTile(x,y) )
            exploredMap[x][y] = value;
//        if ( !Controller.updateList.contains(Controller.map.getNode(x, y)))
//            Controller.updateList.add(Controller.map.getNode(x, y));
    }
    
    public static void updateDescriptor(int x, int y, int value){
        CZ3004MDPACTUALLY.controller.mapsimulator.contentPanel.updateDescriptor(x, y, value);
    }
    
    //Below methods used for robot movement checking, especially for front
    public static boolean frontIsTraversable() {
        if (frontIsBorder()) {
            return false;
        }
        return (obstacleMap[Robot.Tile1X][Robot.Tile1Y] == 0 && 
                obstacleMap[Robot.Tile2X][Robot.Tile2Y] == 0 && 
                obstacleMap[Robot.Tile3X][Robot.Tile3Y] == 0);
    }

    public static boolean frontIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile2Y >= Controller.height) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile2Y < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile2X < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile2X >= Controller.width) {
                    return true;
                }
                break;
        }
        return false;
    }
    
    public static boolean frontPlusOneIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile2EY >= Controller.height) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile2EY < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile2EX < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile2EX >= Controller.width) {
                    return true;
                }
                break;
        }
        return false;
    }
    
    public static boolean canCalibrateFront() {
        if (frontIsBorder())
            return true;
        if ( //obstacleMap[Robot.Tile1X][Robot.Tile1Y] == 1 && 
             obstacleMap[Robot.Tile2X][Robot.Tile2Y] == 1 )//&& 
             //obstacleMap[Robot.Tile3X][Robot.Tile3Y] == 1 )
            return true;
//        if (frontPlusOneIsBorder()){
//            if ( NotObstacleIsExplored(Robot.Tile1X, Robot.Tile1Y) && 
//                 NotObstacleIsExplored(Robot.Tile2X, Robot.Tile2Y) && 
//                 NotObstacleIsExplored(Robot.Tile3X, Robot.Tile3Y) )
//                return true;
//            else 
//                return false;
//        }
//        if ( NotObstacleIsExplored(Robot.Tile1X, Robot.Tile1Y) && 
//             NotObstacleIsExplored(Robot.Tile2X, Robot.Tile2Y) && 
//             NotObstacleIsExplored(Robot.Tile3X, Robot.Tile3Y) &&
//             obstacleMap[Robot.Tile1EX][Robot.Tile1EY] == 1 && 
//             obstacleMap[Robot.Tile2EX][Robot.Tile2EY] == 1 && 
//             obstacleMap[Robot.Tile3EX][Robot.Tile3EY] == 1     
//           )
//            return true;
        return false;
    } 

    public static boolean leftIsTraversable() {
        if (leftIsBorder()) {
            return false;
        }
        return (obstacleMap[Robot.Tile5X][Robot.Tile5Y] == 0 && 
                obstacleMap[Robot.Tile6X][Robot.Tile6Y] == 0 && 
                obstacleMap[Robot.Tile7X][Robot.Tile7Y] == 0);
    }

    public static boolean leftIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile6X < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile6X >= Controller.width) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile6Y < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile6Y >= Controller.height) {
                    return true;
                }
                break;                
        }
        return false;
    }
    
    public static boolean leftPlusOneIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile6EX < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile6EX >= Controller.width) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile6EY < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile6EY >= Controller.height) {
                    return true;
                }
                break;                
        }
        return false;
    }
    
    public static boolean canCalibrateLeft() {
        if (leftIsBorder())
            return true;
        if ( obstacleMap[Robot.Tile5X][Robot.Tile5Y] == 1 && 
             obstacleMap[Robot.Tile6X][Robot.Tile6Y] == 1 && 
             obstacleMap[Robot.Tile7X][Robot.Tile7Y] == 1 )
            return true;
//        if (leftPlusOneIsBorder()){
//            if ( NotObstacleIsExplored(Robot.Tile5X, Robot.Tile5Y) && 
//                 NotObstacleIsExplored(Robot.Tile6X, Robot.Tile6Y) && 
//                 NotObstacleIsExplored(Robot.Tile7X, Robot.Tile7Y) )
//                return true;
//            else 
//                return false;
//        }
//        if ( NotObstacleIsExplored(Robot.Tile5X, Robot.Tile5Y) && 
//             NotObstacleIsExplored(Robot.Tile6X, Robot.Tile6Y) && 
//             NotObstacleIsExplored(Robot.Tile7X, Robot.Tile7Y) &&
//             obstacleMap[Robot.Tile5EX][Robot.Tile5EY] == 1 && 
//             obstacleMap[Robot.Tile6EX][Robot.Tile6EY] == 1 && 
//             obstacleMap[Robot.Tile7EX][Robot.Tile7EY] == 1     
//           )
//            return true;
        return false;
    } 

    public static boolean rightIsTraversable() {
        if (rightIsBorder()) {
            return false;
        }
        return (obstacleMap[Robot.Tile8X][Robot.Tile8Y] == 0 && 
                obstacleMap[Robot.Tile9X][Robot.Tile9Y] == 0 && 
                obstacleMap[Robot.Tile10X][Robot.Tile10Y] == 0);
    }

    public static boolean rightIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile9X >= Controller.width) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile9X < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile9Y >= Controller.height) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile9Y < 0) {
                    return true;
                }
                break;
        }
        return false;
    }
    
    public static boolean rightPlusOneIsBorder() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                if (Robot.Tile9EX >= Controller.width) {
                    return true;
                }
                break;
            case Direction.DIRECTION_DOWN:
                if (Robot.Tile9EX < 0) {
                    return true;
                }
                break;
            case Direction.DIRECTION_LEFT:
                if (Robot.Tile9EY >= Controller.height) {
                    return true;
                }
                break;
            case Direction.DIRECTION_RIGHT:
                if (Robot.Tile9EY < 0) {
                    return true;
                }
                break;
        }
        return false;
    }
    
    public static boolean canCalibrateRight() {
        if (rightIsBorder())
            return true;
        if ( obstacleMap[Robot.Tile8X][Robot.Tile8Y] == 1 && 
             obstacleMap[Robot.Tile9X][Robot.Tile9Y] == 1 && 
             obstacleMap[Robot.Tile10X][Robot.Tile10Y] == 1 )
            return true;
//        if (rightPlusOneIsBorder()){
//            if ( NotObstacleIsExplored(Robot.Tile8X, Robot.Tile8Y) && 
//                 NotObstacleIsExplored(Robot.Tile9X, Robot.Tile9Y) && 
//                 NotObstacleIsExplored(Robot.Tile10X, Robot.Tile10Y) )
//                return true;
//            else 
//                return false;
//        }
//        if ( NotObstacleIsExplored(Robot.Tile8X, Robot.Tile8Y) && 
//             NotObstacleIsExplored(Robot.Tile9X, Robot.Tile9Y) && 
//             NotObstacleIsExplored(Robot.Tile10X, Robot.Tile10Y) &&
//             obstacleMap[Robot.Tile8EX][Robot.Tile8EY] == 1 && 
//             obstacleMap[Robot.Tile9EX][Robot.Tile9EY] == 1 && 
//             obstacleMap[Robot.Tile10EX][Robot.Tile10EY] == 1     
//           )
//            return true;
        return false;
    } 
}
