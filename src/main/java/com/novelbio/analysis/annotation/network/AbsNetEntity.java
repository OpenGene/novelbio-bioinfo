package com.novelbio.analysis.annotation.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.database.domain.AbsPathway;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modcopeid.GeneID;

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
	public static int ENTITY_GENE = 123; 
	public static int ENTITY_COMPOUND = 456;
	public static int ENTITY_DRUG = 789;
	/**
	 * �ýڵ�����ͣ�������ʾ���Ψһ�㡣
	 * ��ʱ��������������Ʃ��Kegg����һ���ڵ�����������
	 * ��ô����������ͼƬ��Ӧ����ͬһ���㡣��ʱ��Ϳ�����ENTITY_COMPLEX����ʾ�����ڵ㣬����С�������������
	 * 
	 */
	public static int ENTITY_COMPLEX = 1024;
	/**
	 * �����Ǳ�entity���ڵ�DBinfo
	 */
	public static String DBINFO_KEGG = "kegg"; 
	public static String DBINFO_REACTOME = "Reactome";

	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	HashSet<GeneID> hashCopedIDs = new HashSet<GeneID>();
	
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
	
	int taxID = 0;
	
	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	public HashSet<GeneID> getHashCopedIDs() {
		return hashCopedIDs;
	}
	/**
	 * �ýڵ�������ɸ�CopedID
	 */
	public void addCopedID(GeneID copedID)
	{
		hashCopedIDs.add(copedID);
	}
 
	/**
	 * �ýڵ������
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * �ýڵ������
	 */
	public void setTaxID(int taxID)
	{
		this.taxID = taxID;
	}
	/**
	 * ��ֵӦ����AbsNetEntity��ENTITY�е�һԱ�������ǻ��������ҩ��ȣ�������Ҫ�������
	 */
	public String getFlag() {
		return flag;
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
		
		return true;
	}
	
	public int hashCode()
	{
		int hashCode = hashCopedIDs.hashCode() + lsDataBase.hashCode()*1000000;
		return hashCode;
	}
	
	/**
	 * ��������
	 */
	static ArrayList<AbsNetRelate> hashAbsNetRelate = new ArrayList<AbsNetRelate>();
	
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
	 * ����ĳ���ڵ㣬�ڶ�Ӧ��pathway�в���������ؽڵ�
	 * @return
	 */
	public abstract ArrayList<AbsNetRelate> getRelate(boolean blast, int StaxID, double evalue);
	
}
