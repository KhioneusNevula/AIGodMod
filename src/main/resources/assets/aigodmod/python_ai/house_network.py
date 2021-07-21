import glob
from itertools import islice
import os
import os
import pickle
import subprocess
import sys
import time

import pip
from tensorflow.keras import layers

import numpy as np
import tensorflow as tf


# subprocess.check_call([sys.executable, "-m", "pip", "install", "pyyaml", "h5py"])
checkpoint_dir = './training_checkpoints'
checkpoint_prefix = os.path.join(checkpoint_dir, "ckpt")




print("tensorflow imported")
print("Hello from house network file!")

# as usual, TODO generalize this better
settingspath = os.path.realpath(__file__)[:-61] + "run\\config\\"


outputfile = open(settingspath + "pythonprogramoutput.txt", mode='w')

BUFFER_SIZE = 60000
BATCH_SIZE = 256 # change to match batch size
CHANNELS = 64
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
            
with open(settingspath + "javaprogramoutput.txt") as settings:
    for line in settings.readlines():
        print(line)
        key, value = line.split("=")
        if key == "channels":
            CHANNELS = int(value)    


def make_discriminator_model():
    
    inputshape = (DIMENSIONS[0], DIMENSIONS[1], DIMENSIONS[2], CHANNELS)

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

def make_generator_model():
    model = tf.keras.Sequential()
    model.add(layers.Dense(5*5*5*256, use_bias=False, input_shape=(noise_dim,)))
    model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Reshape((5, 5, 5, 256)))
    assert model.output_shape == (None, 5, 5, 5, 256) 
    # The assert line shows the expected output shape at this stage. None is the batch size

    model.add(layers.Conv3DTranspose(128, (5, 5, 5), strides=(1, 1, 1), padding='same', use_bias=False))
    assert model.output_shape == (None, 5, 5, 5, 128)
    model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Conv3DTranspose(64, (5, 5, 5), strides=(3, 3, 3), padding='same', use_bias=False))
    assert model.output_shape == (None, 15, 15, 15, 64)
    model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Conv3DTranspose(64, (5, 5, 5), strides=(2, 2, 2), padding='same', use_bias=False, activation='tanh'))
    assert model.output_shape == (None, 30, 30, 30, 64) #What is the desired output size for the whole generator? 

    model.add(layers.ReLU())
    
    return model

generator = make_generator_model()
noise = tf.random.normal([1, noise_dim])



with open(settingspath + "gen_model.json", "w") as json_file:
    json_file.write(generator.to_json())
generator.save_weights(settingspath + "gen_model_weights.h5", save_format="h5")
generated_image=generator(noise, training=False)


outputfile.close()