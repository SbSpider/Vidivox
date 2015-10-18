package framework;

import java.io.File;

public class ScratchDir {

	static File scratchDir;

	public static void setScratchDir(File dir) {
		scratchDir = dir;
	}
	
	public static File getScratchDir(){
		return scratchDir;
	}

}
