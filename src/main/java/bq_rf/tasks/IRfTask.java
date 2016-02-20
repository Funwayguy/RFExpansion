package bq_rf.tasks;

import java.util.UUID;
import net.minecraft.inventory.Slot;

public interface IRfTask
{
	/**
	 * Submits an ItemEnergyContainer
	 */
	public void submitItem(UUID owner, Slot input, Slot output);
	
	/**
	 * Submits raw RF energy to the task and returns any left over
	 */
	public int submitEnergy(UUID owner, int amount);
}
