package entity.friceDB;
/**
 * �й�taxID�ı��
 * @author zong0jie
 *
 */
public class TaxInfo {
	
	/**
	 * NCBI������ID
	 */	
	private int taxID;
	/**
	 * NCBI������ID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	/**
	 * NCBI������ID
	 */
	public int getTaxID() {
		return this.taxID;
	}
	
	/**
	 * KEGG�ϵ���д
	 */
	private String abbr;//��д
	/**
	 * KEGG�ϵ���д
	 */
	public void setAbbr(String abbr) {
		this.abbr=abbr;
	}
	/**
	 * KEGG�ϵ���д
	 */
	public String getAbbr() {
		return this.abbr;
	}
	
	/**
	 * ������
	 */
	private String latin;//������
	/**
	 * ������
	 */
	public void setLatin(String latin) {
		this.latin=latin;
	}
	/**
	 * ������
	 */
	public String getLatin() {
		return this.latin;
	}
	
	/**
	 * ������
	 */
	private String comName;//������
	/**
	 * ������
	 */
	public void setComName(String comName) {
		this.comName=comName;
	}
	/**
	 * ������
	 */
	public String getComName() {
		return this.comName;
	}
	
	/**
	 * ������
	 */
	private String chnName;//������
	/**
	 * ������
	 */
	public void setChnName(String chnName) {
		this.chnName=chnName;
	}
	/**
	 * ������
	 */
	public String getChnName() {
		return this.chnName;
	}

}
