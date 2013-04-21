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
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;

public class Scr2Target {
	
	

	/**
	 * 
	 * @param pathName 指定输出某个pathway的关系,是kegg的pathwayID，类似 "path:hsa04010"，为""时输出全部
	 * @param accID 输入的accID
	 * @param ResultFIleScr2Target
	 * @param QtaxID  如果是symbol需要指定，否则指定为0
	 * @param blast 是否进行blast
	 * @param subTaxID 如果进行blast，目的物种是什么
	 * @param evalue evalue 阈值是多少
	 * @throws Exception
	 */
	public static void getGene2RelateKo(String pathName, List<String> accID,String ResultFIleScr2Target, String resultFIleAttribute,int QtaxID,boolean blast,int subTaxID,double evalue) throws Exception {
		GeneID geneID = null;
		ServKIDKeg2Ko servKIDKeg2Ko = new ServKIDKeg2Ko();
		ServKEntry servKEntry = new ServKEntry();
		//丛数据库获得taxID
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
		 * 保存关系的一个list，object[2]
		 * 0：qGenKegInfo[7]<br>
		<b>0: queryID</b><br>
	1: geneID<br>
	2: UniProtID<br>
	3: KeggID<br>
	如果上面没有keggID时，后面blast就可能有信息<br>
	4. blast evalue<br>
	5: subTax 目标物种<br>
	6: subGeneID 目标物种的geneID<br>
	7: subKO 目标物种的KO，注意不是keggID，KO可直接用于比对到本物种上去,如果有多个KO，则用"//"隔开<br>
	<b>1. Hashtable- String - KGpathRelation </b>
	string:targe的KeggID/KO
	KGpathRelation ： 具体信息 
		 */
		ArrayList<Object[]> lsRelationInfo = new ArrayList<Object[]>();
		ArrayList<String[]> lsAccID = QKegPath.getGeneID(accID, QtaxID);
		//一个一个的accID去查找
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
				//ko里面都是人类同源基因的KO，那么现在要把这些KOmapping回本物种，如果本物中的该KO已经有了对应的KeggID，则将此KO换成本KeggID，如果没有，则跳过
				for (int j = 0; j < ko.length; j++)
				{
					////////////////如果geneBlast到了人类，并且得到了相应的KO，那么尝试获得该KO所对应本物种的KeggID，并用KeggID直接mapping回本基因。如果没有KeggID，则用KO去mapping////////////////////////////////////////////////////////////////
					KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
					kgiDkeg2Ko.setKo(ko[j]); kgiDkeg2Ko.setTaxID(QtaxID);
					ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos2 = servKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
					if (lsKgiDkeg2Kos2 != null && lsKgiDkeg2Kos2.size()>0) 
					{
						//虽然一个ko对应多个keggID，但是对于pathway来说，一个ko就对应到一个pathway上，所以一个ko就够了
						String keggID = lsKgiDkeg2Kos2.get(0).getKeggID();//这就是本物中的KeggID，用这个KeggID直接可以搜索相应的pathway
						ko[j] = keggID;
					}
				}
			}
			//一个基因对应多个ko，那么一个ko可能就有一个entry，遍历一个基因对应的所有ko来
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
				
						///////////////各种测试//////////////////测试看在relation中是不是存在compound
						if (key.contains("ko")) {
							System.out.println(key);
						}
						if (tmpkGpathRelation.getSKGentry().getType().equals("compound")) {
							System.out.println(tmpkGpathRelation.getSKGentry().getEntryName());
						}
						///////////////////////////////////////////
						//虽然多个ko，每一个ko都有对应多个entry，但是实际上这多个ko都是一个gene的，也就是说应该将这多个ko对应的信息合并到一个基因上去
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
	 * @param pathName 指定输出某个pathway的关系,是kegg的pathwayID，类似 "path:hsa04010"，为""时输出全部
	 * @param accID 输入的accID
	 * @param ResultFIleScr2Target
	 * @param QtaxID  如果是symbol需要指定，否则指定为0
	 * @param blast 是否进行blast
	 * @param subTaxID 如果进行blast，目的物种是什么
	 * @param evalue evalue 阈值是多少
	 * @throws Exception
	 */
	public static void getGene2RelateKo2(String pathName, ArrayList<String> lsKeggID,String ResultFIleScr2Target, String resultFIleAttribute,int QtaxID) throws Exception
	{
		ServKEntry servKEntry = new ServKEntry();
		/**
		 * 保存关系的一个list，object[2]
		 * 0：qGenKegInfo[7]<br>
		<b>0: queryID</b><br>
	1: geneID<br>
	2: UniProtID<br>
	3: KeggID<br>
	如果上面没有keggID时，后面blast就可能有信息<br>
	4. blast evalue<br>
	5: subTax 目标物种<br>
	6: subGeneID 目标物种的geneID<br>
	7: subKO 目标物种的KO，注意不是keggID，KO可直接用于比对到本物种上去,如果有多个KO，则用"//"隔开<br>
	<b>1. Hashtable- String - KGpathRelation </b>
	string:targe的KeggID/KO
	KGpathRelation ： 具体信息 
		 */
		ArrayList<Object[]> lsRelationInfo = new ArrayList<Object[]>();
		//一个一个的accID去查找
		for (int i = 0; i < lsKeggID.size(); i++) 
		{
			Hashtable<String, KGpathScr2Trg> hashEntryRelation = new Hashtable<String, KGpathScr2Trg>();
			String[] ko =  new String[1];
			ko[0] = lsKeggID.get(i);
			String[] qGenKegInfo = new String[4];
			qGenKegInfo[0] = ko[0]; qGenKegInfo[1] = ko[0];  qGenKegInfo[2] = ko[0]; qGenKegInfo[3] = ko[0]; 
			//一个基因对应多个ko，那么一个ko可能就有一个entry，遍历一个基因对应的所有ko来
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
				
						///////////////各种测试//////////////////测试看在relation中是不是存在compound
						if (key.contains("ko")) {
							System.out.println(key);
						}
						if (tmpkGpathRelation.getSKGentry().getType().equals("compound")) {
							System.out.println(tmpkGpathRelation.getSKGentry().getEntryName());
						}
						///////////////////////////////////////////
						//虽然多个ko，每一个ko都有对应多个entry，但是实际上这多个ko都是一个gene的，也就是说应该将这多个ko对应的信息合并到一个基因上去
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
	 * 给定pathway，和一个geneID信息以及其所对应的targetGeneID信息，返回pathway的sourceTarget表
	 * @param pathName
	 * @param lsRelationInfo
	 * @param QtaxID
	 * @param ResultFIleScr2Target
	 * @throws Exception
	 */
	private static void getRelation2(String pathName, ArrayList<Object[]> lsRelationInfo ,int QtaxID,String ResultFIleScr2Target, String resultFIleAttribute) throws Exception 
	{
		//source 2 target 的表格
		//string[3] 0:source 1:target 2:relation
		ArrayList<String[]> lsScr2Target = new ArrayList<String[]>();
		
		//将待搜索的Entry单独放出来
		ArrayList<String[]> lsRelationEntry = new ArrayList<String[]>();
		for (int i = 0; i < lsRelationInfo.size(); i++) {
			lsRelationEntry.add((String[]) lsRelationInfo.get(i)[0]);
		}
		
		Hashtable<String,String[]> hashEntryInfo = new Hashtable<String, String[]>();
		//装载最后的结果
		Hashtable<String,String[]> hashEntryInfoResult = new Hashtable<String, String[]>();
		/////////////每个entry都标注上具体信息，然后装入hash表，方便后面查询//////////////////////////////////////////////////
		for (int j = 0; j < lsRelationEntry.size(); j++)
		{
			/**
			 * 保存关系的一个list，object[2]
			 * 0：qGenKegInfo[7]<br>
			<b>0: queryID</b><br>
		1: geneID<br>
		2: UniProtID<br>
		3: KeggID<br>
		如果上面没有keggID时，后面blast就可能有信息<br>
		4. blast evalue<br>
		5: subTax 目标物种<br>
		6: subGeneID 目标物种的geneID<br>
		7: subKO 目标物种的KO，注意有些KO已经转变为本物种的keggID，KO可直接用于比对到本物种上去,如果有多个KO，则用"//"隔开<br>
		<b>1. Hashtable- String - KGpathRelation </b>
		string:targe的KeggID/KO
		KGpathRelation ： 具体信息 
			 */
			String[] qGenKegInfo = lsRelationEntry.get(j);
			//先获得该基因的信息 string[2] : 0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
			String[] queryGenInfo = new String[8];
			for (int k = 0; k < queryGenInfo.length; k++) {//赋初值""
				queryGenInfo[k] = "";
			}
			//没有同源ko,上面已经把这种情况跳过了
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
		//开始盘查tmpHashEntryRelation的中间变量
		KGpathScr2Trg tmpkGpathRelation = null;
		//正式开始比较，将每一个entry都当作source
		for (int i = 0; i < lsRelationEntry.size(); i++) 
		{
			String[] qGenKegInfo = lsRelationEntry.get(i);
			Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation = (Hashtable<String, KGpathScr2Trg>) lsRelationInfo.get(i)[1];
			//对于每一个潜在source，用所有source搜索其target
			//因为把所有的基因都放到source里去了，那么只要把source搜索他们的target就能搜到相应的一对关系。
			for (int j = 0; j < lsRelationEntry.size(); j++)
			{
				String[] targetGenKegInfo = lsRelationEntry.get(j);
				//首先获得一个RelationEntry里面的所有ko，其实这么多ko都是对到同一个基因上的
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
					//找到了一对关系
					if (( tmpkGpathRelation =tmpHashEntryRelation.get(ko[k])) != null) 
					{
						 String[] tmpScr2Target = new String[5];
						 tmpScr2Target[0] = hashEntryInfo.get(qGenKegInfo[0])[0];
						 tmpScr2Target[1] = hashEntryInfo.get(targetGenKegInfo[0])[0];
						 tmpScr2Target[2] = tmpkGpathRelation.getType();
						 tmpScr2Target[3] = tmpkGpathRelation.getSubtypeInfo()[0];
						 tmpScr2Target[4] = tmpkGpathRelation.getPathName();
						 //////////////////陈岱要求的mapk////////////////////////////////////////////////////////////////////
						if (!tmpScr2Target[4].contains(pathName)) {
							continue;
						}
						 ///////////////////////////////////////////////////////////////////////////////////////////
						 //装载关系
						 lsScr2Target.add(tmpScr2Target);
						 //将entry的属性装入hash表
						 String[] ScrEntryAttribute = hashEntryInfo.get(qGenKegInfo[0]);
						 //装入pathway
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
						 //装入pathway
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
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(ResultFIleScr2Target, true);
		txtReadandWrite.ExcelWrite(lsScr2Target);
		
		
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
		
		TxtReadandWrite txtReadandWrite2 = new TxtReadandWrite(resultFIleAttribute, true);
		txtReadandWrite2.ExcelWrite(lsAttribute);
		txtReadandWrite.close();
		txtReadandWrite2.close();
	}
	
	/**
	 * 给定pathway，和一个geneID信息以及其所对应的targetGeneID信息，返回pathway的sourceTarget表
	 * @param pathName
	 * @param lsRelationInfo
	 * @param QtaxID
	 * @param ResultFIleScr2Target
	 * @throws Exception
	 */
	private static void getRelation(String pathName, ArrayList<Object[]> lsRelationInfo ,int QtaxID,String ResultFIleScr2Target, String resultFIleAttribute) throws Exception 
	{
		//source 2 target 的表格
		//string[3] 0:source 1:target 2:relation
		ArrayList<String[]> lsScr2Target = new ArrayList<String[]>();
		
		//将待搜索的Entry单独放出来
		ArrayList<String[]> lsRelationEntry = new ArrayList<String[]>();
		for (int i = 0; i < lsRelationInfo.size(); i++) {
			lsRelationEntry.add((String[]) lsRelationInfo.get(i)[0]);
		}
		
		Hashtable<String,String[]> hashEntryInfo = new Hashtable<String, String[]>();
		//装载最后的结果
		Hashtable<String,String[]> hashEntryInfoResult = new Hashtable<String, String[]>();
		/////////////每个entry都标注上具体信息，然后装入hash表，方便后面查询//////////////////////////////////////////////////
		for (int j = 0; j < lsRelationEntry.size(); j++)
		{
			/**
			 * 保存关系的一个list，object[2]
			 * 0：qGenKegInfo[7]<br>
			<b>0: queryID</b><br>
		1: geneID<br>
		2: UniProtID<br>
		3: KeggID<br>
		如果上面没有keggID时，后面blast就可能有信息<br>
		4. blast evalue<br>
		5: subTax 目标物种<br>
		6: subGeneID 目标物种的geneID<br>
		7: subKO 目标物种的KO，注意有些KO已经转变为本物种的keggID，KO可直接用于比对到本物种上去,如果有多个KO，则用"//"隔开<br>
		<b>1. Hashtable- String - KGpathRelation </b>
		string:targe的KeggID/KO
		KGpathRelation ： 具体信息 
			 */
			String[] qGenKegInfo = lsRelationEntry.get(j);
			//先获得该基因的信息 string[2] : 0: symbol/AccID    1: taxID    2: description   3:blast Evalue      4:subject TaxID   5:subject Symbol/accID    6:subject Description    7: pathWay
			String[] queryGenInfo = new String[8];
			for (int k = 0; k < queryGenInfo.length; k++) {//赋初值""
				queryGenInfo[k] = "";
			}
			//没有同源ko,上面已经把这种情况跳过了
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
				//如果geneID存在
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
				//如果没有symbol
				queryGenInfo[5] = copedID.getSymbol();
				queryGenInfo[6] = copedID.getDescription();
			}
			hashEntryInfo.put(qGenKegInfo[0], queryGenInfo);
		}
		
		
		

		//开始盘查tmpHashEntryRelation的中间变量
		KGpathScr2Trg tmpkGpathRelation = null;
		//正式开始比较，将每一个entry都当作source
		for (int i = 0; i < lsRelationEntry.size(); i++) 
		{
			String[] qGenKegInfo = lsRelationEntry.get(i);
			Hashtable<String, KGpathScr2Trg> tmpHashEntryRelation = (Hashtable<String, KGpathScr2Trg>) lsRelationInfo.get(i)[1];
			//对于每一个潜在source，用所有source搜索其target
			//因为把所有的基因都放到source里去了，那么只要把source搜索他们的target就能搜到相应的一对关系。
			for (int j = 0; j < lsRelationEntry.size(); j++)
			{
				String[] targetGenKegInfo = lsRelationEntry.get(j);
				//首先获得一个RelationEntry里面的所有ko，其实这么多ko都是对到同一个基因上的
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
					//找到了一对关系
					if (( tmpkGpathRelation =tmpHashEntryRelation.get(ko[k])) != null) 
					{
						 String[] tmpScr2Target = new String[5];
						 tmpScr2Target[0] = hashEntryInfo.get(qGenKegInfo[0])[0];
						 tmpScr2Target[1] = hashEntryInfo.get(targetGenKegInfo[0])[0];
						 tmpScr2Target[2] = tmpkGpathRelation.getType();
						 tmpScr2Target[3] = tmpkGpathRelation.getSubtypeInfo()[0];
						 tmpScr2Target[4] = tmpkGpathRelation.getPathName();
						 //////////////////陈岱要求的mapk////////////////////////////////////////////////////////////////////
						if (!tmpScr2Target[4].contains(pathName)) {
							continue;
						}
						 ///////////////////////////////////////////////////////////////////////////////////////////
						 //装载关系
						 lsScr2Target.add(tmpScr2Target);
						 //将entry的属性装入hash表
						 String[] ScrEntryAttribute = hashEntryInfo.get(qGenKegInfo[0]);
						 //装入pathway
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
						 //装入pathway
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
