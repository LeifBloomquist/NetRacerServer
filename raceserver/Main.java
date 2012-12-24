/*
 * Main.java
 *
 * Created on February 20, 2008, 3:51 PM
 *
 * Author: LBLOOMQU
 */

package raceserver;

import java.util.Vector;


/**
 * @author LBLOOMQU
 */
public class Main
{ 
    /** Creates a new instance of Main */
    public Main()
    {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        JavaTools.printlnTime("-----------------------------------------------");
        JavaTools.printlnTime("NetRacer Server Version 0.005");
        
        // Vector of all users.
        Vector<RaceCar> allUsers = new Vector();
        
        // TESTING - create two fake cars
        //allUsers.add(new RaceCar((byte)3));
        //allUsers.add(new RaceCar((byte)4));
        
        // Start the thread that updates everything
        UpdaterThread ut = new UpdaterThread(allUsers);
        ut.start();
        
        // Instantiate a UDP listener, and let it take over.
        UDPListener udp = new UDPListener( 3002, allUsers );
    }    
}
