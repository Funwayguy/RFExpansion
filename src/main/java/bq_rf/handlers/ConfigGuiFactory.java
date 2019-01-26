package bq_rf.handlers;

import bq_rf.client.gui.GuiBQRFConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {
    
    }
    
    @Override
    public boolean hasConfigGui()
    {
        return true;
    }
    
    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new GuiBQRFConfig(parentScreen);
    }
    
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }
}
