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

import es.ubu.lsi.perikymata.util.FiltersXmlAdapter;
import javafx.beans.property.StringProperty;

/**
 * Common Interface for applied filters.
 * 
 * @author Sergio Chico Carrancio
 */
public interface Filter {
	/**
	 * Returns the StringProperty name of the Filter for showing it in the GUI.
	 * 
	 * @return filter name
	 */
	public StringProperty getFiltername();

	/**
	 * Returns the StringProperty of the arguments for showing them in the GUI,
	 * formatted in an human-readable way.
	 * 
	 * @return filter arguments
	 */
	public StringProperty getFilterArgs();

	/**
	 * Runs the filter on the image.
	 * 
	 * @param image
	 *            to filter.
	 * @return filtered image.
	 */
	public BufferedImage run(BufferedImage image);

	/**
	 * Returns a string with the representation of the object, the format must
	 * be as follows: NAME:ARG1,ARG2,... Used to create the XML persistence.
	 * 
	 * @see FiltersXmlAdapter#unmarshal(String)
	 * @return
	 */
	public String getSmallStringRepresentation();
}
