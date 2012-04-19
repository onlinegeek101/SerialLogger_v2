package logger.serial;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class replayGUI extends JFrame {

	Replayer player;
	private final JButton play;
	private final JSpinner delay;
	private final JSlider time;
	private Boolean isPaused = true;
	private final ImageIcon playIcon;
	private final ImageIcon pause;

	public static void main(String[] args) {
		new replayGUI(new Replayer(null, null));
	}

	GridLayout layout;
	JLabel currentTime;
	public replayGUI(Replayer rep) {
		this.setTitle("Serial GPS Replayer: " + rep.title);
		player = rep;
		layout = new GridLayout(2, 1);
		this.setLayout(layout);
		delay = new JSpinner();
		delay.setModel(new SpinnerNumberModel(1, 0, 5, .1));
		delay.setMaximumSize(new Dimension(50, delay.getSize().height));
		playIcon = new ImageIcon(this.getClass().getResource(
				"/res/images/play.png"));
		pause = new ImageIcon(this.getClass().getResource(
				"/res/images/pause.png"));
		play = new JButton(playIcon);
		play.setFocusPainted(false);
		play.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JButton fireer = (JButton) arg0.getSource();
				if (isPaused) {
					fireer.setIcon(pause);
					try {
						if(time.getValue() == time.getMaximum())
						{
							time.setValue(0);
						}
						player.play(time.getValue(), ((Double) delay.getModel()
								.getValue()).doubleValue());
					} catch (IOException e) {
						System.err
								.println("ReplayGUI SOMETHING BROKE WHILE STARTING TO PLAY");
						System.exit(1);
					}
					delay.setEnabled(false);
					time.setEnabled(true);

				} else {
					fireer.setIcon(playIcon);
					player.pause();
					delay.setEnabled(true);
					time.setEnabled(true);
				}
				isPaused = !isPaused;
			}

		});
		time = new JSlider();
		JLabel gpsTime = new JLabel("N/A");
		player.setTimeCursor(time);
		player.setPlayButton(play);
		time.setMaximum(rep.elements);
		time.setMinimum(0);
		time.setValue(0);
		time.setMaximumSize(new Dimension(480, time.getSize().height));
		int seconds = (int) (player.elements*((Double)(delay.getModel().getValue())).doubleValue());
		currentTime = new JLabel("0:0:0 - " + seconds/(60*60) + ":" + seconds/60 + ":" + seconds%60);
		time.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {
				int seconds = (int) (player.elements*((Double)(delay.getModel().getValue())).doubleValue());
				double curseconds = time.getValue()/((double)time.getMaximum());
				int currentseconds = (int) (seconds*curseconds);
				currentTime.setText( currentseconds/(60*60) + ":" + currentseconds/(60) + ":" + currentseconds%60 + " - " + seconds/(60*60) + ":" + seconds/60 + ":" + seconds%60);
			}
			
		});
		player.setTimeLbl(currentTime);
		GroupLayout lay2 = new GroupLayout(this.getContentPane());
		lay2.setAutoCreateGaps(true);
		lay2.setAutoCreateContainerGaps(true);
		lay2.setHorizontalGroup(lay2
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(
						lay2.createSequentialGroup().addComponent(play)
								.addComponent(delay)
								.addComponent(currentTime)
								.addComponent(gpsTime)
								).addComponent(time));
		lay2.setVerticalGroup(lay2
				.createSequentialGroup()
				.addGroup(
						lay2.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(play).addComponent(delay).addComponent(currentTime).addComponent(gpsTime))
				.addComponent(time));
		player.setGPSTimeLabel(gpsTime);
		this.setLayout(lay2);
		this.add(play);
		this.add(delay);
		this.add(currentTime);
		this.add(gpsTime);
		this.add(time);
		this.setSize(500, 105);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
