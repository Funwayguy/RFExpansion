package bq_rf.block;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import bq_rf.core.BQRF;
import bq_rf.network.RfPacketType;
import bq_rf.tasks.IRfTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileRfStation extends TileEntity implements IEnergyStorage, ISidedInventory, ITickable
{
	private final IItemHandler itemHandler;
	private final ItemStack[] itemStacks = new ItemStack[2];
	private boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	private IQuest qCached;
	
	public TileRfStation()
	{
		super();
		
		this.itemHandler = new RSItemHandler(this);
	}
	
	@Override
	public void update()
	{
		if(worldObj.isRemote || !isSetup() || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;
		
		long wtt = worldObj.getTotalWorldTime();
		if(wtt%10 == 0 && owner != null)
		{
		    if(wtt%20 == 0) qCached = null;
            IQuest q = getQuest();
            IRfTask t = getTask();
            MinecraftServer server = worldObj.getMinecraftServer();
            EntityPlayerMP player = server == null ? null : server.getPlayerList().getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            
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
			    		if(server != null) server.getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
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
	    		worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
			}
		}
	}
	
	@Override
	public int receiveEnergy(int energy, boolean simulate)
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
	    		worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
			} else
			{
				needsUpdate = true;//remainder != energy;
			}
		}
		
		return energy - remainder;
	}
	
	@Override
    public int extractEnergy(int amount, boolean sim)
    {
        return 0;
    }
    
    @Override
    public int getEnergyStored()
    {
        return 0;
    }
    
    @Override
    public int getMaxEnergyStored()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean canExtract()
    {
        return false;
    }
    
    @Override
    public boolean canReceive()
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
		return ItemStackHelper.getAndSplit(itemStacks, idx, amount);
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
	public String getName()
	{
		return BQRF.rfStation.getLocalizedName();
	}

	@Override
	public boolean hasCustomName()
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
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int idx, ItemStack stack)
	{
		if(idx != 0)
		{
			return false;
		}
		
		return stack.hasCapability(CapabilityEnergy.ENERGY, null) && stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored() > 0;
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
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.getNbtCompound());
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
    		worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		payload.setTag("tile", this.writeToNBT(new NBTTagCompound()));
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
	public NBTTagCompound writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		
		tags.setString("owner", owner != null? owner.toString() : "");
		tags.setInteger("questID", questID);
		tags.setInteger("task", taskID);
		tags.setTag("input", itemStacks[0] != null? itemStacks[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStacks[1] != null? itemStacks[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		
		return tags;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[]{0,1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side)
	{
		return slot == 0 && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side)
	{
		return slot == 1;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(itemStacks, index);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		itemStacks[0] = null;
		itemStacks[1] = null;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(BQRF.rfStation.getLocalizedName());
	}
	
	@Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY)
		{
			return true;
		}
		
        return super.hasCapability(capability, facing);
    }
	
	@Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
		} else if(capability == CapabilityEnergy.ENERGY)
        {
            return CapabilityEnergy.ENERGY.cast(this);
        }
		
        return super.getCapability(capability, facing);
    }
}
