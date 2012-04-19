package logger.serial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.Timer;

public class Replayer implements ActionListener{

	private SerialConnection conx = null;
	private File repFile;
	private JLabel gpsTime;
	private LineNumberReader num_reader;
	private HashMap<Integer,Integer> timeMap;
	private HashMap<Integer,Integer> lineMap;
	public int length = 0; 
	public int elements = 0;
	public String title;
	@SuppressWarnings("unused")
	private JSlider updatePlayCursor;
	private JLabel cur;
	Timer delayer;
	public Replayer(SerialConnection con,File replayFile)
	{
		title = replayFile.getName();
		delayer = new Timer(1000, this);
		repFile = replayFile;
		conx = con;
		timeMap = new HashMap<Integer,Integer>();
		lineMap = new HashMap<Integer,Integer>();
	}
	
	public void setGPSTimeLabel(JLabel l)
	{
		gpsTime = l;
	}
	
	public void setTimeLbl(JLabel c)
	{
		cur = c;
	}
	
	public void setTimeCursor(JSlider spin)
	{
		updatePlayCursor = spin;
	}
	
	public boolean startReplayer() throws IOException
	{
		FileReader readMe = new FileReader(repFile);
		num_reader = new LineNumberReader(readMe);
		getTimeMap(num_reader);
		new replayGUI(this);
		return true;
	}
	
	public void getTimeMap(LineNumberReader num_reader) throws IOException
	{
		int i = 0;
		while(num_reader.ready())
		{
			String line = num_reader.readLine();
			if(line.startsWith("$GPGGA"))
			{
				timeMap.put(i,length);
				lineMap.put(length,i);
				i++;
				elements++;
			}
			length += line.length();
		}
		num_reader.close();
		//System.out.println("File is length: " + length);
	}
	
	String next = "";
	int charCount;
	public void play(int value, double delay) throws IOException {
		System.out.println("playing line: " + value + " w/ Delay: " + delay);
		int seconds = (int) (elements*delay);
		cur.setText("00:00:00 - " + (seconds)/(60*60) + ":" + (seconds)/(60) + ":" + (seconds)%(60));
		end = false;
		FileReader readMe = new FileReader(repFile);
		readMe.skip(timeMap.get(value));
		charCount = timeMap.get(value);
		num_reader = new LineNumberReader(readMe);
		next = readGPSSection();
		delayer.setDelay((int) (delay*1000));
		delayer.start();
	}
	
	public void changeDelay(double delay)
	{
		delayer.setDelay((int) (delay*1000));
	}
	
	boolean end = false;
	public String readGPSSection() throws IOException
	{
		int stop = length +1;
		String temp;
		if(lineMap.get(charCount) != null && timeMap.get(lineMap.get(charCount) + 1) != null)
			{
				int timeOfCommand = lineMap.get(charCount);
				stop = timeMap.get(timeOfCommand + 1);
			}
		else
		{
			end = true;
		}
		temp = "";
		int i = 0;
		while((num_reader.ready())&&(i < stop - charCount))
			{
				String line = num_reader.readLine();
				if(line.startsWith("$GPRMC"))
				{
					String item = line.split(",")[1];
					gpsTime.setText(item);
				}
				temp += line + "\n";
				i += line.length();
			}
		charCount = stop;
		return temp;
	}
	
	public void step() throws IOException
	{
		conx.sendData(next.getBytes());
		if(updatePlayCursor != null)
		{
			updatePlayCursor.setValue(updatePlayCursor.getValue() +1);
		}
		if(end)
		{
			num_reader.close();
			stop();
		}
		else
		{
		next = readGPSSection();
		}
	}

	public void pause() {
		delayer.stop();
		//System.out.println("paused");
		
	}
	
	public void stop()
	{
		FileReader readMe;
		try {
			delayer.stop();
			//System.out.println("Stopped");
			readMe = new FileReader(repFile);
		charCount = 0;
		num_reader = new LineNumberReader(readMe);
		if(this.updatePlayCursor.getValue() == this.updatePlayCursor.getMaximum())
		{
			this.updatePlayCursor.setValue(0);
		}
		playButton.doClick();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	long last_time;
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			//System.out.println((System.currentTimeMillis() - last_time) /1000);
			//last_time = System.currentTimeMillis();
			step();
		} catch (IOException e) {
			System.err.println("Serial Connection lost");
			System.exit(1);
		}
	}
	JButton playButton;
	public void setPlayButton(JButton play) {
		playButton = play;
	}
	
}
