/*
 * UDPListener.java
 *
 * Created on July 17, 2007, 3:24 PM
 *
 * Listens for UDP packets from clients and updates the internal model of
 * the playing field.
 */

package raceserver;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Vector;


/**
 * @author LBLOOMQU
 */
public class UDPListener
{
    private Vector<RaceCar> allUsers = null;
    
    /** Creates a new instance of UDPListener */
    public UDPListener(int port, Vector allUsers)
    {
        this.allUsers = allUsers;  // Keep a reference to the users list

        try
        {
            DatagramSocket socket = new DatagramSocket(port);                
                
            JavaTools.printlnTime( "Waiting for packets on port " + port );
            
            /* Loop Forever, waiting for packets. */      
            while (true) 
            {             
                DatagramPacket packet = new DatagramPacket(new byte[20],20);   // Yes, create a new packet each time (old one garbage collected)
                socket.receive(packet);  // This blocks!                
                
                // Handle it
                handlePacket(packet);              
            }
        }
        catch (SocketException ex)
        {
            JavaTools.printlnTime( "Socket Exception: " + ex.toString());
        }
        catch (IOException ex)
        {
            JavaTools.printlnTime( "IO Exception: " + ex.toString());
        }   
    }
    
    /**
     *  Handle a received packet.
     */    
    private void handlePacket(DatagramPacket packet)
    {
        byte[] packetBytes = packet.getData();        
        
        // Check Checksum - Future
        
        // Determine user
        RaceCar who = null; // meaning not identified yet
        
        synchronized (allUsers)
        {
            for (RaceCar aCar : allUsers)
            {
                if ( aCar.getAddress().equals( packet.getAddress()) )  //match found
                {
                    who = aCar;
                    break;
                }
            }
        }
        
        // Did we get a match?        
        if (who == null)
        {
            // No match, create new user and add to vector
            JavaTools.printlnTime( "Creating player from " + packet.getAddress() );
            who = new RaceCar(packet);
            
            synchronized (allUsers) 
            {
                allUsers.add(who);
            }
            return;  
        }        
        else
        {
            // Update existing user
            who.receiveUpdate(packetBytes);
        }
    }
}