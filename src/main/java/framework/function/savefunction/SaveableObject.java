package framework.function.savefunction;

/**
 * An object that is saveable.
 * 
 * @author sraj144
 *
 */
public abstract class SaveableObject {

	/**
	 * A unique name for the thing to save.
	 */
	String uniqueName;

	public abstract Object getValue();		
	public abstract void setValue(Object obj);
	
}
