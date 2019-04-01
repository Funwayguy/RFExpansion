package bq_rf.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
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
import bq_rf.tasks.TaskRfRate;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.util.vector.Vector4f;

import java.text.DecimalFormat;
import java.util.UUID;

public class PanelTaskRate extends CanvasEmpty
{
    private static final DecimalFormat df = new DecimalFormat("0.##");
    private static final String[] suffixes = new String[]{"","K","M","B","T"};
    
    private final TaskRfRate task;
    private final IQuest quest;
    
    private IValueIO<Float> barValue;
    private UUID uuid;
    
    private boolean flipFlop = false;
    
    public PanelTaskRate(IGuiRect rect, IQuest quest, TaskRfRate task)
    {
        super(rect);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if((System.currentTimeMillis()/500L)%2 == 0 != flipFlop)
        {
            flipFlop = !flipFlop;
            
            long progress = !quest.getProperty(NativeProps.GLOBAL) ? task.getPartyProgress(uuid) : task.getGlobalProgress();
            final float percent = (float)((double)progress/(double)task.duration);
            
            barValue.writeValue(percent);
        }
        
        super.drawPanel(mx, my, partialTick);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        Minecraft mc = Minecraft.getMinecraft();
        uuid = QuestingAPI.getQuestingUUID(mc.thePlayer);
        
        String strReq = formatRF(task.rate);
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -16, -32, 32, 32, 0), new ItemTexture(new BigItemStack(Blocks.redstone_block))));
        
        long progress = !quest.getProperty(NativeProps.GLOBAL) ? task.getPartyProgress(uuid) : task.getGlobalProgress();
        final float percent = (float)((double)progress/(double)task.duration);
        
        PanelHBarFill fillBar = new PanelHBarFill(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 0, 0, -16), 0));
        fillBar.setFillColor(new GuiColorStatic(0xFFFF0000));
        barValue = new FloatSimpleIO(percent, 0F, 1F).setLerp(true, 0.01F);
        fillBar.setFillDriver(barValue);
        this.addPanel(fillBar);
        
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 4, 0, -16), -1), EnumChatFormatting.BOLD + strReq + " RF/t").setAlignment(1));
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
