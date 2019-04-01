package bq_rf.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;
import bq_rf.network.PktHandlerRfTile;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.nbt.NBTTagCompound;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(BQRF.instance, new GuiHandler());
		
		IPacketRegistry pktReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
    	pktReg.registerHandler(new PktHandlerRfTile());
    	
    	IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> tskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
    	tskReg.register(FactoryTaskRfCharge.INSTANCE);
    	tskReg.register(FactoryTaskRfRate.INSTANCE);
    	
    	BQRF.rfStation.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}
 
	public void registerThemes()
	{
	}
	
	public void registerRenderers()
	{
	}
}
