package in.co.thingsdata.synchronization.lamport.algo.common;

public class RegisterMessage implements Message {
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 2946640877705853760L;
	private int processNumber = 0;
    
	public RegisterMessage() {}
	
	public RegisterMessage (int ProcessNumber) {
        this.processNumber = ProcessNumber;
    }
	
    public int getProcessNumber() {
		return processNumber;
	}
    
}