package com.novelbio.database.domain.kegg;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.mapper.kegg.MapKEntry;
import com.novelbio.database.mapper.kegg.MapKRealtion;
import com.novelbio.database.model.modcopeid.CopedID;


public class KGentry {
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	private int id;
	
	/**
	 * the KEGGID of this entry，输入时先要将name以空格分割成一个一个单独的名字
	 * example： name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
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
	 * 该entry所在的pathway
	 */
	private String pathName;
	
	/**
	 * component总数
	 */
	private int compNum;
	
	/**
	 * component的总数
	 */
	public void setCompNum(int compNum)
	{
		this.compNum=compNum;
	}
	/**
	 * component的总数
	 */
	public int getCompNum()
	{
		return compNum;
	}
	
	/**
	 * component的 ID，没啥用
	 */
	private int compID;
	/**
	 * component的entry ID，没啥用
	 */
	public void setCompID(int compID)
	{
		this.compID=compID;
	}
	/**
	 * component的entry ID
	 */
	public int getCompID()
	{
		return compID;
	}
	
	/**
	 * component复合物的entryID，在relaction中就可能以这个entryID来表示关系
	 */
	private int parentID;
	/**
	 * component复合物的entryID，在relaction中就可能以这个entryID来表示关系
	 */
	public void setParentID(int parentID)
	{
		this.parentID=parentID;
	}
	/**
	 * component复合物的entryID，在relaction中就可能以这个entryID来表示关系
	 */
	public int getParentID()
	{
		return parentID;
	}
	
	/**
	 * 当type为map且不为本pathway时，将本pathway和该map组成source--target并且放入KGRelation类中
	 * 当type为group时，将component中涉及到的所有entry两两遍历组成source--target并且放入KGReaction类中
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
	 *  the identification number of this entry，从1开始记数
	 */
	public int getID()
	{
		return this.id;
	}
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	public void setID(int id)
	{
		this.id=id;
	}
	
	/**
	 * the KEGGID of this entry，只有单独一个 
	 * example： name="sma:SAV_2461"<br>
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
	 * the KEGGID of this entry，输入时先要将name以空格分割成一个一个单独的名字
	 * example： name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
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
	 * 该entry所在的pathway
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
	/**
	 * 该entry所在的pathway
	 */
	public String getPathName() {
		return this.pathName;
	}
	
	/**
	 * 当type为map且不为本pathway时，将本pathway和该map组成source--target并且放入KGRelation类中
	 * 当type为group时，将component中涉及到的所有entry两两遍历组成source--target并且放入KGReaction类中
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
	 * 当type为map且不为本pathway时，将本pathway和该map组成source--target并且放入KGRelation类中
	 * 当type为group时，将component中涉及到的所有entry两两遍历组成source--target并且放入KGReaction类中
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
	////////////////////////////////
	/**
	 * 以下是功能性的方法
	 */
	////////////////////////////////
	/**
	 * 获得与本Entity组成复合体的其他KegEntity，注意List中不包含本KegEntity
	 * @return
	 */
	public ArrayList<KGentry> getCompEntity()
	{
		ArrayList<KGentry> lsKGentries = new ArrayList<KGentry>();
		/**
		 * 保存最后获得与之相关的entry信息
		 */
		//如果这个entry是有parentID的，也就是一个component
		if (parentID == 0) {
			return null;
		}
		////////如果有复合物，先查找entry中的复合物，将该复合物的其余复合物都找到//////////////////////////////////////////////////////////////////////////////////////////////////////////
		KGentry tmpqkGentry = new KGentry();//查询用的KGentry
		tmpqkGentry.setPathName(pathName);
		tmpqkGentry.setParentID(parentID);
		//这里都是与queryEntry是component的entry,
		ArrayList<KGentry> lsSubKGentries = getLsEntity(tmpqkGentry);
		for (KGentry kGentry : lsSubKGentries) {
			if (kGentry.getID() == id) {
				continue;
			}
			lsKGentries.add(kGentry);
		}
		return lsKGentries;
	}
	
	
	/**
	 * 获得有本Entity参与的relation关系，里面可能会有重复项存在
	 * @return
	 */
	public ArrayList<KGrelation> getRelatEntity()
	{
		KGrelation tmpQkGrelation=new KGrelation();
		tmpQkGrelation.setEntry1ID(id); tmpQkGrelation.setPathName(pathName);
		ArrayList<KGrelation> lsKGrelations1 = MapKRealtion.queryLsKGrelations(tmpQkGrelation);
		
		tmpQkGrelation.setEntry2ID(id); tmpQkGrelation.setPathName(pathName);
		ArrayList<KGrelation> lsKGrelations2 = MapKRealtion.queryLsKGrelations(tmpQkGrelation);
		/////////设定来自哪个
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
		 * 给定entryID和pathName，查找该pathway中的具体KGentry，没有就返回null<br>
		 * 因为一个entryID可能会对应多个keggID，那么也就会返回多个KGentry对象<br>
		 * 首先查找entry的ID，没找到的话，查找parentID<br>
		 * 如果是parentID，不需要将结果--也就是component 之间连起来，因为在输入（所有项）查找的时候 已经考虑了之间的联系，
		 * 这些关系在将（所有）项目mapping回去的时候会被计算到
		 * @param entryID
		 * @param pathName
		 * @return
		 */
		public static ArrayList<KGentry> getPathEntry(int entryID, String pathName) {
			KGentry qKGentry = new KGentry();
			qKGentry.setID(entryID); qKGentry.setPathName(pathName);
			ArrayList<KGentry> lskegEntities = getLsEntity(qKGentry);
			if (lskegEntities == null ) 
			{
				qKGentry = new KGentry();
				qKGentry.setParentID(entryID); qKGentry.setPathName(pathName);
				 lskegEntities = getLsEntity(qKGentry);
			}
			if (lskegEntities == null || lskegEntities.size() < 1) {
				return null;
			}
			return lskegEntities;
		}
	
	

		/**
		 * 给定kGentry，用里面的信息搜数据库并返回，如果没搜到的话就返回null<br>
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
		public static ArrayList<KGentry> getLsEntity(KGentry kGentry) {
			ArrayList<KGentry> lskGentries = MapKEntry.queryLsKGentries(kGentry);
			if (lskGentries == null || lskGentries.size() < 1) {
				return null;
			}
			return lskGentries;
		}

		/**
		 * 给定kGentry，用里面的信息搜数据库并返回，如果没搜到的话就返回null<br>
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
		public static ArrayList<KGentry> getLsEntity(String kegID) {
			KGentry kGentry = new KGentry();
			kGentry.setEntryName(kegID);
			ArrayList<KGentry> lskGentries = MapKEntry.queryLsKGentries(kGentry);
			if (lskGentries == null || lskGentries.size() < 1) {
				return null;
			}
			return lskGentries;
		}
		
		/**
		 * 比较entryName, PathName, 该pathway中的entryID三项
		 */
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			KGentry otherObj = (KGentry)obj;
			if (id == otherObj.getID() && pathName.equals(otherObj.getPathName()) &&  name.equals(otherObj.getEntryName())) {
				return true;
			}
			return false;
		}
		
		/**
		 * 重写hashcode
		 * 用id + name + pathName来做hashcode
		 */
		public int hashCode(){
			String hash = id + name + pathName;
			return hash.hashCode();
		}
		
	
}
