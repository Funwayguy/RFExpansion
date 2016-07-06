package bq_rf.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import betterquesting.network.handlers.PktHandler;
import bq_rf.block.TileRfStation;

public class PktHandlerRfTile extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return;
		}
		
		NBTTagCompound tileData = data.getCompoundTag("tile");
		TileEntity tile = sender.worldObj.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		
		if(tile != null && tile instanceof TileRfStation)
		{
			((TileRfStation)tile).SyncTile(tileData);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
