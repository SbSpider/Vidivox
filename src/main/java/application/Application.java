package application;

import application.gui.Window;

/**
 * Begins the application.
 * 
 * @author sraj144
 *
 */
public class Application {
	public static void main(String[] args) {
		Window window = new Window();
		window.begin(args);
	}
}
