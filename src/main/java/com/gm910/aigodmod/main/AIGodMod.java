package com.gm910.aigodmod.main;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gm910.aigodmod.god.neural.StructureDataNDArray;
import com.gm910.aigodmod.util.GMUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MODID)
public class AIGodMod {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	private StructureDataNDArray test;

	public AIGodMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		// some preinit code
		LOGGER.info("HELLO FROM PREINIT");
		ResourceLocation pythonLoc = new ResourceLocation(Reference.MODID, "house_network.py");
		/*
		 * Properties props = new Properties();
		 * 
		 * props.put("python.home", "mods/Lib/"); props.put("jython.home", "mods/Lib/");
		 * props.put("python.security.respectJavaAccessibility", "false");
		 * props.put("python.import.site", "false");
		 * 
		 * Properties preprops = System.getProperties();
		 * 
		 * PythonInterpreter.initialize(preprops, props, new String[0]);
		 */
		// PythonUtils.execPythonFileFromResourceLocation(this.getClass(), pythonLoc);

		CompoundNBT testNBT = GMUtils.loadNBTFile(GMUtils.getDataStream(this.getClass(),
				new ResourceLocation("minecraft", "structures/village/desert/houses/desert_medium_house_1.nbt")));

		System.out.println(testNBT);

		test = new StructureDataNDArray();
		test.init(testNBT);
		int[][][][] datar = test.getDataArray();
		System.out.println("Data shape: " + Arrays.toString(StructureDataNDArray.getDimensionsOf(datar)));

		StructureDataNDArray.writeAllHousesToJavaOutput();

	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		// do something that can only be done on the client
		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
		// some example code to dispatch IMC to another mod
		InterModComms.sendTo(Reference.MODID, "helloworld", () -> {
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});
	}

	private void processIMC(final InterModProcessEvent event) {
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}",
				event.getIMCStream().map(m -> m.getMessageSupplier().get()).collect(Collectors.toList()));
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		// do something when the server starts
		LOGGER.info("HELLO from server starting");

	}

	@SubscribeEvent
	public void serverStarted(FMLServerStartedEvent event) {

	}

	@SubscribeEvent
	public void playerLoaded(LivingUpdateEvent event) {
		if (!(event.getEntity().level instanceof ServerWorld))
			return;
		if (!(event.getEntity() instanceof PlayerEntity))
			return;
		if (event.getEntity().tickCount != 10)
			return;
		ServerWorld world = (ServerWorld) event.getEntity().level;
		test.placeInWorld(world, world.getRandomPlayer().blockPosition().above(40));
	}

	// You can use EventBusSubscriber to automatically subscribe events on the
	// contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}

	}
}