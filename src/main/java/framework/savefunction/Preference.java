package framework.savefunction;

import framework.savefunction.saveableobjects.SaveableObject;

/**
 * A preference. This will be used to hold string values.
 * 
 * @author sraj144
 *
 */
public class Preference extends SaveableObject {

	private String value;

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object obj) {
		value = (String) obj;
	}
}
