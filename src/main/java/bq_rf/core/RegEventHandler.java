package bq_rf.core;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class RegEventHandler
{
	public static final List<Item> ALL_ITEMS = new ArrayList<>();
	public static final List<Block> ALL_BLOCKS = new ArrayList<>();
	public static final List<IRecipe> ALL_RECIPES = new ArrayList<>();
	
	private static boolean setupRecipes = false;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModelEvent(ModelRegistryEvent event)
	{
		BQRF.proxy.registerRenderers();
	}
	
	@SubscribeEvent
	public static void registerBlockEvent(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(ALL_BLOCKS.toArray(new Block[0]));
	}
	
	@SubscribeEvent
	public static void registerItemEvent(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(ALL_ITEMS.toArray(new Item[0]));
	}
	
	@SubscribeEvent
	public static void registerRecipeEvent(RegistryEvent.Register<IRecipe> event)
	{
		if(!setupRecipes)
		{
			initRecipes();
		}
		
		IRecipe[] tmp = ALL_RECIPES.toArray(new IRecipe[0]);
		event.getRegistry().registerAll(tmp);
	}
    
    private static void registerBlock(Block b, String name)
    {
    	ResourceLocation res = new ResourceLocation(BQRF.MODID + ":" + name);
    	ALL_BLOCKS.add(b.setRegistryName(res));
    	ALL_ITEMS.add(new ItemBlock(b).setRegistryName(res));
    }
    
    private static void addShapedRecipe(String name, String group, ItemStack stack, Object... ing)
    {
    	ResourceLocation rName = new ResourceLocation(BQRF.MODID, name);
    	ResourceLocation rGroup = new ResourceLocation(BQRF.MODID, group);
    	
    	ALL_RECIPES.add(new ShapedOreRecipe(rGroup, stack, ing).setRegistryName(rName));
    }
    
    private static void initRecipes()
    {
    	addShapedRecipe("rf_station", "questing", new ItemStack(BQRF.rfStation), "IRI", "RSR", "IRI", 'I', new ItemStack(Items.IRON_INGOT), 'R', new ItemStack(Items.REDSTONE), 'S', new ItemStack(Block.REGISTRY.getObject(new ResourceLocation("betterquesting:submit_station"))));
    	
    	setupRecipes = true;
    }
	
	// SETUP ALL THE THINGS
	static {
    	registerBlock(BQRF.rfStation, "rf_station");
	}
}
