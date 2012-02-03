package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.domain.geneanno.Blast2GeneInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.Gene2GoInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Uni2GoInfo;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
@Deprecated
public class GOQuery {
 
	
	/**
	 * 
	 * @param accID 输入geneID
	 * @param taxID taxID
	 * @param GOClass GO的分类：<br>
	 * <b>P</b>:Biological process <br>
	 * <b>C</b>:Cellular component <br>
	 * <b>F</b>:Molecular function
	 * @param blast 是否blast
	 * @param evalue blast的阈值
	 * @param StaxID 目的物种ID
	 * @return
	 * <b>当blast为false时</b><br>
	 * ArrayList-String[7] <br>
其中：0: queryID<br>
1: uniID<br>
2: symbol<br>
3: description<br>
4: GOID<br>
5: GOTerm<br>
6: Evidence<br>
	 * <b>如果没找到，则返回null，不会返回一个空的list</b><br>
	 * 	 * <b>当blast为true时</b><br>
	 * ArrayList-String[15]
	 * 其中：<br>
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
	 * <b>如果没找到GO信息，则返回null，不会返回一个空的list</b>
	 */
	public static ArrayList<String[]> getLsGeneGo(String accID,int taxID,String GOClass,boolean blast, double evalue, int StaxID) {
		if (!blast) {
			return getGenGo(accID, taxID, null,GOClass);
		}
		else {
			ArrayList<String> lsAccID = ServAnno.getNCBIUni(accID, taxID);
			String[] thisaccID = new String[3];
			thisaccID[0] = lsAccID.get(0); thisaccID[1] = accID; thisaccID[2] = lsAccID.get(1);
			Blast2GeneInfo blast2GeneInfo = ServBlastInfo2.getBlastGen2Go(thisaccID, taxID, blast, StaxID, evalue);
			return copeBlastInfo(blast2GeneInfo, evalue, GOClass, false, null, true);
		}
	}
	
	/**
	 * 给定queryID，返回该基因的GO信息
	 * @param accID string[3]
	 * 0: ID类型："geneID"或"uniID"或"accID"
		1: accID
		2: 具体转换的ID
	 * @param taxID 物种ID，如果不是Symbol，可以为0
	 * @param lsGene2Go 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 * @return ArrayList-String[7]
其中：0: queryID<br>
1: uniID<br>
2: symbol<br>
3: description<br>
4: GOID<br>
5: GOTerm<br>
6: Evidence<br>
	 * 如果没找到，则返回null，不会返回一个空的list<br>
	 */
	public static  ArrayList<String[]> getGenGo(String accID, int taxID,ArrayList<String[]> lsGene2Go,String GOClass )
	{
		ArrayList<String> lsAccID = ServAnno.getNCBIUni(accID, taxID);
		String[] thisaccID = new String[3];
		thisaccID[0] = lsAccID.get(0); thisaccID[1] = accID; thisaccID[2] = lsAccID.get(1);
		if (thisaccID[0].equals("geneID")) {
			Gene2GoInfo gene2GoQueryInfo = ServGo.getGen2GoInfo(thisaccID, taxID);
			return copeGenUni2GoInfo(gene2GoQueryInfo, lsGene2Go,true,GOClass);
		}
		else if (thisaccID[0].equals("uniID")) {
			Uni2GoInfo uni2GoQueryInfo = ServGo.getUni2GenGoInfo(thisaccID, taxID);
			return copeGenUni2GoInfo(uni2GoQueryInfo, lsGene2Go,true,GOClass);
		}
		return null;
	}
	
	
	
	/**
	 * 给定Gene2GoInfo的列表，将其中信息整理为
	 * ArrayList-String[7]<br>
	 * 其中：0: queryID<br>
	 * 1: geneID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * 如果没找到，则返回null，不会返回一个空的list<br>
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: geneID <br>
	 * 1：GOID,GOID,GOID...<br>
	 * 那么将本次查询的结果保存进去<br>
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 * @param gene2GoInfo 由getGenGo产生
	 * @param lsGene2Go
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为 <br> 
	 * 0：sepID  true: accID  false:geneID <br> 
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null<br>
	 * <b>注意输入前要对Gene2GoInfo中的geneID去重复<b/>
	 * @param sep 是否分割ID，对应在lsGene2Go会有相应的变化
	 * @param GOClass P，F和C，如果为""那么三个都要
	 */
	public static ArrayList<String[]> copeGenUni2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go,boolean sep,String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//如果没有找到基因 或者 该基因没有Go的信息，则跳过
		if (gene2GoInfo == null || gene2GoInfo.getLsGOInfo() == null || gene2GoInfo.getLsGOInfo().size()==0) 
			return null;
		ArrayList<Gene2Go> lsGoInfos = gene2GoInfo.getLsGOInfo();
		
		//设置GeneID2Go的基因列
		String[] strGene2Go=new String[2];
		if (sep) 
			strGene2Go[0]=gene2GoInfo.getQuaryID();
		else 
			strGene2Go[0]=gene2GoInfo.getGeneId()+"";
		
		strGene2Go[1]=""; int NumGO = 0;//这个专门用来计数得到了几个GOID
		for (int j = 0; j < lsGoInfos.size(); j++) 
		{
			String[] tmpGeneGoInfo = ServGo.getHashGo2Term().get(lsGoInfos.get(j).getGOID());

			String[] gene2Go=new String[7];
			for (int k = 0; k < gene2Go.length; k++) {
				gene2Go[k]="";//赋初值，都为空值
			}
			gene2Go[0] = gene2GoInfo.getQuaryID();
			gene2Go[1] = gene2GoInfo.getGeneId() + "";
			
			GeneInfo tmpGeneInfo = gene2GoInfo.getGeneInfo();
			
			//annotation
			if(tmpGeneInfo != null && tmpGeneInfo.getSymb() != null)
			{
				gene2Go[2] = tmpGeneInfo.getSymb().split("//")[0];
				gene2Go[3] = tmpGeneInfo.getDescrp();
			}
			/////////////////////////////////如果没有symbol的话，就随便找一个accID放上去//////////////////////////////////////////////////
			else 
				gene2Go[2] = ServAnno.getUniGenName(gene2Go[1]);
			
			if (tmpGeneGoInfo != null && !tmpGeneGoInfo[1].trim().equals("") && (tmpGeneGoInfo[3].equals(GOClass) || GOClass.equals(""))) {
				
				gene2Go[4] = tmpGeneGoInfo[1];
				gene2Go[5] = tmpGeneGoInfo[2];
				gene2Go[6] = lsGoInfos.get(j).getEvidence();
				lsGoResult.add(gene2Go);
				//装入lsUniGene2Go
				if (NumGO == 0) 
					strGene2Go[1] = tmpGeneGoInfo[1];
				else
					strGene2Go[1] = strGene2Go[1]+","+ tmpGeneGoInfo[1];
				NumGO ++;
			}
		}
		
		if (lsGene2Go != null && NumGO > 0) 
			lsGene2Go.add(strGene2Go);
		if (lsGoResult.size() == 0) {
			return null;
		}
		return lsGoResult;
	}
	
	/**
	 * 给定Uni2GoInfo的列表，将其中信息整理为<br>
	 * ArrayList-String[7]<br>
	 * 其中：0: queryID<br>
	 * 1: uniID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * 如果没找到，则返回null，不会返回一个空的list<br>
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: uniID <br>
	 * 1：GOID,GOID,GOID...<br>
	 * 那么将本次查询的结果保存进去<br>
	 *  如果Gene2GoInfo中没有GO信息，那么返回null<br>
	 * @param Uni2GoInfos 由getGenGo产生
	 * @param lsUniGene2Go 用于GO分析的一个东西。输入一个 lsUniGene2Go-Stirng[]，其中String[]的结构为
	 * 0：sepID  true: accID  false:uniID <br> 
	 * 1：GOID,GOID,GOID...<br> 
	 * 那么将本次查询的结果保存进去<br> 
	 * <b>注意输入前要对uni2GoInfo中的uniID去重复<b/>
	 */
	public static ArrayList<String[]>  copeGenUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go,boolean sep,String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//如果没有找到基因 或者 该基因没有Go的信息，则跳过
		if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
			return null;
		ArrayList<UniGene2Go> lsUniGoInfos = uni2GoInfo.getLsUniGOInfo();
		
		//设置GeneID2Go的基因列
		String[] strGene2Go=new String[2];
		if (sep) 
			strGene2Go[0]=uni2GoInfo.getQuaryID();
		else 
			strGene2Go[0]=uni2GoInfo.getUniID();
		
		strGene2Go[1]=""; int NumGO = 0;//这个专门用来计数得到了几个GOID
		for (int j = 0; j < lsUniGoInfos.size(); j++) 
		{
			String[] tmpGeneGoInfo = ServGo.getHashGo2Term().get(lsUniGoInfos.get(j).getGOID());
			


			String[] uni2Go=new String[7];
			for (int k = 0; k < uni2Go.length; k++) {
				uni2Go[k]="";//赋初值，都为空值
			}
			uni2Go[0] = uni2GoInfo.getQuaryID();
			uni2Go[1] = uni2GoInfo.getUniID();
			
			UniGeneInfo tmpUniGeneInfo = uni2GoInfo.getUniGeneInfo();
			
			//annotation
			if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymb() != null)
			{
				uni2Go[2] = tmpUniGeneInfo.getSymb().split("//")[0];
				uni2Go[3] = tmpUniGeneInfo.getDescrp();
			}
			/////////////////////////////////如果没有symbol的话，就随便找一个accID放上去//////////////////////////////////////////////////
			else 
				uni2Go[2] = ServAnno.getUniGenName(uni2Go[1]);
			
			if  (tmpGeneGoInfo != null && !tmpGeneGoInfo[1].trim().equals("") && (tmpGeneGoInfo[3].equals(GOClass) || GOClass.equals(""))) {
				uni2Go[4] = tmpGeneGoInfo[1];
				uni2Go[5] = tmpGeneGoInfo[2];
				uni2Go[6] = lsUniGoInfos.get(j).getEvidence();
				lsGoResult.add(uni2Go);
				//装入lsUniGene2Go
				if (NumGO == 0) 
					strGene2Go[1] = tmpGeneGoInfo[1];
				else
					strGene2Go[1] = strGene2Go[1]+","+ tmpGeneGoInfo[1];
				NumGO ++;
			}
		}
 
		if (lsUniGene2Go != null && NumGO > 0) 
			lsUniGene2Go.add(strGene2Go);
		
		if (lsGoResult.size() == 0) {
			return null;
		}
		return lsGoResult;
	}
	
	/**
	 * 给定由getGenGoBlast方法产生的Blast2GeneInfos类，按照evalue的阈值，将其中信息整理为
	 * ArrayList-String[7]
	 * 其中：<br>
	 * 0: GOID<br>
	 * 1: GOTerm<br>
	 * 2: GO可信度<br>
	 * 3: queryID<br>
	 * 4: queryGeneID<br>
	 * 5: querySymbol<br>
	 * 6: subjectSymbol<br>
	 * 此外，输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * @param Blast2GeneInfo 由getGenGoBlast方法产生的Blast2GeneInfos类
	 * @param evalue 相似度的阈值
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	public static ArrayList<String[]> copeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		Gene2GoInfo Qgene2GoInfo = blast2GeneInfo.getQueryGene2GoInfo();
		Uni2GoInfo Quni2GoInfo = blast2GeneInfo.getQueryUniGene2GoInfo();
		Gene2GoInfo Sgene2GoInfo = blast2GeneInfo.getSubjectGene2GoInfo();
		Uni2GoInfo Suni2GoInfo = blast2GeneInfo.getSubjectUni2GoInfo();
		BlastInfo blastInfo = blast2GeneInfo.getBlastInfo();
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		//来记录这四个组分里面哪些有哪些没有，第一列，每组是否有值1：有 0：null
		//第二列，每组内go的数量有多少
		//四行依次为Qgene2GoInfo，Quni2GoInfo，Sgene2GoInfo，Suni2GoInfo
		int[][] flag=new int[4][2];
		/////初始化都设为0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////检查这四个组的情况，并且记录下来，这时候还要考虑evalue///////////////////////////////////////////////////////////////////////////////////////
		if (Qgene2GoInfo!=null) {
			flag[0][0]=1;
			if (Qgene2GoInfo.getLsGOInfo()!=null)
				flag[0][1]=Qgene2GoInfo.getLsGOInfo().size();
			else 
				flag[0][1]=0;
		}
		else if (Quni2GoInfo!=null) {
			flag[1][0]=1;
			if (Quni2GoInfo.getLsUniGOInfo()!=null)
				flag[1][1]=Quni2GoInfo.getLsUniGOInfo().size();
			else 
				flag[1][1]=0;
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) {
			flag[2][0]=1;
			if (Sgene2GoInfo.getLsGOInfo()!=null)
				flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
			else 
				flag[2][1]=0;
		}
		else if (Suni2GoInfo!=null&&blastInfo.getEvalue()<=evalue) {
			flag[3][0]=1;
			if (Suni2GoInfo.getLsUniGOInfo()!=null)
				flag[3][1]=Suni2GoInfo.getLsUniGOInfo().size();
			else 
				flag[3][1]=0;
		}
		///////////四组里面，GO最多的是多少项////////////////////////////////////////////////////
		int max=flag[0][1];
		for (int j = 0; j < flag.length; j++) {
			
			if (max<flag[j][1]) 
				max=flag[j][1];
		}
		/////////////////////////////////////////////////////////
		String accID = "";
		String geneID = "";
		String symbol = "";
		String subSymbol = "";
		/////////////////////////////////////////////////////////
/////////////将accID，symbol，subSymbol填充好///////////////////////////////////////////////////////////
		if (Qgene2GoInfo!=null) 
		{
			accID = Qgene2GoInfo.getQuaryID();
			if (Qgene2GoInfo.getGeneId() == 0) {
				geneID = accID;
			}
			else {
				geneID = Qgene2GoInfo.getGeneId()+"";
			}
			GeneInfo tmpGeneInfo=Qgene2GoInfo.getGeneInfo();
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
				symbol = tmpGeneInfo.getSymb().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				symbol = ServAnno.getGenName(Qgene2GoInfo.getGeneId());
			}
		}
		else if(Quni2GoInfo!=null)
		{
			accID = Quni2GoInfo.getQuaryID();
			geneID = Quni2GoInfo.getUniID();
			UniGeneInfo tmpUniGeneInfo=Quni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymb()!=null)
				symbol = tmpUniGeneInfo.getSymb().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				symbol = ServAnno.getUniGenName(Quni2GoInfo.getUniID());
			}
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) 
		 {
			GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
				subSymbol = tmpGeneInfo.getSymb().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				subSymbol = ServAnno.getGenName(Sgene2GoInfo.getGeneId());
			}
		 }
		else if (Suni2GoInfo!=null && blastInfo.getEvalue()<=evalue)
		{
			UniGeneInfo tmpUniGeneInfo=Suni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymb()!=null)
				subSymbol = tmpUniGeneInfo.getSymb().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				subSymbol = ServAnno.getUniGenName(Suni2GoInfo.getUniID());
			}
		}
		//////////////////////////////////////////////////////////////////////
		//如果就没有GO，那很可能该基因就没对上去。
		if (max==0) 
		{
			if (getNoGo) 
			{
				return null;
			}
			/**
			 * 最后记录的信息就在这里面了
			 */
			String[] tmpBlastInfo=new String[7];
			
			
			//赋初值
			for (int j = 0; j < tmpBlastInfo.length; j++) {
				tmpBlastInfo[j]="";
			}
			tmpBlastInfo[3] = accID;
			tmpBlastInfo[4] = geneID;
			tmpBlastInfo[5] = symbol;
			tmpBlastInfo[6] = subSymbol;
			/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
			lsGoResult.add(tmpBlastInfo);
		}
		else {
			String[] gene2Go = new String[2]; gene2Go[0] = ""; gene2Go[1] = "";
			if (SepID) {
				if (Qgene2GoInfo!=null) 
					gene2Go[0]=Qgene2GoInfo.getQuaryID();
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getQuaryID();
			}
			else {
				if (Qgene2GoInfo!=null )
				{
					if (Qgene2GoInfo.getGeneId() == 0) //很有可能只是新建的Qgene2GoInfo，没有geneID项目
						gene2Go[0]=Qgene2GoInfo.getQuaryID();
					else 
						gene2Go[0]=Qgene2GoInfo.getGeneId()+"";
				}
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getUniID();
			}
			
			Set<String> hashGene2Go=new HashSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
			for (int j = 0; j < max; j++) {
				if (Qgene2GoInfo!=null) 
				{
					//因为现在用max，也就是所有go的最大的一行为j，所以这里要判断某个具体的go是否大于j。否则会出错
					if (Qgene2GoInfo.getLsGOInfo()!=null&&Qgene2GoInfo.getLsGOInfo().size()>j) 
					{
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//赋初值
							for (int k = 0; k < tmpBlastInfo.length; k++) {
								tmpBlastInfo[k]="";
							}
							tmpBlastInfo[3] = accID;
							tmpBlastInfo[4] = geneID;
							tmpBlastInfo[5] = symbol;
							tmpBlastInfo[6] = subSymbol;
							tmpBlastInfo[0] = GoInfo[1];
							tmpBlastInfo[1] = GoInfo[2];
							tmpBlastInfo[2] = Qgene2GoInfo.getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[0].trim().equals("")) {
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(GoInfo[1]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
				else if(Quni2GoInfo!=null)
				{
					if (Quni2GoInfo.getLsUniGOInfo()!=null&&Quni2GoInfo.getLsUniGOInfo().size()>j) 
					{
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//赋初值
							for (int k = 0; k < tmpBlastInfo.length; k++) {
								tmpBlastInfo[k]="";
							}
							tmpBlastInfo[3] = accID;
							tmpBlastInfo[4] = geneID;
							tmpBlastInfo[5] = symbol;
							tmpBlastInfo[6] = subSymbol;
							tmpBlastInfo[0] = GoInfo[1];
							tmpBlastInfo[1] = GoInfo[2];;
							tmpBlastInfo[2]=Quni2GoInfo.getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[0].trim().equals("")) {
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
				///////////////////////////////////////////////////
				if (Sgene2GoInfo!=null && blastInfo.getEvalue()<=evalue) 
				 {
					if (Sgene2GoInfo.getLsGOInfo()!=null&&Sgene2GoInfo.getLsGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//赋初值
							for (int k = 0; k < tmpBlastInfo.length; k++) {
								tmpBlastInfo[k]="";
							}
							tmpBlastInfo[3] = accID;
							tmpBlastInfo[4] = geneID;
							tmpBlastInfo[5] = symbol;
							tmpBlastInfo[6] = subSymbol;
							tmpBlastInfo[0] = GoInfo[1];
							tmpBlastInfo[1] = GoInfo[2];
							tmpBlastInfo[2]=Sgene2GoInfo.getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[0].trim().equals("")) {
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
				else if (Suni2GoInfo!=null && blastInfo.getEvalue()<=evalue)
				{
					if (Suni2GoInfo.getLsUniGOInfo()!=null&&Suni2GoInfo.getLsUniGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//赋初值
							for (int k = 0; k < tmpBlastInfo.length; k++) {
								tmpBlastInfo[k]="";
							}
							tmpBlastInfo[3] = accID;
							tmpBlastInfo[4] = geneID;
							tmpBlastInfo[5] = symbol;
							tmpBlastInfo[6] = subSymbol;
							tmpBlastInfo[0] = GoInfo[1];
							tmpBlastInfo[1] = GoInfo[2];
							tmpBlastInfo[2]=Suni2GoInfo.getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[0].trim().equals("")) {
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
			}
			if (lsGene2Go !=null) {
				//将treeset中的不重复的geneID装入结果文件
				for (String s : hashGene2Go) {
					if (gene2Go[1].trim().equals("")) 
						gene2Go[1]=s;
					else
						gene2Go[1]=gene2Go[1]+","+s;
				}
				lsGene2Go.add(gene2Go);
			}
		}
		return lsGoResult;
	}
	

	/**
	 * 
	 * @param Qgene2GoInfo  本项目必须不能为null，如果输入时本项目为null，就是说没有找到对应的geneID，那么本项目将QueryID设置为accID
	 * @param Quni2GoInfo
	 * @param Sgene2GoInfo
	 * @param Suni2GoInfo
	 * @param blastInfo 
	 * @param evalue
	 * @param GOClass Go的类型 P: biological Process F:molecular Function C: cellular Component 如果GOClass为""那么就选择全部
	 * @param SepID 是否将ID分开，如果合并ID，也就是本项为False的话，lsGene2Go的第0项就为geneID，否则为accID
	 * @param lsGene2Go
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: geneID <br>
	 * 1：GOID,GOID,GOID...<br>
	 * 那么将本次查询的结果保存进去
	 * @param getNoGo ：有些ID没有Go，是否将这个ID保留下来 true:不保留
	 * @return
	 * 按照evalue的阈值，将其中信息整理为
	 * ArrayList-String[15]
	 * 其中：<br>
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
	 * <b>如果没找到GO信息，则返回null，不会返回一个空的list</b>
	 */
	public static ArrayList<String[]> copeBlastInfo(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,
			boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		Gene2GoInfo Qgene2GoInfo = blast2GeneInfo.getQueryGene2GoInfo();
		Uni2GoInfo Quni2GoInfo = blast2GeneInfo.getQueryUniGene2GoInfo();
		Gene2GoInfo Sgene2GoInfo = blast2GeneInfo.getSubjectGene2GoInfo();
		Uni2GoInfo Suni2GoInfo = blast2GeneInfo.getSubjectUni2GoInfo();
		BlastInfo blastInfo = blast2GeneInfo.getBlastInfo();
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		//来记录这四个组分里面哪些有哪些没有，第一列，每组是否有值1：有 0：null
		//第二列，每组内go的数量有多少
		//四行依次为Qgene2GoInfo，Quni2GoInfo，Sgene2GoInfo，Suni2GoInfo
		int[][] flag=new int[4][2];
		/////初始化都设为0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////检查这四个组的情况，并且记录下来，这时候还要考虑evalue///////////////////////////////////////////////////////////////////////////////////////
		if (Qgene2GoInfo!=null) {
			flag[0][0]=1;
			if (Qgene2GoInfo.getLsGOInfo()!=null)
				flag[0][1]=Qgene2GoInfo.getLsGOInfo().size();
			else 
				flag[0][1]=0;
		}
		else if (Quni2GoInfo!=null) {
			flag[1][0]=1;
			if (Quni2GoInfo.getLsUniGOInfo()!=null)
				flag[1][1]=Quni2GoInfo.getLsUniGOInfo().size();
			else 
				flag[1][1]=0;
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) {
			flag[2][0]=1;
			if (Sgene2GoInfo.getLsGOInfo()!=null)
				flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
			else 
				flag[2][1]=0;
		}
		else if (Suni2GoInfo!=null&&blastInfo.getEvalue()<=evalue) {
			flag[3][0]=1;
			if (Suni2GoInfo.getLsUniGOInfo()!=null)
				flag[3][1]=Suni2GoInfo.getLsUniGOInfo().size();
			else 
				flag[3][1]=0;
		}
		///////////四组里面，GO最多的是多少项////////////////////////////////////////////////////
		int max=flag[0][1];
		for (int j = 0; j < flag.length; j++) {
			
			if (max<flag[j][1]) 
				max=flag[j][1];
		}
		//如果就没有GO，那很可能该基因就没对上去。
		if (max==0) 
		{
			if (getNoGo) {
				return null;
			}
			/**
			 * 最后记录的信息就在这里面了
			 */
			String[] tmpBlastInfo=new String[15];
			//赋初值
			for (int j = 0; j < tmpBlastInfo.length; j++) {
				tmpBlastInfo[j]="";
			}
			if (Qgene2GoInfo != null) 
			{
				tmpBlastInfo[0] = Qgene2GoInfo.getQuaryID();
				if (Qgene2GoInfo.getGeneId() != 0) {
					tmpBlastInfo[1] = Qgene2GoInfo.getGeneId()+"";
				}
				
				GeneInfo tmpGeneInfo = Qgene2GoInfo.getGeneInfo();
				if(tmpGeneInfo != null && tmpGeneInfo.getSymb() != null)
				{
					tmpBlastInfo[2] = tmpGeneInfo.getSymb().split("//")[0];
					tmpBlastInfo[3] = tmpGeneInfo.getDescrp();
				}
				//如果没有symbol的话，就随便找一个accID放上去
				else {
					tmpBlastInfo[2] = ServAnno.getGenName(Qgene2GoInfo.getGeneId());
					}
			}
			else if(Quni2GoInfo!=null)
			{
				tmpBlastInfo[0] = Quni2GoInfo.getQuaryID();
				tmpBlastInfo[1] = Quni2GoInfo.getUniID();
				UniGeneInfo tmpUniGeneInfo = Quni2GoInfo.getUniGeneInfo();
				if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymb() != null)
				{
					tmpBlastInfo[2] = tmpUniGeneInfo.getSymb().split("//")[0];
					tmpBlastInfo[3] = tmpUniGeneInfo.getDescrp();
				}
				else {
					tmpBlastInfo[2] = ServAnno.getUniGenName(Quni2GoInfo.getUniID());
				}
			}
			///////////////////////////////////////////////////
			if (blastInfo.getEvalue()<=evalue&&Sgene2GoInfo!=null) 
			 {
				tmpBlastInfo[7]=blastInfo.getEvalue()+"";
				tmpBlastInfo[8]=Sgene2GoInfo.getTaxID()+"";
				tmpBlastInfo[9]=Sgene2GoInfo.getGeneId()+"";
				GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
				if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
				{
					tmpBlastInfo[10] = tmpGeneInfo.getSymb().split("//")[0];
					tmpBlastInfo[11] = tmpGeneInfo.getDescrp();
				}
				else {
					tmpBlastInfo[10] = ServAnno.getGenName(Sgene2GoInfo.getGeneId());
				}
			}
			else if (blastInfo.getEvalue() <= evalue&&Suni2GoInfo != null)
			{
				tmpBlastInfo[7] = blastInfo.getEvalue()+"";
				tmpBlastInfo[8] = Suni2GoInfo.getTaxID()+"";
				tmpBlastInfo[9] = Suni2GoInfo.getUniID();
				UniGeneInfo tmpUniGeneInfo = Suni2GoInfo.getUniGeneInfo();
				if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymb() != null)
				{
					tmpBlastInfo[10] = tmpUniGeneInfo.getSymb().split("//")[0];
					tmpBlastInfo[11] = tmpUniGeneInfo.getDescrp();
				}
				else {
					tmpBlastInfo[9] = ServAnno.getUniGenName(Suni2GoInfo.getUniID());
				}
			}
			/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
			lsGoResult.add(tmpBlastInfo);
		}
		else
		{
			String[] gene2Go=new String[2];gene2Go[0]="";gene2Go[1]="";
			Set<String> hashGene2Go=new HashSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
			if (SepID) {
				if (Qgene2GoInfo!=null) 
					gene2Go[0]=Qgene2GoInfo.getQuaryID();
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getQuaryID();
			}
			else {
				if (Qgene2GoInfo!=null )
				{
					if (Qgene2GoInfo.getGeneId() == 0) //很有可能只是新建的Qgene2GoInfo，没有geneID项目
						gene2Go[0]=Qgene2GoInfo.getQuaryID();
					else 
						gene2Go[0]=Qgene2GoInfo.getGeneId()+"";
				}
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getUniID();
			}

			for (int j = 0; j < max; j++) {
				String[] tmpBlastInfo=new String[15];
				//赋初值
				for (int k = 0; k < tmpBlastInfo.length; k++) {
					tmpBlastInfo[k]="";
				}
				
				if (Qgene2GoInfo!=null) 
				{
					tmpBlastInfo[0] = Qgene2GoInfo.getQuaryID();
					if (Qgene2GoInfo.getGeneId() != 0) 
						tmpBlastInfo[1] = Qgene2GoInfo.getGeneId()+"";
					else 
						tmpBlastInfo[1] = Qgene2GoInfo.getQuaryID();
					
					GeneInfo tmpGeneInfo=Qgene2GoInfo.getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
					{
						tmpBlastInfo[2] = tmpGeneInfo.getSymb().split("//")[0];
						tmpBlastInfo[3] = tmpGeneInfo.getDescrp();
					}
					else {
						tmpBlastInfo[2] = ServAnno.getGenName(Qgene2GoInfo.getGeneId());
					}
					if (Qgene2GoInfo.getLsGOInfo()!=null&&Qgene2GoInfo.getLsGOInfo().size()>j) 
					{
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GOClass.equals(""))) {
							tmpBlastInfo[4] = GoInfo[1];
							tmpBlastInfo[5] = GoInfo[2];
							tmpBlastInfo[6] = Qgene2GoInfo.getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[4].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(GoInfo[1]);
						}
					}
				}
				else if(Quni2GoInfo!=null)
				{
					tmpBlastInfo[0]=Quni2GoInfo.getQuaryID();
					tmpBlastInfo[1]=Quni2GoInfo.getUniID();
					UniGeneInfo tmpUniGeneInfo=Quni2GoInfo.getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymb()!=null)
					{
						tmpBlastInfo[2]=tmpUniGeneInfo.getSymb().split("//")[0];
						tmpBlastInfo[3] = tmpUniGeneInfo.getDescrp();
					}
					else {
						tmpBlastInfo[2]= ServAnno.getUniGenName(Quni2GoInfo.getUniID());
					}
					if (Quni2GoInfo.getLsUniGOInfo()!=null&&Quni2GoInfo.getLsUniGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null &&  (GoInfo[3].equals(GOClass) || GOClass.equals(""))) {
							tmpBlastInfo[4] = GoInfo[1];
							tmpBlastInfo[5] = GoInfo[2];
							tmpBlastInfo[6]=Quni2GoInfo.getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[4].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(GoInfo[1]);
						}
					}
				}
				///////////////////////////////////////////////////
				if (Sgene2GoInfo != null && blastInfo.getEvalue() < evalue) 
				 {
					tmpBlastInfo[7]=blastInfo.getEvalue() +"";
					tmpBlastInfo[8]=Sgene2GoInfo.getTaxID()+"";
					tmpBlastInfo[9]=Sgene2GoInfo.getGeneId()+"";
					GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
					{
						tmpBlastInfo[10]=tmpGeneInfo.getSymb().split("//")[0];
						tmpBlastInfo[11]=tmpGeneInfo.getDescrp();
					}
					else {
						tmpBlastInfo[10]= ServAnno.getGenName(Sgene2GoInfo.getGeneId());
					}
					if (Sgene2GoInfo.getLsGOInfo()!=null&&Sgene2GoInfo.getLsGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GOClass.equals("")))  {
							tmpBlastInfo[12]=GoInfo[1];
							tmpBlastInfo[13]=GoInfo[2];
							tmpBlastInfo[14]=Sgene2GoInfo.getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[12].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(GoInfo[1]);
						}
					}
				}
				else if (Suni2GoInfo!=null && blastInfo.getEvalue() <evalue) {
					tmpBlastInfo[7]=blastInfo.getEvalue() +"";
					tmpBlastInfo[8]=Suni2GoInfo.getTaxID()+"";
					tmpBlastInfo[9]=Suni2GoInfo.getUniID();
					UniGeneInfo tmpUniGeneInfo=Suni2GoInfo.getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymb()!=null)
					{
						tmpBlastInfo[10]=tmpUniGeneInfo.getSymb().split("//")[0];
						tmpBlastInfo[11]=tmpUniGeneInfo.getDescrp();
					}
					else {
						tmpBlastInfo[10]=ServAnno.getUniGenName(Suni2GoInfo.getUniID());
					}
					if (Suni2GoInfo.getLsUniGOInfo()!=null&&Suni2GoInfo.getLsUniGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = ServGo.getHashGo2Term().get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GOClass.equals("")))  {
							tmpBlastInfo[12] = GoInfo[1];
							tmpBlastInfo[13] = GoInfo[2];
							tmpBlastInfo[14] = Suni2GoInfo.getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[12].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(GoInfo[1]);
						}
					}
				}
				if ( !tmpBlastInfo[4].equals("")  ||  !tmpBlastInfo[12].equals("")  ) {
					lsGoResult.add(tmpBlastInfo);
				}
			}
			if (lsGene2Go != null) {
				for (String s : hashGene2Go) {
					if (gene2Go[1].trim().equals("")) 
						gene2Go[1]=s;
					else
						gene2Go[1]=gene2Go[1]+","+s;
				}
				lsGene2Go.add(gene2Go);
			}
		}
		if (lsGoResult.size() == 0) {
			return null;
		}
		return lsGoResult;
	}

}
