package com.novelbio.analysis.annotation.pathway.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.database.entity.AbsPathway;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;

/**
 * 保存网络图中单个节点信息的类，每个节点用entityID进行区分，所以必须在初始化的时候手工<br>
 * 设定每个节点的entityID，在比较节点的时候也是通过entityID来比较两个节点是否一致<br>
 * <br>
 * 应该是个超类<br>
 * 其中包括该节点所包含的基因，该节点所在的数据库，该节点所在的pathway<br>
 * 
 * @author zong0jie
 *
 */
public abstract class AbsNetEntity {
	/**
	 * 以下是本entity大概属于什么类别
	 */
	public static String ENTITY_GENE = "gene"; 
	public static String ENTITY_COMPOUND = "compound";
	public static String ENTITY_DRUG = "drug";
	
	/**
	 * 以下是本entity处在的DBinfo
	 */
	public static String DBINFO_KEGG = "kegg"; 
	public static String DBINFO_REACTOME = "Reactome";

	/**
	 * 该节点包括若干个CopedID
	 */
	HashSet<CopedID> hashCopedIDs = new HashSet<CopedID>();
	
	/**
	 * 该节点所在的pathway信息
	 */
	ArrayList<AbsPathway> lsPathInfo = new ArrayList<AbsPathway>(); 
	
	/**
	 * 该节点的ID，用来表示这个唯一点。
	 * 有时候会有这种情况，譬如Kegg里面一个节点有三个基因，而这三个基因在reactome中对应了三个不同的节点。
	 * 那么这四个点在图片中应该是同一个点。这时候就可以用entityID来表示这四个节点在最后的网络图中其实是同一个节点。
	 * 
	 */
	private String entityID = "";
	
	/**
	 * 数值应该是AbsNetEntity中ENTITY中的一员，可以是化合物，基因，药物等，按照需要可以添加
	 * 只有当flag=ENTITY_GENE时，才会有lsNcbiids或lsUniProtIDs
	 */
	private String flag = "";
	
	private ArrayList<String> lsDataBase = new ArrayList<String>();
	
	private String taxID = "";
	
	
	/**
	 * 该节点包括若干个CopedID
	 */
	public HashSet<CopedID> getHashCopedIDs() {
		return hashCopedIDs;
	}
	/**
	 * 该节点包括若干个CopedID
	 */
	public void addCopedID(CopedID copedID)
	{
		hashCopedIDs.add(copedID);
	}
	
	/**
	 * 该节点所在的pathway信息
	 */
	public ArrayList<AbsPathway> getLsPathways() {
		return lsPathInfo;
	}
	/**
	 * 该节点所在的pathway信息
	 */
	public void addLsPathways(AbsPathway pathway)
	{
		lsPathInfo.add(pathway);
	}
	
	/**
	 * 该节点的ID，用来表示这个唯一点。
	 * 有时候会有这种情况，譬如Kegg里面一个节点有三个基因，而这三个基因在reactome中对应了三个不同的节点。
	 * 那么这四个点在图片中应该是同一个点。这时候就可以用entityID来表示这四个节点在最后的网络图中其实是同一个节点。
	 */
	public String getEntityID() {
		return entityID;
	}
	/**
	 * 该节点的ID，用来表示这个唯一点。
	 * 有时候会有这种情况，譬如Kegg里面一个节点有三个基因，而这三个基因在reactome中对应了三个不同的节点。
	 * 那么这四个点在图片中应该是同一个点。这时候就可以用entityID来表示这四个节点在最后的网络图中其实是同一个节点。
	 */
	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}
	/**
	 * 该节点的物种
	 */
	public String getTaxID() {
		return taxID;
	}
	/**
	 * 该节点的物种
	 */
	public void setTaxID(String taxID)
	{
		this.taxID = taxID;
	}
	/**
	 * 该节点处于哪个DataBase中，有DBINFO_KEGG 和DBINFO_REACTOME
	 * @return
	 */
	public ArrayList<String> getDataBaseInfo() {
		return lsDataBase;
	}
	/**
	 * 该节点处于哪个DataBase中，有DBINFO_KEGG 和DBINFO_REACTOME
	 * @return
	 */
	public void setDataBaseInfo(String DBinfo)
	{
		this.lsDataBase.add(DBinfo);
	}
	/**
	 * 数值应该是AbsNetEntity中ENTITY中的一员，可以是化合物，基因，药物等，按照需要可以添加
	 * 只有当flag=ENTITY_GENE时，才会有lsNcbiids或lsUniProtIDs
	 */
	public String getFlag() {
		return flag;
	}
	/**
	 * 数值应该是AbsNetEntity中ENTITY中的一员，可以是化合物，基因，药物等，按照需要可以添加
	 * 只有当flag=ENTITY_GENE时，才会有lsNcbiids或lsUniProtIDs
	 */
	public void setFlag(String flag)
	{
		this.flag = flag;
	}
	/**
	 * 	仅比较两个AbsNetEntity的entityID是否相同。但是当entityID.trim()为""时，都忽略
	 */
	public boolean equalSimple(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		AbsNetEntity otherObj = (AbsNetEntity)obj;
		if (entityID.trim().equals("") || otherObj.getEntityID().trim().equals("")) {
			return false;
		}
		return entityID.equals(otherObj.entityID) ;
	}
	/**
	 * 	仔细比较两个AbsNetEntity是否相同
	 * 不过也只是比较entityID，flag，lsNcbiids和lsUniProtIDs是否一致
	 */
	public boolean equals(Object obj) {
		if (!equalSimple(obj))
			return false;
		AbsNetEntity otherObj = (AbsNetEntity)obj;
		if (!flag.equals(otherObj.getFlag())) 
			return false;
		
		//NCBIID已经重写过equals方法了，只比较geneID是否一致
		if (!hashCopedIDs.equals(otherObj.getHashCopedIDs()))
			return false;
		
		//比较所处在的数据库是否一致
		if (!lsDataBase.equals(otherObj.getDataBaseInfo()))
			return false;
		
		return true;
	}
	
	public int hashCode()
	{
		int hashCode = hashCopedIDs.hashCode() + lsDataBase.hashCode();
		return hashCode;
	}
	
	/**
	 * 用来保存
	 */
	static HashMap<String, ArrayList<AbsNetEntity>> hashAbsNetEntity = new HashMap<String, ArrayList<AbsNetEntity>>();
	
	/**
	 * 给定一个entityID，返回该entity所有的细分节点，<br>
	 * 这种情况通常为如下情况：<br>
	 * 譬如Kegg里面一个节点有三个基因，而这三个基因在reactome中对应了三个不同的节点。<br>
	 * 那么这四个点在图片中应该是同一个点。<br>
	 * @param entityID
	 * @return
	 */
	public static ArrayList<AbsNetEntity> getLsAbsNetEntity(String entityID) {
		return hashAbsNetEntity.get(entityID.trim());
	}
	
	/**
	 * 给定一个节点信息，装入指定的哈希表中 
	 * 如果hash表中没有改节点，直接生成一个新key
	 * 如果hash表中有该节点的list，比较该节点信息与该list中的其他元素，如果都不相同，就加入该节点
	 * @param absNetEntity
	 */
	public static void addAbsNetEntity(AbsNetEntity absNetEntity) {
		if (hashAbsNetEntity.get(absNetEntity.getEntityID()) == null) {
			ArrayList<AbsNetEntity> lsAbsNetEntities = new ArrayList<AbsNetEntity>();
			lsAbsNetEntities.add(absNetEntity);
			hashAbsNetEntity.put(absNetEntity.getEntityID(), lsAbsNetEntities);
		}
		else 
		{
			ArrayList<AbsNetEntity> lsAbsNetEntities = hashAbsNetEntity.get(absNetEntity.getEntityID());
			if (!lsAbsNetEntities.contains(absNetEntity)) {
				lsAbsNetEntities.add(absNetEntity);
			}
		}
	}
	
	/**
	 * 清空hash表
	 */
	public static void cleanHash() {
		hashAbsNetEntity = new HashMap<String, ArrayList<AbsNetEntity>>();
	}
	
	/**
	 * 给定某个节点，在对应的pathway中查找它的相关节点
	 * @return
	 */
	public abstract ArrayList<AbsNetRelate> getRelate(boolean blast, int StaxID, double evalue);
	
}
