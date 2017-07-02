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

import numpy as np
import warnings as war

from os import walk
from os import path
from scipy.ndimage import convolve
from skimage import io
from skimage.color import grey2rgb
from skimage.color import rgb2gray
from skimage.draw import line
from skimage.exposure import equalize_adapthist
from skimage.filters import threshold_adaptive
from skimage.morphology import skeletonize_3d, remove_small_objects
from skimage.restoration import denoise_tv_chambolle
from skimage.transform import probabilistic_hough_line

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

        #self.kernels = [self.W, self.NW, self.NE, self.N,
        #                self.SW, self.S, self.SE,
        #                self.E]
        self.kernels = [self.W, self.NW, self.NE,
                        self.E, self.SE, self.SW,
                        self.S, self.N]

    def saveWithLineDetection(self, img, imgSk, lines, savePath, savePathOverlap):
        """
        Saves images which have been applied lines detection.
        :param img: original image
        :param imgSk: skeletonized image
        :param lines: lines detected
        :param savePath: skeletonize image save path
        :param savePathOverlap: overlapped image save path
        :return: None
        """
        imgSkRGB = grey2rgb(imgSk)
        imgOverlapped = np.copy(img)
        for coord in lines:
            rr, cc = line(coord[0][1], coord[0][0], coord[1][1], coord[1][0])
            imgSkRGB[rr, cc] = [255, 0, 0]
            # If png image, len is 4, else 3
            imgOverlapped[rr, cc] = [255, 0, 0, 255] if (len(img[0][0]) == 4) else [255, 0, 0]

        # Save images
        io.imsave(savePath, imgSkRGB)
        io.imsave(savePathOverlap, imgOverlapped)

    def saveWithoutLineDetection(self, img, imgSk, savePath, savePathOverlap):
        """
        Saves images which have not been applied lines detection.
        :param img: original image
        :param imgSk: skeletonized image
        :param savePath: skeletonize image save path
        :param savePathOverlap: overlapped image save path
        :return: None
        """

        # PNG IMAGE
        imgOverlapped = np.copy(img)
        if (len(img[0][0]) == 4):
            imgSkRGB = np.full_like(img, [0, 0, 0, 255])
            imgSkRGB[imgSk == 255] = [255, 0, 0, 255]
            imgOverlapped[imgSk == 255] = [255, 0, 0, 255]
        else:
            # Other types of image
            imgSkRGB = np.full_like(img, [0, 0, 0])
            imgSkRGB[imgSk == 255] = [255, 0, 0]
            imgOverlapped[imgSk == 255] = [255, 0, 0]

        # Save images
        io.imsave(savePath, imgSkRGB)
        io.imsave(savePathOverlap, imgOverlapped)

    def loadImage(self, path):
        """
        Loads an image.
        :param path: image path to load
        :return: Loaded Image
        """
        return io.imread(path)

    def loadImagesFrom(self, package):
        """
        Load a bunch of images.
        :param package: folder path
        :return: dictionary with image loaded
        """
        data = dict()
        for root, dirs, files in walk(package):
            for name in files:
                if '.jpg' in name.lower() or '.jpeg' in name.lower() or '.bmp' in name.lower() or 'tiff' in name.lower() or '.png' in name.lower():
                    data[name] = io.imread(path.join(root, name))
        return data

    """
    def showImage(self, img, returnImg=False):
        Shows an image. Uncomment this function and use it with Jupyter Notebooks.
        :param img: image to show
        :param returnImg: image figure option
        :return: image figure

        fig = plt.figure(figsize=(20, 20))
        plt.imshow(img, cmap=plt.cm.gray)
        if returnImg == True:
            return fig
    """

    def prepareImage(self, img, clip=0.0, nb=100, w=0.5):
        """
        Prepare an image applying filters.
        :param img: orignal image
        :param clip: contrast force
        :param nb: gray bins
        :param w: denoise force
        :return: prepared image
        """
        with war.catch_warnings():
            war.simplefilter("ignore")
            imgGray = rgb2gray(img)
            imgAdapted = equalize_adapthist(imgGray, clip_limit=clip, nbins=nb)
            imgDenoise = denoise_tv_chambolle(imgAdapted, weight=w)
        return imgDenoise

    def deleteSmallObjects(self, img, minLength=30, conn=50):
        """
        Deletes small objects along the image.
        :param img: image for removing objects
        :param minLength: minimum length for an object to be removed
        :param conn: neighborhood of a pixel to determine the object to remove
        :return: image with small objects removed
        """
        img_2 = remove_small_objects(img, minLength, connectivity=conn)
        img_3 = ~np.array(img_2)
        img_4 = remove_small_objects(img_3, minLength, connectivity=conn)
        return ~np.array(img_4)

    def binarizeImage(self, img):
        """
        Binarized an image.
        :param img: image to binarized
        :return: image binarized
        """
        t = threshold_adaptive(img, 1)
        return img >= t

    def kirschProcessing(self, img, kernelId=0, angles=np.linspace(-0.3, 0.3, num=600), lineLength=30, lineGap=16,
                         minLengthSmallObjects=30, conn=50, lineDetection=True):
        """
        Applys kirsch filtering and line detection to a prepared image.
        :param img: prepared image
        :param kernelId: id of the kirsch kernel to apply
        :param angles: angles for detecting lines
        :param lineLength: minimum line length to detect
        :param lineGap: maximum gap between pixel in the image to form a line
        :param minLengthSmallObjects: minimum length for an object to be removed
        :param conn: neighborhood of a pixel to determine the object to remove
        :param lineDetection: true / false if the user want to detect lines
        :return: processed image and lines detected
        """
        # with war.catch_warnings():
        # war.simplefilter("ignore")
        # Aqui aplicamos el kernel de kirsch
        imgConvolve = convolve(img, self.kernels[kernelId])
        imgBin = self.binarizeImage(imgConvolve)
        imgRemoveSmall = self.deleteSmallObjects(imgBin, minLengthSmallObjects, conn)
        imgSkeletonize3D = skeletonize_3d(imgRemoveSmall)

        lines = None
        if (lineDetection == True):
            # Detect lines
            lines = probabilistic_hough_line(imgSkeletonize3D, threshold=0, line_length=lineLength,
                                             line_gap=lineGap,
                                             theta=angles)
        return [imgSkeletonize3D, lines]
