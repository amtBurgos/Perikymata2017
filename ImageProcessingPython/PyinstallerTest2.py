
from skimage import io
from skimage.color import rgb2grey
from skimage import exposure
from skimage.restoration import denoise_tv_chambolle
from _frangi import frangi, hessian
from skimage.filters import threshold_li
from skimage.morphology import skeletonize
import warnings as war
from scipy import misc
import sys
from skimage import morphology
from os import startfile


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
    savePath = sys.argv[2]
    removeSmall = 0
    if (len(sys.argv)==4):
        removeSmall = sys.argv[3]
    img = io.imread(imgPath)
    print('Procesando...')
    result = processImage(img,removeSmall)
    misc.toimage(result, cmin=False, cmax=True).save(savePath)
    print('proceso terminado')
    startfile(savePath)
