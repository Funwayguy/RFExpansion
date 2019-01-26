package bq_rf.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;
import bq_rf.network.PktHandlerRfTile;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(BQRF.instance, new GuiHandler());
	}
 
	public void registerThemes()
	{
	}
	
	public void registerRenderers()
	{
	}
	
	public void registerExpansion()
	{
		IPacketRegistry pktReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
    	pktReg.registerHandler(new PktHandlerRfTile());
    	
    	ITaskRegistry tskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
    	tskReg.registerTask(FactoryTaskRfCharge.INSTANCE);
    	tskReg.registerTask(FactoryTaskRfRate.INSTANCE);
    	
    	BQRF.rfStation.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}
}
