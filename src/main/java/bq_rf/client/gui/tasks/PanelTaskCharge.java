package bq_rf.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.controls.io.FloatSimpleIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelHBarFill;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import bq_rf.tasks.TaskRfCharge;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

import java.text.DecimalFormat;
import java.util.UUID;

public class PanelTaskCharge extends CanvasEmpty
{
    private static final DecimalFormat df = new DecimalFormat("0.##");
    private static final String[] suffixes = new String[]{"","K","M","B","T"};
    
    private final TaskRfCharge task;
    
    private PanelTextBox txtValue;
    private IValueIO<Float> barValue;
    private UUID uuid;
    
    private boolean flipFlop = false;
    
    public PanelTaskCharge(IGuiRect rect, TaskRfCharge task)
    {
        super(rect);
        this.task = task;
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if((System.currentTimeMillis()/500L)%2 == 0 != flipFlop)
        {
            flipFlop = !flipFlop;
            
            long progress = task.getUsersProgress(uuid);
            
            String strReq = formatRF(task.RF);
            String strPrg = formatRF(progress);
            final float percent = (float)((double)progress/(double)task.RF);
            
            txtValue.setText(TextFormatting.BOLD + strPrg + " / " + strReq + " RF");
            barValue.writeValue(percent);
        }
        
        super.drawPanel(mx, my, partialTick);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        Minecraft mc = Minecraft.getMinecraft();
        uuid = QuestingAPI.getQuestingUUID(mc.player);
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -16, -32, 32, 32, 0), new ItemTexture(new BigItemStack(Blocks.REDSTONE_BLOCK))));
        
        long progress = task.getUsersProgress(uuid);
        
        String strReq = formatRF(task.RF);
        String strPrg = formatRF(progress);
        final float percent = (float)((double)progress/(double)task.RF);
        
        
        PanelHBarFill fillBar = new PanelHBarFill(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 0, 0, -16), 0));
        fillBar.setFillColor(new GuiColorStatic(0xFFFF0000));
        barValue = new FloatSimpleIO(percent, 0F, 1F).setLerp(true, 0.01F);
        fillBar.setFillDriver(barValue);
        this.addPanel(fillBar);
        
        txtValue = new PanelTextBox(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 4, 0, -16), -1), TextFormatting.BOLD + strPrg + " / " + strReq + " RF").setAlignment(1);
        this.addPanel(txtValue);
    }
    
    private String formatRF(long value)
    {
        String s = "";
        double n = 1;
        
        for(int i = suffixes.length - 1; i >= 0; i--)
        {
            n = Math.pow(1000D, i);
            if(Math.abs(value) >= n)
            {
                s = suffixes[i];
                break;
            }
        }
        
        return df.format(value / n) + s;
    }
}
