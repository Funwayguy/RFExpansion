package bq_rf.core.proxies;

import bq_rf.client.gui.UpdateNotification;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BQRF.instance, new GuiHandler());
	}

	public void registerThemes()
	{
	}
}
