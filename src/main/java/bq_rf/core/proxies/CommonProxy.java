package bq_rf.core.proxies;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
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
	
	public void registerExpansion()
	{
		IPacketRegistry pktReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
    	pktReg.registerHandler(new PktHandlerRfTile());
    	
    	ITaskRegistry tskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
    	tskReg.registerTask(FactoryTaskRfCharge.INSTANCE);
    	tskReg.registerTask(FactoryTaskRfRate.INSTANCE);
    	
    	GameRegistry.addShapedRecipe(new ItemStack(BQRF.rfStation), "IRI", "RSR", "IRI", 'I', new ItemStack(Items.IRON_INGOT), 'R', new ItemStack(Items.REDSTONE), 'S', new ItemStack((Block)Block.REGISTRY.getObject(new ResourceLocation("betterquesting:submit_station"))));
	}
}
