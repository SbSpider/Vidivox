package framework.component;

import java.io.File;
import java.util.prefs.Preferences;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * File chooser that can use the last location in which a file was saved as the
 * initial directory.
 * 
 * @author sraj144
 *
 */
public class PrefFileChooser {

	private static final String LAST_SAVE_LOCATION = "LastSaveLocation";
	/**
	 * Internal chooser that is used.
	 */
	FileChooser chooser;

	public PrefFileChooser() {
		chooser = new FileChooser();

		Preferences pref = Preferences.userRoot().node("Vidivox");
		String path = pref.get(LAST_SAVE_LOCATION, System.getProperty("user.home"));

		chooser.setInitialDirectory(new File(path));
	}

	/**
	 * Allows setting the extension filters, clearing them first.
	 * 
	 * @param filters
	 */
	public void setExtensionFilters(ExtensionFilter... filters) {
		chooser.getExtensionFilters().clear();
		chooser.getExtensionFilters().addAll(filters);
	}

	/**
	 * Wrapper method for showSaveDialog.
	 * 
	 * @return
	 */
	public File showSaveDialog(Window window) {
		File file = chooser.showSaveDialog(window);

		// Save the directory to the preferences.
		saveToPrefAPI(file);

		return file;
	}

	/**
	 * Wrapper method for showOpenDialog
	 * 
	 * @param window
	 * @return
	 */
	public File showOpenDialog(Window window) {
		File file = chooser.showOpenDialog(window);

		saveToPrefAPI(file);

		return file;
	}

	/**
	 * Wrapper method for setTitle.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		chooser.setTitle(title);
	}

	/**
	 * Sets the initial filename.
	 * 
	 * @param filename
	 */
	public void setInitialFileName(String filename) {
		chooser.setInitialFileName(filename);
	}

	/**
	 * Saves the location to preference api.
	 */
	private void saveToPrefAPI(File file) {

		Preferences pref = Preferences.userRoot().node("Vidivox");

		String path = null;

		if (file.isDirectory()) {
			path = file.getAbsolutePath();
		} else {
			path = file.getParent();
		}

		pref.put(LAST_SAVE_LOCATION, path);
	}

	/**
	 * Gets the internal chooser being used.
	 * 
	 * @return
	 */
	public FileChooser getInternalChooser() {
		return chooser;
	}

}
