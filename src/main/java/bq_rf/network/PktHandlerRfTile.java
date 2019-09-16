package bq_rf.network;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import bq_rf.block.TileRfStation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

public class PktHandlerRfTile
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("bq_rf:tile_sync");
    
    public static void registerHandler()
    {
         QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, PktHandlerRfTile::onServer);
    }
    
    public static void sendEdit(NBTTagCompound tag)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", tag);
         QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
		NBTTagCompound tileData = message.getFirst().getCompoundTag("data");
		TileEntity tile = message.getSecond().world.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		if(tile instanceof TileRfStation) ((TileRfStation)tile).SyncTile(tileData);
    }
}
