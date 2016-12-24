package bq_rf.tasks;

import java.util.UUID;
import net.minecraft.item.ItemStack;
import betterquesting.api.questing.IQuest;

public interface IRfTask
{
	/**
	 * Submits an ItemEnergyContainer
	 */
	public ItemStack submitItem(IQuest quest, UUID owner, ItemStack stack);
	
	/**
	 * Submits raw RF energy to the task and returns any left over
	 */
	public int submitEnergy(IQuest quest, UUID owner, int amount);
}
