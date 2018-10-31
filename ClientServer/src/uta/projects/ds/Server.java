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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

/**
 * Server accepts connections from multiple clients and serves them. It spawns
 * off multiple threads to be able to handle multiple clients at once.
 */
public class Server extends JFrame {

	static JTextArea textFromClient;
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JPanel buttonBar;
	JButton cancelButton;

	// counter for clients identification
	static int clientCount = 1;

	// Vector to store active clients
	static Vector<ClientHandler> clientList = new Vector<>();

	Server() {
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		textFromClient = new JTextArea(80, 80);
		buttonBar = new JPanel();
		cancelButton = new JButton();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			// ======== contentPanel ========
			{
				contentPanel.setLayout(new BoxLayout(contentPanel,
						BoxLayout.X_AXIS));

				// ======== scrollPane1 ========
				{
					scrollPane1.setViewportView(textFromClient);
				}
				contentPanel.add(scrollPane1);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			// ======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {
						0, 85, 80 };
				((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {
						1.0, 0.0, 0.0 };

				// ---- cancelButton ----
				cancelButton.setText("Terminate");
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		this.setTitle("Server");
		this.setVisible(true);

	}

	public static void main(String[] args) {

		new Server(); //creating the GUI

		try {
			ServerSocket serverSocket = new ServerSocket(2228);
			System.out.println("Starting server at 2228");
			Socket socket;

			while (true) {

				// Accept all incoming connections
				socket = serverSocket.accept();
				System.out.println("New client request received : " + socket);

				// obtain input and output streams
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				PrintWriter pw = new PrintWriter(socket.getOutputStream());

				// Create a new handler object for handling this client's
				// requests.
				String clientName = "Client " + clientCount;
				ClientHandler clientHandler = new ClientHandler(clientName, br,
						pw, socket);

				// Create a new Thread with this object.
				Thread thread = new Thread(clientHandler);
				System.out.println("New client connection");

				// Display the client connection message on the server's text
				// area
				Server.textFromClient.append("\nNew connection detected: "
						+ clientName + " on port : " + socket.getPort() + "\n");

				// Add this client to active clients list
				clientList.add(clientHandler);

				// Start the thread.
				thread.start();

				clientCount++;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
}

/**
 * This class will handle requests coming for every client. Each client will
 * have its own handler thread.
 */
class ClientHandler implements Runnable {

	private String name;
	Socket socket;
	BufferedReader br;
	PrintWriter pw;
	Boolean loggedIn;

	public ClientHandler(String name, BufferedReader br, PrintWriter pw,
			Socket s) {
		super();
		this.name = name;
		this.socket = s;
		this.br = br;
		this.pw = pw;
		this.loggedIn = true;
	}

	@Override
	public void run() {

		String thisLine;
		while (true)
			try {
				if ((thisLine = br.readLine()) != null) {
					// Get request from the client and parse the attributes
					String header = thisLine;
					String hostname = br.readLine();
					String userAgent = br.readLine();
					String contentType = br.readLine();
					String contentLength = br.readLine();
					br.readLine();
					String randomNumber = br.readLine().trim();

					// Appends request received to server's text area
					Server.textFromClient.append("\nRequest received from "
							+ name);
					Server.textFromClient.append("\nHTTP Header: \n" + header
							+ "\n" + hostname + "\n" + userAgent + "\n"
							+ contentType + "\n" + contentLength + "\n");
					Server.textFromClient
							.append("\nRandom number received from " + name
									+ " : " + randomNumber);
					Server.textFromClient.append("\nServer will sleep for "
							+ randomNumber + " seconds\n");

					// Sleep for randomNumber seconds
					Thread.sleep(Integer.parseInt(randomNumber) * 1000);

					/*
					 * Building response according to HTTP 1.1 Response format
					 * 
					 * HTTP/1.1 200 OK Date: Fri, 31 Dec 1999 23:59:59 GMT
					 * Content-Type: text/plain Content-Length: 42
					 */
					StringBuffer response = new StringBuffer();
					response.append("HTTP/1.1 200 OK\n");
					response.append("Date: " + new Date() + "\n");
					response.append(contentType + "\n");
					response.append("Content-Length: " + randomNumber.length());
					response.append("\r\n");

					// Send response to the client
					PrintWriter out = new PrintWriter(socket.getOutputStream(),
							false);
					out.print(response.toString());
					out.print("\nServer waited " + randomNumber
							+ " seconds for " + name + ".\n");
					out.flush();

					// Display the response being sent to the client
					Server.textFromClient.append("\nResponse sent to " + name
							+ "\n");
					Server.textFromClient.append(response.toString());
				} else {
					Server.textFromClient.append(name + " disconnected!\n");
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}