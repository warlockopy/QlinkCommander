package utilities;

public class Tokenizer {
	
	private int tokensLeft, ptr1, ptr2;
	private String str;
	
	public Tokenizer (final String str){
		tokensLeft = 1;
		this.str = "," + str + ",";
		
		for (int i = 0; i < str.length(); ++i)
			if (str.charAt(i) == ',')
				++tokensLeft;
		
		ptr1 = 0;
		ptr2 = 0;
		
	}
	
	public String nextToken (){
		ptr1 = ptr2++;
		
		if (!hasMoreTokens ()) return "";
		
		while (ptr2 < str.length() && str.charAt(ptr2) != ',')
			++ptr2;
		
		--tokensLeft;
		
		
		return str.substring(ptr1 + 1, ptr2);
	}
	
	public boolean hasMoreTokens (){
		return tokensLeft > 0;
	}
	
	public int countTokens (){
		return tokensLeft;
	}
	
	public int nextInt (){
		int ans = 0;
		String token = nextToken ();
		
		if (token.length() > 0)
			ans = Integer.parseInt(token);
		
		return ans;
	}
	
	public int nextHex (){
		int ans = 0;
		String token = nextToken ();
		
		if (token.length() > 0)
			ans = Integer.parseInt(token, 16);
		
		return ans;
	}
	
	public double nextDouble (){
		double ans = 0.0;
		String token = nextToken ();
		
		if (token.length() > 0)
			ans = Double.parseDouble (token);
		
		return ans;
	}
	
	public boolean nextBoolean (){
		int intValue = nextInt ();
		
		return intValue != 0;
	}
	
	@Override
	public String toString (){
		return countTokens () + " tokens left in string " + str;
	}
}
