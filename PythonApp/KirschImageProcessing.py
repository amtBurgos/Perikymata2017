"""
  License: GPL

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
"""

from skimage import io
from os import walk
from os import path

from skimage.color import rgb2grey
from skimage import exposure
from skimage.restoration import denoise_tv_chambolle

from skimage.filters import threshold_adaptive
from skimage.color import rgb2gray
from skimage.morphology import skeletonize_3d, remove_small_objects, skeletonize
from scipy.ndimage import convolve
from skimage.transform import probabilistic_hough_line
import warnings as war
import matplotlib.pyplot as plt
import numpy as np
from skimage.draw import line
from skimage.color import grey2rgb
from skimage.io import imsave


class KirschImageProcessing():
    def __init__(self):
        # kernelG1
        self.N = np.array([[5, 5, 5],
                           [-3, 0, -3],
                           [-3, -3, -3]], dtype=np.float32)

        # kernelG2
        self.NW = np.array([[5, 5, -3],
                            [5, 0, -3],
                            [-3, -3, -3]], dtype=np.float32)

        # kernelG3
        self.W = np.array([[5, -3, -3],
                           [5, 0, -3],
                           [5, -3, -3]], dtype=np.float32)

        # kernelG4
        self.SW = np.array([[-3, -3, -3],
                            [5, 0, -3],
                            [5, 5, -3]], dtype=np.float32)

        # kernelG5
        self.S = np.array([[-3, -3, -3],
                           [-3, 0, -3],
                           [5, 5, 5]], dtype=np.float32)

        # kernelG6
        self.SE = np.array([[-3, -3, -3],
                            [-3, 0, 5],
                            [-3, 5, 5]], dtype=np.float32)

        # kernelG7
        self.E = np.array([[-3, -3, 5],
                           [-3, 0, 5],
                           [-3, -3, 5]], dtype=np.float32)

        # kernelG8
        self.NE = np.array([[-3, 5, 5],
                            [-3, 0, 5],
                            [-3, -3, -3]], dtype=np.float32)

        self.kernels = [self.N, self.NW, self.W,
                        self.SW, self.S, self.SE,
                        self.E, self.NE]

    def saveFilteredImage(self, imgSk, lines, savePath):
        """
        Saves the image to the specified folder.
        :param imgSk: image skeletonized
        :param lines: lines detedted
        :param savePath: save path
        :return: None
        """
        img = grey2rgb(imgSk)
        for coord in lines:
            rr, cc = line(coord[0][1], coord[0][0], coord[1][1], coord[1][0])
            img[rr, cc] = [255, 0, 0]
        imsave(savePath, img)

    def saveOverlappedImage(self, img, lines, savePathOverlap):
        """
        Overlap the lines detected into the original image
        :param img: original image
        :param lines: lines detected
        :param savePathOverlap:
        """
        imgOverlapped = np.copy(img)
        if len(img[0][0]) == 4:
            # PNG image with RGBA Channel
            for coord in lines:
                rr, cc = line(coord[0][1], coord[0][0], coord[1][1], coord[1][0])
                imgOverlapped[rr, cc] = [255, 0, 0, 255]
            print('fin')
        elif len(img[0][0]) == 3:
            # NORMAL IMAGE with RGB Channel
            for coord in lines:
                rr, cc = line(coord[0][1], coord[0][0], coord[1][1], coord[1][0])
                imgOverlapped[rr, cc] = [255, 0, 0]

        imsave(savePathOverlap, imgOverlapped)

    def saveWithoutLineDetection(self, img, imgSk, savePath, savePathOverlap):
        # Save skeletonize
        imgSkAux = list()
        [imgSkAux.append([]) for height in range(len(imgSk))]

        for i in range(len(imgSk)):
            for j in range(len(imgSk[0])):
                if (imgSk[i][j] == 0):
                    imgSkAux[i].append(np.array([0, 0, 0, 255], dtype=np.uint8))
                else:
                    imgSkAux[i].append(np.array([255, 0, 0, 255], dtype=np.uint8))

        #Save detected
        imsave(savePath, imgSkAux)

        #Save overlapped
        #imgOverlapped = np.copy(img)

        imgOverlapped = list()
        [imgOverlapped.append([]) for height in range(len(img))]
        if len(img[0][0]) == 4:
            # PNG image with RGBA Channel
            for i in range(len(imgSk)):
                for j in range(len(imgSk[0])):
                    if (imgSk[i][j] != 0):
                        #imgOverlapped[i][j] = np.array([255, 0, 0, 255], np.uint8)
                        imgOverlapped[i].append(np.array([255, 0, 0, 255], dtype=np.uint8))
                    else:
                        imgOverlapped[i].append(img[i][j])

        elif len(img[0][0]) == 3:
            # NORMAL IMAGE with RGB Channel
            for i in range(len(imgSk)):
                for j in range(len(imgSk[0])):
                    if (imgSk[i][j] == 1):
                        #imgOverlapped[i][j] = np.array([255, 0, 0], np.uint8)
                        imgOverlapped[i].append(np.array([255, 0, 0], dtype=np.uint8))
                    else:
                        imgOverlapped[i].append(img[i][j])
        #Save overlapped
        imsave(savePathOverlap, imgOverlapped)

    def loadImage(self, path):
        return io.imread(path)

    def loadImagesFrom(self, package):
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

    def showImage(self, img, returnImg=False):
        fig = plt.figure(figsize=(20, 20))
        plt.imshow(img, cmap=plt.cm.gray)
        if returnImg == True:
            return fig

    def prepareImage(self, img, clip=0.0, nb=100, w=0.5):
        with war.catch_warnings():
            war.simplefilter("ignore")
            imgGray = rgb2gray(img)
            imgAdapted = exposure.equalize_adapthist(imgGray, clip_limit=clip, nbins=nb)
            imgDenoise = denoise_tv_chambolle(imgAdapted, weight=w)
        return imgDenoise

    def deleteSmallObjects(self, img, minLength=30, conn=50):
        img_2 = remove_small_objects(img, minLength, connectivity=conn)
        img_3 = ~np.array(img_2)
        img_4 = remove_small_objects(img_3, minLength, connectivity=conn)
        return ~np.array(img_4)

    def binarizeImage(self, img):
        t = threshold_adaptive(img, 1)
        return img >= t

    def kirschProcessing(self, img, kernelId=1, angles=np.linspace(-0.3, 0.3, num=600), lineLength=30, lineGap=16,
                         minLengthSmallObjects=30, conn=50, lineDetection=True):
        #with war.catch_warnings():
            #war.simplefilter("ignore")
            # Aqui aplicamos el kernel de kirsch
        imgConvolve = convolve(img, self.kernels[kernelId])
        imgBin = self.binarizeImage(imgConvolve)
        imgRemoveSmall = self.deleteSmallObjects(imgBin, minLengthSmallObjects, conn)
        imgSkeletonize3D = skeletonize_3d(imgRemoveSmall)

        lines = None
        if (lineDetection == True):
            # Detect lines
            lines = probabilistic_hough_line(imgSkeletonize3D, threshold=0, line_length=lineLength,line_gap=lineGap, theta=angles)
        return [imgSkeletonize3D, lines]

    def kirschProcessing1D(self, img, kernelId=3, angles=np.linspace(-0.3, 0.3, num=600), lineLength=30, lineGap=16,
                           minLength=30, conn=50):
        with war.catch_warnings():
            war.simplefilter("ignore")
            # Aqui aplicamos el kernel de kirsch
            imgConvolve = convolve(img, self.kernels[kernelId])
            imgBin = self.binarizeImage(imgConvolve)
            imgRemoveSmall = self.deleteSmallObjects(imgBin, minLength, conn)
            imgSkeletonize = skeletonize(imgRemoveSmall)

            # Detectar lineas
            lines = probabilistic_hough_line(imgSkeletonize, threshold=0, line_length=lineLength, line_gap=lineGap,
                                             theta=angles)
        return [imgSkeletonize, lines]
