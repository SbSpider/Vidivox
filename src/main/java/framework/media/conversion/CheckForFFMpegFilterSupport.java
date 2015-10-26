package framework.media.conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks if a filter exists in the ffmpeg system. This will often be critical
 * and not be time consuming, and is therefore not made into a tas.
 * 
 * @author sraj144
 *
 */
public final class CheckForFFMpegFilterSupport {

	/**
	 * Checks the ffmpeg distribution for the filter.
	 * 
	 * @param filter
	 * @throws IOException 
	 */
	public static boolean check(String filter) throws IOException {
		ProcessBuilder procBuilder = new ProcessBuilder("ffmpeg", "-filters");
		Process process = procBuilder.start();
		
		InputStream inputStream = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		
		String line = null;
		
		List<String> filters = new ArrayList<String>();
		while((line = reader.readLine()) != null){
			filters.add(line);
		}
		
		for(String filterElement : filters){
			if(filterElement.contains(filter)){
				return true;
			}
		}
		
		return false;
	}

}
