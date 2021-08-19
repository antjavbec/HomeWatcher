package org.lacabradev.homewatcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.xuggle.XuggleVideo;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;

public class HomeWatcher {
	private static final String ALREADY_RUNNING = "alreadyRunning";
	private static final String IP_CAM_URL = "http://%s/videostream.cgi?user=%s&pwd=%s&count=0";
	private static final Logger log = LogManager.getLogger();
	private static final String IP_CAM_IP = "ipcam.ip";
	private static final String IP_CAM_USER = "ipcam.user";
	private static final String IP_CAM_PASSWORD = "ipcam.password";
	private static HomeWatcher runningInstance;
	private static Configuration configuration;
	
	public static Configuration getConfiguration() {
		if (configuration == null) {
			throw new UnsupportedOperationException("No se ha proporcionado una configuraci�n");
		} else {
			return configuration;
		}
	}
	
	public static void main(String[] args) {
		boolean alreadyRunning = false;
    	try {
    		JUnique.acquireLock(HomeWatcher.class.getName(), new MessageHandler() {
				
				public String handle(String message) {
					if (ALREADY_RUNNING.equals(message)) {
						runningInstance.bringToFront();
					}
					return null;
				}
    		});
    		alreadyRunning = false;
    	} catch (AlreadyLockedException e) {
    		alreadyRunning = true;
    	}
    	
    	if (!alreadyRunning) {
    		runningInstance = new HomeWatcher();
    		runningInstance.launch(args);
    	} else {
   			JUnique.sendMessage(HomeWatcher.class.getName(), ALREADY_RUNNING);
    	}
	}
	
	// TODO - Cuando sea aplicaci�n gr�fica, eliminar este m�todo para cambiar el ciclo de aplicaci�n por el de JavaFX. 
	private void launch(String[] args) {
		if (args.length < 1) {
			log.error("No se ha proporcionado archivo de configuraci�n");
			System.exit(-1);
		}
		Path configFilePath = Paths.get(args[0]);
		if (!Files.exists(configFilePath) || Files.isDirectory(configFilePath)) {
			log.error("El archivo de configuraci�n indicado no existe o es un directorio: '" + args[0] + "'");
			System.exit(-2);
		}
		try {
			readConfiguration(configFilePath);
		} catch (ConfigurationException e) {
			log.error("Error leyendo el archivo de configuraci�n '" + configFilePath + "': " + e.getMessage());
			System.exit(-3);
		}
		
		Video<MBFImage> video;
		try {
//			video = new XuggleVideo("http://149.43.156.105/mjpg/video.mjpg");
			video = new XuggleVideo("http://192.168.1.131/videostream.cgi?user=admin&pwd=xeniaantonio&count=0");
			VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void readConfiguration(Path configFilePath) throws ConfigurationException {
		configuration = (new Configurations()).properties(configFilePath.toFile());
	}
	
	private void bringToFront() {
		// TODO - Cuando sea aplicaci�n gr�fica, traer al frente la aplicaci�n que ya est� corriendo.
	}
}
