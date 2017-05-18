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
import java.net.Socket;
import java.net.SocketException;

/**
 * Runnable prototype for client socket.
 *
 * @author Andres Miguel Teran
 *
 */
public class ThreadPrototype implements Runnable {

	/**
	 * Client socket.s
	 */
	Socket client;

	/**
	 * Input stream.
	 */
	private BufferedReader in;

	/**
	 * Output string.
	 */
	private PrintWriter out;

	/**
	 * Constructs a thread for the client socket.
	 *
	 * @param client
	 *            client socket
	 */
	public ThreadPrototype(Socket client) {
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
			String message = null;
			System.out.println("Waiting message...");
			// while ((message = in.readLine()) != null) {
			message = in.readLine();
			System.out.println("Received: " + message);
			// Doing handshake
			if (message.equals("HELLO FROM PYTHON")) {
				out.print("HELLO FROM JAVA");
				out.flush();
				System.out.println("Message sent, communication completed");
			}
			// }

			// Calling different operations
			System.out.println("Sending 2");
			out.println("0,imagePath,imageSave");
			out.flush();
			message = in.readLine();
			System.out.println("Received 2: " + message);
			System.out.println("2º: " + message);
		} catch (SocketException e) {
			System.out.println("Client disconnected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
