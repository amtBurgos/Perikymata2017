package es.ubu.lsi.perikymata.modelo.filters;

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
import java.text.DecimalFormat;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 * @author Sergio Chico Carrancio
 *
 */
public class Prewitt implements Filter {

	/**
	 * Name of the filter.
	 */
	public static final String FILTERNAME = "Prewitt";

	/**
	 * size of the mask for the filter.
	 */
	private int size;

	/**
	 * Strength of the filter.
	 */
	private double force;

	/**
	 * Constructor of the filter, adds the arguments to the object.
	 * 
	 * @param size
	 *            size of the mask.
	 * @param force
	 *            strength of the filter.
	 */
	public Prewitt(int size, double force) {
		this.size = size;
		this.force = force;

	}

	/**
	 * Creates a matrix with a prewitt mask using the objects attributes.
	 * 
	 * A 3x3 sized, force 1 is: 1 0 -1 1 0 -1 1 0 -1
	 * 
	 * A 3x3 sized, force 2 is: 2 0 -2 2 0 -2 2 0 -2
	 * 
	 * A 5x5 sized, force 1 is: 2 1 0 -1 -2 2 1 0 -1 -2 2 1 0 -1 -2 2 1 0 -1 -2
	 * 2 1 0 -1 -2
	 * 
	 * @return matrix of the mask in the shape of an array.
	 */
	private float[] calculatePrewittMatrix() {
		int value = size / 2;
		float[] matrix = new float[size * size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				matrix[j * size + i] = (float) (value * force);
			}
			value--;
		}
		return matrix;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage run(BufferedImage image) {
		ImageProcessor proc = new ColorProcessor(image);
		// kernel,width,height
		proc.convolve(calculatePrewittMatrix(), size, size);
		return (BufferedImage) proc.createImage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringProperty getFiltername() {
		return new SimpleStringProperty(FILTERNAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringProperty getFilterArgs() {
		return new SimpleStringProperty("Size: " + size + "; Force: " + new DecimalFormat("#.##").format(force));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSmallStringRepresentation() {
		return FILTERNAME + ":" + size + "," + force;
	}

}
