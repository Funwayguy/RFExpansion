package bq_rf.network;

import net.minecraft.util.ResourceLocation;
import betterquesting.core.BetterQuesting;
import bq_rf.core.BQRF;

public enum RfPacketType
{
	RF_TILE;
	
	public ResourceLocation GetLocation()
	{
		return new ResourceLocation(BQRF.MODID + ":" + this.toString().toLowerCase());
	}
	
	public String GetName()
	{
		return BetterQuesting.MODID + ":" + this.toString().toLowerCase();
	}
}
