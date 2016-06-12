package bq_rf.tasks;

import java.util.UUID;
import betterquesting.quests.QuestInstance;
import net.minecraft.inventory.Slot;

public interface IRfTask
{
	/**
	 * Submits an ItemEnergyContainer
	 */
	public void submitItem(QuestInstance quest, UUID owner, Slot input, Slot output);
	
	/**
	 * Submits raw RF energy to the task and returns any left over
	 */
	public int submitEnergy(QuestInstance quest, UUID owner, int amount);
}
