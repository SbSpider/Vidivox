package application.gui.screens.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import application.gui.screens.components.VideoPlayer;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;

/**
 * THe controller for the main screen.
 * 
 * @author sraj144
 */
public class MainScreenController implements Initializable {

	@FXML
	MenuBar menuBar;
	@FXML
	BorderPane mainScreen_Root;

	/**
	 * Initializes the screen.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		VideoPlayer player = new VideoPlayer();

		mainScreen_Root.setCenter(player);
		player.setVisible(true);
	}

}
