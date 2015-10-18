package framework.savefunction;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import framework.savefunction.saveableobjects.SaveableObject;

public class SaveFileDO implements Serializable {

	public SaveFileDO() {

	}

	/**
	 * The name of the save file.
	 */
	private String saveFilename;

	private String saveDirectory;

	// private Hashmap
	private HashMap<String, SaveableObject> saveableObjects = new HashMap<String, SaveableObject>();

	public String getSaveFilename() {
		return saveFilename;
	}

	public String getSaveDirectory() {
		return saveDirectory;
	}

	public void setSaveFilename(String saveFilename) {
		this.saveFilename = saveFilename;

		this.saveDirectory = new File(saveFilename).getParent();
	}

	public HashMap<String, SaveableObject> getSaveableObjects() {
		return saveableObjects;
	}

	public void addToHashmap(String key, SaveableObject value) {
		saveableObjects.put(key, value);
	}

	public void setSaveableObjects(HashMap<String, SaveableObject> saveableObjects) {
		this.saveableObjects = saveableObjects;
	}

}
