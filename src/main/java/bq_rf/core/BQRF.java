package bq_rf.core;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.quests.tasks.TaskRegistry;
import bq_rf.block.BlockRfStation;
import bq_rf.block.TileRfStation;
import bq_rf.core.proxies.CommonProxy;
import bq_rf.handlers.ConfigHandler;
import bq_rf.network.PktHandlerRfTile;
import bq_rf.network.RfPacketType;
import bq_rf.tasks.TaskRfCharge;
import bq_rf.tasks.TaskRfRate;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = BQRF.MODID, version = BQRF.VERSION, name = BQRF.NAME, guiFactory = "bq_rf.handlers.ConfigGuiFactory")
public class BQRF
{
    public static final String MODID = "bq_rf";
    public static final String VERSION = "BQ_RF_VER";
    public static final String NAME = "RF Expansion";
    public static final String PROXY = "bq_rf.core.proxies";
    public static final String CHANNEL = "BQ_RF";
	
	@Instance(MODID)
	public static BQRF instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static Block rfStation = new BlockRfStation();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	PacketTypeRegistry.RegisterType(new PktHandlerRfTile(), RfPacketType.RF_TILE.GetLocation());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.registerThemes();
    	
    	GameRegistry.registerBlock(rfStation, "rf_station");
    	GameRegistry.registerTileEntity(TileRfStation.class, "rf_station");
    	
    	TaskRegistry.RegisterTask(TaskRfCharge.class, new ResourceLocation(MODID + ":rf_charge"));
    	TaskRegistry.RegisterTask(TaskRfRate.class, new ResourceLocation(MODID + ":rf_rate"));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	GameRegistry.addShapedRecipe(new ItemStack(rfStation), "IRI", "RSR", "IRI", 'I', new ItemStack(Items.iron_ingot), 'R', new ItemStack(Items.redstone), 'S', new ItemStack(BetterQuesting.submitStation));
    }
}
