package bq_rf.handlers;

import bq_rf.block.TileRfStation;
import bq_rf.client.gui.inventory.ContainerRfStation;
import bq_rf.client.gui.inventory.GuiRfStation;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(ID == 0 && tile instanceof TileRfStation)
		{
			return new ContainerRfStation(player.inventory, (TileRfStation)tile);
		}
		
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(ID == 0 && tile instanceof TileRfStation)
		{
			return new GuiRfStation(null, player.inventory, (TileRfStation)tile);
		}
		
		return null;
	}
}
