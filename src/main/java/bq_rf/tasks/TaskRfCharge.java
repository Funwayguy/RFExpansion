package bq_rf.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import betterquesting.api.utils.JsonHelper;
import bq_rf.client.gui.tasks.GuiTaskRfCharge;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
import cofh.api.energy.IEnergyContainerItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskRfCharge implements ITask, IRfTask, IProgression<Long>
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	private HashMap<UUID, Long> userProgress = new HashMap<UUID, Long>();
	public long RF = 100000;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskRfCharge.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return BQRF.MODID + ".task.rf_charge";
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
	public void update(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%20 == 0 && !QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
		{
			long total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getUniqueID()) : getGlobalProgress();
			
			if(total >= RF)
			{
				setComplete(player.getUniqueID());
			}
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(player.inventoryContainer == null || isComplete(player.getUniqueID()))
		{
			return;
		}
		
		for(int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			
			if(stack == null)
			{
				continue;
			}
			
			stack = submitItem(quest, player.getUniqueID(), stack);
			player.inventory.setInventorySlotContents(i, stack);
			
			long total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getUniqueID()) : getGlobalProgress();
			
			if(total >= RF)
			{
				setComplete(player.getUniqueID());
				break;
			}
		}
	}
	
	@Override
	public ItemStack submitItem(IQuest quest, UUID owner, ItemStack stack)
	{
		if(stack == null || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return stack;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		
		Long progress = getUsersProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = eItem.extractEnergy(stack, requesting, false);
		progress += extracted;
		setUserProgress(owner, progress);
		
		long total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= RF)
		{
			setComplete(owner);
		}
		
		return stack;
	}
	
	@Override
	public int submitEnergy(IQuest quest, UUID owner, int amount)
	{
		Long progress = getUsersProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = Math.min(amount, requesting);
		progress += extracted;
		setUserProgress(owner, progress);
		
		long total = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= RF)
		{
			setComplete(owner);
		}
		
		return (int)(amount - extracted);
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
		
		json.addProperty("rf", RF);
		
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
		
		RF = JsonHelper.GetNumber(json, "rf", 100000).longValue();
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
	
	private JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
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
		
		return json;
	}
	
	@Override
	public float getParticipation(UUID uuid)
	{
		return (float)(getUsersProgress(uuid) / (double) RF);
	}
	
	@Override
	public void setUserProgress(UUID uuid, Long progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Long getUsersProgress(UUID... uuid)
	{
		long total = 0;
		
		for(UUID mem : uuid)
		{
			Long l = userProgress.get(mem);
			total += l == null? 0 : l;
		}
		
		return total;
	}
	
	public Long getPartyProgress(UUID uuid)
	{
		long total = 0;
		
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
	public Long getGlobalProgress()
	{
		long total = 0;
		
		for(Long i : userProgress.values())
		{
			total += i == null? 0 : i;
		}
		
		return total;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskRfCharge(quest, this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
	
	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
