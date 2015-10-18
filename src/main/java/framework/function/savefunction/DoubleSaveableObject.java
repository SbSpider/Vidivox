package framework.function.savefunction;

public class DoubleSaveableObject extends SaveableObject  {

	double value;

	public DoubleSaveableObject() {
	}

	public DoubleSaveableObject(double value) {
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object obj) {
		value = (double) obj;
	}

}
