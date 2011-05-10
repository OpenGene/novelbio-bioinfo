package entity.kegg;
 
public class KGreaction {
	/**
	 * the ID of this reaction,��Entry��ID��ͬһ��
	 */
	private int id;
	 
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	private String name;
	
	/**
	 * �����Ӧ��ID�����ض���pathway�µ�
	 */
	private String pathName;
	
	
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	private String type;
 
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	private String alt;
	
	/**
	 * the ID of this reaction,��Entry��ID��ͬһ��
	 * �����Ӧ��ID�����ض���pathway�µ�
	 */
	public int getID() {
		return this.id;
	}
	/**
	 * the ID of this reaction,��Entry��ID��ͬһ��
	 * �����Ӧ��ID�����ض���pathway�µ�
	 */
	public void setID(int id) {
		 this.id=id;
	}
	
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * already trim()
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	public void setName(String name) {
		 this.name=name.trim();
	}
	
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * already trim()
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	public void setType(String type) {
		this.type=type.trim();
	}
 
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	public String getAlt() {
		return this.alt;
	}
	/**
	 * already trim()
	 * The alt element specifies the alternative name of its parent element.
	 */
	public void setAlt(String alt) {
		this.alt=alt.trim();
	}
	/**
	 * �����Ӧ�����ض���pathway�µ�
	 */
	public String getPathName() {
		return this.pathName;
	}
	/**
	 * already trim()
	 * �����Ӧ�����ض���pathway�µ�
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
}
