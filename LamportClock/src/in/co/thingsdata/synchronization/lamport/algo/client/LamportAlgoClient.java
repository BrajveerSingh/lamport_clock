package in.co.thingsdata.synchronization.lamport.algo.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class LamportAlgoClient {
	
	private LamportProcess lamportProcess;
	private List<LamportProcess> list = new ArrayList<>();

	public static void main(String args[]) {

		new LamportAlgoClient().startClient();

	}

	private void startClient() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				start();
			}

		});

	}

	private void start() {

		final JFrame mainFrame = new JFrame("Lamport Algorithm Client");

		mainFrame.setSize(600, 500);

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JButton btnAddProcess = new JButton("Add New Process");

		btnAddProcess.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				lamportProcess = new LamportProcess();
			
				list.add(lamportProcess);
				
				try {
					new Thread(lamportProcess).start();
				} catch (Exception ex) {
					ex.printStackTrace();
					mainFrame.dispose();
				}
			
			}
		
		});
		mainFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				
				System.out.println("Client application is going to shutdown.");
				
				for (LamportProcess process : list) {
					process.stop();
					process.dispose();
				}
				
			}

		});
		mainFrame.add(btnAddProcess);

		mainFrame.pack();

		mainFrame.setVisible(true);

	}
}
