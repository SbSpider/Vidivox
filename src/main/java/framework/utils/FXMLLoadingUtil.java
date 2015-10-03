package framework.utils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FXMLLoadingUtil {

	private static Map<String, Parent> fxmlMap = new HashMap<String, Parent>();

	/**
	 * Private constructor.
	 */
	private FXMLLoadingUtil() {

	}

	/**
	 * "Registers fxml". This means that the name of the file specified is
	 * attempted to be found, and then loaded into a hashmap with the name of
	 * the fxml as the key. This means that any fxml being used should be
	 * preloaded - this makes sure that all the needed files can be found before
	 * usage.
	 * 
	 * @param fxmlFilename
	 *            name of the file to load (assumes that /fxml/ and the filename
	 *            extension are added automatically.
	 * @throws IOException
	 *             thrown if a file could not be found.
	 */
	public static void registerFXML(String fxmlFilename) throws IOException {
		Parent root = loadFXML(fxmlFilename);

		// Load into the fxmlMap.
		fxmlMap.put(fxmlFilename, root);
	}

	/**
	 * 
	 * Tries to get the fxml from the map.
	 * 
	 * @param fxmlFilename
	 *            the name of the fxml to get.
	 * @return the root object.
	 */
	public static Parent getFXMLRoot(String fxmlFilename) {
		Parent root = fxmlMap.get(fxmlFilename);
		return root;
	}

	/**
	 * Will load the fxml from the file location, in the folder /fxml and ending
	 * with .fmxl.
	 * 
	 * If the file can not be found, then the code will exit. This is because of
	 * sanity checking issues - if fxml can't be found, then a screen won't be
	 * visible.
	 * 
	 * @param fxmlFileName
	 * @return the root element from the fxml.
	 * @throws IOException
	 *             thrown if the file could not be found.
	 */
	private static Parent loadFXML(String fxmlFileName) throws IOException {
		Parent root = null;

		String url = "/fxml/" + fxmlFileName + ".fxml";
		root = registerFXML_FQ(url);

		return root;
	}

	/**
	 * Uses the fully qualified name to register.
	 * 
	 * @param videoPlayerFxmlFq
	 * @return
	 * @throws IOException
	 */
	public static Parent registerFXML_FQ(String videoPlayerFxmlFq) throws IOException {
		Parent root = null;

		URL resource = FXMLLoadingUtil.class.getClass().getResource(videoPlayerFxmlFq);
		root = FXMLLoader.load(resource);

		return root;
	}
}
