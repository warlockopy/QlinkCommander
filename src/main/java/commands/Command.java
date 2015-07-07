package commands;

public class Command {
	
	private String message;
	private String mobileId;
	
	public Command (final String message, final String mobileId){
		this.message = message;
		this.mobileId = mobileId;
	}
	
	public String getMessage (){
		return message;
	}
	
	public String getMobileId (){
		return mobileId;
	}
	
	public boolean hasMobileId (final String mobileId){
		return this.mobileId.equals(mobileId);
	}
	
	@Override
	public String toString (){
		return getMessage ();
	}
	
}
