package bq_rf.client.gui.tasks;

import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.RenderUtils;
import bq_rf.tasks.TaskRfRate;

public class GuiTaskRfRate extends GuiEmbedded
{
	ItemStack bottle = new ItemStack(Blocks.REDSTONE_BLOCK);
	QuestInstance quest;
	TaskRfRate task;
	
	public GuiTaskRfRate(QuestInstance quest, TaskRfRate task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		float required = task.rate;
		int midX = sizeX/2;
		String suffix2 = "";
		
		long progress = quest == null || !quest.globalQuest? task.GetPartyProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		if(required >= 1000000000)
		{
			required /= 1000000000F;
			suffix2 = "B";
		} else if(required >= 1000000)
		{
			required /= 1000000F;
			suffix2 = "M";
		} else if(required >= 1000)
		{
			required /= 1000F;
			suffix2 = "K";
		}
		
		DecimalFormat df = new DecimalFormat("0.##");
		
		int barProg = (int)(MathHelper.clamp_float(progress/(float)task.duration, 0F, 1F) * (barSize - 2));
		
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		GlStateManager.scale(2F, 2F, 2F);
		RenderUtils.RenderItemStack(screen.mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GlStateManager.popMatrix();
		GuiQuesting.drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		GuiQuesting.drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.RED.getRGB());
		String txt = TextFormatting.BOLD + df.format(required) + suffix2 + " RF/t";
		screen.mc.fontRendererObj.drawString(txt, posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}
}
