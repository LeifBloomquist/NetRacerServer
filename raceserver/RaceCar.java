/*
 * RaceCar.java
 *
 * Created on February 20, 2008, 3:52 PM - Leif Bloomquist
 *
 * RaceCar object for NetRacer.
 */

package raceserver;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.net.InetAddress;


/**
 *
 * @author LBLOOMQU
 */
public class RaceCar
{
    private byte playerNum;         // Player Number
    private String myName;          // User Name
    private InetAddress myIP;       // User IP Address
    
    private int Xpos;
    private int Ypos;
    private byte XspeedLow;
    private byte XspeedHigh;
    private byte YspeedLow;
    private byte YspeedHigh;    
    private byte carColor;
    private byte spriteNum; 
    private int timeoutCounter;    
    
    public boolean test;  // never times out
    
    // TESTING - create fake instance
    public RaceCar(byte num)
    {
        myName = new String("Test");       
        playerNum = num;
        Xpos = 160;
        Ypos = 50*num;
        XspeedHigh = 0;
        XspeedLow = 0;
        YspeedLow = 0;
        YspeedHigh = 0;
        
        // Reset timeout
        timeoutCounter = 0;
        
        // Fake spriteNum and color
        spriteNum = 35;        
        carColor = 0;
        
        // test car
        test = true;
        
        try
        {
            myIP = InetAddress.getByAddress("127.0.0."+num, new byte[]{127, 0, 0, num});
        } 
        catch (UnknownHostException ex)
        {
            JavaTools.printlnTime(ex.toString()); 
        }
    }
    
    /** Creates a new instance of RaceCar */
    public RaceCar(DatagramPacket packet)
    {
        myName = new String("Player");
        myIP = packet.getAddress();
        receiveUpdate(packet.getData());
                
        test = false;
    }
    
    public void sendUpdate(byte[] message)
    {       
        try
        {            
            // Initialize a datagram packet with data and address
            DatagramPacket packet = new DatagramPacket(message, message.length, myIP, 3000);

            // Create a datagram socket, send the packet through it, close it
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        }
        catch (Exception e)
        {
            JavaTools.printlnTime(e.toString());
        }
    }
       
    /** Return the InetAddress, for comparisons */
    public InetAddress getAddress()
    {
        return myIP;
    } 
    
    /** Return X,Y positions */
    public int getXpos()
    {
        return Xpos;
    }
    
    public int getYpos()
    {
        return Ypos;
    }
    
     /** Return X,Y speeds */
    public byte getXspeedLow()
    {
        return XspeedLow;
    }
    
    public byte getXspeedHigh()
    {
        return XspeedHigh;
    }
    
    public byte getYspeedLow()
    {
        return YspeedLow;
    }
    
    public byte getYspeedHigh()
    {
        return YspeedHigh;
    }
    
    /** Return Color */
    public byte getColor()
    {
        return carColor;
    }
    
    /** Return Sprite# */
    public byte getSprite()
    {
        return spriteNum;
    }
    
    // Increment the timeout - called by UpdaterThread
    public void incTimeout()
    {
       if (timeoutCounter < 10000) timeoutCounter++;
    }
    
    // Check the timeout
    // true if timed out
    public boolean checkTimeout()
    {
       if (test) return false;  // never 
        
       return (timeoutCounter > 20);   // two seconds @ 10 Hz update          
    }
    
    /** Update me with new data from client */
    public void receiveUpdate(byte[] data)
    {
        playerNum  = data[0];  
        Xpos       = (0xFF & data[1]) + (0xFF & data[2])*256;  // 0xFF used to force to signed
        Ypos       = (0xFF & data[3]) + (0xFF & data[4])*256;
        XspeedLow  = data[5];
        XspeedHigh = data[6];
        YspeedLow  = data[7];
        YspeedHigh = data[8];
        carColor   = data[9];
        spriteNum  = data[10];        
        
        // Reset timeout
        timeoutCounter = 0;
        
       // System.out.println("Player " + myIP.toString() + " location: " + Xpos + " " + Ypos);     
    } 
}
