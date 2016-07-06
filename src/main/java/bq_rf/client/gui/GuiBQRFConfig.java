package bq_rf.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import bq_rf.core.BQRF;
import bq_rf.handlers.ConfigHandler;

@SideOnly(Side.CLIENT)
public class GuiBQRFConfig extends GuiConfig
{
	public GuiBQRFConfig(GuiScreen parent)
	{
		super(parent, new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), BQRF.MODID, false, false, BQRF.NAME);
	}
}
