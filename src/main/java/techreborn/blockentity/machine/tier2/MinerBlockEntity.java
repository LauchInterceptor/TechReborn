package techreborn.blockentity.machine.tier2;

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

	private static final Logger log = LoggerFactory.getLogger(MinerBlockEntity.class);

	private MinerProcessingState state;


	public MinerBlockEntity(BlockPos pos, BlockState state) {
		super(TRBlockEntities.MINER, pos, state, "Miner", 128, 10_000, TRContent.Machine.MINER.block, 0);
		// Inventory
		this.inventory = new RebornInventory<>(5, "MinerBlockEntity", 64, this);
		this.state = new MinerProcessingState.Ready();
	}

	public MinerProcessingState getState() {
		return state;
	}

	public static boolean canUseAsMiningTool(ItemStack itemStack) {
		return itemStack.isIn(TRContent.ItemTags.MINER_ACCEPTED_TOOLS);
	}

	public static boolean canUseAsProspectingTool(ItemStack itemStack) {
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
			.energySlot(ENERGY_SLOT, 8, 72)
			.syncEnergyValue()
			.addInventory()
			.create(this, syncID);

		//TODO Sync Nbt
	}

	private BlockPos findProbe(World world, final BlockPos startPosition) {
		BlockPos lastMiningPipePosition;
		BlockPos nextProbePosition = lastMiningPipePosition = startPosition;

		// Descent until we reached the end of the mining pipe
		while (world.getBlockState(nextProbePosition).isOf(TRContent.MINING_PIPE) || world.getBlockState(nextProbePosition).isOf(TRContent.Machine.MINER.block)) {
			lastMiningPipePosition = nextProbePosition;
			nextProbePosition = nextProbePosition.down();
		}
		return lastMiningPipePosition;
	}

	private BlockPos findDrillHead(World world, final BlockPos startPosition) {
		BlockPos drillPosition;
		BlockPos nextProbePosition = drillPosition = startPosition;
		BlockState probedBlockState;

		probeLoop:
		while ((probedBlockState = world.getBlockState(nextProbePosition)).isOf(TRContent.MINING_PIPE)) {
			drillPosition = nextProbePosition;
			switch (probedBlockState.get(MiningPipeBlock.TYPE)) {
				case PIPE, JUNCTION -> {
					Direction pipeDirection = probedBlockState.get(MiningPipeBlock.FACING);
					nextProbePosition = nextProbePosition.offset(pipeDirection);
				}
				case DRILL -> {
					break probeLoop;
				}
			}
		}
		return drillPosition;
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
						probingState.setHeadPosition(findProbe(world, probingState.getHeadPosition()));
						probingState.setProbeReached();
					}

					if (this.hasProspectingTool()) {
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

					if (probingState.getHeadMovementCooldown() <= 0) {
						if (probingState.getHeadPosition() != this.pos) {
							this.replaceMiningPipe(world, probingState.getHeadPosition(), MiningPipeBlock.MiningPipeType.PIPE, Direction.DOWN);
						}
						probingState.setHeadPosition(probedBlockPosition);
						this.placeMiningPipe(world, probingState.getHeadPosition(), MiningPipeBlock.MiningPipeType.DRILL, Direction.DOWN);
						probingState.setHeadMovementCooldown((int) (55 * (1.0 - getSpeedMultiplier())) + 5);
					} else {
						probingState.tickProbeMoveCooldown();
					}
				}
				case DRILLING -> {
					MinerProcessingState.Drilling drillingState = (MinerProcessingState.Drilling) this.state;

					BlockState drilledBlock = world.getBlockState(drillingState.getDrilledBlockPosition());

					// If no drilling time was set
					if (!drillingState.isRemainingDrillingTimeSet()) {
						float hardness = drilledBlock.getHardness(world, drillingState.getDrilledBlockPosition());
						if (hardness < 0) {
							this.state = new MinerProcessingState.Retracting(drillingState.getHeadPosition());
							break;
						}

						drillingState.setRemainingDrillingTime((int) ((30 * hardness) / getToolSpeedMultiplier(drilledBlock)));
						log.info("Block: {} ticksToBreak: {}", drilledBlock, drillingState.getRemainingDrillingTime());
					}

					// If we still have to drill
					if (drillingState.isDrillingTimeRemaining()) {
						drillingState.tickRemainingDrillingTime();
					} else {
						//TODO Add logic for getting the resources into the inventory
						world.breakBlock(drillingState.getDrilledBlockPosition(), false);
						this.state = new MinerProcessingState.Probing(drillingState.getHeadPosition());
					}
				}
				case PROSPECTING -> {
					//TODO
				}
				case DRILLING_ORE -> {
					//TODO
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

		if (this.prospectedTargetBlock == null){
			this
		}
	}

	public Iterator<BlockPos> setupProspectionIterator(final BlockPos probePosition, final int probeRange) {
		return new BlockProspectingIterable(probePosition, probeRange, true).iterator();
	}

	public boolean placeMiningPipe(World world, BlockPos pos, MiningPipeBlock.MiningPipeType type, Direction direction) {
		ItemStack miningPipeStack = this.inventory.getStack(MINING_PIPE_SLOT);
		if (!miningPipeStack.isEmpty()) {
			world.setBlockState(pos, TRContent.MINING_PIPE.getDefaultState().with(MiningPipeBlock.TYPE, type).with(MiningPipeBlock.FACING, direction));
			miningPipeStack.decrement(1);
			return true;
		}
		return false;
	}

	public boolean replaceMiningPipe(World world, BlockPos pos, MiningPipeBlock.MiningPipeType type, Direction direction) {
		world.setBlockState(pos, TRContent.MINING_PIPE.getDefaultState().with(MiningPipeBlock.TYPE, type).with(MiningPipeBlock.FACING, direction));
		return false;
	}


	public boolean miningNecessitiesFulfilled() {
		return hasMiningTool() && hasMiningPipe();
	}

	public boolean hasMiningTool() {
		ItemStack miningTool = this.inventory.getStack(MINING_TOOL_SLOT);
		// TODO See if I need to double check the mining tool being an actual mining tool or if the filtered slot suffices.
		return MinerBlockEntity.canUseAsMiningTool(miningTool);
	}

	public boolean hasMiningPipe() {
		ItemStack miningPipe = this.inventory.getStack(MINING_PIPE_SLOT);
		return miningPipe.isOf(TRContent.MINING_PIPE.asItem());
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
