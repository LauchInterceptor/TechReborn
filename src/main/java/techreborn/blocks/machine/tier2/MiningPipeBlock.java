package techreborn.blocks.machine.tier2;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import techreborn.init.TRBlockSettings;
import techreborn.init.TRContent;

public class MiningPipeBlock extends BlockWithEntity implements Waterloggable {

	public MiningPipeBlock(){
		super(TRBlockSettings.miningPipe());
	}
	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		throw new IllegalStateException("MiningPipeBlock does not support getCodec!");
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return null;
	}
}
