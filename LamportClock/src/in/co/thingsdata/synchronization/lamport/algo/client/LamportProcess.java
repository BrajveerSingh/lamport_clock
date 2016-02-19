package in.co.thingsdata.synchronization.lamport.algo.client;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import in.co.thingsdata.synchronization.lamport.algo.common.CommunicationMessage;
import in.co.thingsdata.synchronization.lamport.algo.common.PropertiesReader;
import in.co.thingsdata.synchronization.lamport.algo.common.RegisterMessage;
import in.co.thingsdata.synchronization.lamport.algo.common.exception.NotConncetedException;

public class LamportProcess implements Runnable {
	private Socket socket;
	private int processNumber;
	private int time = 0;

	private ObjectInputStream fromServer;
	private ObjectOutputStream toServer;

	private JFrame processFrame;
	private JTextField txtSendMessageTo;
	private JPanel clockPanel;
	private JPanel clockRatePanel;
	private JButton btnIncrement;
	private JButton btnDecrement;
	private JLabel clock;
	private JPanel messagePanel;
	private JLabel lblSendMessageTo;
	private JButton btnSend;
	private Timer timer;
	private JTextArea notificationArea;

	private boolean stop = false;

	private ActionListener sendMessage = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				CommunicationMessage message = new CommunicationMessage(processNumber, time,
						Integer.parseInt(txtSendMessageTo.getText()));
				toServer.writeObject(message);
			} catch (UnknownHostException ex) {
				Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	};

	private ActionListener incrementClockRate = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			timer.setDelay(timer.getDelay() / 2);
		}
	};

	private ActionListener decrementClockRate = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			timer.setDelay(timer.getDelay() * 2);
		}
	};

	LamportProcess() {
	}

	private int initiateConnection() {

		try {
			socket = new Socket(PropertiesReader.getProperty("server.ip"),
					Integer.valueOf(PropertiesReader.getProperty("server.port")));
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());

			RegisterMessage message = (RegisterMessage) fromServer.readObject();
			processNumber = message.getProcessNumber();
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnknownHostException ex) {
			Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
		Logger.getLogger(LamportProcess.class.getName()).log(Level.INFO, "Lamport Process is connected to the server at serverIP = " + PropertiesReader.getProperty("server.ip") + " and server port " + PropertiesReader.getProperty("server.port"));
		return processNumber;
	}

	private void Communicate() throws NotConncetedException {
		CommunicationMessage message;
		while (!stop) {
			try {
				if (null == fromServer) {
					throw new NotConncetedException ("Connection is not established with the Server. Please check whether server is running or not.");
				}
				message = (CommunicationMessage) fromServer.readObject();
				notificationArea.append("Got a message from " + message.getSendingProcessNumber() + " with timestamp "
						+ message.getTime() + "\n");
				if (time <= message.getTime()) {
					time = message.getTime();
					timer.restart();
					clockTick.actionPerformed(null);
				}
			} catch (EOFException ex) {
				Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
				stop();
			}catch (IOException ex) {
				Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private ActionListener clockTick = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			clock.setText(String.valueOf(time));
			time++;
		}
	};

	@Override
	public void run() {
		setupGUI();
		try {
			Communicate();
		} catch (NotConncetedException e) {
			//e.printStackTrace();
			processFrame.dispose();
			Logger.getLogger(LamportProcess.class.getName()).log(Level.SEVERE, null, e);
		
			stop();
			
			System.exit(1);
		}
		
	}

	private void setupGUI() {

		processFrame = new JFrame("Lamport Process : " + processNumber);
		processFrame.setLocationByPlatform(true);
		processFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		processFrame.setLayout(new FlowLayout());
		processFrame.setSize(395, 240);

		clockPanel = new JPanel(new FlowLayout());
		clock = new JLabel();
		clock.setFont(new Font("Times New Roman", 0, 50));
		clockTick.actionPerformed(null);
		clockPanel.add(clock);
		clockPanel.setVisible(true);

		clockRatePanel = new JPanel(new GridLayout(2, 1));
		btnIncrement = new JButton("+");
		btnIncrement.setToolTipText("Click to increase clock rate");
		btnDecrement = new JButton("-");
		btnDecrement.setToolTipText("Click to decrease clock rate");
		btnIncrement.addActionListener(incrementClockRate);
		btnDecrement.addActionListener(decrementClockRate);
		clockRatePanel.add(btnIncrement);
		clockRatePanel.add(btnDecrement);
		clockPanel.add(clockRatePanel);
		processFrame.add(clockPanel);

		messagePanel = new JPanel(new GridLayout());
		lblSendMessageTo = new JLabel("Send a message to ");
		messagePanel.add(lblSendMessageTo);
		txtSendMessageTo = new JTextField();
		txtSendMessageTo.addActionListener(sendMessage);
		messagePanel.add(txtSendMessageTo);
		btnSend = new JButton("Send");
		btnSend.addActionListener(sendMessage);
		messagePanel.add(btnSend);
		processFrame.add(messagePanel);

		notificationArea = new javax.swing.JTextArea(5, 32);
		processFrame.add((new JPanel(new GridLayout(1, 1))).add(new JScrollPane(notificationArea)));

		processFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				stop();
			}
		});
		timer = new Timer(2000, clockTick);
		timer.start();

		processFrame.setVisible(true);

		processNumber = initiateConnection();
		processFrame.setTitle("Lamport Process : " + processNumber);

	}

	void stop() {
		try {
			stop = true;
			if (null != socket && !socket.isClosed()) {
				socket.close();
				socket = null;
			}
			if (null != fromServer) {
				fromServer.close();
				fromServer = null;
			}
			if (null != toServer) {
				toServer.close();
				toServer = null;
			}
			if (null != timer && timer.isRunning()) {
				timer.stop();
				timer = null;
			}
			this.processFrame.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void dispose() {
		processFrame.dispose();
	}
}