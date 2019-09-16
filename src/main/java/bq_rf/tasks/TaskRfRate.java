package bq_rf.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_rf.client.gui.tasks.PanelTaskRate;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfRate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

public class TaskRfRate implements ITask, IRfTask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	private final HashMap<UUID, Integer> userProgress = new HashMap<>();
	
	public int rate = 100000;
	public int duration = 200;
	public boolean delExcess = false;
	
	public final HashMap<UUID, Long> lastInput = new HashMap<>();
	
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
		completeUsers.add(uuid);
	}

	@Override
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
            userProgress.clear();
            completeUsers.clear();
        } else
        {
            userProgress.remove(uuid);
            completeUsers.remove(uuid);
        }
	}
	
	@Override
	public ItemStack submitItem(DBEntry<IQuest> quest, UUID owner, ItemStack stack)
	{
		if(stack.isEmpty()) return stack;
        
        IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
		if(cap == null) return stack;
		
		int progress = getUsersProgress(owner);
		
		int extracted = cap.extractEnergy(delExcess? Integer.MAX_VALUE : rate, true);
		
		if(extracted >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
		}
		
		setUserProgress(owner, progress);
		if(progress >= duration)
		{
			setComplete(owner);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            EntityPlayer player = server == null ? null : server.getPlayerList().getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) qc.markQuestDirty(quest.getID());
		}
		
		return stack;
	}
	
	@Override
	public int submitEnergy(DBEntry<IQuest> quest, UUID owner, int amount)
	{
		if(isComplete(owner)) return amount;
		
		int progress = getUsersProgress(owner);
		
		if(amount >= rate)
		{
			progress = Math.min(progress + 1, duration); // Adds extra to counter the update decrease
			long tmp = System.currentTimeMillis()/1000L;
			lastInput.put(owner, tmp); // Set the time the last input was received to the nearest second
		}
		
		setUserProgress(owner, progress);
		
		if(progress >= duration)
		{
			setComplete(owner);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            EntityPlayer player = server == null ? null : server.getPlayerList().getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) qc.markQuestDirty(quest.getID());
		}
		
		return delExcess? 0 : (amount - rate);
	}
	
	@Override
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
		int total = getUsersProgress(pInfo.UUID);
		if(total >= duration) setComplete(pInfo.UUID);
	}
	
	public long GetUserLast(UUID uuid)
	{
		Long l = lastInput.get(uuid);
		return l == null? 0 : l;
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
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge)
        {
            completeUsers.clear();
            userProgress.clear();
        }
		
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
		
		NBTTagList pList = nbt.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			try
			{
                NBTTagCompound pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                userProgress.put(uuid, pTag.getInteger("value"));
			} catch(Exception e)
			{
				BQRF.logger.log(Level.ERROR, "Unable to load user progress for task", e);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		NBTTagList progArray = new NBTTagList();
		
		if(users != null)
        {
            users.forEach((uuid) -> {
                if(completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
                
                Integer data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setInteger("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                pJson.setInteger("value", data);
                progArray.appendTag(pJson);
            });
        }
		
		nbt.setTag("completeUsers", jArray);
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}
	
	private void setUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
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
	
	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
		return new PanelTaskRate(rect, this);
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}
}
