/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz3004mdpactually;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author HuaBa
 */
public class CZ3004MDPACTUALLY {

    /**
     * @param args the command line arguments
     */
    
    //controller is static, to enable the listeners in mapSimulator to work
    public static Controller controller = new Controller();
    
    public static void main(String[] args) {
        // TODO code application logic here
        controller.start();
        /*Connection connect = new Connection();
        //connect.writeData("test");
        while (true){
        try{
        TimeUnit.SECONDS.sleep(5);
        }
        catch (Exception e){}
        System.out.println(connect.readData());
        }*/
                
    }
    
}
