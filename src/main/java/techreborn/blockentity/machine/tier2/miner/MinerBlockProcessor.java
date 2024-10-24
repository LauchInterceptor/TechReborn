package techreborn.blockentity.machine.tier2.miner;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import reborncore.common.screen.builder.BlockEntityScreenHandlerBuilder;
import techreborn.blockentity.machine.tier0.block.BlockProcessable;
import techreborn.blockentity.machine.tier0.block.BlockProcessor;
import techreborn.blockentity.machine.tier0.block.ProcessingStatus;

import java.util.UUID;

public class MinerBlockProcessor implements BlockProcessor {

	private final UUID processorId = UUID.randomUUID();

	private final BlockProcessable processable;

	private final int outputSlot;

	private final int baseBreakTime;

	private final int baseCostToBreak;

	public MinerBlockProcessor(BlockProcessable processable, int outputSlot, int baseBreakTime, int baseCostToBreak) {
		this.processable = processable;

		this.outputSlot = outputSlot;

		this.baseBreakTime = baseBreakTime;
		this.baseCostToBreak = baseCostToBreak;
	}

	@Override
	public ProcessingStatus onTick(World world, BlockPos positionInFront) {
		return null;
	}

	@Override
	public ProcessingStatus getStatusEnum() {
		return null;
	}

	@Override
	public int getCurrentTickTime() {
		return 0;
	}

	@Override
	public int getTickTime() {
		return 0;
	}

	@Override
	public void readNbt(NbtCompound tag) {

	}

	@Override
	public void writeNbt(NbtCompound tag) {

	}

	@Override
	public BlockEntityScreenHandlerBuilder syncNbt(BlockEntityScreenHandlerBuilder builder) {
		return null;
	}
}
