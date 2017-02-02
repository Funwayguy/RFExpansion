package bq_rf.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITickableTask;
import betterquesting.api.utils.JsonHelper;
import bq_rf.client.gui.tasks.GuiTaskRfRate;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import cofh.api.energy.IEnergyContainerItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskRfRate implements ITask, IRfTask, IProgression<Integer>, ITickableTask
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	private HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	
	public int rate = 100000;
	public int duration = 200;
	public boolean delExcess = false;
	
	public HashMap<UUID, Long> lastInput = new HashMap<UUID, Long>();
	public long globalLast = 0L;
	public int globalProg = 0;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskRfRate.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return BQRF.MODID + ".task.rf_rate";
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		if(!completeUsers.contains(uuid))
		{
			completeUsers.add(uuid);
		}
	}

	@Override
	public void resetUser(UUID uuid)
	{
		userProgress.remove(uuid);
		completeUsers.remove(uuid);
	}

	@Override
	public void resetAll()
	{
		userProgress.clear();
		completeUsers.clear();
	}
	
	@Override
	public ItemStack submitItem(IQuest quest, UUID owner, ItemStack stack)
	{
		if(stack == null || isComplete(owner) || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return stack;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		
		int progress = getUsersProgress(owner);
		
		int extracted = eItem.extractEnergy(stack, delExcess? Integer.MAX_VALUE : rate, true);
		
		if(extracted >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			globalProg = Math.min(globalProg + 1, duration);
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
			globalLast = tmp;
		}
		
		setUserProgress(owner, progress);
		
		int total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= duration)
		{
			setComplete(owner);
		}
		
		return stack;
	}
	
	@Override
	public int submitEnergy(IQuest quest, UUID owner, int amount)
	{
		if(isComplete(owner))
		{
			return amount;
		}
		
		int progress = getUsersProgress(owner);
		
		if(amount >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			globalProg = Math.min(globalProg + 1, duration);
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
			globalLast = tmp;
		}
		
		setUserProgress(owner, progress);
		
		int total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= duration)
		{
			setComplete(owner);
		}
		
		return delExcess? 0 : (amount - rate);
	}
	
	@Override
	@Deprecated
	public void update(EntityPlayer player, IQuest quest){}
	
	@Override
	public void updateTask(EntityPlayer player, IQuest quest)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getUniqueID()) : getGlobalProgress();
		
		if(total >= duration)
		{
			setComplete(player.getUniqueID());
			return;
		}
		
		long last = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? GetUserLast(player.getUniqueID()) : GetGlobalLast();
		
		if(last != System.currentTimeMillis()/1000L) // Check if the last input was within an acceptable period of time
		{
			int progress = getUsersProgress(player.getUniqueID());
			progress = Math.max(0, progress - 1);
			setUserProgress(player.getUniqueID(), progress);
			globalProg = Math.max(0, globalProg - 1);
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		int total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getUniqueID()) : getGlobalProgress();
		
		if(total >= duration)
		{
			setComplete(player.getUniqueID());
		}
	}
	
	public long GetUserLast(UUID uuid)
	{
		Long l = lastInput.get(uuid);
		return l == null? 0 : l;
	}
	
	public long GetGlobalLast()
	{
		return globalLast;
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			return this.writeProgressToJson(json);
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.addProperty("rf", rate);
		json.addProperty("duration", duration);
		json.addProperty("voidExcess", delExcess);
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			this.readProgressFromJson(json);
			return;
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		rate = JsonHelper.GetNumber(json, "rf", 100000).intValue();
		duration = JsonHelper.GetNumber(json, "duration", 200).intValue();
		delExcess = JsonHelper.GetBoolean(json, "voidExcess", delExcess);
	}
	
	private void readProgressFromJson(JsonObject json)
	{
		completeUsers = new ArrayList<UUID>();
		for(JsonElement entry : JsonHelper.GetArray(json, "completeUsers"))
		{
			if(entry == null || !entry.isJsonPrimitive())
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
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
	
	private JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
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
		
		return json;
	}
	
	@Override
	public void setUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer getUsersProgress(UUID... uuid)
	{
		int total = 0;
		
		for(UUID mem : uuid)
		{
			Integer n = userProgress.get(mem);
			total += n == null? 0 : n;
		}
		
		return total;
	}
	
	public Integer getPartyProgress(UUID uuid)
	{
		int total = 0;
		
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
		
		if(party == null)
		{
			return getUsersProgress(uuid);
		} else
		{
			for(UUID mem : party.getMembers())
			{
				if(mem != null && party.getStatus(mem).ordinal() <= 0)
				{
					continue;
				}
				
				total += getUsersProgress(mem);
			}
		}
		
		return total;
	}
	
	@Override
	public Integer getGlobalProgress()
	{
		return globalProg;
	}
	
	@Override
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskRfRate(quest, this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public float getParticipation(UUID uuid)
	{
		return 0;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
