/* 
 * =====================================================================
 *
 * Author: Leif Bloomquist
 * Created on March 10, 2005
 * 
 * =====================================================================
 */

package raceserver;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.channels.FileLock;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.table.TableColumn;



/**
 * @author Leif Bloomquist
 *
 * Miscellaneous static "helper" routines.
 */
public abstract class JavaTools
{
    static FileLock lck = null; //Putting it here keeps it active for the life of the application
    
    public static boolean isEven(int test) 
    {
        if ((test % 2) == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /** Resize columns in an arbitrary table */
    public static void setTableColWidths(JTable table, int[] widths )
    {
        int width = table.getWidth();
        int cols = table.getColumnCount();
        
        if (cols != widths.length)
        {
            System.out.println( "Warning, table has " + cols +" columns, but " + widths.length + " widths specified." );
            return;
        }
		
		// Set Auto resizing
		table.setAutoResizeMode( JTable.AUTO_RESIZE_NEXT_COLUMN );
		
		for (int i=0; i<cols; i++) 
		{
		    TableColumn column = table.getColumnModel().getColumn(i);
		    column.setPreferredWidth( widths[i] );   //Sol
	    }
    }
        
  
    /** Helper function for chooseFile. 
     * Adds an extention to a string (i.e. a filename), only if it isn't there already.
     * */
    public static String smartAppend( String s, String ext )
    {
        if (s.endsWith(ext))
        {
            return s;
        }
        else
        {
            return s + ext;
        }
    }
    
    /** Choose a file to load or save. */
    public static String chooseFile( final String ext, final String Description, Object parent, boolean save, String saveFileDefault, String path )
    {        
        // Create .ext filter
        FileFilter filter = new FileFilter() {
            public boolean accept(File f)  { return f.getName().endsWith(ext); }
            public String getDescription() { return Description; }
        };
        
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        File f = new File( path );
        fc.setCurrentDirectory( f );        
        
        if (save)
        {
            ((BasicFileChooserUI)fc.getUI()).setFileName( saveFileDefault );
            fc.showSaveDialog( (Component)parent );
        }
        else
        {
            fc.showOpenDialog( (Component)parent );
        }
        
        File selFile = fc.getSelectedFile();
        
        if (selFile == null)  //Cancel - TODO - this doesn't work if a filename was clicked!
        {
            return null;
        }
        
        
        return smartAppend( selFile.getName(), ext );  
    }
    
    /** Only allow one instance */
    public static void onlyOneInstance()
    {
        try 
        {            
            lck = new FileOutputStream("raceserver.lock").getChannel().tryLock();
            if(lck == null) 
            {
                JOptionPane.showMessageDialog( null, "A previous instance is already running!" , "Application", JOptionPane.ERROR_MESSAGE );
                System.exit(1);
            }
            return;
        }
        catch (Exception e)
        {
            System.out.println("Can't create/read lock file: " + e.toString() );
        };   
    }


    /** Convert a long into bytes */
    public static byte[] longToBytes(long l, boolean msb)
    {
        byte[] ret = new byte[8];
        int shift;
        for(int ss = 0; ss < ret.length; ss++){
            shift = 8 * (msb ? ret.length - ss - 1 : ss);
            ret[ss] = (byte) ((l >>> shift) & 0xff);
        }
        return ret;
    }
    
    /** Return High Byte of an int. 
    *
    * @param int 
    * @return byte 
    */
   public static byte getHighByte(int x)
   {
    return (byte)  (x >>> 8);
   }
   
   /** Return Low Byte of an int.
    * 
    * @param int 
    * @return byte
    */
   public static byte getLowByte(int x)
   {
    return (byte) (x & 0xFF);
   }
   
   /** Convert a Byte to a 2-digit Hex String.
    * 
    * @param int
    * 
    * @return String 
    */
   public static String byteToString(byte b)
   {
    String tempstring = Integer.toHexString((int) b);
    
    // Pad with 0 if needed
    if (tempstring.length() == 1)
    {
        tempstring = "0" + tempstring;
    }
    
    // For signed bytes, strip off the "FFFFFF..." at the front by getting the last 2 characters.
    if (tempstring.length() > 2)
    {
        tempstring = tempstring.substring(tempstring.length()-2); 
    }

    return tempstring; 
   }
   
// Returns the contents of the file in a byte array.
   public static byte[] getBytesFromFile(String filename) throws IOException 
   {
       File file = new File( filename );       
       InputStream is = new FileInputStream(file);
   
       // Get the size of the file
       long length = file.length();
   
       // You cannot create an array using a long type.
       // It needs to be an int type.
       // Before converting to an int type, check
       // to ensure that file is not larger than Integer.MAX_VALUE.
       if (length > Integer.MAX_VALUE) {
           // File is too large
           return null;
       }
   
       // Create the byte array to hold the data
       byte[] bytes = new byte[(int)length];
   
       // Read in the bytes
       int offset = 0;
       int numRead = 0;
       while (offset < bytes.length
              && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
           offset += numRead;
       }
   
       // Ensure all the bytes have been read in
       if (offset < bytes.length) 
       {
           System.out.println("Could not completely read file " + file.getName());
       }
   
       // Close the input stream and return bytes
       is.close();
       return bytes;
   }
   
   /**
    *  Convert four bytes into a Single Precision 32-bit Float.
    */
   public static float bytesToFloat(byte b1, byte b2, byte b3, byte b4)
   {
     int i = (b1&0xff) | ((b2&0xff) << 8) | ((b3&0xff) << 16) | (b4 << 24);
     return Float.intBitsToFloat(i);
   }
   
   /**
    *  Turn two bytes from a byte array into an integer.  Low byte first.
    *  More robust and readable than doing it inline very time.
    */
   public static int getIntegerAt(byte[] bytearray, int offset)
   {
       if (bytearray == null)
       {
           return -1;  // TODO , error message                   
       }
       
       if (offset >= (bytearray.length-1))
       {
           return -1;  // TODO , error message
       }
               
       int bl=bytearray[offset] & 0xFF;   // This trick makes a byte always positive
       int bh=bytearray[offset+1] & 0xFF;
       
       return bl + (bh*256); 
   }
   
   /**
    *  Turn four bytes from a byte array into an float.  Low byte first.
    *  More robust and readable than doing it inline very time.
    */
   public static float getFloatAt(byte[] bytearray, int offset)
   {
      return bytesToFloat(bytearray[offset+2],
                          bytearray[offset+3],
                          bytearray[offset+0],
                          bytearray[offset+1]);
   }

    public static long getSignedIntegerAt(byte[] message, int i)
    {      
        return (message[i + 1] << 8) | (message[i + 0] & 0x0ff);
    }

    public static long getLongIntegerAt(byte[] message, int i)
    {
        long wh=(long)getIntegerAt(message, i)*65536;
        long wl=(long)getIntegerAt(message, i+2);
        return wl+wh;
    }   
    
    /**
     *  Return a string with the date right now in the format:
     *  Mon Apr 24 13:35:45 EDT 2006
     */         
    public static String Now()
    {
        Date dt = new Date();
        return dt.toString();
    }
    
    /**
     *  Numeric Input
     *
     * @param prompt    String to display in the prompt
     *
     * @return null if user presses Cancel, or a number
     */         
    public static String inputNumericString( String prompt )
    {
        String response = null;         
        boolean success = false;

        while (!success)
        {
            try
            {
                response = JOptionPane.showInputDialog(null, prompt, "Numeric Input", JOptionPane.QUESTION_MESSAGE );          
                if (response == null) return null;  // Cancelled
                
                // Attempt to convert the input to a long.  Not that we'll use it, but as a test of the #.
                long temp = Long.parseLong(response);                
                success = true;
            }
            catch (NumberFormatException error) 
            {
                JOptionPane.showMessageDialog( null, "You must enter a number!", "Error", JOptionPane.ERROR_MESSAGE );
            }
        } 
        
        return response;         
    }
    
  
    /**
     *  Convert a number of seconds to HH:MM:SS string
     *
     * @param timeInSeconds - Total time in seconds.
     * @return String
     */           
   public static String calcHMS(long timeInSeconds) 
   {
      DecimalFormat twodigits = new java.text.DecimalFormat("00"); 
       
      long hours, minutes, seconds;
      hours = timeInSeconds / 3600;
      timeInSeconds = timeInSeconds - (hours * 3600);
      minutes = timeInSeconds / 60;
      timeInSeconds = timeInSeconds - (minutes * 60);
      seconds = timeInSeconds;
      String s = twodigits.format(hours) + ":" + twodigits.format(minutes) + ":" + twodigits.format(seconds);
      return s;
   }
   
   /**
    * Output a string with a timestamp in front.
    */
   public static void printlnTime(String s)
   {
       System.out.println(Now() + " :: " + s);
   }
}
