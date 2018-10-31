/**
 * Submitted by: Prajakta Ganesh Jalisatgi
 * ID: 1001637722
 * <p>
 * References:
 * https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/
 * https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/
 * http://www.java2s.com/Code/Java/Network-Protocol/AverysimpleWebserverWhenitreceivesaHTTPrequestitsendstherequestbackasthereply.htm
 * https://www.jmarshall.com/easy/http/#postmethod
 * https://stackoverflow.com/questions/15247752/gui-client-server-in-java
 */

package uta.projects.ds;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Client connects to the server via socket and generates a random integer and uploads that integer to the server.
 */
public class Client extends JFrame implements ActionListener {

	private static int serverPort = 2228;
	static Socket socket;
	private static JTextArea textFromServer;
	JButton sendRandomNumber;
	JButton disconnectButton;

	Client() {
		// constructor that creates the GUI for Client
		this.setTitle("Client");
		this.setSize(650, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.getContentPane().setLayout(null);

		initComponents();
		this.setVisible(true);
	}

	/**
	 * Initializing all the components used in this JFrame
	 */
	private void initComponents() {
		textFromServer = new JTextArea();
		textFromServer.setEditable(false);
		textFromServer.setLineWrap(true);
		textFromServer.setBounds(10, 10, 500, 300);
		add(textFromServer);

		sendRandomNumber = new JButton();
		sendRandomNumber.setText("Send Request");
		sendRandomNumber.setBounds(10, 350, 500, 50);
		sendRandomNumber.addActionListener(this); // passes button object to
													// send data after clicking
													// it
		add(sendRandomNumber);

		disconnectButton = new JButton();
		disconnectButton.setText("Disconnect");
		disconnectButton.setBounds(10, 400, 500, 50);
		disconnectButton.addActionListener(this);
		add(disconnectButton);
	}

	public static void main(String[] args) {
		{
			new Client();
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName("localhost");

				// establish the connection
				socket = new Socket(inetAddress, getServerPort());

				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));

				// read data sent from the server
				while (true) {
					String thisLine = br.readLine();
					//Checking if server is disconnected
					if (thisLine == null) {
						textFromServer.append("\nServer disconnected!\n");
						return;
					}
                    //Read the messages sent from the server
					while (!thisLine.isEmpty()) {
						thisLine = br.readLine();
					}
					String msg = br.readLine();
					textFromServer.append("\n" + msg);
				}
			} catch (UnknownHostException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Using random function to generate a random number from 5 to 15
	 */
	private static int generateRandomNumber() {
		// generate random number
		Random rand = new Random();
		int randomNumber = rand.nextInt(10) + 5;
		System.out.println("random number : " + randomNumber);
		return randomNumber;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Action performed when the Send Request button is clicked
		if (e.getSource().equals(sendRandomNumber)) {
			try {
				System.out.println("Client sending text");

				// calls the generateRandomNumber function defined above
				int randomNumber = generateRandomNumber();
				String random = String.valueOf(randomNumber);

				PrintWriter out = new PrintWriter(socket.getOutputStream(),
						false);

				/*
				 * Start sending our reply using the HTTP 1.1 protocol Host is
				 * mandatory in HTTP 1.1 Body contains the random number
				 * 
				 * POST /path/script.cgi HTTP/1.0 From: frog@jmarshall.com
				 * User-Agent: HTTPTool/1.0 Content-Type:
				 * application/x-www-form-urlencoded Content-Length: 32
				 * 
				 * home=Cosby&favorite+flavor=flies
				 */

				out.print("POST /path/script.cgi HTTP/1.1\n"); // Version &
																// status code
				out.print("Host: "
						+ socket.getInetAddress().getCanonicalHostName() + "\n");
				out.print("User-Agent: HTTPTool/1.0\n");
				out.print("Content-Type: text/plain\n"); // The type of data
				out.print("Content-Length: " + random.length() + "\n");
				out.print("\r\n"); // End of headers
				out.print(random + "\n"); // Body
				out.flush();
				textFromServer.append("\nRequest sent with random number: "
						+ random);

			} catch (IOException exception) {
				exception.printStackTrace();
			}
		} else if (e.getSource().equals(disconnectButton)) {// Action performed when the disconnect button is clicked
			System.out.println("Client Disconnected gracefully");
			System.exit(0); //kills the client process
		}
	}

	public static int getServerPort() {
		return serverPort;
	}

}
