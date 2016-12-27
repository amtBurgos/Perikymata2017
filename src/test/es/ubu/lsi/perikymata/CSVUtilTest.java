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

import org.junit.Test;

import es.ubu.lsi.perikymata.util.CSVUtil;
import es.ubu.lsi.perikymata.vista.PerikymataCountController;

public class CSVUtilTest {

	@Test
	public void test() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// It is a private method, so we make it accessible to be able to test
		// it.
		Method method = CSVUtil.class.getDeclaredMethod("orderPeaksByxCoord", List.class);
		method.setAccessible(true);

		List<int[]> l = new ArrayList<>();
		l.add(new int[] { 10, 0 });
		l.add(new int[] { 7, 5 });
		l.add(new int[] { 3, 0 });
		l.add(new int[] { 4, 2 });
		l.add(new int[] { 1, 2 });

		// ordering
		method.invoke(new PerikymataCountController(), l);

		List<int[]> lr = new ArrayList<>();
		lr.add(new int[] { 1, 2 });
		lr.add(new int[] { 3, 0 });
		lr.add(new int[] { 4, 2 });
		lr.add(new int[] { 7, 5 });
		lr.add(new int[] { 10, 0 });

		Iterator<int[]> il = l.iterator();
		Iterator<int[]> ilr = lr.iterator();
		while (il.hasNext() && ilr.hasNext()) {
			assertArrayEquals(il.next(), ilr.next());
		}
	}

}
