package com.novelbio.analysis.annotation.pathway.network;

import java.util.ArrayList;

import com.novelbio.database.entity.AbsPathway;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;

/**
 * 保存网络图中单个节点信息的类<br>
 * 应该是个超类<br>
 * 其中包括该节点所包含的基因，该节点所在的数据库，该节点所在的pathway<br>
 * 
 * @author zong0jie
 *
 */
public class AbsNetEntity {
	/**
	 * 以下是本entity大概属于什么类别
	 */
	public static String ENTITY_GENE = "gene"; 
	public static String ENTITY_COMPOUND = "compound";
	public static String ENTITY_DRUG = "drug";
	
	
	
	
	
	
	/**
	 * 该节点包括若干个NCBIID
	 */
	ArrayList<NCBIID> lsNcbiids;
	
	/**
	 * 该节点包括若干个UniProtID
	 */
	ArrayList<UniProtID> lsUniProtIDs;
	
	/**
	 * 该节点所在的pathway信息
	 */
	ArrayList<AbsPathway> lsPathInfo; 
	
	/**
	 * 该节点的ID
	 */
	String entityID;
	/**
	 * 数值应该是AbsNetEntity中ENTITY中的一员，可以是化合物，基因，药物等，按照需要可以添加
	 * 只有当flag=ENTITY_GENE时，才会有lsNcbiids或lsUniProtIDs
	 */
	String flag = "";
	
	
	
}
