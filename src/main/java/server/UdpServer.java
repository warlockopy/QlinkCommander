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
			
			while (true){
				
				//System.out.println ("Waiting for report...\n");
				
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
				
				/*
				if (mobileId.equals("861074023783734"))
					System.out.println ("*****\nRONDA\n*****");
				else if (mobileId.equals("861074023780227"))
					System.out.println ("**********\nSUPERVISOR\n**********");
				*/
				
				
				if (!savedUnits.contains(mobileId))
					save (mobileId, model, ipAddress.toString(), port);
				
				
				if (isGV200){
					System.out.println (incomingMessage);
					System.out.println ("Mobile ID:  " + mobileId);
					System.out.println ("IP address: " + ipAddress);
					System.out.println ("Port:       " + port);
					System.out.println ("---------------------------------");
				}
				
				if (isGV200 && !idSet.contains(mobileId)){
					String commandString = commandGV55;
					//commandString = "AT+GTCFG=gmt100,gmt100,gmt100,1,,0,0,3F,2,,1FFFF,,0,0,300,0,,0,0,,,FFFF$";
					commandString = "AT+GTAIS=gv200,1,250,28000,5,1,0,0,0,0,1,,,,0000$";
					
					Command command = new Command (commandString, mobileId);
					saveString ("Id: " + mobileId + "\nCommand: " + commandString + "\n----\n", "Enviados.txt");
					
					DatagramPacket sendPacket;
					byte [] sendData = command.getMessage().getBytes ();
					sendPacket = new DatagramPacket (sendData, sendData.length, ipAddress, port);
					sock.send (sendPacket);
					
					idSet.add(mobileId);
					System.out.println ("Command " + commandString + " sent to mobile " + mobileId + ".\nCount = " + idSet.size());
					System.out.println ("---------------------------------");
				}
				
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
	
}
