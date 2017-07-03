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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//import es.ubu.lsi.perikymata.modelo.filters.Filter;
import es.ubu.lsi.perikymata.util.DrawLineXmlAdapter;
//import es.ubu.lsi.perikymata.util.FiltersXmlAdapter;
import javafx.scene.shape.PathElement;

/**
 * Project class, stores persistent data. Is used to read or create a XML file.
 *
 * @author Sergio Chico Carrancio
 * @author Andres Miguel Teran
 */
@XmlRootElement
public class Project {
	/**
	 * Name of the project.
	 */
	private String projectName;

	/**
	 * List of the elements used to draw the free-draw line.
	 */
	private List<PathElement> linePath;

	/**
	 * Coordinates of the detected perikymata.
	 */
	private List<int[]> peaksCoords = new ArrayList<>();

	/**
	 * Measure for calculating the distance between perikymata.
	 */
	private Measure measure = new Measure();

	/**
	 * x Coordinate of the Start Decile
	 */
	private Double xDecileStart = null;
	/**
	 * x Coordinate of the End Decile
	 */
	private Double xDecileEnd = null;

	/**
	 * Temporary folder for temporary files.
	 */
	private String temporaryFolder;

	/**
	 * Getter for project name, used to read from a XML.
	 *
	 * @return project name.
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Setter for project name.
	 *
	 * @param projectName
	 *            name to set.
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 *
	 * @return the free draw linepath
	 */
	@XmlJavaTypeAdapter(DrawLineXmlAdapter.class)
	public List<PathElement> getLinePath() {
		return linePath;
	}

	/**
	 *
	 * @param linePath
	 *            the linePath to set
	 */
	public void setLinePath(List<PathElement> linePath) {
		this.linePath = linePath;
	}

	/**
	 * @return the peaksCoords
	 */
	public List<int[]> getPeaksCoords() {
		return peaksCoords;
	}

	/**
	 * @param peaksCoords
	 *            the peaksCoords to set
	 */
	public void setPeaksCoords(List<int[]> peaksCoords) {
		this.peaksCoords = peaksCoords;
	}

	/**
	 * @return the measure
	 */
	public Measure getMeasure() {
		return measure;
	}

	/**
	 * @param measure
	 *            the measure to set
	 */
	public void setMeasure(Measure measure) {
		this.measure = measure;
	}

	/**
	 * @return the xDecileStart
	 */
	public Double getxDecileStart() {
		return xDecileStart;
	}

	/**
	 * @param xDecileStart
	 *            the xDecileStart to set
	 */
	public void setxDecileStart(Double xDecileStart) {
		this.xDecileStart = xDecileStart;
	}

	/**
	 * @return the xDecileEnd
	 */
	public Double getxDecileEnd() {
		return xDecileEnd;
	}

	/**
	 * @param xDecileEnd
	 *            the xDecileEnd to set
	 */
	public void setxDecileEnd(Double xDecileEnd) {
		this.xDecileEnd = xDecileEnd;
	}

	/**
	 * Getter for the temporary folder path.
	 *
	 * @return temporary folder path
	 */
	public String getTemporaryFolder() {
		return temporaryFolder;
	}

	/**
	 * Setter for the temporary folder.
	 *
	 * @param tempFolderPath
	 */
	public void setTemporaryFolder(String tempFolderPath) {
		this.temporaryFolder = tempFolderPath;
	}

}
