package techreborn.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techreborn.client.TechRebornCreativeTab;
import techreborn.init.ModBlocks;

import java.security.InvalidParameterException;
import java.util.List;

public class BlockMachineFrame extends Block {

    public static ItemStack getFrameByName(String name, int count) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(name)) {
                return new ItemStack(ModBlocks.machineframe, count, i);
            }
        }
        throw new InvalidParameterException("The part " + name + " could not be found.");
    }

    public static final String[] types = new String[]
            {"aluminum", "iron", "bronze", "brass", "steel", "titanium"};


    public BlockMachineFrame(Material material) {
        super(material);
        setUnlocalizedName("techreborn.machineFrame");
        setCreativeTab(TechRebornCreativeTab.instance);
        setHardness(1f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
        for (int meta = 0; meta < types.length; meta++) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    @Override
    public int damageDropped(int metaData) {
        return metaData;
    }

}
