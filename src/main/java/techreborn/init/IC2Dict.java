package techreborn.init;

import ic2.core.block.BlockIC2Fence;
import ic2.core.block.BlockTexGlass;
import ic2.core.block.type.ResourceBlock;
import ic2.core.block.wiring.CableType;
import ic2.core.item.block.ItemCable;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.MiscResourceType;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import techreborn.Core;

/**
 * Created by modmuss50 on 16/07/2016.
 */
public class IC2Dict {

	public static void init() {

		IC2Duplicates.GRINDER.setIc2Stack(BlockName.te.getItemStack(TeBlock.macerator.getName()));
		IC2Duplicates.ELECTRICAL_FURNACE.setIc2Stack(BlockName.te.getItemStack(TeBlock.electric_furnace.getName()));
		IC2Duplicates.IRON_FURNACE.setIc2Stack(BlockName.te.getItemStack(TeBlock.iron_furnace.getName()));
		IC2Duplicates.GENERATOR.setIc2Stack(BlockName.te.getItemStack(TeBlock.generator.getName()));
		IC2Duplicates.EXTRACTOR.setIc2Stack(BlockName.te.getItemStack(TeBlock.extractor.getName()));
		IC2Duplicates.SOLAR_PANEL.setIc2Stack(BlockName.te.getItemStack(TeBlock.solar_generator.getName()));
		IC2Duplicates.RECYCLER.setIc2Stack(BlockName.te.getItemStack(TeBlock.recycler.getName()));
		IC2Duplicates.COMPRESSOR.setIc2Stack(BlockName.te.getItemStack(TeBlock.compressor.getName()));
		IC2Duplicates.BAT_BOX.setIc2Stack(BlockName.te.getItemStack(TeBlock.batbox.getName()));
		IC2Duplicates.MFE.setIc2Stack(BlockName.te.getItemStack(TeBlock.mfe.getName()));
		IC2Duplicates.MFSU.setIc2Stack(BlockName.te.getItemStack(TeBlock.mfsu.getName()));
		IC2Duplicates.LVT.setIc2Stack(BlockName.te.getItemStack(TeBlock.lv_transformer.getName()));
		IC2Duplicates.MVT.setIc2Stack(BlockName.te.getItemStack(TeBlock.mv_transformer.getName()));
		IC2Duplicates.HVT.setIc2Stack(BlockName.te.getItemStack(TeBlock.hv_transformer.getName()));
		IC2Duplicates.CABLE_COPPER.setIc2Stack(getIC2Cable(CableType.copper, 0));
		IC2Duplicates.CABLE_GOLD.setIc2Stack(getIC2Cable(CableType.gold, 0));
		IC2Duplicates.CABLE_ICOPPER.setIc2Stack(getIC2Cable(CableType.copper, 1));
		IC2Duplicates.CABLE_IGOLD.setIc2Stack(getIC2Cable(CableType.gold, 1));
		IC2Duplicates.CABLE_HV.setIc2Stack(getIC2Cable(CableType.tin, 0));
		IC2Duplicates.CABLE_IHV.setIc2Stack(getIC2Cable(CableType.tin, 1));
		IC2Duplicates.CABLE_IIHV.setIc2Stack(getIC2Cable(CableType.tin, 2));
		IC2Duplicates.CABLE_GLASSFIBER.setIc2Stack(getIC2Cable(CableType.glass, 0));

		IC2Duplicates.UPGRADE_OVERCLOCKER.setIc2Stack(ItemName.upgrade.getItemStack("overclocker"));
		IC2Duplicates.UPGRADE_STORAGE.setIc2Stack(ItemName.upgrade.getItemStack("energy_storage"));
		IC2Duplicates.UPGRADE_TRANSFORMER.setIc2Stack(ItemName.upgrade.getItemStack("transformer"));
		IC2Duplicates.MIXED_METAL.setIc2Stack(ItemName.ingot.getItemStack("alloy"));
		//Rubber - ore dic: itemRubber, hidden from JEI
		//Rubber Sap - only used to make rubber, hidden from JEI
		//Rubber tree blocks, hidden when deduplication is on, and rubber tress are not set to gen, includes tree taps

		try {
			CraftingItemType.circuit.getName();

			OreDictionary.registerOre("reBattery", ItemName.re_battery.getItemStack());

			OreDictionary.registerOre("circuitBasic", ItemName.crafting.getItemStack(CraftingItemType.circuit));
			OreDictionary.registerOre("circuitAdvanced", ItemName.crafting.getItemStack(CraftingItemType.advanced_circuit));

			OreDictionary.registerOre("machineBlockBasic", BlockName.resource.getItemStack(ResourceBlock.machine));
			OreDictionary.registerOre("machineBlockAdvanced", BlockName.resource.getItemStack(ResourceBlock.advanced_machine));

			OreDictionary.registerOre("lapotronCrystal", ItemName.lapotron_crystal.getItemStack());
			OreDictionary.registerOre("energyCrystal", ItemName.lapotron_crystal.getItemStack());

			OreDictionary.registerOre("drillBasic", ItemName.drill.getItemStack());
			OreDictionary.registerOre("drillDiamond", ItemName.diamond_drill.getItemStack());
			OreDictionary.registerOre("drillAdvanced", ItemName.iridium_drill.getItemStack());

			ItemStack industrialTnt = BlockName.te.getItemStack(TeBlock.itnt);
			industrialTnt.setItemDamage(1);
			OreDictionary.registerOre("industrialTnt", industrialTnt);

			OreDictionary.registerOre("craftingIndustrialDiamond", ItemName.crafting.getItemStack(CraftingItemType.industrial_diamond));
			OreDictionary.registerOre("fertilizer", ItemName.crafting.getItemStack(CraftingItemType.bio_chaff));
			OreDictionary.registerOre("hvTransformer", BlockName.te.getItemStack(TeBlock.hv_transformer));

			//TODO:

			//OreDictionary.registerOre("insulatedGoldCableItem", BlockName.te.getItemStack(CableType.gold));

			//OreDictionary.registerOre("ic2Generator", ModBlocks.Generator);
			//OreDictionary.registerOre("ic2SolarPanel", ModBlocks.solarPanel);
			//OreDictionary.registerOre("ic2Macerator", ModBlocks.Grinder);
			//OreDictionary.registerOre("ic2Extractor", ModBlocks.Extractor);
			//OreDictionary.registerOre("ic2Windmill", ModBlocks.windMill);
			//OreDictionary.registerOre("ic2Watermill", ModBlocks.waterMill);

			OreDictionary.registerOre("uran235", ItemName.nuclear.getItemStack(NuclearResourceType.uranium_235));
			OreDictionary.registerOre("uran238", ItemName.nuclear.getItemStack(NuclearResourceType.uranium_238));
			OreDictionary.registerOre("smallUran238", ItemName.nuclear.getItemStack(NuclearResourceType.small_uranium_238));
			OreDictionary.registerOre("smallUran235", ItemName.nuclear.getItemStack(NuclearResourceType.small_uranium_235));

			OreDictionary.registerOre("fenceIron", BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
			OreDictionary.registerOre("rubberWood", BlockName.rubber_wood.getItemStack());
			OreDictionary.registerOre("glassReinforced", BlockName.glass.getItemStack(BlockTexGlass.GlassType.reinforced));

			OreDictionary.registerOre("oreIridium", ItemName.misc_resource.getItemStack(MiscResourceType.iridium_ore));
		} catch (NoClassDefFoundError notFound) {
			Core.logHelper.warn(
				"Can't enable integration: IC2 installed but cannot be hooked\n" +
					"Do you use incompatible IC2 version?\n" +
					"Please create issue on github and provide FULL LOG and mod list");
		} catch (Throwable error) {
			Core.logHelper.warn(
				"Exception thrown during IC2 integration init\n" +
					"Do you use incompatible IC2 version?\n" +
					"Please create issue on github and provide FULL LOG and mod list.\n" +
					"Error stack trace: ");
			error.printStackTrace();
		}
	}


	public static ItemStack getIC2Cable(CableType type, int insulation){
		if(insulation > type.maxInsulation){
			return null;
		}
		ItemCable itemCable = ItemName.cable.getInstance();
		return itemCable.getCable(type, insulation);
	}


}
