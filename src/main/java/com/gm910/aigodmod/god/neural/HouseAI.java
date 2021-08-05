package com.gm910.aigodmod.god.neural;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import org.deeplearning4j.nn.api.Layer.TrainingMode;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ActivationLayer;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.Convolution3D;
import org.deeplearning4j.nn.conf.layers.Deconvolution3D;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnn3DPreProcessor;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.layers.core.KerasFlatten;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.DefaultRandom;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.lossfunctions.impl.LossBinaryXENT;

import com.google.common.collect.Maps;

public class HouseAI {

	public static final int NOISE_DIM = 100;

	public static final double GENERATOR_LR = 0.0001;
	public static final double DISCRIMINATOR_LR = 0.0001;

	public static int HOUSE_CHANNELS;

	public static int[] SPATIAL_DIMENSIONS;

	private MultiLayerNetwork generator;
	private MultiLayerNetwork discriminator;

	public HouseAI() {
		if (StructureDataNDArray.getSpatialDimensions() == null) {
			StructureDataNDArray.initBlockStateList();

		}
		SPATIAL_DIMENSIONS = StructureDataNDArray.getSpatialDimensions();

		HOUSE_CHANNELS = StructureDataNDArray.numArraySlots();
	}

	/**
	 * One step in training
	 */
	public void trainStep(INDArray images) {

		Objects.requireNonNull(generator, "Generator is unintialized");
		Objects.requireNonNull(discriminator, "Discriminator is uninitialized");

		try (org.nd4j.linalg.api.rng.Random rand1 = (new DefaultRandom())) {
			INDArray inp = rand1.nextDouble(new int[] { StructureDataNDArray.BATCH_SIZE, NOISE_DIM });
			this.generator.setInput(inp);
			INDArray genOut = generator.activate(TrainingMode.TRAIN);

			discriminator.setInput(images);
			INDArray realOut = discriminator.activate(TrainingMode.TRAIN);
			discriminator.setInput(genOut);
			INDArray fakeOut = discriminator.activate(TrainingMode.TRAIN);

			double genLoss = generatorLoss(fakeOut);
			double discLoss = discriminatorLoss(realOut, fakeOut);

			Adam discOpt = Adam.builder().learningRate(DISCRIMINATOR_LR).build();
			Adam genOpt = Adam.builder().learningRate(GENERATOR_LR).build();
			// AdamUpdater discOptU = discOpt.instantiate(null, false)
		} catch (Exception e) {
			throw new RuntimeException("Issues building keras model", e);
		}
	}

	public MultiLayerNetwork buildGeneratorModel() {
		try {
			int unit1 = (SPATIAL_DIMENSIONS[0] / 6);
			int unit2 = (SPATIAL_DIMENSIONS[1] / 6);
			int unit3 = (SPATIAL_DIMENSIONS[2] / 6);
			long[] tensorShape = Arrays.stream(StructureDataNDArray.getTensorDimensions()).mapToLong((i) -> (long) i)
					.toArray();

			NeuralNetConfiguration.Builder builder1 = new NeuralNetConfiguration.Builder();
			ListBuilder builder = builder1.list();
			builder.setInputType(InputType.feedForward(NOISE_DIM));
			builder.layer(
					new DenseLayer.Builder().units(unit1 * unit2 * unit3 * HOUSE_CHANNELS * 4).hasBias(false).build());

			builder.layer(new BatchNormalization());
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));

			FeedForwardToCnn3DPreProcessor preproc = new FeedForwardToCnn3DPreProcessor(5, 5, 5);
			preproc.setNumChannels(HOUSE_CHANNELS * 4);
			builder.inputPreProcessor(3, preproc);

			builder.layer(deconv3D(HOUSE_CHANNELS * 4, HOUSE_CHANNELS * 2, new int[] { 5, 5, 5 }, new int[] { 1, 1, 1 },
					false));
			System.out.println("Current type: " + builder.getLayerActivationTypes());

			// builder.layer(new BatchNormalization());
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));

			builder.layer(
					deconv3D(HOUSE_CHANNELS * 2, HOUSE_CHANNELS, new int[] { 5, 5, 5 }, new int[] { 3, 3, 3 }, false));
			// builder.layer(new BatchNormalization());
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));

			Deconvolution3D layer = new Deconvolution3D.Builder().nIn(HOUSE_CHANNELS).kernelSize(5, 5, 5)
					.stride(2, 2, 2).hasBias(false).nOut(HOUSE_CHANNELS).activation(Activation.TANH).build();
			layer.setConvolutionMode(ConvolutionMode.Same);
			builder.layer(layer);
			builder.layer(new ActivationLayer(Activation.RELU));
			assert Arrays.equals(
					builder.getLayerActivationTypes().get(builder.getLayerActivationTypes().size()).getShape(),
					tensorShape)
					: new IllegalStateException("Output of array is "
							+ Arrays.toString(builder.getLayerActivationTypes()
									.get(builder.getLayerActivationTypes().size() - 1).getShape())
							+ " expected " + Arrays.toString(tensorShape));
			MultiLayerConfiguration config = builder.build();
			MultiLayerNetwork network = new MultiLayerNetwork(config);
			network.init();
			network.setLearningRate(GENERATOR_LR);
			return generator = network;

		} catch (Exception e) {
			throw new RuntimeException("Error creating AI model", e);
		}
	}

	public static MultiLayerNetwork importKerasModel(InputStream fileStream)
			throws IOException, InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {

		// KerasLayer.registerCustomLayer("Conv3DTranspose",
		// Deconvolution3DKerasLayer.class);
		return KerasModelImport.importKerasSequentialModelAndWeights(fileStream);

	}

	public MultiLayerNetwork buildDiscriminatorModel() {
		try {

			int unit1 = (SPATIAL_DIMENSIONS[0] / 6);
			int unit2 = (SPATIAL_DIMENSIONS[1] / 6);
			int unit3 = (SPATIAL_DIMENSIONS[2] / 6);
			long[] tensorShape = Arrays.stream(StructureDataNDArray.getTensorDimensions()).mapToLong((i) -> (long) i)
					.toArray();
			NeuralNetConfiguration.Builder builder1 = new NeuralNetConfiguration.Builder();
			ListBuilder builder = builder1.list();
			builder.setInputType(InputType.convolutional3D(Convolution3D.DataFormat.NDHWC, tensorShape[0],
					tensorShape[1], tensorShape[2], tensorShape[3]));

			builder.layer(conv3D(HOUSE_CHANNELS, HOUSE_CHANNELS, new int[] { 5, 5, 5 }, new int[] { 2, 2, 2 }, false));
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));
			builder.layer(new DropoutLayer(0.3));

			builder.layer(
					conv3D(HOUSE_CHANNELS, HOUSE_CHANNELS * 2, new int[] { 5, 5, 5 }, new int[] { 3, 3, 3 }, false));
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));
			builder.layer(new DropoutLayer(0.3));

			builder.layer(conv3D(HOUSE_CHANNELS * 2, HOUSE_CHANNELS * 4, new int[] { 5, 5, 5 }, new int[] { 1, 1, 1 },
					false));
			builder.layer(new ActivationLayer(Activation.LEAKYRELU));
			builder.layer(new DropoutLayer(0.3));

			builder.inputPreProcessor(9, (new KerasFlatten(Maps.newHashMap()))
					.getInputPreprocessor(builder.getLayerActivationTypes().toArray(InputType[]::new)));

			builder.layer(new DenseLayer.Builder().units(1).hasBias(false).activation(Activation.SIGMOID).build());

			MultiLayerConfiguration config = builder.build();
			MultiLayerNetwork network = new MultiLayerNetwork(config);
			network.init();
			network.setLearningRate(DISCRIMINATOR_LR);
			return discriminator = network;

		} catch (Exception e) {
			throw new RuntimeException("Error creating discriminator model", e);
		}
	}

	public static Deconvolution3D deconv3D(int filters, int out, int[] kernel, int[] stride, boolean hasBias) {
		Deconvolution3D layer = new Deconvolution3D.Builder().nIn(filters).kernelSize(kernel).stride(stride)
				.hasBias(hasBias).nOut(out).build();
		layer.setConvolutionMode(ConvolutionMode.Same);
		return layer;
	}

	public static Convolution3D conv3D(int filters, int out, int[] kernel, int[] stride, boolean hasBias) {
		Convolution3D layer = new Convolution3D.Builder().nIn(filters).kernelSize(kernel).stride(stride)
				.hasBias(hasBias).nOut(out).build();
		layer.setConvolutionMode(ConvolutionMode.Same);
		return layer;
	}

	public static double crossEntropy(INDArray labels, INDArray arr) {
		ILossFunction func = new LossBinaryXENT();
		return func.computeScore(labels, arr, Activation.IDENTITY.getActivationFunction(), null, false);
	}

	public static double discriminatorLoss(INDArray realOut, INDArray fakeOut) {
		double realLoss = crossEntropy(Nd4j.onesLike(realOut), realOut);
		double fakeLoss = crossEntropy(Nd4j.zerosLike(fakeOut), fakeOut);
		return fakeLoss + realLoss;
	}

	public static double generatorLoss(INDArray discriminatorFakeOutput) {
		return crossEntropy(Nd4j.onesLike(discriminatorFakeOutput), discriminatorFakeOutput);
	}

}
