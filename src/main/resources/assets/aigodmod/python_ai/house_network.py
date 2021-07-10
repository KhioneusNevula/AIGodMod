import os
import sys
import glob
from itertools import islice
import os
import time

from tensorflow.keras import layers

import numpy as np
import tensorflow as tf



checkpoint_dir = './training_checkpoints'
checkpoint_prefix = os.path.join(checkpoint_dir, "ckpt")




print("tensorflow imported")
print("Hello from house network file!")

# as usual, TODO generalize this better
settingspath = os.path.realpath(__file__)[:-61] + "run\\config\\"


outputfile = open(settingspath + "pythonprogramoutput.txt", mode='w')

BUFFER_SIZE = 60000
BATCH_SIZE = 256 # change to match batch size
EPOCHS = 50
noise_dim = 100
generator_lr = .0001
discriminator_lr = .0001
DIMENSIONS = ()



with open(settingspath + "hardcodedaisettings.txt") as settings:
    for line in settings.readlines():
        print(line)
        key, value = line.split("=")
        if key == "dimensions":
            DIMENSIONS = tuple([int(dim) for dim in value.split(",")])
        elif key == "batch_size":
            BATCH_SIZE = int(value)
            
            


def make_discriminator_model():
    
    inputshape = ()

    model = tf.keras.Sequential()

    model.add(layers.Conv3D(64, (5, 5, 5), strides=(2, 2, 2), padding='same',
                                     input_shape=inputshape))
    #TODO: Fill in input_shape above
    
    model.add(layers.LeakyReLU())
    #TODO: Add dropout here
    model.add(layers.Dropout(0.3))

    model.add(layers.Conv3D(128, (5, 5, 5), strides=(2, 2, 2), padding='same'))
    model.add(layers.LeakyReLU())
    #TODO: Add dropout here
    model.add(layers.Dropout(0.3))

    model.add(layers.Flatten())
    #TODO: Add final Dense layer here - how many outputs?
    model.add(layers.Dense(1, activation='sigmoid'))

    return model



outputfile.close()