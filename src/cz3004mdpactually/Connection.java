/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
/**
 *
 * @author Jo
 */
public class Connection {
    
    Socket client;
    BufferedReader input;
    //BufferedWriter output;
    OutputStream output;
    static String receiveMsg;

    public Connection(){
        try{
            client = new Socket("192.168.9.1", 1111); //IP, and port number
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //output = new BufferedWriter (new OutputStreamWriter(client.getOutputStream()));
            output = client.getOutputStream();
        }
        catch (UnknownHostException e){
            System.err.println("Don't know host");
        }
        catch (IOException e){
            System.err.println("Couldn't get I/O for the connection");
        }
    }
    
    //format of data to write is x0984298329834, x is a= arduino, b= android
    public void writeData(String data){
        if (client != null && output != null){
            try{
                //output.write(data);
                output.write(data.getBytes(Charset.forName("utf-8")));
                output.flush();
                System.out.println(data);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    
    public String readData(){
        String data = null;
        System.out.println("Inside");
        if (client != null && input != null){
            try{
                data = input.readLine();      
            }                          
            catch (IOException e){
                System.out.println("Exception!");
            }
        }
        System.out.println(data);
        return data;
    }
    
    public void close(){
        try {
            output.close();
            input.close();
            client.close();
            System.out.println("Close the port");
        } 
        catch (IOException e){
            System.out.println(e);
        }
    }
    
    public int messageRecognition(){
        String message = readData();
        String tmp = "Invalid Input"; //just a placeholder
        
        if (message.equals("Pause")){
            while (true){
                message = readData();
                if (message.equals("Resume"))
                    break;
                else if(message.equals("Stop")){
                    close();
                    System.exit(0);
                }
                else{
                    if (!message.equals(""))  //if the message isn't empty
                        tmp = message;  //this means a message arrived after the pause command, store it
                }
            }
            message = tmp;
        }     
        if(message.equals("Stop")){
            close();
            System.exit(0);
        }   
        
        switch(message){
            case "Move Forward finished":
                return 1;                                 
            case "Turn right finished":
                return 2;
            case "Turn left finished":
                return 3;
            case "Stop finished":
                return 4;
            case "Invalid Input":
                return 5;
//            case "String Received":
//                return 6;
            case "Explore Function":
                return 10;
            case "Fastest Path":
                return 11;
            default:
                if(message.matches("Invalid input*"))
                    return -1;
                if(message.matches("SSZ*")){  //Set Start Zone
                    receiveMsg = message;
                    return 7;
                }
                if(message.matches("SGZ*")){  //Set Goal Zone
                    receiveMsg = message;
                    return 8;
                }
                if(message.matches("SRL*")){  //Set Robot Location
                    receiveMsg = message;
                    return 9;
                }
                if(message.matches("Close the port*")){
                    close();
                    return -2;
                }
                    
                else {//this means sensor input is coming
                        receiveMsg = message;
                        return 12;
                }              
        }
    }
    
    //used to get start/end/robot location from Android
    public int[] zoneParse(){
        String message = receiveMsg;
        String[] coordinates = {};
        int[] coordinatesInt = new int[2];
        
        message = message.substring(3);     //get the coordinates after the 3 char header  
        coordinates = message.split(" ");
        coordinatesInt[0] = Integer.parseInt(coordinates[0]);
        coordinatesInt[1] = Integer.parseInt(coordinates[1]);
        
        return coordinatesInt;
    }
    
    public int[] sensorDataParse(){
        System.out.println("message");
        String message = receiveMsg;
        String[] sensorData = {};
        int[] sensorDataInt = new int[5];
        System.out.println(message);
        sensorData = message.split(" ");
        
        for (int i = 0; i < 5; i++){
            sensorDataInt[i] = Integer.parseInt(sensorData[i]);
            System.out.println(sensorDataInt[i]);
        }
        
        return sensorDataInt;
    }
}
