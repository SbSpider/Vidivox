package framework.media.conversion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.concurrent.Task;

/**
 * Sox command for mixing.
 * 
 * @author sraj144
 *
 */
public class SoxConverterTask extends Task<Void> {

	String command;

	public SoxConverterTask() {
		command = "sox -m ";
	}

	public SoxConverterTask(String commnad) {
		this.command = command;
	}

	public SoxConverterTask(String silencedInputPath, String actualInputPath, int seconds, String outputFilePath) {
		command = "-v0 " + silencedInputPath + " \"| sox " + actualInputPath + " -c 2 -p pad " + seconds + " \" "
				+ outputFilePath;
	}

	@Override
	protected Void call() throws Exception {

		ProcessBuilder procBulder = new ProcessBuilder("/bin/bash", "-c", command);
		procBulder.redirectErrorStream(true);

		try {
			Process process = procBulder.start();

			InputStream inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line = null;

			while ((line = reader.readLine()) != null) {
				System.out.println("SOX: " + line);
			}

		} catch (Exception e) {
			System.out.println("Failed to process sox thread");
			e.printStackTrace();
		}

		return null;
	}

}
