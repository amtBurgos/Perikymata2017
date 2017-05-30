package es.ubu.lsi.perikymata.util;

/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import es.ubu.lsi.perikymata.MainApp;
import ij.ImagePlus;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

/**
 * Util class for processing image and find perikymata.
 *
 * @author Sergio Chico Carrancio
 *
 */
public class ProfileUtil {

	/**
	 * Gets the orthogonal line of a point based on the previous and next
	 * points, ordered from side to side.
	 *
	 * @param pointsVector
	 *            List of all the points used to get the profile.
	 * @param index
	 *            index of the current point.
	 * @param length
	 *            length of the orthogonal line.
	 * @return array of the points of the orthogonal line that surrounds the
	 *         point. Output is ordered from side to side.
	 */
	private static int[][] ortogonalLineOfAPoint(List<int[]> pointsVector, int index, int length) {
		// distances are dx=x2-x1 and dy=y2-y1, then the normals are (-dy, dx)
		// and (dy, -dx)
		int[] point = pointsVector.get(index);
		int[] prev;
		int[] follow;

		if (index == 0) {
			prev = point;
			follow = pointsVector.get(index + 1);
		} else if (index == ((pointsVector.size()) - 1)) {
			prev = pointsVector.get(index - 1);
			follow = point;
		} else {
			prev = pointsVector.get(index - 1);
			follow = pointsVector.get(index + 1);
		}

		int dx = follow[0] - prev[0];
		int dy = follow[1] - prev[1];
		int[] v1 = { -dy, dx };
		int[] v2 = { dy, -dx };

		// Array of coords
		int[][] orthogonal = new int[2 * length + 1][];
		int ortIndex = 0;

		// point -> point + (v1 or v2) are the orthonormal vectors to the
		// prev -> follow vector, so multiplying by "i" we can get all the
		// points in these two directions, chess' "horse" movements can give
		// farther coordinates with a lower max length.

		// First side vector.
		for (int i = length; i >= 0; i--) {
			orthogonal[ortIndex] = new int[] { i * v2[0] + point[0], i * v2[1] + point[1] };
			ortIndex++;
		}
		// Original point
		orthogonal[ortIndex] = point;
		ortIndex++;
		// Other side vector.
		for (int i = 1; i < length; i++) {
			orthogonal[ortIndex] = new int[] { i * v1[0] + point[0], i * v1[1] + point[1] };
			ortIndex++;
		}

		return orthogonal;
	}

	/**
	 * Gets the intensity matrix of the line drawn by the user and its
	 * surrounding region using the orthogonal vector.
	 *
	 * @param image
	 *            Image to get the Intensity matrix of.
	 * @param pointsVector
	 *            List of points of the image that are going to be used to
	 *            calculate the orthogonal matrix.
	 * @param length
	 *            Number of pixels to get from each side.
	 * @return Matrix(List of Lists) that contains the intensity of the pixels
	 *         surrounding the pointsVector.
	 */
	private static List<List<Integer>> getOrthogonalIntensityMatrix(BufferedImage image, List<int[]> pointsVector,
			int length) {
		List<List<Integer>> intensityMatrix = new LinkedList<>();
		List<int[][]> orthogonalLine = getOrthogonalCoordinatesMatrix(pointsVector, length);

		for (int i = 0; i < orthogonalLine.size(); i++) {
			intensityMatrix.add(getOrthogonalIntensity(image, orthogonalLine.get(i)));
		}
		return intensityMatrix;
	}

	/**
	 * Gets a matrix with the coordinates surrounding the pointsVector line
	 * using the orthogonal vector of each side.
	 *
	 * @param pointsVector
	 *            List of points of the image that are going to be used to
	 *            calculate the orthogonal matrix.
	 * @param length
	 *            Length of each sides' orthogonal vector.
	 * @return Coordinates of the elements of the orthogonal line, containing in
	 *         each position of the list an array of each orthogonal line, with
	 *         the x-y coordinates
	 */
	private static List<int[][]> getOrthogonalCoordinatesMatrix(List<int[]> pointsVector, int length) {
		List<int[][]> orthogonalLine = new LinkedList<>();
		for (int i = 0; i < pointsVector.size(); i++) {
			orthogonalLine.add(ortogonalLineOfAPoint(pointsVector, i, length));
		}
		return orthogonalLine;
	}

	/**
	 * Gets the intensity of each point of the orthogonal vector.
	 *
	 * @param image
	 *            Filtered image to calculate the profile of.
	 * @param orthogonalLine
	 *            Points of the orthogonal vector.
	 * @return Mean of the intensity of the orthogonal vector.
	 */
	private static List<Integer> getOrthogonalIntensity(BufferedImage image, int[][] orthogonalLine) {
		List<Integer> ret = new ArrayList<>();
		for (int[] point : orthogonalLine) {
			// We assume that the image will be in grayScale (8bit) or rgb (24
			// bits) but
			// all the three channels having the same value. r = g = b.
			// gray = r+g+b/3, so if they are the same gray= 3b/b=b, and blue
			// being the
			// 8 rightmost bits, we can take these same 8 bits for
			// both RGB and GrayScale images.
			ret.add(image.getRGB(point[0], point[1]) & 0xFF);
		}
		return ret;

	}

	///////// DONT DELETE AMT /////////////
	/**
	 * Calculates the Bresenham distance between two points. That is, a straight
	 * line using Cartesian coordinates.
	 *
	 * @param x0
	 *            X coordinate of Starting point.
	 * @param y0
	 *            Y coordinate of Starting point.
	 * @param x1
	 *            X coordinate of Ending point.
	 * @param y1
	 *            Y coordinate of Ending point.
	 * @return A list of the coordinates between the starting point and The
	 *         ending point.
	 */
	private static List<int[]> Bresenham(int x0, int y0, int x1, int y1) {
		int x, y, dx, dy, p, incE, incNE, stepx, stepy;
		dx = (x1 - x0);
		dy = (y1 - y0);
		List<int[]> llist = new LinkedList<>();

		// gets the start and end points
		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}

		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}

		x = x0;
		y = y0;
		// iterates to the end of the line
		if (dx > dy) {
			p = 2 * dy - dx;
			incE = 2 * dy;
			incNE = 2 * (dy - dx);
			while (x != x1) {
				x = x + stepx;
				if (p < 0) {
					p = p + incE;
				} else {
					y = y + stepy;
					p = p + incNE;
				}
				llist.add(new int[] { x, y });
			}
		} else {
			p = 2 * dx - dy;
			incE = 2 * dx;
			incNE = 2 * (dx - dy);
			while (y != y1) {
				y = y + stepy;
				if (p < 0) {
					p = p + incE;
				} else {
					x = x + stepx;
					p = p + incNE;
				}
				llist.add(new int[] { x, y });
			}
		}
		return llist;
	}

	///////// DONT DELETE AMT /////////////
	/**
	 * Uses the pathList of drawn line to get all the pixels that are under the
	 * line.
	 *
	 * @return List of coordinates of the pixels under the line.
	 */
	public static List<int[]> getProfilePixels(List<PathElement> freeDrawPathList) {
		LinkedList<int[]> profile = new LinkedList<>();
		int x0 = (int) ((MoveTo) (freeDrawPathList.get(0))).getX();
		int y0 = (int) ((MoveTo) (freeDrawPathList.get(0))).getY();
		profile.add(new int[] { x0, y0 });
		int x1;
		int y1;
		for (int i = 1; i < freeDrawPathList.size(); i++) {
			x1 = (int) ((LineTo) (freeDrawPathList.get(i))).getX();
			y1 = (int) ((LineTo) (freeDrawPathList.get(i))).getY();
			profile.addAll(Bresenham(x0, y0, x1, y1));
			x0 = x1;
			y0 = y1;
		}
		return profile;
	}

	/**
	 * Returns the mean of the intensity profile with a width of two pixels at
	 * each side by using the orthogonal vectors of the given the coordinates of
	 * a line.
	 *
	 * @param profileCoords
	 *            Coordinates of a single-pixeled line.
	 * @param mainapp
	 *            Reference to the main application to get the images.
	 * @return intensity profile
	 */
	public static List<Integer> getIntensityProfile(List<int[]> profileCoords, MainApp mainapp) {

		// DONT NEEDED
		BufferedImage original = SwingFXUtils.fromFXImage(mainapp.getFullImage(), null);
		// DONT NEEDED
		BufferedImage prewitt = SwingFXUtils.fromFXImage(mainapp.getFilteredImage(), null);

		// WILL NEED
		// BufferedImage filtered =
		// SwingFXUtils.fromFXImage(mainapp.getFilteredImage(), null);

		int[] roi = getRoi(profileCoords);

		// APLICAR ROI A LA IMAGEN FILTRADA PARA DELIMITAR BIEN LA IMAGEN POR SI
		// ACASO NOS PASAMOS CON LA LINEA
		BufferedImage clahe = executeClahe(roi[0], roi[1], roi[2], roi[3], original);
		return getPrewittCLAHEProfile(prewitt, clahe, profileCoords);
	}

	/**
	 * Finds the list of maximum local intensities in the profile. If there are
	 * two or more points next to each other with the same intensity, the middle
	 * point is taken.
	 *
	 * @param profile
	 *            Intensity profile.
	 * @param threshold
	 *            minimum value for a peak to be treated as a perikymata
	 * @return List of the indexes where perikymata has been found.
	 */
	public static List<Integer> findLocalPeaks(List<Integer> profile, double threshold) {
		int l = profile.size();
		int lastMaxIndex = 0;
		int lastMaxValue = 0;
		List<Integer> peaks = new ArrayList<>();

		for (int i = 0; i < profile.size() - 1; i++) {

			if (profile.get(i) > lastMaxValue) {
				// Intensity is growing
				lastMaxValue = profile.get(i);
				lastMaxIndex = i;
			} else if (profile.get(i) < lastMaxValue) {
				// Intensity is geting lower, so there is a local max.

				// local max is only stored if is higher than the threshold.
				// If There is more than one consecutive max value, the mid
				// point is stored.
				if (profile.get(i - 1) >= threshold) {
					if (lastMaxIndex == i - 1) {
						peaks.add(i - 1);
					} else {
						peaks.add((i + lastMaxIndex) / 2);
					}

				}
				// index is set to a known value.
				lastMaxValue = 0;
				lastMaxIndex = 0;
			}
		}
		// Check if the profile ends with one or more maxes.
		if (profile.get(l - 1) != 0) {
			if (profile.get(l - 1) == lastMaxValue)
				peaks.add(((l - 1) + lastMaxIndex) / 2);
			else if (profile.get(l - 2) < profile.get(l - 1))
				peaks.add(l - 1);
		}
		return peaks;
	}

	private static BufferedImage executeClahe(int x1, int y1, int x2, int y2, BufferedImage im) {
		ImagePlus ip = new ImagePlus();
		ip.setImage(im);
		int width = Math.max(x2, x1) - Math.min(x2, x1);
		int height = Math.max(y2, y1) - Math.min(y2, y1);
		// Maybe mask on last argument can be used to apply convolve only to
		// desired pixels.
		return CLAHE_.run(ip, 63, 255, 3, new Rectangle(x1, y1, width, height), null);
		// fullImage.setImage(SwingFXUtils.toFXImage(res, null));
	}

	///////// DONT DELETE AMT/////////////
	/**
	 * Gets the minimum square that where it can fit the free drawn line, used
	 * to apply CLAHE on the minimum possible region.
	 *
	 * @param pointsVector
	 *            Coordinates of the drawn line.
	 * @return Coordinates Of the top left point of the square(1) and the bottom
	 *         right points of the square(2) as follows: [x1,y1,x2,y2]
	 */
	private static int[] getRoi(List<int[]> pointsVector) {
		int x1 = Integer.MAX_VALUE, x2 = 0;
		int y1 = Integer.MAX_VALUE, y2 = 0;
		for (int[] coords : pointsVector) {
			if (coords[0] < x1) {
				x1 = coords[0];
			} else if (coords[0] > x2) {
				x2 = coords[0];
			}

			if (coords[1] < y1) {
				y1 = coords[1];
			} else if (coords[1] > y2) {
				y2 = coords[1];
			}
		}
		return new int[] { x1, y1, x2, y2 };
	}

	/**
	 * Substracts Prewitt - CLAHE, gets the profile of the line drawn over the
	 * subtracted zone and returns it smoothening it with a 1D Gaussian
	 * convolution. It can be used to get the profile of any X-Y images.
	 *
	 * @param prewitt
	 *            Prewitt image.
	 * @param clahe
	 *            CLAHE image.
	 * @param line
	 *            Line to calculate the profile.
	 * @return Profile used to calculate where the perikymata are.
	 */
	private static List<Integer> getPrewittCLAHEProfile(BufferedImage prewitt, BufferedImage clahe, List<int[]> line) {
		List<List<Integer>> intensityMatrix = substractMatrixes(getOrthogonalIntensityMatrix(prewitt, line, 2),
				getOrthogonalIntensityMatrix(clahe, line, 2));
		List<Integer> intensityProfile = getProfileFromIntensityMatrix(intensityMatrix);
		return new Gauss1D().convolve1D(intensityProfile, 5);
	}

	/**
	 * Substracts MatrixA - MatrixB element by element.
	 *
	 * @param a
	 *            Matrix A
	 * @param b
	 *            Matrix B
	 * @return MatrixA - MatrixB. Negative values are returned as zero.
	 */
	private static List<List<Integer>> substractMatrixes(List<List<Integer>> a, List<List<Integer>> b) {
		List<Integer> ortogonal;
		List<List<Integer>> ret = new LinkedList<>();
		for (int i = 0; i < a.size(); i++) {
			ortogonal = new ArrayList<>(a.get(0).size());
			for (int j = 0; j < a.get(0).size(); j++) {
				// 0 if negative.
				ortogonal.add(Math.max(0, a.get(i).get(j) - b.get(i).get(j)));
			}
			ret.add(ortogonal);
		}
		return ret;
	}

	/**
	 * Reduces every column of a intensity Matrix to a single point.
	 *
	 * @param intensityMatrix
	 *            Matrix of intensity.
	 * @return Intensity profile of the matrix.
	 */
	private static List<Integer> getProfileFromIntensityMatrix(List<List<Integer>> intensityMatrix) {
		List<Integer> ret = new ArrayList<>(intensityMatrix.size());
		Integer temp;
		for (List<Integer> orthogonalVector : intensityMatrix) {
			temp = new Integer(0);
			for (Integer element : orthogonalVector) {
				temp = Integer.sum(temp, element);
			}
			ret.add(temp / orthogonalVector.size());
		}
		return ret;

	}
}
