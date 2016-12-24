package bq_rf.client.gui.tasks;

import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.RenderUtils;
import bq_rf.tasks.TaskRfRate;

public class GuiTaskRfRate extends GuiElement implements IGuiEmbedded
{
	private final Minecraft mc;
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	private final ItemStack bottle = new ItemStack(Blocks.redstone_block);
	private IQuest quest;
	private TaskRfRate task;
	
	public GuiTaskRfRate(IQuest quest, TaskRfRate task, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		
		this.task = task;
		this.quest = quest;
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		float required = task.rate;
		int midX = sizeX/2;
		String suffix2 = "";
		
		long progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getPartyProgress(mc.thePlayer.getUniqueID()) : task.getGlobalProgress();
		
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
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glScalef(2F, 2F, 2F);
		RenderUtils.RenderItemStack(mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GL11.glPopMatrix();
		drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.RED.getRGB());
		String txt = EnumChatFormatting.BOLD + df.format(required) + suffix2 + " RF/t";
		mc.fontRenderer.drawString(txt, posX + sizeX/2 - mc.fontRenderer.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
	}

	@Override
	public void onMouseClick(int mx, int my, int click)
	{
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
}
