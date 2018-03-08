
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

public class procedure {

	public boolean search(String fileName){
		if(Info_Peer.local.fileList.size()!=0){
			for(int i = 0; i < Info_Peer.local.fileList.size(); i++){
				if(fileName.equals(Info_Peer.local.fileList.get(i))){
					return true;
				}
			}
		}
		return false;	
	}
	
	
	
	
	public synchronized void Add_to_Message(int messageNum, MessageID varta_no){
		Message_addThread Add_to_Message = new Message_addThread(messageNum, varta_no);
		Thread thread = new Thread(Add_to_Message);
		thread.start();
		thread = null;
	}
	
	public synchronized void Search_to_Message(Message message, int TTL){
		Message_searchThread Search_to_Message = new Message_searchThread(message, TTL);
		Thread thread = new Thread(Search_to_Message);
		thread.start();
		thread = null;
	}

	public void query(MessageID varta_no, int TTL, String fileName){
		queryThread query = new queryThread(varta_no, TTL, fileName);
		Thread thread = new Thread(query);
		thread.start();
		thread = null;
	}
	
	public void hitQuery(MessageID varta_no, int TTL, String fileName, String IP, int port){
		hitQueryThread hitQuery = new hitQueryThread(varta_no, TTL, fileName, IP, port);
		Thread thread = new Thread(hitQuery);
		thread.start();
		thread = null;
	}
	
	public void downLoad(String fileName, int indexNum, String IP, int port){
		downloadThread downLoad = new downloadThread(fileName, indexNum, IP, port);
		Thread thread = new Thread(downLoad);
		thread.start();
		thread = null;
	}
	
	public void sendFile(String fileName, String IP, int port){
		new SeThread(fileName, IP, port);
	}
	class Message_addThread implements Runnable{

		private int messageNum;
		private MessageID varta_no;
		
		public Message_addThread(int messageNum, MessageID varta_no){
			this.messageNum = messageNum;
			this.varta_no = varta_no;
		}
		@Override
		public void run() {
			
			Info_Peer.local.messageTable.put(messageNum, varta_no);

		}
		
	}
	
	class Message_searchThread implements Runnable{
		private Message message;
		private MessageID varta_no;
		private String fileName;
		private int TTL;
		
		public Message_searchThread(Message message, int TTL){
			this.message = message;
			this.TTL = TTL;
		}
		@Override
		public void run() {
			
			varta_no = message.getMessageID();
        	TTL = message.getTTL();
        	fileName = message.getfileName();
        	
        	int key = varta_no.getSequenceNumber();
        	MessageID ID = null;
        	Node node = null;
        	Iterator it = Info_Peer.local.messageTable.entrySet().iterator();
        	while(it.hasNext()){
        		Entry entry = (Entry) it.next();
        		if(entry.getKey().equals(key)){
        			ID = (MessageID) entry.getValue();

        		}
        	}
        	
        	if(ID != null){
        		int seqNum = ID.getSequenceNumber();
        		node = ID.getPeerID();
        		if(node.equals(Info_Peer.local.nick)){

        			Info_Peer.local.hitQueryRequest++;	
        			String fileIp = message.getPeerIP();
        			int filePort = message.getPort();
        			if(filePort != -1){                  			
        				Node peer = new Node(fileIp, filePort);
        				boolean b = false;
        				for(int i = 0; i<Info_Peer.dest.destPeer.size();i++){
        					if(Info_Peer.dest.destPeer.get(i).IP.equals(peer.IP)&&
        							Info_Peer.dest.destPeer.get(i).port == peer.port){
        						b = true;
        					}
        				}
        				
        				if(!b){
        					Info_Peer.dest.destPeer.add(peer);
        				}	
        			}
        		}else{
        			hitQuery(ID, TTL, fileName, message.getPeerIP(), message.getPort());
        		}                 		
        	}
		}
		
	}
	
	class queryThread implements Runnable{

		private MessageID varta_no;
		private int TTL;
		private String fileName;
		private Message message;
		private String command;
		
		public queryThread(MessageID varta_no, int TTL, String fileName) {
			this.command = "query";
			this.varta_no = varta_no;
			this.TTL = TTL;
			this.fileName = fileName;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Node upstream = varta_no.getPeerID();
			int sequence = varta_no.getSequenceNumber();
			if(Info_Peer.local.neighbor.size()!=0){
				for(int i = 0; i < Info_Peer.local.neighbor.size(); i++){
					if(upstream.peerName.equals(Info_Peer.local.neighbor.get(i).peerName)
							&& upstream.IP.equals(Info_Peer.local.neighbor.get(i).IP)){
						
					}else{
						
						Info_Peer.local.messageNum++;
						MessageID oldMessage = new MessageID(sequence,upstream);
						Add_to_Message(Info_Peer.local.messageNum, oldMessage);
						MessageID newMessage = new MessageID(Info_Peer.local.messageNum,Info_Peer.local.nick);
						message = new Message(command, newMessage, TTL, fileName);
						
						try {
							FileWriter writer = new FileWriter("./peerLog.txt",true);
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String time = df.format(new Date());
							writer.write(time + "\t\tSend:"+command + " " + message.getMessageID().getSequenceNumber() 
									+ " " +message.getfileName()+" "+message.getMessageID().getPeerID().peerName+"\t\n");
							writer.close();	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						new clientThread(message, Info_Peer.local.neighbor.get(i).IP, Info_Peer.local.neighbor.get(i).port);
					}
				}
			}		
		}
		
	}
	
	class hitQueryThread implements Runnable{
		
		private String command;
		private MessageID varta_no;
		private int TTL;
		private String fileName;
		private Message message;
		String IP;
		int port;

		public hitQueryThread(MessageID varta_no, int TTL, String fileName, String IP, int port){
			this.command = "hitQuery";
			this.varta_no = varta_no;
			this.TTL = TTL;
			this.fileName = fileName;
			this.IP = IP;
			this.port = port;
		}
		@Override
		public void run() {
			Node upstream = varta_no.getPeerID();
			// Set local peer name to varta_no
			MessageID messageid = new MessageID(varta_no.getSequenceNumber(), Info_Peer.local.nick);

			message = new Message(command, messageid, TTL, fileName, IP, port);
			
			try {
				FileWriter writer = new FileWriter("./peerLog.txt",true);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(new Date());
				writer.write(time + "\t\tSend:"+command + " " + message.getMessageID().getSequenceNumber() 
						+ " " +message.getfileName()+" "+message.getMessageID().getPeerID().peerName+"\t\n");
				writer.close();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new clientThread(message, upstream.IP, upstream.port);
		}
		
	}
	
	class downloadThread implements Runnable{

		private Message message;
		private String command;
		private String fileName;
		private int indexNum;
		String IP;
		int port;
		
		public downloadThread(String fileName, int indexNum, String IP, int port){
			this.command = "download";
			this.fileName = fileName;
			this.indexNum = indexNum;
			this.IP = IP;
			this.port = port;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			message = new Message(command, fileName, IP, port);
			new clientThread(message, Info_Peer.dest.destPeer.get(indexNum-1).IP, Info_Peer.dest.destPeer.get(indexNum-1).port);
		}
		
	}
	
	class clientThread extends Thread{
		private String IP;
		private int port;
		private Message message;
		
		public clientThread(Message message, String IP, int port){
			this.message = message;
			this.IP = IP;
			this.port = port;
			start();
		}
		
		public void run(){
			Socket sc = null;
			ObjectOutputStream os = null;  
            
			try {
				sc = new Socket(IP,port);
				os = new ObjectOutputStream(sc.getOutputStream());
				os.writeObject(message);
				os.flush();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {   
                if (os != null)
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}  
                if (sc != null)
					try {
						sc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}   
            }
		}
	}
	
}


class SeThread extends Thread{
	String fileName = null;
	String IP = null;
	int port = 0;
	public SeThread(String fileName, String IP, int port){
		this.fileName = fileName;
		this.IP = IP;
		this.port = port;
		start();
	}
	
	public void run(){

		int length = 0;  
        double sumL = 0 ;  
        byte[] sendBytes = null;  
        Socket sc = null;  
        DataOutputStream dos = null;  
        FileInputStream fis = null;  
        boolean bool = false;
     
        try {  
            File file = new File("./Look/" + fileName); 
            long l = file.length();   
            sc = new Socket(IP,port);                
            dos = new DataOutputStream(sc.getOutputStream());  
            fis = new FileInputStream(file);        
            sendBytes = new byte[1024];   
            
            while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {  
                sumL += length;               
                System.out.println("Sent:"+((sumL/l)*100)+"%");
                dos.write(sendBytes, 0, length);  
                dos.flush();  
            }   
            //
            if(sumL==l){  
                bool = true;  
                try {
					FileWriter writer = new FileWriter("./peerLog.txt",true);
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = df.format(new Date());
					writer.write(time + "\t\tSend " + fileName + "successfully!\t\n");
					writer.close();	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }  
            
        }catch (Exception e) {  
            System.out.println("error");  
            bool = false;  
            e.printStackTrace();    
        }finally{    
            if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
            if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
            if (sc != null)
				try {
					sc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}      
        }  
        System.out.println(bool?"Success":"Fail");  
        
	}
}
