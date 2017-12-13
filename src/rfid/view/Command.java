package rfid.view;

public class Command 
{
	
	private static final byte STX = 0x2;
	private static final byte ETX = 0x3;

	/**
	 * Calculate CRC for dscemtec SHL-2001
	 * @param bArr payload
	 * @return crc for scemtec SHL-2001
	 */
	public static byte calcScemtecCRC(byte[] bArr)
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
		byte bArr[] = new byte[cmd.length + 2]; 		// STX, cmd, ETX
		bArr[0] = STX; 									// start with STX
		
		for (int i = 0; i < cmd.length; i++ ) 
		{
			bArr[i+1] = cmd[i]; 						// fill after STX
		}
	
		bArr[cmd.length + 1] = ETX; 					// end with ETX
		byte crc = calcScemtecCRC( bArr ); 				// get CRC
		// new array with CRC
		byte bArr2[] = new byte[bArr.length + 1]; 		// STX, cmd, ETX, CRC

		for (int i = 0; i < bArr.length; i++ ) 
		{
			bArr2[i] = bArr[i]; // copy
		}
	
		bArr2[bArr.length] = crc;
		
		return bArr2;
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
}
