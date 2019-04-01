package bq_rf.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_rf.client.gui.tasks.PanelTaskRate;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskRfRate implements ITask, IRfTask, IProgression<Integer>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	private final HashMap<UUID, Integer> userProgress = new HashMap<>();
	
	public int rate = 100000;
	public int duration = 200;
	public boolean delExcess = false;
	
	public final HashMap<UUID, Long> lastInput = new HashMap<>();
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
		if(stack == null || !(stack.getItem() instanceof IEnergyContainerItem)) return stack;
        
        IEnergyContainerItem cap = (IEnergyContainerItem)stack.getItem();
		if(cap == null) return stack;
		
		int progress = getUsersProgress(owner);
		
		int extracted = cap.extractEnergy(stack, delExcess? Integer.MAX_VALUE : rate, true);
		
		if(extracted >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			globalProg = Math.min(globalProg + 1, duration);
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
			globalLast = tmp;
		}
		
		setUserProgress(owner, progress);
		
		int total = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
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
		
		int total = !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= duration)
		{
			setComplete(owner);
		}
		
		return delExcess? 0 : (amount - rate);
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		int total = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getUniqueID()) : getGlobalProgress();
		
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("rf", rate);
		nbt.setInteger("duration", duration);
		nbt.setBoolean("voidExcess", delExcess);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	    rate = nbt.getInteger("rf");
	    duration = nbt.getInteger("duration");
	    delExcess = nbt.getBoolean("voidExcess");
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress.clear();
		NBTTagList pList = json.getTagList("userProgress", 10);
		for(int i = 0; i < pList.tagCount(); i++)
		{
			NBTTagCompound pTag = pList.getCompoundTagAt(i);
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(pTag.getString("uuid"));
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			userProgress.put(uuid, pTag.getInteger("value"));
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
		NBTTagList progArray = new NBTTagList();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			pJson.setInteger("value", entry.getValue());
			progArray.appendTag(pJson);
		}
		json.setTag("userProgress", progArray);
		
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
    @SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
		return new PanelTaskRate(rect, quest, this);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public float getParticipation(UUID uuid)
	{
		return 0;
	}
}
