/*
 * UpdaterThread.java
 *
 * Updates game state for all players.
 */

package raceserver;

import java.util.Vector;


/**
 * @author LBLOOMQU
 */
public class UpdaterThread extends Thread 
{   
    private final int CAR_Y_POSITION = 150;         // Y Location on screen
    private final int CAR_Y_BOTTOM   = 50;          // Space between car and pottom of screen 
                                                    // (these should sum to 200)
    private final int TRACK_LENGTH_PIXELS = 1591;   // Length (Y) of track in pixels
    
    private Vector<RaceCar> allUsers = null;
     
    /** Creates a new instance of UpdaterThread */
    public UpdaterThread(Vector allUsers)
    {
        // Save a reference
        this.allUsers = allUsers;
    }                   
    
    /** Main updating thread. */
    public void run()                       
    {   
        // Loop forever
        while (true)
        {   
            synchronized (allUsers) 
            {
                // Update each player
                for (RaceCar who : allUsers)
                {
                    byte[] message = new byte[101];
                    int offset = 1;   // Reserve first byte for packet type (future)

                    if (who.test) continue;  // Don't bother updating test cars

                    // Look at all other users
                    for (RaceCar other : allUsers)
                    {
                        if (who == other) continue;     // Don't update with myself

                        // Determine difference between my car and other cars
                        int ydiff = other.getYpos() - who.getYpos();

                        // Handle wraparound (other car in front)
                        if (ydiff < (CAR_Y_POSITION - TRACK_LENGTH_PIXELS)) ydiff += TRACK_LENGTH_PIXELS;

                        // Handle wraparound (current car in front)
                        if (ydiff > (TRACK_LENGTH_PIXELS - CAR_Y_BOTTOM))   ydiff -= TRACK_LENGTH_PIXELS;

                        // Is this other player visible?
                        if ((ydiff < CAR_Y_POSITION)   &&   // On screen, ahead
                            (ydiff > -CAR_Y_BOTTOM))        // On screen, behind
                        {
                            message[offset+0] = JavaTools.getLowByte(other.getXpos());   
                            message[offset+1] = JavaTools.getHighByte(other.getXpos());          
                            message[offset+2] = (byte)(CAR_Y_POSITION-JavaTools.getLowByte(ydiff)); // Low
                            message[offset+3] = 0;  // High byte of Y diff, if needed someday
                            message[offset+4] = other.getXspeedLow();
                            message[offset+5] = other.getXspeedHigh();
                            message[offset+6] = other.getYspeedLow();
                            message[offset+7] = other.getYspeedHigh();
                            message[offset+8] = other.getColor();  
                            message[offset+9] = other.getSprite();
                            message[offset+10]= 0;  // Future, i.e. Actions
                            message[offset+11]= 0;  // Future, i.e. Actions

                            offset += 12;
                        } // if
                    } // others

                    who.sendUpdate(message);
                } // foreach            

                // Check for lost players - remove if timed out
                for (RaceCar who : allUsers)
                {
                    who.incTimeout();                
                    if (who.checkTimeout()) 
                    {
                        JavaTools.printlnTime("Removing player from " + who.getAddress().toString() );
                        allUsers.removeElement(who);
                        break; // To avoid exception 
                    }
                }            
            } // synchronized
            
            try
            {
                Thread.sleep(10);   //was 100, then 50
            }
            catch (InterruptedException ex)
            {
                JavaTools.printlnTime( "Thread Interrupted: " + ex.toString());
            }               
        } //while 
    }
}