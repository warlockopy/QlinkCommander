package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class QueclinkReport {
	
	private static SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddhhmmss");
	protected ArrayList <Integer> templateIds;
	protected String protocolVersion;
	protected String uniqueId;
	protected String deviceName;
	protected long sendTime;
	protected int countNumber;
	
	private final double MILE2KM = 1.609344;
	private static final long timeAdjust = -4 * 3600;
	
	public abstract String encode ();
	
	public static long toSeconds (final String utcTime){
		Date date;
		long seconds = 0;
		
		try {
			date = format.parse(utcTime);
			seconds = date.getTime() / 1000 + timeAdjust;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return seconds;
	}
	
	public abstract int getTemplateId ();
	
	//Se usa cuando se generan varios reportes Scope
	//a partir de un reporte Queclink
	public int getTemplateIdAt (int index){
		
		int ans = getTemplateId ();
		
		if (templateIds != null && index >= 0 && index < templateIds.size ())
			ans = templateIds.get(index);
		
		return ans;
	}
	
	public void addTemplateId (int id){
		templateIds.add(id);
	}
	
	public String getUnitId (){
		return uniqueId;
	}
	
	public int toKm (double mileage){
		return (int) (mileage * 1.0);
	}
	
	public String getQueclinkVersion (){
		
		String code = "??";
		String ans = "Unknown";
		
		if (protocolVersion.length() > 2)
			code = protocolVersion.substring(0, 2);
		
		if (code.equals("02"))      ans = "GL200";
		else if (code.equals("04")) ans = "GV200";
		else if (code.equals("06")) ans = "GV300";
		else if (code.equals("08")) ans = "GMT100";
		else if (code.equals("0F")) ans = "GV55";
		else if (code.equals("10")) ans = "GV55 LITE";
		else if (code.equals("11")) ans = "GL500";
		else if (code.equals("1A")) ans = "GL300";
		else if (code.equals("1F")) ans = "GV500";
		else    ans = "<Unknown code " + code + ">";
		
		return ans;
	}
	
	public static String getQueclinkVersion (String protocolVersion){
		
		String code = "??";
		String ans = "Unknown";
		
		if (protocolVersion.length() > 2)
			code = protocolVersion.substring(0, 2);
		
		if (code.startsWith("02"))      ans = "GL200";
		else if (code.startsWith("04")) ans = "GV200";
		else if (code.startsWith("06")) ans = "GV300";
		else if (code.startsWith("08")) ans = "GMT100";
		else if (code.startsWith("0F")) ans = "GV55";
		else if (code.startsWith("10")) ans = "GV55 LITE";
		else if (code.startsWith("11")) ans = "GL500";
		else if (code.startsWith("1A")) ans = "GL300";
		else if (code.startsWith("1F")) ans = "GV500";
		else    ans = "<Unknown code " + code + ">";
		
		return ans;
	}
	
	public void echo (Object obj){
		System.out.println (obj);
	}

}
