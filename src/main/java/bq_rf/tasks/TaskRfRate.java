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
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.JsonHelper;
import bq_rf.client.gui.tasks.GuiTaskRfRate;
import bq_rf.core.BQRF;
import cofh.api.energy.IEnergyContainerItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskRfRate extends TaskBase implements IRfTask, IProgressionTask<Integer>
{
	public long globalLast = 0L;
	public int globalProg = 0;
	public HashMap<UUID, Long> lastInput = new HashMap<UUID, Long>();
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public int rate = 100000;
	public int duration = 200;
	public boolean delExcess = false;
	
	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack stack = input.getStack();
		
		if(stack == null || isComplete(owner) || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		
		int progress = GetUserProgress(owner);
		
		int extracted = eItem.extractEnergy(stack, delExcess? Integer.MAX_VALUE : rate, true);
		
		if(extracted >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			globalProg = Math.min(globalProg + 1, duration);
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
			globalLast = tmp;
		}
		
		SetUserProgress(owner, progress);
		
		if(eItem.getEnergyStored(stack) <= 0)
		{
			output.putStack(stack);
			input.putStack(null);
		}
	}
	
	@Override
	public int submitEnergy(UUID owner, int amount)
	{
		if(isComplete(owner))
		{
			return amount;
		}
		
		int progress = GetUserProgress(owner);
		
		if(amount >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			globalProg = Math.min(globalProg + 1, duration);
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
			globalLast = tmp;
		}
		
		SetUserProgress(owner, progress);
		
		return delExcess? 0 : (amount - rate);
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int total = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		
		if(total >= duration)
		{
			setCompletion(player.getUniqueID(), true);
			return;
		}
		
		long last = quest == null || !quest.globalQuest? lastInput.get(player.getUniqueID()) : globalLast;
		
		if(last != System.currentTimeMillis()/1000L) // Check if the last input was within an acceptable period of time
		{
			int progress = GetUserProgress(player.getUniqueID());
			progress = Math.max(0, progress - 1);
			SetUserProgress(player.getUniqueID(), progress);
			globalProg = Math.max(0, globalProg - 1);
		}
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
		userProgress = new HashMap<UUID,Integer>();
		globalProg = 0;
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("rf", rate);
		json.addProperty("duration", duration);
		json.addProperty("voidExcess", delExcess);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		rate = JsonHelper.GetNumber(json, "rf", 100000).intValue();
		duration = JsonHelper.GetNumber(json, "duration", 200).intValue();
		delExcess = JsonHelper.GetBoolean(json, "voidExcess", delExcess);
		
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
		
		userProgress = new HashMap<UUID,Integer>();
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
			
			userProgress.put(uuid, JsonHelper.GetNumber(entry.getAsJsonObject(), "value", 0).intValue());
		}
	}
	
	@Override
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
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
		return new GuiTaskRfRate(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return BQRF.MODID + ".task.rf_rate";
	}
	
	@Override
	public void SetUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer GetUserProgress(UUID uuid)
	{
		Integer i = userProgress.get(uuid);
		return i == null? 0 : i;
	}
	
	@Override
	public Integer GetPartyProgress(UUID uuid)
	{
		int total = 0;
		
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
	public Integer GetGlobalProgress()
	{
		return globalProg;
	}
}
