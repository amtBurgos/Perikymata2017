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
        # Apply frangi filter
        frangiImg = frangi(imgAdaptedDenoise)
        # Calculate image threshold
        threshold = threshold_li(frangiImg)
        # Apply threshold
        thresholdedImg = frangiImg >= threshold
        # Skeletonize image
        skImg = skeletonize(thresholdedImg)
        # Remove small objects (min_size fixed to 70)
        result = morphology.remove_small_objects(skImg, min_size=removeSmall, connectivity=100)
    return result


if __name__ == "__main__":
    imgPath = sys.argv[1]
    img = io.imread(imgPath)
    result = processImage(img)
    savePath = sys.argv[2]
    misc.toimage(result, cmin=False, cmax=True).save(savePath)
