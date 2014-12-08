package com.novelbio.database.domain.kegg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.database.service.servkegg.ServKRelation;


@Document(collection="kgentry")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "name_path_id_idx", def = "{'name': 1, 'pathName': -1, 'id' : 1}")
 })
public class KGentry {
	/** mongodbId */
	@Id
	String id;
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	private int entryId;
	
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
	@Indexed
	private String name;
	
	/**  该entry所在的pathwayID */
	@Indexed
	private String pathName;
	
	/** component总数 */
	private int compNum;
	/** component的 ID，没啥用 */
	private int compID;
	
	/** component复合物的entryID，在relaction中就可能以这个entryID来表示关系 */
	private int parentID;
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
	
	@Indexed
	private int taxID;

	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	private String reactionName;
	
	/** mongodb的id */
	public String getId() {
		return id;
	}
	/** mongodb的id */
	public void setId(String identry) {
		this.id = identry;
	}
	
	/** component的总数 */
	public void setCompNum(int compNum) {
		this.compNum=compNum;
	}
	/** component的总数 */
	public int getCompNum() {
		return compNum;
	}
	

	/** component的entry ID，没啥用 */
	public void setCompID(int compID) {
		this.compID=compID;
	}
	/** component的entry ID */
	public int getCompID() {
		return compID;
	}

	/** component复合物的entryID，在relaction中就可能以这个entryID来表示关系 */
	public void setParentID(int parentID) {
		this.parentID=parentID;
	}
	/** component复合物的entryID，在relaction中就可能以这个entryID来表示关系 */
	public int getParentID() {
		return parentID;
	}
	
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	public int getEntryId() {
		return this.entryId;
	}
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	public void setEntryId(int id) {
		this.entryId=id;
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
	public ArrayList<KGentry> getCompEntity() {
		ArrayList<KGentry> lsKGentries = new ArrayList<KGentry>();
		/**
		 * 保存最后获得与之相关的entry信息
		 */
		//如果这个entry是有parentID的，也就是一个component
		if (parentID == 0) {
			return null;
		}
		////////如果有复合物，先查找entry中的复合物，将该复合物的其余复合物都找到//////////////////////////////////////////////////////////////////////////////////////////////////////////
		ServKEntry servKEntry = ServKEntry.getInstance();
		//这里都是与queryEntry是component的entry,
		List<KGentry> lsSubKGentries = servKEntry.findByPathNameAndParentId(pathName, parentID);
		
		for (KGentry kGentry : lsSubKGentries) {
			if (kGentry.getEntryId() == entryId) {
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
	public List<KGrelation> getRelatEntity() 	{
		ServKRelation servKRelation = ServKRelation.getInstance();
		KGrelation tmpQkGrelation=new KGrelation();
		tmpQkGrelation.setEntry1ID(entryId); tmpQkGrelation.setPathName(pathName);
		List<KGrelation> lsKGrelations1 = servKRelation.findByPathNameAndEntry1Id(pathName, entryId);
		
		tmpQkGrelation.setEntry2ID(entryId); tmpQkGrelation.setPathName(pathName);
		List<KGrelation> lsKGrelations2 = servKRelation.findByPathNameAndEntry2Id(pathName, entryId);
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
	 * 获得本entry所在pathway的名字
	 * @return
	 */
	public String getPathTitle() {
		ServKPathway servKPathway = ServKPathway.getInstance();
		KGpathway kGpathway = servKPathway.findByPathName(getPathName());
		if (kGpathway == null) {
			return null;
		}
		return kGpathway.getTitle();
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
//		public static ArrayList<KGentry> getLsEntity(KGentry kGentry) {
//			ServKEntry servKEntry = ServKEntry.getInstance();
//			ArrayList<KGentry> lskGentries = servKEntry.queryLsKGentries(kGentry);
//			if (lskGentries == null || lskGentries.size() < 1) {
//				return null;
//			}
//			return lskGentries;
//		}

		/**
		 * 给定kGentry，用里面的信息搜数据库并返回，如果没搜到的话就返回new arraylist<br>
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
		public static List<KGentry> getLsEntity(String kegID) {
			if (kegID == null) {
				return new ArrayList<KGentry>();
			}
			ServKEntry servKEntry = ServKEntry.getInstance();
			List<KGentry> lskGentries = servKEntry.findByName(kegID);
			if (lskGentries == null) {
				return new ArrayList<KGentry>();
			}
			return lskGentries;
		}
		
		/**
		 * 比较entryName, PathName, 该pathway中的entryID三项
		 */
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			KGentry otherObj = (KGentry)obj;
			if (entryId == otherObj.getEntryId() && pathName.equals(otherObj.getPathName()) &&  name.equals(otherObj.getEntryName())) {
				return true;
			}
			return false;
		}
		
		/**
		 * 重写hashcode
		 * 用id + name + pathName来做hashcode
		 */
		public int hashCode(){
			String hash = entryId + name + pathName;
			return hash.hashCode();
		}
		
	
}
