package com.novelbio.analysis.annotation.pathway.network;

import java.util.ArrayList;

import com.novelbio.database.entity.AbsPathway;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;

/**
 * ��������ͼ�е����ڵ���Ϣ����<br>
 * Ӧ���Ǹ�����<br>
 * ���а����ýڵ��������Ļ��򣬸ýڵ����ڵ����ݿ⣬�ýڵ����ڵ�pathway<br>
 * 
 * @author zong0jie
 *
 */
public class AbsNetEntity {
	/**
	 * �����Ǳ�entity�������ʲô���
	 */
	public static String ENTITY_GENE = "gene"; 
	public static String ENTITY_COMPOUND = "compound";
	public static String ENTITY_DRUG = "drug";
	
	
	
	
	
	
	/**
	 * �ýڵ�������ɸ�NCBIID
	 */
	ArrayList<NCBIID> lsNcbiids;
	
	/**
	 * �ýڵ�������ɸ�UniProtID
	 */
	ArrayList<UniProtID> lsUniProtIDs;
	
	/**
	 * �ýڵ����ڵ�pathway��Ϣ
	 */
	ArrayList<AbsPathway> lsPathInfo; 
	
	/**
	 * �ýڵ��ID
	 */
	String entityID;
	/**
	 * ��ֵӦ����AbsNetEntity��ENTITY�е�һԱ�������ǻ��������ҩ��ȣ�������Ҫ�������
	 * ֻ�е�flag=ENTITY_GENEʱ���Ż���lsNcbiids��lsUniProtIDs
	 */
	String flag = "";
	
	
	
}
