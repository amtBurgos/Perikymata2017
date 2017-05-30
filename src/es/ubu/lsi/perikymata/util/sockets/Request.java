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
	public static int DEFAULT_FILTER = 0;

	/**
	 * Advanced filter code.
	 */
	public static int ADVANCED_FILTER = 1;

	/**
	 * Close server code.
	 */
	public static int CLOSE_SERVER = -1;

	/**
	 * Image path to filter.
	 */
	private String imagePath;

	/**
	 * Location for saving the filtered image.
	 */
	private String savePath;

	/**
	 * Action code for python server.
	 */
	private int code;

	/**
	 * Build a default request.
	 *
	 * @param code
	 *            default code
	 * @param imagePath
	 *            image to filter
	 * @param savePath
	 *            save path
	 */
	public Request(int code, String imagePath, String savePath) {
		this.code = code;
		this.imagePath = imagePath;
		this.savePath = savePath;
	}

	/**
	 * Returns the request as a valid string.
	 */
	@Override
	public String toString() {
		String command = "";
		if (code == DEFAULT_FILTER) {
			command = "" + code + "," + imagePath + "," + savePath;
		} else if (code == CLOSE_SERVER) {
			command = "" + code + "," + "NULL";
		} else if (code == ADVANCED_FILTER) {
			command = "" + code + "," + "TO DO";
		}
		return command;
	}

}
