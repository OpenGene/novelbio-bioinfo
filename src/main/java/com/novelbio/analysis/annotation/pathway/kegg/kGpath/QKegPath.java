package com.novelbio.analysis.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.novelbio.analysis.annotation.pathway.network.KGpathScr2Trg;
import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.domain.kegg.*;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathRelation;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.database.service.servkegg.ServKRelation;







/**
 * ������Ҫѡ��Blast��Blast�������pathway��Ϣ��
 * @author zong0jie
 *
 */
public class QKegPath {
	/**
	 *����У��<br>
	 * ����string[3]��geneID��Ϣ��blast����Ϣ
	 * 0:queryID
	 * 1:geneID 	
	 * 2:UniProtID :���1û���������
	 * �����Ҫblast����blast˳��ΪgeneID��queryID��uniProtID��ʱ������blast���ȴ���������
	 * ���geneID��Ӧ��keggID
	 * �����blast�����ұ����ָ�geneIDû��KeggID����ôѡ��blast���ֵ�KeggID
	 * �����blast�����ұ����и�geneID��keggID����ô����
	 * �����������taxID����ô
	 * @param geneIDType ���Ϊ���֣�˵����ncbiID�����Ϊ��ĸ��˵����uniprotID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return
	 * ���� string[8]:<br>
	 * 0: queryID <br>
	 * 1: geneID<br>
	 * 2: UniProtID<br>
	 * 3: KeggID <br>
	 * �������û��keggIDʱ������blast�Ϳ�������Ϣ<br>
	 * 4. blast evalue<br>
	 * 5: subTax Ŀ������<br>
	 * 6: subGeneID Ŀ�����ֵ�geneID<br>
	 * 7: subKO Ŀ�����ֵ�KO��ע�ⲻ��keggID��KO��ֱ�����ڱȶԵ���������ȥ,����ж��KO������"//"����
	 * 
	 */
	public static String[] getKeggID(int taxID, String[] geneIDInfo,boolean blast,int subTaxID,double evalue ) 
	{
		GeneID copedID = null;
		if (geneIDInfo[1]!=null) {
			copedID = new GeneID(GeneID.IDTYPE_GENEID, geneIDInfo[1], taxID);
		}
		else if (geneIDInfo[2]!=null) {
			copedID = new GeneID(GeneID.IDTYPE_UNIID, geneIDInfo[2], taxID);
		}
		else {
			copedID = new GeneID(geneIDInfo[0], taxID);
		}
		copedID.setBlastInfo(evalue, subTaxID);
		
		String[] kegIDInfo = new String[8];
		kegIDInfo[0] = geneIDInfo[0]; kegIDInfo[1] = geneIDInfo[1]; kegIDInfo[2] = geneIDInfo[2];
		kegIDInfo[3] = copedID.getKeggInfo().getKegID();
		if (kegIDInfo[3] != null) {
			return kegIDInfo;
		}
		if (copedID.getLsBlastInfos() == null || copedID.getLsBlastInfos().size() == 0) {
			return kegIDInfo;
		}
		kegIDInfo[4] = copedID.getLsBlastInfos().get(0).getEvalue() + "";
		kegIDInfo[5] = subTaxID + "";
		kegIDInfo[6] = copedID.getGeneIDBlast().getGenUniID();
		ArrayList<String> lsKO =  copedID.getGeneIDBlast().getKeggInfo().getLsKo();
		if (lsKO == null || lsKO.size() == 0) {
			return kegIDInfo;
		}
		else {
			kegIDInfo[7] = lsKO.get(0);
			for (int i = 1; i < lsKO.size(); i++) {
				kegIDInfo[7] = kegIDInfo[7] + "//" + lsKO.get(i);
			}
			return kegIDInfo;
		}
	}
	/**
	 *
	 * ����������Ϣ���飬��ID��Ϣת��ΪString[3],ͬʱȥ���࣬Ҳ���ǽ���ͬ��geneIDֻ����һ������ͬ��UniProtIDҲֻ����һ��
	 * 0:queryID
	 * 1:geneID
	 * 2:UniProtID :���1û���������
	 * @param accID �����accID
	 * @param taxID�������symbol��Ҫָ��������ָ��Ϊ0
	 * @param subTaxID
	 * @param evalue
	 */
	public static ArrayList<String[]> getGeneID(List<String> accID, int taxID) 
	{
		/**
		 * ����ȥ�ظ��ı�keyΪgeneID/UniProtID/accID��value
		 * ΪString[3]
		 * 0:queryID
		 * 1:geneID 	
		 * 2:UniProtID :���1û���������
		 */
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < accID.size(); i++) 
		{
			GeneID copedID = new GeneID(accID.get(i), taxID);
			try {
				if (hashCopedID.contains(copedID)) {
					continue;
				}
			} catch (Exception e) {
				if (hashCopedID.contains(copedID)) {
					continue;
				}
			}
		
			String[] geneInfo = new String[3];
			geneInfo[0] = accID.get(i);
			if (copedID.getIDtype().equals(GeneID.IDTYPE_GENEID)) {
				geneInfo[1] = copedID.getGenUniID();
			}
			else if (copedID.getIDtype().equals(GeneID.IDTYPE_UNIID)) {
				geneInfo[2] = copedID.getGenUniID();
			}
			lsResult.add(geneInfo);
		}
		return lsResult;
	}
	
	/**
	 * ����KGentry���󣬽�������relation��entry��ץ������
	 * ע��KGentry��������ֺ����ֶ�Ӧ���Ǳ����֣������blast����������(������)������Ҫת����KO�ͱ�����
	 * @param kGentry���������ͨ����ko��taxID����entry��õ�
	 * @return Hashtable-String-KGpathRelation: string��  keggID��ko
	 * ���һ����ϵ��Ҳ����һ��Ŀ��entry��������ͬ��pathway�г��֣���ô��Ӧ��ֵ������ظ�����ͨͨ���ϣ���"//"����
	 */
	public static Hashtable<String, KGpathScr2Trg>  getHashKGpathRelation(KGentry kGentry)
	{
		ServKEntry servKEntry = new ServKEntry();
		ServKRelation servKRelation = new ServKRelation();
		/**
		 * �����������֮��ص�entry��Ϣ
		 */
		Hashtable<String, KGpathScr2Trg> hashKGpathRelations = new Hashtable<String, KGpathScr2Trg>();
		//������entry����parentID�ģ�Ҳ����һ��component
		if (kGentry.getParentID()>0)
		{
			////////����и�����Ȳ���entry�еĸ�������ø���������ิ���ﶼ�ҵ�//////////////////////////////////////////////////////////////////////////////////////////////////////////
			KGentry tmpqkGentry = new KGentry();//��ѯ�õ�KGentry
			tmpqkGentry.setPathName(kGentry.getPathName());
			tmpqkGentry.setParentID(kGentry.getParentID());
			//���ﶼ����queryEntry��component��entry,
			ArrayList<KGentry> lsSubKGentries=servKEntry.queryLsKGentries(tmpqkGentry);
			for (int i = 0; i < lsSubKGentries.size(); i++) 
			{
				KGentry tmpkgGentrySub=lsSubKGentries.get(i);
				//һ��pathway�ϵ�entry���ж��keggID��Ӧ��ȥ����ô��ʱ����ͬentry��keggID--Ҳ����ͬһ��entry�Ĳ�ͬ�Ļ���֮�䲻Ҫ����ϵ����������
				if (tmpkgGentrySub.getID() == kGentry.getID())
				{
					continue;
				}
				KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
				kGpathRelation.setQKGentry(kGentry);
				kGpathRelation.setSKGentry(tmpkgGentrySub);
				kGpathRelation.setType("component");
				kGpathRelation.setPathName(tmpkgGentrySub.getPathName());
				addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
			}
			////////////////�������ֱ���entryID��parentIDȥ����relation����ý����entryID////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////������entryIDȥ����//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kGentry.getID()); tmpQkGrelation.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations = servKRelation.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0) 
				{
					System.out.println("��entryID"+kGrelation.getEntry2ID()+"��pathway"+ kGrelation.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kGentry);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation.getSubtypeName();
					tmpSubInfo[1] = kGrelation.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
					
				}	
			}
			//////////////////////Ȼ����parentIDȥ��///////////////////////////////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation2=new KGrelation();
			tmpQkGrelation2.setEntry1ID(kGentry.getParentID()); tmpQkGrelation2.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations2 = servKRelation.queryLsKGrelations(tmpQkGrelation2);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation2 = removeRep(lsKGrelations2);
			
			Enumeration keys2=hashPathID2KGRelation2.keys();
			while(keys.hasMoreElements())
			{
				String key2=(String)keys2.nextElement();
				KGrelation kGrelation2 = hashPathID2KGRelation.get(key2);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation2.getEntry2ID(), kGrelation2.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("��entryID"+kGrelation2.getEntry2ID()+"��pathway"+ kGrelation2.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kGentry);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation2.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation2.getSubtypeName();
					tmpSubInfo[1] = kGrelation2.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
				}
			}
		}
		else
		{
			////////////////////////////////////////////////ֱ����entryIDȥ����//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kGentry.getID()); tmpQkGrelation.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations = servKRelation.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ��(Ҳ����һ��entry1-entry2���ж����ͬ��subtype)��������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//������entry��Ӧ������ʵ��entry--Ҳ���ǲ�ͬ��keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("��entryID"+kGrelation.getEntry2ID()+"��pathway"+ kGrelation.getPathName()+"��û�ҵ���Ӧ��entry");
				}
				for (int j = 0; j < lskGentriesSub.size(); j++)
				{
					KGpathScr2Trg kGpathRelation = new KGpathScr2Trg();
					kGpathRelation.setQKGentry(kGentry);
					kGpathRelation.setSKGentry(lskGentriesSub.get(j));
					kGpathRelation.setType(kGrelation.getType());
					String[] tmpSubInfo = new String[2];
					tmpSubInfo[0] = kGrelation.getSubtypeName();
					tmpSubInfo[1] = kGrelation.getSubtypeValue();
					kGpathRelation.setSubtypeInfo(tmpSubInfo);
					kGpathRelation.setPathName(lskGentriesSub.get(j).getPathName());
					addHashKGpathRelation(hashKGpathRelations,kGpathRelation);
				}
			}
		}
		return hashKGpathRelations;
	}
	
	/**
	 * ��kGpathRelationװ��hash���У�keyΪkeggID/KO��valueΪKGpathRelation
	 * key: entryName
	 * value:KGpathRelation
	 * ���� KGpathRelation��ÿ����ϵ����ж��Type��SubtypeInfo��tmpPathName����ô���Ǹ��Զ���//����
	 * @param hashKGpathRelations
	 * @param kGpathRelation
	 */
	public static void addHashKGpathRelation(Hashtable<String, KGpathScr2Trg> hashKGpathRelations, KGpathScr2Trg kGpathRelation)
	{
		//�����entry��hash���Ѿ����֣���ô����relation�Ĺ�ϵ
		KGentry tmpkgGentrySub=kGpathRelation.getSKGentry();
		if (hashKGpathRelations.containsKey(tmpkgGentrySub.getEntryName() ) )
		{
			KGpathScr2Trg tmpkGpathRelation = hashKGpathRelations.get(tmpkgGentrySub.getEntryName());
			String[] tmpSubtypeInfo = tmpkGpathRelation.getSubtypeInfo();
			String tmpType = tmpkGpathRelation.getType();
			String tmpPathName = tmpkGpathRelation.getPathName();
			///////////////////////////////////////////////��ͬ��type///////////////////////////////////////
			if (tmpType == null) {
				tmpType = kGpathRelation.getType();
				tmpkGpathRelation.setType(tmpType);
			}
			else if ( tmpType != null && kGpathRelation.getType() != null && !tmpType.contains(kGpathRelation.getType() )  ) 
			{
				tmpType = tmpType + "//" + kGpathRelation.getType();
				tmpkGpathRelation.setType(tmpType);
				//System.out.println("��entry��ϵ���������ϣ����������в�ͬ��type");
			}
			
			////////////////////////////////////////��ͬ��subtype///////////////////////////////////////////////////////////////////////
			if (tmpSubtypeInfo[0] == null) {
				//���ô��ݣ�����Ҫ��װ��hash��
				tmpSubtypeInfo[0]=kGpathRelation.getSubtypeInfo()[0];
				tmpSubtypeInfo[1]=kGpathRelation.getSubtypeInfo()[1];
			}
			if (tmpSubtypeInfo[0] != null && kGpathRelation.getSubtypeInfo()[0]!=null
					&& !tmpSubtypeInfo[0].contains(kGpathRelation.getSubtypeInfo()[0]))
			{
				//���ô��ݣ�����Ҫ��װ��hash��
				tmpSubtypeInfo[0]=tmpSubtypeInfo[0]+"//"+kGpathRelation.getSubtypeInfo()[0];
				tmpSubtypeInfo[1]=tmpSubtypeInfo[1]+"//"+kGpathRelation.getSubtypeInfo()[1];
			}
			//��ͬ��pathName
			if (!tmpPathName.contains( kGpathRelation.getPathName() ) )
			{
				tmpPathName = tmpPathName + "//" + kGpathRelation.getPathName();
				tmpkGpathRelation.setPathName(tmpPathName);
			//	System.out.println("��entry��ϵ���������ϣ����������в�ͬ��pathName");
			}
			return;
		}
		hashKGpathRelations.put(tmpkgGentrySub.getEntryName(), kGpathRelation);
	}
	
	
	
	/**
	 * ��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����
	 * ȥ������󣬽�subtype��//�ָ
	 * @param lsKGrelations
	 * @return
	 */
	private static Hashtable<String, KGrelation> removeRep(ArrayList<KGrelation> lsKGrelations ) 
	{
		/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
		Hashtable<String, KGrelation> hashPathID2KGRelation = new Hashtable<String, KGrelation>();
		for (int i = 0; i < lsKGrelations.size(); i++) 
		{
			KGrelation tmpKGrelation = lsKGrelations.get(i);
			//��ΪentryID1����һ���ģ����Բ�����entryID1
			String key = tmpKGrelation.getPathName() + tmpKGrelation.getEntry2ID(); //pathName��entryID2������һ���Ƕ�һ�޶����ˣ����������string��Ϊkey���Դ���ȥ�ظ�������Ļ�Ҫ��KGrelation������дhash��
			if (hashPathID2KGRelation.containsKey(key)) 
			{
				//��������ظ���entry1��entry2�ԣ� ��ô��subtypeName��subtypeValue�����ں���
				KGrelation kGrelationtmp = hashPathID2KGRelation.get(key);
				if ( kGrelationtmp.getSubtypeName().equals( tmpKGrelation.getSubtypeName() )  ) {
					continue;
				}
				kGrelationtmp.setSubtypeName(kGrelationtmp.getSubtypeName()+ "//" + tmpKGrelation.getSubtypeName());
				kGrelationtmp.setSubtypeValue(kGrelationtmp.getSubtypeValue()+ "//" + tmpKGrelation.getSubtypeValue());
			}
			else 
			{
				hashPathID2KGRelation.put(key, tmpKGrelation);
			}
		}
		return hashPathID2KGRelation;
	}
	
	
	
	/**
	 * ����entryID��pathName�����Ҿ����entry<br>
	 * ��Ϊһ��entryID���ܻ��Ӧ���keggID����ôҲ�ͻ᷵�ض��KGentry����<br>
	 * ���Ȳ���entry��ID��û�ҵ��Ļ�������parentID<br>
	 * �����parentID������Ҫ�����--Ҳ����component ֮������������Ϊ�����루��������ҵ�ʱ�� �Ѿ�������֮�����ϵ����Щ��ϵ�ڽ������У���Ŀmapping��ȥ��ʱ��ᱻ���㵽
	 * @param entryID
	 * @param pathName
	 * @return
	 */
	private static ArrayList<KGentry> getRelateEntry(int entryID, String pathName) {
		ServKEntry servKEntry = new ServKEntry();
		KGentry qKGentry = new KGentry();
		qKGentry.setID(entryID); qKGentry.setPathName(pathName);
		
		ArrayList<KGentry> lskGentries = servKEntry.queryLsKGentries(qKGentry);
		if (lskGentries != null && lskGentries.size() > 0) 
		{
			return lskGentries;
		}
		//���û����entryID��Ŀ��������������parentID������
		else 
		{
			qKGentry = new KGentry();
			qKGentry.setParentID(entryID); qKGentry.setPathName(pathName);
			ArrayList<KGentry> lsKGentries2=servKEntry.queryLsKGentries(qKGentry);
			return lsKGentries2;
		}
	}
	
	
	/**
	 * ����pathName�����Ҹ�pathway��Ϣ�Լ���֮�й�ϵ��PathwayName<br>
	 * @param pathName
	 * @return object[]
	 * 0: ��PathID��Ӧ��KGpathway
	 * 1����pathID��trg��Ӧ��lsPathName
	 */
	private static ArrayList<String> getRelatePath(String pathName) {
		ServKPathRelation servKPathRelation = new ServKPathRelation();
		KGpathRelation kGpathRelation = new KGpathRelation();
		KGrelation kGrelation =new KGrelation();
		kGpathRelation.setPathName(pathName);
		ArrayList<KGpathRelation> lsKGpathRelations = servKPathRelation.queryLskGpathRelations(kGpathRelation);
		ArrayList<String> lsTrgPathName = new ArrayList<String>();
		for (KGpathRelation kGpathRelation2 : lsKGpathRelations) {
			lsTrgPathName.add(kGpathRelation2.getTrgPath());
		}
		return lsTrgPathName;
	}
	/**
	 * ����һ��pathID��������ЩpathID��Ӧ��kGpathway
	 * �ڲ�ȥ�ո�
	 * Hashtable-String-KGpathway<br>
	 * key��pathID value��KGpathway
	 * @param PathID
	 * @return
	 */
	private static Hashtable<String, KGpathway> getHashKGpathway(String[] PathID)
	{
		ServKPathway servKPathway = new ServKPathway();
		Hashtable<String, KGpathway> hashPath = new Hashtable<String, KGpathway>();
		for (String string : PathID) {
			string = string.trim();
			KGpathway kGpathway = new KGpathway();
			kGpathway.setPathName(string);
			kGpathway = servKPathway.queryKGpathway(kGpathway);
			hashPath.put(string, kGpathway);
		}
		return hashPath;
	}
	/**
	 * ����һϵ�е�pathID������pathID��hash��
	 * pathID���Զ�ȥ�ո�
	 * key��scr pathID value��ArrayList-KGpathway
	 */
	private static Hashtable<String,ArrayList<String>> getHashPathID(String[] PathID) {
		Hashtable<String,ArrayList<String>> hashlsKgPath = new Hashtable<String, ArrayList<String>>();
		for (String string : PathID) {
			ArrayList<String> lsTrgPath = getRelatePath(string.trim());
			hashlsKgPath.put(string.trim(),lsTrgPath);
		}
		return hashlsKgPath;
	}
	
	/**
	 * ����һϵ��pathID,
	 * pathID�Զ�ȥǰ��ո��Լ�ÿ��pathID����Ӧ��target���������target�Ĺ�ϵ���Լ����Ա�<br>
	 * ����ǽ������pathIDװ��hashset��Ȼ�����pathID��target��������ң����������ٵĲ��Ҷ�ģ��о������Ч��
	 * @param PathID
	 * @return Object[] ��һ������
	 * 0��lsscr2trg string[]
	 * 0:srcPathName
	 * 1:srcPathTitle
	 * 2:trgpathName
	 * 3:trgPathTitle
	 * 1: attribute string[]
	 */
	private static Object[] getSrc2Trg(String[] PathID) {
		//�������PathID���һ��hashset����������
		HashSet<String> hashPathID = new HashSet<String>();
		
		for (String string : PathID) {
			hashPathID.add(string.trim());
		}
		//���1��ÿ��scr��Ӧ��trg��Ϣ
		ArrayList<String[]> lsscr2trg = new ArrayList<String[]>();
		//���2��ÿ��scr��attribute
		Hashtable<String,String[]> hashAttribute = new Hashtable<String,String[]>();
		
		//ÿ��path����Ӧ��trgPathID��hahs��
		Hashtable<String,ArrayList<String>> hashLsScr2HashTrg = getHashPathID(PathID);
		//ÿ��pathway�ľ�����Ϣ
		Hashtable<String, KGpathway> hashPath2KGpathway = getHashKGpathway(PathID);
		
		ArrayList<String> lstmpTrgPathName = new ArrayList<String>();
		//������pathIDȥ����
		for (String scrPathName : PathID) {
			//������pathID��target
			if ((lstmpTrgPathName = hashLsScr2HashTrg.get(scrPathName.trim())) != null) {
				//����Щtarget��pathID����
				for (String trgPathName : lstmpTrgPathName) {
					//����ҵ���˵��������һ��src2trg
					if (hashPathID.contains(trgPathName)) {
						KGpathway scrKGpathway = hashPath2KGpathway.get(scrPathName);
						KGpathway trgKGpathway = hashPath2KGpathway.get(trgPathName);
						String[] tmpRelate = new String[4];
						tmpRelate[0] = scrPathName;
						tmpRelate[1] = scrKGpathway.getTitle();
						tmpRelate[2] =trgPathName;
						tmpRelate[3] =trgKGpathway.getTitle();
						lsscr2trg.add(tmpRelate);
						String[] tmpScrAttr = new String[2];
						tmpScrAttr[0] = scrPathName;
						tmpScrAttr[1] = scrKGpathway.getTitle();
						String[] tmpTrgAttr = new String[2];
						tmpTrgAttr[0] = trgPathName;
						tmpTrgAttr[1] = trgKGpathway.getTitle();
						hashAttribute.put(scrPathName, tmpScrAttr);
						hashAttribute.put(trgPathName, tmpTrgAttr);
					}
				}
			}
		}
		ArrayList<String[]> lsattr = new ArrayList<String[]>();
		Enumeration<String> keys=hashAttribute.keys();

		while(keys.hasMoreElements()){
		String key=keys.nextElement();
		lsattr.add(hashAttribute.get(key));
		} 
		Object[] objResult = new Object[2];
		objResult[0] = lsscr2trg;
		objResult[1] = lsattr;
		return objResult;
	}
	
//	/**
//	 * ������򣬷��ظû����KeggID
//	 * @param geneID
//	 * @return
//	 */
//	public static ArrayList<String> getGeneKegID(String[] geneID,int taxID)
//	{
//		ArrayList<String> lsKeggID = new ArrayList<String>();
//		for (int i = 0; i < geneID.length; i++) {
//			String KeggID = getKeggID(geneID[i],taxID);
//			if (KeggID != null) {
//				lsKeggID.add(KeggID);
//			}
//		}
//		return lsKeggID;
//	}
//	
//	
//	/**
//	 * ����geneID�����ظû����KO,Ŀǰֻ֧��NCBIID�к��еĻ���
//	 * @param genID
//	 */
//	private static String getKeggID2(String genID,int taxID) {
//		ServKIDgen2Keg servKIDgen2Keg = new ServKIDgen2Keg();
//		ServNCBIID servGeneAnno = new ServNCBIID();
//		NCBIID ncbiid = new NCBIID();
//		ncbiid.setAccID(genID);ncbiid.setTaxID(taxID);
//		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
//		long geneID = 0;
//		if (lsNcbiids != null && lsNcbiids.size()>0) {
//			geneID = lsNcbiids.get(0).getGeneId();
//		}
//		if(geneID == 0)
//		{
//			return null;
//		}
//		
//		KGIDgen2Keg kgiDgen2Keg = new KGIDgen2Keg();
//		kgiDgen2Keg.setGeneID(geneID);
//		kgiDgen2Keg.setTaxID(taxID);
//		KGIDgen2Keg kgiDgen2KegS = servKIDgen2Keg.queryKGIDgen2Keg(kgiDgen2Keg);
//		if (kgiDgen2KegS != null) {
//			return kgiDgen2KegS.getKeggID();
//		}
//		else {
//			return null;
//		}
//	}
	
}







