/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
/**
 *
 * @author HuaBa
 */
public class Mapsimulator  extends JFrame {

	// GUI Widgets
        public static final int WIDTH = 500;
        public static final int HEIGHT = 1200;
        public static final int GRIDWIDTH = 15;
        public static final int GRIDHEIGHT = 20;
        //public static Map map1 = new Map(20,15);
        JFrame frame;
        MapGrid contentPanel;
	public Mapsimulator(){
            frame = new JFrame();
            frame.setTitle("Algorithm Simulation");
            contentPanel = new MapGrid();
            init();
        }
	public void init() {
                frame.setSize(WIDTH, HEIGHT);
		frame.setLayout(new BorderLayout());
                Container content = frame.getContentPane();
                content.add(contentPanel, BorderLayout.CENTER);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	} // init

} 


