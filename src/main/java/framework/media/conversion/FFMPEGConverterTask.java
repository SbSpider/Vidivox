package framework.media.conversion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.concurrent.Task;
import javafx.util.Duration;

public class FFMPEGConverterTask extends Task<Void> {

	/**
	 * Command to perform via ffmpeg
	 */
	String command;
	private Duration duration;

	/**
	 * Default constructor
	 */
	public FFMPEGConverterTask() {
		// Defualt options.
		command = "ffmpeg -y -strict -2 -preset ultrafast -codec aac ";
	}

	/**
	 * Allows definition for custom command.
	 * 
	 * @param command
	 */
	public FFMPEGConverterTask(String command) {
		this.command = command;
	}

	/**
	 * Converts between input and output file, primarily used for type
	 * converion.
	 * 
	 * @param inputFileName
	 * @param outputFilename
	 */
	public FFMPEGConverterTask(String inputFileName, String outputFilename) {
		this();
		command += "-i " + inputFileName + " " + outputFilename;
	}

	/**
	 * Used to merge an audio and a video with sidechain compression.
	 * 
	 * @param inputAudioFilePath
	 * @param inputVideoFilePath
	 * @param outputFilePath
	 */
	public FFMPEGConverterTask(String inputAudioFilePath, String inputVideoFilePath, String outputFilePath) {
		this();
		command += "-i " + inputAudioFilePath + " -i " + inputVideoFilePath
				+ " -filter_complex \"[1:a]asplit=2[sc][mix];[0:a][sc]sidechaincompress[compr];[compr][mix]amerge\" "
				+ outputFilePath;
	}

	/**
	 * Sets the duration of the longest thing.
	 * 
	 * @param duration
	 */
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	@Override
	protected Void call() throws Exception {

		ProcessBuilder procBuilder = new ProcessBuilder("/bin/bash", "-c", command);
		procBuilder.redirectErrorStream(true);

		System.out.println("Command running:\n");
		System.out.println(command);

		try {
			Process process = procBuilder.start();

			InputStream inputStream = process.getInputStream();
			// OutputStream outputStream = process.getOutputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line = null;

			while ((line = reader.readLine()) != null) {
				System.out.println("Progress: " + line);

				// Output is of type:Process: frame= 190 fps=189 q=22.0 size=
				// 1585kB time=00:00:07.62 bitrate=1702.8kbits/s

				// Regex matching pattern
				Pattern timeFindingPattern = Pattern.compile("(?<=time=)[\\d:.]*");

				Matcher matcher = timeFindingPattern.matcher(line);

				if (!matcher.find()) {
					continue;
				}

				String time = matcher.group();

				String[] split = time.split(":");
				int hours = Integer.parseInt(split[0]);
				int minute = Integer.parseInt(split[1]);
				double seconds = Double.parseDouble(split[2]);

				int milliseconds = (int) (seconds * 1000);
				milliseconds += minute * 60 * 1000;
				milliseconds += hours * 60 * 1000;

				updateProgress(milliseconds, duration.toMillis());
			}

		} catch (Exception e) {
			System.out.println("Process failed!");
			e.printStackTrace();
		}

		return null;
	}

}
