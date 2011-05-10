package com.novelbio.database.entity.kegg;


public class KGentry {
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry����1��ʼ����
	 */
	private int id;
	
	/**
	 * the KEGGID of this entry������ʱ��Ҫ��name�Կո�ָ��һ��һ������������
	 * example�� name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
	 * detail:<br>
	 * <b>path:(accession)</b>  	pathway map
	 * ex) name="path:map00040"
	 *<b> ko:(accession)</b> 	KO (ortholog group)
	 * ex) name="ko:E3.1.4.11"
	 *<b> ec:(accession)</b> 	enzyme
	 * ex) name="ec:1.1.3.5"
	 * <b>rn:(accession)</b> 	reaction
	 * ex) name="rn:R00120"
	 * <b>cpd:(accession)</b> 	chemical compound
	 * ex) name="cpd:C01243"
	 * <b>gl:(accession)</b> 	glycan
	 * ex) name="gl:G00166"
	 * <b>[org prefix]:(accession)</b> 	gene product of a given organism
	 * ex) name="eco:b1207"
	 * <b>group:(accession)</b> 	complex of KOs
	 * If accession is undefined, "undefined" is specified.
	 * ex) name="group:ORC"
	 */
	private String name;
	
	/**
	 * ��entry���ڵ�pathway
	 */
	private String pathName;
	
	/**
	 * component����
	 */
	private int compNum;
	
	/**
	 * component������
	 */
	public void setCompNum(int compNum)
	{
		this.compNum=compNum;
	}
	/**
	 * component������
	 */
	public int getCompNum()
	{
		return compNum;
	}
	
	/**
	 * component�� ID��ûɶ��
	 */
	private int compID;
	/**
	 * component��entry ID��ûɶ��
	 */
	public void setCompID(int compID)
	{
		this.compID=compID;
	}
	/**
	 * component��entry ID
	 */
	public int getCompID()
	{
		return compID;
	}
	
	/**
	 * component�������entryID����relaction�оͿ��������entryID����ʾ��ϵ
	 */
	private int parentID;
	/**
	 * component�������entryID����relaction�оͿ��������entryID����ʾ��ϵ
	 */
	public void setParentID(int parentID)
	{
		this.parentID=parentID;
	}
	/**
	 * component�������entryID����relaction�оͿ��������entryID����ʾ��ϵ
	 */
	public int getParentID()
	{
		return parentID;
	}
	
	/**
	 * ��typeΪmap�Ҳ�Ϊ��pathwayʱ������pathway�͸�map���source--target���ҷ���KGRelation����
	 * ��typeΪgroupʱ����component���漰��������entry�����������source--target���ҷ���KGReaction����
	 * the type of this entry. detail:<br>
	 * <b>ortholog</b> 	the node is a KO (ortholog group)<br>
	 * <b>enzyme</b> 	the node is an enzyme<br>
	 * <b>reaction</b> 	the node is a reaction<br>
	 * <b>gene</b> 	the node is a gene product (mostly a protein)<br>
	 * <b>group</b> 	the node is a complex of gene products (mostly a protein complex)<br>
     *<b>compound</b> 	the node is a chemical compound (including a glycan)<br>
     *<b>map</b> 	the node is a linked pathway map<br>
	 */
	private String type;
	
	
	/**
	 * the resource location of the information about this entry  example:<br>
	 * <b>URL</b> 	ex)link="http://www.genome.jp/dbget-bin/www_bget?eco+b1207"
	 */
	private String linkEntry;
	
	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	private String reactionName;
	
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry����1��ʼ����
	 */
	public int getID()
	{
		return this.id;
	}
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry����1��ʼ����
	 */
	public void setID(int id)
	{
		this.id=id;
	}
	
	/**
	 * the KEGGID of this entry��ֻ�е���һ�� 
	 * example�� name="sma:SAV_2461"<br>
	 * detail:<br>
	 * <b>path:(accession)</b>  	pathway map
	 * ex) name="path:map00040"
	 *<b> ko:(accession)</b> 	KO (ortholog group)
	 * ex) name="ko:E3.1.4.11"
	 *<b> ec:(accession)</b> 	enzyme
	 * ex) name="ec:1.1.3.5"
	 * <b>rn:(accession)</b> 	reaction
	 * ex) name="rn:R00120"
	 * <b>cpd:(accession)</b> 	chemical compound
	 * ex) name="cpd:C01243"
	 * <b>gl:(accession)</b> 	glycan
	 * ex) name="gl:G00166"
	 * <b>[org prefix]:(accession)</b> 	gene product of a given organism
	 * ex) name="eco:b1207"
	 * <b>group:(accession)</b> 	complex of KOs
	 * If accession is undefined, "undefined" is specified.
	 * ex) name="group:ORC"
	 */
	public String getEntryName() {
		return this.name;
	}
	/**
	 * the KEGGID of this entry������ʱ��Ҫ��name�Կո�ָ��һ��һ������������
	 * example�� name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
	 * detail:<br>
	 * <b>path:(accession)</b>  	pathway map
	 * ex) name="path:map00040"
	 *<b> ko:(accession)</b> 	KO (ortholog group)
	 * ex) name="ko:E3.1.4.11"
	 *<b> ec:(accession)</b> 	enzyme
	 * ex) name="ec:1.1.3.5"
	 * <b>rn:(accession)</b> 	reaction
	 * ex) name="rn:R00120"
	 * <b>cpd:(accession)</b> 	chemical compound
	 * ex) name="cpd:C01243"
	 * <b>gl:(accession)</b> 	glycan
	 * ex) name="gl:G00166"
	 * <b>[org prefix]:(accession)</b> 	gene product of a given organism
	 * ex) name="eco:b1207"
	 * <b>group:(accession)</b> 	complex of KOs
	 * If accession is undefined, "undefined" is specified.
	 * ex) name="group:ORC"
	 */
	public void setEntryName(String name) {
		this.name=name.trim();
	}
	
	
	/**
	 * already trim()
	 * ��entry���ڵ�pathway
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
	/**
	 * ��entry���ڵ�pathway
	 */
	public String getPathName() {
		return this.pathName;
	}
	
	/**
	 * ��typeΪmap�Ҳ�Ϊ��pathwayʱ������pathway�͸�map���source--target���ҷ���KGRelation����
	 * ��typeΪgroupʱ����component���漰��������entry�����������source--target���ҷ���KGReaction����
	 * the type of this entry. detail:<br>
	 * <b>ortholog</b> 	the node is a KO (ortholog group)<br>
	 * <b>enzyme</b> 	the node is an enzyme<br>
	 * <b>reaction</b> 	the node is a reaction<br>
	 * <b>gene</b> 	the node is a gene product (mostly a protein)<br>
	 * <b>group</b> 	the node is a complex of gene products (mostly a protein complex)<br>
     *<b>compound</b> 	the node is a chemical compound (including a glycan)<br>
     *<b>map</b> 	the node is a linked pathway map<br>
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * already trim()
	 * ��typeΪmap�Ҳ�Ϊ��pathwayʱ������pathway�͸�map���source--target���ҷ���KGRelation����
	 * ��typeΪgroupʱ����component���漰��������entry�����������source--target���ҷ���KGReaction����
	 * the type of this entry. detail:<br>
	 * <b>ortholog</b> 	the node is a KO (ortholog group)<br>
	 * <b>enzyme</b> 	the node is an enzyme<br>
	 * <b>reaction</b> 	the node is a reaction<br>
	 * <b>gene</b> 	the node is a gene product (mostly a protein)<br>
	 * <b>group</b> 	the node is a complex of gene products (mostly a protein complex)<br>
     *<b>compound</b> 	the node is a chemical compound (including a glycan)<br>
     *<b>map</b> 	the node is a linked pathway map<br>
	 */
	public void setType(String type) {
		this.type=type.trim();
	}
	
	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	public String getReaction() {
		return this.reactionName;
	}
	
	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	public void setReaction(String reactionName) {
		this.reactionName=reactionName.trim();
	}
	
	/**
	 * the resource location of the information about this entry  example:<br>
	 * <b>URL</b> 	ex)link="http://www.genome.jp/dbget-bin/www_bget?eco+b1207"
	 */
	public String getLinkEntry() {
		return this.linkEntry;
	}
	/**
	 * already trim()
	 * the resource location of the information about this entry  example:<br>
	 * <b>URL</b> 	ex)link="http://www.genome.jp/dbget-bin/www_bget?eco+b1207"
	 */
	public void setLinkEntry(String linkEntry) {
		this.linkEntry=linkEntry.trim();
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	
	
	
}
