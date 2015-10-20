package framework.media.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import framework.ScratchDir;
import javafx.concurrent.Task;

public class FFMPEGGenerateWaveform extends Task<File> {

	File inputFile;

	/**
	 * 
	 * @param inputFile
	 *            the file to generaete the graph from.
	 */
	public FFMPEGGenerateWaveform(File inputFile) {
		this.inputFile = inputFile;
	}

	@Override
	protected File call() throws Exception {
		File outputFile = null;

		String inputFilePath = inputFile.getAbsolutePath();

		// Generate the output file name.
		String outputFilePath = ScratchDir.getScratchDir().getAbsolutePath() + "/"
				+ FilenameUtils.removeExtension(FilenameUtils.getBaseName(inputFilePath)) + ".png";

		// The patht to the file specifying how to plot (stored in jar file).
		List<String> gnuPlotLines = IOUtils.readLines(getClass().getResource("/gnuplot/waveform.gnuplot").openStream());

		String gnuPlotPath = ScratchDir.getScratchDir().getAbsolutePath() + "/waveform.gnuplot";

		// Write the gnu file to the scratch dir.
		Files.write(Paths.get(gnuPlotPath), gnuPlotLines, Charset.defaultCharset());

		try {

			String command = "ffmpeg -i " + inputFilePath + " -ac 1 -filter:a aresample=8000 -map "
					+ "0:a -c:a pcm_s16le -f data - | gnuplot " + gnuPlotPath + " > " + outputFilePath;
			ProcessBuilder procBuilder = new ProcessBuilder("/bin/bash", "-c", command);
			procBuilder.redirectErrorStream(true);

			System.out.println("Using command:\n");
			System.out.println(command);

			Process process = procBuilder.start();

			InputStream inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line = null;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

		} catch (Exception e) {
			System.out.println("FFMPEG Waveform generation failed");
			e.printStackTrace();
		}

		System.out.println("Generated file location: " + outputFilePath);
		return new File(outputFilePath);
	}

}
