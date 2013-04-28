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
 * 根据需要选择Blast或不Blast，来获得pathway信息。
 * @author zong0jie
 *
 */
public class QKegPath {
	/**
	 *经过校正<br>
	 * 输入string[3]的geneID信息和blast的信息
	 * 0:queryID
	 * 1:geneID 	
	 * 2:UniProtID :如果1没有这个才有
	 * 如果需要blast，则blast顺序为geneID、queryID，uniProtID暂时不参与blast，等待后期完善
	 * 获得geneID对应的keggID
	 * 如果有blast，并且本物种该geneID没有KeggID，那么选择blast物种的KeggID
	 * 如果有blast，并且本物中该geneID有keggID，那么跳过
	 * 如果本基因有taxID，那么
	 * @param geneIDType 如果为数字，说明是ncbiID，如果为字母，说明是uniprotID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return
	 * 返回 string[8]:<br>
	 * 0: queryID <br>
	 * 1: geneID<br>
	 * 2: UniProtID<br>
	 * 3: KeggID <br>
	 * 如果上面没有keggID时，后面blast就可能有信息<br>
	 * 4. blast evalue<br>
	 * 5: subTax 目标物种<br>
	 * 6: subGeneID 目标物种的geneID<br>
	 * 7: subKO 目标物种的KO，注意不是keggID，KO可直接用于比对到本物种上去,如果有多个KO，则用"//"隔开
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
		kegIDInfo[6] = copedID.getGeneIDBlast().getGeneUniID();
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
	 * 输入序列信息数组，将ID信息转化为String[3],同时去冗余，也就是将相同的geneID只保留一个，相同的UniProtID也只保留一个
	 * 0:queryID
	 * 1:geneID
	 * 2:UniProtID :如果1没有这个才有
	 * @param accID 输入的accID
	 * @param taxID：如果是symbol需要指定，否则指定为0
	 * @param subTaxID
	 * @param evalue
	 */
	public static ArrayList<String[]> getGeneID(List<String> accID, int taxID) 
	{
		/**
		 * 用来去重复的表，key为geneID/UniProtID/accID，value
		 * 为String[3]
		 * 0:queryID
		 * 1:geneID 	
		 * 2:UniProtID :如果1没有这个才有
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
			if (copedID.getIDtype() == GeneID.IDTYPE_GENEID) {
				geneInfo[1] = copedID.getGeneUniID();
			}
			else if (copedID.getIDtype() == GeneID.IDTYPE_UNIID) {
				geneInfo[2] = copedID.getGeneUniID();
			}
			lsResult.add(geneInfo);
		}
		return lsResult;
	}
	
	/**
	 * 输入KGentry对象，将与其有relation的entry都抓出来。
	 * 注意KGentry对象的名字和物种都应该是本物种，如果是blast的其他物种(如人类)，首先要转换成KO和本物种
	 * @param kGentry，这个可以通过用ko和taxID查找entry表得到
	 * @return Hashtable-String-KGpathRelation: string是  keggID或ko
	 * 如果一个关系，也就是一个目的entry在两个不同的pathway中出现，那么相应的值如果不重复，就通通加上，用"//"隔开
	 */
	public static Hashtable<String, KGpathScr2Trg>  getHashKGpathRelation(KGentry kGentry)
	{
		ServKEntry servKEntry = new ServKEntry();
		ServKRelation servKRelation = new ServKRelation();
		/**
		 * 保存最后获得与之相关的entry信息
		 */
		Hashtable<String, KGpathScr2Trg> hashKGpathRelations = new Hashtable<String, KGpathScr2Trg>();
		//如果这个entry是有parentID的，也就是一个component
		if (kGentry.getParentID()>0)
		{
			////////如果有复合物，先查找entry中的复合物，将该复合物的其余复合物都找到//////////////////////////////////////////////////////////////////////////////////////////////////////////
			KGentry tmpqkGentry = new KGentry();//查询用的KGentry
			tmpqkGentry.setPathName(kGentry.getPathName());
			tmpqkGentry.setParentID(kGentry.getParentID());
			//这里都是与queryEntry是component的entry,
			ArrayList<KGentry> lsSubKGentries=servKEntry.queryLsKGentries(tmpqkGentry);
			for (int i = 0; i < lsSubKGentries.size(); i++) 
			{
				KGentry tmpkgGentrySub=lsSubKGentries.get(i);
				//一个pathway上的entry会有多个keggID对应上去，那么这时候相同entry的keggID--也就是同一个entry的不同的基因，之间不要有联系，所以跳过
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
			////////////////接下来分别用entryID和parentID去查找relation表，获得结果的entryID////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////首先用entryID去查找//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kGentry.getID()); tmpQkGrelation.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations = servKRelation.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0) 
				{
					System.out.println("用entryID"+kGrelation.getEntry2ID()+"在pathway"+ kGrelation.getPathName()+"中没找到对应的entry");
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
			//////////////////////然后用parentID去查///////////////////////////////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation2=new KGrelation();
			tmpQkGrelation2.setEntry1ID(kGentry.getParentID()); tmpQkGrelation2.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations2 = servKRelation.queryLsKGrelations(tmpQkGrelation2);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation2 = removeRep(lsKGrelations2);
			
			Enumeration keys2=hashPathID2KGRelation2.keys();
			while(keys.hasMoreElements())
			{
				String key2=(String)keys2.nextElement();
				KGrelation kGrelation2 = hashPathID2KGRelation.get(key2);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation2.getEntry2ID(), kGrelation2.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("用entryID"+kGrelation2.getEntry2ID()+"在pathway"+ kGrelation2.getPathName()+"中没找到对应的entry");
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
			////////////////////////////////////////////////直接用entryID去查找//////////////////////////////////////////////////////////////
			KGrelation tmpQkGrelation=new KGrelation();
			tmpQkGrelation.setEntry1ID(kGentry.getID()); tmpQkGrelation.setPathName(kGentry.getPathName());
			ArrayList<KGrelation> lsKGrelations = servKRelation.queryLsKGrelations(tmpQkGrelation);
			/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致(也就是一对entry1-entry2含有多个不同的subtype)，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
			Hashtable<String, KGrelation> hashPathID2KGRelation = removeRep(lsKGrelations);
			Enumeration keys=hashPathID2KGRelation.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				KGrelation kGrelation = hashPathID2KGRelation.get(key);
			
				//搜索该entry对应的所有实际entry--也就是不同的keggID-entry
				ArrayList<KGentry> lskGentriesSub = getRelateEntry(kGrelation.getEntry2ID(), kGrelation.getPathName());
				if ( lskGentriesSub == null || lskGentriesSub.size() == 0)
				{
					System.out.println("用entryID"+kGrelation.getEntry2ID()+"在pathway"+ kGrelation.getPathName()+"中没找到对应的entry");
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
	 * 将kGpathRelation装入hash表中，key为keggID/KO，value为KGpathRelation
	 * key: entryName
	 * value:KGpathRelation
	 * 其中 KGpathRelation中每个关系如果有多个Type和SubtypeInfo和tmpPathName，那么它们各自都用//隔开
	 * @param hashKGpathRelations
	 * @param kGpathRelation
	 */
	public static void addHashKGpathRelation(Hashtable<String, KGpathScr2Trg> hashKGpathRelations, KGpathScr2Trg kGpathRelation)
	{
		//如果该entry在hash中已经出现，那么检查该relation的关系
		KGentry tmpkgGentrySub=kGpathRelation.getSKGentry();
		if (hashKGpathRelations.containsKey(tmpkgGentrySub.getEntryName() ) )
		{
			KGpathScr2Trg tmpkGpathRelation = hashKGpathRelations.get(tmpkgGentrySub.getEntryName());
			String[] tmpSubtypeInfo = tmpkGpathRelation.getSubtypeInfo();
			String tmpType = tmpkGpathRelation.getType();
			String tmpPathName = tmpkGpathRelation.getPathName();
			///////////////////////////////////////////////不同的type///////////////////////////////////////
			if (tmpType == null) {
				tmpType = kGpathRelation.getType();
				tmpkGpathRelation.setType(tmpType);
			}
			else if ( tmpType != null && kGpathRelation.getType() != null && !tmpType.contains(kGpathRelation.getType() )  ) 
			{
				tmpType = tmpType + "//" + kGpathRelation.getType();
				tmpkGpathRelation.setType(tmpType);
				//System.out.println("该entry关系有两个以上，而且他们有不同的type");
			}
			
			////////////////////////////////////////不同的subtype///////////////////////////////////////////////////////////////////////
			if (tmpSubtypeInfo[0] == null) {
				//引用传递，不需要再装入hash表
				tmpSubtypeInfo[0]=kGpathRelation.getSubtypeInfo()[0];
				tmpSubtypeInfo[1]=kGpathRelation.getSubtypeInfo()[1];
			}
			if (tmpSubtypeInfo[0] != null && kGpathRelation.getSubtypeInfo()[0]!=null
					&& !tmpSubtypeInfo[0].contains(kGpathRelation.getSubtypeInfo()[0]))
			{
				//引用传递，不需要再装入hash表
				tmpSubtypeInfo[0]=tmpSubtypeInfo[0]+"//"+kGpathRelation.getSubtypeInfo()[0];
				tmpSubtypeInfo[1]=tmpSubtypeInfo[1]+"//"+kGpathRelation.getSubtypeInfo()[1];
			}
			//不同的pathName
			if (!tmpPathName.contains( kGpathRelation.getPathName() ) )
			{
				tmpPathName = tmpPathName + "//" + kGpathRelation.getPathName();
				tmpkGpathRelation.setPathName(tmpPathName);
			//	System.out.println("该entry关系有两个以上，而且他们有不同的pathName");
			}
			return;
		}
		hashKGpathRelations.put(tmpkgGentrySub.getEntryName(), kGpathRelation);
	}
	
	
	
	/**
	 * 因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的
	 * 去除冗余后，将subtype用//分割开
	 * @param lsKGrelations
	 * @return
	 */
	private static Hashtable<String, KGrelation> removeRep(ArrayList<KGrelation> lsKGrelations ) 
	{
		/////////////////////////////////////////因为有些relation中有这种情况：entry1、entry2完全一致，就是subtype不一致，这个就是将一致的entry1、entry2去冗余的////////////////////////////////////////////////////////////////
		Hashtable<String, KGrelation> hashPathID2KGRelation = new Hashtable<String, KGrelation>();
		for (int i = 0; i < lsKGrelations.size(); i++) 
		{
			KGrelation tmpKGrelation = lsKGrelations.get(i);
			//因为entryID1都是一样的，所以不考虑entryID1
			String key = tmpKGrelation.getPathName() + tmpKGrelation.getEntry2ID(); //pathName和entryID2两个在一起是独一无二的了，用这个连城string作为key，以此来去重复，否则的话要在KGrelation类中重写hash码
			if (hashPathID2KGRelation.containsKey(key)) 
			{
				//如果出现重复的entry1和entry2对， 那么将subtypeName和subtypeValue附加在后面
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
	 * 给定entryID和pathName，查找具体的entry<br>
	 * 因为一个entryID可能会对应多个keggID，那么也就会返回多个KGentry对象<br>
	 * 首先查找entry的ID，没找到的话，查找parentID<br>
	 * 如果是parentID，不需要将结果--也就是component 之间连起来，因为在输入（所有项）查找的时候 已经考虑了之间的联系，这些关系在将（所有）项目mapping回去的时候会被计算到
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
		//如果没有在entryID项目搜索到基因，则用parentID项搜索
		else 
		{
			qKGentry = new KGentry();
			qKGentry.setParentID(entryID); qKGentry.setPathName(pathName);
			ArrayList<KGentry> lsKGentries2=servKEntry.queryLsKGentries(qKGentry);
			return lsKGentries2;
		}
	}
	
	
	/**
	 * 给定pathName，查找该pathway信息以及与之有关系的PathwayName<br>
	 * @param pathName
	 * @return object[]
	 * 0: 本PathID对应的KGpathway
	 * 1：本pathID的trg对应的lsPathName
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
	 * 给定一组pathID，返回这些pathID对应的kGpathway
	 * 内部去空格
	 * Hashtable-String-KGpathway<br>
	 * key：pathID value：KGpathway
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
	 * 给定一系列的pathID，返回pathID的hash表
	 * pathID会自动去空格
	 * key：scr pathID value：ArrayList-KGpathway
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
	 * 给定一系列pathID,
	 * pathID自动去前后空格，以及每个pathID所对应的target，获得它与target的关系表以及属性表<br>
	 * 这个是将输入的pathID装入hashset，然后遍历pathID的target在里面查找，这样遍历少的查找多的，感觉能提高效率
	 * @param PathID
	 * @return Object[] 是一个数组
	 * 0：lsscr2trg string[]
	 * 0:srcPathName
	 * 1:srcPathTitle
	 * 2:trgpathName
	 * 3:trgPathTitle
	 * 1: attribute string[]
	 */
	private static Object[] getSrc2Trg(String[] PathID) {
		//将输入的PathID编成一个hashset，方便搜索
		HashSet<String> hashPathID = new HashSet<String>();
		
		for (String string : PathID) {
			hashPathID.add(string.trim());
		}
		//结果1，每个scr对应的trg信息
		ArrayList<String[]> lsscr2trg = new ArrayList<String[]>();
		//结果2，每个scr的attribute
		Hashtable<String,String[]> hashAttribute = new Hashtable<String,String[]>();
		
		//每个path所对应的trgPathID的hahs表
		Hashtable<String,ArrayList<String>> hashLsScr2HashTrg = getHashPathID(PathID);
		//每个pathway的具体信息
		Hashtable<String, KGpathway> hashPath2KGpathway = getHashKGpathway(PathID);
		
		ArrayList<String> lstmpTrgPathName = new ArrayList<String>();
		//用所有pathID去查找
		for (String scrPathName : PathID) {
			//如果这个pathID有target
			if ((lstmpTrgPathName = hashLsScr2HashTrg.get(scrPathName.trim())) != null) {
				//用这些target在pathID中找
				for (String trgPathName : lstmpTrgPathName) {
					//如果找到了说明出现了一组src2trg
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
//	 * 输入基因，返回该基因的KeggID
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
//	 * 给定geneID，返回该基因的KO,目前只支持NCBIID中含有的基因
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







