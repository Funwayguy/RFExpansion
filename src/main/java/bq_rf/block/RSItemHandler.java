package bq_rf.block;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class RSItemHandler implements IItemHandlerModifiable
{
	private final TileRfStation tile;
	
	public RSItemHandler(TileRfStation tile)
	{
		this.tile = tile;
	}
	
	@Override
	public int getSlots()
	{
		return tile.getSizeInventory();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if(stack.isEmpty() || !tile.isItemValidForSlot(slot, stack))
		{
			return stack;
		}
		
		// Existing stack
		ItemStack ts1 = getStackInSlot(slot);
		
		if(!stack.isItemEqual(ts1))
		{
			return stack;
		}
		
		int inMax = Math.min(stack.getCount(), stack.getMaxStackSize() - ts1.getCount());
		// Input stack
		ItemStack ts2 = stack.copy();
		ts2.setCount(inMax);
		
		if(!simulate)
		{
			if(ts1.isEmpty())
			{
				ts1 = ts2;
			} else
			{
				ts1.grow(ts2.getCount());
			}
			
			tile.setInventorySlotContents(slot, ts1);
		}
		
		if(stack.getCount() > inMax)
		{
			// Left over stack
			ItemStack ts3 = stack.copy();
			ts3.setCount(stack.getCount() - inMax);
			return ts3;
		}
		
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(slot != 1 || amount <= 0)
		{
			return ItemStack.EMPTY;
		}
		
		if(!simulate)
		{
			return tile.decrStackSize(slot, amount);
		}
		
		ItemStack stack = getStackInSlot(slot);
		
		if(stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		
		int outMax = Math.min(stack.getCount(), amount);
		
		ItemStack ts1 = stack.copy();
		ts1.setCount(outMax);
		
		return ts1;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		tile.setInventorySlotContents(slot, stack);
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return tile.getStackInSlot(slot);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}
}
