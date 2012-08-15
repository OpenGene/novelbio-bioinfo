package com.novelbio.analysis.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.novelbio.analysis.annotation.pathway.network.KGpathScr2Trg;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;

public class Scr2Target {
	
	

	/**
	 * 
	 * @param pathName ָ�����ĳ��pathway�Ĺ�ϵ,��kegg��pathwayID������ "path:hsa04010"��Ϊ""ʱ���ȫ��
	 * @param accID �����accID
	 * @param ResultFIleScr2Target
	 * @param QtaxID  �����symbol��Ҫָ��������ָ��Ϊ0
	 * @param blast �Ƿ����blast
	 * @param subTaxID �������blast��Ŀ��������ʲô
	 * @param evalue evalue ��ֵ�Ƕ���
	 * @throws Exception
	 */
	public static void getGene2RelateKo(String pathName, List<String> accID,String ResultFIleScr2Target, String resultFIleAttribute,int QtaxID,boolean blast,int subTaxID,double evalue) throws Exception
	{
		GeneID geneID = null;
		ServKIDKeg2Ko servKIDKeg2Ko = new ServKIDKeg2Ko();
		ServKEntry servKEntry = new ServKEntry();
		//�����ݿ���taxID
		if (QtaxID <= 0)
		{
			for (int i = 0; i < accID.size(); i++) 
			{
				geneID = new GeneID(accID.get(i), 0);
				if (geneID.getTaxID() != 0) {
					QtaxID = geneID.getTaxID();
					break;
				}
			}
		}
		/**
		 * �����ϵ��һ��list��object[2]
		 * 0��qGenKegInfo[7]<br>
		<b>0: queryID</b><br>
	1: geneID<br>
	2: UniProtID<br>
	3: KeggID<br>
	�������û��keggIDʱ������blast�Ϳ�������Ϣ<br>
	4. blast evalue<br>
	5: subTax Ŀ������<br>
	6: subGeneID Ŀ�����ֵ�geneID<br>
	7: subKO Ŀ�����ֵ�KO��ע�ⲻ��keggID��KO��ֱ�����ڱȶԵ���������ȥ,����ж��KO������"//"����<br>
	<b>1. Hashtable- String - KGpathRelation </b>
	string:targe��KeggID/KO
	KGpathRelation �� ������Ϣ 
		 */
		ArrayList<Object[]> lsRelationInfo = new ArrayList<Object[]>();
		ArrayList<String[]> lsAccID = QKegPath.getGeneID(accID, QtaxID);
		//һ��һ����accIDȥ����
		for (int i = 0; i < lsAccID.size(); i++) 
		{
			Hashtable<String, KGpathScr2Trg> hashEntryRelation = new Hashtable<String, KGpathScr2Trg>();
			
			String[] qGenKegInfo=QKegPath.getKeggID(QtaxID, lsAccID.get(i), blast, subTaxID, evalue);
			if (qGenKegInfo[3]==null&&qGenKegInfo[7]==null) {
				continue;
			}
			String[] ko = null;
			if (qGenKegInfo[3]!=null) {
				ko = new String[1];
				ko[0] = qGenKegInfo[3];
			}
			else if (qGenKegInfo[7]!=null) {
				ko = qGenKegInfo[7].split("//");
				//ko���涼������ͬԴ�����KO����ô����Ҫ����ЩKOmapping�ر����֣���������еĸ�KO�Ѿ����˶�Ӧ��KeggID���򽫴�KO���ɱ�KeggID�����û�У�������
				for (int j = 0; j < ko.length; j++)
				{
					////////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô���Ի�ø�KO����Ӧ�����ֵ�KeggID������KeggIDֱ��mapping�ر��������û��KeggID������KOȥmapping////////////////////////////////////////////////////////////////
					KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
					kgiDkeg2Ko.setKo(ko[j]); kgiDkeg2Ko.setTaxID(QtaxID);
					ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = servKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
					if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
					{
						//��Ȼһ��ko��Ӧ���keggID�����Ƕ���pathway��˵��һ��ko�Ͷ�Ӧ��һ��pathway�ϣ�����һ��ko�͹���
						String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//����Ǳ����е�KeggID�������KeggIDֱ�ӿ���������Ӧ��pathway
						ko[j] = keggID;
					}
				}
			}
			//һ�������Ӧ���ko����ôһ��ko���ܾ���һ��entry������һ�������Ӧ������ko��
			for (int j = 0; j < ko.length; j++)
			{
				KGentry qkGentry=new KGentry();
				qkGentry.setEntryName(ko[j]);qkGentry.setTaxID(QtaxID);
				ArrayList<KGentry> lsKGentryQuery = servKEntry.queryLsKGentries(qkGentry);
 				for (int k = 0; k < lsKGentryQuery.size(); k++)
				{
 					if (lsKGentryQuery.get(k).getEntryName().equals("hsa:56604")) {
 						System.out.println("stop");
					}
 					
					Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation=QKegPath.getHashKGpathRelation(lsKGentryQuery.get(k));
					Enumeration<String> keys=tmpHashEntryRelation.keys();
					while(keys.hasMoreElements())
					{
						String key = keys.nextElement();
						KGpathScr2Trg tmpkGpathRelation = tmpHashEntryRelation.get(key);
				
						///////////////���ֲ���//////////////////���Կ���relation���ǲ��Ǵ���compound
						if (key.contains("ko")) {
							System.out.println(key);
						}
						if (tmpkGpathRelation.getSKGentry().getType().equals("compound")) {
							System.out.println(tmpkGpathRelation.getSKGentry().getEntryName());
						}
						///////////////////////////////////////////
						//��Ȼ���ko��ÿһ��ko���ж�Ӧ���entry������ʵ��������ko����һ��gene�ģ�Ҳ����˵Ӧ�ý�����ko��Ӧ����Ϣ�ϲ���һ��������ȥ
						QKegPath.addHashKGpathRelation(hashEntryRelation, tmpkGpathRelation);
					}
				}
			}
			Object[] tmpRelation = new Object[2];
			tmpRelation[0]=qGenKegInfo; tmpRelation[1] = hashEntryRelation;
			lsRelationInfo.add(tmpRelation);
		}
		
		getRelation(pathName,lsRelationInfo,QtaxID,ResultFIleScr2Target, resultFIleAttribute);
	}
	
	/**
	 * 
	 * @param pathName ָ�����ĳ��pathway�Ĺ�ϵ,��kegg��pathwayID������ "path:hsa04010"��Ϊ""ʱ���ȫ��
	 * @param accID �����accID
	 * @param ResultFIleScr2Target
	 * @param QtaxID  �����symbol��Ҫָ��������ָ��Ϊ0
	 * @param blast �Ƿ����blast
	 * @param subTaxID �������blast��Ŀ��������ʲô
	 * @param evalue evalue ��ֵ�Ƕ���
	 * @throws Exception
	 */
	public static void getGene2RelateKo2(String pathName, ArrayList<String> lsKeggID,String ResultFIleScr2Target, String resultFIleAttribute,int QtaxID) throws Exception
	{
		ServKEntry servKEntry = new ServKEntry();
		/**
		 * �����ϵ��һ��list��object[2]
		 * 0��qGenKegInfo[7]<br>
		<b>0: queryID</b><br>
	1: geneID<br>
	2: UniProtID<br>
	3: KeggID<br>
	�������û��keggIDʱ������blast�Ϳ�������Ϣ<br>
	4. blast evalue<br>
	5: subTax Ŀ������<br>
	6: subGeneID Ŀ�����ֵ�geneID<br>
	7: subKO Ŀ�����ֵ�KO��ע�ⲻ��keggID��KO��ֱ�����ڱȶԵ���������ȥ,����ж��KO������"//"����<br>
	<b>1. Hashtable- String - KGpathRelation </b>
	string:targe��KeggID/KO
	KGpathRelation �� ������Ϣ 
		 */
		ArrayList<Object[]> lsRelationInfo = new ArrayList<Object[]>();
		//һ��һ����accIDȥ����
		for (int i = 0; i < lsKeggID.size(); i++) 
		{
			Hashtable<String, KGpathScr2Trg> hashEntryRelation = new Hashtable<String, KGpathScr2Trg>();
			String[] ko =  new String[1];
			ko[0] = lsKeggID.get(i);
			String[] qGenKegInfo = new String[4];
			qGenKegInfo[0] = ko[0]; qGenKegInfo[1] = ko[0];  qGenKegInfo[2] = ko[0]; qGenKegInfo[3] = ko[0]; 
			//һ�������Ӧ���ko����ôһ��ko���ܾ���һ��entry������һ�������Ӧ������ko��
			for (int j = 0; j < ko.length; j++)
			{
				KGentry qkGentry=new KGentry();
				qkGentry.setEntryName(ko[j]);qkGentry.setTaxID(QtaxID);
				ArrayList<KGentry> lsKGentryQuery = servKEntry.queryLsKGentries(qkGentry);
 				for (int k = 0; k < lsKGentryQuery.size(); k++)
				{
					Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation=QKegPath.getHashKGpathRelation(lsKGentryQuery.get(k));
					Enumeration<String> keys=tmpHashEntryRelation.keys();
					while(keys.hasMoreElements())
					{
						String key = keys.nextElement();
						KGpathScr2Trg tmpkGpathRelation = tmpHashEntryRelation.get(key);
				
						///////////////���ֲ���//////////////////���Կ���relation���ǲ��Ǵ���compound
						if (key.contains("ko")) {
							System.out.println(key);
						}
						if (tmpkGpathRelation.getSKGentry().getType().equals("compound")) {
							System.out.println(tmpkGpathRelation.getSKGentry().getEntryName());
						}
						///////////////////////////////////////////
						//��Ȼ���ko��ÿһ��ko���ж�Ӧ���entry������ʵ��������ko����һ��gene�ģ�Ҳ����˵Ӧ�ý�����ko��Ӧ����Ϣ�ϲ���һ��������ȥ
						QKegPath.addHashKGpathRelation(hashEntryRelation, tmpkGpathRelation);
					}
				}
			}
			Object[] tmpRelation = new Object[2];
			tmpRelation[0]=qGenKegInfo; tmpRelation[1] = hashEntryRelation;
			lsRelationInfo.add(tmpRelation);
		}
		
		getRelation2(pathName,lsRelationInfo,QtaxID,ResultFIleScr2Target, resultFIleAttribute);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * ����pathway����һ��geneID��Ϣ�Լ�������Ӧ��targetGeneID��Ϣ������pathway��sourceTarget��
	 * @param pathName
	 * @param lsRelationInfo
	 * @param QtaxID
	 * @param ResultFIleScr2Target
	 * @throws Exception
	 */
	private static void getRelation2(String pathName, ArrayList<Object[]> lsRelationInfo ,int QtaxID,String ResultFIleScr2Target, String resultFIleAttribute) throws Exception 
	{
		//source 2 target �ı��
		//string[3] 0:source 1:target 2:relation
		ArrayList<String[]> lsScr2Target = new ArrayList<String[]>();
		
		//����������Entry�����ų���
		ArrayList<String[]> lsRelationEntry = new ArrayList<String[]>();
		for (int i = 0; i < lsRelationInfo.size(); i++) {
			lsRelationEntry.add((String[]) lsRelationInfo.get(i)[0]);
		}
		
		Hashtable<String,String[]> hashEntryInfo = new Hashtable<String, String[]>();
		//װ�����Ľ��
		Hashtable<String,String[]> hashEntryInfoResult = new Hashtable<String, String[]>();
		/////////////ÿ��entry����ע�Ͼ�����Ϣ��Ȼ��װ��hash����������ѯ//////////////////////////////////////////////////
		for (int j = 0; j < lsRelationEntry.size(); j++)
		{
			/**
			 * �����ϵ��һ��list��object[2]
			 * 0��qGenKegInfo[7]<br>
			<b>0: queryID</b><br>
		1: geneID<br>
		2: UniProtID<br>
		3: KeggID<br>
		�������û��keggIDʱ������blast�Ϳ�������Ϣ<br>
		4. blast evalue<br>
		5: subTax Ŀ������<br>
		6: subGeneID Ŀ�����ֵ�geneID<br>
		7: subKO Ŀ�����ֵ�KO��ע����ЩKO�Ѿ�ת��Ϊ�����ֵ�keggID��KO��ֱ�����ڱȶԵ���������ȥ,����ж��KO������"//"����<br>
		<b>1. Hashtable- String - KGpathRelation </b>
		string:targe��KeggID/KO
		KGpathRelation �� ������Ϣ 
			 */
			String[] qGenKegInfo = lsRelationEntry.get(j);
			//�Ȼ�øû������Ϣ string[2] : 0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
			String[] queryGenInfo = new String[8];
			for (int k = 0; k < queryGenInfo.length; k++) {//����ֵ""
				queryGenInfo[k] = "";
			}
			//û��ͬԴko,�����Ѿ����������������
			if (qGenKegInfo[3] == null && qGenKegInfo[7] == null) 
			{
				continue;
			}
			else if (qGenKegInfo[3] !=null)
			{
				queryGenInfo[0] = qGenKegInfo[0];
				queryGenInfo[2] = qGenKegInfo[0];
				queryGenInfo[1] = QtaxID + "";
			}
			hashEntryInfo.put(qGenKegInfo[0], queryGenInfo);
		}
		//��ʼ�̲�tmpHashEntryRelation���м����
		KGpathScr2Trg tmpkGpathRelation = null;
		//��ʽ��ʼ�Ƚϣ���ÿһ��entry������source
		for (int i = 0; i < lsRelationEntry.size(); i++) 
		{
			String[] qGenKegInfo = lsRelationEntry.get(i);
			Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation = (Hashtable<String, KGpathScr2Trg>) lsRelationInfo.get(i)[1];
			//����ÿһ��Ǳ��source��������source������target
			//��Ϊ�����еĻ��򶼷ŵ�source��ȥ�ˣ���ôֻҪ��source�������ǵ�target�����ѵ���Ӧ��һ�Թ�ϵ��
			for (int j = 0; j < lsRelationEntry.size(); j++)
			{
				String[] targetGenKegInfo = lsRelationEntry.get(j);
				//���Ȼ��һ��RelationEntry���������ko����ʵ��ô��ko���ǶԵ�ͬһ�������ϵ�
				String[] ko = null;
				if (targetGenKegInfo[3]!=null)
				{
					ko = new String[1];
					ko[0] = targetGenKegInfo[3];
				}
				else if (targetGenKegInfo[7]!=null)
				{
					ko = targetGenKegInfo[7].split("//");
				}
				for (int k = 0; k < ko.length; k++) 
				{
					//�ҵ���һ�Թ�ϵ
					if (( tmpkGpathRelation =tmpHashEntryRelation.get(ko[k])) != null) 
					{
						 String[] tmpScr2Target = new String[5];
						 tmpScr2Target[0] = hashEntryInfo.get(qGenKegInfo[0])[0];
						 tmpScr2Target[1] = hashEntryInfo.get(targetGenKegInfo[0])[0];
						 tmpScr2Target[2] = tmpkGpathRelation.getType();
						 tmpScr2Target[3] = tmpkGpathRelation.getSubtypeInfo()[0];
						 tmpScr2Target[4] = tmpkGpathRelation.getPathName();
						 //////////////////���Ҫ���mapk////////////////////////////////////////////////////////////////////
						if (!tmpScr2Target[4].contains(pathName)) {
							continue;
						}
						 ///////////////////////////////////////////////////////////////////////////////////////////
						 //װ�ع�ϵ
						 lsScr2Target.add(tmpScr2Target);
						 //��entry������װ��hash��
						 String[] ScrEntryAttribute = hashEntryInfo.get(qGenKegInfo[0]);
						 //װ��pathway
						 if (ScrEntryAttribute[7].equals("")) {
							 ScrEntryAttribute[7] = tmpkGpathRelation.getPathName();
						} 
						 else {
							String[] tmpPath = tmpkGpathRelation.getPathName().split("//");
							for (int m = 0; m < tmpPath.length; m++) {
								if (!ScrEntryAttribute[7].contains(tmpPath[m])) {
									ScrEntryAttribute[7] =ScrEntryAttribute[7] + "//" +tmpPath[m];
								}
							}
						}
					
						 
						 String[] targetEntryAttribute = hashEntryInfo.get(targetGenKegInfo[0]);
						 //װ��pathway
						 if (targetEntryAttribute[7].equals("")) {
							 targetEntryAttribute[7] = tmpkGpathRelation.getPathName();
						} 
						 else {
							String[] tmpPath = tmpkGpathRelation.getPathName().split("//");
							for (int m = 0; m < tmpPath.length; m++) {
								if (!targetEntryAttribute[7].contains(tmpPath[m])) {
									targetEntryAttribute[7] =targetEntryAttribute[7] + "//" +tmpPath[m];
								}
							}
						}
						 
						 hashEntryInfoResult.put(qGenKegInfo[0],ScrEntryAttribute);
						 hashEntryInfoResult.put(targetGenKegInfo[0], targetEntryAttribute);
						 break;
					}
				}
			}
		}
		String[] title = new String[5];
		title[0] = "source"; title[1] = "target"; title[2] = "relation"; title[3] = "detailRelation";  title[4] = "pathway"; 
		lsScr2Target.add(0, title);
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(ResultFIleScr2Target, true, false);
		txtReadandWrite.ExcelWrite(lsScr2Target, "\t", 1, 1);
		
		
		Enumeration<String> keys=hashEntryInfoResult.keys();
		
		ArrayList<String[]> lsAttribute = new ArrayList<String[]>();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			String[] tmpAttribute = hashEntryInfoResult.get(key);
			lsAttribute.add(tmpAttribute);
		} 
		//0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
		String[] title2 = new String[8];
		title2[0] = "symbol/AccID";title2[1] = "taxID";title2[2] = "description";title2[3] = "blast Evalue";
		title2[4] = "subject TaxID";title2[5] = "subject Symbol/accID";title2[6] = "subject Description";
		title2[7] = "pathWay";
		lsAttribute.add(0,title2);
		txtReadandWrite.setParameter(resultFIleAttribute, true, false);
		txtReadandWrite.ExcelWrite(lsAttribute, "\t", 1, 1);
	}
	
	/**
	 * ����pathway����һ��geneID��Ϣ�Լ�������Ӧ��targetGeneID��Ϣ������pathway��sourceTarget��
	 * @param pathName
	 * @param lsRelationInfo
	 * @param QtaxID
	 * @param ResultFIleScr2Target
	 * @throws Exception
	 */
	private static void getRelation(String pathName, ArrayList<Object[]> lsRelationInfo ,int QtaxID,String ResultFIleScr2Target, String resultFIleAttribute) throws Exception 
	{
		ServGeneInfo servGeneInfo = new ServGeneInfo();
		//source 2 target �ı��
		//string[3] 0:source 1:target 2:relation
		ArrayList<String[]> lsScr2Target = new ArrayList<String[]>();
		
		//����������Entry�����ų���
		ArrayList<String[]> lsRelationEntry = new ArrayList<String[]>();
		for (int i = 0; i < lsRelationInfo.size(); i++) {
			lsRelationEntry.add((String[]) lsRelationInfo.get(i)[0]);
		}
		
		Hashtable<String,String[]> hashEntryInfo = new Hashtable<String, String[]>();
		//װ�����Ľ��
		Hashtable<String,String[]> hashEntryInfoResult = new Hashtable<String, String[]>();
		/////////////ÿ��entry����ע�Ͼ�����Ϣ��Ȼ��װ��hash����������ѯ//////////////////////////////////////////////////
		for (int j = 0; j < lsRelationEntry.size(); j++)
		{
			/**
			 * �����ϵ��һ��list��object[2]
			 * 0��qGenKegInfo[7]<br>
			<b>0: queryID</b><br>
		1: geneID<br>
		2: UniProtID<br>
		3: KeggID<br>
		�������û��keggIDʱ������blast�Ϳ�������Ϣ<br>
		4. blast evalue<br>
		5: subTax Ŀ������<br>
		6: subGeneID Ŀ�����ֵ�geneID<br>
		7: subKO Ŀ�����ֵ�KO��ע����ЩKO�Ѿ�ת��Ϊ�����ֵ�keggID��KO��ֱ�����ڱȶԵ���������ȥ,����ж��KO������"//"����<br>
		<b>1. Hashtable- String - KGpathRelation </b>
		string:targe��KeggID/KO
		KGpathRelation �� ������Ϣ 
			 */
			String[] qGenKegInfo = lsRelationEntry.get(j);
			//�Ȼ�øû������Ϣ string[2] : 0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
			String[] queryGenInfo = new String[8];
			for (int k = 0; k < queryGenInfo.length; k++) {//����ֵ""
				queryGenInfo[k] = "";
			}
			//û��ͬԴko,�����Ѿ����������������
			if (qGenKegInfo[3] == null && qGenKegInfo[7] == null) 
			{
				continue;
			}
			else if (qGenKegInfo[3] !=null)
			{
				GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, qGenKegInfo[1], QtaxID);
				queryGenInfo[0] = copedID.getSymbol();
				queryGenInfo[2] = copedID.getDescription();
				queryGenInfo[1] = QtaxID + "";
				
			}
			else if (qGenKegInfo[7] != null) 
			{
				//���geneID����
				if (qGenKegInfo[1] != null) {
					GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, qGenKegInfo[1], QtaxID);
					queryGenInfo[0] = copedID.getSymbol();
					queryGenInfo[2] = copedID.getDescription();
					queryGenInfo[1] = QtaxID + "";
				}
				else 
				{
					queryGenInfo[0] = qGenKegInfo[0];
				}
				
				queryGenInfo[3] = qGenKegInfo[4]; queryGenInfo[4] = qGenKegInfo[5];
				GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, qGenKegInfo[6], 0);
				//���û��symbol
				queryGenInfo[5] = copedID.getSymbol();
				queryGenInfo[6] = copedID.getDescription();
			}
			hashEntryInfo.put(qGenKegInfo[0], queryGenInfo);
		}
		
		
		

		//��ʼ�̲�tmpHashEntryRelation���м����
		KGpathScr2Trg tmpkGpathRelation = null;
		//��ʽ��ʼ�Ƚϣ���ÿһ��entry������source
		for (int i = 0; i < lsRelationEntry.size(); i++) 
		{
			String[] qGenKegInfo = lsRelationEntry.get(i);
			Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation = (Hashtable<String, KGpathScr2Trg>) lsRelationInfo.get(i)[1];
			//����ÿһ��Ǳ��source��������source������target
			//��Ϊ�����еĻ��򶼷ŵ�source��ȥ�ˣ���ôֻҪ��source�������ǵ�target�����ѵ���Ӧ��һ�Թ�ϵ��
			for (int j = 0; j < lsRelationEntry.size(); j++)
			{
				String[] targetGenKegInfo = lsRelationEntry.get(j);
				//���Ȼ��һ��RelationEntry���������ko����ʵ��ô��ko���ǶԵ�ͬһ�������ϵ�
				String[] ko = null;
				if (targetGenKegInfo[3]!=null)
				{
					ko = new String[1];
					ko[0] = targetGenKegInfo[3];
				}
				else if (targetGenKegInfo[7]!=null)
				{
					ko = targetGenKegInfo[7].split("//");
				}
				for (int k = 0; k < ko.length; k++) 
				{
					//�ҵ���һ�Թ�ϵ
					if (( tmpkGpathRelation =tmpHashEntryRelation.get(ko[k])) != null) 
					{
						 String[] tmpScr2Target = new String[5];
						 tmpScr2Target[0] = hashEntryInfo.get(qGenKegInfo[0])[0];
						 tmpScr2Target[1] = hashEntryInfo.get(targetGenKegInfo[0])[0];
						 tmpScr2Target[2] = tmpkGpathRelation.getType();
						 tmpScr2Target[3] = tmpkGpathRelation.getSubtypeInfo()[0];
						 tmpScr2Target[4] = tmpkGpathRelation.getPathName();
						 //////////////////���Ҫ���mapk////////////////////////////////////////////////////////////////////
						if (!tmpScr2Target[4].contains(pathName)) {
							continue;
						}
						 ///////////////////////////////////////////////////////////////////////////////////////////
						 //װ�ع�ϵ
						 lsScr2Target.add(tmpScr2Target);
						 //��entry������װ��hash��
						 String[] ScrEntryAttribute = hashEntryInfo.get(qGenKegInfo[0]);
						 //װ��pathway
						 if (ScrEntryAttribute[7].equals("")) {
							 ScrEntryAttribute[7] = tmpkGpathRelation.getPathName();
						} 
						 else {
							String[] tmpPath = tmpkGpathRelation.getPathName().split("//");
							for (int m = 0; m < tmpPath.length; m++) {
								if (!ScrEntryAttribute[7].contains(tmpPath[m])) {
									ScrEntryAttribute[7] =ScrEntryAttribute[7] + "//" +tmpPath[m];
								}
							}
						}
					
						 
						 String[] targetEntryAttribute = hashEntryInfo.get(targetGenKegInfo[0]);
						 //װ��pathway
						 if (targetEntryAttribute[7].equals("")) {
							 targetEntryAttribute[7] = tmpkGpathRelation.getPathName();
						} 
						 else {
							String[] tmpPath = tmpkGpathRelation.getPathName().split("//");
							for (int m = 0; m < tmpPath.length; m++) {
								if (!targetEntryAttribute[7].contains(tmpPath[m])) {
									targetEntryAttribute[7] =targetEntryAttribute[7] + "//" +tmpPath[m];
								}
							}
						}
						 
						 hashEntryInfoResult.put(qGenKegInfo[0],ScrEntryAttribute);
						 hashEntryInfoResult.put(targetGenKegInfo[0], targetEntryAttribute);
						 break;
					}
				}
			}
		}
		String[] title = new String[5];
		title[0] = "source"; title[1] = "target"; title[2] = "relation"; title[3] = "detailRelation";  title[4] = "pathway"; 
		lsScr2Target.add(0, title);
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(ResultFIleScr2Target);
		excelOperate.WriteExcel(1, 1, lsScr2Target);
//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
//		txtReadandWrite.setParameter(ResultFIleScr2Target, true, false);
//		txtReadandWrite.ExcelWrite(lsScr2Target, "\t", 1, 1);
		
		
		Enumeration<String> keys=hashEntryInfoResult.keys();
		
		ArrayList<String[]> lsAttribute = new ArrayList<String[]>();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			String[] tmpAttribute = hashEntryInfoResult.get(key);
			lsAttribute.add(tmpAttribute);
		} 
		//0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
		String[] title2 = new String[8];
		title2[0] = "symbol/AccID";title2[1] = "taxID";title2[2] = "description";title2[3] = "blast Evalue";
		title2[4] = "subject TaxID";title2[5] = "subject Symbol/accID";title2[6] = "subject Description";
		title2[7] = "pathWay";
		lsAttribute.add(0,title2);
		ExcelOperate excelOperate2 = new ExcelOperate();
		excelOperate2.openExcel(resultFIleAttribute);
		excelOperate2.WriteExcel(1, 1, lsAttribute);
//		txtReadandWrite.setParameter(resultFIleAttribute, true, false);
//		txtReadandWrite.ExcelWrite(lsAttribute, "\t", 1, 1);
	}
	
}
