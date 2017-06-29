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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client socket that connects with the python server.
 *
 * @author Andres Miguel Teran
 *
 */
public class ClientSocket {

	/**
	 * Host name.
	 */
	private final String HOST = "localhost";

	/**
	 * Port list to try
	 */
	private final int DEFAULT_PORT = 10341;

	/**
	 * Client socket.
	 */
	private Socket client;

	/**
	 * Input channel.
	 */
	private BufferedReader in;

	/**
	 * Output channel.
	 */
	private PrintWriter out;

	/**
	 * ClientSocket constructor.
	 *
	 * @throws UnknownHostException
	 *             invalid host
	 * @throws IOException
	 *             Server not ready
	 */
	public ClientSocket() throws ConnectException, IOException {
		client = new Socket(HOST, DEFAULT_PORT);
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintWriter(client.getOutputStream(), true);
		handshake();
	}

	/**
	 * Test the connection sending a message.
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean handshake() throws IOException {
		boolean correct = false;
		String message = in.readLine();
		if (message.equals("HELLO FROM PYTHON")) {
			out.print("HELLO FROM JAVA");
			out.flush();
			correct = true;
		}
		return correct;
	}

	/**
	 * Sends a string request to the server.
	 *
	 * @param request
	 *            request
	 */
	public void send(Request request) {
		out.print(request.toString());
		out.flush();
	}

	/**
	 * Closes the socket.
	 */
	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive response from python server.
	 *
	 * @return OK / ERROR
	 */
	@SuppressWarnings("finally")
	public String receive() {
		String response = "ERROR";
		try {
			response = in.readLine();
		} catch (IOException e) {
			response += e.toString();
		} finally {
			return response;
		}
	}
}
