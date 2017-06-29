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
import unittest
from ImageProcessing import processImage
from os import walk
from os import path
from os import makedirs
from os import chmod
from skimage import io
from scipy import misc


class ImageProcessingTest(unittest.TestCase):
    """
    Class for testing that the processing function works
    and that the images can be obtained. It is needed a visual
    inspection by the tester after obtain the images.

    @author: Andrés Miguel Terán
    @version: 1.0
    """

    def setUp(self):
        """
        Prepare the folder structure for storage the result images.
        """
        counter = 0
        directories = ['TestResults',
                       'TestResults\\Fragments',
                       'TestResults\\FullImages',
                       'TestResults\\RemoveSmallObjects',
                       'TestResults\\RemoveSmallObjects\\Filtered',
                       'TestResults\\RemoveSmallObjects\\Original']
        for directory in directories:
            if not path.exists(directory):
                makedirs(directory)
                chmod(directory, 0o666)
            counter += 1
        self.assertEquals(counter, 6)

    def general_test(self):
        """
        Applies the new image processing method for:
         - Image fragments from teeth 1
         - Full image of teeth 1 made by the stitching application.
         - Full image of teeth 1 filtered by the old method.
        """
        resultFullImagePath = 'TestResults\\FullImages\\'
        resultFragmentsPath = 'TestResults\\Fragments\\'
        counter = 0
        for root, dirs, files in walk('Images\\'):
            for file in files:
                if 'tif' in file or 'png' in file:
                    imgPath = path.join(root, file)
                    img = io.imread(imgPath)
                    result = processImage(img)
                    savePath = ''
                    if 'FullImages' in imgPath:
                        savePath += resultFullImagePath + file
                    else:
                        savePath += resultFragmentsPath + file
                    misc.toimage(result, cmin=False, cmax=True).save(savePath)
                    counter += 1
        self.assertEquals(counter, 7)

    def remove_small_objects_test(self):
        """
        Applies the new image processing method by changing the
        'min-size-remove-small-objets' parameter ten in ten for:
         - Full image of teeth 1 made by the stitching application.
         - Full image of teeth 1 filtered by the old method.
        """
        resultSmallObjectsOriginal = 'TestResults\\RemoveSmallObjects\\Original\\'
        resultSmallObjectsFiltered = 'TestResults\\RemoveSmallObjects\\Filtered\\'
        counter = 0
        for root, dirs, files in walk('Images\\'):
            for file in files:
                if ('Full_Image.png' in file) or ('Filtered_Image.png' in file):
                    imgPath = path.join(root, file)
                    img = io.imread(imgPath)
                    for min_size in range(0, 110, 10):
                        result = processImage(img, min_size)
                        savePath = ''
                        if 'Full_Image.png' in file:
                            savePath += resultSmallObjectsOriginal
                        elif 'Filtered_Image.png' in file:
                            savePath += resultSmallObjectsFiltered
                        savePath += str(min_size) + '_' + file
                        misc.toimage(result, cmin=False, cmax=True).save(savePath)
                        counter += 1
        self.assertEquals(counter, 22)


#if __name__ == '__main__':
#    unittest.main()

# prepareEnvironment()
# GeneralTest()
# RemoveSmallObjectsTest()
