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
    //change all in astar to use this instead of map.isObstacle()
    public static int[][] obstacleMap = new int[Controller.width][Controller.height];
    //explored map here is merely to aid the descriptor functions, map nodes also have isExplored()
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
    
    public static boolean NotObstacleIsExplored(int x, int y) {
        return isValidTile(x, y) && isExploredTile(x, y) && !isObstacleTile(x, y);
    }
    
    public static void setObstacleTile(int x, int y, int value){
        if ( isValidTile(x,y) )
            obstacleMap[x][y] = value;
//        if ( !Controller.updateList.contains(Controller.map.getNode(x, y)))
//            Controller.updateList.add(Controller.map.getNode(x, y));
    }
    
    public static void setExploredTile(int x, int y, int value){
        if ( isValidTile(x,y) )
            exploredMap[x][y] = value;
//        if ( !Controller.updateList.contains(Controller.map.getNode(x, y)))
//            Controller.updateList.add(Controller.map.getNode(x, y));
    }
    
//    public static void updateDescriptor(int x, int y, int value){
//        CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor2.updateDescriptor2(x, y, value);
//        CZ3004MDPACTUALLY.controller.mapsimulator.gridPanelDescriptor1.updateDescriptor1(x, y, value);
//    }
    
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
}
