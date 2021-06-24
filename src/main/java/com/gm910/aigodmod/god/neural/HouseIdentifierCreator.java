package com.gm910.aigodmod.god.neural;

import org.tensorflow.Graph;
import org.tensorflow.keras.layers.Layers;
import org.tensorflow.keras.models.Model;
import org.tensorflow.keras.models.Sequential;
import org.tensorflow.op.Scope;
import org.tensorflow.op.nn.Conv2d;

public class HouseIdentifierCreator {
	
	private Model<Float> model;
	

	public HouseIdentifierCreator() {
		model = Sequential.of(Float.class, Layers.input(30, 30, 30), Layers.flatten(), Conv2d.<Float>create(new Scope(new Graph()), null, null, null, null, null));
	}

}
