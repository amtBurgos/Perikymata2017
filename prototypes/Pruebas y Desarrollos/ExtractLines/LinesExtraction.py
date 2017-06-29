from skimage import io
from os import walk
from os import path

from skimage.color import rgb2grey
from skimage import exposure
from skimage.restoration import denoise_tv_chambolle
from _frangi import frangi, hessian
from skimage.filters import threshold_adaptive, threshold_local
from skimage import morphology
from skimage.color import rgb2gray
from skimage.morphology import skeletonize
import warnings as war
from scipy import misc
from scipy.ndimage.filters import prewitt, sobel
from scipy import misc

from Procesado import *

import matplotlib.pyplot as plt
import numpy as np


class LinesExtraction():

    def __init__(self):
        pass

    def saveImage(self, img, savePath):
        #Guarda en blanco y negro
        misc.toimage(img, cmin=False, cmax=True).save(savePath)

    def loadImage(self, path):
        return rgb2gray(io.imread(path))

    def loadImagesFrom(self,package):
        """
        Cargar imágenes dada la ruta de una carpeta. Carga también las imágenes en subcarpetas
        @param package - ruta a una carpeta
        @return data - diccionario nombre-imagen con las imágenes cargadas
        """
        data = dict()
        for root, dirs, files in walk(package):
            for name in files:
                if '.jpg' in name.lower() or '.jpeg' in name.lower() or '.bmp' in name.lower() or 'tiff' in name.lower() or '.png' in name.lower():
                    data[name] = io.imread(path.join(root, name))
        return data

    # Procesar la imagen con umbral adaptativo
    def processImageAdaptative(self,img, clipLimit, nb, w, removeSmall, conn,blockSize):
        """
        Apply some filters to the given image. Include adaptative threshold.
        :param img: image to process
        :param clipLimit: clip_limit parameter (0,1)
        :param nb: nbins parameter (0,100)
        :param w: weight parameter (0,1)
        :param removeSmall: min_size parameter (0,100)
        :param conn: connectivity parameter (0,100)
        :return: processed image
        """
        result = None
        # disable warnings
        with war.catch_warnings():
            war.simplefilter("ignore")
            # Convert image to grey scale
            img = rgb2grey(img)
            # Equalize histogram
            imgAdapted = exposure.equalize_adapthist(img, clip_limit=clipLimit, nbins=nb)
            # Reduce noise
            imgAdaptedDenoise = denoise_tv_chambolle(imgAdapted, weight=w)
            # Apply Prewitt
            pwt = prewitt(imgAdaptedDenoise)
            # Apply frangi filter
            frangiImg = frangi(pwt)
            # Calculate image threshold
            threshold = threshold_adaptive(frangiImg,blockSize)
            # Apply threshold
            thresholdedImg = frangiImg >= threshold
            # Skeletonize image
            skImg = skeletonize(thresholdedImg)
            # Remove small objects (min_size fixed to 70)
            result = morphology.remove_small_objects(skImg, min_size=removeSmall, connectivity=conn)
            # Show result
            plt.figure()
            plt.imshow(result, cmap='gray')
        return result

    # Probar filtro sobel
    def sobelY(self, img):
        # dx = ndimage.sobel(img, 0, mode='constant', cval=0.0)  # horizontal derivative
        dy = sobel(img, 1, mode='constant', cval=0.0)  # vertical derivative
        # mag = np.hypot(dx, dy)  # magnitude
        # mag *= 255.0 / np.max(mag)  # normalize (Q&D)
        return dy

    # Probar filtro sobel
    def sobelX(self, img):
        dx = sobel(img, 0, mode='constant', cval=0.0)  # horizontal derivative
        # mag = np.hypot(dx, dy)  # magnitude
        # mag *= 255.0 / np.max(mag)  # normalize (Q&D)
        return dx

    def sobelEntero(self, img):
        dx = sobel(img, 0, mode='constant', cval=0.0)  # horizontal derivative
        dy = sobel(img, 1, mode='constant', cval=0.0)  # vertical derivative
        mag = np.hypot(dx, dy)  # magnitude
        mag *= 255.0 / np.max(mag)  # normalize (Q&D)
        return mag


    # Funciones para mostrar la imagen y las lineas encontradas
    '''
    Muesta imagen con los segmentos superpuestos
    '''
    def mostrar(self, imagen, segmentos, name, guardar=False, path=None):
        alto = len(imagen)
        ancho = len(imagen[0])
        plt.figure(figsize=(ancho / 50, alto / 50), dpi=300)
        plt.axis('off')
        plt.imshow(imagen, cmap=plt.cm.gray)
        for line in segmentos:
            p0, p1 = line
            plt.plot((p0[0], p1[0]), (p0[1], p1[1]), 'b', linewidth=1)
        if guardar==True:
            fig = plt.gcf()
            self.guardarFigura(fig, name, path)


    """
    Guarda la figura en la carpeta especificada
    """
    def guardarFigura(self, fig, name, path):
        fig.savefig(path + str(name[0]) + '-' + str(name[1]) + '-' + str(name[2]) + '.png', dpi=300, frameon=False, bbox_inches='tight', pad_inches=0.0)

    """
    Guarda una imagen
    """
    def guardarImagen(self, img, path):
        misc.toimage(img, cmin=False, cmax=True).save(path)

    """
    Muestra una imagen. Solo para el diente 2 y lo muestra en grande
    """
    def mostrarImagen(self, img):
        alto = len(img)
        ancho = len(img[0])
        plt.figure(figsize=(ancho / 50, alto / 50), dpi=300)
        plt.imshow(img, cmap='gray')

    """
        Muestra una imagen normal y la guarda
        """
    def mostrarYGuardarImagen(self, img, mostrar=True,title=None,path=None, guardar=False):
        if mostrar==True:
            plt.figure()
            if title!=None:
                plt.title(title)
            plt.imshow(img, cmap='gray')
        if guardar==True:
            misc.toimage(img, cmin=False, cmax=True).save(path)

    '''
    Muestra el segmento i-esimo
    '''

    def mostrarI(self,imagen, segmentos, i):
        plt.figure(figsize=(12, 12))
        plt.imshow(imagen, cmap=plt.cm.gray)
        for line in segmentos:
            p0, p1 = line
            plt.plot((p0[0], p1[0]), (p0[1], p1[1]), 'b', linewidth=1)

        p0, p1 = segmentos[i]
        plt.plot((p0[0], p1[0]), (p0[1], p1[1]), 'r', linewidth=2)
        # fig.savefig('foo.jpg',bbox_inches='tight')

    '''

    Muestra imagen con los segmentos combinados (de multiples segmentos se crea uno que los une a todos)
    Solo muestra el segmento combinado i-esimo
    '''

    def mostrarK(self,imagen, segmentos, k_components, index):
        plt.figure(figsize=(12, 12))
        plt.imshow(imagen, cmap=plt.cm.gray)

        segToShow = list(map(lambda x: segmentos[x], k_components[1][index]))

        for line in segToShow:
            p0, p1 = line
            plt.plot((p0[0], p1[0]), (p0[1], p1[1]), 'b', linewidth=1)

        procesado = Procesado()
        combinado = procesado.combinaSegmentos(segToShow)
        p0, p1 = combinado
        plt.plot((p0[0], p1[0]), (p0[1], p1[1]), 'r', linewidth=3)