import java.util.*;
public class Conversion {
	
	
	@SuppressWarnings("serial")
	private static final Map<Character,String> map = new HashMap<>() {{
		put('0',"0000");
		put('1',"0001");
		put('2',"0010");
		put('3',"0011");
		put('4',"0100");
		put('5',"0101");
		put('6',"0110");
		put('7',"0111");
		put('8',"1000");
		put('9',"1001");
		put('A',"1010");
		put('B',"1011");
		put('C',"1100");
		put('D',"1101");
		put('E',"1110");
		put('F',"1111");
	}};
	
	
	
	
	static String hexToBin(String str)
	{	
		str= str.toUpperCase();
		String ans = "";
		for(char c:str.toCharArray())
		{
			ans += map.get(c);
		}
		
		return ans;
	}
	

}
