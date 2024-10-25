package techreborn.blockentity.machine.tier2;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.RebornInventory;
import techreborn.blockentity.machine.GenericMachineBlockEntity;
import techreborn.blockentity.machine.tier0.block.BlockProcessable;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;

import java.util.Iterator;
import java.util.Set;

public class MinerBlockEntity extends GenericMachineBlockEntity implements BuiltScreenHandlerProvider, BlockProcessable {

	/**
	 * Horizontal manhattan distance the ore prospection can cover per level.
	 */
	static final int DEFAULT_PROSPECTING_RANGE = 2;

	// Vertical distance the mining pipe can be extended from the miner
	static final int DEFAULT_DEPTH = 0;

	// Blocks per tick that are examined when a vertical level is scanned for ore.
	static final float PROSPECTING_SPEED = 1f;

	static final int MINING_TOOL_SLOT = 0;
	static final int PROSPECTING_TOOL_SLOT = 1;

	static final int MINING_PIPE_SLOT = 2;
	static final int OUTPUT_SLOT = 3;
	static final int ENERGY_SLOT = 4;

	//TODO: Add scanner when it is finished
	static final Set<Item> ALLOWED_PROSPECTING_TOOLS = Set.of();

	private int timeToMine;

	// Depth that can be reached with current tools
	private int maxDepth;

	private int prospectingRange;

	private boolean done;

	private BlockPos probePosition;
	private BlockPos minedBlock;
	private BlockPos prospectedTargetBlock;

	private Iterator<BlockPos> blockProspectionIterator;



	public MinerBlockEntity(BlockPos pos, BlockState state) {
		super(TRBlockEntities.MINER, pos, state, "Miner", 128, 10_000, TRContent.Machine.MINER.block, 0);
		// Inventory
		this.inventory = new RebornInventory<>(5, "MinerBlockEntity", 64, this);
		this.maxDepth = DEFAULT_DEPTH;
		this.done = false;
	}

	public static boolean canUseAsMiningTool(ItemStack itemStack){
		return itemStack.isIn(TRContent.ItemTags.MINER_ACCEPTED_TOOLS);
	}

	public static boolean canUseAsProspectingTool(ItemStack itemStack){
		return ALLOWED_PROSPECTING_TOOLS.contains(itemStack.getItem());
	}

	@Override
	public BuiltScreenHandler createScreenHandler(int syncID, PlayerEntity player) {
		return new ScreenHandlerBuilder("miner")
			.player(player.getInventory()).inventory().hotbar().addInventory().blockEntity(this)
			.filterSlot(MINING_TOOL_SLOT, 25 + 20, 25, MinerBlockEntity::canUseAsMiningTool)
			.filterSlot(MINING_PIPE_SLOT, 25 + 20 + 20, 25, itemStack -> itemStack.isOf(TRContent.MINING_PIPE.asItem()))
			.filterSlot(PROSPECTING_TOOL_SLOT, 25 + 20 + 20 + 20, 25, MinerBlockEntity::canUseAsProspectingTool)
			.outputSlot(OUTPUT_SLOT, 25 + 20 + 20, 25 + 30)
			.energySlot(ENERGY_SLOT,8,72)
			.syncEnergyValue()
			.addInventory()
			.create(this, syncID);
	}

	@Override
	public void tick(World world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity) {
		super.tick(world, pos, state, blockEntity);
		if (world == null || world.isClient) return;

		charge(ENERGY_SLOT);

		// If we're done mining
		if (this.done) return;

		// If something is missing for mining to happen
		if (!miningNecessitiesFulfilled()) return;

		//If no scanner is present just dig straight down
		if (!hasProspectingTool()){
			// If we're not mining anything
			if(minedBlock == null){
				BlockState blockState = world.getBlockState(probePosition.down());
				if (blockState.isAir() ){
					probePosition = probePosition.down();
					return;
				} else {
					minedBlock = probePosition.down();
				}

			}
		}

		if (this.prospectedTargetBlock == null){
			this
		}


	}

	public void setupProspectionIterator(final BlockPos probePosition){
		this.blockProspectionIterator = new BlockProspectingIterable(probePosition, ).iterator();
	}

	public boolean miningNecessitiesFulfilled(){
		return hasMiningTool() && hasMiningPipe();
	}

	public boolean hasMiningTool(){
		ItemStack miningTool = this.inventory.getStack(MINING_TOOL_SLOT);
		// TODO See if I need to double check the mining tool being an actual mining tool or if the filtered slot suffices.
		return MinerBlockEntity.canUseAsMiningTool(miningTool);
	}

	public boolean hasMiningPipe(){
		ItemStack miningPipe = this.inventory.getStack(MINING_PIPE_SLOT);
		return miningPipe.isOf(TRContent.MINING_PIPE.asItem());
	}

	public boolean hasProspectingTool(){
		ItemStack scanner = this.inventory.getStack(PROSPECTING_TOOL_SLOT);
		return !scanner.isEmpty();
	}

	@Override
	public boolean consumeEnergy(int amount) {
		//TODO Figure out when this happens
		return true;
	}

	@Override
	public void playSound() {
		// TODO play some sounds
	}

	static class BlockProspectingIterable implements Iterable<BlockPos>{

		final BlockPos probePosition;
		final int probeRange;
		final boolean clockwise;

		int offsetX;
		int offsetZ;
		int direction;



		public BlockProspectingIterable(BlockPos probePosition, int probeRange, boolean rotateClockwise){
			this.probePosition = probePosition;
			this.probeRange = probeRange;
			this.clockwise = rotateClockwise;
			this.offsetX = 1;
			this.offsetZ = 0;
			this.direction = 0;
		}

		@NotNull
		@Override
		public Iterator<BlockPos> iterator() {
			return new Iterator<>() {
				@Override
				public boolean hasNext() {
					return offsetX + offsetZ < probeRange;
				}

				@Override
				public BlockPos next() {
					BlockPos nextPosition = probePosition.add(offsetX, 0, offsetZ);
					// rotate the offset by 90 degrees
					int temp = offsetZ;
					offsetZ = offsetX;
					offsetX = temp;

					if (clockwise){
						offsetZ = -offsetZ;
					}else{
						offsetX = -offsetX;
					}

					// if we rotated so the coordinate is in the first quadrant again
					if (offsetX >= 1 && offsetZ >= 0) {
						// We iterate to the next manhattan distance position
						if (offsetZ == 0) {
							offsetZ = offsetX;
							offsetX = 1;
						} else {
							offsetX++;
							offsetZ--;
						}
					}

					return nextPosition;
				}
			};
		}
	}

}
