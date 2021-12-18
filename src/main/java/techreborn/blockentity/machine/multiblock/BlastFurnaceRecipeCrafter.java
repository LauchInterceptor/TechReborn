package techreborn.blockentity.machine.multiblock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import reborncore.common.crafting.RebornRecipe;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.util.RebornInventory;
import techreborn.init.ModRecipes;

import java.util.ArrayList;
import java.util.List;

public class BlastFurnaceRecipeCrafter extends RecipeCrafter {

	private int ticksSinceLastChange;

	public BlastFurnaceRecipeCrafter(BlockEntity blockEntity, int inputs, int outputs, RebornInventory<?> inventory,
									 int[] inputSlots, int[] outputSlots){
		super(ModRecipes.BLAST_FURNACE,blockEntity,inputs, outputs, inventory, inputSlots, outputSlots);

	}

	private List<ItemStack> getCurrentInputs(){
		List<ItemStack> inputStacks = new ArrayList<>();
		for (int inputSlot : inputSlots) {
			inputStacks.add(inventory.getStack(inputSlot));
		}
		return inputStacks;
	}

	private List<ItemStack> getPartialOutput(RebornRecipe recipe, List<ItemStack> inputs){
		for(ItemStack input : inputs){
			if(input.isDamageable()){
				float durabilityPercentage = 1 - ((float)input.getDamage()) / input.getMaxDamage();
				return recipe.getOutputs().stream().map(itemStack -> {
					ItemStack copied = itemStack.copy();
					if (copied.getCount() > 1) {
						copied.setCount((int) Math.floor(copied.getCount() * durabilityPercentage));
					}
					return copied;
				}).toList();
			}
		}
		return recipe.getOutputs();
	}

	@Override
	public void updateEntity() {
		if (blockEntity.getWorld() == null || blockEntity.getWorld().isClient) {
			return;
		}
		ticksSinceLastChange++;
		// Force a has chanced every second
		if (ticksSinceLastChange == 20) {
			setInvDirty(true);
			ticksSinceLastChange = 0;
			setIsActive();
		}
		// It will now look for new recipes.
		if (currentRecipe == null && isInvDirty()) {
			updateCurrentRecipe();
		}
		if (currentRecipe != null) {
			// If it doesn't have all the inputs reset
			if (isInvDirty() && !hasAllInputs()) {
				currentRecipe = null;
				currentTickTime = 0;
				setIsActive();
			}
			// If it has reached the recipe tick time
			if (currentRecipe != null && currentTickTime >= currentNeededTicks && hasAllInputs()) {
				boolean canGiveInvAll = true;
				List<ItemStack> recipeOutput = getPartialOutput(currentRecipe, getCurrentInputs());
				// Checks to see if it can fit the output
				for (int i = 0; i < recipeOutput.size(); i++) {
					if (!canFitOutput(recipeOutput.get(i), outputSlots[i])) {
						canGiveInvAll = false;
					}
				}
				// The slots that have been filled
				ArrayList<Integer> filledSlots = new ArrayList<>();
				if (canGiveInvAll && currentRecipe.onCraft(blockEntity)) {
					for (int i = 0; i < recipeOutput.size(); i++) {
						// Checks it has not been filled
						if (!filledSlots.contains(outputSlots[i])) {
							// Fills the slot with the output stack
							fitStack(recipeOutput.get(i).copy(), outputSlots[i]);
							filledSlots.add(outputSlots[i]);
						}
					}
					// This uses all the inputs
					useAllInputs();
					// Reset
					currentRecipe = null;
					currentTickTime = 0;
					updateCurrentRecipe();
					//Update active sate if the blockEntity isnt going to start crafting again
					if (currentRecipe == null) {
						setIsActive();
					}
				}
			} else if (currentRecipe != null && currentTickTime < currentNeededTicks) {
				long useRequirement = getEuPerTick(currentRecipe.getPower());
				if (energy.tryUseExact(useRequirement)) {
					currentTickTime++;
					if ((currentTickTime == 1 || currentTickTime % 20 == 0) && soundHanlder != null) {
						soundHanlder.playSound(false, blockEntity);
					}
				}
			}
		}
		setInvDirty(false);
	}
}
