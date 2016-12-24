package bq_rf.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_rf.core.BQRF;
import bq_rf.tasks.TaskRfCharge;
import com.google.gson.JsonObject;

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
	public TaskRfCharge loadFromJson(JsonObject json)
	{
		TaskRfCharge task = new TaskRfCharge();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
