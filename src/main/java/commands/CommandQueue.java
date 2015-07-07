package commands;

import java.util.ArrayList;
import java.util.LinkedList;

public class CommandQueue {
	
	private static final int MAX_QUEUE_SIZE = 1000;
	private LinkedList <Command> commands;
	
	public CommandQueue (){
		commands = new LinkedList ();
	}
	
	public void addCommand (Command com){
		
		if (commands.size () > MAX_QUEUE_SIZE)
			System.out.println ("Warning: Too many commands in queue");
		else
			commands.add(com);
	}
	
	public ArrayList <Command> extractCommandsWithId (final String mobileId){
		ArrayList <Command> ans = new ArrayList ();
		LinkedList <Command> match = new LinkedList (); 
		
		for (Command com : commands)
			if (com.hasMobileId(mobileId))
				match.add(com);
		
		for (Command com : match){
			commands.remove(com);
			ans.add(com);
		}
		
		return ans;
	}
}
