package bq_rf.block;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.QuestCache;
import bq_rf.core.BQRF;
import bq_rf.network.RfPacketType;
import bq_rf.tasks.IRfTask;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class TileRfStation extends TileEntity implements IEnergyReceiver, ISidedInventory
{
	private final ItemStack[] itemStacks = new ItemStack[2];
	private boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	private IQuest qCached;
	
	public TileRfStation()
	{
		super();
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote || !isSetup() || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;
		
		long wtt = worldObj.getTotalWorldTime();
		if(wtt%10 == 0 && owner != null)
		{
		    if(wtt%20 == 0) qCached = null;
            IQuest q = getQuest();
            IRfTask t = getTask();
            MinecraftServer server = MinecraftServer.getServer();
            EntityPlayerMP player = getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
            
			if(q != null && t != null && itemStacks[0] != null && itemStacks[1] == null)
			{
				ItemStack inStack = itemStacks[0].copy();
				ItemStack beforeStack = itemStacks[0].copy();
				
				if(isItemValidForSlot(0, inStack))
				{
					itemStacks[0] = t.submitItem(q, owner, inStack);
					
					if(t.isComplete(owner))
					{
						reset();
						needsUpdate = true;
			    		if(server != null) server.getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
					} else
					{
						if(!itemStacks[0].equals(beforeStack)) needsUpdate = true;
					}
				} else
				{
					itemStacks[1] = inStack;
					itemStacks[0] = null;
				}
			}
			
			if(needsUpdate)
			{
				needsUpdate = false;
				
				if(q != null && qc != null)
				{
					qc.markQuestDirty(questID);
				}
			}
			
			if(t != null && t.isComplete(owner))
			{
				reset();
	    		server.getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			}
		}
	}
	
	private EntityPlayerMP getPlayerByUUID(UUID uuid)
    {
        MinecraftServer server = MinecraftServer.getServer();
        if(server == null) return null;
        
        for(EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
        {
            if(player.getGameProfile().getId().equals(uuid)) return player;
        }
        
        return null;
    }
	
	@Override
	public int receiveEnergy(ForgeDirection dir, int energy, boolean simulate)
	{
	    if(!isSetup() || energy <= 0 || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return 0;
	    
		IQuest q = getQuest();
		IRfTask t = getTask();
		
		int remainder = 0;
		
		if(!simulate)
		{
			remainder = t.submitEnergy(q, owner, energy);
		
			if(t.isComplete(owner))
			{
				needsUpdate = true;
				reset();
	    		MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			} else
			{
				needsUpdate = true;//remainder != energy;
			}
		}
		
		return energy - remainder;
	}
	
    @Override
    public int getEnergyStored(ForgeDirection dir)
    {
        return 0;
    }
    
    @Override
    public int getMaxEnergyStored(ForgeDirection dir)
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean canConnectEnergy(ForgeDirection dir)
    {
        return isSetup();
    }

	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		if(idx < 0 || idx >= itemStacks.length)
		{
			return null;
		} else
		{
			return itemStacks[idx];
		}
	}

	@Override
	public ItemStack decrStackSize(int idx, int amount)
	{
		if(idx < 0 || idx >= itemStacks.length || itemStacks[idx] == null)
		{
			return null;
		}
		
        if (amount >= itemStacks[idx].stackSize)
        {
            ItemStack itemstack = itemStacks[idx];
            itemStacks[idx] = null;
            return itemstack;
        }
        else
        {
            itemStacks[idx].stackSize -= amount;
            ItemStack cpy = itemStacks[idx].copy();
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
	public void setInventorySlotContents(int idx, @Nonnull ItemStack stack)
	{
		if(idx < 0 || idx >= itemStacks.length)
		{
			return;
		}
		
		itemStacks[idx] = stack;
	}

	@Override
	public String getInventoryName()
	{
		return BQRF.rfStation.getLocalizedName();
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
        return owner == null || player.getUniqueID().equals(owner);
        
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
		if(idx != 0 || stack == null || !(stack.getItem() instanceof IEnergyContainerItem))
		{
			return false;
		}
		
		IEnergyContainerItem eItem = (IEnergyContainerItem)stack.getItem();
		return eItem.getEnergyStored(stack) > 0;
	}
	
	public IQuest getQuest()
	{
		if(questID < 0)
		{
			return null;
		} else
		{
		    if(qCached == null) qCached = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(questID);
			return qCached;
		}
	}
	
	public ITask getRawTask()
	{
		IQuest q = getQuest();
		
		if(q == null || taskID < 0)
		{
			return null;
		} else
		{
			return q.getTasks().getValue(taskID);
		}
	}
	
	public IRfTask getTask()
	{
		ITask t = getRawTask();
		return t instanceof IRfTask? (IRfTask)t : null;
	}
	
	public void setupTask(UUID owner, IQuest quest, ITask task)
	{
		if(owner == null || quest == null || task == null)
		{
			reset();
		}
		
		this.questID = QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest);
		this.qCached = quest;
		this.taskID = quest.getTasks().getID(task);
		
		if(this.questID < 0 || this.taskID < 0)
        {
            reset();
            return;
        }
		
		this.owner = owner;
		this.markDirty();
	}
	
	public boolean isSetup()
	{
		return owner != null && questID >= 0 && taskID >= 0;
	}
	
	public void reset()
	{
		owner = null;
		questID = -1;
		taskID = -1;
		qCached = null;
		this.markDirty();
	}

    /**
     * Overridden in a sign to provide the text.
     */
	@Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
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
	@Override
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
    		if(data != null) this.readFromNBT(data);
    		this.markDirty();
    		MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		NBTTagCompound tileData = new NBTTagCompound();
    		this.writeToNBT(tileData);
    		payload.setTag("tile", tileData);
    		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(RfPacketType.RF_TILE.GetLocation(), payload));
    	}
    }
	
	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		
		itemStacks[0] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("input"));
		itemStacks[1] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("output"));
		
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
		
		if(!isSetup()) // All data must be present for this to run correctly
		{
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
		tags.setTag("input", itemStacks[0] != null? itemStacks[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStacks[1] != null? itemStacks[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
	}
	
	private static final int[] slotsForFace = new int[]{0,1};
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return slotsForFace;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return slot == 0 && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot == 1;
	}
}
