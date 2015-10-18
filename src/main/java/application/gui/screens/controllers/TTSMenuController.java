package application.gui.screens.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.gui.Window;
import framework.component.PrefFileChooser;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class TTSMenuController implements Initializable {

	/**
	 * The button to initiate the merge.
	 */
	@FXML
	Button MergeButton;

	/**
	 * The text field for the user to enter text in.
	 */
	@FXML
	TextArea UserTextField;

	/**
	 * The name of the tts file.
	 */
	private String ttsFileName = null;

	@FXML
	Button previewButton;

	private FestivalTask task;

	/**
	 * Initializes the screen.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	/**
	 * Handles the merge button. This will primarily display filechooser's and
	 * generate the wav file.
	 *
	 * Note, no limit was placed on the length of the text that can be inserted.
	 * This was because even with 20-40 words, the length of the input was far
	 * shorter than that of the video. The festival code used for merging is
	 * also able to handle differences in video and audio length, padding either
	 * until the same final length is achieved.
	 * 
	 * @param event
	 *            the event to use.
	 */
	@FXML
	public void handleMerge(ActionEvent event) {

		System.out.println("merging");
		if (isEmptyText()) {
			Alert alert = new Alert(AlertType.ERROR, "No text entered into box");
			alert.setWidth(400);
			alert.setHeight(300);
			alert.showAndWait();
			return;
		}

		String textToConvert = UserTextField.getText();

		PrefFileChooser chooser = new PrefFileChooser();

		// Used
		// http://stackoverflow.com/questions/14256588/opening-a-javafx-filechooser-in-the-user-directory
		// Set to user directory or go to default if cannot access
		String userDirectoryString = System.getProperty("user.home");
		File userDirectory = new File(userDirectoryString);
		if (!userDirectory.canRead()) {
			// If defaulting, then make sure handles win and non-win
			// platforms otherwise risk crash
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				userDirectory = new File("c:/");
			} else {
				userDirectory = new File("/");
			}
		}

		chooser.setTitle("Please select location to save text to speech output file to");
		chooser.setExtensionFilters(new ExtensionFilter("Wav - wav", "*.wav"),
				new ExtensionFilter("All Files", "*.*"));

		File saveFile = chooser.showSaveDialog(Window.getPrimaryStage());

		if (saveFile != null) {
			String fileName = saveFile.getAbsolutePath();

			if (!fileName.endsWith(".wav")) {
				fileName = fileName + ".wav";
			}

			String cmd = "echo \"" + textToConvert + "\"" + " | text2wave -o " + fileName + " -F 48000"; // Creates

			ProcessBuilder fileMaker = new ProcessBuilder("/bin/bash", "-c", cmd);
			try {
				Process process = fileMaker.start();
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				System.out.println("Error with voice synthesis output creation.");
				e.printStackTrace();
			}

			// Not converting to mp3 to avoid data loss due to compression.

			Node source = (Node) event.getSource();
			Stage stage = (Stage) source.getScene().getWindow();
			stage.close();

			ttsFileName = fileName;
		}
	}

	/**
	 * Returns the name of the tts file.
	 * 
	 * @return the name of the tts file.
	 */
	public String getTTSFilename() {
		return ttsFileName;
	}

	@FXML
	public void handlePreviewButton(ActionEvent event) {

		if (previewButton.getText().equals("Preview")) {

			if (isEmptyText()) {
				Alert alert = new Alert(AlertType.ERROR, "No text entered into box");
				alert.setWidth(400);
				alert.setHeight(300);
				alert.showAndWait();
				return;
			}

			String message = UserTextField.getText();

			task = new FestivalTask(message);

			Thread thread = new Thread(task);
			thread.setDaemon(false);
			thread.start();

			previewButton.setText("Stop");

			task.setOnSucceeded(e -> previewButton.setText("Preview"));

		} else if (previewButton.getText().equals("Stop")) {

			task.cancel();
			previewButton.setText("Preview");
		}

	}

	private boolean isEmptyText() {
		String text = UserTextField.getText();

		if (text == null) {
			return true;
		}

		text = text.trim();

		if (text.equals("")) {
			return true;
		}

		return false;
	}

	class FestivalTask extends Task<Void> {

		String message;
		private Process process;
		private int pid;

		public FestivalTask(String message) {
			this.message = message;
		}

		@Override
		protected Void call() throws Exception {

			ProcessBuilder procBuilder = new ProcessBuilder("festival", "--tts");
			procBuilder.redirectErrorStream(true);
			process = procBuilder.start();

			// Reflection hack to get pid.
			if (process.getClass().getName().equals("java.lang.UNIXProcess")) {

				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true); // pid is private in UNIXProcess
				pid = f.getInt(process);
			}

			OutputStream outputStream = process.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream);

			writer.write(message + "\n");
			writer.flush();
			writer.close();

			while (process.isAlive() && !isCancelled()) {

			}

			return null;
		}

		@Override
		protected void cancelled() {
			if (process != null && process.isAlive()) {
				// If it is alive, then we kill the process using pid

				// Create killing process
				ProcessBuilder pb = new ProcessBuilder("pstree", "-p", Integer.toString(pid));
				pb.redirectErrorStream(true);

				String psLine = null;
				try {
					// Starts pstree process and waits for it to finish, then
					// grabs
					// the line to processes.
					Process start = pb.start();
					start.waitFor();
					psLine = new BufferedReader(new InputStreamReader(start.getInputStream())).readLine();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// http://stackoverflow.com/questions/2367381/how-to-extract-numbers-from-a-string-and-get-an-array-of-ints
				Pattern p = Pattern.compile("-?\\d+");
				// Matcher m =
				// p.matcher(psLine.substring(psLine.indexOf("aplay")));
				Matcher m = p.matcher(psLine);

				String[] pids = new String[6];

				// Grabs all of the integers found in the process - currently
				// found
				// to be capped at 6.
				int i = 0;
				while (m.find()) {
					pids[i] = m.group();
					// System.out.println(pids[i]);
					i++;
				}

				// System.out.println(psLine);

				try {
					// In reverse order kill the processes.
					for (i = i - 1; i >= 0; i--) {
						new ProcessBuilder("kill", "-9", pids[i]).start().waitFor();
					}
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// Ask process to finish nicely.
			process.destroy();

			super.cancelled();
		}

	}

}
