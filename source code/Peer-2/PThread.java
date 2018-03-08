import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;



public class PThread extends Thread{
	private BufferedReader br;
	public procedure pf;
	private ServerSocket skt;
	private Socket sc;
	
	public PThread(ServerSocket skt, procedure pf)throws IOException{
		super();
		this.skt = skt;	
		this.pf = pf;
		start();
	}
	
	public void run(){  
	    Socket sc = null;         
		try{
			while(true){	
				sc = skt.accept();	
				call(sc, pf);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(sc!=null){
					sc.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private static void call(final Socket sc, final procedure pf) throws IOException {
		new Thread(new Runnable(){

			@Override
			public void run() {
                String IP = null;
                int port = -1;
                boolean flag = false;
                ObjectInputStream is = null;  
                ObjectOutputStream os = null; 
                String command;
                MessageID varta_no;
                int TTL;
                String fileName;
                
                DateFormat df;
                String time;
                FileWriter writer = null;
                try {  
                	writer = new FileWriter("./peerLog.txt",true);
                	
                    is = new ObjectInputStream(new BufferedInputStream(sc.getInputStream()));  
                    os = new ObjectOutputStream(sc.getOutputStream());  
  
                    Object obj = is.readObject();  
                    Message message = (Message)obj;  
                    
                    command = message.getCommand();
  
                    if("query".equals(command)){
                    	
                    	df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            			time = df.format(new Date());
            			writer.write(time + "\t\tReceive: "+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" "
            					+ message.getMessageID().getPeerID().peerName + "\t\n");
                    
                    	varta_no = message.getMessageID();
                    	TTL = message.getTTL();
                    	fileName = message.getfileName();
                    	
                    	if(TTL>0){
                    		TTL = TTL - 1;                   		
                    		
                    		pf.query(varta_no, TTL, fileName);
                    		flag = pf.search(fileName);
                    		if(flag){
                    			System.out.println(fileName+" is on "+Info_Peer.local.nick.peerName);
                    			pf.hitQuery(varta_no, TTL, fileName, Info_Peer.local.nick.IP, Info_Peer.local.nick.port);
                    			
                    			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    			time = df.format(new Date());
                    			writer.write(time + "\t\tFile "+fileName + " is found on " + Info_Peer.local.nick.peerName+"\r\n");
                    				
                    			
                    		}else{
                    			pf.hitQuery(varta_no, TTL, fileName, IP, port);
                    		}
                    	}else{
                    		flag = pf.search(fileName);
                    		if(flag){
                    			
                    			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    			time = df.format(new Date());
                    			writer.write(time + "\t\tFile "+fileName + " is found on " + Info_Peer.local.nick.peerName+"\r\n");
                    			
                    			System.out.println(fileName+" is on "+Info_Peer.local.nick.peerName);
                    			pf.hitQuery(varta_no, TTL, fileName, Info_Peer.local.nick.IP, Info_Peer.local.nick.port);            			
                    		}else{
                    			pf.hitQuery(varta_no, TTL, fileName, IP, port);
                    		}
                    	}
                    	
                    }else if("hitQuery".equals(command)){
                    	
                    	df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            			time = df.format(new Date());
            			writer.write(time + "\t\tReceive: "+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" "
            					+ message.getMessageID().getPeerID().peerName + "\t\n");
                    	
                    	TTL = message.getTTL();
                    	
                    	if(TTL>=0){
                    		TTL = TTL - 1;
                    		pf.Search_to_Message(message, TTL);
                        		
                        }
                    	
                    }else if("download".equals(command)){
                    	fileName = message.getfileName();
                    	String peerip = message.getPeerIP();
                    	int peerport = message.getPort();
                    	pf.sendFile(fileName, peerip, peerport);
                    }
 
                    writer.close();
                } catch (IOException e) {  
                    e.printStackTrace();
                } catch(ClassNotFoundException e) {  
                    e.printStackTrace(); 
                } finally {  
                    try {  
                        is.close();  
                    } catch(Exception ex) {}  
                    try {  
                        os.close();  
                    } catch(Exception ex) {}  
                    try {  
                        sc.close();  
                    } catch(Exception ex) {}  
                }  
			}
			
		}).start();
	}
}

class ReThread extends Thread{
	int port = 0;
	String fileName = null;
	String IP = null;
	
	public ReThread(String fileName, String IP, int port){
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
     
        FileWriter writer = null;
        
        try {  
        	writer = new FileWriter("./peerLog.txt",true);
        	
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
            
            if(sumL==l){  
                bool = true;  
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			String time = df.format(new Date());
    			writer.write(time + "\t\tFile "+fileName + " is received successfully!\r\n");
            } 
            
            writer.close();	
        }catch (Exception e) {  
            System.out.println("error");  
            bool = false;  
            e.printStackTrace();    
        }finally{    
            if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}  
            if (fis != null)
				try {
					fis.close();
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
        System.out.println(bool?"Success":"Fail");  
        
	}
}

