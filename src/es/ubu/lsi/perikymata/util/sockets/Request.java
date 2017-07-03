package es.ubu.lsi.perikymata.util.sockets;
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

import java.util.LinkedList;
import java.util.List;

/**
 * Request message for python server.
 *
 * @author Andres Miguel Teran
 *
 */
public class Request {

	/**
	 * Default filter code.
	 */
	public final static int DEFAULT_FILTER = 0;

	/**
	 * Advanced filter code.
	 */
	public final static int ADVANCED_FILTER = 1;

	/**
	 * Close server code.
	 */
	public final static int CLOSE_SERVER = -1;

	/**
	 * List with the parameters for the python server.
	 */
	private List<Object> parameters;

	/**
	 * Builds a default request.
	 *
	 * @param code
	 *            default code
	 * @param imagePath
	 *            image to filter
	 * @param savePath
	 *            save path
	 * @param savePathOverlapped
	 *            save path for the overlapped image
	 */
	public Request(int code, String imagePath, String savePath, String savePathOverlapped) {
		parameters = new LinkedList<Object>();
		parameters.add(code);
		parameters.add(imagePath);
		parameters.add(savePath);
		parameters.add(savePathOverlapped);

	}

	/**
	 * Builds an advance request for python server.
	 *
	 * @param code
	 *            advanced code
	 * @param imagePath
	 *            image to filter
	 * @param savePath
	 *            save path
	 * @param savePathOverlapped
	 *            save path for the overlapped image
	 * @param detectLinesOrNot
	 *            1 or 0 if the user wants line detection
	 * @param denoiseWeigh
	 *            denoise force parameter
	 * @param kernel
	 *            kernel id to use
	 * @param minAngle
	 *            minimum angle for lines detection
	 * @param maxAngle
	 *            maximum angle for lines detection
	 * @param minLineLength
	 *            minimum line length accepted for detect lines
	 * @param lineGap
	 *            maximum gap between pixel in the image to form a line
	 * @param smallObjectLenght
	 *            maximum length for removing an object in the image
	 */
	public Request(int code, String imagePath, String savePath, String savePathOverlapped, int detectLinesOrNot,
			double denoiseWeigh, int kernel, double minAngle, double maxAngle, int minLineLength, int lineGap,
			int smallObjectLenght) {
		parameters = new LinkedList<Object>();
		parameters.add(code);
		parameters.add(imagePath);
		parameters.add(savePath);
		parameters.add(savePathOverlapped);
		parameters.add(detectLinesOrNot);
		parameters.add(denoiseWeigh);
		parameters.add(kernel);
		parameters.add(minAngle);
		parameters.add(maxAngle);
		parameters.add(minLineLength);
		parameters.add(lineGap);
		parameters.add(smallObjectLenght);
	}

	/**
	 * Builds a request for closing the python server.
	 *
	 * @param code
	 *            close server code
	 * @param closeString
	 *            close server string
	 */
	public Request(int code, String closeString) {
		parameters = new LinkedList<Object>();
		parameters.add(code);
		parameters.add(closeString);
	}

	/**
	 * Returns the request as a valid string.
	 */
	@Override
	public String toString() {
		String command = "";
		for (Object o : parameters) {
			command += o.toString();
			command += ",";
		}
		command = command.substring(0, command.lastIndexOf(","));
		return command;
	}

}
