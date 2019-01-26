package bq_rf.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import betterquesting.api.network.IPacketHandler;
import bq_rf.block.TileRfStation;

public class PktHandlerRfTile implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return RfPacketType.RF_TILE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null) return;
		
		NBTTagCompound tileData = data.getCompoundTag("tile");
		TileEntity tile = sender.world.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		
		if(tile instanceof TileRfStation) ((TileRfStation)tile).SyncTile(tileData);
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
