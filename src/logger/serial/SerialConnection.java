package logger.serial;
import java.io.*;
import gnu.io.*;

public class SerialConnection implements Runnable{
	private SerialPort serialPort;
	private OutputStream outputStream;
	static boolean      outputBufferEmptyFlag = false;
	boolean keepRunning = false;

	public SerialConnection(SerialPort port, OutputStream out)
	{
		serialPort = port;
		outputStream = out;
	}
	
	public SerialPort getPort()
	{
		return serialPort;
	}
	
	 public void stopThread()
     {
             keepRunning = false;
     }
	
	public boolean start()
	{
		return false;
	}
	
	public synchronized void sendData(byte  [] data) throws IOException {
			//System.out.println("Writing Data"); DEBUG - TJP
			outputStream.write(data);
		try {
			Thread.sleep(2);  // Be sure data is xferred before
		} catch (Exception e) {}
			outputStream.flush();
	}
	
	public synchronized byte[] readData(byte [] data) throws IOException
	{
		serialPort.getInputStream().read(data);
		return data;
	}
	
	public boolean ready()
	{
		try {
			if(serialPort.getInputStream().available() > 4)
			{
				return true;
			}
			else
			{
				return false;
			}
		} catch (IOException e) {
			stopThread();
			return false;
		}
	}

	public void run() {
				
	}

}
