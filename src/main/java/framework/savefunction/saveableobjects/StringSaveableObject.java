package framework.savefunction.saveableobjects;

public class StringSaveableObject extends SaveableObject {

	String value;

	public StringSaveableObject() {
	}

	public StringSaveableObject(String value) {
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object obj) {
		value = (String) obj;
	}
}
