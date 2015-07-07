package packetmain;

import java.net.*;
import java.util.Scanner;

import commands.Command;

import server.UdpServer;

public class Main {
	
	public static void main (String args []) {
		
		UdpServer server = new UdpServer (5000); //port 5000
		Scanner sc = new Scanner (System.in);
		
		server.start();
		
		while (true){
			String message = sc.next();
			
			if (message.equals("exit"))
				break;
			
			String mobileId = sc.next();
			
			server.addCommand (new Command (message, mobileId));
		}
		
		echo ("Terminated");
		server.interrupt();
		sc.close ();
		System.exit(0);
		
	}
	
	public static void echo (Object obj){
		System.out.println (obj);
	}
	
}
