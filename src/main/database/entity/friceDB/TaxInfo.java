package entity.friceDB;
/**
 * 有关taxID的表格
 * @author zong0jie
 *
 */
public class TaxInfo {
	
	/**
	 * NCBI的物种ID
	 */	
	private int taxID;
	/**
	 * NCBI的物种ID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	/**
	 * NCBI的物种ID
	 */
	public int getTaxID() {
		return this.taxID;
	}
	
	/**
	 * KEGG上的缩写
	 */
	private String abbr;//缩写
	/**
	 * KEGG上的缩写
	 */
	public void setAbbr(String abbr) {
		this.abbr=abbr;
	}
	/**
	 * KEGG上的缩写
	 */
	public String getAbbr() {
		return this.abbr;
	}
	
	/**
	 * 拉丁名
	 */
	private String latin;//常用名
	/**
	 * 拉丁名
	 */
	public void setLatin(String latin) {
		this.latin=latin;
	}
	/**
	 * 拉丁名
	 */
	public String getLatin() {
		return this.latin;
	}
	
	/**
	 * 常用名
	 */
	private String comName;//常用名
	/**
	 * 常用名
	 */
	public void setComName(String comName) {
		this.comName=comName;
	}
	/**
	 * 常用名
	 */
	public String getComName() {
		return this.comName;
	}
	
	/**
	 * 中文名
	 */
	private String chnName;//中文名
	/**
	 * 中文名
	 */
	public void setChnName(String chnName) {
		this.chnName=chnName;
	}
	/**
	 * 中文名
	 */
	public String getChnName() {
		return this.chnName;
	}

}
