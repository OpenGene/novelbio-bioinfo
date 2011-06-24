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
 * ��������ͼ�е����ڵ���Ϣ���࣬ÿ���ڵ���entityID�������֣����Ա����ڳ�ʼ����ʱ���ֹ�<br>
 * �趨ÿ���ڵ��entityID���ڱȽϽڵ��ʱ��Ҳ��ͨ��entityID���Ƚ������ڵ��Ƿ�һ��<br>
 * <br>
 * Ӧ���Ǹ�����<br>
 * ���а����ýڵ��������Ļ��򣬸ýڵ����ڵ����ݿ⣬�ýڵ����ڵ�pathway<br>
 * 
 * @author zong0jie
 *
 */
public abstract class AbsNetEntity {
	/**
	 * �����Ǳ�entity�������ʲô���
	 */
	public static String ENTITY_GENE = "gene"; 
	public static String ENTITY_COMPOUND = "compound";
	public static String ENTITY_DRUG = "drug";
	
	/**
	 * �����Ǳ�entity���ڵ�DBinfo
	 */
	public static String DBINFO_KEGG = "kegg"; 
	public static String DBINFO_REACTOME = "Reactome";

	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	HashSet<CopedID> hashCopedIDs = new HashSet<CopedID>();
	
	/**
	 * �ýڵ����ڵ�pathway��Ϣ
	 */
	ArrayList<AbsPathway> lsPathInfo = new ArrayList<AbsPathway>(); 
	
	/**
	 * �ýڵ��ID��������ʾ���Ψһ�㡣
	 * ��ʱ��������������Ʃ��Kegg����һ���ڵ����������򣬶�������������reactome�ж�Ӧ��������ͬ�Ľڵ㡣
	 * ��ô���ĸ�����ͼƬ��Ӧ����ͬһ���㡣��ʱ��Ϳ�����entityID����ʾ���ĸ��ڵ�����������ͼ����ʵ��ͬһ���ڵ㡣
	 * 
	 */
	private String entityID = "";
	
	/**
	 * ��ֵӦ����AbsNetEntity��ENTITY�е�һԱ�������ǻ��������ҩ��ȣ�������Ҫ�������
	 * ֻ�е�flag=ENTITY_GENEʱ���Ż���lsNcbiids��lsUniProtIDs
	 */
	private String flag = "";
	
	private ArrayList<String> lsDataBase = new ArrayList<String>();
	
	private String taxID = "";
	
	
	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	public HashSet<CopedID> getHashCopedIDs() {
		return hashCopedIDs;
	}
	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	public void addCopedID(CopedID copedID)
	{
		hashCopedIDs.add(copedID);
	}
	
	/**
	 * �ýڵ����ڵ�pathway��Ϣ
	 */
	public ArrayList<AbsPathway> getLsPathways() {
		return lsPathInfo;
	}
	/**
	 * �ýڵ����ڵ�pathway��Ϣ
	 */
	public void addLsPathways(AbsPathway pathway)
	{
		lsPathInfo.add(pathway);
	}
	
	/**
	 * �ýڵ��ID��������ʾ���Ψһ�㡣
	 * ��ʱ��������������Ʃ��Kegg����һ���ڵ����������򣬶�������������reactome�ж�Ӧ��������ͬ�Ľڵ㡣
	 * ��ô���ĸ�����ͼƬ��Ӧ����ͬһ���㡣��ʱ��Ϳ�����entityID����ʾ���ĸ��ڵ�����������ͼ����ʵ��ͬһ���ڵ㡣
	 */
	public String getEntityID() {
		return entityID;
	}
	/**
	 * �ýڵ��ID��������ʾ���Ψһ�㡣
	 * ��ʱ��������������Ʃ��Kegg����һ���ڵ����������򣬶�������������reactome�ж�Ӧ��������ͬ�Ľڵ㡣
	 * ��ô���ĸ�����ͼƬ��Ӧ����ͬһ���㡣��ʱ��Ϳ�����entityID����ʾ���ĸ��ڵ�����������ͼ����ʵ��ͬһ���ڵ㡣
	 */
	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}
	/**
	 * �ýڵ������
	 */
	public String getTaxID() {
		return taxID;
	}
	/**
	 * �ýڵ������
	 */
	public void setTaxID(String taxID)
	{
		this.taxID = taxID;
	}
	/**
	 * �ýڵ㴦���ĸ�DataBase�У���DBINFO_KEGG ��DBINFO_REACTOME
	 * @return
	 */
	public ArrayList<String> getDataBaseInfo() {
		return lsDataBase;
	}
	/**
	 * �ýڵ㴦���ĸ�DataBase�У���DBINFO_KEGG ��DBINFO_REACTOME
	 * @return
	 */
	public void setDataBaseInfo(String DBinfo)
	{
		this.lsDataBase.add(DBinfo);
	}
	/**
	 * ��ֵӦ����AbsNetEntity��ENTITY�е�һԱ�������ǻ��������ҩ��ȣ�������Ҫ�������
	 * ֻ�е�flag=ENTITY_GENEʱ���Ż���lsNcbiids��lsUniProtIDs
	 */
	public String getFlag() {
		return flag;
	}
	/**
	 * ��ֵӦ����AbsNetEntity��ENTITY�е�һԱ�������ǻ��������ҩ��ȣ�������Ҫ�������
	 * ֻ�е�flag=ENTITY_GENEʱ���Ż���lsNcbiids��lsUniProtIDs
	 */
	public void setFlag(String flag)
	{
		this.flag = flag;
	}
	/**
	 * 	���Ƚ�����AbsNetEntity��entityID�Ƿ���ͬ�����ǵ�entityID.trim()Ϊ""ʱ��������
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
	 * 	��ϸ�Ƚ�����AbsNetEntity�Ƿ���ͬ
	 * ����Ҳֻ�ǱȽ�entityID��flag��lsNcbiids��lsUniProtIDs�Ƿ�һ��
	 */
	public boolean equals(Object obj) {
		if (!equalSimple(obj))
			return false;
		AbsNetEntity otherObj = (AbsNetEntity)obj;
		if (!flag.equals(otherObj.getFlag())) 
			return false;
		
		//NCBIID�Ѿ���д��equals�����ˣ�ֻ�Ƚ�geneID�Ƿ�һ��
		if (!hashCopedIDs.equals(otherObj.getHashCopedIDs()))
			return false;
		
		//�Ƚ������ڵ����ݿ��Ƿ�һ��
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
	 * ��������
	 */
	static HashMap<String, ArrayList<AbsNetEntity>> hashAbsNetEntity = new HashMap<String, ArrayList<AbsNetEntity>>();
	
	/**
	 * ����һ��entityID�����ظ�entity���е�ϸ�ֽڵ㣬<br>
	 * �������ͨ��Ϊ���������<br>
	 * Ʃ��Kegg����һ���ڵ����������򣬶�������������reactome�ж�Ӧ��������ͬ�Ľڵ㡣<br>
	 * ��ô���ĸ�����ͼƬ��Ӧ����ͬһ���㡣<br>
	 * @param entityID
	 * @return
	 */
	public static ArrayList<AbsNetEntity> getLsAbsNetEntity(String entityID) {
		return hashAbsNetEntity.get(entityID.trim());
	}
	
	/**
	 * ����һ���ڵ���Ϣ��װ��ָ���Ĺ�ϣ���� 
	 * ���hash����û�иĽڵ㣬ֱ������һ����key
	 * ���hash�����иýڵ��list���Ƚϸýڵ���Ϣ���list�е�����Ԫ�أ����������ͬ���ͼ���ýڵ�
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
	 * ���hash��
	 */
	public static void cleanHash() {
		hashAbsNetEntity = new HashMap<String, ArrayList<AbsNetEntity>>();
	}
	
	/**
	 * ����ĳ���ڵ㣬�ڶ�Ӧ��pathway�в���������ؽڵ�
	 * @return
	 */
	public abstract ArrayList<AbsNetRelate> getRelate(boolean blast, int StaxID, double evalue);
	
}
