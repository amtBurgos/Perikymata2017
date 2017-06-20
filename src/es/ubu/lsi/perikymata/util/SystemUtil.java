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

/**
 * Util class for operating system features.
 *
 * @author Andres Miguel Teran
 *
 */
public class SystemUtil {
	/**
	 * Cheks if is a Windows operating system.
	 *
	 * @return if is a Windows system
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toUpperCase().contains("WIN");
	}

	/**
	 * Check if the operating system is for 32 bits.
	 *
	 * @return if is a 64 bits System
	 */
	public static boolean is64bits() {
		return System.getProperty("os.arch").contains("64");
	}
}
