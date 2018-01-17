package rfid.view;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Command 
{
	
	private static final byte STX = 0x2;
	private static final byte ETX = 0x3;

	/**
	 * Calculate CRC for dscemtec SHL-2001
	 * @param bArr payload
	 * @return crc for scemtec SHL-2001
	 */
	private static byte calcScemtecCRC(byte[] bArr)
	{
		byte crc = 0x0;
		
		for(int i = 0; i < bArr.length; i++)
		{
			crc^=bArr[i];  				//XOR
		}
		
		return crc;
	}
	
	/**
	* Calculate full command with STX, ETX and CRC from a command for scemtec SHL-2001
	* @author Ralf S. Mayer, june 2015
	* @param cmd payload only (w.o. STX and ETX)
	* @return full command string for scemtec SHL-2001 including STX, cmd, ETX, CRC
	*/
	public static byte[] calcScemtecFullCmd( byte[] cmd )
	{
		byte bArr[] = new byte[cmd.length + 3]; 		// STX, cmd, ETX
		bArr[0] = STX; 									// start with STX
		
		for (int i = 0; i < cmd.length; i++ ) 
		{
			bArr[i+1] = cmd[i]; 						// fill after STX
		}
	
		bArr[cmd.length + 1] = ETX; 					// end with ETX
		byte crc = calcScemtecCRC( bArr ); 				// get CRC
		// new array with CRC

		bArr[bArr.length-1] = crc;
		
		return bArr;
	}
	
	
	/**
	* Output command array as decimals
	* @author Ralf S. Mayer, june 2015
	* @param cmd
	* @return
	*/
	public static String cmdToDecString( byte[] cmd )
	{
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < cmd.length - 1; i++ ) 
		{
			buf.append(String.format( "%03d", cmd[i] ) + ", " );
		}
		
		buf.append( String.format( "%03d", cmd[cmd.length - 1] ) );
		return buf.toString();
	}
	
	/**
	* 
	* @param cmd
	* @return
	*/
	public static String cmdToHexString( byte[] cmd )
	{
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < cmd.length - 1; i++ ) 
		{
			buf.append(String.format( "%02X", cmd[i] ) + ", " );
		}
		
		buf.append( String.format( "%02X", cmd[cmd.length - 1] ) );
		return buf.toString();
	}
	
	public static String hexStringToString(byte[] cmd ) 
	{
		String hex = cmdToHexString(cmd);
		
	    int l = hex.length();
	    byte[] data = new byte[l/2];
	    
	    for (int i = 0; i < l; i += 2) 
	    {
	        data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	                             + Character.digit(hex.charAt(i+1), 16));
	    }


	    String st = new String(data, StandardCharsets.UTF_8);
	    
	    return st;
	}
	
	public static void writeToBib(String append)
	{
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		
		try 
		{
		    fw = new FileWriter("bib.txt", true);
		    bw = new BufferedWriter(fw);
		    out = new PrintWriter(bw);
		    out.println(append);
		    out.close();
		} 
		catch (IOException e) 
		{
		    //exception handling left as an exercise for the reader
		}
		finally 
		{
		    if(out != null)
			    out.close();
		
		    try 
		    {
		        if(bw != null)
		            bw.close();
		    } catch (IOException e) {
		        //exception handling left as an exercise for the reader
		    }
		    
		    try 
		    {
		        if(fw != null)
		            fw.close();
		    } catch (IOException e) {
		        //exception handling left as an exercise for the reader
		    }
		}
	}
	
}
