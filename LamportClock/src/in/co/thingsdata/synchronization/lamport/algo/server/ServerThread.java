package in.co.thingsdata.synchronization.lamport.algo.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import in.co.thingsdata.synchronization.lamport.algo.common.CommunicationMessage;

class ServerThread implements Runnable {
    private LamportServer server;
   // private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean stop = false;
 
    ServerThread (LamportServer server, Socket socket) {
        try {
            this.server = server;
           // this.socket = socket;
            this.inputStream = new ObjectInputStream (socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        CommunicationMessage message;
        while (!stop) {
            try {
                message = (CommunicationMessage) inputStream.readObject();
                outputStream = (ObjectOutputStream)server.getOutputConnections().get(message.getReceivingProcessNumber()-1);
                outputStream.writeObject (message);
            } catch (SocketException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                stop();
            }catch (EOFException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                stop();
            }catch (IOException ex) {
                Logger.getLogger(LamportServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void stop() {
    	
    	try {
    		stop = true;
    		
			if (null != inputStream) {
				inputStream.close();
				inputStream = null;
			}
			if (null != outputStream) {
				outputStream.close();
				outputStream = null;
			}
			if (null != server && null != server.getOutputConnections() && server.getOutputConnections().size() > 0) {
				for (OutputStream os :server.getOutputConnections()) {
					if (null != os) {
						os.close();
						os = null;
					}
				}
				server = null;
			}
    	} catch (Exception e) {
    		
    	}
    	
    }
 
}