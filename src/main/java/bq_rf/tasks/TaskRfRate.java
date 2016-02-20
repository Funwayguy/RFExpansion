package bq_rf.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
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
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public int rate = 100000;
	
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
		int extracted = eItem.extractEnergy(stack, rate, true);
		progress = Math.max(progress, extracted);
		userProgress.put(owner, progress);
		
		if(eItem.getEnergyStored(stack) <= 0)
		{
			output.putStack(stack);
			input.putStack(null);
		}
		
		if(progress >= rate)
		{
			setCompletion(owner, true);
		}
	}
	
	@Override
	public int submitEnergy(UUID owner, int amount)
	{
		Integer progress = userProgress.get(owner);
		progress = progress != null? progress : 0;
		int extracted = Math.min(amount, rate);
		progress = Math.max(progress, extracted);
		userProgress.put(owner, progress);
		
		if(progress >= rate)
		{
			setCompletion(owner, true);
		}
		
		return amount;
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
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			pJson.addProperty("value", entry.getValue());
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		rate = JsonHelper.GetNumber(json, "rf", 100000).intValue();
		
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