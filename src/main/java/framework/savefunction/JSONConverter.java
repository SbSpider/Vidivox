package framework.savefunction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Converter for SaveFileDO to and from json.
 * 
 * @author sraj144
 *
 */
public final class JSONConverter {

	/**
	 * Converts the object to json.
	 * 
	 * @param saveFileDO
	 *            the object to convert.
	 * @return the json return.
	 */
	public static String convertToJson(SaveFileDO saveFileDO) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		String json = gson.toJson(saveFileDO, saveFileDO.getClass());

		return json;
	}

	/**
	 * Converts the json to a saveFileDO.
	 * 
	 * @param json
	 *            the JSON object.
	 * @return returns the java object.
	 */
	public static SaveFileDO convertToDO(String json) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		SaveFileDO saveFileDO = gson.fromJson(json, SaveFileDO.class);

		return saveFileDO;
	}

}
