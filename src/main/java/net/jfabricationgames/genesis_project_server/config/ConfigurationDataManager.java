package net.jfabricationgames.genesis_project_server.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationDataManager {
	
	private static final Logger LOGGER = LogManager.getLogger(ConfigurationDataManager.class);
	
	private static final String CONFIG_RESOURCE_FILE = "config/configs.properties";
	
	private Map<String, String> configs;
	
	private static ConfigurationDataManager instance;
	
	private ConfigurationDataManager() throws IOException {
		configs = new HashMap<String, String>();
		loadConfigurations();
	}
	
	public static synchronized ConfigurationDataManager getInstance() throws IOException {
		if (instance == null) {
			instance = new ConfigurationDataManager();
		}
		return instance;
	}
	
	private void loadConfigurations() throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties configProperties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream(CONFIG_RESOURCE_FILE)) {
			configProperties.load(resourceStream);
		}
		
		LOGGER.debug("loading configurations from configuration properties file");
		for (Object key : configProperties.keySet()) {
			String keyString = key.toString();
			String configFile = configProperties.getProperty(keyString);
			
			LOGGER.debug("keyString: " + key.toString() + "   configFile: " + configFile);
			
			String loadedConfig = loadConfigFileToString(configFile);
			//LOGGER.debug("loaded config: " + loadedConfig.substring(0, Math.min(200, loadedConfig.length())) + "...   configs: " + configs);
			configs.put(keyString, loadedConfig);
		}
	}
	
	private String loadConfigFileToString(String configFile) throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		File file = new File(loader.getResource(configFile).getFile());
		String loadedConfig = Files.readAllLines(file.toPath()).stream().collect(Collectors.joining("\n"));
		return loadedConfig;
	}
	
	public String getConfiguration(String configName) {
		return configs.get(configName);
	}
}