package bq_rf.block;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import bq_rf.core.BQRF;
import bq_rf.network.PacketRf;
import bq_rf.tasks.IRfTask;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;

public class TileRfStation extends TileEntity implements IEnergyReceiver, ISidedInventory
{
	ItemStack[] itemStack = new ItemStack[2];
	boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	@Override
	public boolean canConnectEnergy(ForgeDirection dir)
	{
		return true;
	}
	
	@Override
	public int getEnergyStored(ForgeDirection dir)
	{
		return 0;
	}
	
	@Override
	public int getMaxEnergyStored(ForgeDirection dir)
	{
		return 0;
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			return;
		}
		
		QuestInstance q = getQuest();
		IRfTask t = getTask();
		
		if(worldObj.getTotalWorldTime()%10 == 0)
		{
			if(owner != null && q != null && t != null && owner != null && itemStack[0] != null)
			{
				if(isItemValidForSlot(0, itemStack[0]))
				{
					Slot sIn = new Slot(this, 0, 0, 0);
					Slot sOut = new Slot(this, 1, 0, 0);
					t.submitItem(owner, sIn, sOut);
					
					if(((TaskBase)t).isComplete(owner))
					{
						q.UpdateClients();
						reset();
			    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
					} else
					{
						needsUpdate = true;
					}
				}
			}
			
			if(needsUpdate)
			{
				needsUpdate = false;
				
				if(q != null && !worldObj.isRemote)
				{
					q.UpdateClients();
				}
			} else if(t != null && ((TaskBase)t).isComplete(owner))
			{
				reset();
	    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			}
		}
	}
	
	@Override
	public int receiveEnergy(ForgeDirection dir, int energy, boolean simulate)
	{
		QuestInstance q = getQuest();
		IRfTask t = getTask();
		
		if(q == null || t == null || energy <= 0)
		{
			return 0;
		}
		
		int remainder = 0;
		int amount = energy;
		
		if(!simulate)
		{
			remainder = t.submitEnergy(owner, energy);
		
			if(((TaskBase)t).isComplete(owner))
			{
				q.UpdateClients();
				reset();
	    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			} else
			{
				needsUpdate = true;
			}
		}
		
		return amount - remainder;
	}

	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		if(idx < 0 || idx >= itemStack.length)
		{
			return null;
		} else
		{
			return itemStack[idx];
		}
	}

	@Override
	public ItemStack decrStackSize(int idx, int amount)
	{
		if(idx < 0 || idx >= itemStack.length || itemStack[idx] == null)
		{
			return null;
		}
		
        if (amount >= itemStack[idx].stackSize)
        {
            ItemStack itemstack = itemStack[idx];
            itemStack[idx] = null;
            return itemstack;
        }
        else
        {
            itemStack[idx].stackSize -= amount;
            ItemStack cpy = itemStack[idx].copy();
            cpy.stackSize = amount;
            return cpy;
        }
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int idx, ItemStack stack)
	{
		if(idx < 0 || idx >= itemStack.length)
		{
			return;
		}
		
		itemStack[idx] = stack;
	}

	@Override
	public String getInventoryName()
	{
		return "RF Submission Station";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if(owner == null || player.getUniqueID().equals(owner))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int idx, ItemStack stack)
	{
		if(idx != 0)
		{
			return false;
		}
		
		return stack != null && stack.getItem() instanceof IEnergyContainerItem;
	}
	
	public QuestInstance getQuest()
	{
		if(questID < 0)
		{
			return null;
		} else
		{
			return QuestDatabase.getQuestByID(questID);
		}
	}
	
	public IRfTask getTask()
	{
		QuestInstance q = getQuest();
		
		if(q == null || taskID < 0 || taskID >= q.tasks.size())
		{
			return null;
		} else
		{
			TaskBase t = q.tasks.get(taskID);
			return t instanceof IRfTask? (IRfTask)t : null;
		}
	}
	
	public void setupTask(UUID owner, QuestInstance quest, IRfTask task)
	{
		if(owner == null || quest == null || task == null)
		{
			reset();
		}
		
		this.owner = owner;
		this.questID = quest.questID;
		this.taskID = quest.tasks.indexOf(task);
		this.markDirty();
	}
	
	public boolean isSetup()
	{
		return owner != null && questID < 0 && taskID < 0;
	}
	
	public void reset()
	{
		owner = null;
		questID = -1;
		taskID = -1;
		this.markDirty();
	}

    /**
     * Overridden in a sign to provide the text.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbttagcompound);
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.func_148857_g());
    }
    
    /**
     * Ignores parameter on client side (uses own data instead)
     */
    public void SyncTile(NBTTagCompound data)
    {
    	if(!worldObj.isRemote)
    	{
    		this.readFromNBT(data);
    		this.markDirty();
    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		NBTTagCompound tileData = new NBTTagCompound();
    		this.writeToNBT(tileData);
    		payload.setTag("tile", tileData);
    		payload.setInteger("ID", 0);
    		BQRF.instance.network.sendToServer(new PacketRf(payload));
    	}
    }
	
	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		
		itemStack[0] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("input"));
		itemStack[1] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("ouput"));
		
		try
		{
			owner = UUID.fromString(tags.getString("owner"));
		} catch(Exception e)
		{
			this.reset();
			return;
		}
		
		questID = tags.hasKey("questID")? tags.getInteger("questID") : -1;
		taskID = tags.hasKey("task")? tags.getInteger("task") : -1;
		
		if(isSetup()) // All data must be present for this to run correctly
		{
			BQRF.logger.log(Level.ERROR, "One or more tags were missing!", new Exception());
			this.reset();
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		tags.setString("owner", owner != null? owner.toString() : "");
		tags.setInteger("questID", questID);
		tags.setInteger("task", taskID);
		tags.setTag("input", itemStack[0] != null? itemStack[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStack[1] != null? itemStack[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[]{0,1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return slot == 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot == 1;
	}
}
