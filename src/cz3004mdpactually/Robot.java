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
public class Robot {
    //This class basically keeps track of where the robot is, it stores coordinates for all 3x3 tiles it occupies
    //There is also a method that refreshes the position of the robot after a movement, using the head to keep track
    
    //  0 | 1  | 2  | 3  | 4
    //  5 | R1 | R2 | R3 | 8   
    //  6 | R8 | R9 | R4 | 9
    //  7 | R7 | R6 | R5 | 10
    //    |    |    |    |
    
    public static int R1X = 0, R2X = 0, R3X = 0, R4X = 0, R5X = 0, R6X = 0, R7X = 0, R8X = 0, R9X = 0;
    public static int R1Y = 0, R2Y = 0, R3Y = 0, R4Y = 0, R5Y = 0, R6Y = 0, R7Y = 0, R8Y = 0, R9Y = 0;
    
    public static int Tile1X = 0, Tile2X = 0, Tile3X = 0;  //front of robot
    public static int Tile1Y = 0, Tile2Y = 0, Tile3Y = 0;    
    public static int Tile1EX = 0, Tile2EX = 0, Tile3EX = 0; //e means extended
    public static int Tile1EY = 0, Tile2EY = 0, Tile3EY = 0;
    
    public static int Tile5X = 0, Tile6X = 0, Tile7X = 0; //left of robot
    public static int Tile5Y = 0, Tile6Y = 0, Tile7Y = 0;
    public static int Tile5EX = 0, Tile6EX = 0, Tile7EX = 0;
    public static int Tile5EY = 0, Tile6EY = 0, Tile7EY = 0;
    
    public static int Tile8X = 0, Tile9X = 0, Tile10X = 0; //right of robot
    public static int Tile8Y = 0, Tile9Y = 0, Tile10Y = 0;
    public static int Tile8EX = 0, Tile9EX = 0, Tile10EX = 0;
    public static int Tile8EY = 0, Tile9EY = 0, Tile10EY = 0;
    
    public static int Tile0X = 0, Tile4X = 0; //diagonals, remain unused
    public static int Tile0Y = 0, Tile4Y = 0;
    
    //no need to track back of robot, because surely scanned for obstacles already
    //actual robot also doesn't have any back sensors
    
    //Basically, Controller will pass the header position after it's updated, this method will calibrate all the rest
    public static void defineRobotPosition(int x, int y){
        R2X = x;
        R2Y = y;

        switch (Direction.CUR_DIRECTION) {
            case Direction.DIRECTION_UP:
            //   |    | 1e | 2e | 3e |    |
            //   | 0  | 1  | 2  | 3  | 4  |
            // 5e| 5  | R1 | R2 | R3 | 8  | 8e 
            // 6e| 6  | R8 | R9 | R4 | 9  | 9e
            // 7e| 7  | R7 | R6 | R5 | 10 | 10e     
            //   |    |    |    |    |    |
                
                R1X = x - 1; 
                R1Y = y;          
                R3X = x + 1; 
                R3Y = y;
                R4X = x + 1;
                R4Y = y - 1;
                R5X = x + 1;
                R5Y = y - 2;
                R6X = x;
                R6Y = y - 2;
                R7X = x - 1;
                R7Y = y - 2;
                R8X = x - 1;
                R8Y = y - 1;
                R9X = x;
                R9Y = y - 1;
                
                Tile1X = x - 1;
                Tile1Y = y + 1;
                Tile2X = x;
                Tile2Y = y + 1;
                Tile3X = x + 1;
                Tile3Y = y + 1;                
                Tile5X = x - 2;
                Tile5Y = y;
                Tile6X = x - 2;
                Tile6Y = y - 1;
                Tile7X = x - 2;
                Tile7Y = y - 2;
                Tile8X = x + 2; 
                Tile8Y = y; 
                Tile9X = x + 2;
                Tile9Y = y - 1;
                Tile10X = x + 2;
                Tile10Y = y - 2;
                             
                Tile1EX = x - 1;
                Tile1EY = y + 2;
                Tile2EX = x;
                Tile2EY = y + 2;   
                Tile3EX = x + 1;
                Tile3EY = y + 2; 
                Tile5EX = x - 3;
                Tile5EY = y;
                Tile6EX = x - 3;
                Tile6EY = y - 1;   
                Tile7EX = x - 3;
                Tile7EY = y - 2;
                Tile8EX = x + 3;
                Tile8EY = y;
                Tile9EX = x + 3;
                Tile9EY = y - 1;   
                Tile10EX = x + 3;
                Tile10EY = y - 2;
                break;
                
            case Direction.DIRECTION_DOWN:                
            //     |    |    |    |    |    |
            // 10e | 10 | R5 | R6 | R7 | 7  | 7e
            //  9e | 9  | R4 | R9 | R8 | 6  | 6e
            //  8e | 8  | R3 | R2 | R1 | 5  | 5e
            //     | 4  | 3  | 2  | 1  | 0  |
            //     |    | 3e | 2e | 1e |    |
                
                R1X = x + 1; 
                R1Y = y;          
                R3X = x - 1; 
                R3Y = y;
                R4X = x - 1;
                R4Y = y + 1;
                R5X = x - 1;
                R5Y = y + 2;
                R6X = x;
                R6Y = y + 2;
                R7X = x + 1;
                R7Y = y + 2;
                R8X = x + 1;
                R8Y = y + 1;
                R9X = x;
                R9Y = y + 1;

                Tile1X = x + 1;
                Tile1Y = y - 1;
                Tile2X = x;
                Tile2Y = y - 1;
                Tile3X = x - 1;
                Tile3Y = y - 1;                
                Tile5X = x + 2;
                Tile5Y = y;
                Tile6X = x + 2;
                Tile6Y = y + 1;
                Tile7X = x + 2;
                Tile7Y = y + 2;
                Tile8X = x - 2;
                Tile8Y = y;
                Tile9X = x - 2;
                Tile9Y = y + 1;
                Tile10X = x - 2;
                Tile10Y = y + 2;      
                               
                Tile1EX = x + 1;
                Tile1EY = y - 2;
                Tile2EX = x;
                Tile2EY = y - 2;   
                Tile3EX = x - 1;
                Tile3EY = y - 2; 
                Tile5EX = x + 3;
                Tile5EY = y;
                Tile6EX = x + 3;
                Tile6EY = y + 1;   
                Tile7EX = x + 3;
                Tile7EY = y + 2;    
                Tile8EX = x - 3;
                Tile8EY = y;
                Tile9EX = x - 3;
                Tile9EY = y + 1;   
                Tile10EX = x - 3;
                Tile10EY = y + 2;
                break;

            case Direction.DIRECTION_LEFT:     
            //     |    | 8e | 9e | 10e
            //     | 4  | 8  | 9  | 10
            //  3e | 3  | R3 | R4 | R5
            //  2e | 2  | R2 | R9 | R6
            //  1e | 1  | R1 | R8 | R7
            //     | 0  | 5  | 6  | 7
            //     |    | 5e | 6e | 7e 
                               
                R1X = x; 
                R1Y = y - 1;          
                R3X = x; 
                R3Y = y + 1;
                R4X = x + 1;
                R4Y = y + 1;  
                R5X = x + 2;
                R5Y = y + 1;
                R6X = x + 2;
                R6Y = y;    
                R7X = x + 2; 
                R7Y = y - 1;
                R8X = x + 1;
                R8Y = y - 1;
                R9X = x + 1;
                R9Y = y;

                Tile1X = x - 1;
                Tile1Y = y - 1;
                Tile2X = x - 1;
                Tile2Y = y;
                Tile3X = x - 1;
                Tile3Y = y + 1;                
                Tile5X = x;
                Tile5Y = y - 2;
                Tile6X = x + 1;
                Tile6Y = y - 2;
                Tile7X = x + 2;
                Tile7Y = y - 2;
                Tile8X = x;
                Tile8Y = y + 2; 
                Tile9X = x + 1;
                Tile9Y = y + 2;
                Tile10X = x + 2;
                Tile10Y = y + 2;     
                
                Tile1EX = x - 2;
                Tile1EY = y - 1;
                Tile2EX = x - 2;
                Tile2EY = y;   
                Tile3EX = x - 2;
                Tile3EY = y + 1; 
                Tile5EX = x;
                Tile5EY = y - 3;
                Tile6EX = x + 1;
                Tile6EY = y - 3;   
                Tile7EX = x + 2;
                Tile7EY = y - 3;    
                Tile8EX = x;
                Tile8EY = y + 3;
                Tile9EX = x + 1;
                Tile9EY = y + 3;   
                Tile10EX = x + 2;
                Tile10EY = y + 3;
                break;
            
            case Direction.DIRECTION_RIGHT:  
            //    | 7e | 6e | 5e |    |
            //    | 7  | 6  | 5  | 0  |
            //    | R7 | R8 | R1 | 1  | 1e  
            //    | R6 | R9 | R2 | 2  | 2e
            //    | R5 | R4 | R3 | 3  | 3e
            //    | 10 | 9  | 8  | 4  |
            //    | 10e| 9e | 8e |    |
                
                R1X = x; 
                R1Y = y + 1;          
                R3X = x; 
                R3Y = y - 1;
                R4X = x - 1;
                R4Y = y - 1;  
                R5X = x - 2;
                R5Y = y - 1;
                R6X = x - 2;
                R6Y = y;    
                R7X = x - 2; 
                R7Y = y + 1;
                R8X = x - 1;
                R8Y = y + 1;
                R9X = x - 1;
                R9Y = y;
                
                Tile1X = x + 1;
                Tile1Y = y + 1;
                Tile2X = x + 1;
                Tile2Y = y;
                Tile3X = x + 1;
                Tile3Y = y - 1;                
                Tile5X = x;
                Tile5Y = y + 2;
                Tile6X = x - 1;
                Tile6Y = y + 2;
                Tile7X = x - 2;
                Tile7Y = y + 2;
                Tile8X = x;
                Tile8Y = y - 2; 
                Tile9X = x - 1;
                Tile9Y = y - 2;
                Tile10X = x - 2;
                Tile10Y = y - 2; 
                
                Tile1EX = x + 2;
                Tile1EY = y + 1;
                Tile2EX = x + 2;
                Tile2EY = y;   
                Tile3EX = x + 2;
                Tile3EY = y - 1; 
                Tile5EX = x;
                Tile5EY = y + 3;
                Tile6EX = x - 1;
                Tile6EY = y + 3;   
                Tile7EX = x - 2;
                Tile7EY = y + 3;    
                Tile8EX = x;
                Tile8EY = y - 3;
                Tile9EX = x - 1;
                Tile9EY = y - 3;   
                Tile10EX = x - 2;
                Tile10EY = y - 3;
                break;     
        }
    }
    
    
}
