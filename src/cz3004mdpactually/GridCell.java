/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author HuaBa
 */
public class GridCell extends JPanel{
    private int x;
    private int y;
    boolean enableFlag = true;
    public GridCell (int x, int y){
        this.x = x;
        this.y = y;
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                if(SwingUtilities.isLeftMouseButton(e)){
                    if(enableFlag){
                        if(getBackground() == Color.DARK_GRAY){
                            setBackground(Color.WHITE);
                            Controller.map.setIsObstacle(x, y, false);
                            //System.out.println(x); //for testing
                            //System.out.println(y);
                            //System.out.println(Controller.map.getNode(x, y).isObstacle());
                        }
                        else if (getBackground() == Color.WHITE){
                            setBackground(Color.DARK_GRAY);
                            Controller.map.setIsObstacle(x, y, true);
                            //System.out.println(x);
                            //System.out.println(y);
                            //System.out.println(Controller.map.getNode(x, y).isObstacle());
                        }
                    }
                }
                else if (SwingUtilities.isRightMouseButton(e)){
                    if(getBackground() == Color.ORANGE){
                        if(Controller.map.getNode(x, y).isObstacle()){
                            setBackground(Color.DARK_GRAY);
                        }
                        else{
                            setBackground(Color.WHITE);
                        }
                        Controller.map.setIsExplored(x, y, true);
                        //System.out.println(x); //for testing
                        //System.out.println(y);
                        //System.out.println(Controller.map.getNode(x, y).isObstacle());
                    }
                    else{
                        setBackground(Color.ORANGE);
                        Controller.map.setIsExplored(x, y, false);
                        //System.out.println(x);
                        //System.out.println(y);
                        //System.out.println(Controller.map.getNode(x, y).isObstacle());
                    }
                }
            }    
        });
    }
    public void setEnable(boolean enableFlag){
        this.enableFlag = enableFlag;
    }
    public boolean getEnabled(){
        return this.enableFlag;
    }
}
