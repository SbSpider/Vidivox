package framework.function.savefunction;

import java.io.Serializable;

/**
 * An object that is saveable.
 * 
 * @author sraj144
 *
 */
public abstract class SaveableObject implements Serializable {

	/**
	 * A unique name for the thing to save.
	 */
	String uniqueName;

	public abstract Object getValue();

	public abstract void setValue(Object obj);

}
