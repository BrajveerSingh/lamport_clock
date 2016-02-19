package in.co.thingsdata.synchronization.lamport.algo.common;

public class CommunicationMessage implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2655789445898897458L;
	private int sendingProcessNumber;
	private int time;
	private int receivingProcessNumber;

	public CommunicationMessage(int sendingProcessNumber, int time, int receivingProcessNumber) {
		this.sendingProcessNumber = sendingProcessNumber;
		this.time = time;
		this.receivingProcessNumber = receivingProcessNumber;
	}

	public int getSendingProcessNumber() {
		return sendingProcessNumber;
	}

	public int getTime() {
		return time;
	}

	public int getReceivingProcessNumber() {
		return receivingProcessNumber;
	}
	
}
