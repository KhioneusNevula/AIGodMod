package com.gm910.aigodmod.god.neural;

import static org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils.getConvolutionModeFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils.getDilationRate;
import static org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils.getKernelSizeFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils.getPaddingFromBorderModeConfig;
import static org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils.getStrideFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.utils.KerasActivationUtils.getIActivationFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.utils.KerasInitilizationUtils.getWeightInitFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.utils.KerasLayerUtils.getHasBiasFromConfig;
import static org.deeplearning4j.nn.modelimport.keras.utils.KerasLayerUtils.getNOutFromConfig;

import java.util.Map;

import org.deeplearning4j.nn.api.layers.LayerConstraint;
import org.deeplearning4j.nn.conf.CNN2DFormat;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Convolution3D;
import org.deeplearning4j.nn.conf.layers.Deconvolution3D;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolution;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolutionUtils;
import org.deeplearning4j.nn.modelimport.keras.utils.KerasConstraintUtils;
import org.deeplearning4j.nn.weights.IWeightInit;

public class Deconvolution3DKerasLayer extends KerasConvolution {

	/**
	 * Pass-through constructor from KerasLayer
	 *
	 * @param kerasVersion major keras version
	 * @throws UnsupportedKerasConfigurationException Unsupported Keras config
	 */
	public Deconvolution3DKerasLayer(Integer kerasVersion) throws UnsupportedKerasConfigurationException {
		super(kerasVersion);
	}

	/**
	 * Constructor from parsed Keras layer configuration dictionary.
	 *
	 * @param layerConfig dictionary containing Keras layer configuration
	 * @throws InvalidKerasConfigurationException     Invalid Keras config
	 * @throws UnsupportedKerasConfigurationException Unsupported Keras config
	 */
	public Deconvolution3DKerasLayer(Map<String, Object> layerConfig)
			throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
		this(layerConfig, true);
	}

	/**
	 * Constructor from parsed Keras layer configuration dictionary.
	 *
	 * @param layerConfig           dictionary containing Keras layer configuration
	 * @param enforceTrainingConfig whether to enforce training-related
	 *                              configuration options
	 * @throws InvalidKerasConfigurationException     Invalid Keras config
	 * @throws UnsupportedKerasConfigurationException Unsupported Keras config
	 */
	public Deconvolution3DKerasLayer(Map<String, Object> layerConfig, boolean enforceTrainingConfig)
			throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
		super(layerConfig, enforceTrainingConfig);

		hasBias = getHasBiasFromConfig(layerConfig, conf);
		numTrainableParams = hasBias ? 2 : 1;
		int[] dilationRate = getDilationRate(layerConfig, 3, conf, false);

		IWeightInit init = getWeightInitFromConfig(layerConfig, conf.getLAYER_FIELD_INIT(), enforceTrainingConfig, conf,
				kerasMajorVersion);

		LayerConstraint biasConstraint = KerasConstraintUtils.getConstraintsFromConfig(layerConfig,
				conf.getLAYER_FIELD_B_CONSTRAINT(), conf, kerasMajorVersion);
		LayerConstraint weightConstraint = KerasConstraintUtils.getConstraintsFromConfig(layerConfig,
				conf.getLAYER_FIELD_W_CONSTRAINT(), conf, kerasMajorVersion);

		Deconvolution3D.Builder builder = new Deconvolution3D.Builder().name(this.layerName)
				.nOut(getNOutFromConfig(layerConfig, conf)).dropOut(this.dropout)
				.activation(getIActivationFromConfig(layerConfig, conf)).weightInit(init)
				.dataFormat(KerasConvolutionUtils.getDataFormatFromConfig(layerConfig, conf) == CNN2DFormat.NCHW
						? Convolution3D.DataFormat.NCDHW
						: Convolution3D.DataFormat.NDHWC)
				.l1(this.weightL1Regularization).l2(this.weightL2Regularization)
				.convolutionMode(getConvolutionModeFromConfig(layerConfig, conf))
				.kernelSize(getKernelSizeFromConfig(layerConfig, 2, conf, kerasMajorVersion)).hasBias(hasBias)
				.stride(getStrideFromConfig(layerConfig, 2, conf));
		int[] padding = getPaddingFromBorderModeConfig(layerConfig, 2, conf, kerasMajorVersion);
		if (hasBias)
			builder.biasInit(0.0);
		if (padding != null)
			builder.padding(padding);
		if (dilationRate != null)
			builder.dilation(dilationRate);
		if (biasConstraint != null)
			builder.constrainBias(biasConstraint);
		if (weightConstraint != null)
			builder.constrainWeights(weightConstraint);
		this.layer = builder.build();
		Deconvolution3D deconvolution2D = (Deconvolution3D) layer;
		deconvolution2D.setDefaultValueOverriden(true);
	}

	/**
	 * Get DL4J ConvolutionLayer.
	 *
	 * @return ConvolutionLayer
	 */
	public Deconvolution3D getDeconvolution3DLayer() {
		return (Deconvolution3D) this.layer;
	}

	/**
	 * Get layer output type.
	 *
	 * @param inputType Array of InputTypes
	 * @return output type as InputType
	 * @throws InvalidKerasConfigurationException Invalid Keras config
	 */
	@Override
	public InputType getOutputType(InputType... inputType) throws InvalidKerasConfigurationException {
		if (inputType.length > 1)
			throw new InvalidKerasConfigurationException(
					"Keras Convolution layer accepts only one input (received " + inputType.length + ")");
		return this.getDeconvolution3DLayer().getOutputType(-1, inputType[0]);
	}

}