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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Connects with a python application by sockets.
 *
 * @author Andres Miguel Teran
 *
 */
public class JavaServerPrototype {

	/**
	 * Port number. Free port found at
	 * http://www.networksorcery.com/enp/protocol/ip/ports08000.htm
	 */
	private final int port = 8885;

	/**
	 * Whether the server must be running or not.
	 */
	private boolean alive;

	/**
	 * OutputStream.
	 */
	PrintWriter out;

	/**
	 * Socket
	 */
	ServerSocket serverSocket;

//	/**
//	 * Starts the serves.
//	 *
//	 * @throws IOException
//	 *             IOException
//	 */
//	public void startServer() throws IOException {
//		ServerSocket server = new ServerSocket(port);
//		Socket clientPython = null;
//		alive = true;
//		System.out.println("Servidor iniciado");
//		clientPython = serverSocket.accept();
//
//		PrintWriter out = new PrintWriter(clientPython.getOutputStream(), true);
//		BufferedReader in = new BufferedReader(new InputStreamReader(clientPython.getInputStream()));
//
//		String msgReceived = null;
//		while ((msgReceived = in.readLine()) != null) {
//			System.out.println(msgReceived);
//		}
//
//		// Thread thread = new Thread(new ThreadPrototype(clientPython));
//		// thread.start();
//	}

	/**
	 * Returns whether the server is running or not.s
	 *
	 * @return true if server is running
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Shutdowns the server.
	 */
	public void shutdown() {
		this.alive = false;
	}

	/**
	 * @param args
	 *            arguments
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		JavaServerPrototype server = new JavaServerPrototype();
		//server.startServer();
		Scanner scan = new Scanner(System.in);
		Socket clientSocket = serverSocket.accept();
		while (true) {
			try {
				ServerThreadForClient clientThread = new ServerThreadForClient(clientSocket, clientId++);
				usersList.add(clientThread);
				clientThread.start();
			} catch (IOException e) {
				System.err.println("# Cliente no aceptado.");
			}
		}
	}

}
