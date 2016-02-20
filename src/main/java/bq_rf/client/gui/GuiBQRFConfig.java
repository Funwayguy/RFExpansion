package bq_rf.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import bq_rf.core.BQRF;
import bq_rf.handlers.ConfigHandler;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBQRFConfig extends GuiConfig
{
	@SuppressWarnings({"rawtypes", "unchecked"})
	public GuiBQRFConfig(GuiScreen parent)
	{
		super(parent, new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), BQRF.MODID, false, false, BQRF.NAME);
	}
}
