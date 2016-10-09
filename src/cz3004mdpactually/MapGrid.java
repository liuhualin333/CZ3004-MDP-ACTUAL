/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.io.*;
import java.awt.*;
import javax.swing.*;
/**
 *
 * @author HuaBa
 */
public class MapGrid extends JPanel {
    private JPanel[][] grid = new JPanel[Mapsimulator.GRIDWIDTH][Mapsimulator.GRIDHEIGHT];
    private JLabel[][] labels = new JLabel[Mapsimulator.GRIDWIDTH][Mapsimulator.GRIDHEIGHT]; 
    private int previousX = 1;
    private int previousY = 1;
    public MapGrid(){
        init();
    }
    private void init(){
        setLayout(new GridLayout(Mapsimulator.GRIDHEIGHT, Mapsimulator.GRIDWIDTH));
        //java draws the map from top down, left to right, doesn't affect X axis, but Y axis is inverted
        //hence invert j to draw the final node first on the Y axis, go from 20->0 instead of 0->20
        for (int j = Mapsimulator.GRIDHEIGHT - 1; j >= 0; j--){
            for (int i = 0; i < Mapsimulator.GRIDWIDTH; i++){
                GridCell cell = new GridCell(i, j);
                labels[i][j] = new JLabel();
                grid[i][j] = cell;
                cell.setBorder(BorderFactory.createLineBorder(Color.black));
                if(StateOfMap.exploredMap[i][j]==0){
                    cell.setBackground(Color.ORANGE);
                }
                else{
                    cell.setBackground(Color.WHITE); 
                    if (StateOfMap.obstacleMap[i][j] == 1){
                        cell.setBackground(Color.DARK_GRAY);    
                    }
                }
                if(i>=Controller.startZoneLocation[0]-1 && i <= Controller.startZoneLocation[0]+1 && j>=Controller.startZoneLocation[1]-1 && j<=Controller.startZoneLocation[1]+1){
                    //System.out.println(startZoneLocation[0]+""+startZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    if(i == Controller.startZoneLocation[0] && j == Controller.startZoneLocation[1]){
                        labels[i][j].setText("S");
                    }
                    grid[i][j] = cell;
                }
                else if(i>=Controller.goalZoneLocation[0]-1 && i <= Controller.goalZoneLocation[0]+1 && j>=Controller.goalZoneLocation[1]-1 && j<=Controller.goalZoneLocation[1]+1){
                    //System.out.println(Controller.goalZoneLocation[0]+""+Controller.goalZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    if(i == Controller.goalZoneLocation[0] && j == Controller.goalZoneLocation[1]){
                        labels[i][j].setText("E");
                    }
                    grid[i][j] = cell;
                }
                grid[i][j].add(labels[i][j]);
                add(cell);
            }
        }
    }
 
    public void paintRobotLocation(int x, int y){
        cleanup();

        previousX = x;
        previousY = y;
        for (int i = x-1; i <= x+1; i++){
            for(int j = y-1; j <= y+1; j++){
                if(getGrid(i,j).getBackground()!=Color.green){
                    getGrid(i,j).setBackground(Color.red);
                    if(i == Robot.R2X && j == Robot.R2Y){
                        getGrid(i,j).setBackground(Color.green);
                    }
                    getGrid(i,j).revalidate();
                    getGrid(i,j).repaint();
                }
            }
        }
        
    }
    public void cleanup(){
        int x = previousX;
        int y = previousY;
        for (int i = x-1; i <= x+1; i++){
            for(int j = y-1; j <= y+1; j++){
                if(((GridCell)getGrid(i,j)).getEnabled()){
                    getGrid(i,j).setBackground(Color.white);
                    getGrid(i,j).revalidate();
                    getGrid(i,j).repaint();
                }
                else{
                    getGrid(i,j).setBackground(Color.CYAN);
                    getGrid(i,j).revalidate();
                    getGrid(i,j).repaint();
                }
            }
        }
    }
    public JPanel getGrid(int x, int y){
        JPanel gridRequired = grid[x][y];
        return gridRequired;
    }

    public void updateDescriptor(int x, int y, int value){
        if (((GridCell)grid[x][y]).getEnabled() == true){
            if(StateOfMap.exploredMap[x][y] == 1){                   
                if(StateOfMap.obstacleMap[x][y] == 1){
                    grid[x][y].setBackground(Color.DARK_GRAY);
                }
                else {
                    grid[x][y].setBackground(Color.WHITE);
                }
            }
            else{
                grid[x][y].setBackground(Color.ORANGE);
            }
        }
        else if(((GridCell)grid[x][y]).getEnabled() == false){
                    grid[x][y].setBackground(Color.CYAN);
                }
    }
            
}
                
