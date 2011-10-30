package com.novelbio.analysis.annotation.GO.queryDB;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeSet;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFCGene2GoInfo;
import com.novelbio.database.entity.friceDB.Blast2GeneInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniProtID;

public class QBlastGO {
	
	/**
	 * 用来保存geneID2Go，和UniProtID2Go的两个文件
	 * 主要用来写入背景
	 * geneID \t GOID,GOID,GOID
	 */
	ArrayList<String[]> lsGene2Go=null;

	/**
	 * 保存了Blast状况的List
	 */
	ArrayList<Blast2GeneInfo> lsBlast2GeneInfos;
	
	/**
	 * 
	 * 给定基因文件，背景文件，taxID文件，返回两个列表
	 * 1. GeneID的GO列表
	 * 2. BackGroundGO列表，格式为 geneID \t GOID \t GOID....\n
	 * 3. GeneID的列表  格式为 geneID \n
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param geneID
	 * @param backGroundFile
	 * @param taxQID
	 * @param taxSID
	 * @param evalue
	 * @param resultGeneGofile
	 * @param resultBGGofile
	 * @param resultGeneIDfile
	 * @return object[2] 0:strGeneID  1:lsGoResult,带标题
	 * 		title[0] = "accessID";<br>
		title[1] = "GeneSymbol";<br>
		title[2] = "Blast2Symbol";<br>
		title[3] = "GOID";<br>
		title[4] = "GOTerm";;<br>
		title[5] = "GOEvidence";<br>
	 * @throws Exception
	 */
	public Object[] goAnalysis(String GOClass,String[] geneID,String backGroundFile,int taxQID, int taxSID,double evalue, String resultGeneGofile,String resultBGGofile,String resultGeneIDfile) throws Exception 
	{
		//获得TaxID，TaxID写在taxIDfile的第一行，是数字
		TxtReadandWrite txtTaxID=new TxtReadandWrite();
		
		getBlastGenID2GoInfo(geneID, taxQID,taxSID);
		ArrayList<String[]> lsGoResult=copeBlastInfoSimple(evalue,GOClass);
		String[] title = new String[6];
		title[0] = "AccessID";
		title[1] = "GeneSymbol";
		title[2] = "Blast2Symbol";
		title[3] = "GOID";
		title[4] = "GOTerm";
		title[5] = "GOEvidence";
		lsGoResult.add(0, title);
		//GeneID写入结果
		//GeneID去重复
		TreeSet<String> treeGeneID=new TreeSet<String>();
		for (int i = 0; i < lsGene2Go.size(); i++) {
			treeGeneID.add(lsGene2Go.get(i)[0]);
		}
 
 
		String[] strGeneID = new String[treeGeneID.size()]; int i=0;
		for (String s : treeGeneID) {
			strGeneID[i]=s;i++;
		}
		Object[] objGeneID_GeneInfo = new Object[2];
		objGeneID_GeneInfo[0] = strGeneID;
		objGeneID_GeneInfo[1] = lsGoResult;
		TxtReadandWrite txtGeneID=new TxtReadandWrite(); txtGeneID.setParameter(resultGeneIDfile, true, false);
		txtGeneID.Rwritefile(strGeneID);
		
		//GeneGo写入结果
		txtGeneID.setParameter(resultGeneGofile, true, false);
		txtGeneID.ExcelWrite(lsGoResult, "\t", 1, 1);
		/**
		 * BackGroudID的处理
		 */
		String[] BackGroundID = KGprepare.getAccID(backGroundFile, 1, 1);;
		getBlastGenID2GoInfo(BackGroundID, taxQID,taxSID);
		ArrayList<String[]> lsBGGoResult=copeBlastInfoSimple(evalue,GOClass);
		//GeneID2Go写入结果
		TxtReadandWrite txtBGGeneID=new TxtReadandWrite(); txtBGGeneID.setParameter(resultBGGofile, true, false);
		txtBGGeneID.ExcelWrite(lsGene2Go, "\t", 1, 1);
		return objGeneID_GeneInfo;
	}
	
	
	
	/**
	 * 给定基因，背景文件，taxID文件<br>
	 * 将结果放入object[2]中<br>
	 * 0：lsGOInfo ArrayList-String[] 准备做fisher的表格  GOID	GOTerm	difGene	AllDifGene	GeneInGoID	AllGene	//后面加的Pvalue	FDR	enrichment	logP <br>
	 * 1：lsGoResult ArrayList-String[] GO的具体信息 accessID  GeneSymbol  Blast2Symbol  GOID  GOTerm  GOEvidence<br>
	 * @param geneID 基因数组
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param backGroundFile
	 * @param taxIDfile 第一行 queryTaxID，第二行 subjectTaxID，第三行 evalue
	 * @throws Exception 
	 */
//	public Object[] goAnalysis(String[] geneID,String GOClass, String backGroundFile, String taxIDfile) throws Exception 
	public Object[] goAnalysis(int taxQID, String[] geneID,String GOClass, String backGroundFile,double evalue, int taxSID) throws Exception 
	{
		//////////////////Gene的处理//////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * GeneID的处理
		 */
		getBlastGenID2GoInfo(geneID, taxQID,taxSID);
		ArrayList<String[]> lsGoResult=copeBlastInfoSimple(evalue,GOClass);
		/**
		String[] title = new String[6];
		title[0] = "accessID";
		title[1] = "GeneSymbol";
		title[2] = "Blast2Symbol";
		title[3] = "GOID";
		title[4] = "GOTerm";
		title[5] = "GOEvidence";
		lsGoResult.add(0, title);
		**/
		//GeneID写入结果
		//GeneID去重复
		TreeSet<String> treeGeneID=new TreeSet<String>();
		for (int i = 0; i < lsGene2Go.size(); i++) {
			treeGeneID.add(lsGene2Go.get(i)[0]);
		}
		
		
		//用hash表来将gene2Go里的信息去重复。
		Hashtable<String, String> hashGene2Go=new Hashtable<String, String>();
		for (int j = 0; j < lsGene2Go.size(); j++) {
			hashGene2Go.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
		}
		Hashtable<String, ArrayList<String>> hashGo2Gene = convertGene2Go(hashGene2Go);

		//////////////////BackGround的处理//////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * BackGroudID的处理
		 */
		String[] backGroundID = KGprepare.getAccID(backGroundFile, 1, 1);//getQueryID(backGroundFile);
		getBlastGenID2GoInfo(backGroundID, taxQID,taxSID);
		ArrayList<String[]> lsBGResult = copeBlastInfoSimple(evalue,GOClass);
		//GeneID写入结果
		//GeneID去重复
		TreeSet<String> treeBGGeneID=new TreeSet<String>();
		for (int i = 0; i < lsGene2Go.size(); i++) {
			treeBGGeneID.add(lsGene2Go.get(i)[0]);
		}
		
		
		//用hash表来将gene2Go里的信息去重复。
		Hashtable<String, String> hashBGGene2Go=new Hashtable<String, String>();
		for (int j = 0; j < lsGene2Go.size(); j++) {
			hashBGGene2Go.put(lsGene2Go.get(j)[0], lsGene2Go.get(j)[1]);
		}
		Hashtable<String, ArrayList<String>> hashBGGo2Gene = convertGene2Go(hashBGGene2Go);
		//////////////////BackGround的处理//////////////////////////////////////////////////////////////////////////////////////////
		int NumDifGene=hashGene2Go.size();
		int NumAllGene=hashBGGene2Go.size();
		ArrayList<String[]> lsGOInfo = cope2HashForPvalue(hashGo2Gene, NumDifGene,hashBGGo2Gene,NumAllGene);
		//TxtReadandWrite goInfo=new TxtReadandWrite();
		//goInfo.setParameter(writeFIle, true, false);
		//goInfo.ExcelWrite(lsGOInfo, "\t", 1, 1);
		
		/**
		TxtReadandWrite txtGeneID=new TxtReadandWrite();
		//GeneGo写入结果
		txtGeneID.setParameter(resultGeneGofile, true, false);
		txtGeneID.ExcelWrite(lsGoResult, "\t", 1, 1);
		**/
		//将结果放入object[2]中
		//0：lsGOInfo 准备做fisher的表格 
		//1：lsGoResult GO的具体信息 accessID  GeneSymbol  Blast2Symbol  GOID  GOTerm  GOEvidence
		Object[] objResult = new Object[2];
		objResult[0] = lsGOInfo;
		objResult[1] = lsGoResult;
		
		return objResult;
	}
	
	
	

	/**
	 * 给定两个hashtable，一个为差异基因：
	 * GOTerm--list-GeneID[]
	 * 一个为总基因
	 * GOTerm---list-GeneID[]
	 * 注意差异基因必须在总基因中
	 * 
	 * @return
	 * arrayList-string[6]
	 * 0:GOID
	 * 1:GOTerm
	 * 2:difInGoNum
	 * 3:difNum
	 * 4:GeneGoNum
	 * 5:GeneNum
	 */
	private ArrayList<String[]> cope2HashForPvalue(Hashtable<String, ArrayList<String>> hashDif,int NumDif,Hashtable<String, ArrayList<String>> hashAll ,int NumAll) {
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
		Enumeration keys=hashDif.keys();
		//这个是结果文件，保存了如下结果：
		//GOID,GOterm,该GOID的差异基因Num，总差异基因Num，该GOID的总基因Num，总基因的Num,
		while(keys.hasMoreElements()){
			String GOID=(String)keys.nextElement();
		    ArrayList<String> lsGeneID = hashDif.get(GOID);
		    String[] tmpResult=new String[6];
		    tmpResult[0]=GOID;
		    if (GOID.trim().equals("")) {
				continue;
			}
		    Go2Term go2Term=new Go2Term(); go2Term.setGoIDQuery(GOID);
		    Go2Term go2Term2=MapGo2Term.queryGo2Term(go2Term);

		    	tmpResult[1]=go2Term2.getGoTerm();

		    
		    tmpResult[2]=lsGeneID.size()+"";
		    tmpResult[3]=NumDif+"";
		    tmpResult[4]=hashAll.get(GOID).size()+"";
		    tmpResult[5]=NumAll+"";
		    lsResult.add(tmpResult);
		}
		
		return lsResult;
	}
	
	/**
	 * 将hash gene--go 转变为
	 * hash go--list-gene
	 * @param hashGene2Go gene--goID1,goID2,goID3.....
	 * @return
	 */
	private Hashtable<String, ArrayList<String>> convertGene2Go(Hashtable<String, String> hashGene2Go)
	{
		//////////////////////////将差异基因的Gene2Go的信息转换为Go2Gene的信息//////////////////////////////
		Hashtable<String, ArrayList<String>> hashGo2Gene = new Hashtable<String, ArrayList<String>>();
		
		//将gene2go的信息转入hashGo2Gene
		Enumeration keys = hashGene2Go.keys();
		while(keys.hasMoreElements())
		{
			String key=(String)keys.nextElement();
		    String val = (String) hashGene2Go.get(key);
		    String[] ss=val.split(",");
		    for (int j = 0; j < ss.length; j++) 
		    {
		    	String GOID=ss[j].trim();
		    	if (hashGo2Gene.containsKey(GOID))
		    	{
					ArrayList<String> lsgeneID=hashGo2Gene.get(GOID);
					if (!lsgeneID.contains(key))
					{
						lsgeneID.add(key);
					}
				}
		    	else
		    	{
					ArrayList<String> lsgeneID=new ArrayList<String>();
					lsgeneID.add(key);
					hashGo2Gene.put(GOID, lsgeneID);
				}
			}
		}
		return hashGo2Gene;
	}

	
	/**
	 * 给定的AccID，先搜索NCBIID，用geneID搜BlastInfo表，<br>
	 * 如果搜到了那么然后用subjectID搜索NCBIID/UnID表，获得subject信息<br>
	 * 如果没搜到NCBIID，那么直接搜BlastInfo表，如果搜到了那么然后用subjectID搜索NCBIID/UnID表，获得subject信息<br>
	 * 不行再搜索UniProtID，用UniGeneID搜BlastInfo表，如果搜到了那么就停止，<br>
	 * 不行再直接搜索BlastInfo表，<br>
	 * 最后总结出一个来。<br>
	 * 生成一个list，	ArrayList-Blast2GeneInfo lsBlast2GeneInfos
	 * @param AccID
	 * @param QueryTaxID
	 */
	private void getBlastGenID2GoInfo(String[] AccID,int QueryTaxID,int SubjectTaxID) {
		lsBlast2GeneInfos=new ArrayList<Blast2GeneInfo>(); 
		for (int i = 0; i < AccID.length; i++)
		{
			Blast2GeneInfo blast2GeneInfo=new Blast2GeneInfo();
	
			NCBIID ncbiid=new NCBIID();
			ncbiid.setAccID(AccID[i]);ncbiid.setTaxID(QueryTaxID);
			UniProtID uniProtID=new UniProtID();
			uniProtID.setAccID(AccID[i]);uniProtID.setTaxID(QueryTaxID);
			////////首先用accID去查找NCBIID，如果能找到，则记录下queryGene信息，然后用geneID查找blastInfo，如果能找到，则用subjectGeneID查找NCBIID,获得subjectGene信息，本次查找结束//////////////////////////////////////////////////////////
			ArrayList<Gene2GoInfo> lsGene2GoQueryInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
			
			if (lsGene2GoQueryInfo!=null&&lsGene2GoQueryInfo.size()>0)
			{
				Gene2GoInfo gene2GoQueryInfo=lsGene2GoQueryInfo.get(0);
				blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//直接装入
				BlastInfo blastInfo=new BlastInfo();
				blastInfo.setQueryID(gene2GoQueryInfo.getGeneId()+"");
				blastInfo.setQueryTax(QueryTaxID);blastInfo.setSubjectTax(SubjectTaxID);
				BlastInfo blastInforesult=MapBlastInfo.queryBlastInfo(blastInfo);
				//在blast表里面存在
				if (blastInforesult!=null) 
				{
					blast2GeneInfo.setBlastInfo(blastInforesult);
					//用blast的subjectGeneID去查找NCBIID表/UniProtID表
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult.getSubjectID());
						//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
						NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
						NCBIID ncbiidsubject2=null;
						Gene2GoInfo gene2GoSubjectInfo=null;
						ArrayList<NCBIID> lsNcbiids=MapNCBIID.queryLsNCBIID(ncbiidSbuject);
						if (lsNcbiids!=null&&lsNcbiids.size()>0) {
							ncbiidsubject2=lsNcbiids.get(0);
							gene2GoSubjectInfo=DaoFCGene2GoInfo.queryGeneDetail(ncbiidsubject2);
							blast2GeneInfo.setSubjectGene2GoInfo(gene2GoSubjectInfo);
						}
						
					} catch (Exception e) 
					{
						String SubjectUniGeneID=blastInforesult.getSubjectID();
						UniProtID uniProtIDSubject=new UniProtID();uniProtIDSubject.setUniID(SubjectUniGeneID);
						UniProtID uniProtIDSubject2=null;
						Uni2GoInfo uni2GoInfoSubject=null;
						ArrayList<UniProtID> lsUniProtIDs=MapUniProtID.queryLsUniProtID(uniProtIDSubject);
						if (lsUniProtIDs!=null&&lsUniProtIDs.size()>0) 
						{
							uniProtIDSubject2=lsUniProtIDs.get(0);
							uni2GoInfoSubject=DaoFCGene2GoInfo.queryUniDetail(uniProtIDSubject2);
							blast2GeneInfo.setSubjectUni2GoInfo(uni2GoInfoSubject);
						}	
					}
				}
				//如果没有在blastInfo表里面找到，那么说明这个geneID在blast表中不存在，再试试输入的accID能不能直接在blastInfo中找到
				//这里有个前提，accID2目的基因的blast必须要做过。目前是 agilent的探针和human对应的表有，所以要这么找
				else 
				{
				
					BlastInfo blastInfo2=new BlastInfo();
					blastInfo2.setQueryID(AccID[i]);blastInfo2.setQueryTax(QueryTaxID);blastInfo2.setSubjectTax(SubjectTaxID);
					BlastInfo blastInforesult2=MapBlastInfo.queryBlastInfo(blastInfo2);
					if (blastInforesult2!=null) 
					{
						blast2GeneInfo.setBlastInfo(blastInfo2);//.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
						//用blast的subjectGeneID去查找NCBIID表/UniProtID表
						try {
							long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
							//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
							NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
							NCBIID ncbiidsubject2=null;
							Gene2GoInfo gene2GoSubjectInfo=null;
							ArrayList<NCBIID> lsNcbiids=MapNCBIID.queryLsNCBIID(ncbiidSbuject);
							if (lsNcbiids!=null&&lsNcbiids.size()>0) {
								ncbiidsubject2=lsNcbiids.get(0);
								gene2GoSubjectInfo=DaoFCGene2GoInfo.queryGeneDetail(ncbiidsubject2);
								blast2GeneInfo.setSubjectGene2GoInfo(gene2GoSubjectInfo);
							}
						} catch (Exception e) 
						{
							String SubjectUniGeneID=blastInforesult2.getSubjectID();
							UniProtID uniProtIDSubject=new UniProtID();uniProtIDSubject.setUniID(SubjectUniGeneID);
							UniProtID uniProtIDSubject2=null;
							Uni2GoInfo uni2GoInfoSubject=null;
							ArrayList<UniProtID> lsUniProtIDs=MapUniProtID.queryLsUniProtID(uniProtIDSubject);
							if (lsUniProtIDs!=null&&lsUniProtIDs.size()>0) 
							{
								uniProtIDSubject2=lsUniProtIDs.get(0);
								uni2GoInfoSubject=DaoFCGene2GoInfo.queryUniDetail(uniProtIDSubject2);
								blast2GeneInfo.setSubjectUni2GoInfo(uni2GoInfoSubject);
							}
						}
					}
				}
			}
			//给出的accID在NCBIID中找不到，那么直接将该ID查找BlastInfo表，看能不能找到。
			else {
				Gene2GoInfo gene2GoQueryInfo=new Gene2GoInfo();//因为建立了一个新的对象，所以不是引用传递了
				gene2GoQueryInfo.setQuaryID(AccID[i]);
				blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//直接装入
				BlastInfo blastInfo3=new BlastInfo();
				blastInfo3.setQueryID(AccID[i]);blastInfo3.setQueryTax(QueryTaxID);blastInfo3.setSubjectTax(SubjectTaxID);
				BlastInfo blastInforesult2=MapBlastInfo.queryBlastInfo(blastInfo3);
				if (blastInforesult2!=null) 
				{
					blast2GeneInfo.setBlastInfo(blastInforesult2);
					//用blast的subjectGeneID去查找NCBIID表/UniProtID表
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
						//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
						NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
						NCBIID ncbiidsubject2=null;
						Gene2GoInfo gene2GoSubjectInfo=null;
						ArrayList<NCBIID> lsNcbiids=MapNCBIID.queryLsNCBIID(ncbiidSbuject);
						if (lsNcbiids!=null&&lsNcbiids.size()>0) {
							ncbiidsubject2=lsNcbiids.get(0);
							gene2GoSubjectInfo=DaoFCGene2GoInfo.queryGeneDetail(ncbiidsubject2);
							blast2GeneInfo.setSubjectGene2GoInfo(gene2GoSubjectInfo);
						}
					} catch (Exception e) 
					{
						String SubjectUniGeneID=blastInforesult2.getSubjectID();
						UniProtID uniProtIDSubject=new UniProtID();uniProtID.setUniID(SubjectUniGeneID);
						UniProtID uniProtIDSubject2=null;
						Uni2GoInfo uni2GoInfoSubject=null;
						ArrayList<UniProtID> lsUniProtIDs=MapUniProtID.queryLsUniProtID(uniProtIDSubject);
						if (lsUniProtIDs!=null&&lsUniProtIDs.size()>0) 
						{
							uniProtIDSubject2=lsUniProtIDs.get(0);
							uni2GoInfoSubject=DaoFCGene2GoInfo.queryUniDetail(uniProtIDSubject2);
							blast2GeneInfo.setSubjectUni2GoInfo(uni2GoInfoSubject);
						}
					}
				}
			}
			lsBlast2GeneInfos.add(blast2GeneInfo);
		}
	}
	
	/**
	 * 给定lsBlast2GeneInfos的列表，按照evalue的阈值，将其中信息整理为
	 * ArrayList-String[13]
	 * 其中：<br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 5: GO可信度<br>
	 * 6: blastEvalue<br>
	 * 7: taxID<br>
	 * 8: subjectGeneID<br>
	 * 9: subjectSymbol<br>
	 * 10: GOID<br>
	 * 11: GOTerm<br>
	 * 12: GO可信度<br>
	 * 同时生成一个 lsGene2Go
	 * @param lsGene2GoInfos
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private ArrayList<String[]> copeBlastInfo(double evalue,String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		
		for (int i = 0; i < lsBlast2GeneInfos.size(); i++) 
		{
			Blast2GeneInfo blast2GeneInfo=lsBlast2GeneInfos.get(i);

			Gene2GoInfo Qgene2GoInfo=blast2GeneInfo.getQueryGene2GoInfo();
			Uni2GoInfo Quni2GoInfo=blast2GeneInfo.getQueryUniGene2GoInfo();
			Gene2GoInfo Sgene2GoInfo=blast2GeneInfo.getSubjectGene2GoInfo();
			Uni2GoInfo Suni2GoInfo=blast2GeneInfo.getSubjectUni2GoInfo();
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
			if (Sgene2GoInfo!=null&&blast2GeneInfo.getBlastInfo().getEvalue()<=evalue) {
				flag[2][0]=1;
				if (Sgene2GoInfo.getLsGOInfo()!=null)
					flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
				else 
					flag[2][1]=0;
			}
			else if (Suni2GoInfo!=null&&blast2GeneInfo.getBlastInfo().getEvalue()<=evalue) {
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
				/**
				 * 最后记录的信息就在这里面了
				 */
				String[] tmpBlastInfo=new String[13];
				//赋初值
				for (int j = 0; j < tmpBlastInfo.length; j++) {
					tmpBlastInfo[j]="";
				}
				if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
				{
					tmpBlastInfo[0]=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
					tmpBlastInfo[1]=blast2GeneInfo.getQueryGene2GoInfo().getGeneId()+"";
					GeneInfo tmpGeneInfo=blast2GeneInfo.getQueryGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
						tmpBlastInfo[2]=tmpGeneInfo.getSymbol().split("//")[0];
					//如果没有symbol的话，就随便找一个accID放上去
					else {
						NCBIID ncbiid = new NCBIID(); ncbiid.setGeneId(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
						ArrayList<NCBIID> lsncbiidsub = MapNCBIID.queryLsNCBIID(ncbiid);
						if (lsncbiidsub!=null && lsncbiidsub.size()>0)  {
							tmpBlastInfo[2] = lsncbiidsub.get(0).getAccID();
						}
					}
					
					
				}
				else if(blast2GeneInfo.getQueryUniGene2GoInfo()!=null)
				{
					tmpBlastInfo[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
					tmpBlastInfo[1]=blast2GeneInfo.getQueryUniGene2GoInfo().getUniID();
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getQueryUniGene2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						tmpBlastInfo[2]=tmpUniGeneInfo.getSymbol().split("//")[0];
					else {
						UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
						ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
						if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)  {
							tmpBlastInfo[2] = lsUniProtIDs.get(0).getAccID();
						}
					}
					
					
				}
				///////////////////////////////////////////////////
				if (blast2GeneInfo.getBlastInfo().getEvalue()<=evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
				 {
					tmpBlastInfo[6]=blast2GeneInfo.getBlastInfo().getEvalue()+"";
					tmpBlastInfo[7]=blast2GeneInfo.getSubjectGene2GoInfo().getTaxID()+"";
					tmpBlastInfo[8]=blast2GeneInfo.getSubjectGene2GoInfo().getGeneId()+"";
					GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
						tmpBlastInfo[9]=tmpGeneInfo.getSymbol().split("//")[0];
				}
				else if (blast2GeneInfo.getBlastInfo().getEvalue()<=evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
				{
					tmpBlastInfo[6]=blast2GeneInfo.getBlastInfo().getEvalue()+"";
					tmpBlastInfo[7]=blast2GeneInfo.getSubjectUni2GoInfo().getTaxID()+"";
					tmpBlastInfo[8]=blast2GeneInfo.getSubjectUni2GoInfo().getUniID();
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						tmpBlastInfo[9]=tmpUniGeneInfo.getSymbol().split("//")[0];
				}
				/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
				//lsGoResult.add(tmpBlastInfo);
			}
			else {
				String[] gene2Go=new String[2];gene2Go[0]="";gene2Go[1]="";
				TreeSet<String> treeGene2Go=new TreeSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
				if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
					gene2Go[0]=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
				else if (blast2GeneInfo.getQueryUniGene2GoInfo()!=null) 
					gene2Go[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
				
				
				for (int j = 0; j < max; j++) {
					String[] tmpBlastInfo=new String[13];
					//赋初值
					for (int k = 0; k < tmpBlastInfo.length; k++) {
						tmpBlastInfo[k]="";
					}
					
					if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
					{
						tmpBlastInfo[0]=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
						tmpBlastInfo[1]=blast2GeneInfo.getQueryGene2GoInfo().getGeneId()+"";
						GeneInfo tmpGeneInfo=blast2GeneInfo.getQueryGene2GoInfo().getGeneInfo();
						if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
							tmpBlastInfo[2]=tmpGeneInfo.getSymbol().split("//")[0];
						if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().size()>j) 
						{
							if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
								tmpBlastInfo[3]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
							}
						}
					}
					else if(blast2GeneInfo.getQueryUniGene2GoInfo()!=null)
					{
						tmpBlastInfo[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
						tmpBlastInfo[1]=blast2GeneInfo.getQueryUniGene2GoInfo().getUniID();
						UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getQueryUniGene2GoInfo().getUniGeneInfo();
						if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
							tmpBlastInfo[2]=tmpUniGeneInfo.getSymbol().split("//")[0];
						if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().size()>j) {
							if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
								tmpBlastInfo[3]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
							}
						}
					}
					///////////////////////////////////////////////////
					if (blast2GeneInfo.getBlastInfo().getEvalue()<evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
					 {
						tmpBlastInfo[6]=blast2GeneInfo.getBlastInfo().getEvalue()+"";
						tmpBlastInfo[7]=blast2GeneInfo.getSubjectGene2GoInfo().getTaxID()+"";
						tmpBlastInfo[8]=blast2GeneInfo.getSubjectGene2GoInfo().getGeneId()+"";
						GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
						if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
							tmpBlastInfo[9]=tmpGeneInfo.getSymbol().split("//")[0];
						if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().size()>j) {
							if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
								tmpBlastInfo[10]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOID();
								tmpBlastInfo[11]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
								tmpBlastInfo[12]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[10].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[10]);
							}
						}
					}
					else if (blast2GeneInfo.getBlastInfo().getEvalue()<evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null) {
						tmpBlastInfo[6]=blast2GeneInfo.getBlastInfo().getEvalue()+"";
						tmpBlastInfo[7]=blast2GeneInfo.getSubjectUni2GoInfo().getTaxID()+"";
						tmpBlastInfo[8]=blast2GeneInfo.getSubjectUni2GoInfo().getUniID();
						UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
						if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
							tmpBlastInfo[9]=tmpUniGeneInfo.getSymbol().split("//")[0];
						if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().size()>j) {
							if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
								tmpBlastInfo[10]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOID();
								tmpBlastInfo[11]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
								tmpBlastInfo[12]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[10].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[10]);
							}
						}
					}
					if ( !tmpBlastInfo[3].equals("")  ||  !tmpBlastInfo[10].equals("")  ) {
						lsGoResult.add(tmpBlastInfo);
					}
				}
				
				for (String s : treeGene2Go) {
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
	 * 给定lsBlast2GeneInfos的列表，按照evalue的阈值，将其中信息整理为
	 * ArrayList-String[13]
	 * 其中：<br>
	 * 0: queryID<br>
	 * 1: querySymbol<br>
	 * 2: subjectSymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 5: GO可信度<br>
	 * 同时生成一个 lsGene2Go
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private ArrayList<String[]> copeBlastInfoSimple(double evalue,String GOClass)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		
		for (int i = 0; i < lsBlast2GeneInfos.size(); i++) 
		{
			Blast2GeneInfo blast2GeneInfo=lsBlast2GeneInfos.get(i);

			Gene2GoInfo Qgene2GoInfo=blast2GeneInfo.getQueryGene2GoInfo();
			Uni2GoInfo Quni2GoInfo=blast2GeneInfo.getQueryUniGene2GoInfo();
			Gene2GoInfo Sgene2GoInfo=blast2GeneInfo.getSubjectGene2GoInfo();
			Uni2GoInfo Suni2GoInfo=blast2GeneInfo.getSubjectUni2GoInfo();
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
			if (Sgene2GoInfo!=null&&blast2GeneInfo.getBlastInfo().getEvalue()<=evalue) {
				flag[2][0]=1;
				if (Sgene2GoInfo.getLsGOInfo()!=null)
					flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
				else 
					flag[2][1]=0;
			}
			else if (Suni2GoInfo!=null&&blast2GeneInfo.getBlastInfo().getEvalue()<=evalue) {
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
				/**
				 * 最后记录的信息就在这里面了
				 */
				String[] tmpBlastInfo=new String[6];
				//赋初值
				for (int j = 0; j < tmpBlastInfo.length; j++) {
					tmpBlastInfo[j]="";
				}
				if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
				{
					tmpBlastInfo[0] = blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
					GeneInfo tmpGeneInfo = blast2GeneInfo.getQueryGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo != null && tmpGeneInfo.getSymbol() != null)
						tmpBlastInfo[1] = tmpGeneInfo.getSymbol().split("//")[0];
					//如果没有symbol的话，就随便找一个accID放上去
					else {
						NCBIID ncbiid = new NCBIID(); ncbiid.setGeneId(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
						if (ncbiid.getGeneId()>0) {
							ArrayList<NCBIID> lsncbiidsub = MapNCBIID.queryLsNCBIID(ncbiid);
							if (lsncbiidsub!=null && lsncbiidsub.size()>0)  {
								tmpBlastInfo[1] = lsncbiidsub.get(0).getAccID();
							}
						}
					}
				}
				else if(blast2GeneInfo.getQueryUniGene2GoInfo()!=null)
				{
					tmpBlastInfo[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getQueryUniGene2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						tmpBlastInfo[1]=tmpUniGeneInfo.getSymbol().split("//")[0];
					else {
						UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
						if (uniProtID.getUniID() != null && !uniProtID.getUniID().equals("")) {
							ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
							if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)  {
								tmpBlastInfo[1] = lsUniProtIDs.get(0).getAccID();
							}
						}
					}
				}
				///////////////////////////////////////////////////
				if (blast2GeneInfo.getBlastInfo().getEvalue()<=evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
				 {
					GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
						tmpBlastInfo[2]=tmpGeneInfo.getSymbol().split("//")[0];
					else {
						NCBIID ncbiid = new NCBIID(); ncbiid.setGeneId(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
						if (ncbiid.getGeneId()>0) {
							ArrayList<NCBIID> lsncbiidsub = MapNCBIID.queryLsNCBIID(ncbiid);
							if (lsncbiidsub!=null && lsncbiidsub.size()>0)  {
								tmpBlastInfo[2] = lsncbiidsub.get(0).getAccID();
							}
						}
					}
					
				}
				else if (blast2GeneInfo.getBlastInfo().getEvalue()<=evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
				{
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						tmpBlastInfo[2]=tmpUniGeneInfo.getSymbol().split("//")[0];
					else {
						UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
						if (uniProtID.getUniID() != null && !uniProtID.getUniID().equals("")) {
							ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
							if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)  {
								tmpBlastInfo[2] = lsUniProtIDs.get(0).getAccID();
							}
						}
					}
				}
				/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
				//lsGoResult.add(tmpBlastInfo);
			}
			else {
				String[] gene2Go = new String[2]; gene2Go[0] = ""; gene2Go[1] = "";
				TreeSet<String> treeGene2Go=new TreeSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
				if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
					gene2Go[0]=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
				else if (blast2GeneInfo.getQueryUniGene2GoInfo()!=null) 
					gene2Go[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
				
				
				for (int j = 0; j < max; j++) {
					String accID = "";
					String symbol = "";
					String subSymbol = "";
					if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
					{
						accID=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
						GeneInfo tmpGeneInfo=blast2GeneInfo.getQueryGene2GoInfo().getGeneInfo();
						if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
							symbol = tmpGeneInfo.getSymbol().split("//")[0];
						//如果没有symbol的话，就随便找一个accID放上去
						else {
							NCBIID ncbiid = new NCBIID(); ncbiid.setGeneId(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
							if (ncbiid.getGeneId()>0) {
								ArrayList<NCBIID> lsncbiidsub = MapNCBIID.queryLsNCBIID(ncbiid);
								if (lsncbiidsub!=null && lsncbiidsub.size()>0)  {
									symbol = lsncbiidsub.get(0).getAccID();
								}
							}
						}
						//因为现在用max，也就是所有go的最大的一行为j，所以这里要判断某个具体的go是否大于j。否则会出错
						if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().size()>j) 
						{
							if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
								String[] tmpBlastInfo=new String[6];
								//赋初值
								for (int k = 0; k < tmpBlastInfo.length; k++) {
									tmpBlastInfo[k]="";
								}
								tmpBlastInfo[0] = accID;
								tmpBlastInfo[1] = symbol;
								tmpBlastInfo[2] = subSymbol;
								tmpBlastInfo[3]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
								lsGoResult.add(tmpBlastInfo);
							}
						}
					}
					else if(blast2GeneInfo.getQueryUniGene2GoInfo()!=null)
					{
						accID=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
						UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getQueryUniGene2GoInfo().getUniGeneInfo();
						if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
							symbol = tmpUniGeneInfo.getSymbol().split("//")[0];
						//如果没有symbol的话，就随便找一个accID放上去
						else {
							UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
							if (uniProtID.getUniID() != null && !uniProtID.getUniID().equals("")) {
								ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
								if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)  {
									symbol = lsUniProtIDs.get(0).getAccID();
								}
							}
						}
						if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().size()>j) 
						{
							if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
								String[] tmpBlastInfo=new String[6];
								//赋初值
								for (int k = 0; k < tmpBlastInfo.length; k++) {
									tmpBlastInfo[k]="";
								}
								
								tmpBlastInfo[0] = accID;
								tmpBlastInfo[1] = symbol;
								tmpBlastInfo[2] = subSymbol;
								tmpBlastInfo[3]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
								lsGoResult.add(tmpBlastInfo);
							}
						}
					}
					///////////////////////////////////////////////////
					if (blast2GeneInfo.getBlastInfo().getEvalue()<evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
					 {
						GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
						if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
							subSymbol = tmpGeneInfo.getSymbol().split("//")[0];
						//如果没有symbol的话，就随便找一个accID放上去
						else {
							NCBIID ncbiid = new NCBIID(); ncbiid.setGeneId(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
							if (ncbiid.getGeneId()>0) {
								ArrayList<NCBIID> lsncbiidsub = MapNCBIID.queryLsNCBIID(ncbiid);
								if (lsncbiidsub!=null && lsncbiidsub.size()>0)  {
									subSymbol = lsncbiidsub.get(0).getAccID();
								}
							}
						}
						if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().size()>j) {
							if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
								String[] tmpBlastInfo=new String[6];
								//赋初值
								for (int k = 0; k < tmpBlastInfo.length; k++) {
									tmpBlastInfo[k]="";
								}
								tmpBlastInfo[0] = accID;
								tmpBlastInfo[1] = symbol;
								tmpBlastInfo[2] = subSymbol;
								tmpBlastInfo[3]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
								lsGoResult.add(tmpBlastInfo);
							}
						}
					}
					else if (blast2GeneInfo.getBlastInfo().getEvalue()<evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
					{
						UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
						if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
							subSymbol = tmpUniGeneInfo.getSymbol().split("//")[0];
						//如果没有symbol的话，就随便找一个accID放上去
						else {
							UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
							if (uniProtID.getUniID() != null && !uniProtID.getUniID().equals("")) {
								ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
								if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)  {
									subSymbol = lsUniProtIDs.get(0).getAccID();
								}
							}
						}

						if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().size()>j) {
							if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
								String[] tmpBlastInfo=new String[6];
								//赋初值
								for (int k = 0; k < tmpBlastInfo.length; k++) {
									tmpBlastInfo[k]="";
								}
								tmpBlastInfo[0] = accID;
								tmpBlastInfo[1] = symbol;
								tmpBlastInfo[2] = subSymbol;
								tmpBlastInfo[3]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOID();
								tmpBlastInfo[4]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
								tmpBlastInfo[5]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getEvidence();
								if (tmpBlastInfo[3].trim().equals("")) {
									System.out.println("error");
								}
								treeGene2Go.add(tmpBlastInfo[3]);
								lsGoResult.add(tmpBlastInfo);
							}
						}
					}
 
				}
				//将treeset中的不重复的geneID装入结果文件
				for (String s : treeGene2Go) {
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
	
	
	
}
