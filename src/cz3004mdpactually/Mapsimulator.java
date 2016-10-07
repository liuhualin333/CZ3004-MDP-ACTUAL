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
        JFrame explorationDescriptor1;
        JFrame explorationDescriptor2;
        MapGrid contentPanel;
        public MapGrid gridPanelDescriptor1;
        public MapGrid gridPanelDescriptor2;
        JPanel controlPanel;
        JPanel functionalityPanel;
        JPanel filePanel;
        JPanel speedAndPercent;
        JPanel descriptor1filePanel;
        JPanel descriptor2filePanel;
        JPanel descriptor1ContrPanel;
        JPanel descriptor2ContrPanel;
        JComboBox speedlist;
        JComboBox percentlist;
        JComboBox timelist;
        JPanel speedPanel;
        JPanel percentPanel;
        JPanel timePanel;
        JLabel speedLabel;
        JLabel percentLabel;
        JLabel timeLabel;
        JButton exploration;
        JButton fastestPath;
        JButton refresh;
        JButton loadfile;
        JButton savefile;
        JButton savefile1;
        JButton savefile2;
        String[] speedList = { "1", "2", "4", "8", "16" };
        String[] percentList = {"10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"};
        String[] timeList = {"-", "30s", "60s", "120s", "180s", "240s", "300s", "360s"};
        int speed;
	public Mapsimulator(){
            frame = new JFrame();
            explorationDescriptor1 = new JFrame();
            explorationDescriptor2 = new JFrame();
            frame.setTitle("Algorithm Simulation");
            contentPanel = new MapGrid();
            gridPanelDescriptor1 = new MapGrid(1);
            gridPanelDescriptor2 = new MapGrid(2);
            controlPanel = new JPanel();
            functionalityPanel = new JPanel();
            filePanel = new JPanel();
            speedlist = new JComboBox(speedList);
            percentlist = new JComboBox(percentList);
            timelist = new JComboBox(timeList);
            timePanel = new JPanel();
            timePanel.setLayout(new FlowLayout());
            percentPanel = new JPanel();
            percentPanel.setLayout(new FlowLayout());
            speedPanel = new JPanel();
            speedPanel.setLayout(new FlowLayout());
            speedAndPercent = new JPanel();
            speedAndPercent.setLayout(new GridLayout(1,3));
            speedLabel = new JLabel();
            percentLabel = new JLabel();
            timeLabel = new JLabel();
            descriptor1filePanel = new JPanel();
            descriptor2filePanel = new JPanel(); 
            descriptor1ContrPanel = new JPanel();
            descriptor2ContrPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(3,1));
            descriptor1ContrPanel.setLayout(new GridLayout(1,1));
            descriptor2ContrPanel.setLayout(new GridLayout(1,1));
            exploration = new JButton("Exploration");
            fastestPath = new JButton("FastestPath");
            refresh = new JButton("SetUnexplored");
            loadfile = new JButton("Loadfile");
            savefile = new JButton("Savefile");
//            savefile1 = new JButton("Savefile");
//            savefile2 = new JButton("Savefile");
            exploration.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    gridPanelDescriptor1 = new MapGrid(1);
                    gridPanelDescriptor2 = new MapGrid(2);
                    Container content1 = explorationDescriptor1.getContentPane();
                    Container content2 = explorationDescriptor2.getContentPane();
                    explorationDescriptor1.setTitle("Exploration Descriptor1");
                    explorationDescriptor2.setTitle("Exploration Descriptor2");
                    explorationDescriptor1.setSize(WIDTH, HEIGHT);
		    explorationDescriptor1.setLayout(new BorderLayout());
                    explorationDescriptor2.setSize(WIDTH, HEIGHT);
		    explorationDescriptor2.setLayout(new BorderLayout());
                    explorationDescriptor1.setLocation(frame.getX() + frame.getWidth(), frame.getY());
                    explorationDescriptor2.setLocation(explorationDescriptor1.getX() + explorationDescriptor1.getWidth(), explorationDescriptor1.getY());
                    content1.add(gridPanelDescriptor1, BorderLayout.CENTER);
                    content2.add(gridPanelDescriptor2, BorderLayout.CENTER);
//                    descriptor1filePanel.add(savefile1);
//                    descriptor1ContrPanel.add(descriptor1filePanel);
//                    descriptor2filePanel.add(savefile2);
//                    descriptor2ContrPanel.add(descriptor2filePanel);
//                    content1.add(descriptor1ContrPanel, BorderLayout.SOUTH);
//                    content2.add(descriptor2ContrPanel,BorderLayout.SOUTH);
                    explorationDescriptor1.setVisible(true);
                    explorationDescriptor2.setVisible(true);
                    int speed = Integer.parseInt((String)speedlist.getSelectedItem());
                    String percent = (String)percentlist.getSelectedItem();
                    String time = (String)timelist.getSelectedItem();
                    CZ3004MDPACTUALLY.controller.explore(percent, speed, time);
                }
            });
            loadfile.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    try{
                    contentPanel.loadFile();
                    }
                    catch (IOException ex){
                        System.out.println("File loading failed");
                    }
                }
            });
            savefile.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    try{
                        contentPanel.saveFile1();
                        contentPanel.saveFile2();
                    }
                    catch (IOException ex){
                        System.out.println("File saving failed");
                    }
                }   
            });
//            savefile1.addMouseListener(new MouseAdapter(){
//                @Override
//                public void mousePressed(MouseEvent e){
//                    try{
//                        gridPanelDescriptor1.saveFile1();
//                    }
//                    catch (IOException ex){
//                        System.out.println("File saving failed");
//                    }
//                }   
//            });
//            savefile2.addMouseListener(new MouseAdapter(){
//                @Override
//                public void mousePressed(MouseEvent e){
//                    try{
//                    gridPanelDescriptor2.saveFile2();
//                    }
//                    catch (IOException ex){
//                        System.out.println("File saving failed");
//                    }
//                }   
//            });
            refresh.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    contentPanel.refresh1();
                }
            });
            fastestPath.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    int speed = Integer.parseInt((String)speedlist.getSelectedItem());
                    CZ3004MDPACTUALLY.controller.fastPath(speed);
                }
            });
            init();
        }
	public void init() {
                frame.setSize(WIDTH, HEIGHT);
		frame.setLayout(new BorderLayout());
                Container content = frame.getContentPane();
                content.add(contentPanel, BorderLayout.CENTER);
                content.add(controlPanel, BorderLayout.SOUTH);
                speedLabel.setText("Speed:");
                percentLabel.setText("Percent:");
                timeLabel.setText("Time:");
                speedPanel.add(speedLabel);
                percentPanel.add(percentLabel);
                timePanel.add(timeLabel);
                speedPanel.add(speedlist);
                percentPanel.add(percentlist);
                timePanel.add(timelist);
                functionalityPanel.add(exploration);
                functionalityPanel.add(fastestPath);
                functionalityPanel.add(refresh);
                speedAndPercent.add(speedPanel);
                speedAndPercent.add(percentPanel);
                speedAndPercent.add(timePanel);
                filePanel.add(loadfile);
                filePanel.add(savefile);
                controlPanel.add(functionalityPanel);
                controlPanel.add(filePanel);
                controlPanel.add(speedAndPercent);
                
                
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	} // init

} 


