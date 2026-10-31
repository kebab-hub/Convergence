package io.github.kebab_hub.subspace.data;

import io.github.kebab_hub.subspace.common.registry.ModItemTags;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public final class SubspaceRecipeProvider extends RecipeProvider {
    public SubspaceRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder.shapeless(ModRegistries.PHASE_CRYSTAL.get())
                .requires(Ingredient.of(Tags.Items.GEMS_AMETHYST), 2)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has_amethyst", has(Tags.Items.GEMS_AMETHYST))
                .save(output);

        ShapedRecipeBuilder.shaped(ModRegistries.STABILIZED_PHASE_CRYSTAL.get())
                .pattern(" I ").pattern("CPC").pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('P', ModItemTags.PHASE_CRYSTALS)
                .unlockedBy("has_phase_crystal", has(ModItemTags.PHASE_CRYSTALS))
                .save(output);

        ShapedRecipeBuilder.shaped(ModRegistries.RESONANCE_COIL.get())
                .pattern(" C ").pattern("CSC").pattern(" C ")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('S', ModItemTags.STABILIZED_PHASE_CRYSTALS)
                .unlockedBy("has_stabilized_crystal", has(ModItemTags.STABILIZED_PHASE_CRYSTALS))
                .save(output);

        // Three metal ingots become three alloy ingots; the crystal is consumed as the additive.
        ShapelessRecipeBuilder.shapeless(ModRegistries.PHASE_ALLOY_INGOT.get(), 3)
                .requires(Ingredient.of(Tags.Items.INGOTS_IRON), 2)
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(ModItemTags.PHASE_CRYSTALS)
                .unlockedBy("has_phase_crystal", has(ModItemTags.PHASE_CRYSTALS))
                .save(output);

        ShapedRecipeBuilder.shaped(ModRegistries.PHASE_ALLOY_PLATE.get())
                .pattern("I").pattern("I")
                .define('I', ModItemTags.PHASE_ALLOY_INGOTS)
                .unlockedBy("has_phase_alloy", has(ModItemTags.PHASE_ALLOY_INGOTS))
                .save(output);
    }
}
