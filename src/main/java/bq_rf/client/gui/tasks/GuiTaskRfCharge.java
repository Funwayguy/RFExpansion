package bq_rf.client.gui.tasks;

import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.RenderUtils;
import bq_rf.tasks.TaskRfCharge;

public class GuiTaskRfCharge extends GuiEmbedded
{
	ItemStack bottle = new ItemStack(Blocks.redstone_block);
	QuestInstance quest;
	TaskRfCharge task;
	
	public GuiTaskRfCharge(QuestInstance quest, TaskRfCharge task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		double required = task.RF;
		int midX = sizeX/2;
		String suffix1 = "";
		String suffix2 = "";
		
		long progress = quest == null || !quest.globalQuest? task.GetPartyProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		double rf = progress;
		
		if(rf >= 1000000000000L)
		{
			rf /= 1000000000000D;
			suffix1 = "T";
		} else if(rf >= 1000000000L)
		{
			rf /= 1000000000D;
			suffix1 = "B";
		} else if(rf >= 1000000L)
		{
			rf /= 1000000D;
			suffix1 = "M";
		} else if(rf >= 1000L)
		{
			rf /= 1000D;
			suffix1 = "K";
		}
		
		if(required >= 1000000000000L)
		{
			required /= 1000000000000D;
			suffix2 = "T";
		} else if(required >= 1000000000L)
		{
			required /= 1000000000D;
			suffix2 = "B";
		} else if(required >= 1000000L)
		{
			required /= 1000000D;
			suffix2 = "M";
		} else if(required >= 1000L)
		{
			required /= 1000D;
			suffix2 = "K";
		}
		
		DecimalFormat df = new DecimalFormat("0.##");
		
		int barProg = (int)(MathHelper.clamp_float(progress/(float)task.RF, 0F, 1F) * (barSize - 2));
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glScalef(2F, 2F, 2F);
		RenderUtils.RenderItemStack(screen.mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GL11.glPopMatrix();
		GuiQuesting.drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		GuiQuesting.drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.RED.getRGB());
		String txt = EnumChatFormatting.BOLD + "" + df.format(rf) + suffix1 + "/" + df.format(required) + suffix2 + " RF";
		screen.mc.fontRenderer.drawString(txt, posX + sizeX/2 - screen.mc.fontRenderer.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}
}
