package techreborn.blockentity.machine.tier2.miner;


import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class MinerProcessingState{
	public final MinerProcessingStatus status;

	protected MinerProcessingState(MinerProcessingStatus status) {
		this.status = status;
	}

	public MinerProcessingStatus getStatus(){
		return this.status;
	}

	public static class NoEnergy extends MinerProcessingState {
		public final long missingEnergy;
		public NoEnergy(long missingEnergy) {
			super(MinerProcessingStatus.NO_ENERGY);
			this.missingEnergy = missingEnergy;
		}

		public long getMissingEnergy() {
			return missingEnergy;
		}
	}

	public static class NoTool extends MinerProcessingState {
		public NoTool() {
			super(MinerProcessingStatus.NO_TOOL);
		}
	}

	public static class NoPipe extends MinerProcessingState {
		public NoPipe() {
			super(MinerProcessingStatus.NO_PIPE);
		}
	}

	public static class Ready extends MinerProcessingState {
		public Ready() {
			super(MinerProcessingStatus.READY);
		}
	}

	public static class Probing extends MinerProcessingState {
		BlockPos headPosition;
		boolean reachedHead;
		boolean skipProspectionNextBlock;
		int headMovementCooldown;
		public Probing(BlockPos headPosition) {
			super(MinerProcessingStatus.PROBING);
			this.headPosition = headPosition;
			this.reachedHead = false;
			this.skipProspectionNextBlock = false;
			this.headMovementCooldown = 0;
		}

		public Probing(BlockPos headPosition, boolean skipProspectionNextBlock) {
			super(MinerProcessingStatus.PROBING);
			this.headPosition = headPosition;
			this.reachedHead = false;
			this.skipProspectionNextBlock = skipProspectionNextBlock;
			this.headMovementCooldown = 0;
		}

		public void setProbeReached(){
			this.reachedHead = true;
		}

		public boolean hasReachedProbe(){
			return this.reachedHead;
		}

		public boolean doSkipProspectionNextBlock() {
			return skipProspectionNextBlock;
		}

		public void setSkipProspectionNextBlock(boolean skipProspectionNextBlock) {
			this.skipProspectionNextBlock = skipProspectionNextBlock;
		}

		public void setHeadPosition(BlockPos headPosition) {
			this.headPosition = headPosition;
		}

		public BlockPos getHeadPosition() {
			return headPosition;
		}

		public void setHeadMovementCooldown(int headMovementCooldown) {
			this.headMovementCooldown = headMovementCooldown;
		}

		public int getHeadMovementCooldown() {
			return headMovementCooldown;
		}

		public void tickProbeMoveCooldown(){
			this.headMovementCooldown--;
		}
	}

	public static class Drilling extends MinerProcessingState {
		BlockPos headPosition;
		BlockPos drilledBlockPosition;
		int remainingDrillingTime;
		public Drilling(BlockPos headPosition, BlockPos drilledBlockPosition) {
			super(MinerProcessingStatus.DRILLING);
			this.headPosition = headPosition;
			this.drilledBlockPosition = drilledBlockPosition;
			remainingDrillingTime = -1;
		}

		public void setHeadPosition(BlockPos headPosition) {
			this.headPosition = headPosition;
		}

		public BlockPos getHeadPosition() {
			return headPosition;
		}

		public void setDrilledBlockPosition(BlockPos drilledBlockPosition) {
			this.drilledBlockPosition = drilledBlockPosition;
		}

		public BlockPos getDrilledBlockPosition() {
			return drilledBlockPosition;
		}

		public boolean isRemainingDrillingTimeSet(){
			return this.remainingDrillingTime >= 0;
		}

		public boolean isDrillingTimeRemaining(){
			return this.remainingDrillingTime > 0;
		}

		public void tickRemainingDrillingTime(){
			this.remainingDrillingTime--;
		}

		public void setRemainingDrillingTime(int remainingDrillingTime) {
			this.remainingDrillingTime = remainingDrillingTime;
		}

		public int getRemainingDrillingTime() {
			return remainingDrillingTime;
		}
	}

	public static class Prospecting extends MinerProcessingState {
		BlockPos headPosition;
		List<BlockPos> prospectedBlocks;
		public Prospecting(BlockPos headPosition) {
			super(MinerProcessingStatus.PROSPECTING);
			this.headPosition = headPosition;
			this.prospectedBlocks = new ArrayList<>();
		}

		public void addProspectedBlock(BlockPos prospectedBlock){
			this.prospectedBlocks.add(prospectedBlock);
		}

		public List<BlockPos> getProspectedBlocks(){
			return prospectedBlocks;
		}

		public BlockPos getHeadPosition() {
			return headPosition;
		}

		public void setHeadPosition(BlockPos headPosition) {
			this.headPosition = headPosition;
		}
	}

	public static class DrillingOre extends MinerProcessingState {
		BlockPos headPosition;
		List<BlockPos> prospectedBlocks;
		int remainingDrillingTime;

		public DrillingOre(BlockPos headPosition, List<BlockPos> prospectedBlocks) {
			super(MinerProcessingStatus.DRILLING_ORE);
			this.headPosition = headPosition;
			this.prospectedBlocks = prospectedBlocks;
			this.remainingDrillingTime = -1;
		}

		public BlockPos getNextProspectedBlock(){
			if (this.prospectedBlocks.isEmpty()){
				return null;
			}
			return this.prospectedBlocks.getFirst();
		}

		public void completeNextProspectedBlock(){
			this.prospectedBlocks.removeFirst();
		}

		public BlockPos getHeadPosition() {
			return headPosition;
		}

		public void setHeadPosition(BlockPos headPosition) {
			this.headPosition = headPosition;
		}

		public boolean isRemainingDrillingTimeSet(){
			return this.remainingDrillingTime >= 0;
		}

		public boolean isDrillingTimeRemaining(){
			return this.remainingDrillingTime > 0;
		}

		public void tickRemainingDrillingTime(){
			this.remainingDrillingTime--;
		}

		public void setRemainingDrillingTime(int remainingDrillingTime) {
			this.remainingDrillingTime = remainingDrillingTime;
		}

		public int getRemainingDrillingTime() {
			return remainingDrillingTime;
		}
	}

	public static class Retracting extends MinerProcessingState {
		BlockPos headPosition;
	    public Retracting(BlockPos headPosition) {
	        super(MinerProcessingStatus.RETRACTING);
			this.headPosition = headPosition;
	    }

		public BlockPos getHeadPosition() {
			return headPosition;
		}

		public void setHeadPosition(BlockPos headPosition) {
			this.headPosition = headPosition;
		}
	}

	public static class Done extends MinerProcessingState {
		public Done() {
			super(MinerProcessingStatus.DONE);
		}
	}


}
