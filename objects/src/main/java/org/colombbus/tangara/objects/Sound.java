/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;

/**
 * This class provides an object able to create some sounds.
 * @author Benoit
 *
 */
@Localize(value="Sound",localizeParent=true)
public abstract class Sound extends TObject implements LineListener
{
	private static Logger LOG = Logger.getLogger(Sound.class);
	private File soundFile;
	private Clip soundPlayer;
	private AudioInputStream soundInput;
	private ArrayList<DisplaySound> displays;
	private static final String[] DEFAULT_EXTENSIONS = {"wav"}; //$NON-NLS-1$
	private static final String FILE_EXTENSION="wav";
	private static final int BUFFER_SIZE = 65536;
	private static final int MAX_RECORDING_TIME = 120; // in seconds
	private static final int GAIN_MAX_VALUE = 10; // absolute value : gain between -GAIN_MAX_VALUE and GAIN_MAX_VALUE
	private RecordingThread record;
	private boolean playing;
	private boolean recording;
	private boolean pausing;
	private long currentPosition;
	private boolean gainControl;
	private boolean muteControl;
	private boolean pauseByFreeze;
	private int gain; // going from -GAIN_MAX_VALUE to GAIN_MAX_VALUE
	private boolean mute;
	private Object sync;

	/**
	 * Creates an instance of this class.
	 */
    @Localize(value="Sound")
	public Sound()
	{
		soundFile = null;
		soundInput = null;
		soundPlayer = null;
		record = null;
		displays = new ArrayList<DisplaySound>();
		playing = false;
		recording = false;
		gainControl = false;
		muteControl = false;
		pausing = false;
		pauseByFreeze = false;
		currentPosition = 0;
		gain = 0;
		mute = false;
		sync = new Object();
	}

    @Localize(value="Sound")
    public Sound(String fileName)
    {
    	this();
    	loadFile(fileName);
    }

    /**
     * Returns a temporary file.
     * @return
     */
    private File createTempFile()
    {
    	File tmpFile = null;
		try
		{
	    	tmpFile = File.createTempFile("sound","."+FILE_EXTENSION);
			tmpFile.deleteOnExit();
		}
		catch (IOException e)
		{
			Program.instance().writeMessage(getMessage("tempFileError"));
		}
		return tmpFile;
    }

    /**
     * Converts the source file to a WAVE file.
     * @param source
     * @throws Exception
     */
	private File  convertFile(File source) throws Exception
	{
		// Read input stream from source file
		AudioInputStream stream = null;
		stream = AudioSystem.getAudioInputStream(source);

		// Detect audio format
		AudioFormat.Encoding encoding = stream.getFormat().getEncoding();
		if (!(encoding.equals(AudioFormat.Encoding.PCM_SIGNED)||encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED)))
		{
			AudioFormat.Encoding targetEncoding =AudioFormat.Encoding.PCM_SIGNED;
			stream = AudioSystem.getAudioInputStream(targetEncoding, stream);
		}
		File newFile = createTempFile();
		AudioSystem.write(stream, AudioFileFormat.Type.WAVE, newFile);
		return newFile;
	}

	private void setInput(File source) {
		try {
			if ((source!=null)&&(source.length()>0)) {
				soundFile = source;
				soundInput  = AudioSystem.getAudioInputStream(soundFile);
				if (soundPlayer != null && soundPlayer.isOpen()) {
					soundPlayer.close();
				}
				soundPlayer = AudioSystem.getClip();
				soundPlayer.addLineListener(this);
				soundPlayer.open(soundInput);
				setControls();
			}
		}
		catch (LineUnavailableException e)
		{
			Program.instance().writeMessage(getMessage("lineUnavailable"));
		}
		catch (IOException e)
		{
			Program.instance().writeMessage(getMessage("IOError"));
		}
		catch (UnsupportedAudioFileException e) {
			Program.instance().writeMessage(getMessage("load.error"));
			LOG.error("getInputStream: could not read soundFile");
		}
		for (DisplaySound d:displays) {
			d.setSound(this);
		}
	}

	/**
	 * Loads the given sound file.
	 * @param fileName
	 */
    @Localize(value="Sound.loadFile")
	public void loadFile(String fileName)
	{
		if (playing)
			stop();
		if (recording)
			stopRecording();
		if (pausing)
			pausing = false;
		try
		{
    		File sourceFile = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
			if (sourceFile == null)
				throw new Exception(MessageFormat.format(getMessage("load.fileNotFound"), fileName));
			File convertedFile = convertFile(sourceFile);
			setInput(convertedFile);
		}
		catch (UnsupportedAudioFileException e)
		{
			Program.instance().writeMessage(getMessage("unsupportedFormat"));
		}
		catch (Exception e)
		{
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
			Program.instance().writeMessage(message);
		}
	}

    /**
     * Saves the given sound file.
     * @param fileName
     */
    @Localize(value="Sound.saveFile")
    public void saveFile(String fileName)
    {
		if (playing)
			stop();
		if (recording)
			stopRecording();
    	FileChannel in = null;
    	FileChannel out = null;
    	try
    	{
            String actualFileName = new File(fileName).getName();
            int pointPosition = actualFileName.lastIndexOf('.');
	    	if (pointPosition>-1)
	    	{
	    		String extension = actualFileName.substring(pointPosition+1);
	    		if (extension.length()==0)
	    			fileName += FILE_EXTENSION;
	    	}
	    	else
	    	{
				fileName += "." + FILE_EXTENSION;
	    	}
	    	File file = new File(fileName);
	    	if (!file.isAbsolute())
	    	{
	    		// the name does not contain any directory reference : add the user home directory
	    		file = new File(Configuration.instance().getUserHome(),fileName);
	    		fileName = file.getAbsolutePath();
	    	}
	    	if (!file.getParentFile().exists())
	    	{
	    		throw new Exception(MessageFormat.format(getMessage("save.directoryNotFound"),file.getParent()));
	    	}
	    	if (file.exists())
   			{
   				String title = getMessage("save.override.title");
   				String message = MessageFormat.format(getMessage("save.override.message"), fileName);
				Object[] options = {getMessage("tangara.yes"), getMessage("tangara.no")};
				int answer = JOptionPane.showOptionDialog(getGraphicsPane(),
							message,
							title, JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
						    null,     //do not use a custom Icon
						    options,  //the titles of buttons
						    options[0]);
				if (answer != JOptionPane.OK_OPTION)
					return;
   			}

	    	// Copy sound file to destination file
			in = new FileInputStream(soundFile).getChannel();
			out = new FileOutputStream(file).getChannel();
			in.transferTo(0, in.size(), out);
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("save.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
    	}
    	finally
    	{
    		if (in != null)
    		{
    		  	try
    		  	{
    		  		in.close();
    			}
    		  	catch (IOException e)
    		  	{}
    		}
    		if (out != null)
    		{
    		  	try
    		  	{
    		  		out.close();
    			}
    		  	catch (IOException e)
    		  	{}
    		}
    	}
    }

    /**
     * Returns the AudioInputStream of the sound file
     * @return
     */
	public AudioInputStream getInputStream()
	{
		if ((soundFile!=null)&&(soundFile.length()>0))
		{
			try
			{
				return AudioSystem.getAudioInputStream(soundFile);
			}
			catch (Exception e)
			{
				LOG.error("getInputStream: could not read soundFile");
			}
		}
		return null;
	}

	/**
	 * Plays the sound.
	 */
    @Localize(value="Sound.play")
	public void play()
	{
    	long position = 0;
    	synchronized(sync) {
    		if (pausing)
    		{
    			pausing = false;
    			position = currentPosition;
    		}
    	}
   		playFrom(position);
	}

    /**
     * Starts playing starting at the given date in milliseconds.
     * @param millisecs
     */
    @Localize(value="Sound.playFrom")
	public void playFrom(long milisecs)
	{
		if (playing||recording) {
			stop();
		}
		synchronized(sync) {
			if (soundInput!=null)
			{
				soundPlayer.setMicrosecondPosition(milisecs*1000);
				soundPlayer.start();
				playing = true;
				for (DisplaySound d:displays)
				{
					d.start();
				}
	    	}
		}
	}

    /**
     * Updates the status of playing, which indicates whether or not the sound has been stopped.
     */
    @Override
	public void update(LineEvent event)
    {
    	if (event.getType().equals(LineEvent.Type.STOP))
    	{
    		playing = false;
    	}
    }

    /**
     * returns the playing status.
     * @return
     */
	public boolean isPlaying()
	{
		return playing;
	}

	/**
	 * Sets the different controls (gain, mute, ...)
	 */
    private void setControls()
    {
    	if (soundPlayer != null)
    	{
    		if (gainControl)
    		{
    			try
    			{
    				FloatControl gainControl = (FloatControl)soundPlayer.getControl(FloatControl.Type.MASTER_GAIN);
    				if (gain >= 0)
    					gainControl.setValue(gainControl.getMaximum()*gain/GAIN_MAX_VALUE);
    				else
    					gainControl.setValue(-gainControl.getMinimum()*gain/GAIN_MAX_VALUE);
    			}
    			catch (IllegalArgumentException e)
    			{
    				LOG.error("Control of type Master Gain not supported");
    			}
    		}
    		if (muteControl)
    		{
    			try
    			{
    				BooleanControl muteControl = (BooleanControl)soundPlayer.getControl(BooleanControl.Type.MUTE);
    				muteControl.setValue(mute);
    			}
    			catch (IllegalArgumentException e)
    			{
    				LOG.error("Control of type Mute not supported");
    			}
    		}
    	}
    }

    /**
     * Registers a DisplaySound.
     * @param d
     */
	public void registerDisplay(DisplaySound d)
	{
		if (!displays.contains(d))
			displays.add(d);
	}

	/**
	 * Returns the current position.
	 * @return
	 */
	public long getCurrentPosition()
	{
		return soundPlayer.getLongFramePosition();
	}

	/**
	 * Stops the sound.
	 */
    @Localize(value="Sound.stop")
	public void stop()
	{
    	synchronized(sync) {
	    	if (playing)
	    	{
				if (soundPlayer !=null)
				{
					soundPlayer.stop();
				}
				playing = false;
	    	}
	    	if (recording)
	    	{
	    		stopRecording();
	    	}
	    	pausing = false;
    	}
	}

    /**
     * Records the sound.
     */
    @Localize(value="Sound.record")
    public void record()
	{
    	if (recording)
    	{
    		Program.instance().writeMessage(getMessage("alreadyRecording"));
    		return;
    	}
    	if (playing)
    		stop();
    	synchronized (sync) {
			record = new RecordingThread();
			record.start();
			MaxTime max = new MaxTime(record);
			max.start();
			recording = true;
    	}
	}

    /**
     * Stop the sound recording.
     */
	private void stopRecording()
	{
		if (record != null)
		{
			File newFile = record.stopRecording();
			if (newFile != null)
			{
				setInput(newFile);
			}
			record = null;
		}
		recording = false;
	}

	/**
	 * Pauses the sound
	 */
    @Localize(value="Sound.pause")
	public void pause()
	{
		if (recording)
		{
    		Program.instance().writeMessage(getMessage("pauseNotSupported"));
    		return;
		}
		if (pausing)
    	{
    		play();
    		return;
    	}
		synchronized(sync) {
	    	if (playing)
	    	{
				if (soundPlayer !=null)
				{
					soundPlayer.stop();
					currentPosition = soundPlayer.getMicrosecondPosition()/1000; // miliseconds
				}
				playing = false;
				pausing = true;
	    	}
		}
	}

    /**
     * Stop the sound recording.
     */
    @Localize(value="Sound.removeBefore")
	public void removeBefore(long milisecs)
	{
        if (milisecs<0)
        {
            Program.instance().writeMessage(getMessage("error.wrongValue"));
            return;
        }
		if (playing||recording||pausing)
			stop();
		synchronized(sync) {
			File newFile = createTempFile();
			AudioInputStream originalInput = getInputStream();
			AudioFormat format = originalInput.getFormat();
			float frameRate = format.getFrameRate();
			long numberOfFrames = (long)(frameRate*milisecs/1000);
	        if (numberOfFrames>originalInput.getFrameLength())
	            Program.instance().writeMessage(getMessage("error.durationTooLong"));
	        else
	        {
	        	long toSkip = format.getFrameSize()*numberOfFrames;
	        	try
	        	{
	        		originalInput.skip(toSkip);
	        		AudioInputStream newInput = new AudioInputStream(originalInput, format, originalInput.getFrameLength()-numberOfFrames);
	        		AudioSystem.write(newInput, AudioFileFormat.Type.WAVE, newFile);
	        		setInput(newFile);
	        	}
	        	catch (IOException e)
	        	{
	        		Program.instance().writeMessage(getMessage("IOError"));
	        	}
	        }
		}
	}

    /**
     * Cuts the sound and removes the part after a given date in milliseconds.
     * @param milisecs
     */
    @Localize(value="Sound.removeAfter")
    public void removeAfter(long milisecs)
	{
        if (milisecs<0)
        {
            Program.instance().writeMessage(getMessage("error.wrongValue"));
            return;
        }
		if (playing||recording||pausing)
			stop();
		synchronized(sync) {
			File newFile = createTempFile();
			AudioInputStream originalInput = getInputStream();
			AudioFormat format = originalInput.getFormat();
			float frameRate = format.getFrameRate();
			long numberOfFrames = (long)(frameRate*milisecs/1000);
			if (numberOfFrames>originalInput.getFrameLength())
			    Program.instance().writeMessage(getMessage("error.durationTooLong"));
			else
			{
	    		AudioInputStream newInput = new AudioInputStream(originalInput,format,numberOfFrames);
	    		try
	    		{
	    			AudioSystem.write(newInput, AudioFileFormat.Type.WAVE, newFile);
	    			setInput(newFile);
	    		}
	    		catch (IOException e)
	    		{
	    			Program.instance().writeMessage(getMessage("IOError"));
	    		}
			}
		}
	}

	/**
	 *Sets the gain.
	 * @param value
	 */
    @Localize(value="Sound.setGain")
	public void setGain(int value)
	{
		gain = value;
		if (gain <-GAIN_MAX_VALUE)
			gain = -GAIN_MAX_VALUE;
		if (gain>GAIN_MAX_VALUE)
			gain = GAIN_MAX_VALUE;
		gainControl = true;
	}

    /**
     * Sets the mute control.
     * @param value
     */
    @Localize(value="Sound.setMute")
	public void setMute(boolean value)
	{
		muteControl = true;
		mute = value;
	}

    @Override
	public void freeze(boolean value)
    {
    	if (value)
    	{
    		if (playing)
    		{
    			pause();
    			pauseByFreeze = true;
    		}
    		else
    			pauseByFreeze = false;
    	}
    	else
    	{
    		if (pauseByFreeze)
    		{
    			pause();
    			pauseByFreeze = false;
    		}
    	}
    }

    @Override
	public void deleteObject()
    {
		if (playing||recording)
			stop();
		displays.clear();
		displays = null;
    	super.deleteObject();
    }

    /**
     * This thread performs the recording of the sound.
     * @author Benoit
     *
     */
	class RecordingThread extends Thread
	{
		TargetDataLine line;
		AudioFormat format;
		File recordFile;
		Object lock;

		public RecordingThread()
		{
			line = null;
			// PCM 44.1 kHz, 16 bit signed, mono, little-endian
			format = new AudioFormat(44100.0F,16, 1,true,false);
			recordFile = createTempFile();
			lock = new Object();
		}

		@Override
		public void start()
		{
			if (recordFile != null)
			{
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
				if (!AudioSystem.isLineSupported(info))
				{
					Program.instance().writeMessage(getMessage("unsupportedFormat"));
					return;
				}
				try
				{
				    line = (TargetDataLine) AudioSystem.getLine(info);
				    line.open(format,BUFFER_SIZE);
				    line.start();
					super.start();
				}
				catch (LineUnavailableException ex)
				{
					Program.instance().writeMessage(getMessage("lineUnavailable"));
				}
			}
		}

		@Override
		public void run()
		{
		    try
			{
		    	synchronized (lock)
		    	{
		    		AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.WAVE, recordFile);
		    	}
			}
			catch (IOException e)
			{
				Program.instance().writeMessage(getMessage("record.unableToWriteFile"));
			}
		}

		public File stopRecording()
		{
			if (line !=null)
			{
				line.stop();
				line.drain();
				line.close();
			}
			// wait for the writing process to end.
			synchronized (lock)
			{
				return recordFile;
			}
		}
	}

	/**
	 * This thread permits to stop the recording thread after a Maximum time.
	 * @author Benoit
	 *
	 */
	class MaxTime extends Thread
	{
		RecordingThread toWatch;

		public MaxTime(RecordingThread thread)
		{
			toWatch = thread;
		}

		@Override
		public void run()
		{
			try
			{
				sleep(MAX_RECORDING_TIME*1000);
			}
			catch (InterruptedException e)
			{
			}
			if ((toWatch!=null)&&(toWatch.isAlive()))
			{
				stopRecording();
			}
		}
	}



}
