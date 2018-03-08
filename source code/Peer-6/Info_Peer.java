

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class Info_Peer {
	public static class local{
		public static int messageNum;
		public static int peerNum;
		public static int hitQueryRequest;
		public static int TTL = 3;	
		public static int cutoffTime = 3000;
		public static Node nick = new Node();
		public static String path = "./Look";
		public static String config = "./config.txt";
		public static ArrayList<Node> neighbor = new ArrayList<Node>();
		public static ArrayList<String> fileList = new ArrayList<String>();
		public static ConcurrentHashMap<Integer,MessageID> messageTable = new ConcurrentHashMap<Integer,MessageID>();
	}
	
	public static class dest{
		public static ArrayList<Node> destPeer = new ArrayList<Node>();
	}
	
	public static void initial(){
		Info_Peer.local.messageNum = 0;
		Info_Peer.local.peerNum = 0;
		Info_Peer.local.hitQueryRequest = 0;
		Info_Peer.local.neighbor = new ArrayList<Node>();
		Info_Peer.local.messageTable = new ConcurrentHashMap<Integer,MessageID>();
		Info_Peer.dest.destPeer = new ArrayList<Node>();
	}
	
}
