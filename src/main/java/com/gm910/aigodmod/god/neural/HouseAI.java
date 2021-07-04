package com.gm910.aigodmod.god.neural;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.op.Ops;
import org.tensorflow.op.Scope;
import org.tensorflow.op.core.Shape;
import org.tensorflow.op.nn.Conv3d;
import org.tensorflow.types.TInt64;

public class HouseAI {

	
	
	public HouseAI(long dataSize) {
		long[] tensorShape = Arrays.stream(StructureDataNDArray.getTensorDimensions()).mapToLong((i) -> (long)i).toArray();
		long[] inputShape = ArrayUtils.insert(0, tensorShape, dataSize);
		
		Graph graph = new Graph();
		
		
	}
	

}
