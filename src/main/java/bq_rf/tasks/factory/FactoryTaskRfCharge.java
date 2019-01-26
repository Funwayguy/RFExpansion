package bq_rf.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_rf.core.BQRF;
import bq_rf.tasks.TaskRfCharge;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskRfCharge implements IFactory<TaskRfCharge>
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
	public TaskRfCharge loadFromNBT(NBTTagCompound nbt)
	{
		TaskRfCharge task = new TaskRfCharge();
		task.readFromNBT(nbt);
		return task;
	}
	
}
