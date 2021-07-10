package com.gm910.aigodmod.god.neural;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.gm910.aigodmod.main.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class converts a structure into a tensor. The tensor will have
 * dimensions as follows - Width - Height - Depth - Block state index
 * 
 * @author borah
 *
 */
public class StructureDataNDArray {

	private int[][][][] dataArray;

	/**
	 * The number of block types in the game to train on
	 */
	private static Integer numBlocks;
	private static List<ResourceLocation> orderedBlocks;

	private static List<BlockState> universalOrderedBlockStates;
	/**
	 * This Map is used so that every possible block state a block could have has a
	 * standard sorted order, so that indices can be used to refer to block states.
	 */
	public static Map<ResourceLocation, List<BlockState>> blockStateMap = new HashMap<>();
	private boolean isInitialized = false;
	/**
	 * The number of block states possible for the single block with the most
	 * different block states
	 */
	private static Integer maxBlockStates;

	private static int[] dimensions;

	public static int BATCH_SIZE;

	private static int[] tensorDimensions;

	public StructureDataNDArray() {
		initBlockList();
	}

	/**
	 * Whether this object has been initialized using a block region
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * Sets up the N-dimensional array with the blocks in the region. All
	 * incompatible modded blocks are replaced with stone!
	 * 
	 * @param world
	 * @param region
	 */
	public void init(World world, AxisAlignedBB region) {

		dataArray = new int[(int) region.getXsize()][(int) region.getYsize()][(int) region
				.getZsize()][universalOrderedBlockStates.size()];

		for (int x = (int) region.minX; x <= (int) region.maxX; x++) {
			for (int y = (int) region.minY; y <= (int) region.maxY; y++) {
				for (int z = (int) region.minZ; z <= (int) region.maxZ; z++) {

					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos);
					Block block = state.getBlock();
					ResourceLocation regName = state.getBlock().getRegistryName();
					if (!isCompatibleMod(regName)) {
						state = Blocks.STONE.defaultBlockState();
						regName = state.getBlock().getRegistryName();
					}
					dataArray[x][y][z][getUniversalIndexOfBlockState(state)] = 1;
				}
			}
		}
		isInitialized = true;
	}

	/**
	 * initializes data array from nbt file
	 * 
	 * @param dat
	 */
	public void init(CompoundNBT dat) {
		ListNBT size = (ListNBT) dat.get("size");
		dataArray = new int[size.getInt(0)][size.getInt(1)][size.getInt(2)][universalOrderedBlockStates.size()];
		List<BlockState> palette = new ArrayList<>();
		ListNBT paletteNBT = (ListNBT) dat.get("palette");
		for (INBT inbt : paletteNBT) {
			CompoundNBT tag = (CompoundNBT) inbt;

		}
	}

	public int[][][][] getDataArray() {
		if (!isInitialized)
			throw new IllegalStateException("Cannot access data array before initialized");
		return dataArray;
	}

	/**
	 * REturns the array index of this block
	 * 
	 * @param block
	 * @return
	 */
	public int getIndexOfBlock(Block block) {
		return getOrderedBlocks().indexOf(block.getRegistryName());
	}

	/**
	 * Returns the local (relative to the block) index of this block state
	 * 
	 * @param state
	 * @return
	 */
	public static int getIndexOfBlockState(BlockState state) {
		return getBlockStateMap().get(state.getBlock().getRegistryName()).indexOf(state);
	}

	/**
	 * Returns the actual index in the universal list of this block state
	 */
	public static int getUniversalIndexOfBlockState(BlockState state) {
		if (universalOrderedBlockStates == null || universalOrderedBlockStates.isEmpty())
			initBlockList();
		return universalOrderedBlockStates.indexOf(state);
	}

	/**
	 * Helper method to check the current Minecraft
	 */
	public static void initBlockList() {
		if (numBlocks != null)
			return;
		orderedBlocks = new ArrayList<>(ForgeRegistries.BLOCKS.getKeys());
		orderedBlocks.removeIf(((Predicate<ResourceLocation>) (StructureDataNDArray::isCompatibleMod)).negate());
		orderedBlocks.sort(Comparator.naturalOrder());
		numBlocks = orderedBlocks.size();

		universalOrderedBlockStates = new ArrayList<>();

		for (ResourceLocation key : orderedBlocks) {
			Block block = ForgeRegistries.BLOCKS.getValue(key);
			List<BlockState> stateList = new ArrayList<>(block.getStateDefinition().getPossibleStates());
			stateList.sort((c1, c2) -> NBTUtil.writeBlockState(c1).getAsString()
					.compareTo(NBTUtil.writeBlockState(c2).getAsString()));
			blockStateMap.put(key, stateList);
			universalOrderedBlockStates.addAll(stateList);
			if (stateList.size() > maxBlockStates)
				maxBlockStates = stateList.size();
		}

		tensorDimensions = new int[] { dimensions[0], dimensions[1], dimensions[2],
				universalOrderedBlockStates.size() };
	}

	/**
	 * Whether the mod given by this resource location is compatible with the Neural
	 * Network of the AI
	 * 
	 * @param rl
	 * @return
	 */
	public static boolean isCompatibleMod(ResourceLocation rl) {
		return (rl.getNamespace().equals("minecraft") || !rl.getNamespace().equals(Reference.MODID));
	}

	public static int[] getDimensions() {
		return dimensions;
	}

	public static int[] getTensorDimensions() {
		if (tensorDimensions == null)
			initBlockList();
		return tensorDimensions;
	}

	public static int getNumBlocks() {
		if (numBlocks == null)
			initBlockList();
		return numBlocks;
	}

	public static int getMaxBlockStates() {
		if (maxBlockStates == null)
			initBlockList();
		return maxBlockStates;
	}

	public static Map<ResourceLocation, List<BlockState>> getBlockStateMap() {
		if (blockStateMap == null || blockStateMap.isEmpty())
			initBlockList();
		return blockStateMap;
	}

	public static List<ResourceLocation> getOrderedBlocks() {
		if (getOrderedBlocks() == null || getOrderedBlocks().isEmpty())
			initBlockList();
		return orderedBlocks;
	}

	/**
	 * TODO generalize this to work with a jar file Reads from the hardcoded
	 * settings file to initialize necessary values
	 */
	public static void initValuesFromSettings() {
		Path settings = Paths.get("config/hardcodedaisettings.txt");

		try (BufferedReader reader = Files.newBufferedReader(settings)) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				try {
					String[] split = line.split("=");

					if (split.length != 2)
						throw new IllegalStateException(
								"Invalid file format for \"" + line + "\" in hardcodedsettings file");
					String key = split[0];
					String value = split[1];

					switch (key) {
					case "dimensions": {
						String[] intsS = value.split(",");
						if (intsS.length != 3)
							throw new IllegalStateException("Invalid dimensions format " + Arrays.toString(intsS));

						dimensions = Arrays.stream(intsS).mapToInt(Integer::parseInt).toArray();
					}
						break;

					case "batch_size": {
						BATCH_SIZE = Integer.parseInt(value);
					}
						break;
					}

				} catch (Exception e) {
					throw new ReportedException(CrashReport.forThrowable(e, "error while reading line " + line));
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO generalize this to work with jar file Writes to output settings file to
	 * initialize necessary values for python script
	 */
	public static void initOutputFile() {

	}

}
