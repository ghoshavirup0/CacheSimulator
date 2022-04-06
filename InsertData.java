import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InsertData {

	private Cache cache;
	private Map<String, String> map;
	private List<String> data;
	
	
	int l1Reads, l1ReadMiss, l1Writes, l1WriteMiss, l1WriteBacks;
	int l2Reads, l2ReadMiss, l2Writes, l2WriteMiss, l2WriteBacks;
	int totalMemoryTraffic;
	int l1EvictionDueToInclusionCount = 0;
	int OPTGlobalIndex = 0;
	
	public InsertData(Cache cache, Map<String, String> map, List<String> data) {
		this.cache = cache;
		this.map = map;
		this.data = data;
		
		
		l1Reads = l1ReadMiss = l1Writes = l1WriteMiss = l1WriteBacks = 0;
		l2Reads = l2ReadMiss = l2Writes = l2WriteMiss = l2WriteBacks = 0;
		
		
	
		insert();
	}
	
	
	int getIndexAtL1Cache(String str)
	{
		try {
		return Integer.parseInt(str.substring(cache.l1Tag,cache.l1Tag+cache.l1Index),2);
		}catch(Exception e) {
			return 0;
		}
	}
	
	int getIndexAtL2Cache(String str)
	{
		try {
		return Integer.parseInt(str.substring(cache.l2Tag,cache.l2Tag+cache.l2Index),2);
		}
		catch(Exception e)
		{
			return 0;
		}
	}
	
	String getTagL1Cache(String str)
	{
		return str.substring(0,cache.l1Tag);
	}
	
	String getTagL2Cache(String str)
	{
		return str.substring(0,cache.l2Tag);
	}
	
	class Node{
		String str;
		int index;
		public Node(String str, int index) {
			super();
			this.str = str;
			this.index = index;
		}
		public String getStr() {
			return str;
		}
		public void setStr(String str) {
			this.str = str;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		
	}
	
	
	Map<Integer, List<Node>> hexData = new HashMap<>();
	
	private void insert() {
		// TODO Auto-generated method stub

		
		for(int i=0; i<data.size(); i++)
		{
			String str = data.get(i);
			
			String temp = str.split(" ")[1];
			
			int index = getIndexAtL1Cache(map.get(temp));
			
			if(!hexData.containsKey(index))
				hexData.put(index, new ArrayList<>());
			
			hexData.get(index).add(new Node(temp,i));
		}
		
		
		for(int i=0; i<data.size(); i++)
		{	
			String str = data.get(i);
			
			OPTGlobalIndex = i;
			
			boolean readOrWrite = str.split(" ")[0].toLowerCase().equals("r");
			
			str = str.split(" ")[1];
			
			if(readOrWrite)
				readOperationL1(str, map.get(str));
			else
				writeOperationL1(str, map.get(str));
			
			
			
		}
		
		

		if(cache.L2.size() == 0)
		{
			totalMemoryTraffic = l1ReadMiss + l1WriteMiss +l1WriteBacks;
		}
		else
			totalMemoryTraffic = l2ReadMiss + l2WriteMiss +l2WriteBacks + l1EvictionDueToInclusionCount;

		printCache();
		 
		 
		
	}

	
	
	// PSEUDO LRU
	
	void allocate(int ar[], int mid, int idx, int levelValue, int dir)
    {
        if(levelValue == 0)
        {
            ar[idx] = dir;
            return;
        }
        else if(mid>idx)
        {
            ar[mid] = 0;
            allocate(ar, mid - levelValue, idx, levelValue/2, dir);
            
        }
        else
        {
            ar[mid] = 1;
            allocate(ar, mid + levelValue, idx, levelValue/2, dir);
        }
    }
	
	void updatePLRU(int ar[], int index) {
		
		 int idx = index;
         int dir = 0;
         if(index%2 != 0)
         {
             idx --;
             dir = 1;
         }
         int mid = (ar.length-1)/2;
         allocate(ar, mid, idx, (mid+1)/2, dir);
         
	}
	
	
	
	
	
	
	// ******************************************************					L1 CACHE					******************************************************
	
	int blankCountL1 = 0;
	List<Integer> blankIndexL1 = new ArrayList<>();
	int rowIndex = 0;
	// READ OPERATION FOR L1 CACHE
	private void readOperationL1(String data, String bits) {
		// TODO Auto-generated method stub
		
		List<CacheBlock> li = cache.L1.get(getIndexAtL1Cache(bits));
		String tag = getTagL1Cache(bits);
		
		
		l1Reads++;
		for(CacheBlock cb: li)
		{
			if(cb.tag.equals(tag)) {
				readHitL1(tag, li, cb);
				
				// PLRU
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], li.indexOf(cb));
				
				return;
			}
		}
		
		rowIndex = getIndexAtL1Cache(bits);
		
		l1ReadMiss++;
		
		// 	IF CACHE IS EMPTY THEN ADD THE DATA AND DECREASE THE LRU COUNTER VALUE FOR OTHER DATA BLOCKS
		if(li.size()<cache.l1Set )
		{
			
			for(CacheBlock cb: li)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
				cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
			}
			
			
			if(blankCountL1 != 0)
			{
				li.add(blankIndexL1.get(0),new CacheBlock(data, tag, cache.l1Set -1 , false));
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], blankIndexL1.remove(0));
				blankCountL1--;
				
			}
			else
			{
				li.add(new CacheBlock(data, tag, cache.l1Set -1 , false));
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], li.size()-1);
			}
			
			if(cache.L2.size() != 0)
			{
				readOperationL2(data, bits, false, null);
			}
		}
		else // USE REPLACEMENT POLICY
		{
			updateCacheL1(data, tag, li, true);
			
		}
		
		
	}
	// READ-HIT OPERATION IN L1 CACHE
	private void readHitL1(String tag, List<CacheBlock> li, CacheBlock c) {
			// TODO Auto-generated method stub
		
		// LRU
			int value = c.getLRUaccessCounter();
			
			for(CacheBlock cb: li)
			{
				if(cb.tag.equals(tag)) {
					
					cb.setLRUaccessCounter(cache.l1Set-1);
					
				}
				else if(cb.getLRUaccessCounter() > value)
				{
					cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
				}
			}
			
			
		}
	
	
	
	

	// WRITE OPERATION FOR L1 CACHE
	private void writeOperationL1(String data, String bits) {
		// TODO Auto-generated method stub
		
		List<CacheBlock> li = cache.L1.get(getIndexAtL1Cache(bits));
		String tag = getTagL1Cache(bits);
		
		
		
		l1Writes++;
		for(CacheBlock cb: li)
		{
			if(cb.tag.equals(tag)) {
				writeHitL1(tag, li, cb);
				cb.setDirtyBit(true);
				
				// PLRU
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], li.indexOf(cb));
				
				return;
			}
		}
		rowIndex = getIndexAtL1Cache(bits);
		
		l1WriteMiss++;
		
		// 	IF CACHE IS EMPTY THEN ADD THE DATA AND DECREASE THE LRU COUNTER VALUE FOR OTHER DATA BLOCKS
		if(li.size()<cache.l1Set)
		{
			
			
			for(CacheBlock cb: li)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
				cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
			}
				
			
			if(blankCountL1 != 0)
			{
				li.add(blankIndexL1.get(0),new CacheBlock(data, tag, cache.l1Set -1 , true));
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], blankIndexL1.remove(0));
				blankCountL1--;
			}
			else
			{
				li.add(new CacheBlock(data, tag, cache.l1Set -1 , true));
				updatePLRU(cache.plruL1[getIndexAtL1Cache(bits)], li.size()-1);
			}
				
			
			if(cache.L2.size() != 0)
			{
				readOperationL2(data, bits, false, null);
			}
			
		}
		else // USE REPLACEMENT POLICY
		{
			updateCacheL1(data, tag, li, false);
		}
		
		
	}
	
	// WRITE-HIT OPERATION IN L1 CACHE
	private void writeHitL1(String tag, List<CacheBlock> li, CacheBlock c) {
		// TODO Auto-generated method stub
		int value = c.getLRUaccessCounter();
		
		for(CacheBlock cb: li)
		{
			if(cb.tag.equals(tag)) {
				
				cb.setLRUaccessCounter(cache.l1Set-1);
			}
			else if(cb.getLRUaccessCounter() > value)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
			}
		}
		
	}

	
	
	
	
	
	
	// ******************************************************					L2 CACHE					******************************************************

	
	// READ OPERATION IN L2 CACHE
	private void readOperationL2(String data, String bits, boolean isEvicted, CacheBlock evictedBlock) {
		// TODO Auto-generated method stub
		
		List<CacheBlock> li = cache.L2.get(getIndexAtL2Cache(bits));
		String tag = getTagL2Cache(bits);
		
		if(isEvicted)
		{
			writeOperationL2(evictedBlock.getData(),map.get(evictedBlock.getData()));
		}
		
		l2Reads++;
		
		for(CacheBlock cb: li)
		{
			if(cb.getTag().equals(tag)) {
				readHitL2(tag, li, cb);
				
				// PLRU
				updatePLRU(cache.plruL2[getIndexAtL2Cache(bits)], li.indexOf(cb));
				return;
			}
		}
		
		l2ReadMiss++;
		rowIndex = getIndexAtL1Cache(bits);
		//	IF CACHE IS EMPTY THEN ADD THE DATA AND DECREASE THE LRU COUNTER VALUE FOR OTHER DATA BLOCKS
		if(li.size()<cache.l2Set)
		{
			for(CacheBlock cb: li)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
				cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
			}
				
				
			li.add(new CacheBlock(data, tag, cache.l2Set -1 , false));
			
			// PLRU
			updatePLRU(cache.plruL2[getIndexAtL2Cache(bits)], li.size()-1);
		}
		else // USE REPLACEMENT POLICY
		{					
			updateCacheL2(data, tag, li, true);
			
		}
		
	}


	// READ-HIT OPERATION IN L2 CACHE
	private void readHitL2(String tag, List<CacheBlock> li, CacheBlock c) {
		// TODO Auto-generated method stub
		int value = c.getLRUaccessCounter();
		
		for(CacheBlock cb: li)
		{
			if(cb.tag.equals(tag)) {
				
				cb.setLRUaccessCounter(cache.l2Set-1);
				
				
				
			}
			else if(cb.getLRUaccessCounter() > value)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
			}
		}
	}


	// WRITE OPERATION FOR L2 CACHE
	private void writeOperationL2(String data, String bits) {
		// TODO Auto-generated method stub
		List<CacheBlock> li = cache.L2.get(getIndexAtL2Cache(bits));
		String tag = getTagL2Cache(bits);
		
		l2Writes++;
		
		for(CacheBlock cb: li)
		{
			if(cb.getTag().equals(tag)) {
				writeHitL2(tag, li, cb);
				cb.setDirtyBit(true);
				
				// PLRU
				updatePLRU(cache.plruL2[getIndexAtL2Cache(bits)], li.indexOf(cb));
				
				return;
			}
		}
		
		l2WriteMiss++;
		rowIndex = getIndexAtL1Cache(bits);
		
		//	IF CACHE IS EMPTY THEN ADD THE DATA AND DECREASE THE LRU COUNTER VALUE FOR OTHER DATA BLOCKS
		if(li.size()<cache.l2Set)
		{
			for(CacheBlock cb: li)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
				cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
			}
				
				
			li.add(new CacheBlock(data, tag, cache.l2Set -1 , true));
			
			// PLRU
			updatePLRU(cache.plruL2[getIndexAtL2Cache(bits)], li.size()-1);
		}
		else // USE REPLACEMENT POLICY
		{
			updateCacheL2(data, tag, li, false);
		}
		
	}

	


	// WRITE-HIT OPERATION IN L2 CACHE
	private void writeHitL2(String tag, List<CacheBlock> li, CacheBlock c) {
		// TODO Auto-generated method stub
		int value = c.getLRUaccessCounter();
		
		for(CacheBlock cb: li)
		{
			if(cb.tag.equals(tag)) {
				
				cb.setLRUaccessCounter(cache.l2Set-1);
			}
			else if(cb.getLRUaccessCounter() > value)
			{
				cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
			}
		}
		
	}



	
	
	
	
	
	
	
	
	
	
	
	// ******************************************************					REPLACEMENT POLICY					******************************************************
	
	
	private int getPLRUEvictionIndex(int[] ar) {
		// TODO Auto-generated method stub
		int mid = (ar.length-1)/2;
		int levelValue = (mid+1)/2;
		
		return deAllocate(mid,levelValue,ar);
	}

	
	private int deAllocate(int mid, int levelValue, int[] ar) {
		// TODO Auto-generated method stub
		if(levelValue == 0)
		{
			if(ar[mid] == 0)
			{
				ar[mid] = 1;
				return mid+1;
			}
			else
			{
				ar[mid] = 0;
				return mid;
			}
		}
		else if(ar[mid] == 0)
		{
			ar[mid] = 1;
			return deAllocate(mid + levelValue, levelValue/2, ar);
		}
		else
		{
			ar[mid] = 0;
			return deAllocate(mid - levelValue, levelValue/2, ar);
		}
		
	}


	// REPLACEMENT POLICY FOR L1 CACHE (DEFAULT LRU)
	private void updateCacheL1(String data,String tag, List<CacheBlock> li, boolean read) {
		// TODO Auto-generated method stub
		
		int index = 0;
		
		
		switch(cache.replacementPolicy)
		{
			case 1:{
				index = getPLRUEvictionIndex(cache.plruL1[getIndexAtL1Cache(map.get(data))]);
				break;
			}
			case 2:{
				
				index = getOPTEvictionIndex(li);
				int val = li.get(index).getOPTaccessCounter();
				for(CacheBlock cb: li)
				{
					if(cb.getOPTaccessCounter()<val)
						cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
				
				}
				
				break;
			}
			default:{

				for(int i=0; i<li.size(); i++)
				{
					CacheBlock cb = li.get(i);
					if(cb.getLRUaccessCounter() == 0)
					{
						index = i;
					}
					else
					{
						cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
					}
				}
				
				break;
			}
		}
		
		CacheBlock evicted = li.remove(index);
		
		if(evicted.isDirtyBit())
		{
			l1WriteBacks++;
		}
		
		
		
		li.add(index, new CacheBlock(data, tag, cache.l1Set -1 , true));
		
		if(read)
		{
			li.get(index).setDirtyBit(false);
		}
		
	
		 
		if(cache.L2.size() != 0 )
		{
			if(evicted.isDirtyBit())
				writeOperationL2(evicted.getData(), map.get(evicted.getData()));
			
			readOperationL2(data, map.get(data), false, null);
		}
			
		
		
	}
	
	
	
	

	private int getOPTEvictionIndex(List<CacheBlock> li) {
		// TODO Auto-generated method stub
		
		
		int index = 0;
		
		int ar[] = new int[li.size()];
		
		Arrays.fill(ar, Integer.MAX_VALUE);
		
		List<Node> nodeList = hexData.get(rowIndex);
		
		
		for(int i=0; i<ar.length; i++)
		{
			CacheBlock cb = li.get(i);
			
			for(int j=0; j<nodeList.size(); j++)
			{
				Node node = nodeList.get(j);
				
				if(node.getIndex()>OPTGlobalIndex)
				{
					String tag = getTagL1Cache(map.get(node.getStr()));
					
					if(cb.getTag().equals(tag) ) {
						ar[i] = node.getIndex() - OPTGlobalIndex;
						break;
					}
				}
			}
		}
		

		
		int max = -1;
		for(int i : ar)
			max = Math.max(i, max);
		
		int leastAccessed = -1;
		
		for(int i=0; i<li.size(); i++)
		{
			if(ar[i] == max && leastAccessed < li.get(i).getOPTaccessCounter())
			{
				leastAccessed = li.get(i).getOPTaccessCounter();
				index = i;
				return i;
			}
		}
		
		return index;
		
		
	}


	// REPLACEMENT POLICY FOR L2 CACHE (DEFAULT LRU)
	private void updateCacheL2(String data, String tag, List<CacheBlock> li, boolean read) {
		// TODO Auto-generated method stub
		int index = 0;
		
		
		switch (cache.replacementPolicy) {
			case 1:{
				
				index = getPLRUEvictionIndex(cache.plruL2[getIndexAtL2Cache(map.get(data))]);
				break;
			}
			case 2:{
				index = getOPTEvictionIndex(li);
				int val = li.get(index).getOPTaccessCounter();
				for(CacheBlock cb: li)
				{
					if(cb.getOPTaccessCounter()<val)
						cb.setOPTaccessCounter(cb.getOPTaccessCounter()+1);
				
				}
				
				break;
				
			}
			default:{
				for(int i=0; i<li.size(); i++)
				{
					CacheBlock cb = li.get(i);
					if(cb.getLRUaccessCounter() == 0)
					{
						index = i;
						
					}
					else
					{
						cb.setLRUaccessCounter(cb.getLRUaccessCounter()-1);
					}
				}
				break;
			}
		}
		
		
		
		CacheBlock evicted = li.remove(index);
		
		if(evicted.isDirtyBit())
		{
			l2WriteBacks++;
		}
		
		li.add(index, new CacheBlock(data, tag, cache.l2Set -1 , true));
		
		if(read)
		{
			li.get(index).setDirtyBit(false);

		}
		
		if(cache.inclusionProperty == 1)
		{
			evictFromL1(evicted);
		}
		
	}
	
	
	
	
	
	
	
	
	private void evictFromL1(CacheBlock evicted) {
		// TODO Auto-generated method stub
		
		int index = getIndexAtL1Cache(map.get(evicted.getData()));
		String tag =getTagL1Cache(map.get(evicted.getData()));
		
		List<CacheBlock> li = cache.L1.get(index);
		
		for(CacheBlock cb: li)
		{
			if(cb.getTag().equals(tag))
			{
				int idx = li.indexOf(cb);
				blankCountL1++;
				blankIndexL1.add(idx);
				CacheBlock temp = li.remove(idx);
				if(temp.isDirtyBit())
					l1EvictionDueToInclusionCount++;
				break;
			}
		}
		
		
	}


	// ******************************************************				PRINTING THE OUTPUT				******************************************************
	private void printCache() {
		// TODO Auto-generated method stub
		System.out.println("===== Simulator configuration =====");
		System.out.println("BLOCKSIZE:             "	+	cache.blockSize);
		System.out.println("L1_SIZE:               "	+	cache.L1_Size);
		System.out.println("L1_ASSOC:              "	+	cache.L1_Assoc);
		System.out.println("L2_SIZE:               "	+	cache.L2_Size);
		System.out.println("L2_ASSOC:              "	+	cache.L2_Assoc);
		System.out.println("REPLACEMENT POLICY:    "	+	(cache.replacementPolicy == 0?"LRU":(cache.replacementPolicy == 1?"Pseudo-LRU":"Optimal")));
		System.out.println("INCLUSION PROPERTY:    "	+	(cache.inclusionProperty == 0?"non-inclusive":"inclusive"));
		System.out.println("trace_file:            "	+	cache.traceFile);
		
		
		
		System.out.println("===== L1 contents =====");
		
		for(int i=0; i<cache.L1.size(); i++)
		{
			//if(cache.L1.get(i).size()!=0)
			System.out.print("Set     "+i+":");
			for(CacheBlock cb:cache.L1.get(i)) {
				System.out.print(" "+binaryToHex(cb.getTag())+(cb.isDirtyBit()?" D":" "));
			}
			System.out.println();
		}
		
		if(cache.L2.size() != 0)
		{
			System.out.println("===== L2 contents =====");
			
			for(int i=0; i<cache.L2.size(); i++)
			{
				//if(cache.L1.get(i).size()!=0)
				System.out.print("Set     "+i+":");
				for(CacheBlock cb:cache.L2.get(i)) {
					System.out.print(" "+binaryToHex(cb.getTag())+(cb.isDirtyBit()?" D":" "));
				}
				System.out.println();
			}
		}
		
		System.out.println("===== Simulation results (raw) =====");
		
		System.out.println("a. number of L1 reads:        "	+	l1Reads);
		System.out.println("b. number of L1 read misses:  "	+	l1ReadMiss);
		System.out.println("c. number of L1 writes:       "	+	l1Writes);
		System.out.println("d. number of L1 write misses: "	+	l1WriteMiss);
		System.out.println("e. L1 miss rate:              "	+	String.format("%.6f",getL1MissRate()));
		System.out.println("f. number of L1 writebacks:   "	+	l1WriteBacks);
		System.out.println("g. number of L2 reads:        "	+	l2Reads);
		System.out.println("h. number of L2 read misses:  "	+	l2ReadMiss);
		System.out.println("i. number of L2 writes:       "	+	l2Writes);
		System.out.println("j. number of L2 write misses: "	+	l2WriteMiss);
		System.out.println("k. L2 miss rate:              "	+	String.format("%.6f",getL2MissRate()));
		System.out.println("l. number of L2 writebacks:   "	+	l2WriteBacks);
		System.out.println("m. total memory traffic:      "	+	totalMemoryTraffic);
	}
	
	
	
	private double getL2MissRate() {
		// TODO Auto-generated method stub
		return (double)(l2ReadMiss)/(double)(l1ReadMiss + l1WriteMiss);
	}


	private double getL1MissRate() {
		// TODO Auto-generated method stub
		return (double)(l1ReadMiss + l1WriteMiss)/(double)(l1Reads + l1Writes);
	}


	private String binaryToHex(String str)
	{
		return new BigInteger(str,2).toString(16);
	}
	
	

}
