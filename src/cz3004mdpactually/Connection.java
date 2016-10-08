/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.net.*;
import java.io.*;
/**
 *
 * @author Jo
 */
public class Connection {
    Socket client;
    BufferedReader input;
    DataOutputStream output;
    
    public Connection(){
        try{
            client = new Socket("192.168.9.1", 1111); //IP, and port number
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new DataOutputStream(client.getOutputStream());
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
                output.writeUTF(data);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    
    public String readData(){
        String data = null;
        
        if (client != null && input != null){
            try{
                data = input.readLine();  //read each individual line then break?
            }                           //otherwise might be reading forever. TODO
            catch (IOException e){
                e.printStackTrace();
            }
        }
        return data;
    }
    
    public void close(){
        try {
            output.close();
            input.close();
            client.close();
        } 
        catch (IOException e){
            System.out.println(e);
        }
    }
    public int messageRecognition(){
        String message = readData();
        
        switch(message){
            case "Move Forward finished":
                return 1;
                //break;
            case "Turn right finished":
                return 2;
                //break;
            case "Turn left finished":
                return 3;
                //break;
            case "Stop finished":
                return 4;
                //break;
            case "Invalid input":
                return 5;
                //break;
            default:
                if(message.matches("Invalid input*")){
                    return 5;
                    //break;
                }
                else //this means scanner input is coming
                    return 6;
                //break;    
        }
    }
    
    public int[] sensorDataParse(){
        String message = readData();
        String[] sensorData = {};
        int[] sensorDataInt = {};
        
        sensorData = message.split(" ");
        for (int i = 0; i < 5; i++){
            sensorDataInt[i] = Integer.parseInt(sensorData[i]);
        }
        
        return sensorDataInt;
    }
}
