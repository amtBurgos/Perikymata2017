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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Tests a socket connection with python.
 *
 * @author Andres Miguel Teran
 *
 */
public class TestServer {

	/**
	 * Communication port.
	 */
	public static final int PORT = 8885;

	/**
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		System.out.println("Starting server...");
		ServerSocket server = null;
		Socket client = null;
		try {
			server = new ServerSocket(PORT);
			client = server.accept();
			System.out.println("Client connected");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread thread = new Thread(new TestThreadSocketPrototype(client));
		thread.start();

	}

}
