package bq_rf.network;

import net.minecraft.util.ResourceLocation;
import bq_rf.core.BQRF;

public enum RfPacketType
{
	RF_TILE;
	
	private final ResourceLocation ID;
	
	private RfPacketType()
	{
		ID = new ResourceLocation(BQRF.MODID, this.toString().toLowerCase());
	}
	
	public ResourceLocation GetLocation()
	{
		return ID;
	}
}
