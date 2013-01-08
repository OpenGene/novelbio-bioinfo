package com.novelbio.analysis.annotation.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.database.domain.AbsPathway;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgeneid.GeneID;

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
	public static int ENTITY_GENE = 123; 
	public static int ENTITY_COMPOUND = 456;
	public static int ENTITY_DRUG = 789;
	/**
	 * 该节点的类型，用来表示这个唯一点。
	 * 有时候会有这种情况，譬如Kegg里面一个节点有三个基因。
	 * 那么这三个点在图片中应该是同一个点。这时候就可以用ENTITY_COMPLEX来表示这个大节点，其他小点和它连起来。
	 * 
	 */
	public static int ENTITY_COMPLEX = 1024;
	/**
	 * 以下是本entity处在的DBinfo
	 */
	public static String DBINFO_KEGG = "kegg"; 
	public static String DBINFO_REACTOME = "Reactome";

	/**
	 * 该节点包括若干个CopedID
	 */
	HashSet<GeneID> hashCopedIDs = new HashSet<GeneID>();
	
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
	
	int taxID = 0;
	
	/**
	 * 该节点包括若干个CopedID
	 */
	public HashSet<GeneID> getHashCopedIDs() {
		return hashCopedIDs;
	}
	/**
	 * 该节点包括若干个CopedID
	 */
	public void addCopedID(GeneID copedID)
	{
		hashCopedIDs.add(copedID);
	}
 
	/**
	 * 该节点的物种
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 该节点的物种
	 */
	public void setTaxID(int taxID)
	{
		this.taxID = taxID;
	}
	/**
	 * 数值应该是AbsNetEntity中ENTITY中的一员，可以是化合物，基因，药物等，按照需要可以添加
	 */
	public String getFlag() {
		return flag;
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
		
		return true;
	}
	
	public int hashCode()
	{
		int hashCode = hashCopedIDs.hashCode() + lsDataBase.hashCode()*1000000;
		return hashCode;
	}
	
	/**
	 * 用来保存
	 */
	static ArrayList<AbsNetRelate> hashAbsNetRelate = new ArrayList<AbsNetRelate>();
	
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
	 * 给定某个节点，在对应的pathway中查找它的相关节点
	 * @return
	 */
	public abstract ArrayList<AbsNetRelate> getRelate(boolean blast, int StaxID, double evalue);
	
}
