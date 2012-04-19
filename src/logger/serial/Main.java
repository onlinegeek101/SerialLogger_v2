package logger.serial;

import java.io.File;
import java.io.IOException;

public class Main {

	private static String usage = "Usage: java -jar SerialLogger.jar com_port FileName";
	private static String port = "";
	public static void main(String [] args)
	{
		if(args.length < 2)
		{
			System.out.println(usage);
			System.exit(1);
		}
		System.out.println("Connecting to COMPort: " + args[0]);
		if(!COMPortManager.getInstance().openConnection(args[0], 4800))
		{
			System.out.println("Unable to open COMPort: " + args[0]);
			System.exit(1);
		}
		port = args[0];
		File replayFile = new File(args[1]);
		if(!replayFile.exists() || replayFile.isDirectory() || !replayFile.isFile() || !replayFile.canRead())
		{
			System.out.println("Cannot Open File: " + args[1]);
		}
		try {
			Replayer replayIt = new Replayer(COMPortManager.getInstance().grabConnection(port),replayFile);
			replayIt.startReplayer();
		} catch (IOException e) {
			System.out.println("COMConnection Lost");
		}
		
	}
	
}
