package bq_rf.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_rf.client.gui.tasks.PanelTaskCharge;
import bq_rf.core.BQRF;
import bq_rf.tasks.factory.FactoryTaskRfCharge;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

public class TaskRfCharge implements ITask, IRfTask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
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
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
		if(pInfo.PLAYER.inventory == null || isComplete(pInfo.UUID)) return;
		
		for(int i = 0; i < pInfo.PLAYER.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = pInfo.PLAYER.inventory.getStackInSlot(i);
			
			if(stack.isEmpty()) continue;
			
			stack = submitItem(quest, pInfo.UUID, stack);
			pInfo.PLAYER.inventory.setInventorySlotContents(i, stack);
			
			if(isComplete(pInfo.UUID)) break;
		}
		
        pInfo.markDirty(Collections.singletonList(quest.getID()));
	}
	
	@Override
	public ItemStack submitItem(DBEntry<IQuest> quest, UUID owner, ItemStack stack)
	{
		if(stack.isEmpty()) return stack;
        
        IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
		if(cap == null) return stack;
		
		Long progress = getUsersProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = cap.extractEnergy(requesting, false);
		progress += extracted;
		setUserProgress(owner, progress);
		if(progress >= RF) setComplete(owner);
		
		return stack;
	}
	
	@Override
	public int submitEnergy(DBEntry<IQuest> quest, UUID owner, int amount)
	{
		Long progress = getUsersProgress(owner);
		progress = progress != null? progress : 0;
		int requesting =  (int)Math.min(Integer.MAX_VALUE, RF - progress);
		int extracted = Math.min(amount, requesting);
		progress += extracted;
		setUserProgress(owner, progress);
		if(progress >= RF) setComplete(owner);
		
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
                userProgress.put(uuid, pTag.getLong("value"));
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
                
                Long data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setLong("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                pJson.setLong("value", data);
                progArray.appendTag(pJson);
            });
        }
		
		nbt.setTag("completeUsers", jArray);
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}
	
	private void setUserProgress(UUID uuid, Long progress)
	{
		userProgress.put(uuid, progress);
	}
	
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
		return new PanelTaskCharge(rect, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}
}
