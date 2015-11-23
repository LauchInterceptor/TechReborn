package techreborn.blocks.generator;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import techreborn.Core;
import techreborn.blocks.BlockMachineBase;
import techreborn.client.GuiHandler;
import techreborn.tiles.TileDieselGenerator;

public class BlockDieselGenerator extends BlockMachineBase {


    public BlockDieselGenerator(Material material) {
        super(material);
        setUnlocalizedName("techreborn.dieselgenerator");
    }


    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileDieselGenerator();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(fillBlockWithFluid(world, x, y, z, player, side, hitX, hitY, hitZ)){
            return true;
        }
        if (!player.isSneaking())
            player.openGui(Core.INSTANCE, GuiHandler.dieselGeneratorID, world, x, y,
                    z);
        return true;
    }


}
