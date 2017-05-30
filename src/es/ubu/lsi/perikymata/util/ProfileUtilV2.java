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

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

/**
 * Util class for get an image profile and find perikymata. There are reused
 * methods from previous version.
 *
 * @author Andres Miguel Teran *
 */
public class ProfileUtilV2 {

	/**
	 * Uses the pathList of drawn line to get all the pixels that are under the
	 * line.
	 *
	 * @return List of coordinates of the pixels under the line.
	 * @author Sergio Chico Carrancio
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
	 * Finds the red pixel which are supposed to be the perikymata detected by
	 * the python server.
	 *
	 * @param profileCoords
	 *            coords list with the user line
	 * @param image
	 *            filtered image to find perikymata
	 * @return coords list which contains red pixels
	 */
	public static List<int[]> findRedPixels(List<int[]> profileCoords, BufferedImage image) {
		List<int[]> redPixels = new LinkedList<int[]>();
		for (int[] coord : profileCoords) {
			int rgb = image.getRGB(coord[0], coord[1]);
			// Red color in RGB int format
			if (rgb <= -65530 && rgb >= -65550) {
				redPixels.add(coord);
			}
		}
		return redPixels;
	}

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
	 * @author Sergio Chico Carrancio
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
}
