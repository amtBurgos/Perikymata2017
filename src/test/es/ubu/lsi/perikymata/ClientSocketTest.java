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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.ubu.lsi.perikymata.util.sockets.ClientSocket;
import es.ubu.lsi.perikymata.util.sockets.Request;

/**
 * Test for ClientSocket class.
 *
 * @author Andres Miguel Teran
 *
 */
public class ClientSocketTest {

	/**
	 * Server object
	 */
	private ServerSocket server;

	/**
	 * Starts the server for testing.
	 */
	private void startServer() {
		Thread t = new Thread(() -> {
			try {
				int port = 10341;
				server = new ServerSocket(port);
				Socket client = server.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.print("HELLO FROM PYTHON\n");
				out.flush();
				String message = in.readLine();
				assertEquals(message, "HELLO FROM JAVA");
				String request = in.readLine();
				String[] values = request.split(",");
				assertEquals(values[0], "0");
				assertEquals(values[1], "CLOSE_SERVER");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				assertTrue(false);
			}
		});
		t.start();
	}

	/**
	 * Initialize environment.
	 *
	 * @throws java.lang.Exception
	 *             Sockets Exceptions
	 */
	@Before
	public void setUp() throws Exception {
		startServer();
	}

	/**
	 * Actions to make at the end.
	 *
	 * @throws java.lang.Exception
	 *             Sockets Exceptions
	 */
	@After
	public void tearDown() throws Exception {
		server.close();
	}

	/**
	 * Test the connection and sends a request.
	 */
	@Test
	public void ConnectionTest() {
		boolean sended = true;
		ClientSocket test;
		try {
			test = new ClientSocket();
			test.send(new Request(Request.CLOSE_SERVER, "CLOSE_SERVER"));
		} catch (Exception e) {
			sended = false;
		} finally {
			assertEquals(sended, true);
		}
	}

}
