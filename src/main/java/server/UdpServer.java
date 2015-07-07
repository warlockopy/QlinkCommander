package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.TreeSet;

import commands.Command;
import commands.CommandQueue;
import utilities.Tokenizer;

public class UdpServer extends Thread {
	
	static final int bufferSize = 65536;
	private int localPortNumber;
	private CommandQueue queue;

	
	public UdpServer (int localPort) {
		this.localPortNumber = localPort;
		queue = new CommandQueue ();
	}
	
	public void run (){
		DatagramSocket sock = null;
		TreeSet <String> idSet = new TreeSet ();
		
		//Enviar este comando una vez a cada equipo GV55 que se reporte
		final String commandString = "AT+GTEPS=gmt100,0,250,250,0,0,0,0,0,0,1,1,,,FFFF$";
		
		try{
			sock = new DatagramSocket (localPortNumber);
			byte [] buffer = new byte [bufferSize];
			DatagramPacket incoming = new DatagramPacket (buffer, buffer.length);
			System.out.println ("Started");
			
			while (true){
				
				//System.out.println ("Waiting for report...\n");
				
				sock.receive(incoming);
				byte [] data = incoming.getData ();
				String incomingMessage = new String (data, 0, incoming.getLength ());
				Tokenizer tok = new Tokenizer (incomingMessage);
				String messageType = tok.nextToken();
				String protocolVersion = tok.nextToken();
				boolean isGV55 = protocolVersion.startsWith("0F");
				String mobileId = getMobileIdFrom (incomingMessage);
				InetAddress ipAddress = incoming.getAddress();
				int port = incoming.getPort();
				
				System.out.println (incomingMessage);
				System.out.println ("Mobile ID:  " + mobileId);
				System.out.println ("IP address: " + ipAddress);
				System.out.println ("Port:       " + port);
				System.out.println ("---------------------------------");
				
				if (isGV55 && !idSet.contains(mobileId)){
					Command command = new Command (commandString, mobileId);
					
					DatagramPacket sendPacket;
					byte [] sendData = command.getMessage().getBytes ();
					sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
					sock.send (sendPacket);
					
					idSet.add(mobileId);
					System.out.println ("Command sent to mobile " + mobileId + ".   Count = " + idSet.size());
					System.out.println ("---------------------------------");
				}
				
				/*
				ArrayList <Command> commandsToSend = queue.extractCommandsWithId(mobileId);
				
				//Send commands
				for (Command com : commandsToSend){
					DatagramPacket sendPacket;
					byte [] sendData = com.getMessage().getBytes ();
					sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
					sock.send (sendPacket);
					
					System.out.println ("Command sent to " + mobileId + ": " + com);
					System.out.println ("---------------------------------");
				}*/
				
			}
		}
		catch (IOException e){
			System.err.println("IOException " + e);
		}
	}
	
	public void addCommand (Command com){
		queue.addCommand(com);
		System.out.println ("Command added: " + com.getMessage());
		System.out.println ("---------------------------------");
	}
	
	private String getMobileIdFrom (final String qString){
		Tokenizer tok = new Tokenizer (qString);
		String ans = "None";
		
		if (tok.countTokens() >= 3){
			tok.nextToken(); //+XXXX:GTXXX
			tok.nextToken(); //Protocol version
			ans = tok.nextToken(); //uniqueId
		}
		
		return ans;
	}
	
}
