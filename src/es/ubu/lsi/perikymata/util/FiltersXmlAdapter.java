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
import es.ubu.lsi.perikymata.modelo.filters.Filter;
import es.ubu.lsi.perikymata.modelo.filters.Gauss;
import es.ubu.lsi.perikymata.modelo.filters.Prewitt;

/**
 * Class used to pass a filter from XML to an object and vice versa.
 * 
 * @see Project#getFilterList()
 * @author Sergio Chico Carrancio
 */
public class FiltersXmlAdapter extends XmlAdapter<String, Filter> {

	/**
	 * @see Filter#getSmallStringRepresentation() {@inheritDoc}
	 */
	@Override
	public Filter unmarshal(String v) throws Exception {
		String[] filterString = v.split(":");
		Filter filter = null;
		if (filterString[0].equals(Gauss.FILTERNAME)) {
			filter = new Gauss(Double.parseDouble(filterString[1]));
		} else if (filterString[0].equals(Prewitt.FILTERNAME)) {
			String[] parameters = filterString[1].split(",");
			filter = new Prewitt(Integer.parseInt(parameters[0]), Double.parseDouble(parameters[1]));
		}
		return filter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String marshal(Filter v) throws Exception {
		return v.getSmallStringRepresentation();
	}
}