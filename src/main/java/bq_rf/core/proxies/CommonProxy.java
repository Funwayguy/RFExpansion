package bq_rf.core.proxies;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import bq_rf.client.gui.UpdateNotification;
import bq_rf.core.BQRF;
import bq_rf.handlers.GuiHandler;
import bq_rf.network.PktHandlerRfTile;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

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
	
	public void registerExpansion()
	{
		IPacketRegistry pktReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
    	pktReg.registerHandler(new PktHandlerRfTile());
    	
    	ITaskRegistry tskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
    	tskReg.registerTask(FactoryTaskRfCharge.INSTANCE);
    	tskReg.registerTask(FactoryTaskRfRate.INSTANCE);
    	
    	BQRF.rfStation.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
    	
    	GameRegistry.addShapedRecipe(new ItemStack(BQRF.rfStation), "IRI", "RSR", "IRI", 'I', new ItemStack(Items.iron_ingot), 'R', new ItemStack(Items.redstone), 'S', new ItemStack((Block)Block.blockRegistry.getObject("betterquesting:submit_station")));
	}
}
