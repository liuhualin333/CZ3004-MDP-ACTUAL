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
public class Direction {
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_DOWN = 3;
    public static final int DIRECTION_LEFT = 4;
    public static int CUR_DIRECTION = DIRECTION_RIGHT;
    
    public static final int TURN_RIGHT = 1;
    public static final int TURN_LEFT = 2;
    
    /*public static String getCurDirChar() {
        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
                return "T";
            case Direction.DIRECTION_RIGHT:
                return "R";
            case Direction.DIRECTION_DOWN:
                return "B";
            case Direction.DIRECTION_LEFT:
                return "L";
        }
        return "T";
    }
    
    public static int getDirInt(String sDir) {
        switch (sDir) 
            case "t":
                return Direction.DIRECTION_UP;
            case "b":
                return Direction.DIRECTION_DOWN;
            case "l":
                return Direction.DIRECTION_LEFT;
            case "r":
                return Direction.DIRECTION_RIGHT;
        }
        return 1;
    }*/
}
