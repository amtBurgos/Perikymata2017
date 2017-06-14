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
from threading import *
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
        # Communication Protocol
        self.DEFAULT_FILTER = 0
        self.ADVANCED_FILTER = 1
        self.CLOSE_SERVER = -1

        self.clients = dict()
        with socket(AF_INET, SOCK_STREAM) as self.server:
            self.server.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
            self.server.bind((host, port))
            self.server.listen(5)
            self.serverWorking = True
            print('Server started, waiting clients...')
            while self.serverWorking:
                client, addr = self.server.accept()
                Thread(target=self.clientThread, args=(client, addr)).start()

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
                    request = client.recv(4096).decode("utf-8")
                    if request == '':
                        # Disconnected
                        print(address, ": disconnected")
                        working = False
                    else:
                        operations = request.split(",")
                        response = self.filter(operations)
                        self.sendMessage(client, response)
                        if (response == "CLOSE_SERVER"):
                            # self.closeServer()
                            self.serverWorking = False
                            working = False
                        print(address, ": work finished")
                except Exception as e:
                    print(address, ": Error filtering, closing connection...")
                    self.sendMessage(client, "ERROR")
                    working = False

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

    def filter(self, operations):
        try:
            kirsch = KirschImageProcessing()

            # First, extract common parameters
            imagePath = str(operations[1])
            savePath = str(operations[2])
            savePathOverlap = str(operations[3])
            kernel = 2
            denoiseWeigh = 0.5
            angleDetection = np.linspace(-0.3, 0.3, num=600)
            minLineLength = 30
            lineGapLength = 16
            smallObjectLength = 30
            detectLines = True

            #If request is an advanced option request
            if (operations[0] == self.ADVANCED_FILTER):
                # Codes for solving the advanced request
                detectLines = bool(operations[4])
                denoiseWeigh = float(operations[5])
                kernel = int(operations[6]) + 1
                minAngle = float(operations[7])
                maxAngle = float(operations[8])
                minLineLength = int(operations[9])
                lineGapLength = int(operations[10])
                smallObjectLength = int(operations[11])
                angleDetection = np.linspace(minAngle, maxAngle, num=800)


            img = kirsch.loadImage(imagePath)
            imgPrepared = kirsch.prepareImage(img, w=denoiseWeigh)
            imgSk, lines = kirsch.kirschProcessing(imgPrepared, kernelId=kernel, angles=angleDetection,
                                                   lineLength=minLineLength, lineGap=lineGapLength,
                                                   minLengthSmallObjects=smallObjectLength, lineDetection=detectLines)

            #Save images depending if the detect lines option is active
            if (detectLines == True):
                kirsch.saveFilteredImage(imgSk, lines, savePath)
                # Save overlapped image too
                kirsch.saveOverlappedImage(img, lines, savePathOverlap)
            else:
                kirsch.saveWithoutLineDetection(img, imgSk, savePath, savePathOverlap)
            return "OK"
        except (Exception):
            return "ERROR"

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
