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
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;

import es.ubu.lsi.perikymata.MainApp;

/**
 * Test that the logger is working.
 * 
 * @author Sergio Chico Carrancio
 *
 */
public class LoggerTest {
	@Rule
	public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

	@Test
	public void loggerTest() {
		// Must make a file on the running directory with this text. Hard to
		// make an assert of it because filename is current timestamp.
		MainApp main = new MainApp();
		main.configureLogger();
		main.getLogger().log(Level.SEVERE, "Testing logger.", new RuntimeException());
	}

}
