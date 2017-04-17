from skimage import io
from os import walk
from os import path

from skimage.color import rgb2grey
from skimage import exposure
from skimage.restoration import denoise_tv_chambolle
from _frangi import frangi, hessian
from skimage.filters import threshold_adaptive
from skimage import morphology
from skimage.morphology import skeletonize
import warnings as war
from scipy import misc
from scipy.ndimage.filters import prewitt

from Procesado import *

import matplotlib.pyplot as plt


"""
Clase que contendrá una longitud y un ángulo.
El ángulo será respecto al eje X en radianes
"""
class Polilineas():

    def __init__(self, longitud, angulo):
        self.__longitud = longitud
        self.__angulo = angulo

    def getLongitud(self):
        return self.__longitud

    def getAngulo(self):
        return self.__angulo