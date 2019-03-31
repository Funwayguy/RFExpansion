package bq_rf.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IRfTask extends ITask
{
	/**
	 * Submits an ItemEnergyContainer
	 */
	ItemStack submitItem(IQuest quest, UUID owner, ItemStack stack);
	
	/**
	 * Submits raw RF energy to the task and returns any left over
	 */
	int submitEnergy(IQuest quest, UUID owner, int amount);
}
