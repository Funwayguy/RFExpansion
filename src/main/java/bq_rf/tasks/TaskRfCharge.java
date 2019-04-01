package bq_rf.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_rf.client.gui.tasks.PanelTaskCharge;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
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

public class TaskRfCharge implements ITask, IRfTask, IProgression<Long>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	private final HashMap<UUID, Long> userProgress = new HashMap<>();
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
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(player.inventory == null) return;
		
		UUID uuid = QuestingAPI.getQuestingUUID(player);
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			
			if(stack == null) continue;
			
			stack = submitItem(quest, player.getUniqueID(), stack);
			player.inventory.setInventorySlotContents(i, stack);
			
			if(isComplete(uuid)) break;
		}
		
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
	}
	
	@Override
	public ItemStack submitItem(IQuest quest, UUID owner, ItemStack stack)
	{
		if(stack == null || !(stack.getItem() instanceof IEnergyContainerItem)) return stack;
        
        IEnergyContainerItem cap = (IEnergyContainerItem)stack.getItem();
		if(cap == null) return stack;
		
		Long progress = getUsersProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = cap.extractEnergy(stack, requesting, false);
		progress += extracted;
		setUserProgress(owner, progress);
		
		long total = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
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
		
		long total = !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(owner) : getGlobalProgress();
		
		if(total >= RF)
		{
			setComplete(owner);
		}
		
		return (amount - extracted);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setLong("rf", RF);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	    RF = nbt.getLong("rf");
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
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
		NBTTagList pList = nbt.getTagList("userProgress", 10);
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
			
			userProgress.put(uuid, pTag.getLong("value"));
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		nbt.setTag("completeUsers", jArray);
		
		NBTTagList progArray = new NBTTagList();
		for(Entry<UUID,Long> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			pJson.setLong("value", entry.getValue());
			progArray.appendTag(pJson);
		}
		nbt.setTag("userProgress", progArray);
		
		return nbt;
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
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
		return new PanelTaskCharge(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
}
