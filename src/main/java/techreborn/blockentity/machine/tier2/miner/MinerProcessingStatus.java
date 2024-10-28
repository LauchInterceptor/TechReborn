package techreborn.blockentity.machine.tier2.miner;

import net.minecraft.text.Text;

public enum MinerProcessingStatus {
	NO_ENERGY(Text.translatable("gui.techreborn.block.miner.no_energy")),
	NO_TOOL(Text.translatable("gui.techreborn.block.miner.no_tool")),
	NO_PIPE(Text.translatable("gui.techreborn.block.miner.no_pipe")),
	READY(Text.translatable("gui.techreborn.block.miner.ready")),
	PROBING(Text.translatable("gui.techreborn.block.miner.probing")),
	DRILLING(Text.translatable("gui.techreborn.block.miner.drilling")),
	PROSPECTING(Text.translatable("gui.techreborn.block.miner.prospecting")),
	DRILLING_ORE(Text.translatable("gui.techreborn.block.miner.drilling_ore")),
	RETRACTING(Text.translatable("gui.techreborn.block.miner.retracting")),
	DONE(Text.translatable("gui.techreborn.block.miner.done"));

	private final Text displayText;

	MinerProcessingStatus(Text displayText){
		this.displayText = displayText;
	}

	public Text getDisplayText() {
		return displayText;
	}
}
