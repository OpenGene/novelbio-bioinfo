package com.novelBio.annotation.GO.queryDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.poi.hdf.extractor.SEP;

import com.novelBio.annotation.genAnno.AnnoQuery;
import com.novelBio.annotation.genAnno.GOQuery;
import com.novelBio.annotation.pathway.kegg.prepare.KGprepare;
import com.novelBio.base.dataOperate.TxtReadandWrite;

import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSGo2Term;

import entity.friceDB.BlastInfo;
import entity.friceDB.Gene2Go;
import entity.friceDB.Gene2GoInfo;
import entity.friceDB.GeneInfo;
import entity.friceDB.Go2Term;
import entity.friceDB.NCBIID;
import entity.friceDB.Uni2GoInfo;
import entity.friceDB.UniGene2Go;
import entity.friceDB.UniProtID;

/**
 * 先用CopeID类对输入的geneID做一个整理，包括ID预处理以及合并等，得到的list结果进入该模块做GO的查询<br>
 * 输入的list为：
 * 	 * arrayList-string[3] :<br>
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
 * @author zong0jie
 *
 */
public class QgeneID2Go {

	/**
	 * 查找单个geneID的信息，暂时没用到
	 * @param ncbiid
	 * @return 返回两个arrayList,
	 * 输入的ncbiid<b>必须有geneID项目</b>。<br>
	 * 如果是<b>分开accID</b>的，那么输入的ncbiid必须<b>有accID项目</b>，那么<br>
	 * 第一个geneInfo：0：accID  1:geneSymbol/accID  2:description<br>
	 * 如果是<b>合并accID</b>，那么输入的ncbiid必须<b>没有accID项目</b>，那么<br>
	 * 第一个geneInfo：0：geneID  1:geneSymbol/accID  2:description<br>
	 * 如果没查到Go，那么就这个为止，后面都不会有了<br>
	 * 第二个goID：goID goID <br>
	 * 第三个evidence：对应每个goID的evidence 譬如IEA等<br>
	 * 后面可以继续加上其他信息
	 */
	public static ArrayList<String[]> getGenGoInfo(NCBIID ncbiid)
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] genInfo = new String[3];
		String[] geneAnoInfo = AnnoQuery.getGenInfo(ncbiid.getGeneId());
		if (ncbiid.getAccID() == null || ncbiid.getAccID().equals("")) {
			genInfo[0] = ncbiid.getGeneId()+"";
		}
		else {
			genInfo[0] = ncbiid.getAccID();
		}
		genInfo[1] = geneAnoInfo[0];genInfo[2] = geneAnoInfo[1];
		lsResult.add(genInfo);
		///////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<Gene2Go> lsGene2Gos = GOQuery.getGen2Go(ncbiid);
		
		if (lsGene2Gos != null && lsGene2Gos.size()>0) {
			String[] goID = new String[lsGene2Gos.size()];
			String[] goEvidence = new String[lsGene2Gos.size()];
			for (int i = 0; i<lsGene2Gos.size() ; i++) {
				goID[i] = lsGene2Gos.get(i).getGOID();
				goEvidence[i] = lsGene2Gos.get(i).getEvidence();
			}
			lsResult.add(goID);lsResult.add(goEvidence);
		}
		return lsResult;
	}
	
	/**
	 * 查找单个UniprotID的信息，暂时没用到
	 * @param ncbiid
	 * @return 返回两个arrayList,
	 * 输入的ncbiid<b>必须有geneID项目</b>。<br>
	 * 如果是<b>分开accID</b>的，那么输入的uniProtID必须<b>有accID项目</b>，那么<br>
	 * 第一个geneInfo：0：accID  1:geneSymbol/accID  2:description<br>
	 * 如果是<b>合并accID</b>，那么输入的ncbiid必须<b>没有accID项目</b>，那么<br>
	 * 第一个geneInfo：0：uniID  1:geneSymbol/accID  2:description<br>
	 * 如果没查到Go，那么就这个为止，后面都不会有了<br>
	 * 第二个goID：goID goID <br>
	 * 第三个evidence：对应每个goID的evidence 譬如IEA等<br>
	 * 后面可以继续加上其他信息
	 */
	public static ArrayList<String[]> getUniGenGoInfo(UniProtID uniProtID)
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] genInfo = new String[3];
		String[] geneAnoInfo = AnnoQuery.getUniGenInfo(uniProtID.getUniID());
		if (uniProtID.getAccID() == null || uniProtID.getAccID().equals("")) {
			genInfo[0] = uniProtID.getUniID();
		}
		else {
			genInfo[0] = uniProtID.getAccID();
		}
		genInfo[1] = geneAnoInfo[0];genInfo[2] = geneAnoInfo[1];
		lsResult.add(genInfo);
		///////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<UniGene2Go> lsuniGene2Gos = GOQuery.getUniGen2Go(uniProtID);
		
		if (lsuniGene2Gos != null && lsuniGene2Gos.size()>0) {
			String[] goID = new String[lsuniGene2Gos.size()];
			String[] goEvidence = new String[lsuniGene2Gos.size()];
			for (int i = 0; i<lsuniGene2Gos.size() ; i++) {
				goID[i] = lsuniGene2Gos.get(i).getGOID();
				goEvidence[i] = lsuniGene2Gos.get(i).getEvidence();
			}
			lsResult.add(goID);lsResult.add(goEvidence);
		}
		return lsResult;
	}

	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 */
	public static Gene2GoInfo getGen2GoInfo(String[] genInfo,int taxID)
	{
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(genInfo[1]);
			ncbiid.setGeneId(Long.parseLong(genInfo[2]));
			ncbiid.setTaxID(taxID);
			return DaoFCGene2GoInfo.queryGeneDetail(ncbiid);
	}
	
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 */
	public static Uni2GoInfo getUni2GenGoInfo(String[] genInfo,int taxID)
	{
		UniProtID uniProtID = new UniProtID();
		uniProtID.setAccID(genInfo[1]);
		uniProtID.setUniID(genInfo[2]);
		uniProtID.setTaxID(taxID);
		return DaoFCGene2GoInfo.queryUniDetail(uniProtID);
	}
	
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * 返回两个arrayList,
	 * 如果是<b>分开accID</b>的，那么<br>
	 * 第一个geneInfo：0：accID  1:geneSymbol/accID  2:description<br>
	 * 如果是<b>合并accID</b>，那么<br>
	 * 第一个geneInfo：0：uniID  1:geneSymbol/accID  2:description<br>
	 * 如果没查到Go，那么就这个为止，后面都不会有了<br>
	 * 第二个goID：goID goID <br>
	 * 第三个evidence：对应每个goID的evidence 譬如IEA等<br>
	 * 后面可以继续加上其他信息
	 */
	public static ArrayList<String[]> getGenGoInfo(String[] genInfo,int taxID,boolean sep) {
		ArrayList<String[]> lsGenGoInfo = null;
			if (genInfo[0].equals("geneID"))
			{
				NCBIID ncbiid = new NCBIID();
				if (sep) {
					ncbiid.setAccID(genInfo[1]); 
				}
				ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(taxID);
				lsGenGoInfo = getGenGoInfo(ncbiid);
			}
			else if (genInfo[0].equals("uniID")) 
			{
				UniProtID uniProtID = new UniProtID();
				if (sep) {
					uniProtID.setAccID(genInfo[1]); 
				}
				uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(taxID);
				lsGenGoInfo = getUniGenGoInfo(uniProtID);
			}
			else
			{
				if (!genInfo[0].equals("accID"))
				{
					System.out.println("error 输入ID出现了错误，在QgeneID2Go 的 getGenGoInfo方法");
				}
			}
		return lsGenGoInfo;
	}
	
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * 返回blast的信息
	 */
	public static BlastInfo getBlastInfo(String[] genInfo,int QtaxID,int StaxID,double evalue)
	{
		BlastInfo blastInfo = null;
		if (genInfo[0].equals("geneID")) {
			NCBIID ncbiid = new NCBIID();
			 ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(QtaxID);
			 blastInfo = AnnoQuery.getBlastInfo(ncbiid, evalue, StaxID);
		}
		else if (genInfo[0].equals("uniID")) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(QtaxID);
			blastInfo = AnnoQuery.getBlastInfo(uniProtID, evalue, StaxID);
		}
		else
		{
			String accID = genInfo[1];
			blastInfo =AnnoQuery.getBlastInfo(accID, evalue, StaxID);
		}
		return blastInfo;
	}
	
	/**
	 *  先用CopeID类对输入的geneID做一个整理，包括ID预处理以及合并等，得到的list结果进入该方法做GO的查询<br>
	 *  @param lsAccID 	 
	 *  * arraylist-string[3]<br>
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param QtaxID
	 * @param GOClass Go的类型 P: biological Process F:molecular Function C: cellular Component 如果GOClass为""那么就选择全部
	 * @param sepID 是否分开ID，true分开 false合并ID
	 * @param blast 是否blast
	 * @param evalue
	 * @param StaxID
	 * @return 
	 * <b>如果blast</b>，返回三个arrayList-string[]<br>
	 * 第一个：string[15]
	  * 0: queryID<br>
	  * 1: queryGeneID<br>
	  * 2: querySymbol<br>
	  * 3: Description<br>
	  * 4: GOID<br>
	  * 5: GOTerm<br>
	  * 6: GO可信度<br>
	  * 7: blastEvalue<br>
	  * 8: taxID<br>
	  * 9: subjectGeneID<br>
	  * 10: subjectSymbol<br>
	  * 11: Description<br>
	  * 12: GOID<br>
	  * 13: GOTerm<br>
	  * 14: GO可信度<br>
	 * 第二个：string[2]
	 * lsGene2Go 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: geneID<br>
	 * 1：GOID,GOID,GOID...<br>
	 * 第三个：string[6],也就是GO2Gene，这个只有在NBCfisher中才会用到<br>
	 * 0: GOID<br>
	 * 1: GOTerm<br>
	 * 2: GO可信度<br>
	 * 3: queryID<br>
	 * 4: queryGeneID<br>
	 * 5: querySymbol<br>
	 * 6: subjectSymbol<br>>
	 * <b>如果不blast</b>，返回两个arrayList-string[]<br>
	 * 第一个string[7]
	 * 0: queryID<br>
	 * 1: geneID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * 第二个：string[2]
	 * lsGene2Go 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: geneID<br>
	 * 1：GOID,GOID,GOID...<br>
	 * 
	 * 
	 */
	public static ArrayList<ArrayList<String[]>> getGenGoInfo(ArrayList<String[]> lsAccID, int QtaxID,String GOClass,boolean sepID,boolean blast,double evalue, int StaxID) 
	{
		/**
		 * 保存本基因和blast的信息
		 * 如果不blast
		 * 其中：0: queryID<br>
		 * 1: geneID<br>
		 * 2: symbol<br>
		 * 3: description<br>
		 * 4: GOID<br>
		 * 5: GOTerm<br>
		 * 6: Evidence<br>
		 * 如果blast
		 * 0: queryID<br>
		 * 1: queryGeneID<br>
		 * 2: querySymbol<br>
		 * 3: Description<br>
		 * 4: GOID<br>
		 * 5: GOTerm<br>
		 * 6: GO可信度<br>
		 * 7: blastEvalue<br>
		 * 8: taxID<br>
		 * 9: subjectGeneID<br>
		 * 10: subjectSymbol<br>
		 * 11: Description<br>
		 * 12: GOID<br>
		 * 13: GOTerm<br>
		 * 14: GO可信度<br>
		 */
		ArrayList<String[]> lsGene2GoInfo = new ArrayList<String[]>();
		/**
		 * 只有blast才会有这个项目
		 * 0: GOID
		 * 1: GOTerm
		 * 2: GO可信度
		 * 3: queryID
		 * 4: querySymbol
		 * 5: subjectSymbol
		 */
		ArrayList<String[]> lsGo2Gene = new ArrayList<String[]>();
		//装载0：sepID-true: accID sepID-false: geneID
		//1：GOID,GOID,GOID...
		ArrayList<String[]> lsGene2Go = new ArrayList<String[]>();
		///////////直接查找///////////////////////////////////
		for (String[] strings : lsAccID)
		{
			if (strings[2].equals("LOC_Os01g54230")) {
				System.out.println("test");
			}
			Gene2GoInfo Qgene2GoInfo =null;
			Uni2GoInfo Quni2GoInfo = null;
			
			if (strings[0].equals("geneID")) {
				Qgene2GoInfo = getGen2GoInfo(strings, QtaxID);
			}
			else if(strings[0].equals("uniID"))
			{
				Quni2GoInfo = getUni2GenGoInfo(strings, QtaxID);
			}
			else if(strings[0].equals("accID")) {
				Qgene2GoInfo = new Gene2GoInfo();
				Qgene2GoInfo.setQuaryID(strings[2]);
			}
			Gene2GoInfo QBlastGene2GoInfo =null;
			Uni2GoInfo QBlastUni2GoInfo = null;
			
			BlastInfo blastInfo = null;
			if (blast)
			{
				ArrayList<String[]> lsBlastGenGoInfo = null;
				///////////////搜索blast信息/////////////////////////////////
				blastInfo = getBlastInfo(strings, QtaxID, StaxID, evalue);
				////////////////搜到blast信息后，再回去找geneInfo/////////////////////////////////////////
				if (blastInfo != null && blastInfo.getEvalue()<=evalue)
				{
					String tab = blastInfo.getSubjectTab();
					String[] genInfo = new String[3];
					genInfo[2] = blastInfo.getSubjectID();
					if (tab.equals("NCBIID")) 
					{
						genInfo[1] = AnnoQuery.getGenName(Long.parseLong(genInfo[2]));
						genInfo[0] = "geneID";
						QBlastGene2GoInfo = getGen2GoInfo(genInfo, StaxID);
					}
					else 
					{
						genInfo[1] = AnnoQuery.getUniGenName(genInfo[2]);
						genInfo[0] = "uniID";
						QBlastUni2GoInfo = getUni2GenGoInfo(genInfo, StaxID);
					}
				}
			}

			if (blast) {
				ArrayList<String[]> tmpInfo = GOQuery.copeBlastInfo(Qgene2GoInfo, Quni2GoInfo, QBlastGene2GoInfo, QBlastUni2GoInfo, blastInfo, evalue, GOClass, sepID, lsGene2Go, true);
				if (tmpInfo != null) {
					lsGene2GoInfo.addAll(tmpInfo);
					lsGo2Gene.addAll(GOQuery.copeBlastInfoSimple(Qgene2GoInfo, Quni2GoInfo, QBlastGene2GoInfo, QBlastUni2GoInfo, blastInfo, evalue, GOClass, sepID, null, true));
				}
			}
			else
			{
				ArrayList<String[]> tmpInfo = null;
				if (strings[0].equals("geneID")) 
				{
					tmpInfo = GOQuery.getGene2GoInfo(Qgene2GoInfo, lsGene2Go, sepID, GOClass);
				}
				else if(strings[0].equals("uniID"))
				{
					tmpInfo = GOQuery.getUni2GoInfo(Quni2GoInfo, lsGene2Go, sepID, GOClass);
				}
				if (tmpInfo != null) 
					lsGene2GoInfo.addAll(tmpInfo);
			}
		}
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		if (blast) {
			lsResult.add(lsGene2GoInfo);
			lsResult.add(lsGene2Go);
			lsResult.add(lsGo2Gene);
		}
		else {
			lsResult.add(lsGene2GoInfo);
			lsResult.add(lsGene2Go);
		}
		////////////////////////////////////////////////////////////////////////////////////////
		return lsResult;
	}
	
}
