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

import ij.plugin.filter.GaussianBlur;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Gauss implements Filter {

	/**
	 * Strength of the filter.
	 */
	private double sigmaValue;

	/**
	 * Name of the filter.
	 */
	public static final String FILTERNAME = "Gaussian";

	/**
	 * Constructor of the filter, adds the arguments to the object.
	 * 
	 * @param sigma
	 *            Strenght of the filter.
	 */
	public Gauss(double sigma) {
		sigmaValue = sigma;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage run(BufferedImage image) {
		GaussianBlur gauss = new GaussianBlur();
		ImageProcessor proc = new ColorProcessor(image);
		gauss.blurGaussian(proc, sigmaValue);
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
		return new SimpleStringProperty("Sigma: " + new DecimalFormat("#.##").format(sigmaValue));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSmallStringRepresentation() {
		return FILTERNAME + ":" + sigmaValue;
	}
}
