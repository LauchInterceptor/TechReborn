package techreborn.blocks.machine.tier2;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import techreborn.init.TRBlockSettings;
import techreborn.init.TRContent;

import java.util.Objects;

public class MiningPipeBlock extends Block implements Waterloggable {

	public static final EnumProperty<MiningPipeType> TYPE = EnumProperty.of("pipe_type", MiningPipeType.class);

	public MiningPipeBlock(){
		super(TRBlockSettings.miningPipe());
		setDefaultState(getStateManager().getDefaultState().with(TYPE, MiningPipeType.DRILL));
	}

	@Override
	protected VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.empty();
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(TYPE);
	}

	public static MiningPipeType getType(BlockState state) {
		return state.get(TYPE);
	}

	public static void setType(MiningPipeType type, World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(TYPE, type));
	}

	public enum MiningPipeType implements StringIdentifiable {
		PIPE,
		DRILL;

		@Override
		public String asString() {
			switch (this) {
				case PIPE -> {
					return "pipe";
				}
				case DRILL -> {
					return "drill";
				}
			}
			return null;
		}
	}

}
