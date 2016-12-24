package bq_rf.core;

import betterquesting.api.api.IQuestExpansion;
import betterquesting.api.api.QuestExpansion;

@QuestExpansion
public class BQRF_Expansion implements IQuestExpansion
{
	@Override
	public void loadExpansion()
	{
		BQRF.proxy.registerExpansion();
	}
}
