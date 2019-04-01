package bq_rf.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_rf.core.BQRF;
import bq_rf.tasks.TaskRfRate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskRfRate implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskRfRate INSTANCE = new FactoryTaskRfRate();
	
	private final ResourceLocation ID;
	
	private FactoryTaskRfRate()
	{
		ID = new ResourceLocation(BQRF.MODID, "rf_rate");
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return ID;
	}

	@Override
	public TaskRfRate createNew()
	{
		return new TaskRfRate();
	}

	@Override
	public TaskRfRate loadFromData(NBTTagCompound nbt)
	{
		TaskRfRate task = new TaskRfRate();
		task.readFromNBT(nbt);
		return task;
	}
	
}
