package test.es.ubu.lsi.perikymata;
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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.ubu.lsi.perikymata.util.StitchingUtil;

/**
 * Test for StitchingUtil class.
 *
 * @author Andres Miguel Teran
 *
 */
public class StitchingUtilTest {

	/**
	 * Test object.
	 */
	private StitchingUtil util;

	/**
	 * Operations executed before every test.
	 *
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		util = new StitchingUtil();
	}

	/**
	 * Operations executed after every test.
	 *
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		util = null;
	}

	/**
	 * Creates a folder, sets right permissions and validate it. The validation
	 * must returns true.
	 */
	@Test
	public void ValidateCorrectFolderTest() {
		boolean validation = false;
		try {
			File folder = new File("RightFolder");
			folder.mkdir();
			folder.setReadable(true, false);
			folder.setWritable(true, false);
			validation = util.isTempFolderValid(folder.getAbsolutePath());
			folder.delete();
			assertEquals(validation, true);
		} catch (Exception e) {
			assertEquals(validation, true);
		}
	}

	/**
	 * Creates a folder, sets wrong permissions and validate it. The validation
	 * must returns false.
	 */
	@Test
	public void ValidateIncorrectFolderTest() {
		boolean validation = true;
		try {
			File folder = new File("Wrong Folder");
			folder.mkdir();
			folder.setReadable(true, false);
			folder.setWritable(true, false);
			validation = util.isTempFolderValid(folder.getAbsolutePath());
			folder.delete();
			assertEquals(validation, false);
		} catch (Exception e) {
			assertEquals(validation, false);
		}
	}

	/**
	 * Creates a file, sets right permissions and validate it. The validation
	 * must returns false because it is not a folder.
	 */
	@Test
	public void ValidateNotAFolderTest() {
		boolean validation = true;
		try {
			File folder = new File("WrongFile.txt");
			folder.setReadable(true, false);
			folder.setWritable(true, false);
			validation = util.isTempFolderValid(folder.getAbsolutePath());
			folder.delete();
			assertEquals(validation, false);
		} catch (Exception e) {
			assertEquals(validation, false);
		}
	}

	/**
	 * Test for copying a file properly. Must copy the file and see if the
	 * content is the same.
	 *
	 * @throws IOException
	 */
	@Test
	public void CopyFileProperlyTest() throws IOException {
		boolean copied = false;
		File source = null;
		File target = null;
		BufferedWriter w = null;
		BufferedReader br = null;
		String string = "file_copied";
		String string2 = null;
		try {
			source = new File("source.txt");
			source.setReadable(true, false);
			target = new File("target.txt");
			w = new BufferedWriter(new FileWriter(source));
			w.write(string);
			w.close();

			copied = util.copyFile(source, target, false, false);
			br = new BufferedReader(new FileReader(target));
			string2 = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			assertEquals(copied, true);
			assertEquals(string, string2);
			source.delete();
			target.delete();
			w.close();
			br.close();
		}
	}

	/**
	 * Test that tries to copy a folder into a file. Must return false.
	 */
	@Test
	public void CopyFileWrongTest() {
		boolean copied = true;
		File source = null;
		File target = null;
		try {
			source = new File("source");
			source.mkdir();
			source.setReadable(true);
			target = new File("target.txt");

			copied = util.copyFile(source, target, false, false);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			assertEquals(copied, false);
			source.delete();
			target.delete();
		}
	}

}
