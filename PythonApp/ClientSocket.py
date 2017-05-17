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
from KirschImageProcessing import *


class ClientSocket:
    """
    Client Socket class. Communicates with the java application and do the operations
    the server want.
    @author: Andres Miguel Teran
    """

    def __init__(self, host, port):
        """
        Initializes the socket.
        :param host: host name
        :param port:  port number
        """
        self.client = socket(AF_INET, SOCK_STREAM)
        self.client.connect((host, port))
        self.kirsch = None
        print("Connected to server")
        if self.handShake() == False:
            self.closeSocket()
        else:
            self.kirsch = KirschImageProcessing()
            while True:
                # Wait for responses
                response = self.client.recv(1024).decode("utf-8")
                evaluation = self.evaluateResponse(response)
                done = self.doOperations(evaluation)
                self.send(done)
                if (done == "CLOSE"):
                    self.closeSocket()

    def doOperations(self, evaluation):
        """
        Do the operations the server wants. Contains the communication protocol with the serve.
        The first item of the list will be the operation to do.
        :param evaluation: list with the server message which contains the operations to do
        :return: OK / ERROR if the operation has been done or not.
        """

        done = "ERROR"
        # Code of the operation
        code = int(evaluation[0])
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
            return "CLOSE"

        done = "OK"
        return done

    def evaluateResponse(self, response):
        """
        Splits the message received.
        :param response: server message
        :return: list with the message split
        """
        operations = response.split(",")
        return operations

    def handShake(self):
        """
        Do a handshake with the server to know the connection is well done.
        :return: True / False is the connection is successfull or not.
        """
        done = False
        msg = "HELLO FROM PYTHON\n"
        self.send(msg)
        response = self.client.recv(1024).decode("utf-8")
        if response == "HELLO FROM JAVA":
            done = True
        return done

    def send(self, msg):
        """
        Sends a message to the server.
        :param msg: message to send. Normally it will be and "OK" or "ERROR".
        :return:
        """
        if (msg[-1] != "\n"):
            msg = msg + "\n"
        self.client.send(msg.encode("utf-8"))

    def closeSocket(self):
        """
        Closes this socket.
        :return:
        """
        self.client.close()


if __name__ == "__main__":
    DEFAULT_PORT = 8885
    s = ClientSocket('localhost', DEFAULT_PORT)
