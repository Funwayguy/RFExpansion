package bq_rf.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
import bq_rf.block.BlockRfStation;
import bq_rf.block.TileRfStation;
import bq_rf.core.proxies.CommonProxy;
import bq_rf.handlers.ConfigHandler;

@Mod(modid = BQRF.MODID, version = BQRF.VERSION, name = BQRF.NAME, guiFactory = "bq_rf.handlers.ConfigGuiFactory")
public class BQRF
{
    public static final String MODID = "bq_rf";
    public static final String VERSION = "CI_MOD_VERSION";
    public static final String HASH = "CI_MOD_HASH";
    public static final String BRANCH = "CI_MOD_BRANCH";
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
    	
    	registerBlock(rfStation, "rf_station");
    	GameRegistry.registerTileEntity(TileRfStation.class, "rf_station");
    	
    	proxy.registerRenderers();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
    
    /**
     * Because I'm lazy...
     */
    public void registerBlock(Block b, String name)
    {
    	ResourceLocation res = new ResourceLocation(MODID + ":" + name);
    	GameRegistry.register(b, res);
        GameRegistry.register(new ItemBlock(b).setRegistryName(res));
    }
    
    public void registerItem(Item i, String name)
    {
    	ResourceLocation res = new ResourceLocation(MODID + ":" + name);
        GameRegistry.register(i.setRegistryName(res));
    }
}
