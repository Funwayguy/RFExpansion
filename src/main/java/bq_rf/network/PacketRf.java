package bq_rf.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import bq_rf.block.TileRfStation;
import bq_rf.core.BQRF;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketRf implements IMessage
{
	NBTTagCompound tags = new NBTTagCompound();
	
	public PacketRf()
	{
	}
	
	public PacketRf(NBTTagCompound payload)
	{
		tags = payload;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tags = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		if(BQRF.proxy.isClient() && Minecraft.getMinecraft().thePlayer != null)
		{
			tags.setString("Sender", Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
			tags.setInteger("Dimension", Minecraft.getMinecraft().thePlayer.dimension);
		}
		
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandlerServer implements IMessageHandler<PacketRf,IMessage>
	{
		@Override
		public IMessage onMessage(PacketRf message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BQRF.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BQ Standard packet server side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BQRF.logger.log(Level.ERROR, "Recieved a packet server side with an invalid ID", new NullPointerException());
				return null;
			}
			
			EntityPlayer player = null;
			
			if(message.tags.hasKey("Sender"))
			{
				try
				{
					WorldServer world = MinecraftServer.getServer().worldServerForDimension(message.tags.getInteger("Dimension"));
					player = world.func_152378_a(UUID.fromString(message.tags.getString("Sender")));
				} catch(Exception e)
				{
					
				}
			}
			
			if(ID == 0 && player != null)
			{
				NBTTagCompound tileData = message.tags.getCompoundTag("tile");
				TileEntity tile = player.worldObj.getTileEntity(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z"));
				
				if(tile != null && tile instanceof TileRfStation)
				{
					((TileRfStation)tile).SyncTile(tileData);
				}
				return null;
			}
			
			return null;
		}
	}
	
	public static class HandlerClient implements IMessageHandler<PacketRf,IMessage>
	{
		@Override
		public IMessage onMessage(PacketRf message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BQRF.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BQ Standard packet client side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BQRF.logger.log(Level.ERROR, "Recieved a packet client side with an invalid ID", new NullPointerException());
				return null;
			}
			
			return null;
		}
	}
}
