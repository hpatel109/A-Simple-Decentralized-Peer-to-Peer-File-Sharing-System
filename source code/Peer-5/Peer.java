

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Peer {
	
	public static void reading(String config){
		FileWriter writer = null;
		String s = new String();
		try{
			writer = new FileWriter("./peerLog.txt",true);
			
			File file = new File("./config.txt");
			if(file.isFile() && file.exists()){
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferReader = new BufferedReader(read);
				while((s = bufferReader.readLine()) != null){
					Info_Peer.local.peerNum++;
					String info[] = s.split(" ");
					if(Info_Peer.local.nick.peerName.equals(info[0])){
						Info_Peer.local.nick.IP = info[1];
						Info_Peer.local.nick.port = Integer.parseInt(info[2]);
						if(info.length>2) 
						for(int i = 3; i < info.length; i++){
							BufferedReader reader = new BufferedReader(new FileReader(file));
							while((s = reader.readLine()) != null){
							String temp[] = s.split(" ");
							if(info[i].equals(temp[0])){
							Node node = new Node(temp[0], temp[1], Integer.parseInt(temp[2]));
							Info_Peer.local.neighbor.add(node);
							writer.write(Info_Peer.local.nick.peerName + " neighbour peer information:");
							writer.write(node.peerName + " ");
							}
							}
						}
						writer.write("\t\n");
						System.out.println("Local peer information:");
						Info_Peer.local.nick.NodeInfo();
						System.out.println("Neighbour peers information:");
						
						for(int i = 0; i<Info_Peer.local.neighbor.size(); i++){
							Info_Peer.local.neighbor.get(i).NodeInfo();
						}
					}
				}
			}else{
				System.out.println("Configure file is not exist!");
				writer.write("Configure file is not exist!\r\n");
			}		
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public static void do_register(String fileName){
		FileWriter writer = null;

		try{		
			File file = new File(Info_Peer.local.path + File.separator +
					fileName);
			if(!file.exists()){
				System.out.println(fileName+" is not exist!");
				
			}else{
				writer = new FileWriter("./peerLog.txt",true);	
				
				Info_Peer.local.fileList.add(fileName);
				System.out.println("File "+fileName + " is do_registered !");
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(new Date());
				writer.write(time + "\t\tFile "+fileName + " is do_registered  on the local peer!\r\n");
				writer.close();	
			}	

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void do_unregister(String fileName){
		FileWriter writer = null;
		try{	
			writer = new FileWriter("./peerLog.txt",true);
			Info_Peer.local.fileList.remove(fileName);		
			System.out.println("File "+fileName + " is undo_registered !");
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			writer.write(time + "\t\tFile "+fileName + " is undo_registered  on the index server!\r\n");
			writer.close();	

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
public void File_look(String path){
		File file = new File(path);
		if(!file .exists() && !file.isDirectory()){        
		    file .mkdir();    
		} 
		String test[];
		test = file.list();
		if(test.length!=0){
			for(int i = 0; i<test.length; i++){
				do_register(test[i]);
			}
		}
	}
public void talk(procedure pf)throws IOException{
		
		boolean exit = false;
		
		String fileName = null;
		
		BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
		
		
		while(!exit){
			System.out.println("\n1 Set up peer \n2 Register all file\n3 Search a file\n4 Exit");
			switch(Integer.parseInt(localReader.readLine())){
			case 1:{					
				System.out.println("Enter the peer name:");
				Info_Peer.local.nick.peerName = localReader.readLine();
				Info_Peer.initial();
				reading(Info_Peer.local.config);
				ServerSocket server = null;
			    try{
			    	server = new ServerSocket(Info_Peer.local.nick.port);
			    	System.out.println("\nServer started...Enjoy!");
			    	new PThread(server, pf);
			    }catch(IOException e){
			    	e.printStackTrace();
			    }
				break;
			}
			
			case 2:{
			
				File_look(Info_Peer.local.path);	
				break;	
			}
			case 3:{			

				Info_Peer.dest.destPeer = new ArrayList<Node>();
				Info_Peer.local.hitQueryRequest = 0;
				System.out.println("Enter the file name:");
				fileName = localReader.readLine();
				System.out.println("\nStart processing...\n");
				
				int num = Info_Peer.local.messageNum + 1;
				MessageID varta_no = new MessageID(num, Info_Peer.local.nick);
            	
            	
				pf.query(varta_no, Info_Peer.local.TTL-1, fileName);
				
				long runtime = 0;
				long start = System.currentTimeMillis();
				
				while(runtime<Info_Peer.local.cutoffTime){
					long end = System.currentTimeMillis();
					runtime = end - start;
				}

				
				if(Info_Peer.dest.destPeer.size()!=0){
					int index = 0;
					int indexNum = 0;
					System.out.println(fileName + " found on peers!");
					System.out.println("\n1 Download the file\n2 Cancel and back");
					switch(Integer.parseInt(localReader.readLine())){
					case 1:	
						System.out.println("The destination peer list is:");
						for(int i=0; i<Info_Peer.dest.destPeer.size(); i++){
							index = i + 1;
							System.out.println(index + ":" + Info_Peer.dest.destPeer.get(i).IP + " " + Info_Peer.dest.destPeer.get(i).port);
						}
						System.out.println("Chose which peer to download the file:");
						indexNum = Integer.parseInt(localReader.readLine());

						new DThread(Info_Peer.local.nick.port+1,fileName);
						pf.downLoad(fileName, indexNum, Info_Peer.local.nick.IP, Info_Peer.local.nick.port+1);					
						break;
					case 2:
						break;
					default:
						break;			
					}
				}else{
					System.out.println(fileName + " is not found on peers!");
				}
				break;
			}
			
			case 4:{
				exit = true;
				System.exit(0);
				break;
			}
			default:
				break;
			}
			
		}	
	}
	
	public static void main(String args[]){
		Peer peer = new Peer();
		procedure pf = new procedure();
		//peer.File_look(Info_Peer.local.path);
		new WeThread(Info_Peer.local.path);
		try {
			peer.talk(pf);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}


class WeThread extends Thread {
	String path = null;

	public WeThread(String path){
		this.path = path;
		
		start();
	}
	
	public void run(){
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				
				if(Info_Peer.local.fileList.size()!=0){
					for(int i = 0; i < Info_Peer.local.fileList.size(); i++){
						File file = new File(path + File.separator +
								Info_Peer.local.fileList.get(i));
						if(!file.exists()){
							System.out.println(Info_Peer.local.fileList.get(i)+" is removed!");
							Peer.do_unregister(Info_Peer.local.fileList.get(i));
							
						}
					}
				}
			}
			
		}, 1000, 100);
		     
	}
}



class DThread extends Thread{
	int port;
	String fileName;
	public DThread(int port,String fileName){
		this.port = port;
		this.fileName = fileName;
		start();
	}
	
	public void run(){
		try {
			ServerSocket server = new ServerSocket(port);
			//while(true){
				Socket sc = server.accept();  
                receiveFile(sc,fileName);  
                sc.close();
                server.close();
			//}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
		public static void receiveFile(Socket sc, String fileName) throws IOException{
		byte[] inputByte = null;  
        int length = 0;  
        DataInputStream dis = null;  
        FileOutputStream fos = null;  
        String filePath = "./Look/" + fileName;  
        try {  
            try {  
                dis = new DataInputStream(sc.getInputStream());  
                File f = new File("./Look");  
                if(!f.exists()){  
                    f.mkdir();    
                }  

                fos = new FileOutputStream(new File(filePath));      
                inputByte = new byte[1024];     
                System.out.println("\nStart receiving..."); 
                System.out.println("display file " + fileName);
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {  
                    fos.write(inputByte, 0, length);  
                    fos.flush();      
                }  
                System.out.println("Finish receive:"+filePath);  
            } finally {  
                if (fos != null)  
                    fos.close();  
                if (dis != null)  
                    dis.close();  
                if (sc != null)  
                    sc.close();   
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
}




