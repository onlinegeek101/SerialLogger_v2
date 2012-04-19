package logger.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;

public class COMPortManager {

	
	private HashMap<String,SerialConnection> ports;
	static Enumeration<CommPortIdentifier> portList;
	private static COMPortManager instance;
	
	public COMPortManager()
	{
		ports = new HashMap<String,SerialConnection>();
	}
	
	public static void main(String [] args) throws PortInUseException, UnsupportedCommOperationException, IOException, InterruptedException
	{
		//GUI.main(new String[]{"-install"});
		System.out.println(COMPortManager.getInstance().listPorts());
	    COMPortManager.getInstance().openConnection("", 19200);
	    try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    SerialConnection temp = COMPortManager.getInstance().ports.get(5);
	    temp.getPort().setRTS(true);
	    byte [] dummy = {(byte) 0x0C,0x1,0x00,0x03,(byte)0xFF};
	    System.out.println("Ready To Send");
	    boolean gotResponse = true;
	    Scanner scan = new Scanner(System.in);
	    while(true)
	   {
		gotResponse = false;
		dummy[0] = hexStringToByte(scan.nextLine());
		dummy[1] = hexStringToByte(scan.nextLine());
		dummy[3] = hexStringToByte(scan.nextLine());
	    byte adr = hexStringToByte(scan.nextLine());
	    dummy[4] = adr;
	    temp.getPort().setRTS(false);
	    Thread.sleep(10);
	    System.out.println("Sending " + Arrays.toString(toUnsigned(dummy)) + "Spin " + (dummy[4]*255)/1000 + " seconds");
	    temp.sendData(dummy);
	    temp.getPort().setRTS(true);
	    while(!gotResponse)
	    {
	    	byte [] data = new byte[5];
	    	if(temp.ready())
	    	{
	    		gotResponse = true;
	    		System.out.println(Arrays.toString(toUnsigned(temp.readData(data))));
	    	}
	    }
	    }
	}
	
	public static byte hexStringToByte(String s)
	{
		String [] removed = s.split("0x");
		String fin;
		if(removed.length > 1)
		{
			fin = removed[1];
		}
		else
		{
			fin = removed[0];
		}
		if(fin.length() > 1)
		{
			return (byte)((byte)((Character.digit(fin.charAt(0),16)) << 4 ) + (byte)(Character.digit(fin.charAt(1), 16)));
		}
		else
		{
			return (byte)(Character.digit(fin.charAt(0), 16));
		}
	}
	
	public static int[] toUnsigned(byte [] data)
	{
		int []  out = new int[data.length];
		for(int i = 0; i < data.length;i++)
		{
			out[i] = (0x000000FF)&((int)data[i]);
		}
		return out;
	}
	
	
	public static COMPortManager getInstance()
	{
		if(instance == null)
		{
			instance = new COMPortManager();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public String listPorts()
	{
		String returnMe = "";
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) { 
        	
        	CommPortIdentifier temp = portList.nextElement();
        	returnMe += temp.getName();
        	returnMe += "\n\tType: ";
        	switch(temp.getPortType())
        	{
        	case 1: returnMe += "SerialPort";break;
        	default: returnMe += "Unknown";
        	}
        	returnMe += "\t\tOwner: "+temp.getCurrentOwner() + "\n";
        	
        }      
        return returnMe;
	}
	
	
	public boolean readByte(int com)
	{
		SerialPort temp = ports.get(com).getPort();
		if(temp != null)
		{
			try {
				System.out.println("Attempting To Read Byte");
				while(temp.getInputStream().available() <= 0)
				{
					//System.out.println(temp.getInputStream().available());
				}
				int dummy = temp.getInputStream().read();
				System.out.println("Recieved " + dummy);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
	public SerialConnection grabConnection(String number) throws IOException
	{
		SerialConnection temp = ports.get(number);
		if(temp == null)
		{
			throw new IOException("Port " + number + " not Open or Doesn't Exist");
		}
		return temp;
	}
	
	public boolean openConnection(String comNumber,int baud)
	{
		SerialConnection temp;
		temp = setupConnection(comNumber,baud);// + comNumber,baud);
		if(temp != null)
		{
			ports.put(comNumber, temp);
			return true;
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	private static SerialConnection setupConnection(String comName,int baud) 	{
		CommPortIdentifier portId;
		SerialPort serialPort = null;
		OutputStream outputStream = null;
		boolean portFound = false;
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(comName)) {
					System.out.println("Found port " + comName);
					portFound = true;
					try {
						serialPort =
							(SerialPort) portId.open("SimpleWrite", 2000);
					} catch (PortInUseException e) {
						System.out.println("Port in use.");
						continue;
					}
					try {
						outputStream = serialPort.getOutputStream();
					} catch (IOException e) {}
					try {
						serialPort.setSerialPortParams(baud,
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
					} catch (UnsupportedCommOperationException e) {}
					try {
						serialPort.notifyOnOutputEmpty(true);
					} catch (Exception e) {
						System.out.println("Error setting event notification");
						System.out.println(e.toString());
						System.exit(-1);
					}
				}
			}
		}
		if (!portFound) {
			System.out.println("port " + comName + " not found.");
		}
		return new SerialConnection(serialPort,outputStream);
	}
}
	
