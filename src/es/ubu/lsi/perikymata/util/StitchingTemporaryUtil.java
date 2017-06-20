package es.ubu.lsi.perikymata.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Utility class for temporary folder validation and for copying files to
 * prepare the environment for stitching.
 *
 * @author Andres Miguel Teran
 *
 */
public class StitchingTemporaryUtil {

	/**
	 * Check whether or not the path is a valid directory.
	 *
	 * @param path
	 *            temporary folder path
	 * @return true/false if path is a valid directory
	 */
	public boolean isTempFolderValid(String path) {
		boolean isValid = false;
		File tmp = new File(path);
		if (!path.contains(" ") && tmp.exists() && tmp.isDirectory() && tmp.canRead() && tmp.canWrite()) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * /** Copy a file. Delete source file if success.
	 *
	 * @param sourceFile
	 *            source file
	 * @param targetFile
	 *            target file
	 * @param deleteSource
	 *            true if the source must be deleted when JVM ends.
	 * @param deleteTarget
	 *            true if the target must be deleted when JVM ends.
	 * @return true/false if success
	 */
	public boolean copyFile(File sourceFile, File targetFile, boolean deleteSource, boolean deleteTarget) {
		boolean copied = false;
		BufferedInputStream source = null;
		BufferedOutputStream target = null;
		try {
			source = new BufferedInputStream(new FileInputStream(sourceFile));
			target = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] bytes = new byte[2048];
			int i = source.read(bytes);
			while (i > 0) {
				target.write(bytes, 0, i);
				i = source.read(bytes);
			}
			if (deleteSource == true) {
				sourceFile.deleteOnExit();
			}
			if (deleteTarget == true) {
				targetFile.deleteOnExit();
			}
			targetFile.setReadable(true, false);
			targetFile.setExecutable(true, false);
			copied = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (target != null) {
				try {
					target.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return copied;
	}

	/**
	 * Copy a file. Delete source file if success.
	 *
	 * @param sourceInputStream
	 *            source file inside JAR
	 * @param targetFile
	 *            target file
	 * @param deleteTarget
	 *            true if the target must be deleted when JVM ends
	 * @return true/false if success
	 */
	public boolean copyFile(InputStream sourceInputStream, File targetFile, boolean deleteTarget) {
		boolean copied = false;
		BufferedInputStream source = null;
		BufferedOutputStream target = null;
		try {
			source = new BufferedInputStream(sourceInputStream);
			target = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] bytes = new byte[2048];
			int i = source.read(bytes);
			while (i > 0) {
				target.write(bytes, 0, i);
				i = source.read(bytes);
			}
			if (deleteTarget == true) {
				targetFile.deleteOnExit();
			}
			targetFile.setReadable(true, false);
			targetFile.setExecutable(true, false);
			copied = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (target != null) {
				try {
					target.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return copied;
	}

	/**
	 * Get the name and extension of the image.
	 *
	 * @param path
	 *            of the image
	 * @return string array with the name and extension of the image
	 */
	public String[] getNameAndExtension(String path, boolean isResource) {
		String[] splittedFileName = null;
		if (isResource) {
			splittedFileName = path.split(Pattern.quote("/"));
		} else {
			splittedFileName = path.split(Pattern.quote(System.getProperty("file.separator")));
		}
		String[] nameAndExtension = splittedFileName[splittedFileName.length - 1].split(Pattern.quote("."));
		return nameAndExtension;
	}
}
