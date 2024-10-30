package techreborn.blockentity.machine.tier2;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.RcEnergyItem;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.RebornInventory;
import techreborn.blockentity.machine.GenericMachineBlockEntity;
import techreborn.blockentity.machine.tier0.block.BlockProcessable;
import techreborn.blockentity.machine.tier2.miner.MinerProcessingState;
import techreborn.blocks.machine.tier2.MiningPipeBlock;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;

import java.util.Iterator;
import java.util.Set;

public class MinerBlockEntity extends GenericMachineBlockEntity implements BuiltScreenHandlerProvider, BlockProcessable {

	/**
	 * Horizontal manhattan distance the ore prospection can cover per level.
	 */
	static final int DEFAULT_PROSPECTING_RANGE = 5;

	// Blocks per tick that are examined when a vertical level is scanned for ore.
	static final int DEFAULT_PROSPECTING_SPEED = 5;


	// Slots
	public static final int MINING_TOOL_SLOT = 0;
	public static final int PROSPECTING_TOOL_SLOT = MINING_TOOL_SLOT + 1;
	public static final int MINING_PIPE_INVENTORY_START = PROSPECTING_TOOL_SLOT + 1;
	public static final int MINING_PIPE_INVENTORY_SIZE = 3;
	public static final int OUTPUT_INVENTORY_START = MINING_PIPE_INVENTORY_START + MINING_PIPE_INVENTORY_SIZE + 1 ;
	public static final int OUTPUT_INVENTORY_SIZE = 12;
	public static final int ENERGY_SLOT = OUTPUT_INVENTORY_START + OUTPUT_INVENTORY_SIZE + 1;

	static final int INVENTORY_SIZE = ENERGY_SLOT + 1;

	//TODO: Add scanner when it is finished
	static final Set<Item> ALLOWED_PROSPECTING_TOOLS = Set.of();

	private static final Logger log = LoggerFactory.getLogger(MinerBlockEntity.class);

	private MinerProcessingState state;

	private Iterator<BlockPos> blockProspectionIterator;


	public MinerBlockEntity(BlockPos pos, BlockState state) {
		super(TRBlockEntities.MINER, pos, state, "Miner", 128, 10_000, TRContent.Machine.MINER.block, 0);
		// Inventory
		this.inventory = new RebornInventory<>(INVENTORY_SIZE, "MinerBlockEntity", 64, this);
		this.state = new MinerProcessingState.Ready();
	}

	public MinerProcessingState getState() {
		return state;
	}

	public static boolean canUseAsMiningTool(ItemStack itemStack) {
		return itemStack.isIn(TRContent.ItemTags.MINER_ACCEPTED_TOOLS);
	}

	public static boolean canUseAsProspectingTool(ItemStack itemStack) {
		return true;
//		return ALLOWED_PROSPECTING_TOOLS.contains(itemStack.getItem());
	}

	@Override
	public BuiltScreenHandler createScreenHandler(int syncID, PlayerEntity player) {
		var builder = new ScreenHandlerBuilder("miner")
			.player(player.getInventory()).inventory().hotbar().addInventory().blockEntity(this);

		int offsetX = 36;
		int offsetY = 21;

		builder.filterSlot(MINING_TOOL_SLOT, offsetX, offsetY, MinerBlockEntity::canUseAsMiningTool);
		builder.filterSlot(PROSPECTING_TOOL_SLOT, offsetX, offsetY + 36, MinerBlockEntity::canUseAsProspectingTool);

		for (int i = 0; i< MINING_PIPE_INVENTORY_SIZE; i++){
			builder.filterSlot(MINING_PIPE_INVENTORY_START + i, offsetX + 21, offsetY + i * 18, itemStack -> itemStack.isOf(TRContent.MINING_PIPE.asItem()));
		}

		for (int row=0; row < 3; row++){
			for (int column=0; column < 4; column++) {
				builder.outputSlot(OUTPUT_INVENTORY_START + row * 4 + column, offsetX + 50 + 18 * column, offsetY + 18 * row);
			}
		}

		return builder.energySlot(ENERGY_SLOT, 8, 72)
			.syncEnergyValue()
			.addInventory()
			.create(this, syncID);

		//TODO Sync Nbt
	}

	private BlockPos findDrillHead(World world, final BlockPos startPosition) {
		BlockPos lastMiningPipePosition;
		BlockPos nextProbePosition = lastMiningPipePosition = startPosition;

		// Descent until we reached the end of the mining pipe
		while (world.getBlockState(nextProbePosition).isOf(TRContent.MINING_PIPE) || world.getBlockState(nextProbePosition).isOf(TRContent.Machine.MINER.block)) {
			lastMiningPipePosition = nextProbePosition;
			nextProbePosition = nextProbePosition.down();
		}
		return lastMiningPipePosition;
	}

	@Override
	public void tick(World world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity) {
		super.tick(world, pos, state, blockEntity);
		if (world == null || world.isClient) return;

		charge(ENERGY_SLOT);

		boolean nextTick = false;

		while (!nextTick) {
			nextTick = true;
			switch (this.state.getStatus()) {
				case NO_ENERGY -> {
					MinerProcessingState.NoEnergy noEnergy = (MinerProcessingState.NoEnergy) this.state;
					if (this.getEnergy() >= noEnergy.getMissingEnergy()) this.state = new MinerProcessingState.Ready();
				}
				case NO_TOOL -> {
					if (hasMiningTool()) this.state = new MinerProcessingState.Ready();
				}
				case NO_PIPE -> {
					if (hasMiningPipe()) this.state = new MinerProcessingState.Ready();
				}
				case READY -> {
					if (!hasMiningTool()) {
						this.state = new MinerProcessingState.NoTool();
						break;
					}
					if (!hasMiningPipe()) {
						this.state = new MinerProcessingState.NoPipe();
						break;
					}
					this.state = new MinerProcessingState.Probing(this.pos);
					nextTick = false;
				}
				case PROBING -> {
					MinerProcessingState.Probing probingState = (MinerProcessingState.Probing) this.state;

					if (!probingState.hasReachedProbe()) {
						probingState.setHeadPosition(findDrillHead(world, probingState.getHeadPosition()));
						probingState.setProbeReached();
						probingState.setHeadMovementCooldown((int) (55 * (1.0 - getSpeedMultiplier())) + 5);
					}

					if (this.hasProspectingTool() && !probingState.doSkipProspectionNextBlock()) {
						this.state = new MinerProcessingState.Prospecting(probingState.getHeadPosition());
						break;
					}

					BlockPos probedBlockPosition = probingState.getHeadPosition().down();
					BlockState probedBlock = world.getBlockState(probedBlockPosition);

					// TODO Figure out a better criteria for water and other fluids
					if (!probedBlock.isAir()) {
						this.state = new MinerProcessingState.Drilling(probingState.getHeadPosition(), probedBlockPosition);
						break;
					}

					if (probingState.getHeadMovementCooldown() == 0) {
						if (!consumeMiningPipe()){
							this.state = new MinerProcessingState.NoPipe();
							break;
						}
 						moveDrillHead(world, Direction.DOWN, probingState.getHeadPosition());
						probingState.setHeadPosition(probingState.getHeadPosition().down());
						probingState.setSkipProspectionNextBlock(false);
						probingState.setHeadMovementCooldown((int) (55 * (1.0 - getSpeedMultiplier())) + 5);
					} else {
						probingState.tickProbeMoveCooldown();
					}
				}
				case DRILLING -> {
					MinerProcessingState.Drilling drillingState = (MinerProcessingState.Drilling) this.state;

					BlockState drilledBlock = world.getBlockState(drillingState.getDrilledBlockPosition());

					if (!drillingState.isRemainingDrillingTimeSet()) {
						float hardness = drilledBlock.getHardness(world, drillingState.getDrilledBlockPosition());
						if (hardness < 0) {
							this.state = new MinerProcessingState.Retracting(drillingState.getHeadPosition());
							break;
						}

						drillingState.setRemainingDrillingTime((int) ((30 * hardness) / getToolSpeedMultiplier(drilledBlock)));
						log.info("Block: {} ticksToBreak: {}", drilledBlock, drillingState.getRemainingDrillingTime());
					}

					if (drillingState.isDrillingTimeRemaining()) {
						drillingState.tickRemainingDrillingTime();
					} else {
						//TODO Add logic for getting the resources into the inventory
						world.breakBlock(drillingState.getDrilledBlockPosition(), false);
						this.state = new MinerProcessingState.Probing(drillingState.getHeadPosition());
					}
				}
				case PROSPECTING -> {
					MinerProcessingState.Prospecting prospectingState = (MinerProcessingState.Prospecting) this.state;

					if (this.blockProspectionIterator == null){
						this.blockProspectionIterator = new BlockProspectingIterable(prospectingState.getHeadPosition(), DEFAULT_PROSPECTING_RANGE, false).iterator();
					}

					int checksPerTickRemaining = (int) (DEFAULT_PROSPECTING_SPEED * (1.0 + getSpeedMultiplier()));

					while(checksPerTickRemaining > 0){
						if (!this.blockProspectionIterator.hasNext()){
							this.blockProspectionIterator = null;
							this.state = new MinerProcessingState.DrillingOre(prospectingState.getHeadPosition(), prospectingState.getProspectedBlocks());
							break;
						}

						BlockPos prospectedBlockPosition = this.blockProspectionIterator.next();

						BlockState prospectedBlockState = world.getBlockState(prospectedBlockPosition);

						if (checkDesirable(prospectedBlockState)){
							prospectingState.addProspectedBlock(prospectedBlockPosition);
						}
						checksPerTickRemaining--;
					}
				}
				case DRILLING_ORE -> {
					MinerProcessingState.DrillingOre drillingOreState = (MinerProcessingState.DrillingOre) this.state;

					if(!drillingOreState.isRemainingDrillingTimeSet()){
						BlockPos prospectedBlock = drillingOreState.getNextProspectedBlock();

						if (prospectedBlock == null){
							this.state = new MinerProcessingState.Probing(pos.withY(drillingOreState.getHeadPosition().getY()), true);
							break;
						}

						BlockState prospectedBlockState = world.getBlockState(prospectedBlock);

						float hardness = prospectedBlockState.getHardness(world, prospectedBlock);
						if (hardness < 0) {
							this.state = new MinerProcessingState.Retracting(drillingOreState.getHeadPosition());
							break;
						}

						drillingOreState.setRemainingDrillingTime((int) ((30 * hardness) / getToolSpeedMultiplier(prospectedBlockState)));
					}

					// If we still have to drill
					if (drillingOreState.isDrillingTimeRemaining()) {
						drillingOreState.tickRemainingDrillingTime();
					} else {
						BlockPos prospectedBlock = drillingOreState.getNextProspectedBlock();

						world.breakBlock(prospectedBlock, false);
						drillingOreState.completeNextProspectedBlock();
						drillingOreState.setRemainingDrillingTime(-1);
					}

				}
				case RETRACTING -> {
					MinerProcessingState.Retracting retractionState = (MinerProcessingState.Retracting) this.state;
					BlockPos nextPosition = retractionState.getHeadPosition();
					if (world.getBlockState(nextPosition).isOf(TRContent.MINING_PIPE)) {
						world.removeBlock(nextPosition, false);
						//TODO Add mining pipe back to inventory
						retractionState.setHeadPosition(nextPosition.up());
					} else {
						this.state = new MinerProcessingState.Done();
					}
				}
				case DONE -> {
				}
			}
		}

	}

	@Override
	public void onBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState) {
		BlockPos nextPosition = blockPos.down();
		while (world.getBlockState(nextPosition).isOf(TRContent.MINING_PIPE)) {
			world.breakBlock(nextPosition, true);
			nextPosition = nextPosition.down();
		}
	}

	public boolean moveDrillHead(World world, final Direction direction, final BlockPos oldPosition){
		BlockPos nextPosition = oldPosition.offset(direction);

		BlockState oldPositionBlockState = world.getBlockState(oldPosition);
		BlockState nextPositionOldState = world.getBlockState(nextPosition);

		BlockState oldPositionNewState = null;

		BlockState newPositionNewState = null;

		if (nextPositionOldState.isOf(TRContent.MINING_PIPE)) {
			// If we're going against the direction of the pipe
			oldPositionNewState = Blocks.AIR.getDefaultState();
			newPositionNewState = TRContent.MINING_PIPE.getDefaultState().with(MiningPipeBlock.TYPE, MiningPipeBlock.MiningPipeType.DRILL);

		} else if (nextPositionOldState.isOf(Blocks.AIR)) {
			newPositionNewState = TRContent.MINING_PIPE.getDefaultState().with(MiningPipeBlock.TYPE, MiningPipeBlock.MiningPipeType.DRILL);
			oldPositionNewState = TRContent.MINING_PIPE.getDefaultState().with(MiningPipeBlock.TYPE, MiningPipeBlock.MiningPipeType.PIPE);
		} else {
			return false;
		}

		if(oldPositionBlockState.isOf(TRContent.Machine.MINER.block)){
			oldPositionNewState = null;
		}

		if(newPositionNewState != null){
			world.setBlockState(nextPosition, newPositionNewState);
		}

		if(oldPositionNewState != null){
			world.setBlockState(oldPosition, oldPositionNewState);
		}

		return true;
	}



	public boolean checkDesirable(BlockState state){
		return state.isIn(ConventionalBlockTags.ORES);
	}

	public boolean hasMiningTool() {
		ItemStack miningTool = this.inventory.getStack(MINING_TOOL_SLOT);
		return MinerBlockEntity.canUseAsMiningTool(miningTool);
	}

	public boolean hasMiningPipe() {
		for (int slot = 0; slot < MINING_PIPE_INVENTORY_SIZE; slot++){
			ItemStack itemStack = this.inventory.getStack(MINING_PIPE_INVENTORY_START + slot);
			if (itemStack.isOf(TRContent.MINING_PIPE.asItem())){
				return true;
			}
		}
		return false;
	}

	public boolean consumeMiningPipe(){
		for (int slot = 0; slot < MINING_PIPE_INVENTORY_SIZE; slot++){
			ItemStack itemStack = this.inventory.getStack(MINING_PIPE_INVENTORY_START + slot);
			if (itemStack.isOf(TRContent.MINING_PIPE.asItem())){
				itemStack.decrement(1);
				return true;
			}
		}
		return false;
	}

	public boolean hasProspectingTool() {
		ItemStack scanner = this.inventory.getStack(PROSPECTING_TOOL_SLOT);
		return !scanner.isEmpty();
	}

	public float getToolSpeedMultiplier(BlockState blockState) {
		Item toolItem = this.inventory.getStack(MINING_TOOL_SLOT).getItem();
		if (toolItem instanceof RcEnergyItem energyItem) {
			// TODO Investigate for performance
			// FakeItem hack so tool is always charged.
			ItemStack fakeTool = new ItemStack(toolItem);
			energyItem.setStoredEnergy(fakeTool, energyItem.getEnergyCapacity(fakeTool));
			return fakeTool.getMiningSpeedMultiplier(blockState);
		}
		return this.inventory.getStack(MINING_TOOL_SLOT).getMiningSpeedMultiplier(blockState);
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

	static class BlockProspectingIterable implements Iterable<BlockPos> {

		final BlockPos probePosition;
		final int probeRange;
		final boolean clockwise;

		int offsetX;
		int offsetZ;
		int direction;


		public BlockProspectingIterable(BlockPos probePosition, int probeRange, boolean rotateClockwise) {
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

					if (clockwise) {
						offsetZ = -offsetZ;
					} else {
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
