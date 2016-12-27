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
import javax.xml.bind.annotation.adapters.XmlAdapter;

import es.ubu.lsi.perikymata.modelo.Project;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

/**
 * Class used to pass a pathElement from XML to an object and vice versa.
 * 
 * @see Project#getFilterList()
 * @author Sergio Chico Carrancio
 */
public class DrawLineXmlAdapter extends XmlAdapter<String, PathElement> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement unmarshal(String v) throws Exception {
		String[] pathElementString = v.split(":");
		String[] parameters = pathElementString[1].split(",");
		PathElement pathElement = null;
		if (MoveTo.class.getName().equals(pathElementString[0])) {
			pathElement = new MoveTo(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]));
		} else {
			pathElement = new LineTo(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]));
		}
		return pathElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String marshal(PathElement v) throws Exception {
		if (v instanceof MoveTo) {
			MoveTo value = (MoveTo) v;
			return value.getClass().getName() + ":" + value.getX() + "," + value.getY();
		} else {
			LineTo value = (LineTo) v;
			return value.getClass().getName() + ":" + value.getX() + "," + value.getY();
		}
	}
}