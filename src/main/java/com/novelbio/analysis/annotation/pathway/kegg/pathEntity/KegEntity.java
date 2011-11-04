package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.novelbio.analysis.annotation.pathway.network.AbsNetRelate;
import com.novelbio.analysis.annotation.pathway.network.KGpathScr2Trg;
import com.novelbio.analysis.annotation.pathway.network.KegNetRelate;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.mapper.kegg.MapKEntry;
import com.novelbio.database.mapper.kegg.MapKRealtion;

/**
 * KgEntry�������棬����˲����µķ���
 * @author zong0jie
 *
 */
public class KegEntity {
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
	/**
	 * ��kGentry�Ѿ���ȫ�������£���kGentry��������Ϣת��KegEntity
	 * @param kGentry
	 */
	public KegEntity(KGentry kGentry)
	{
		this.compID = kGentry.getCompID();
		this.compNum = kGentry.getCompNum();
		this.id = kGentry.getID();
		this.linkEntry = kGentry.getLinkEntry();
		this.name = kGentry.getEntryName();
		this.parentID = kGentry.getParentID();
		this.pathName = kGentry.getPathName();
		this.reactionName = kGentry.getReaction();
		this.taxID = kGentry.getTaxID();
		this.type = kGentry.getType();
	}

	/**
	 * ����kGentry�����������Ϣ�����ݿⲢ���أ����û�ѵ��Ļ��ͷ���null<br>
	 		where<br>
			if test="name !=null"<br>
				 name=#{name}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
			if test="reactionName !=null"<br>
				and reactionName=#{reactionName}<br>
			/if<br>
			if test="taxID !=null or taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="parentID !=null or parentID !=0"<br>
				and parentID=#{parentID}<br>
			/if<br>
		/where<br>
	 * @param KGentry
	 * @return
	 */
	public static ArrayList<KegEntity> getLsEntity(KGentry kGentry) {
		ArrayList<KegEntity> lsKegEntities = new ArrayList<KegEntity>();
		ArrayList<KGentry> lskGentries = MapKEntry.queryLsKGentries(kGentry);
		if (lskGentries == null || lskGentries.size() < 1) {
			return null;
		}
		for (KGentry kGentry2 : lskGentries) {
			KegEntity kegEntity = new KegEntity(kGentry2);
			lsKegEntities.add(kegEntity);
		}
		return lsKegEntities;
	}
	
	
	
	
	/**
	 * ����뱾Entity��ɸ����������KegEntity��ע��List�в�������KegEntity
	 * @return
	 */
	public ArrayList<KegEntity> getCompEntity()
	{
		ArrayList<KegEntity> lsKegEntities = new ArrayList<KegEntity>();
		/**
		 * �����������֮��ص�entry��Ϣ
		 */
		//������entry����parentID�ģ�Ҳ����һ��component
		if (parentID == 0) {
			return null;
		}
		////////����и�����Ȳ���entry�еĸ�������ø���������ิ���ﶼ�ҵ�//////////////////////////////////////////////////////////////////////////////////////////////////////////
		KGentry tmpqkGentry = new KGentry();//��ѯ�õ�KGentry
		tmpqkGentry.setPathName(pathName);
		tmpqkGentry.setParentID(parentID);
		//���ﶼ����queryEntry��component��entry,
		ArrayList<KegEntity> lsSubKGentries=KegEntity.getLsEntity(tmpqkGentry);
		for (KegEntity kegEntity2 : lsSubKGentries) {
			if (kegEntity2.getID() == id) {
				continue;
			}
			lsKegEntities.add(kegEntity2);
		}
		return lsKegEntities;
	}
	
	
	/**
	 * ����б�Entity�����relation��ϵ��������ܻ����ظ������
	 * @return
	 */
	public ArrayList<KGrelation> getRelatEntity()
	{
		KGrelation tmpQkGrelation=new KGrelation();
		tmpQkGrelation.setEntry1ID(id); tmpQkGrelation.setPathName(pathName);
		ArrayList<KGrelation> lsKGrelations1 = MapKRealtion.queryLsKGrelations(tmpQkGrelation);
		
		tmpQkGrelation.setEntry2ID(id); tmpQkGrelation.setPathName(pathName);
		ArrayList<KGrelation> lsKGrelations2 = MapKRealtion.queryLsKGrelations(tmpQkGrelation);
		/////////�趨�����ĸ�
		for (KGrelation kGrelation : lsKGrelations1) {
			kGrelation.setFlag(KGrelation.FLAG_ENTRYID1);
		}
		for (KGrelation kGrelation : lsKGrelations2) {
			kGrelation.setFlag(KGrelation.FLAG_ENTRYID2);
		}
		
		lsKGrelations1.addAll(lsKGrelations2);
		return lsKGrelations1;
	}
	
	
		/**
		 * ����entryID��pathName�����Ҿ����KegEntity<br>
		 * ��Ϊһ��entryID���ܻ��Ӧ���keggID����ôҲ�ͻ᷵�ض��KGentry����<br>
		 * ���Ȳ���entry��ID��û�ҵ��Ļ�������parentID<br>
		 * �����parentID������Ҫ�����--Ҳ����component ֮������������Ϊ�����루��������ҵ�ʱ�� �Ѿ�������֮�����ϵ��
		 * ��Щ��ϵ�ڽ������У���Ŀmapping��ȥ��ʱ��ᱻ���㵽
		 * @param entryID
		 * @param pathName
		 * @return
		 */
		private static ArrayList<KegEntity> getRelateEntry(int entryID, String pathName) {
			KGentry qKGentry = new KGentry();
			qKGentry.setID(entryID); qKGentry.setPathName(pathName);
			ArrayList<KegEntity> lskegEntities = KegEntity.getLsEntity(qKGentry);
			if (lskegEntities == null ) 
			{
				qKGentry = new KGentry();
				qKGentry.setParentID(entryID); qKGentry.setPathName(pathName);
				 lskegEntities = KegEntity.getLsEntity(qKGentry);
			}
			return lskegEntities;
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
