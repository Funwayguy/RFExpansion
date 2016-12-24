package bq_rf.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_rf.core.BQRF;
import bq_rf.tasks.TaskRfRate;
import com.google.gson.JsonObject;

public class FactoryTaskRfRate implements IFactory<TaskRfRate>
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
	public TaskRfRate loadFromJson(JsonObject json)
	{
		TaskRfRate task = new TaskRfRate();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
