package bq_rf.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import bq_rf.core.BQRF;

public class BlockRfStation extends BlockContainer
{
	public BlockRfStation()
	{
		super(Material.IRON);
		this.setHardness(1);
		this.setUnlocalizedName(BQRF.MODID + ".rf_station");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileRfStation();
	}

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    /**
     * Called upon block activation (right click on the block.)
     */
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		player.openGui(BQRF.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
    	}
        return true;
    }
	
	@Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileRfStation tileStation = (TileRfStation)world.getTileEntity(pos);

        if(tileStation != null)
        {
        	InventoryHelper.dropInventoryItems(world, pos, tileStation);
        }

        super.breakBlock(world, pos, state);
    }
}
