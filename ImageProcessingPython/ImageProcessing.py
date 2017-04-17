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
"""
@author: Andrés Miguel Terán
@version: 1.0
"""
import sys
from skimage import io
from skimage.color import rgb2grey
from skimage import exposure
from skimage.restoration import denoise_tv_chambolle
from _frangi import frangi, hessian
from skimage.filters import threshold_li
from skimage import morphology
from skimage.morphology import skeletonize
import warnings as war
from scipy import misc
import matplotlib.pyplot as plt


def processImage(img, removeSmall=0):
    """
    Apply some filters to the given image.
    :param img: image to process
    @type img: string
    :return: processed image
    @type return: result image
    """
    result = None
    # disable warnings
    with war.catch_warnings():
        war.simplefilter("ignore")
        # Convert image to grey scale
        img = rgb2grey(img)
        # Equalize histogram
        imgAdapted = exposure.equalize_adapthist(img, clip_limit=0.91, nbins=100)
        # Reduce noise
        imgAdaptedDenoise = denoise_tv_chambolle(imgAdapted, weight=0.1)
        plt.figure()
        plt.imshow(imgAdaptedDenoise,cmap='gray')

        # Apply frangi filter
        frangiImg = frangi(imgAdaptedDenoise)
        plt.figure()
        plt.imshow(frangiImg, cmap='gray')
        # Calculate image threshold
        threshold = threshold_li(frangiImg)
        # Apply threshold
        thresholdedImg = frangiImg >= threshold
        plt.figure()
        plt.imshow(thresholdedImg, cmap='gray')
        # Skeletonize image
        skImg = skeletonize(thresholdedImg)
        plt.figure()
        plt.imshow(skImg, cmap='gray')
        # Remove small objects (min_size fixed to 70)
        result = morphology.remove_small_objects(skImg, min_size=removeSmall, connectivity=100)
    return result


from skimage.feature import hessian_matrix, hessian_matrix_eigvals
from skimage import img_as_float
from scipy.ndimage import gaussian_filter
from skimage.morphology import reconstruction
from skimage import morphology
from skimage.morphology import binary_erosion, diamond
from skimage.morphology import skeletonize


def tratamientoAlternativo(img):
    """
    Forma Ismael
    @param img: imagen de muestra a comparar con la máscara.
    @return no_smallF: imagen esqueletonizada sin objetos pequeños.
    """
    with war.catch_warnings():
        war.simplefilter("ignore")
        img = rgb2grey(img)
        imgAdapted = exposure.equalize_adapthist(img, clip_limit=0.91, nbins=100)
        imgAdaptedDenoise = denoise_tv_chambolle(imgAdapted, weight=0.1)

        hxx, hxy, hyy = hessian_matrix(imgAdaptedDenoise, sigma=1.85, mode='wrap', cval=0.11)
        i1, i2 = hessian_matrix_eigvals(hxx, hxy, hyy)  # @UnusedVariable

        edges = img_as_float(i1)
        filtered = gaussian_filter(edges, 1)

        thresh3 = threshold_li(edges)
        thresholded3 = edges >= thresh3

        no_small = morphology.remove_small_objects(thresholded3, min_size=155, connectivity=100)
        selem = diamond(1.9)
        dil = binary_erosion(no_small, selem, out=None)
        skl = skeletonize(dil)
        no_small2 = morphology.remove_small_objects(skl, min_size=55, connectivity=100)

    return no_small2


if __name__ == "__main__":
    #imgPath = sys.argv[1]
    imgPath = 'Images\\FullImages\\Full_Image.png'
    img = io.imread(imgPath)
    processImage(img)
    #result = processImage(img)
    #savePath = sys.argv[2]
    #
    # misc.toimage(result, cmin=False, cmax=True).save(savePath)
