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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.ubu.lsi.perikymata.MainApp;
import es.ubu.lsi.perikymata.modelo.Measure;

/**
 * Util to write the Perikymata data into a CSV
 * 
 * @author Sergio Chico Carrancio
 */
public class CSVUtil {

	/**
	 * <pre>
	 * Creates a CSV file with the following representation:
	 * -------------------------
	 * "Project name"
	 * Measure unit,"measure unit"
	 * Decile size,"measure size, calculated in the measure unit"
	 * 
	 * Decile,Coordinates,Distance to previous
	 * "number of decile","X Y coordinates","Distance in the measure unit to previous"
	 * 
	 * Perikymata per decile
	 * 1,2,3,4,5,6,7,8,9,10
	 * "decile 1","decile 2",...
	 * ----------------------------
	 * </pre>
	 * 
	 * @param mainApp
	 *            Reference to the main application, used to retrieve data.
	 * @param measure
	 *            Measure object that gives the real size of the image.
	 * @param peaksCoords
	 *            List of x,y coordinates.
	 * @param xDecileStart
	 *            x coordinate of the first decile.
	 * @param xDecileEnd
	 *            x coordinate of the last decile.
	 * @param decilesBetween
	 *            x coordinates of the rest of deciles
	 * @throws IOException
	 *             Thrown if there is an error writing the CSV file.
	 */
	public static void createCSV(MainApp mainApp, Measure measure, List<int[]> peaksCoords, double xDecileStart,
			double xDecileEnd, double[] decilesBetween) throws IOException {

		String sFileName = Paths.get(mainApp.getProjectPath(), "Perikymata_Outputs", "Output.csv").toString();
		FileWriter writer = new FileWriter(sFileName);

		// Vector distance from A to B of the measure
		double measureXDist = Math.max(measure.getStartMeasure()[0], measure.getEndMeasure()[0])
				- Math.min(measure.getStartMeasure()[0], measure.getStartMeasure()[0]);
		double measureYDist = Math.max(measure.getStartMeasure()[1], measure.getEndMeasure()[1])
				- Math.min(measure.getStartMeasure()[1], measure.getStartMeasure()[1]);
		double measureTotalDist = Math.sqrt(Math.pow(measureXDist, 2) + Math.pow(measureYDist, 2));

		int[] perikymataPerDecile = new int[10];
		int numPerDecile = 0;
		int currentDecile = 0;
		int currentPerikymataIndex = 0;
		int lastPerikymataIndex = 0;

		// Header
		writer.append(mainApp.getProject().getProjectName());
		writer.append('\n');
		writer.append("Measure Unit");
		writer.append(',');
		writer.append(measure.getMeasureUnit());
		writer.append('\n');

		// Decile size
		int decilePixelSize = (int) (Math.max(decilesBetween[0], decilesBetween[1])
				- Math.min(decilesBetween[0], decilesBetween[1]));
		writer.append("Decile size");
		writer.append(',');
		// Real distance = (measureValue*X)/measureDistance
		writer.append(Double.toString((measure.getMeasureValue() * decilePixelSize) / measureTotalDist));
		writer.append('\n');

		writer.append('\n');

		// Decile data header
		writer.append("Decile");
		writer.append(',');
		writer.append("Coordinates");
		writer.append(',');
		writer.append("Distance to previous");
		writer.append('\n');

		// order peaks by x coord
		orderPeaksByxCoord(peaksCoords);

		// Skips perikymata before the first decile.
		while (xDecileStart > peaksCoords.get(currentPerikymataIndex)[0]) {
			currentPerikymataIndex++;
			lastPerikymataIndex++;
		}

		// Writes the first 9 deciles
		int firstValue = currentPerikymataIndex;
		while (currentDecile < 9) {
			while (currentPerikymataIndex < peaksCoords.size()
					&& decilesBetween[currentDecile] > peaksCoords.get(currentPerikymataIndex)[0]) {
				writeDecileValues(writer, currentDecile, peaksCoords, currentPerikymataIndex, lastPerikymataIndex,
						measureTotalDist, measure);
				numPerDecile++;

				// First element is compared to self on the first iteration,
				// then to the previous.
				if (currentPerikymataIndex != firstValue) {
					lastPerikymataIndex++;
				}
				currentPerikymataIndex++;
			}
			perikymataPerDecile[currentDecile] = numPerDecile;
			currentDecile++;
			numPerDecile = 0;
		}

		// writes the last decile.
		while (currentPerikymataIndex < peaksCoords.size() && xDecileEnd > peaksCoords.get(currentPerikymataIndex)[0]) {
			writeDecileValues(writer, currentDecile, peaksCoords, currentPerikymataIndex, lastPerikymataIndex,
					measureTotalDist, measure);
			currentPerikymataIndex++;
			lastPerikymataIndex++;
			numPerDecile++;
		}
		perikymataPerDecile[9] = numPerDecile;

		// Writes the number of Perikymata per decile
		writer.append('\n');
		writer.append("Perikymata per decile");
		writer.append('\n');

		// Number of the decile
		writer.append('1');
		for (int i = 1; i < 10; i++) {
			writer.append(',');
			writer.append(String.valueOf(i + 1));
		}

		writer.append('\n');
		// Number of perikymata per decile
		writer.append(String.valueOf(perikymataPerDecile[0]));
		for (int i = 1; i < 10; i++) {
			writer.append(',');
			writer.append(String.valueOf(perikymataPerDecile[i]));

		}
		writer.append('\n');

		writer.flush();
		writer.close();

	}

	/**
	 * Writes the value
	 * 
	 * @param writer
	 *            CSV file writer.
	 * @param decile
	 *            Current decile number.
	 * @param peaksCoords
	 *            Coordinates of the perikymata.
	 * @param currentPerikymataIndex
	 *            Current perikymata to write.
	 * @param lastPerikymataIndex
	 *            Last perikymata, used to write the distance.
	 * @param measureTotalDist
	 *            Vector distance between the two points of the measure.
	 * @param measure
	 *            Measure object that gives the real size of the image.
	 * @throws IOException
	 *             If error when writing on file.
	 */
	private static void writeDecileValues(FileWriter writer, int decile, List<int[]> peaksCoords,
			int currentPerikymataIndex, int lastPerikymataIndex, double measureTotalDist, Measure measure)
			throws IOException {
		// Number of decile, starting on 1 and ending on 10.
		writer.append(Integer.toString(decile + 1));
		writer.append(',');

		// Coordinates of the perikymata
		writer.append(Integer.toString(peaksCoords.get(currentPerikymataIndex)[0]) + " "
				+ Integer.toString(peaksCoords.get(currentPerikymataIndex)[1]));
		writer.append(',');

		// Vector distance of the pixels
		double xdist = Math.max(peaksCoords.get(currentPerikymataIndex)[0], peaksCoords.get(lastPerikymataIndex)[0])
				- Math.min(peaksCoords.get(currentPerikymataIndex)[0], peaksCoords.get(lastPerikymataIndex)[0]);
		double ydist = Math.max(peaksCoords.get(currentPerikymataIndex)[1], peaksCoords.get(lastPerikymataIndex)[1])
				- Math.min(peaksCoords.get(currentPerikymataIndex)[1], peaksCoords.get(lastPerikymataIndex)[1]);
		double totalDist = Math.sqrt(Math.pow(xdist, 2) + Math.pow(ydist, 2));

		// Real distance = (measureValue*X)/measureDistance
		writer.append(Double.toString((measure.getMeasureValue() * totalDist) / measureTotalDist));
		writer.append('\n');
	}

	/**
	 * Orders the List of coordinates by the first component(x).
	 * 
	 * @param peaks
	 *            List of the coordinates of the perikymata
	 */
	private static void orderPeaksByxCoord(List<int[]> peaks) {
		Collections.sort(peaks, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				return o1[0] - o2[0];
			}
		});

	}

}
