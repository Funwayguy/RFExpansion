package bq_rf.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import bq_rf.client.gui.UpdateNotification;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new UpdateNotification());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BQRF.instance, new GuiHandler());
	}

	public void registerThemes()
	{
	}
	
	public void registerRenderers()
	{
	}
}
