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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import es.ubu.lsi.perikymata.util.Gauss1D;

/**
 * Test the smoothing (Gauss 1D filter) of a function.
 * 
 * @author Sergio Chico Carrancio
 *
 */
public class Gauss1DTest {
	@Rule
	public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

	@Test
	public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// method is private, so we make it accesible for it to be tested.
		Method method = Gauss1D.class.getDeclaredMethod("calculateHannArray", int.class);
		method.setAccessible(true);
		float[] out = (float[]) method.invoke(new Gauss1D(), 10);
		System.out.println(Arrays.toString(out));
		// This method is hard to make an assert Succes-Fail, so a println is
		// made

		// This test must make the "1" values higher and the "50" lower, like a
		// bell shape.
		List<Integer> profile = new ArrayList<>(Arrays.asList(1, 1, 50, 1, 1, 1));
		Gauss1D g = new Gauss1D();
		List<Integer> res = g.convolve1D(profile, 10);

		System.out.println(res.toString());

		// Same as previous, but with an inverted bell.
		List<Integer> profile1 = new ArrayList<>(Arrays.asList(1, 1, -50, 1, 1, 1));
		List<Integer> res1 = g.convolve1D(profile1, 10);

		System.out.println(res1.toString());

		// This must make the first values lower or zero, and the higer values
		// higher.
		// End values are lower because array is treated as there are zeros at
		// the end.
		ArrayList<Integer> profile2 = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		List<Integer> res2 = g.convolve1D(profile2, 10);
		System.out.println(res2.toString());

	}

}
