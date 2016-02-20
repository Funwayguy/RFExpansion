package bq_rf.core.proxies;

import bq_rf.core.BQRF;
import bq_rf.network.PacketRf;
import cpw.mods.fml.relauncher.Side;


public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
    	
    	BQRF.instance.network.registerMessage(PacketRf.HandlerClient.class, PacketRf.class, 0, Side.CLIENT);
	}
	
	@Override
	public void registerThemes()
	{
	}
}
