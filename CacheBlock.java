
public class CacheBlock {
	String data;
	String tag;
	int LRUaccessCounter;
	int OPTaccessCounter;
	boolean isDirtyBit;
	
	
	
	
	
	public CacheBlock(String data, String tag, int lRUaccessCounter, boolean isDirtyBit) {
		super();
		this.data = data;
		this.tag = tag;
		LRUaccessCounter = lRUaccessCounter;
		this.isDirtyBit = isDirtyBit;
		OPTaccessCounter = 0;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getLRUaccessCounter() {
		return LRUaccessCounter;
	}
	public void setLRUaccessCounter(int lRUaccessCounter) {
		LRUaccessCounter = lRUaccessCounter;
	}
	public boolean isDirtyBit() {
		return isDirtyBit;
	}
	public void setDirtyBit(boolean isDirtyBit) {
		this.isDirtyBit = isDirtyBit;
	}
	
	public int getOPTaccessCounter() {
		return OPTaccessCounter;
	}
	public void setOPTaccessCounter(int oPTaccessCounter) {
		OPTaccessCounter = oPTaccessCounter;
	}
	
	
	
	
	
	
}
