package bq_rf.core.proxies;

import bq_rf.client.gui.UpdateNotification;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;
import bq_rf.network.PacketRf;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
    	
    	BQRF.instance.network.registerMessage(PacketRf.HandlerServer.class, PacketRf.class, 0, Side.SERVER);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BQRF.instance, new GuiHandler());
	}

	public void registerThemes()
	{
	}
}
