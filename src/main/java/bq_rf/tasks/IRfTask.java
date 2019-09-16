package bq_rf.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IRfTask extends ITask
{
	/**
	 * Submits an ItemEnergyContainer
	 */
	ItemStack submitItem(DBEntry<IQuest> quest, UUID owner, ItemStack stack);
	
	/**
	 * Submits raw RF energy to the task and returns any left over
	 */
	int submitEnergy(DBEntry<IQuest> quest, UUID owner, int amount);
}
