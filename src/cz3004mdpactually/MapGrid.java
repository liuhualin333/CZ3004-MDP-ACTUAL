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
    public MapGrid(int code){
        if(code == 1){
            explorationInit1();
        }
        else if(code == 2){
            explorationInit2();
        }
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
                if(i>=Controller.startZoneLocation[0]-1 && i <= Controller.startZoneLocation[0]+1 && j>=Controller.startZoneLocation[1]-1 && j<=Controller.startZoneLocation[1]+1){
                    //System.out.println(startZoneLocation[0]+""+startZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);                    
                    if(i == Controller.startZoneLocation[0] && j == Controller.startZoneLocation[1]){
                        labels[i][j].setText("S");
                    }
                    grid[i][j] = cell;
                }
                if(i>=Controller.goalZoneLocation[0]-1 && i <= Controller.goalZoneLocation[0]+1 && j>=Controller.goalZoneLocation[1]-1 && j<=Controller.goalZoneLocation[1]+1){
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
    private void explorationInit1(){
         setLayout(new GridLayout(Mapsimulator.GRIDHEIGHT, Mapsimulator.GRIDWIDTH));
        
        //java draws the map from top down, left to right, doesn't affect X axis, but Y axis is inverted
        //hence invert j to draw the final node first on the Y axis, go from 20->0 instead of 0->20
        for (int j = Mapsimulator.GRIDHEIGHT - 1; j >= 0; j--){
            for (int i = 0; i < Mapsimulator.GRIDWIDTH; i++){
                GridCell cell = new GridCell(i, j);
                labels[i][j] = new JLabel();
                grid[i][j] = cell;
                cell.setBorder(BorderFactory.createLineBorder(Color.black));
                if(i>=Controller.startZoneLocation[0]-1 && i <= Controller.startZoneLocation[0]+1 && j>=Controller.startZoneLocation[1]-1 && j<=Controller.startZoneLocation[1]+1){
                    //System.out.println(startZoneLocation[0]+""+startZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    labels[i][j].setText("1");
                    if(i == Controller.startZoneLocation[0] && j == Controller.startZoneLocation[1]){
                        labels[i][j].setText("S");
                    }
                    grid[i][j] = cell;
                }
                else if(i>=Controller.goalZoneLocation[0]-1 && i <= Controller.goalZoneLocation[0]+1 && j>=Controller.goalZoneLocation[1]-1 && j<=Controller.goalZoneLocation[1]+1){
                    //System.out.println(Controller.goalZoneLocation[0]+""+Controller.goalZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    labels[i][j].setText("1");
                    if(i == Controller.goalZoneLocation[0] && j == Controller.goalZoneLocation[1]){
                        labels[i][j].setText("E");
                    }
                    grid[i][j] = cell;
                }
                if(StateOfMap.exploredMap[i][j]==0){
                    cell.setBackground(Color.ORANGE);
                    labels[i][j].setText("0");
                }
                else{
                    cell.setBackground(Color.WHITE); 
                    labels[i][j].setText("1");
                    if (StateOfMap.obstacleMap[i][j] == 1){
                        cell.setBackground(Color.DARK_GRAY);    
                    }
                }
                grid[i][j].add(labels[i][j]);
                add(cell);
            }
        }
        
    }
    private void explorationInit2(){
         setLayout(new GridLayout(Mapsimulator.GRIDHEIGHT, Mapsimulator.GRIDWIDTH));
        
        //java draws the map from top down, left to right, doesn't affect X axis, but Y axis is inverted
        //hence invert j to draw the final node first on the Y axis, go from 20->0 instead of 0->20
        for (int j = Mapsimulator.GRIDHEIGHT - 1; j >= 0; j--){
            for (int i = 0; i < Mapsimulator.GRIDWIDTH; i++){
                GridCell cell = new GridCell(i, j);
                
                labels[i][j] = new JLabel();
                grid[i][j] = cell;
                cell.setBorder(BorderFactory.createLineBorder(Color.black));
                if(i>=Controller.startZoneLocation[0]-1 && i <= Controller.startZoneLocation[0]+1 && j>=Controller.startZoneLocation[1]-1 && j<=Controller.startZoneLocation[1]+1){
                    //System.out.println(startZoneLocation[0]+""+startZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    labels[i][j].setText("0");
                    if(i == Controller.startZoneLocation[0] && j == Controller.startZoneLocation[1]){
                        labels[i][j].setText("S");
                    }
                    grid[i][j] = cell;
                }
                else if(i>=Controller.goalZoneLocation[0]-1 && i <= Controller.goalZoneLocation[0]+1 && j>=Controller.goalZoneLocation[1]-1 && j<=Controller.goalZoneLocation[1]+1){
                    //System.out.println(Controller.goalZoneLocation[0]+""+Controller.goalZoneLocation[1]);
                    cell.setEnable(false);
                    cell.setBackground(Color.CYAN);
                    labels[i][j].setText("0");
                    if(i == Controller.goalZoneLocation[0] && j == Controller.goalZoneLocation[1]){
                        labels[i][j].setText("E");
                    }
                    grid[i][j] = cell;
                }
                if(StateOfMap.exploredMap[i][j]==0){
                    cell.setBackground(Color.ORANGE);
                }
                else{
                    cell.setBackground(Color.WHITE); 
                    labels[i][j].setText("0");
                    if (StateOfMap.obstacleMap[i][j] == 1){
                        cell.setBackground(Color.DARK_GRAY);    
                        labels[i][j].setText("1");
                    }
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
    public void refresh (){
        for (int j = Mapsimulator.GRIDHEIGHT - 1; j >= 0; j--){
            for (int i = 0; i < Mapsimulator.GRIDWIDTH; i++){
                grid[i][j].setBackground(Color.WHITE);
                    if(i>=Controller.startZoneLocation[0]-1 && i <= Controller.startZoneLocation[0]+1 && j>=Controller.startZoneLocation[1]-1 && j<=Controller.startZoneLocation[1]+1){
                        //System.out.println(startZoneLocation[0]+""+startZoneLocation[1]);
                        grid[i][j].setBackground(Color.CYAN);
                    }
                    if(i>=Controller.goalZoneLocation[0]-1 && i <= Controller.goalZoneLocation[0]+1 && j>=Controller.goalZoneLocation[1]-1 && j<=Controller.goalZoneLocation[1]+1){
                        //System.out.println(Controller.goalZoneLocation[0]+""+Controller.goalZoneLocation[1]);
                        grid[i][j].setBackground(Color.CYAN);
                    }
                    if(Controller.map.getNode(i, j).isObstacle() == true){
                        grid[i][j].setBackground(Color.DARK_GRAY);
                    }
                if(Controller.map.getNode(i, j).isExplored() == false){
                    grid[i][j].setBackground(Color.ORANGE);
                }
                grid[i][j].validate();
                grid[i][j].repaint();
            }
        }
        Controller.currentLocation[0] = 1;
        Controller.currentLocation[1] = 1;
    }
    public void refresh1(){
        for (int i = 0; i < Mapsimulator.GRIDWIDTH; i++){
            for (int j = 0; j < Mapsimulator.GRIDHEIGHT;j++){
                if(((GridCell)grid[i][j]).getEnabled()){
                    Controller.map.setIsExplored(i, j, false);
                    StateOfMap.exploredMap[i][j] = 0;
                }
                else {
                    Controller.map.setIsExplored(i, j, true);
                    StateOfMap.exploredMap[i][j] = 1;
                }
//                Controller.mapsimulator.gridPanelDescriptor1.updateDescriptor1(i, j, 0);
//                Controller.mapsimulator.gridPanelDescriptor2.updateDescriptor2(i, j, 0);
            }

        }
        refresh();
    }
    public void updateDescriptor1(int x, int y, int value){
        if (((GridCell)grid[x][y]).getEnabled() == true){
            if(StateOfMap.exploredMap[x][y] == 1){
                labels[x][y].setText("1");                    
                if(StateOfMap.obstacleMap[x][y] == 1){
                    grid[x][y].setBackground(Color.DARK_GRAY);
                }
                else {
                    grid[x][y].setBackground(Color.WHITE);
                }
            }
            else{
                labels[x][y].setText("0");
                grid[x][y].setBackground(Color.ORANGE);
            }
        }
                        else if(((GridCell)grid[x][y]).getEnabled() == false){
                    grid[x][y].setBackground(Color.CYAN);
                }
    }
    public void updateDescriptor2(int x, int y, int value){
        if(StateOfMap.exploredMap[x][y] == 1){
            if(StateOfMap.obstacleMap[x][y] == 1){
                grid[x][y].setBackground(Color.DARK_GRAY);
                labels[x][y].setText("1");
            }
            else if(((GridCell)grid[x][y]).getEnabled() == false){
                labels[x][y].setText("0");
                grid[x][y].setBackground(Color.CYAN);
            }
            else {
                grid[x][y].setBackground(Color.WHITE);
                labels[x][y].setText("0");
            }
        }
        else{
            grid[x][y].setBackground(Color.ORANGE);
        }
    }
            
    public void loadFile() throws IOException{
        String loadLine1;
        String loadLine2;
        String binaryLine = "";
        int exploredNum = 0;
        int count = 0;
        File file = new File("Mapfilepart1.txt");
        File file1 = new File("Mapfilepart2.txt");
        Controller.map = new Map(15,20);
        //TODO: ADD SOME PADDING
        Controller.currentLocation[0] = 1;
        Controller.currentLocation[1] = 1;
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            loadLine1 = reader.readLine();
        }
        System.out.println(loadLine1);
        for (int i = 0; i < loadLine1.length(); i++){
            int number = Integer.parseInt(loadLine1.charAt(i)+"",16);
            String tmp = "";
            if(tmp.length()<4){
                tmp = String.format("%4s", Integer.toBinaryString(number)).replace(' ', '0');
            }
            binaryLine += tmp;
        }
        System.out.println(binaryLine);
        System.out.println(binaryLine.length());
        for (int i = 2; i < binaryLine.length()-2; i++){
            
            if(binaryLine.charAt(i) == '0'){
                Controller.map.getNode((i-2)%15, (i-2)/15).setIsExplored(false);
                StateOfMap.exploredMap[(i-2)%15][(i-2)/15] = 0;
            }
            else if(binaryLine.charAt(i) == '1'){
                Controller.map.getNode((i-2)%15, (i-2)/15).setIsExplored(true);
                StateOfMap.exploredMap[(i-2)%15][(i-2)/15] = 1;
                exploredNum +=1;
            }

        }
        try(BufferedReader reader = new BufferedReader(new FileReader(file1))){
            loadLine2 = reader.readLine();
        }
        binaryLine = "";
        System.out.println(loadLine2);
        for (int i = 0; i < loadLine2.length(); i++){
            int number = Integer.parseInt(loadLine2.charAt(i)+"",16);
            String tmp = "";
            if(tmp.length()<4){
                tmp = String.format("%4s", Integer.toBinaryString(number)).replace(' ', '0');
            }
            binaryLine += tmp;
        }
        for (int j = 0; j< Mapsimulator.GRIDHEIGHT; j++){
            for (int i = 0; i< Mapsimulator.GRIDWIDTH; i++){
                if(Controller.map.getNode(i, j).isExplored()){
                    if(binaryLine.charAt(count) == '1'){
                        Controller.map.getNode(i, j).setIsObstacle(true);
                        StateOfMap.obstacleMap[i][j] = 1;
                        System.out.println("Set Obstacle");

                    }
                    count++;
                }
            }
        }
        for (int i = 0; i < Mapsimulator.GRIDWIDTH; i ++){
            for (int j = 0; j< Mapsimulator.GRIDHEIGHT; j++){
//                Controller.mapsimulator.gridPanelDescriptor1.updateDescriptor1(i, j, 0);
//                Controller.mapsimulator.gridPanelDescriptor2.updateDescriptor2(i, j, 0);
            }
        }
//                if(count != exploredNum){
//                    System.out.println("Something goes wrong");
//                    System.out.println(count);
//                    System.out.println(exploredNum);
//                }
        refresh();
    }
    public void saveFile2() throws IOException{
//        File file = new File("Mapfilepart1.txt");
        File file1 = new File("Mapfilepart2.txt");
//        file.createNewFile();
        file1.createNewFile();
//                String part1String = "";
        String part2String = "";
        String tmpString = "";
//                String tmpPart1Str = "";
        String tmpPart2Str = "";
        for(int j = 0; j< Mapsimulator.GRIDHEIGHT; j++){
            for(int i = 0; i< Mapsimulator.GRIDWIDTH; i++){
                Node node = Controller.map.getNode(i, j);
                if(node.isExplored()){
                    if(node.isObstacle()){
                        part2String+="1";
                    }
                    else{
                        part2String+="0";
                    }//no need of part1String
                }
            }
        }
        //padding part
//                part1String = "11"+part1String+"11";
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
        System.out.println(tmpString);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file1))){
            writer.write(tmpString);
            writer.flush();
            writer.close();
        }
    }
    public void saveFile1() throws IOException{
        File file = new File("Mapfilepart1.txt");
//        File file1 = new File("Mapfilepart2.txt");
        file.createNewFile();
//        file1.createNewFile();
        String part1String = "";
//        String part2String = "";
        String tmpString = "";
        String tmpPart1Str = "";
//        String tmpPart2Str = "";
        for(int j = 0; j < Mapsimulator.GRIDHEIGHT; j++){
            for(int i = 0; i< Mapsimulator.GRIDWIDTH; i++){
                Node node = Controller.map.getNode(i, j);
                if(node.isExplored()){
                    part1String+="1";
                }
                else{
                    part1String+="0";
                }//no need of part1String
            }
        }
        //padding part
        part1String = "11"+part1String+"11";
        System.out.println(part1String);
//        for (int i = 0; i < part2String.length()%4; i++){
//            part2String = part2String+"0";
//        }
        for (int i = 0 ; i < part1String.length(); i++){
            tmpPart1Str += part1String.charAt(i);
            if(tmpPart1Str.length() == 4){
                int number = Integer.parseInt(tmpPart1Str,2);
                String hexStr = Integer.toString(number,16);
                tmpString += hexStr;
                tmpPart1Str = "";
            }

        }
        System.out.println(tmpString);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write(tmpString);
            writer.flush();
            writer.close();
        }
        tmpString = "";
    }
}
                
