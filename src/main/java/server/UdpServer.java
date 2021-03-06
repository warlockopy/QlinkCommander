package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import commands.Command;
import commands.CommandQueue;
import utilities.QueclinkReport;
import utilities.Tokenizer;

public class UdpServer extends Thread {
	
	static final int bufferSize = 65536;
	private int localPortNumber;
	private CommandQueue queue;
	private TreeSet <String> savedUnits;
	private final String supervisor = "861074023780227";
	
	//Mensaje para: 861074023738019, 861074023748299, 861074023754602
	private final String mensaje = "AT+GTAIS=gv200,1,250,15000,2,1,0,0,0,0,1,,,,FFFF$";
	private final String ID1 = "861074023738019", ID2 = "861074023748299", ID3 = "861074023754602";
	private boolean done1 = false, done2 = false, done3 = false;

	
	public UdpServer (int localPort) {
		this.localPortNumber = localPort;
		queue = new CommandQueue ();
	}
	
	public void run (){
		DatagramSocket sock = null;
		TreeSet <String> idSet = new TreeSet ();
		savedUnits = new TreeSet ();
		
		final String commandGMT100 = "AT+GTEPS=gmt100,1,250,32000,5,0,0,0,0,0,1,1,,,FFFF$";
		final String commandGV55 = "AT+GTEPS=gv55,1,250,32000,5,0,0,0,0,0,1,,,,FFFF$";
		
		try{
			sock = new DatagramSocket (localPortNumber);
			byte [] buffer = new byte [bufferSize];
			DatagramPacket incoming = new DatagramPacket (buffer, buffer.length);
			System.out.println ("Started");
			
			while (!done1 || !done2 || !done3){
				
				System.out.println ("Waiting for report...\n");
				
				sock.receive(incoming);
				byte [] data = incoming.getData ();
				String incomingMessage = new String (data, 0, incoming.getLength ());
				Tokenizer tok = new Tokenizer (incomingMessage);
				String messageType = tok.nextToken();
				String protocolVersion = tok.nextToken();
				String model = QueclinkReport.getQueclinkVersion(protocolVersion);
				boolean isGMT100 = model.equals("GMT100");
				boolean isGV55 = model.equals("GV55");
				boolean isGV200 = model.equals("GV200");
				String mobileId = getMobileIdFrom (incomingMessage);
				InetAddress ipAddress = incoming.getAddress();
				int port = incoming.getPort();
				
				System.out.println ("Report from " + mobileId + " (" + model + ")");
							
				//if (!savedUnits.contains(mobileId))
				//	save (mobileId, model, ipAddress.toString(), port);
				
				//Enviar mensaje a tres GV200
				if (isGV200){
					if (mobileId.equals(ID1) || mobileId.equals(ID2) || mobileId.equals(ID3)){
						Command command = new Command (mensaje, mobileId);
						DatagramPacket sendPacket;
						byte [] sendData = command.getMessage().getBytes ();
						sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
						sock.send (sendPacket);
						System.out.println ("Sent to " + mobileId);
						
						if (mobileId.equals(ID1)) {done1 = true; }
						if (mobileId.equals(ID2)) {done2 = true; }
						if (mobileId.equals(ID3)) {done3 = true; };
					}
				}
				
				System.out.println ("****************");
				if (done1) { echo (ID1); }
				if (done2) { echo (ID2); }
				if (done3) { echo (ID3); };
				System.out.println ("****************\n");
				
				/*
				if (isGV55 && mobileId.equals("862193020438990")){
					System.out.println (incomingMessage);
					System.out.println ("Mobile ID:  " + mobileId);
					System.out.println ("IP address: " + ipAddress);
					System.out.println ("Port:       " + port);
					System.out.println ("---------------------------------");
				}
				
				if (isGV55 && mobileId.equals ("862193020438990") && !idSet.contains(mobileId)){
					String commandString = commandGV55;
					//commandString = "AT+GTCFG=gmt100,gmt100,gmt100,1,,0,0,3F,2,,1FFFF,,0,0,300,0,,0,0,,,FFFF$";
					//commandString = "AT+GTAIS=gv200,1,250,28000,5,1,0,0,0,0,1,,,,0000$";
					
					Command command = new Command (commandString, mobileId);
					saveString ("Id: " + mobileId + "\nCommand: " + commandString + "\n----\n", "Enviados.txt");
					
					DatagramPacket sendPacket;
					byte [] sendData = command.getMessage().getBytes ();
					sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
					sock.send (sendPacket);
					
					idSet.add(mobileId);
					System.out.println ("Command " + commandString + " sent to mobile " + mobileId + ".\nCount = " + idSet.size());
					System.out.println ("---------------------------------");
				}*/
				
				/*
				
				else {
					String commandString = commandGMT100;
					String name = QueclinkReport.getQueclinkVersion(protocolVersion).toLowerCase() + ",";
					commandString = "AT+CTCFG=" + name + name + "," + "1,0,,,FFFF,,,,,,,,,,,,,,0000$";
					Command command = new Command (commandString, mobileId);
					
					DatagramPacket sendPacket;
					byte [] sendData = command.getMessage().getBytes ();
					sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
					sock.send (sendPacket);
					
					idSet.add(mobileId);
					System.out.println ("Command " + commandString + " sent to mobile " + mobileId + ".\nCount = " + idSet.size());
					System.out.println ("---------------------------------");
				}
				*/
				
				
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
			
			System.out.println ("Done");
		}
		catch (IOException e){
			System.err.println("IOException " + e);
		}
	}
	
	private void save (String mobileId, String model, String ipAddress, int port){
		DateFormat format = new SimpleDateFormat ("yyyy_MM_dd");
		String dateString = format.format (new Date ());
		String dir = "EQUIPOS";
		String fileName = "Reportes" + ".txt";
		
		File directory = new File (dir);
		String path = directory.getAbsolutePath() + "/" + fileName;
		
		if (!directory.exists())
			if (directory.mkdir() == false)
				System.out.println ("Error. No se pudo crear el directorio " + directory.getAbsolutePath());
		
		
		try {
			FileWriter fWriter = new FileWriter (dir + "/" + fileName, true);
			BufferedWriter writer = new BufferedWriter (fWriter);
			
			writer.write("Unit id:    " + mobileId + "\n");
			writer.write("Model:      " + model + "\n");
			writer.write("IP address: " + ipAddress.substring(1) + "\n");
			writer.write("Port:       " + port + "\n");
			writer.write("Time:       " + new Date () + "\n");
			writer.write("------------------------------------------------\n");
			
			savedUnits.add(mobileId);
			
			writer.close ();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveString (String data, String fileName){
		DateFormat format = new SimpleDateFormat ("yyyy_MM_dd");
		String dateString = format.format (new Date ());
		String dir = "MENSAJES";
		
		File directory = new File (dir);
		String path = directory.getAbsolutePath() + "/" + fileName;
		
		if (!directory.exists())
			if (directory.mkdir() == false)
				System.out.println ("Error. No se pudo crear el directorio " + directory.getAbsolutePath());
		
		
		try {
			FileWriter fWriter = new FileWriter (dir + "/" + fileName, true);
			BufferedWriter writer = new BufferedWriter (fWriter);
			writer.write(data);
			writer.close ();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private void echo (Object obj){
		System.out.println (obj);
	}
	
}
