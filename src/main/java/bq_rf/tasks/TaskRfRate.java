package bq_rf.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import bq_rf.client.gui.tasks.GuiTaskRfRate;
import bq_rf.core.BQRF;
import cofh.api.energy.IEnergyContainerItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskRfRate extends TaskBase implements IRfTask
{
	public HashMap<UUID, Long> lastInput = new HashMap<UUID, Long>();
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public int rate = 100000;
	public int duration = 200;
	public boolean delExcess = false;
	
	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack stack = input.getStack();
		
		if(stack == null || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		
		Integer progress = userProgress.get(owner);
		progress = progress != null? progress : 0;
		
		int extracted = eItem.extractEnergy(stack, delExcess? Integer.MAX_VALUE : rate, true);
		
		if(extracted >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			lastInput.put(owner, System.currentTimeMillis()/1000L); // Set the time the last input was received to the nearest second
		}
		
		userProgress.put(owner, progress);
		
		if(progress >= duration)
		{
			setCompletion(owner, true);
		}
		
		if(eItem.getEnergyStored(stack) <= 0)
		{
			output.putStack(stack);
			input.putStack(null);
		}
	}
	
	@Override
	public int submitEnergy(UUID owner, int amount)
	{
		Integer progress = userProgress.get(owner);
		progress = progress != null? progress : 0;
		
		if(amount >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			lastInput.put(owner, System.currentTimeMillis()/1000L); // Set the time the last input was received to the nearest second
		}
		
		userProgress.put(owner, progress);
		
		if(progress >= duration)
		{
			setCompletion(owner, true);
		}
		
		return delExcess? 0 : (amount - rate);
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		Long last = lastInput.get(player.getUniqueID());
		
		if(last == null || last != System.currentTimeMillis()/1000L) // Check if the last input was within an acceptable period of time
		{
			Integer progress = userProgress.get(player.getUniqueID());
			progress = progress != null? progress : 0;
			progress = Math.max(0, progress - 1);
			userProgress.put(player.getUniqueID(), progress);
		}
	}
	
	/**
	 * Called by repeatable quests to reset progress for the next attempt
	 */
	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}
	
	/**
	 * Clear all progress for all users
	 */
	@Override
	public void ResetAllProgress()
	{
		completeUsers = new ArrayList<UUID>();
		userProgress = new HashMap<UUID,Integer>();
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("rf", rate);
		json.addProperty("duration", duration);
		json.addProperty("voidExcess", delExcess);
		
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
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		rate = JsonHelper.GetNumber(json, "rf", 100000).intValue();
		duration = JsonHelper.GetNumber(json, "duration", 200).intValue();
		delExcess = JsonHelper.GetBoolean(json, "voidExcess", delExcess);
		
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
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskRfRate(this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return BQRF.MODID + ".task.rf_rate";
	}
	
}
