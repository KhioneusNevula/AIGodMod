package com.gm910.aigodmod.god.neural;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.gm910.aigodmod.main.Reference;
import com.gm910.aigodmod.util.GMUtils;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * This class converts a structure into a tensor. The tensor will have
 * dimensions as follows - Width - Height - Depth - DyeColor index or all 0 for
 * Air
 * 
 * @author borah
 *
 */
public class StructureDataNDArray {

	private byte[][][][] dataArray;

	/**
	 * Size of the physical structures
	 */
	private static int[] spatialDimensions;

	public static int BATCH_SIZE;

	public static final Map<MaterialColor, Block> MATERIAL_TO_BLOCK = Util.make(Maps.newHashMap(), (map) -> {

		map.put(null, Blocks.AIR);

		map.put(MaterialColor.NONE, Blocks.WHITE_STAINED_GLASS);
		map.put(MaterialColor.CLAY, Blocks.CLAY);
		map.put(MaterialColor.COLOR_BLACK, Blocks.BLACK_WOOL);
		map.put(MaterialColor.COLOR_BLUE, Blocks.BLUE_WOOL);
		map.put(MaterialColor.COLOR_BROWN, Blocks.BROWN_WOOL);
		map.put(MaterialColor.COLOR_CYAN, Blocks.CYAN_WOOL);
		map.put(MaterialColor.COLOR_GRAY, Blocks.GRAY_WOOL);
		map.put(MaterialColor.COLOR_GREEN, Blocks.GREEN_WOOL);
		map.put(MaterialColor.COLOR_LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		map.put(MaterialColor.COLOR_LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		map.put(MaterialColor.COLOR_LIGHT_GREEN, Blocks.LIME_WOOL);
		map.put(MaterialColor.COLOR_MAGENTA, Blocks.MAGENTA_WOOL);
		map.put(MaterialColor.COLOR_ORANGE, Blocks.ORANGE_WOOL);
		map.put(MaterialColor.COLOR_PINK, Blocks.PINK_WOOL);
		map.put(MaterialColor.COLOR_PURPLE, Blocks.PURPLE_WOOL);
		map.put(MaterialColor.COLOR_RED, Blocks.RED_WOOL);
		map.put(MaterialColor.COLOR_YELLOW, Blocks.YELLOW_WOOL);
		map.put(MaterialColor.CRIMSON_HYPHAE, Blocks.RED_TERRACOTTA);
		map.put(MaterialColor.CRIMSON_NYLIUM, Blocks.RED_WOOL);
		map.put(MaterialColor.CRIMSON_STEM, Blocks.MAGENTA_TERRACOTTA);
		map.put(MaterialColor.DIAMOND, Blocks.DIAMOND_BLOCK);
		map.put(MaterialColor.DIRT, Blocks.DIRT);
		map.put(MaterialColor.EMERALD, Blocks.EMERALD_BLOCK);
		map.put(MaterialColor.FIRE, Blocks.RED_WOOL);
		map.put(MaterialColor.GOLD, Blocks.GOLD_BLOCK);
		map.put(MaterialColor.GRASS, Blocks.GREEN_TERRACOTTA);
		map.put(MaterialColor.ICE, Blocks.PACKED_ICE);
		map.put(MaterialColor.LAPIS, Blocks.LAPIS_BLOCK);
		map.put(MaterialColor.METAL, Blocks.LIGHT_GRAY_WOOL);
		map.put(MaterialColor.NETHER, Blocks.RED_TERRACOTTA);
		map.put(MaterialColor.PLANT, Blocks.OAK_LEAVES);
		map.put(MaterialColor.PODZOL, Blocks.ORANGE_TERRACOTTA);
		map.put(MaterialColor.QUARTZ, Blocks.WHITE_WOOL);
		map.put(MaterialColor.SAND, Blocks.WHITE_TERRACOTTA);
		map.put(MaterialColor.SNOW, Blocks.WHITE_WOOL);
		map.put(MaterialColor.STONE, Blocks.STONE);
		map.put(MaterialColor.TERRACOTTA_WHITE, Blocks.WHITE_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_BLACK, Blocks.BLACK_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_BLUE, Blocks.BLUE_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_BROWN, Blocks.BROWN_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_CYAN, Blocks.CYAN_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_GRAY, Blocks.GRAY_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_GREEN, Blocks.GREEN_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_LIGHT_GREEN, Blocks.LIME_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_MAGENTA, Blocks.MAGENTA_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_ORANGE, Blocks.ORANGE_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_PINK, Blocks.PINK_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_PURPLE, Blocks.PURPLE_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_RED, Blocks.RED_TERRACOTTA);
		map.put(MaterialColor.TERRACOTTA_YELLOW, Blocks.YELLOW_TERRACOTTA);
		map.put(MaterialColor.WARPED_HYPHAE, Blocks.PURPLE_TERRACOTTA);
		map.put(MaterialColor.WARPED_NYLIUM, Blocks.CYAN_CONCRETE);
		map.put(MaterialColor.WARPED_STEM, Blocks.CYAN_CONCRETE);
		map.put(MaterialColor.WARPED_WART_BLOCK, Blocks.WARPED_WART_BLOCK);
		map.put(MaterialColor.WATER, Blocks.WATER);
		map.put(MaterialColor.WOOD, Blocks.BROWN_CONCRETE);
		map.put(MaterialColor.WOOL, Blocks.WHITE_WOOL);

	});

	/**
	 * House files to train from
	 */
	public static String[] baseGameFilesToScan;

	/**
	 * Dimensions of the tensor used to abstract the structures
	 */
	private static int[] tensorDimensions;

	public static final Path trainingDatasetFile = Path.of("C:/Users/borah/Desktop/trainingdataset.txt");

	public StructureDataNDArray() {
		initBlockStateList();
	}

	/**
	 * Whether this object has been initialized using a block region
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return dataArray != null;
	}

	/**
	 * Assumption: data is an INDArray of doubles/floats <br>
	 * Converts the given INDArray to an appropriate byte array for a structure by
	 * argMaxing the array
	 * 
	 * @param data
	 */
	public static byte[][][][] convertToUsable(INDArray data) {
		INDArray maxed = data.argMax(1);
		System.out.println(Arrays.toString(maxed.shape()));
		System.out.println(maxed);
		byte[][][][] use = createArrayWithCorrectDimensions();
		for (int x = 0; x < maxed.shape()[1]; x++) {
			for (int y = 0; y < maxed.shape()[2]; y++) {
				for (int z = 0; z < maxed.shape()[3]; z++) {
					use[x][y][z][maxed.getInt(0, x, y, z)] = 1;
				}
			}
		}
		return use;
	}

	/**
	 * Initializes this to the given array; uses the array itself so please don't
	 * change it!
	 * 
	 * @param data
	 */
	public void init(byte[][][][] data) {

		initBlockStateList();

		int validity = isDataValid(data);
		if (validity != 0) {
			throw new IllegalArgumentException("Got invalid array; " + writeDataInvalidityMessage(validity, data));
		}
		this.dataArray = fillWithAir(data);

	}

	/**
	 * Helper method to get the dimensions of a 4d array
	 * 
	 * @param data
	 * @return
	 */
	public static int[] getDimensionsOf(byte[][][][] data) {
		int[] dims = { data.length, data[0].length, data[0][0].length, data[0][0][0].length };
		return dims;
	}

	/**
	 * Creates array with the correct dimensions as given above
	 */
	public static byte[][][][] createArrayWithCorrectDimensions() {
		if (tensorDimensions == null)
			initBlockStateList();
		return new byte[spatialDimensions[0]][spatialDimensions[1]][spatialDimensions[2]][numArraySlots()];
	}

	public static int numArraySlots() {
		return MaterialColor.MATERIAL_COLORS.length + 1;
	}

	/**
	 * Sets up the N-dimensional array with the blocks in the region. All
	 * incompatible modded blocks are replaced with air, as are structure voids! If
	 * the AABB is not exactly {@value dimensions} in size then an error is thrown
	 * 
	 * @param world
	 * @param region
	 */
	public void init(World world, AxisAlignedBB region) {

		if ((int) region.getXsize() != spatialDimensions[0] || (int) region.getYsize() != spatialDimensions[1]
				|| (int) region.getZsize() != spatialDimensions[2]) {
			throw new IllegalArgumentException(region.toString() + " does not match the proper data size of "
					+ Arrays.toString(spatialDimensions));
		}

		dataArray = createArrayWithCorrectDimensions();

		for (int x = (int) region.minX; x <= (int) region.maxX; x++) {
			for (int y = (int) region.minY; y <= (int) region.maxY; y++) {
				for (int z = (int) region.minZ; z <= (int) region.maxZ; z++) {

					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos);
					ResourceLocation regName = state.getBlock().getRegistryName();
					if (!isCompatible(regName)) {
						state = Blocks.AIR.defaultBlockState();
						regName = state.getBlock().getRegistryName();
					}
					dataArray[x][y][z] = encode(state);
				}
			}
		}
		dataArray = fillWithAir(dataArray);
	}

	/**
	 * Gets size of structure from structureNBT
	 * 
	 * @param nbt
	 * @return
	 */
	public static int[] getSize(CompoundNBT nbt) {

		ListNBT sizeNBT = (ListNBT) nbt.get("size");

		return new int[] { sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2) };
	}

	/**
	 * initializes data array from nbt file offset -> a 3 slot array representing
	 * the offset to apply to the structure if it is smaller than the dataset
	 * dimensions
	 * 
	 * @param dat
	 */
	public void init(CompoundNBT dat, int... offset) {
		if (offset.length == 0) {
			offset = new int[3];
		} else if (offset.length != 3)
			throw new IllegalArgumentException("Offset array " + Arrays.toString(offset) + "has bad dimensions");

		ListNBT sizeNBT = (ListNBT) dat.get("size");

		int[] size = { sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2) };

		if (size[0] + offset[0] > spatialDimensions[0] || size[1] + offset[1] > spatialDimensions[1]
				|| size[2] + offset[2] > spatialDimensions[2]) {
			throw new IllegalArgumentException(
					"Data size " + Arrays.toString(size) + " added to offset " + Arrays.toString(offset)
							+ " is greater than max dimensions " + Arrays.toString(spatialDimensions));
		}

		dataArray = createArrayWithCorrectDimensions();
		List<BlockState> palette = new ArrayList<>();
		ListNBT paletteNBT = (ListNBT) dat.get("palette");
		for (INBT inbt : paletteNBT) {
			CompoundNBT tag = (CompoundNBT) inbt;
			BlockState state = NBTUtil.readBlockState(tag);
			palette.add(state);
		}

		ListNBT blocksNBT = (ListNBT) dat.get("blocks");

		for (INBT inbt : blocksNBT) {
			CompoundNBT tag = (CompoundNBT) inbt;

			ListNBT posArray = tag.getList("pos", NBT.TAG_INT);
			BlockPos pos = new BlockPos(posArray.getInt(0), posArray.getInt(1), posArray.getInt(2));
			BlockState state = palette.get(tag.getInt("state"));
			if (!isCompatible(state.getBlock().getRegistryName())) {
				state = Blocks.AIR.defaultBlockState();
			}

			if (state.getBlock() == Blocks.JIGSAW) {
				String finalState = tag.getCompound("nbt").getString("final_state");
				BlockStateParser parser = new BlockStateParser(new StringReader(finalState), true);
				try {
					parser.parse(false);
					BlockState state2 = parser.getState();
					if (state2 != null) {
						state = state2;
					} else {
						System.err.println("Error while parsing blockstate " + finalState + " from jigsaw block");

					}
				} catch (CommandSyntaxException commandsyntaxexception) {
					System.err.println("Error while parsing blockstate " + finalState + " from jigsaw block");
				}
			}

			int x = pos.getX() + offset[0];
			int y = pos.getY() + offset[1];
			int z = pos.getZ() + offset[2];
			dataArray[x][y][z] = encode(state);
		}
		dataArray = fillWithAir(dataArray);
		Set<BlockState> interestingStates = new HashSet<>();

		for (int x = 0; x < dataArray.length; x++) {
			for (int y = 0; y < dataArray[x].length; y++) {
				for (int z = 0; z < dataArray[x][y].length; z++) {
					BlockState decoded = decode(dataArray[x][y][z]);

					if (!decoded.isAir()) {
						interestingStates.add(decoded);
					}
				}
			}
		}

		System.out.print("Initialized data for a structure");
		if (!interestingStates.isEmpty())
			System.out.println("first couple of blocks are "
					+ (new ArrayList<>(interestingStates)).subList(0, Math.min(interestingStates.size(), 5)));
		else
			System.out.println("For some reason all blocks are air");
	}

	/**
	 * Places this structure into the world in the given region
	 * 
	 * @param region
	 */
	public void placeInWorld(World world, AxisAlignedBB region) {
		int validity = isDataValid(dataArray);
		if (validity != 0)
			throw new IllegalStateException("Tried to place structure when data is invalid; "
					+ writeDataInvalidityMessage(validity, dataArray));

		if ((int) region.getXsize() != dataArray.length || (int) region.getYsize() != dataArray[0].length
				|| (int) region.getZsize() != dataArray[0][0].length) {
			throw new IllegalArgumentException(region.toString() + " does not match the proper data size of "
					+ Arrays.toString(getDimensionsOf(dataArray)));
		}

		int minX = (int) region.minX;
		int minY = (int) region.minY;
		int minZ = (int) region.minZ;

		for (int x = 0; x < dataArray.length; x++) {
			for (int y = 0; y < dataArray[0].length; y++) {
				for (int z = 0; z < dataArray[0][0].length; z++) {
					byte[] bsarray = dataArray[x][y][z];
					BlockState block = decode(bsarray);
					BlockPos pos = (new BlockPos(minX + x, minY + y, minZ + z));
					if (!World.isInWorldBounds(pos))
						continue;
					world.setBlock(pos, block, 2);
				}
			}
		}

	}

	/**
	 * Places structure in world using the position as the corner of least
	 * coordinates
	 * 
	 * @param world
	 * @param pos
	 */
	public void placeInWorld(World world, BlockPos pos) {
		AxisAlignedBB bb = new AxisAlignedBB(pos,
				pos.offset(dataArray.length, dataArray[0].length, dataArray[0][0].length));
		placeInWorld(world, bb);
	}

	/**
	 * TODO (if needed?) Writes the dataArray as a structure file
	 * 
	 * @return
	 */
	public CompoundNBT writeStructureFile() throws Exception {
		throw new Exception("Cannot be used currently");
	}

	/**
	 * Helper method to decode a block from a data array
	 */
	public static BlockState decode(byte[] vec) {
		if (tensorDimensions == null)
			initBlockStateList();

		if (vec.length != numArraySlots())
			throw new IllegalArgumentException("Given vector is missized with dimensions of " + vec.length
					+ " when it should have dimensions of " + numArraySlots());

		MaterialColor color = null;
		for (int i = 0; i < MaterialColor.MATERIAL_COLORS.length; i++) {
			if (vec[i] != 0) {
				color = MaterialColor.MATERIAL_COLORS[i];
			}
		}
		if (color == null) {
			return Blocks.AIR.defaultBlockState();
		}

		return MATERIAL_TO_BLOCK.get(color).defaultBlockState();
	}

	/**
	 * Returns null if the
	 * 
	 * @param color
	 * @return
	 */
	public static Block materialToBlock(MaterialColor color) {

		return MATERIAL_TO_BLOCK.get(color);
	}

	public static byte[] encode(BlockState state) {
		byte[] vec = new byte[numArraySlots()];
		int ord = getOrdinal(state.getMaterial().getColor());
		if (state.isAir())
			ord = numArraySlots() - 1;
		vec[ord] = 1;
		return vec;
	}

	public static int getOrdinal(MaterialColor color) {
		for (int i = 0; i < MaterialColor.MATERIAL_COLORS.length; i++) {
			if (MaterialColor.MATERIAL_COLORS[i] == color)
				return i;
		}
		return -1;
	}

	/**
	 * Fills all "empty" slots of the given 4d array with air and returns the array
	 * for convenience
	 * 
	 * @param input
	 * @return
	 */
	private static byte[][][][] fillWithAir(byte[][][][] input) {
		for (int x = 0; x < input.length; x++) {
			for (int y = 0; y < input[0].length; y++) {
				for (int z = 0; z < input[0][0].length; z++) {
					byte[] bsarray = input[x][y][z];
					boolean isEmpty = isArrayZeroes(bsarray);
					if (isEmpty) {
						input[x][y][z] = encode(Blocks.AIR.defaultBlockState());
					}
				}
			}
		}

		return input;
	}

	/**
	 * Checks if every element of this array is a zero
	 * 
	 * @param toCheck
	 * @return
	 */
	private static boolean isArrayZeroes(byte[] toCheck) {

		for (byte a : toCheck) {
			if (a != 0)
				return false;
		}
		return true;

	}

	/**
	 * Checks if the given array has multiple non-zero values
	 * 
	 * @param toCheck
	 * @return
	 */
	private static boolean doesArrayHaveMultipleOnes(byte[] toCheck) {
		boolean foundOne = false;
		for (int val : toCheck) {
			if (val != 0) {
				if (foundOne) {
					return true;
				} else {
					foundOne = true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a number for each problem, or 0 if no problems. Problems are: <br>
	 * - is null (1)<br>
	 * - has the wrong dimensions (2)<br>
	 * - array has multiple non-zero values (3) <br>
	 * 
	 * @param data
	 * @return
	 */
	private static int isDataValid(byte[][][][] data) {
		if (data == null)
			return 1;
		if (!Arrays.equals(getDimensionsOf(data), getTensorDimensions()))
			return 2;
		for (byte[][][] datX : data) {
			for (byte[][] datY : datX) {
				for (byte[] datZ : datY) {
					if (doesArrayHaveMultipleOnes(datZ))
						return 3;
				}
			}
		}
		return 0;
	}

	/**
	 * Returns a string that indicates what is wrong with the data array given the
	 * code returned from isDataValid
	 * 
	 * @param validityCode
	 * @return
	 */
	private static String writeDataInvalidityMessage(int validityCode, byte[][][][] data) {
		switch (validityCode) {
		case 1:
			return "Data is null";
		case 2:
			return "Data has the wrong dimensions; dimensions are " + Arrays.toString(getDimensionsOf(data));

		case 3:
			return "A data point has multiple non-zero values";
		default:
			return "Fine";
		}
	}

	/**
	 * Returns the array itself. Be careful--changing this changes the data
	 * 
	 * @return
	 */
	public byte[][][][] getDataArray() {
		if (!isInitialized())
			throw new IllegalStateException("Cannot access data array before initialized");
		return dataArray;
	}

	/**
	 * Initializes block state list for AI
	 */
	public static void initBlockStateList() {

		initValuesFromSettings();

		System.out.println("Initializing some values for AI...");

		tensorDimensions = new int[] { spatialDimensions[0], spatialDimensions[1], spatialDimensions[2],
				numArraySlots() };
	}

	/**
	 * Whether the block given by this resource location is compatible with the
	 * Neural Network of the AI
	 * 
	 * @param rl
	 * @return
	 */
	public static boolean isCompatible(ResourceLocation rl) {
		return (rl.getNamespace().equals("minecraft") || rl.getNamespace().equals(Reference.MODID))
				&& !rl.equals(Blocks.STRUCTURE_VOID.getRegistryName())
				&& !rl.equals(Blocks.STRUCTURE_BLOCK.getRegistryName());
	}

	/**
	 * Returns the necessary dimensions of the space
	 * 
	 * @return
	 */
	public static int[] getSpatialDimensions() {
		if (spatialDimensions == null)
			initValuesFromSettings();
		return spatialDimensions;
	}

	public static int[] getTensorDimensions() {
		if (tensorDimensions == null)
			initBlockStateList();
		return tensorDimensions;
	}

	/**
	 * TODO generalize this to work with a jar file Reads from the hardcoded
	 * settings file to initialize necessary values
	 */
	public static void initValuesFromSettings() {

		if (spatialDimensions != null)
			return;
		Path settings = Paths.get("config/hardcodedaisettings.txt");
		Path filesToScan = Paths.get("config/basegamefilestoscan.txt");
		System.out.println("Loading AI settings on Java end from " + settings + "...");

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

						spatialDimensions = Arrays.stream(intsS).mapToInt(Integer::parseInt).toArray();
						System.out.println("Set dimensions to " + Arrays.toString(spatialDimensions));
					}
						break;

					case "batch_size": {
						BATCH_SIZE = Integer.parseInt(value);
						System.out.println("Set batch size to " + BATCH_SIZE);
					}
						break;
					}

				} catch (Exception e) {
					throw new RuntimeException("error scanning line " + line, e);
				}

			}
			System.out.println("Done loading settings!");
		} catch (IOException e) {
			throw new RuntimeException("error while reading settings for AI", e);
		}
		try (BufferedReader reader = Files.newBufferedReader(filesToScan)) {
			baseGameFilesToScan = new String[BATCH_SIZE];
			String line = reader.readLine();
			for (int i = 0; line != null && i < BATCH_SIZE; i++) {
				baseGameFilesToScan[i] = line;

				line = reader.readLine();
			}

		} catch (IOException e) {
			throw new RuntimeException("error while reading scanfiles for AI", e);

		}

	}

	/**
	 * Writes all house files in the assets/structures/etc directory to Java
	 */
	public static void writeAllHousesToJavaOutput() {
		initBlockStateList();
		String[] folders = { "desert", "plains", "savanna", "snowy", "taiga" };

		StructureDataNDArray data = new StructureDataNDArray();

		for (String folder : folders) {
			String houses = "structures/village/" + folder + "/houses";
			List<String> files = Arrays.stream(baseGameFilesToScan).filter((e) -> e.startsWith(folder))
					.collect(Collectors.toList());

			try {

				/*
				 * System.out.println(loc); System.out.println(loc.toURI()); Path folderPath =
				 * Path.of(loc.toURI()); DirectoryStream<Path> directory =
				 * Files.newDirectoryStream(folderPath);
				 */

				for (String fileName : files) {

					ResourceLocation newLoc = new ResourceLocation("minecraft", houses + "/" + fileName);

					data.wipe();
					System.gc();
					CompoundNBT nbt = CompressedStreamTools
							.readCompressed(GMUtils.getDataStream(StructureDataNDArray.class, newLoc));

					int[] size = getSize(nbt);

					if (size[0] > spatialDimensions[0] || size[1] > spatialDimensions[1]
							|| size[2] > spatialDimensions[2]) {
						System.out.println("House file at " + fileName + " under " + folder
								+ " is too big with dimensions " + Arrays.toString(size));

						continue;
					}

					System.out.println("Converting " + fileName + " to data array");
					int[] offset1 = { spatialDimensions[0] - size[0], spatialDimensions[1] - size[1],
							spatialDimensions[2] - size[2] };

					data.init(nbt);
					data.writeDataToFile(true);
					data.wipe();
					System.gc();
					data.init(nbt, offset1);
					data.writeDataToFile(true);

				}

			} catch (Exception e) {
				throw new RuntimeException("error while writing files under " + houses, e);
			}
		}

	}

	/**
	 * Clears data in this object for re-use
	 */
	public void wipe() {
		this.dataArray = null;
	}

	/**
	 * Writes data to javaprogramoutput file; Format rules: <br>
	 * semicolon-newline divides pieces of data<br>
	 * 
	 * @param name
	 */
	public void writeDataToFile(boolean append) {
		try {

			Files.writeString(trainingDatasetFile, Arrays.deepToString(dataArray) + ";\n", StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new ReportedException(CrashReport.forThrowable(e, ""));
		}
	}

	/**
	 * TODO generalize this to work with jar file Writes to output settings file
	 */
	public static void setContentsOfOutputFile(String contents) {
		try (BufferedWriter writer = Files.newBufferedWriter(trainingDatasetFile)) {
			writer.write(contents);
		} catch (IOException e) {
			throw new ReportedException(CrashReport.forThrowable(e, "error while writing to java output file for AI"));
		}
	}

	public String toString() {
		return this.getClass().getSimpleName() + Nd4j.createFromArray(dataArray);
	}

}
