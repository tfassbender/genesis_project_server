package net.jfabricationgames.genesis_project_server.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigurationDataManager {
	
	private static final String CONFIG_RESOURCE_FILE = "config/configs.properties";
	
	private Map<String, String> configs;
	
	private static ConfigurationDataManager instance;
	
	private ConfigurationDataManager() throws IOException {
		loadConfigurations();
	}
	
	private void loadConfigurations() throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties configProperties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream(CONFIG_RESOURCE_FILE)) {
			configProperties.load(resourceStream);
		}
		
		for (Object key : configProperties.keySet()) {
			String keyString = key.toString();
			String configFile = configProperties.getProperty(keyString);
			
			String loadedConfig = loadConfigFileToString(configFile);
			configs.put(keyString, loadedConfig);
		}
	}
	
	private String loadConfigFileToString(String configFile) throws IOException {
		File file = new File(configFile);
		String loadedConfig = Files.readAllLines(file.toPath()).stream().collect(Collectors.joining("\n"));
		return loadedConfig;
	}
	
	public static synchronized ConfigurationDataManager getInstance() throws IOException {
		if (instance == null) {
			instance = new ConfigurationDataManager();
		}
		return instance;
	}
	
	public String getConfiguration(String configName) {
		return configs.get(configName);
	}
}