package in.co.thingsdata.synchronization.lamport.algo.server;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import in.co.thingsdata.synchronization.lamport.algo.common.PropertiesReader;
import in.co.thingsdata.synchronization.lamport.algo.common.RegisterMessage;

public final class LamportServer implements Runnable {
	private int numOfClients = 0;
	private ArrayList<ObjectOutputStream> outputConnections = new ArrayList<>();
	private ServerSocket socket;
	private JLabel lblClients;
	private JTextArea txtNotifications;
	private boolean stop;
	private ServerThread serverThread;

	// TODO: Need to handle gracefull shutdown. Remove code from constructor.
	public LamportServer() {

	}

	@Override
	public void run() {

		Socket tempSocket;
		ObjectOutputStream tempObjectOutputStream;

		try {

			while (!stop) {

				try {

					tempSocket = socket.accept();
					tempObjectOutputStream = new ObjectOutputStream(tempSocket.getOutputStream());
					tempObjectOutputStream.writeObject(new RegisterMessage(++numOfClients));
					txtNotifications.append("Registered process number " + numOfClients + "\n");
					updateClientCount();
					outputConnections.add(tempObjectOutputStream);
					serverThread = new ServerThread(this, tempSocket);
					(new Thread(serverThread)).start();
				} catch (SocketException ex) {
					Logger.getLogger(LamportServer.class.getName()).log(Level.SEVERE, null, ex);
					stop();
				} catch (IOException ex) {
					Logger.getLogger(LamportServer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			Logger.getLogger(LamportServer.class.getName()).log(Level.INFO, "Lamport Server is going to shutdown");

		} catch (Exception ex) {
			Logger.getLogger(LamportServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void updateClientCount() {
		this.lblClients.setText("Number of Processes: " + numOfClients);
	}

	private void start() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				setupGUI();
			}
		});

		setupNetworking();
		new Thread(this).start();

	}

	private void setupNetworking() {
		try {
			socket = new ServerSocket(Integer.valueOf(PropertiesReader.getProperty("server.port")));
			Logger.getLogger(LamportServer.class.getName()).log(Level.INFO,
					"Lamport Server is running at port " + PropertiesReader.getProperty("server.port"));
		} catch (IOException ex) {
			Logger.getLogger(LamportServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setupGUI() {
		JFrame serverFrame;
		JPanel serverPanel;

		JScrollPane scrollPane;
		serverFrame = new JFrame("Mediator");
		serverFrame.setLocationByPlatform(true);
		serverFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		serverFrame.setSize(225, 200);

		serverPanel = new JPanel();
		serverPanel.setLayout(new java.awt.GridLayout(0, 1));

		lblClients = new javax.swing.JLabel();
		lblClients.setFont(new Font("Times New Roman", 0, 21));
		updateClientCount();

		txtNotifications = new JTextArea(4, 16);
		txtNotifications.setEditable(false);
		scrollPane = new JScrollPane(txtNotifications);
		scrollPane.setSize(180, 150);

		serverPanel.add(lblClients);
		serverPanel.add(scrollPane);
		serverFrame.add(serverPanel);
		serverFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				stop();
			}
		});
		serverFrame.pack();
		serverFrame.setVisible(true);
	}

	public void stop() {

		try {
			stop = true;
			if (null != serverThread) {
				serverThread.stop();
				serverThread = null;
			}
			if (null != socket) {
				socket.close();
				socket = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ObjectOutputStream> getOutputConnections() {
		return outputConnections;
	}

	public static void main(String[] args) {
		new LamportServer().start();
	}

}