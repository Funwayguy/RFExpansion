package bq_rf.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_rf.core.BQRF;
import bq_rf.tasks.TaskRfCharge;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskRfCharge implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskRfCharge INSTANCE = new FactoryTaskRfCharge();
	
	private final ResourceLocation ID;
	
	private FactoryTaskRfCharge()
	{
		ID = new ResourceLocation(BQRF.MODID, "rf_charge");
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return ID;
	}

	@Override
	public TaskRfCharge createNew()
	{
		return new TaskRfCharge();
	}

	@Override
	public TaskRfCharge loadFromData(NBTTagCompound nbt)
	{
		TaskRfCharge task = new TaskRfCharge();
		task.readFromNBT(nbt);
		return task;
	}
	
}
