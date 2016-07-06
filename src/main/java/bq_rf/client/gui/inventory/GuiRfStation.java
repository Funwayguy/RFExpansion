package bq_rf.client.gui.inventory;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.misc.GuiQuestingContainer;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import bq_rf.block.TileRfStation;
import bq_rf.tasks.IRfTask;

public class GuiRfStation extends GuiQuestingContainer
{
	TileRfStation tile;
	ArrayList<QuestInstance> activeQuests = new ArrayList<QuestInstance>();
	int selQuest = 0;
	QuestInstance quest;
	int selTask = 0;
	TaskBase task;
	
	GuiEmbedded taskUI;
	GuiButtonQuesting btnSelect;
	GuiButtonQuesting btnRemove;
	
	ContainerRfStation subContainer;
	
	public GuiRfStation(GuiScreen parent, InventoryPlayer invo, TileRfStation tile)
	{
		super(parent, "bq_rf.title.rf_station", new ContainerRfStation(invo, tile));
		subContainer = (ContainerRfStation)this.inventorySlots;
		this.tile = tile;
		this.setMaxSize(500, 300);
	}
	
	public void initGui()
	{
		super.initGui();
		
		if(tile == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		selQuest = 0;
		selTask = 0;
		
		activeQuests = QuestDatabase.getActiveQuests(mc.thePlayer.getUniqueID());
		
		buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/2 - 120, guiTop + 32, 20, 20, "<")); // Prev Quest
		buttonList.add(new GuiButtonQuesting(2, guiLeft + sizeX/2 + 100, guiTop + 32, 20, 20, ">")); // Next Quest
		buttonList.add(new GuiButtonQuesting(3, guiLeft + sizeX/2 - 120, guiTop + 52, 20, 20, "<")); // Prev Task
		buttonList.add(new GuiButtonQuesting(4, guiLeft + sizeX/2 + 100, guiTop + 52, 20, 20, ">")); // Next Quest
		btnSelect = new GuiButtonQuesting(5, guiLeft + sizeX/2 - 20, guiTop + 72, 20, 20, EnumChatFormatting.GREEN + "\u2714"); // Select Task
		btnRemove = new GuiButtonQuesting(6, guiLeft + sizeX/2 + 00, guiTop + 72, 20, 20, EnumChatFormatting.RED + "x"); // Remove Task
		buttonList.add(btnSelect);
		buttonList.add(btnRemove);

		int invX = 16 + (sizeX/2 - 24)/2 - 162/2;
		int invY = 92 + (sizeY - 108)/2 - 98/2 + 22;
		
		subContainer.moveInventorySlots(invX + 1, invY + 1);
		
		invX += 18*3;
		invY -= 22;
		
		subContainer.moveSubmitSlot(invX + 1, invY + 1);
		
		invX += 18*2;
		
		subContainer.moveReturnSlot(invX + 1, invY + 1);
		
		RefreshValues();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mx, int my)
	{
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			this.initGui();
			return;
		}
		
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		int invX = guiLeft + 16 + (sizeX/2 - 24)/2 - 162/2;
		int invY = guiTop + 92 + (sizeY - 108)/2 - 98/2 + 22;
		
		for(int j = 0; j < 3; j++)
		{
			for(int i = 0; i < 9; i++)
			{
				this.drawTexturedModalRect(i*18 + invX, j*18 + invY, 0, 48, 18, 18);
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.drawTexturedModalRect(i*18 + invX, 58 + invY, 0, 48, 18, 18);
		}
		
		invX += 18*3;
		invY -= 22;
		
		this.drawTexturedModalRect(invX, invY, 0, 48, 18, 18);
		
		invX += 18*2;
		
		this.drawTexturedModalRect(invX, invY, 0, 48, 18, 18);
		
		mc.fontRendererObj.drawString("-->", invX - 17, invY + 7, Color.BLACK.getRGB(), false);
		mc.fontRendererObj.drawString("-->", invX - 17, invY + 6, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		if(quest != null)
		{
			mc.fontRendererObj.drawString(I18n.format(quest.name), guiLeft + sizeX/2 - 92, guiTop + 40, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		if(task != null)
		{
			mc.fontRendererObj.drawString(task.getDisplayName(), guiLeft + sizeX/2 - 92, guiTop + 60, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		if(taskUI != null)
		{
			taskUI.drawGui(mx, my, partialTick);
		}
	}
	
	@Override
	public void keyTyped(char character, int keyCode) throws IOException
	{
		super.keyTyped(character, keyCode);
		
		if(taskUI != null)
		{
			taskUI.keyTyped(character, keyCode);
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
		if(taskUI != null)
		{
			taskUI.handleMouse();
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(activeQuests.size() > 0)
			{
				selQuest = PositiveModulo(selQuest - 1, activeQuests.size());
			} else
			{
				selQuest = 0;
			}
			RefreshValues();
		} else if(button.id == 2)
		{
			if(activeQuests.size() > 0)
			{
				selQuest = PositiveModulo(selQuest + 1, activeQuests.size());
			} else
			{
				selQuest = 0;
			}
			RefreshValues();
		} else if(button.id == 3)
		{
			if(quest != null && quest.tasks.size() > 0)
			{
				selTask = PositiveModulo(selTask - 1, quest.tasks.size());
			} else
			{
				selTask = 0;
			}
			RefreshValues();
		} else if(button.id == 4)
		{
			if(quest != null && quest.tasks.size() > 0)
			{
				selTask = PositiveModulo(selTask + 1, quest.tasks.size());
			} else
			{
				selTask = 0;
			}
			RefreshValues();
		} else if(button.id == 5) // Select
		{
			tile.setupTask(mc.thePlayer.getUniqueID(), quest, (IRfTask)task);
			tile.SyncTile(null);
			RefreshValues();
		} else if(button.id == 6)
		{
			tile.reset();
			tile.SyncTile(null);
			RefreshValues();
		}
	}
	
	public void RefreshValues()
	{
		if(tile.getTask() != null)
		{
			quest = tile.getQuest();
			task = (TaskBase)tile.getTask();
			taskUI = task.getGui(quest, this, guiLeft + sizeX/2 + 8, guiTop + 92, sizeX/2 - 24, sizeY - 92 - 16);
			
			selQuest = Math.max(0, activeQuests.indexOf(quest));
			selTask = Math.max(0, quest.tasks.indexOf(task));
			
			for(int i = 1; i <= 4; i++)
			{
				((GuiButton)buttonList.get(i)).enabled = false; // Cannot change tile until old values are removed
			}
			
			btnSelect.enabled = false;
			btnRemove.enabled = true;
			return;
		} else
		{
			for(int i = 1; i <= 4; i++)
			{
				((GuiButton)buttonList.get(i)).enabled = true;
			}
			
			btnRemove.enabled = false;
		}
		
		if(selQuest < 0 || selQuest >= activeQuests.size())
		{
			quest = null;
			task = null;
		} else
		{
			quest = activeQuests.get(selQuest);
		}
		
		if(quest == null || selTask < 0 || selTask >= quest.tasks.size())
		{
			task = null;
		} else
		{
			task = quest.tasks.get(selTask);
		}
		
		if(task == null)
		{
			taskUI = null;
		} else
		{
			taskUI = task.getGui(quest, this, guiLeft + sizeX/2 + 8, guiTop + 92, sizeX/2 - 24, sizeY - 92 - 16);
		}
		
		btnSelect.enabled = task != null && task instanceof IRfTask && !task.isComplete(mc.thePlayer.getUniqueID());
	}
	
	/**
	 * Performs a modulo operation correcting negatives to positives
	 */
	int PositiveModulo(int value, int mod) // Shortcut method because I'm lazy
	{
		return (value%mod + mod)%mod;
	}
}
