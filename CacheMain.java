import java.io.*;
import java.util.*; 


public class CacheMain {
	
	int blockSize,l1Size,l1Assoc,l2Size,l2Assoc,replacementPolicy,inclusionProperty,l1Rows,l2Rows;
	String traceFile;
	
	private List<String> data;
	private Map<String,String> map;
	
	private Cache cache;
	
	
	public CacheMain(int blockSize, int l1Size, int l1Assoc, int l2Size, int l2Assoc, int replacementPolicy,
			int inclusionProperty, String traceFile) {
		
		this.blockSize = blockSize;
		this.l1Size = l1Size;
		this.l1Assoc = l1Assoc;
		this.l2Size = l2Size;
		this.l2Assoc = l2Assoc;
		this.replacementPolicy = replacementPolicy;
		this.inclusionProperty = inclusionProperty;
		this.traceFile = traceFile;
		
		
		// Calculating the number of rows and sets in l1 and l2  cache
		// Designing the cache architecture
		calculateRows();
		cache = new Cache(l1Rows, l1Assoc, l2Rows, l2Assoc, blockSize, replacementPolicy, inclusionProperty, l1Size, l2Size, l1Assoc, l2Assoc, traceFile);
		
		// Reading the data
		data = new ArrayList<>();
		map = new HashMap<>();
		
		
		// Loading and decoding the data (Backbone)
		readFile();
		
		// Inserting into the cache memory
		insertValues();
		
	}
	
	private void insertValues() {
		// TODO Auto-generated method stub
		 new InsertData(cache, map, data);
		
	}
	
	private void calculateRows() {
		// TODO Auto-generated method stub
		
		l1Assoc = l1Assoc == 0? 1 : l1Assoc;
		l2Assoc = l2Assoc == 0? 1 : l2Assoc;
		
		l1Rows = l1Size / (l1Assoc * blockSize);
		l2Rows = l2Size / (l2Assoc * blockSize);
		
		
	}

	
	private void readFile() {
		
		try {
			File importTraceFile= new File("traces/"+traceFile);
			
			BufferedReader br = new BufferedReader(new FileReader(importTraceFile));
			//br.read();
			String str;
			while((str = br.readLine()) != null)
			{
				
				if(str.length() == 0)
					continue;
				
				data.add(str);
				decodeData(str);
			}
			
			br.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Unable to read the text file");
			e.printStackTrace();
		}
		

	}

	// Decoding the data from text file
	private void decodeData(String str) {
		// TODO Auto-generated method stub
		str = str.trim();
		String ptr = "";
		try {
			ptr = str = str.split(" ")[1];
			if(map.containsKey(ptr))
				return;
		}catch(Exception ignored){
			return;
		}
		
		String apppendZeros = "00000000";
		if(ptr.length() != 8)
		{
			ptr = apppendZeros.substring(0 , 8 - str.length()) + ptr;
			
		}
		
		// Storing data into map where key is the hex value and value will be its binary form
		map.put(str, Conversion.hexToBin(ptr));
		
	}
	
	

	
}
