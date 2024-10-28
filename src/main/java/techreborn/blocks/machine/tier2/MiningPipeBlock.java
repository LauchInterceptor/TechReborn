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
	public static final DirectionProperty FACING = Properties.FACING;

	public MiningPipeBlock(){
		super(TRBlockSettings.miningPipe());
		setDefaultState(getStateManager().getDefaultState().with(TYPE, MiningPipeType.DRILL).with(FACING, Direction.DOWN));
	}

	public static BlockState pipeTo(Direction direction){
		return TRContent.MINING_PIPE.getDefaultState().with(TYPE, MiningPipeType.PIPE).with(FACING, direction);
	}

	public static BlockState drillTo(Direction direction){
		return TRContent.MINING_PIPE.getDefaultState().with(TYPE, MiningPipeType.DRILL).with(FACING, direction);
	}

	public static BlockState junctionTo(Direction direction){
		return TRContent.MINING_PIPE.getDefaultState().with(TYPE, MiningPipeType.JUNCTION).with(FACING, direction);
	}

	@Override
	protected VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
		if (Objects.requireNonNull(state.get(TYPE)) == MiningPipeType.JUNCTION) {
			return VoxelShapes.fullCube();
		}
		return VoxelShapes.empty();
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!player.getAbilities().allowModifyWorld) {
			return ActionResult.PASS;
		}
		if(!player.isSneaking()){
			Direction facing = state.get(FACING);
			switch (facing){
				case DOWN -> {
					world.setBlockState(pos, state.with(FACING, Direction.UP));
				}
				case UP -> {
					world.setBlockState(pos, state.with(FACING, Direction.NORTH));
				}
				case NORTH -> {
					world.setBlockState(pos, state.with(FACING, Direction.SOUTH));
				}
				case SOUTH -> {
					world.setBlockState(pos, state.with(FACING, Direction.WEST));
				}
				case WEST -> {
					world.setBlockState(pos, state.with(FACING, Direction.EAST));
				}
				case EAST -> {
					world.setBlockState(pos, state.with(FACING, Direction.DOWN));
				}
			}
			System.out.println("Changed facing to" + state.get(FACING));
		}else{
			MiningPipeType type = state.get(TYPE);
			switch (type){
				case PIPE -> {
					world.setBlockState(pos, state.with(TYPE, MiningPipeType.JUNCTION));
				}
				case JUNCTION -> {
					world.setBlockState(pos, state.with(TYPE, MiningPipeType.DRILL));
				}
				case DRILL -> {
					world.setBlockState(pos, state.with(TYPE, MiningPipeType.PIPE));
				}
			}
			System.out.println("Changed type to" + state.get(TYPE));
		}
		return ActionResult.SUCCESS;

	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING).add(TYPE);
	}

	public static Direction getFacing(BlockState state) {
		return state.get(FACING);
	}

	public static void setFacing(Direction facing, World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(FACING, facing));
	}

	public static MiningPipeType getType(BlockState state) {
		return state.get(TYPE);
	}

	public static void setType(MiningPipeType type, World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(TYPE, type));
	}

	public enum MiningPipeType implements StringIdentifiable {
		PIPE,
		JUNCTION,
		DRILL;

		@Override
		public String asString() {
			switch (this) {
				case PIPE -> {
					return "pipe";
				}
				case JUNCTION -> {
					return "junction";
				}
				case DRILL -> {
					return "drill";
				}
			}
			return null;
		}
	}

}
