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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import es.ubu.lsi.perikymata.util.ProfileUtil;
import es.ubu.lsi.perikymata.vista.PerikymataCountController;

/**
 * Tests the method that gets all the coordinates between two points.
 * 
 * @author Sergio Chico Carrancio
 */
public class BresenhamTest {

	@Rule
	public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

	@Test
	@SuppressWarnings("unchecked")
	public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// It is a private method, so we make it accessible to be able to test
		// it.
		Method method = ProfileUtil.class.getDeclaredMethod("Bresenham", int.class, int.class, int.class, int.class);
		method.setAccessible(true);
		List<int[]> lista = (List<int[]>) method.invoke(new PerikymataCountController(), 0, 0, 3, 3);
		List<int[]> resultado = new ArrayList<>();
		// Easy test, given 0,0 and 3,3 the line must pass
		// the points 1,1 2,2 and arrive at 3,3. Harder
		// tests can be made.
		resultado.add(new int[] { 1, 1 });
		resultado.add(new int[] { 2, 2 });
		resultado.add(new int[] { 3, 3 });
		Iterator<int[]> itList = lista.iterator();
		Iterator<int[]> itResult = resultado.iterator();
		while (itList.hasNext()) {
			assertArrayEquals(itList.next(), itResult.next());
		}
	}

}
