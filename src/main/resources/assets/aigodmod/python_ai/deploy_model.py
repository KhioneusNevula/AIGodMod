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


noise_dim = 100

settingspath = os.path.realpath(__file__)[:-61] + "\\run\\config\\"

model = tf.keras.models.load_model(settingspath + "gen_model.h5")
seed = tf.random.normal([5, noise_dim]) 

out = model.predict(seed)
print(out.argmax(axis=4))
for i in out:
    
    print(i.shape)

np.save(settingspath + "ndarrayoutput.npy", out)