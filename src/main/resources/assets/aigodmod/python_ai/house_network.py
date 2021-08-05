import glob
from itertools import islice
import os
import os
import pickle
import subprocess
import sys
import time

from numpy import genfromtxt, ndarray
import pip
from tensorboard.util.tensor_util import make_ndarray
from tensorflow.keras import layers

import numpy as np
import pandas as pd
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
CHANNELS = 65
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

def train(dataset, epochs):

  for epoch in range(epochs):
    start = time.time()

    for image_batch in dataset:
      train_step(image_batch)
    

    print ('Time for epoch {} is {} sec'.format(epoch + 1, time.time()-start))

convenience_save = None
training_data = []
try:
    convenience_save = open("C:/Users/borah/Desktop/convenience_pickle.pkl", 'rb')
    training_data = pickle.load(convenience_save)
    print("Loaded data from pickle file")
except:
    pass
if not convenience_save:
    print("Loading data from regular file")
    train_file = open("C:\\Users\\borah\\Desktop\\trainingdataset.txt", 'r')
    line = train_file.readline()
    count = 1;
    while line != "":
        training_data.append(eval(line.strip(";\n").strip(";")))
        line = train_file.readline()
        count+=1
        print(str(count) + " " + line[:10])
        
    
    convenience_save = open("C:/Users/borah/Desktop/convenience_pickle.pkl", 'wb')
    pickle._dump(training_data, convenience_save)
    convenience_save.close()
    train_file.close()

print("file loaded")
training_data_ndarray = np.array(training_data)

# f = open("C:/Users/borah/Desktop/out.txt", 'w')
print(training_data_ndarray.shape)
# f.close()


def make_discriminator_model():
    
    inputshape = (DIMENSIONS[0], DIMENSIONS[1], DIMENSIONS[2], CHANNELS)

    model = tf.keras.Sequential()

    model.add(layers.Conv3D(CHANNELS, (5, 5, 5), strides=(2, 2, 2), padding='same',
                                     input_shape=inputshape))
    #TODO: Fill in input_shape above
    
    model.add(layers.LeakyReLU())
    #TODO: Add dropout here
    model.add(layers.Dropout(0.3))

    model.add(layers.Conv3D(CHANNELS*2, (5, 5, 5), strides=(2, 2, 2), padding='same'))
    model.add(layers.LeakyReLU())
    #TODO: Add dropout here
    model.add(layers.Dropout(0.3))

    model.add(layers.Flatten())
    #TODO: Add final Dense layer here - how many outputs?
    model.add(layers.Dense(1, activation='sigmoid'))

    return model

def make_generator_model():
    model = tf.keras.Sequential()
    model.add(layers.Dense(5*5*5*CHANNELS*4, use_bias=False, input_shape=(noise_dim,)))
    #model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Reshape((5, 5, 5, CHANNELS*4)))
    assert model.output_shape == (None, 5, 5, 5, CHANNELS*4) 
    # The assert line shows the expected output shape at this stage. None is the batch size

    model.add(layers.Conv3DTranspose(CHANNELS*2, (5, 5, 5), strides=(1, 1, 1), padding='same', use_bias=False))
    assert model.output_shape == (None, 5, 5, 5, CHANNELS*2)
    #model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Conv3DTranspose(CHANNELS, (5, 5, 5), strides=(3, 3, 3), padding='same', use_bias=False))
    assert model.output_shape == (None, 15, 15, 15, CHANNELS)
    #model.add(layers.BatchNormalization())
    model.add(layers.LeakyReLU())

    model.add(layers.Conv3DTranspose(CHANNELS, (5, 5, 5), strides=(2, 2, 2), padding='same', use_bias=False, activation='tanh'))
    assert model.output_shape == (None, 30, 30, 30, CHANNELS) #What is the desired output size for the whole generator? 

    model.add(layers.ReLU())
    
    return model




generator = make_generator_model()
print(generator.summary())

cross_entropy = tf.keras.losses.BinaryCrossentropy(from_logits=True)

def discriminator_loss(real_output, fake_output):
    real_loss = cross_entropy(tf.ones_like(real_output), real_output)
    #YOUR CODE HERE to compute fake_loss and total_loss
    fake_loss = cross_entropy(tf.zeros_like(fake_output), fake_output)


    #END CODE
    return fake_loss+real_loss

def generator_loss(disc_fake_output):
    #YOUR CODE HERE to calculate loss using cross_entropy
    loss = cross_entropy(tf.ones_like(disc_fake_output), disc_fake_output)
    #END CODE
    return loss 

generator_optimizer = tf.keras.optimizers.Adam(generator_lr)
discriminator_optimizer = tf.keras.optimizers.Adam(discriminator_lr)

discriminator = make_discriminator_model()
print (discriminator.summary())



seed = tf.random.normal([5, noise_dim]) 

training_data_ndarray = np.array(training_data)

#Packaging up our training images as a Dataset object
train_dataset = tf.data.Dataset.from_tensor_slices(training_data_ndarray).shuffle(BUFFER_SIZE).batch(BATCH_SIZE)


@tf.function
def train_step(images):
    noise = tf.random.normal([BATCH_SIZE, noise_dim])

    with tf.GradientTape() as gen_tape, tf.GradientTape() as disc_tape:
      #Make images:
      generated_images = generator(noise, training=True)

      #Get the discriminator's output:
      real_output = discriminator(images, training=True)
      fake_output = discriminator(generated_images, training=True)

      #Calculate each network's loss:
      gen_loss = generator_loss(fake_output)
      disc_loss = discriminator_loss(real_output, fake_output)

    #Update each network's weights to (hopefully) reduce the loss next time:
    gradients_of_generator = gen_tape.gradient(gen_loss, generator.trainable_variables)
    gradients_of_discriminator = disc_tape.gradient(disc_loss, discriminator.trainable_variables)

    generator_optimizer.apply_gradients(zip(gradients_of_generator, generator.trainable_variables))
    discriminator_optimizer.apply_gradients(zip(gradients_of_discriminator, discriminator.trainable_variables))



train(train_dataset, EPOCHS)

generator.save(settingspath + "gen_model.h5", save_format="h5")

outputfile.close()