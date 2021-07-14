package com.gm910.aigodmod.god.neural;

import java.util.Arrays;

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
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnn3DPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;

public class HouseAI {

	public static final int NOISE_DIM = 100;

	public static final double GENERATOR_LR = 0.0001;
	public static final double DISCRIMINATOR_LR = 0.0001;

	public static int HOUSE_CHANNELS;

	public static int[] SPATIAL_DIMENSIONS;

	public HouseAI() {
		if (StructureDataNDArray.getSpatialDimensions() == null) {
			StructureDataNDArray.initBlockStateList();

		}
		SPATIAL_DIMENSIONS = StructureDataNDArray.getSpatialDimensions();

		HOUSE_CHANNELS = StructureDataNDArray.numArraySlots();
	}

	public MultiLayerNetwork buildGeneratorModel() {
		try {
			int unit1 = (SPATIAL_DIMENSIONS[0] / 6);
			int unit2 = (SPATIAL_DIMENSIONS[1] / 6);
			int unit3 = (SPATIAL_DIMENSIONS[2] / 6);
			long[] tensorShape = Arrays.stream(StructureDataNDArray.getTensorDimensions()).mapToLong((i) -> (long) i)
					.toArray();
			// long[] inputShape = ArrayUtils.insert(0, tensorShape, dataSize);

			NeuralNetConfiguration.Builder builder1 = new NeuralNetConfiguration.Builder();
			ListBuilder builder = builder1.list();
			builder.setInputType(InputType.feedForward(NOISE_DIM));
			builder.layer(
					new DenseLayer.Builder().units(unit1 * unit2 * unit3 * HOUSE_CHANNELS * 4).hasBias(false).build());
			// builder.setInputType(InputType.convolutional3D(Convolution3D.DataFormat.NDHWC,
			// 5, 5, 5, 256));
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
			return network;

		} catch (Exception e) {
			throw new RuntimeException("Error creating AI model", e);
		}
	}

	public static MultiLayerNetwork buildDiscriminatorModel() {
		try {

			int unit1 = (SPATIAL_DIMENSIONS[0] / 6);
			int unit2 = (SPATIAL_DIMENSIONS[1] / 6);
			int unit3 = (SPATIAL_DIMENSIONS[2] / 6);
			long[] tensorShape = Arrays.stream(StructureDataNDArray.getTensorDimensions()).mapToLong((i) -> (long) i)
					.toArray();
			NeuralNetConfiguration.Builder builder1 = new NeuralNetConfiguration.Builder();
			ListBuilder builder = builder1.list();
			builder.setInputType(
					InputType.convolutional3D(Convolution3D.DataFormat.NDHWC, unit1, unit2, unit3, HOUSE_CHANNELS));

			MultiLayerConfiguration config = builder.build();
			MultiLayerNetwork network = new MultiLayerNetwork(config);
			network.init();
			network.setLearningRate(DISCRIMINATOR_LR);
			return network;

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

}
