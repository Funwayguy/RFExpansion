package bq_rf.handlers;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import bq_rf.core.BQRF;
import bq_rf.core.BQRF_Settings;

public class ConfigHandler
{
	public static Configuration config;
	
	public static void initConfigs()
	{
		if(config == null)
		{
			BQRF.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		config.load();
		
		BQRF_Settings.hideUpdates = config.getBoolean("Hide Updates", Configuration.CATEGORY_GENERAL, false, "Hide update notifications");
		
		config.save();
		
		BQRF.logger.log(Level.INFO, "Loaded configs...");
	}
}
