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

/**
 * Client socket class for testing communication with python application.
 *
 * @author Andres Miguel Teran
 *
 */
public class TestClientSocket {

	/**
	 * Host name.
	 */
	private final static String HOST = "localhost";

	/**
	 * Port number.
	 */
	private final static int PORT = 8885;

	/**
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		Socket client = null;
		try {
			// Create socket
			client = new Socket(HOST, PORT);

			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			String message = null;
			System.out.println("Waiting message...");
			message = in.readLine();
			System.out.println("Received: " + message);

			// Handshake
			if (message.equals("HELLO FROM PYTHON")) {
				out.print("HELLO FROM JAVA");
				out.flush();
				System.out.println("Message sent, communication completed");
			}

			// Calling different operations
			System.out.println("Sending 2");
			out.println("-1,imagePath,imageSave");
			out.flush();
			message = in.readLine();
			System.out.println("Received 2: " + message);
			System.out.println("2º: " + message);

		} catch (ConnectException e) {
			System.out.println("Server not ready.");
		} catch (IOException e) {
			System.out.println("Server disconnected.");
			e.printStackTrace();
		}
	}

}
