package bq_rf.tasks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.JsonHelper;
import bq_rf.client.gui.tasks.GuiTaskRfCharge;
import bq_rf.core.BQRF;
import cofh.api.energy.IEnergyContainerItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskRfCharge extends TaskBase implements IRfTask, IProgressionTask<Long>
{
	public HashMap<UUID, Long> userProgress = new HashMap<UUID, Long>();
	public long RF = 100000;
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%20 == 0 && !QuestDatabase.editMode)
		{
			long total = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
			
			if(total >= RF)
			{
				setCompletion(player.getUniqueID(), true);
			}
		}
	}
	
	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(player.inventoryContainer == null || isComplete(player.getUniqueID()))
		{
			return;
		}
		
		for(int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++)
		{
			Slot ps = player.inventoryContainer.getSlot(i);
			
			if(ps == null)
			{
				continue;
			}
			
			submitItem(player.getUniqueID(), ps, ps);
			
			long total = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
			
			if(total >= RF)
			{
				setCompletion(player.getUniqueID(), true);
			}
		}
	}
	
	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack stack = input.getStack();
		
		if(stack == null || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		
		Long progress = GetUserProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = eItem.extractEnergy(stack, requesting, false);
		progress += extracted;
		SetUserProgress(owner, progress);
		
		if(eItem.getEnergyStored(stack) <= 0)
		{
			// Clear before place in case it's the slots are the same
			input.putStack(null);
			output.putStack(stack);
		}
	}
	
	@Override
	public int submitEnergy(UUID owner, int amount)
	{
		Long progress = GetUserProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = Math.min(amount, requesting);
		progress += extracted;
		SetUserProgress(owner, progress);
		
		return (int)(amount - extracted);
	}
	
	/**
	 * Called by repeatable quests to reset progress for the next attempt
	 */
	@Override
	public void ResetProgress(UUID uuid)
	{
		super.ResetProgress(uuid);
		userProgress.remove(uuid);
	}
	
	/**
	 * Clear all progress for all users
	 */
	@Override
	public void ResetAllProgress()
	{
		super.ResetAllProgress();
		userProgress = new HashMap<UUID,Long>();
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("rf", RF);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		RF = JsonHelper.GetNumber(json, "rf", 100000).longValue();
		
		if(json.has("userProgress"))
		{
			jMig = json;
		}
	}
	
	JsonObject jMig = null;
	
	@Override
	public void readProgressFromJson(JsonObject json)
	{
		super.readProgressFromJson(json);
		
		if(jMig != null)
		{
			json = jMig;
			jMig = null;
		}
		
		userProgress = new HashMap<UUID,Long>();
		for(JsonElement entry : JsonHelper.GetArray(json, "userProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			userProgress.put(uuid, JsonHelper.GetNumber(entry.getAsJsonObject(), "value", 0).longValue());
		}
	}
	
	@Override
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Long> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			try
			{
				pJson.addProperty("uuid", entry.getKey().toString());
				pJson.addProperty("value", entry.getValue());
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to save user progress for task", e);
			}
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
	}
	
	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskRfCharge(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public float GetParticipation(UUID uuid)
	{
		return (float)(GetUserProgress(uuid) / (double) RF);
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return BQRF.MODID + ".task.rf_charge";
	}
	
	@Override
	public void SetUserProgress(UUID uuid, Long progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Long GetUserProgress(UUID uuid)
	{
		Long i = userProgress.get(uuid);
		return i == null? 0 : i;
	}
	
	@Override
	public Long GetPartyProgress(UUID uuid)
	{
		long total = 0;
		
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			return GetUserProgress(uuid);
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				if(mem != null && mem.GetPrivilege() <= 0)
				{
					continue;
				}
				
				total += GetUserProgress(mem.userID);
			}
		}
		
		return total;
	}
	
	@Override
	public Long GetGlobalProgress()
	{
		long total = 0;
		
		for(Long i : userProgress.values())
		{
			total += i == null? 0 : i;
		}
		
		return total;
	}
}
