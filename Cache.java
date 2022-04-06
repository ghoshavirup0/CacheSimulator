import java.util.*;


public class Cache {
	
	List<List<CacheBlock>> L1;
	List<List<CacheBlock>> L2;
	
	int l1Tag, l1Index, offset, l1Set;
	int l2Tag, l2Index, l2Set;
	
	int replacementPolicy,inclusionProperty, blockSize;
	int L1_Size, L1_Assoc;
	int L2_Size, L2_Assoc;
	String traceFile;
	
	int log2(int x) {
	    return (int) (Math.log(x) / Math.log(2));
	}
	 
	

	Cache(int l1Index, int l1Set, int l2Index, int l2Set, int blockSize, int replacementPolicy, int inclusionProperty, int L1_Size, int L2_Size, int L1_Assoc, int L2_Assoc, String traceFile)
	{
		this.inclusionProperty = inclusionProperty;
		this.replacementPolicy = replacementPolicy;
		this.blockSize = blockSize;
		this.l1Set = l1Set;
		this.l2Set = l2Set;
		
		L1 = new ArrayList<>();
		L2 = new ArrayList<>();
		
		List<CacheBlock> temp;
		
		for(int i = 0; i<l1Index; i++)
		{
			temp = new ArrayList<>();
			L1.add(temp);
		}
		
		
		for(int i = 0; i<l2Index; i++)
		{
			temp = new ArrayList<>();
			L2.add(temp);
		}
		
		
		offset = log2(blockSize);
		
		this.l1Index = log2(l1Index);
		this.l2Index = log2(l2Index);
		
		l1Tag = 32 - (this.l1Index + offset);
		l2Tag = 32 - (this.l2Index + offset);
		
		this.L1_Size = L1_Size;
		this.L2_Size = L2_Size;
		this.L1_Assoc = L1_Assoc;
		this.L2_Assoc = L2_Assoc;
		this.traceFile = traceFile;
		
		
		initializePLRU();
		
	}


	int plruL1 [][];
	int plruL2 [][];
	int plruL1Mid, plruL2Mid;
	private void initializePLRU() {
		int tempL1Set = l1Set;
		int tempL2Set = l2Set;
		if(l1Set < 2)
		{
			tempL1Set = 2;
		}
		if(l2Set <2)
		{
			tempL2Set = 2;
		}
		// TODO Auto-generated method stub
		plruL1 =new int [L1.size()][tempL1Set-1];
		plruL2 =new int [L2.size()][tempL2Set-1];
		
		plruL1Mid = (tempL1Set-2)/2;
		plruL2Mid = (tempL2Set-2)/2;
		
	}

}
