"""
  License: GPL

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
"""

from socket import *
import threading
from KirschImageProcessing import *

class ServerSocket:
    """
    Server Socket class. Communicates with the java application and do the operations
    the client want.
    @author: Andres Miguel Teran
    """

    def __init__(self, host, port):
        """
        Initializes the server.
        :param host: host name
        :param port:  port number
        """
        self.clients = dict()
        with socket(AF_INET, SOCK_STREAM) as self.server:
            self.server.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
            self.server.bind((host, port))
            self.server.listen(5)
            print('Server started, waiting clients...')
            while True:
                client, addr = self.server.accept()
                threading.Thread(target=self.clientThread, args=(client, addr)).start()


    def clientThread(self, client, address):
        """
        Thread for client management.
        :param client: client connection
        :param address: client address
        :return: None
        """
        if (self.handShake(client) == False):
            # If handshake is not successful
            client.close()
        else:
            # If handshake is correct
            self.clients[address] = client
            print('Client connected: ' + str(address))
            working = True
            while working:
                try:
                    # Wait for responses
                    request = client.recv(1024).decode("utf-8")
                    operation = request.split(",")
                    response = self.doOperations(operation)
                    self.sendMessage(client, response)
                    if (response == "CLOSE_SERVER"):
                        #self.closeServer()
                        working = False
                except Exception as e:
                    print(address,": disconnected")
                    working = False
            print(address, ": work finished")


    def sendMessage(self, client, msg):
        """
        Checks whether the message is well formed and sends it.
        Message must end with '\n' so they can be sent.
        :param client: client connection
        :param msg: message
        :return: None
        """
        if (msg[-1] != "\n"):
            msg = msg + "\n"
        client.send(msg.encode("utf-8"))

    def doOperations(self, operation):
        """
        Do the operations the server wants. Contains the communication protocol with the serve.
        The first item of the list will be the operation to do.
        :param operation: list with the server message which contains the operations to do
        :return: OK / ERROR if the operation has been done or not.
        """

        done = "ERROR"
        # Code of the operation
        print("Operations: ",operation)
        code = int(operation[0])
        if code == 0:
            print(0)
        elif code == 1:
            print(1)
        elif code == 2:
            print(2)
        elif code == 3:
            print(3)
        elif code == 4:
            print(4)
        elif code == -1:
            return "CLOSE_SERVER"
        done = "OK\n"
        return done

    def handShake(self, client):
        """
        Do a handshake with the server to know the connection is well done.
        :return: True / False is handshake is successful or not.
        """
        done = False
        msg = "HELLO FROM PYTHON\n"
        self.sendMessage(client, msg)
        response = client.recv(1024).decode("utf-8")
        if response == "HELLO FROM JAVA":
            print("Handshake successful")
            done = True
        return done

    def closeServer(self):
        """
        Closes the server and its connections.
        :return:
        """

        for client in self.clients.values():
            client.close()

        self.server.close()


if __name__ == "__main__":
    DEFAULT_PORT = 8885
    ServerSocket('localhost', DEFAULT_PORT)

