package framework.savefunction.saveableobjects;

import javafx.beans.property.DoubleProperty;

public class BindableDoubleSaveableObject extends SaveableObject {

	DoubleProperty value;

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object obj) {
		value.setValue((Number) obj);
	}

}
