package es.ubu.lsi.perikymata.modelo;

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
/**
 * Class that stores the measure of the image.
 * 
 * @author Sergio Chico Carrancio
 */
public class Measure {

	/**
	 * Coordinates of the start and the end of the measure.
	 */
	private double[] startMeasure = null;
	private double[] endMeasure = null;

	/**
	 * Value and unit of the measure.
	 */
	private double measureValue;
	private String measureUnit;

	/**
	 * @return the startMeasure
	 */
	public double[] getStartMeasure() {
		return startMeasure;
	}

	/**
	 * @param startMeasure
	 *            the startMeasure to set
	 */
	public void setStartMeasure(double[] startMeasure) {
		this.startMeasure = startMeasure;
	}

	/**
	 * @return the endMeasure
	 */
	public double[] getEndMeasure() {
		return endMeasure;
	}

	/**
	 * @param endMeasure
	 *            the endMeasure to set
	 */
	public void setEndMeasure(double[] endMeasure) {
		this.endMeasure = endMeasure;
	}

	/**
	 * @return the measureValue
	 */
	public double getMeasureValue() {
		return measureValue;
	}

	/**
	 * @param measureValue
	 *            the measureValue to set
	 */
	public void setMeasureValue(double measureValue) {
		this.measureValue = measureValue;
	}

	/**
	 * @return the measureUnit
	 */
	public String getMeasureUnit() {
		return measureUnit;
	}

	/**
	 * @param measureUnit
	 *            the measureUnit to set
	 */
	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}
}
