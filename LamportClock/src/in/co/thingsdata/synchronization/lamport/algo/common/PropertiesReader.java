package in.co.thingsdata.synchronization.lamport.algo.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
	
	private static Properties props = new Properties();
	
	static {
		try {
			loadProperties();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadProperties() throws FileNotFoundException, IOException {
		File serverConf = new File ("resources/config.properties");
		
		props.load(new FileInputStream(serverConf));
	}
	
	public static String getProperty (String property) {
		return props.getProperty(property);
	}

}
