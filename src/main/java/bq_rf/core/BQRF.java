package bq_rf.core;

import bq_rf.block.BlockRfStation;
import bq_rf.block.TileRfStation;
import bq_rf.core.proxies.CommonProxy;
import bq_rf.handlers.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = BQRF.MODID, version = BQRF.VERSION, name = BQRF.NAME, guiFactory = "bq_rf.handlers.ConfigGuiFactory")
public class BQRF
{
    public static final String MODID = "bq_rf";
    public static final String VERSION = "CI_MOD_VERSION";
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
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.registerThemes();
    	
    	GameRegistry.registerTileEntity(TileRfStation.class, new ResourceLocation(MODID, "rf_station"));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
}
