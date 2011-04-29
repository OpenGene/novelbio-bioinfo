package com.novelBio.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSBlastInfo;
import DAO.FriceDAO.DaoFSGene2Go;
import DAO.FriceDAO.DaoFSGo2Term;
import DAO.FriceDAO.DaoFSNCBIID;
import DAO.FriceDAO.DaoFSUniGene2Go;
import DAO.FriceDAO.DaoFSUniProtID;
import entity.friceDB.Blast2GeneInfo;
import entity.friceDB.BlastInfo;
import entity.friceDB.Gene2Go;
import entity.friceDB.Gene2GoInfo;
import entity.friceDB.GeneInfo;
import entity.friceDB.Go2Term;
import entity.friceDB.NCBIID;
import entity.friceDB.Uni2GoInfo;
import entity.friceDB.UniGene2Go;
import entity.friceDB.UniGeneInfo;
import entity.friceDB.UniProtID;

public class GOQuery {
	
	
	
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, String[]> hashGo2Term = new HashMap<String, String[]>();
	
	/**
	 * 将所有GO信息提取出来放入hash表中，方便查找
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * 如果已经查过了一次，自动返回
	 */
	public static HashMap<String, String[]> getHashGo2Term() {
		if (!hashGo2Term.isEmpty()) {
			return hashGo2Term;
		}
		Go2Term go2Term = new Go2Term();
		ArrayList<Go2Term> lsGo2Terms = DaoFSGo2Term.queryLsGo2Term(go2Term);
		for (Go2Term go2Term2 : lsGo2Terms) 
		{
			String[] strgo2term = new String[4];
			strgo2term[0] = go2Term2.getGoIDQuery(); strgo2term[1] = go2Term2.getGoID();
			strgo2term[2] = go2Term2.getGoTerm(); strgo2term[3] = go2Term2.getGoFunction();
			hashGo2Term.put(strgo2term[0], strgo2term);
		}
		return hashGo2Term;
	}
	
	
	
	
	
	
	
	
	
	
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
	 * ArrayList-String[] <br>
	 * 	* 0:queryID
			 * 1:uniID
			 * 2:symbol
			 * 3:GOID
			 * 4:GOTerm 如果没有结果，则返回null<br>
	 * <b>当blast为true时</b> <br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 	5: GO可信度<br>
	 * 6: blastEvalue<br>
	 * 7: taxID<br>
	 * 8: subjectGeneID<br>
	 * 	9: subjectSymbol<br>
	 * 10: GOID<br>
	 * 11: GOTerm<br>
	 * 12: GO可信度<br>
	 */
	public static ArrayList<String[]> getLsGeneGo(String accID,int taxID,String GOClass,boolean blast, double evalue, int StaxID) {
		if (!blast) {
			return getGenGo(accID, taxID, null);
		}
		else {
			Blast2GeneInfo blast2GeneInfo = getGenGoBlast(accID, taxID, StaxID, evalue);
			return copeBlastInfo(blast2GeneInfo, evalue, GOClass, null);
		}
	}
	
	/**
	 * 给定queryID，返回该基因的GO以及BlastGO信息，用Blast2GeneInfo保存
	 * @param accID queryID
	 * @param taxID 物种ID，如果不是Symbol，可以为0
	 * @param StaxID blast的目标物种ID
	 * @param evalue blast的evalue值
	 * @return
	
	 */
	public static Blast2GeneInfo getGenGoBlast(String accID, int taxID,int StaxID,double evalue)
	{
		//存放结果
		Blast2GeneInfo blast2GeneInfo=new Blast2GeneInfo();

		NCBIID ncbiid=new NCBIID();
		ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
		UniProtID uniProtID=new UniProtID();
		uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
		////////首先用accID去查找NCBIID，如果能找到，则记录下queryGene信息，然后用geneID查找blastInfo，如果能找到，则用subjectGeneID查找NCBIID,获得subjectGene信息，本次查找结束//////////////////////////////////////////////////////////
		ArrayList<Gene2GoInfo> lsGene2GoQueryInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
		
		if (lsGene2GoQueryInfo!=null && lsGene2GoQueryInfo.size() > 0)
		{
			Gene2GoInfo gene2GoQueryInfo=lsGene2GoQueryInfo.get(0);
			blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//直接装入
			BlastInfo blastInfo=new BlastInfo();
			blastInfo.setQueryID(gene2GoQueryInfo.getGeneId()+"");
			blastInfo.setQueryTax(taxID);blastInfo.setSubjectTax(StaxID);
			BlastInfo blastInforesult=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			//在blast表里面存在
			if (blastInforesult!=null && blastInforesult.getEvalue()<=evalue) 
			{
				blast2GeneInfo.setIdentities(blastInforesult.getIdentities());blast2GeneInfo.setEvalue(blastInforesult.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult.getBlastDate());
				//用blast的subjectGeneID去查找NCBIID表/UniProtID表
				try {
					long SubjectGeneID=Long.parseLong(blastInforesult.getSubjectID());
					//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
					NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
					NCBIID ncbiidsubject2=null;
					Gene2GoInfo gene2GoSubjectInfo=null;
					ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiidSbuject);
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
					ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtIDSubject);
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
				blastInfo2.setQueryID(accID);blastInfo2.setQueryTax(taxID);blastInfo2.setSubjectTax(StaxID);
				BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo2);
				if (blastInforesult2!=null && blastInforesult.getEvalue() <= evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
					//用blast的subjectGeneID去查找NCBIID表/UniProtID表
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
						//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
						NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
						NCBIID ncbiidsubject2=null;
						Gene2GoInfo gene2GoSubjectInfo=null;
						ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiidSbuject);
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
						ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtIDSubject);
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
 
		//给出的accID在NCBIID中找不到，那么将该ID查找UniProt表，看能不能找到。
		else 
		{
			ArrayList<Uni2GoInfo> lsUni2GoQueryInfo=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
			if (lsUni2GoQueryInfo!=null && lsUni2GoQueryInfo.size() > 0) //UniGene能搜到GO信息
			{
				Uni2GoInfo uni2GoQueryInfo=lsUni2GoQueryInfo.get(0);
				blast2GeneInfo.setQueryUni2GoInfo(uni2GoQueryInfo);//直接装入
				BlastInfo blastInfo=new BlastInfo();
				blastInfo.setQueryID(uni2GoQueryInfo.getUniID());
				blastInfo.setQueryTax(taxID);blastInfo.setSubjectTax(StaxID);
				BlastInfo blastInforesult=DaoFSBlastInfo.queryBlastInfo(blastInfo);
				//在blast表里面存在
				if (blastInforesult!=null && blastInforesult.getEvalue()<=evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult.getIdentities());blast2GeneInfo.setEvalue(blastInforesult.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult.getBlastDate());
					//用blast的subjectGeneID去查找NCBIID表/UniProtID表
					try 
					{
						long SubjectGeneID=Long.parseLong(blastInforesult.getSubjectID());
						//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
						NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
						NCBIID ncbiidsubject2=null;
						Gene2GoInfo gene2GoSubjectInfo=null;
						ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiidSbuject);
						if (lsNcbiids!=null&&lsNcbiids.size()>0) 
						{
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
						ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtIDSubject);
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
					blastInfo2.setQueryID(accID);blastInfo2.setQueryTax(taxID);blastInfo2.setSubjectTax(StaxID);
					BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo2);
					if (blastInforesult2!=null && blastInforesult.getEvalue() <= evalue) 
					{
						blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
						//用blast的subjectGeneID去查找NCBIID表/UniProtID表
						try {
							long SubjectGeneID = Long.parseLong(blastInforesult2.getSubjectID());
							//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
							NCBIID ncbiidSbuject = new NCBIID(); ncbiidSbuject.setGeneId(SubjectGeneID); 
							NCBIID ncbiidsubject2 = null;
							Gene2GoInfo gene2GoSubjectInfo = null;
							ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiidSbuject);
							if (lsNcbiids != null&&lsNcbiids.size()>0) {
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
							ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtIDSubject);
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
			//如果UniProt里面也没有的话
			else
			{
				Gene2GoInfo gene2GoQueryInfo=new Gene2GoInfo();//因为建立了一个新的对象，所以不是引用传递了
				gene2GoQueryInfo.setQuaryID(accID);
				blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//直接装入
				BlastInfo blastInfo3=new BlastInfo();
				blastInfo3.setQueryID(accID);blastInfo3.setQueryTax(taxID);blastInfo3.setSubjectTax(StaxID);
				BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo3);
				if (blastInforesult2!=null && blastInforesult2.getEvalue()<=evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
					//用blast的subjectGeneID去查找NCBIID表/UniProtID表
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
						//因为只拿到了geneID，而一个geneID在NCBIID表中有好多行，所以这里要选出一个然后进入下一步查找
						NCBIID ncbiidSbuject=new NCBIID();ncbiidSbuject.setGeneId(SubjectGeneID); 
						NCBIID ncbiidsubject2=null;
						Gene2GoInfo gene2GoSubjectInfo=null;
						ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiidSbuject);
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
						ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtIDSubject);
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
		return blast2GeneInfo;
	}
	
	/**
	 * 给定queryID，返回该基因的GO信息
	 * @param accID queryID
	 * @param taxID 物种ID，如果不是Symbol，可以为0
	 * @param lsGene2Go 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 * @return ArrayList-String[5]
	 			* 0:queryID
			 * 1:uniID
			 * 2:symbol
			 * 3:GOID
			 * 4:GOTerm
		如果没有结果，则返回null
	 */
	public static  ArrayList<String[]> getGenGo(String accID, int taxID,ArrayList<String[]> lsGene2Go )
	{
		NCBIID ncbiid=new NCBIID();
		ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
		UniProtID uniProtID=new UniProtID();
		uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
		////////首先用accID去查找NCBIID，如果能找到，则记录下queryGene信息，然后用geneID查找blastInfo，如果能找到，则用subjectGeneID查找NCBIID,获得subjectGene信息，本次查找结束//////////////////////////////////////////////////////////
		ArrayList<Gene2GoInfo> lsGene2GoQueryInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
		
		if (lsGene2GoQueryInfo!=null && lsGene2GoQueryInfo.size() > 0)
		{
			Gene2GoInfo gene2GoQueryInfo=lsGene2GoQueryInfo.get(0);
			return copeGene2GoInfo(gene2GoQueryInfo, lsGene2Go);
		}
 
		//给出的accID在NCBIID中找不到，那么将该ID查找UniProt表，看能不能找到。
		else 
		{
			ArrayList<Uni2GoInfo> lsUni2GoQueryInfo=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
			if (lsUni2GoQueryInfo!=null && lsUni2GoQueryInfo.size() > 0) //UniGene能搜到GO信息
			{
				Uni2GoInfo uni2GoQueryInfo=lsUni2GoQueryInfo.get(0);
				return copeUni2GoInfo(uni2GoQueryInfo, lsGene2Go);
			}
		}
		return null;
	}
//	
//	/**
//	 * 给定基因，返回该基因参与的Pathway
//	 * @return
//	 */
//	public ArrayList<String[]> getGenePath() 
//	{
//		return null;
//	}
//	
//	
//	
	
	
	
	/**
	 * 给定Gene2GoInfo的列表，将其中信息整理为
	 * ArrayList-String[5]
	 * 其中：0: queryID
	 * 1: geneID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * 此外，输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 * @param Gene2GoInfos 由getGenGo产生
	 * @param lsGene2Go
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 */
	private static ArrayList<String[]> copeGene2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		//如果没有找到基因 或者 该基因没有Go的信息，则跳过
		if (gene2GoInfo==null||gene2GoInfo.getLsGOInfo()==null||gene2GoInfo.getLsGOInfo().size()==0) 
			return null;
		
		ArrayList<Gene2Go> lsGoInfos=gene2GoInfo.getLsGOInfo();
		//设置GeneID2Go的基因列
		String[] strGene2Go=new String[2];
		strGene2Go[0]=gene2GoInfo.getGeneId()+"";
		strGene2Go[1]="";
		for (int j = 0; j < lsGoInfos.size(); j++) {
			if (j==0) 
				strGene2Go[1]=lsGoInfos.get(j).getGOID();
			else
				strGene2Go[1]=strGene2Go[1]+", "+lsGoInfos.get(j).getGOID();
			
			//每个基因对应的Go信息，一个基因对应一个GO，然后一对多的话排很多列
			String[] gene2Go=new String[5];
			for (int k = 0; k < gene2Go.length; k++)
				gene2Go[k]="";//赋初值，都为空值
			
			gene2Go[0]=gene2GoInfo.getQuaryID();gene2Go[1]=gene2GoInfo.getGeneId()+"";gene2Go[2]=gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
			gene2Go[3]=lsGoInfos.get(j).getGOID();gene2Go[4]=lsGoInfos.get(j).getGOTerm();
			lsGoResult.add(gene2Go);
		}
		if (lsGene2Go != null) 
			lsGene2Go.add(strGene2Go);
		
		return lsGoResult;
	}
	
	/**
	 * 给定Uni2GoInfo的列表，将其中信息整理为
	 * ArrayList-String[5]
	 * 其中：0: queryID
	 * 1: uniID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * 此外，输入一个 lsUniGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 *  如果Gene2GoInfo中没有GO信息，那么返回null
	 * @param Uni2GoInfos 由getGenGo产生
	 * @param lsUniGene2Go 用于GO分析的一个东西。输入一个 lsUniGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 
	 */
	private static ArrayList<String[]>  copeUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//如果没有找到基因 或者 该基因没有Go的信息，则跳过
		if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
			return null;
		ArrayList<UniGene2Go> lsUniGoInfos=uni2GoInfo.getLsUniGOInfo();
		//设置GeneID2Go的基因列
		String[] strGene2Go = new String[2];
		strGene2Go[0] = uni2GoInfo.getUniID();
		strGene2Go[1] = "";
		for (int j = 0; j < lsUniGoInfos.size(); j++) {
			if (j == 0) 
				strGene2Go[1]=lsUniGoInfos.get(j).getGOID();
			else
				strGene2Go[1]=strGene2Go[1]+", "+lsUniGoInfos.get(j).getGOID();
			
			String[] uni2Go=new String[5];
			for (int k = 0; k < uni2Go.length; k++) {
				uni2Go[k]="";//赋初值，都为空值
			}
			uni2Go[0]=uni2GoInfo.getQuaryID();uni2Go[1]=uni2GoInfo.getUniID();uni2Go[2]=uni2GoInfo.getUniGeneInfo().getSymbol().split("//")[0];
			uni2Go[3]=lsUniGoInfos.get(j).getGOID();uni2Go[4]=lsUniGoInfos.get(j).getGOTerm();
			lsGoResult.add(uni2Go);
		}
		if (lsUniGene2Go != null) 
			lsUniGene2Go.add(strGene2Go);
		
		return lsGoResult;
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
 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为<br>
	 * 0：sepID-true: accID sepID-false: geneID <br>
	 * 1：GOID,GOID,GOID...<br>
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null
	 * @param Gene2GoInfos 由getGenGo产生
	 * @param lsGene2Go
	 * 输入一个 lsGene2Go-Stirng[]，其中String[]的结构为 <br> 
	 * 0：sepID  true: accID  false:geneID <br> 
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * 如果Gene2GoInfo中没有GO信息，那么返回null<br>
	 * <b>注意输入前要对Gene2GoInfo中的geneID去重复<b/>
	 */
	public static ArrayList<String[]> getGene2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go,boolean sep,String GOClass)
	{
		getHashGo2Term();
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
			String[] tmpGeneGoInfo = hashGo2Term.get(lsGoInfos.get(j).getGOID());

			String[] gene2Go=new String[7];
			for (int k = 0; k < gene2Go.length; k++) {
				gene2Go[k]="";//赋初值，都为空值
			}
			gene2Go[0] = gene2GoInfo.getQuaryID();
			gene2Go[1] = gene2GoInfo.getGeneId() + "";
			
			GeneInfo tmpGeneInfo = gene2GoInfo.getGeneInfo();
			
			//annotation
			if(tmpGeneInfo != null && tmpGeneInfo.getSymbol() != null)
			{
				gene2Go[2] = tmpGeneInfo.getSymbol().split("//")[0];
				gene2Go[3] = tmpGeneInfo.getDescription();
			}
			/////////////////////////////////如果没有symbol的话，就随便找一个accID放上去//////////////////////////////////////////////////
			else 
				gene2Go[2] = AnnoQuery.getUniGenName(gene2Go[1]);
			
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
	public static ArrayList<String[]>  getUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go,boolean sep,String GOClass)
	{
		getHashGo2Term();
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
			String[] tmpGeneGoInfo = hashGo2Term.get(lsUniGoInfos.get(j).getGOID());
			


			String[] uni2Go=new String[7];
			for (int k = 0; k < uni2Go.length; k++) {
				uni2Go[k]="";//赋初值，都为空值
			}
			uni2Go[0] = uni2GoInfo.getQuaryID();
			uni2Go[1] = uni2GoInfo.getUniID();
			
			UniGeneInfo tmpUniGeneInfo = uni2GoInfo.getUniGeneInfo();
			
			//annotation
			if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymbol() != null)
			{
				uni2Go[2] = tmpUniGeneInfo.getSymbol().split("//")[0];
				uni2Go[3] = tmpUniGeneInfo.getDescription();
			}
			/////////////////////////////////如果没有symbol的话，就随便找一个accID放上去//////////////////////////////////////////////////
			else 
				uni2Go[2] = AnnoQuery.getUniGenName(uni2Go[1]);
			
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
		
		return lsGoResult;
	}
	
	/**
	 * 暂时不用
	 * 给定由getGenGoBlast方法产生的Blast2GeneInfos类，按照evalue的阈值，将其中信息整理为
	 * ArrayList-String[6]
	 * 其中：<br>
	 * 0: queryID<br>
	 * 1: querySymbol<br>
	 * 2: subjectSymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 5: GO可信度<br>
	 * 此外，输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * @param Blast2GeneInfo 由getGenGoBlast方法产生的Blast2GeneInfos类
	 * @param evalue 相似度的阈值
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private static ArrayList<String[]> TmpcopeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,List<String[]> lsGene2Go)
	{
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		Gene2GoInfo Qgene2GoInfo = blast2GeneInfo.getQueryGene2GoInfo();
		Uni2GoInfo Quni2GoInfo = blast2GeneInfo.getQueryUniGene2GoInfo();
		Gene2GoInfo Sgene2GoInfo = blast2GeneInfo.getSubjectGene2GoInfo();
		Uni2GoInfo Suni2GoInfo = blast2GeneInfo.getSubjectUni2GoInfo();
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
		if (Sgene2GoInfo!=null&&blast2GeneInfo.getEvalue()<=evalue) {
			flag[2][0]=1;
			if (Sgene2GoInfo.getLsGOInfo()!=null)
				flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
			else 
				flag[2][1]=0;
		}
		else if (Suni2GoInfo!=null&&blast2GeneInfo.getEvalue()<=evalue) {
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
				/////////////////////////////////如果没有symbol的话，就随便找一个accID放上去//////////////////////////////////////////////////
				else {
					tmpBlastInfo[1] = AnnoQuery.getGenName(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
				}
			}
			else if(blast2GeneInfo.getQueryUniGene2GoInfo()!=null)
			{
				tmpBlastInfo[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();
				UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getQueryUniGene2GoInfo().getUniGeneInfo();
				if(tmpUniGeneInfo!=null && tmpUniGeneInfo.getSymbol()!=null)
					tmpBlastInfo[1]=tmpUniGeneInfo.getSymbol().split("//")[0];
				else {
					tmpBlastInfo[1] = AnnoQuery.getUniGenName(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
				}
			}
			///////////////////////////////////////////////////
			if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
			 {
				GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
				if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
					tmpBlastInfo[2]=tmpGeneInfo.getSymbol().split("//")[0];
				else {
					tmpBlastInfo[2] = AnnoQuery.getGenName(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
				}
			}
			else if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
			{
				UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
				if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
					tmpBlastInfo[2]=tmpUniGeneInfo.getSymbol().split("//")[0];
				else {
					tmpBlastInfo[2] = AnnoQuery.getUniGenName(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
				}
			}
			/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
			//lsGoResult.add(tmpBlastInfo);
		}
		else {
			String[] gene2Go = new String[2]; gene2Go[0] = ""; gene2Go[1] = "";
			Set<String> hashGene2Go=new HashSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
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
						symbol = AnnoQuery.getGenName(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
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
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
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
						symbol = AnnoQuery.getUniGenName(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
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
								System.out.println("TmpcopeBlastInfoSimple error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
				///////////////////////////////////////////////////
				if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
				 {
					GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
						subSymbol = tmpGeneInfo.getSymbol().split("//")[0];
					//如果没有symbol的话，就随便找一个accID放上去
					else {
						subSymbol = AnnoQuery.getGenName(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
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
							hashGene2Go.add(tmpBlastInfo[3]);
							lsGoResult.add(tmpBlastInfo);
						}
					}
				}
				else if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
				{
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						subSymbol = tmpUniGeneInfo.getSymbol().split("//")[0];
					//如果没有symbol的话，就随便找一个accID放上去
					else {
						subSymbol = AnnoQuery.getUniGenName(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
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
	//public static ArrayList<String[]> copeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,List<String[]> lsGene2Go)
	public static ArrayList<String[]> copeBlastInfoSimple(Gene2GoInfo Qgene2GoInfo,Uni2GoInfo Quni2GoInfo,
			Gene2GoInfo Sgene2GoInfo,Uni2GoInfo Suni2GoInfo,BlastInfo blastInfo,double evalue,String GOClass,
			boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		getHashGo2Term();
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
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
				symbol = tmpGeneInfo.getSymbol().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				symbol = AnnoQuery.getGenName(Qgene2GoInfo.getGeneId());
			}
		}
		else if(Quni2GoInfo!=null)
		{
			accID = Quni2GoInfo.getQuaryID();
			geneID = Quni2GoInfo.getUniID();
			UniGeneInfo tmpUniGeneInfo=Quni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
				symbol = tmpUniGeneInfo.getSymbol().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				symbol = AnnoQuery.getUniGenName(Quni2GoInfo.getUniID());
			}
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) 
		 {
			GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
				subSymbol = tmpGeneInfo.getSymbol().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				subSymbol = AnnoQuery.getGenName(Sgene2GoInfo.getGeneId());
			}
		 }
		else if (Suni2GoInfo!=null && blastInfo.getEvalue()<=evalue)
		{
			UniGeneInfo tmpUniGeneInfo=Suni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
				subSymbol = tmpUniGeneInfo.getSymbol().split("//")[0];
			//如果没有symbol的话，就随便找一个accID放上去
			else {
				subSymbol = AnnoQuery.getUniGenName(Suni2GoInfo.getUniID());
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
						String[] GoInfo = hashGo2Term.get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
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
						String[] GoInfo = hashGo2Term.get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
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
						String[] GoInfo = hashGo2Term.get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
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
						String[] GoInfo = hashGo2Term.get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
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
	 *  * 此外，输入一个 lsGene2Go-Stirng[]，其中String[]的结构为
	 * 0：accID
	 * 1：GOID,GOID,GOID...
	 * 那么将本次查询的结果保存进去
	 * @param lsGene2GoInfos
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private static ArrayList<String[]> copeBlastInfo(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,List<String[]> lsGene2Go)
	{
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
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
		if (Sgene2GoInfo!=null&&blast2GeneInfo.getEvalue()<=evalue) {
			flag[2][0]=1;
			if (Sgene2GoInfo.getLsGOInfo()!=null)
				flag[2][1]=Sgene2GoInfo.getLsGOInfo().size();
			else 
				flag[2][1]=0;
		}
		else if (Suni2GoInfo!=null&&blast2GeneInfo.getEvalue()<=evalue) {
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
					tmpBlastInfo[2] = AnnoQuery.getGenName(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
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
					tmpBlastInfo[2] = AnnoQuery.getUniGenName(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
				}
			}
			///////////////////////////////////////////////////
			if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectGene2GoInfo()!=null) 
			 {
				tmpBlastInfo[6]=blast2GeneInfo.getEvalue()+"";
				tmpBlastInfo[7]=blast2GeneInfo.getSubjectGene2GoInfo().getTaxID()+"";
				tmpBlastInfo[8]=blast2GeneInfo.getSubjectGene2GoInfo().getGeneId()+"";
				GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
				if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
					tmpBlastInfo[9] = tmpGeneInfo.getSymbol().split("//")[0];
				else {
					tmpBlastInfo[9] = AnnoQuery.getGenName(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
				}
			}
			else if (blast2GeneInfo.getEvalue()<=evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null)
			{
				tmpBlastInfo[6]=blast2GeneInfo.getEvalue()+"";
				tmpBlastInfo[7]=blast2GeneInfo.getSubjectUni2GoInfo().getTaxID()+"";
				tmpBlastInfo[8]=blast2GeneInfo.getSubjectUni2GoInfo().getUniID();
				UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
				if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
					tmpBlastInfo[9]=tmpUniGeneInfo.getSymbol().split("//")[0];
				else {
					tmpBlastInfo[9] = AnnoQuery.getUniGenName(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
				}
			}
			/////////////////////////没有go的就不加入最后结果//////////////////////////////////////////////////////////////////
			//lsGoResult.add(tmpBlastInfo);
		}
		else {
			String[] gene2Go=new String[2];gene2Go[0]="";gene2Go[1]="";
			Set<String> hashGene2Go=new HashSet<String>();//先把go放在这个里面去重复，然后在装入gene2Go
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
						tmpBlastInfo[2] = tmpGeneInfo.getSymbol().split("//")[0];
					else {
						tmpBlastInfo[2] = AnnoQuery.getGenName(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
					}
					if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().size()>j) 
					{
						if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
							tmpBlastInfo[3]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOID();
							tmpBlastInfo[4]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
							tmpBlastInfo[5]=blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[3].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
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
						tmpBlastInfo[2]= AnnoQuery.getUniGenName(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
					}
					if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().size()>j) {
						if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
							tmpBlastInfo[3]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOID();
							tmpBlastInfo[4]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
							tmpBlastInfo[5]=blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[3].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(tmpBlastInfo[3]);
						}
					}
				}
				///////////////////////////////////////////////////
				if (blast2GeneInfo.getEvalue() < evalue && blast2GeneInfo.getSubjectGene2GoInfo() != null) 
				 {
					tmpBlastInfo[6]=blast2GeneInfo.getEvalue()+"";
					tmpBlastInfo[7]=blast2GeneInfo.getSubjectGene2GoInfo().getTaxID()+"";
					tmpBlastInfo[8]=blast2GeneInfo.getSubjectGene2GoInfo().getGeneId()+"";
					GeneInfo tmpGeneInfo=blast2GeneInfo.getSubjectGene2GoInfo().getGeneInfo();
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
						tmpBlastInfo[9]=tmpGeneInfo.getSymbol().split("//")[0];
					else {
						tmpBlastInfo[9]= AnnoQuery.getGenName(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
					}
					if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().size()>j) {
						if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
							tmpBlastInfo[10]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOID();
							tmpBlastInfo[11]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getGOTerm();
							tmpBlastInfo[12]=blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[10].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(tmpBlastInfo[10]);
						}
					}
				}
				else if (blast2GeneInfo.getEvalue()<evalue&&blast2GeneInfo.getSubjectUni2GoInfo()!=null) {
					tmpBlastInfo[6]=blast2GeneInfo.getEvalue()+"";
					tmpBlastInfo[7]=blast2GeneInfo.getSubjectUni2GoInfo().getTaxID()+"";
					tmpBlastInfo[8]=blast2GeneInfo.getSubjectUni2GoInfo().getUniID();
					UniGeneInfo tmpUniGeneInfo=blast2GeneInfo.getSubjectUni2GoInfo().getUniGeneInfo();
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
						tmpBlastInfo[9]=tmpUniGeneInfo.getSymbol().split("//")[0];
					else {
						tmpBlastInfo[9]=AnnoQuery.getUniGenName(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
					}
					if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().size()>j) {
						if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
							tmpBlastInfo[10]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOID();
							tmpBlastInfo[11]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getGOTerm();
							tmpBlastInfo[12]=blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getEvidence();
							if (tmpBlastInfo[10].trim().equals("")) {
								System.out.println("copeBlastInfo error");
							}
							hashGene2Go.add(tmpBlastInfo[10]);
						}
					}
				}
				if ( !tmpBlastInfo[3].equals("")  ||  !tmpBlastInfo[10].equals("")  ) {
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
	 */
	public static ArrayList<String[]> copeBlastInfo(Gene2GoInfo Qgene2GoInfo,Uni2GoInfo Quni2GoInfo,
			Gene2GoInfo Sgene2GoInfo,Uni2GoInfo Suni2GoInfo,BlastInfo blastInfo,double evalue,String GOClass,
			boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		getHashGo2Term();
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
				if(tmpGeneInfo != null && tmpGeneInfo.getSymbol() != null)
				{
					tmpBlastInfo[2] = tmpGeneInfo.getSymbol().split("//")[0];
					tmpBlastInfo[3] = tmpGeneInfo.getDescription();
				}
				//如果没有symbol的话，就随便找一个accID放上去
				else {
					tmpBlastInfo[2] = AnnoQuery.getGenName(Qgene2GoInfo.getGeneId());
					}
			}
			else if(Quni2GoInfo!=null)
			{
				tmpBlastInfo[0] = Quni2GoInfo.getQuaryID();
				tmpBlastInfo[1] = Quni2GoInfo.getUniID();
				UniGeneInfo tmpUniGeneInfo = Quni2GoInfo.getUniGeneInfo();
				if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymbol() != null)
				{
					tmpBlastInfo[2] = tmpUniGeneInfo.getSymbol().split("//")[0];
					tmpBlastInfo[3] = tmpUniGeneInfo.getDescription();
				}
				else {
					tmpBlastInfo[2] = AnnoQuery.getUniGenName(Quni2GoInfo.getUniID());
				}
			}
			///////////////////////////////////////////////////
			if (blastInfo.getEvalue()<=evalue&&Sgene2GoInfo!=null) 
			 {
				tmpBlastInfo[7]=blastInfo.getEvalue()+"";
				tmpBlastInfo[8]=Sgene2GoInfo.getTaxID()+"";
				tmpBlastInfo[9]=Sgene2GoInfo.getGeneId()+"";
				GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
				if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
				{
					tmpBlastInfo[10] = tmpGeneInfo.getSymbol().split("//")[0];
					tmpBlastInfo[11] = tmpGeneInfo.getDescription();
				}
				else {
					tmpBlastInfo[10] = AnnoQuery.getGenName(Sgene2GoInfo.getGeneId());
				}
			}
			else if (blastInfo.getEvalue() <= evalue&&Suni2GoInfo != null)
			{
				tmpBlastInfo[7] = blastInfo.getEvalue()+"";
				tmpBlastInfo[8] = Suni2GoInfo.getTaxID()+"";
				tmpBlastInfo[9] = Suni2GoInfo.getUniID();
				UniGeneInfo tmpUniGeneInfo = Suni2GoInfo.getUniGeneInfo();
				if(tmpUniGeneInfo != null && tmpUniGeneInfo.getSymbol() != null)
				{
					tmpBlastInfo[10] = tmpUniGeneInfo.getSymbol().split("//")[0];
					tmpBlastInfo[11] = tmpUniGeneInfo.getDescription();
				}
				else {
					tmpBlastInfo[9] = AnnoQuery.getUniGenName(Suni2GoInfo.getUniID());
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
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
					{
						tmpBlastInfo[2] = tmpGeneInfo.getSymbol().split("//")[0];
						tmpBlastInfo[3] = tmpGeneInfo.getDescription();
					}
					else {
						tmpBlastInfo[2] = AnnoQuery.getGenName(Qgene2GoInfo.getGeneId());
					}
					if (Qgene2GoInfo.getLsGOInfo()!=null&&Qgene2GoInfo.getLsGOInfo().size()>j) 
					{
						//从hash表中获得go2term的信息
						String[] GoInfo = hashGo2Term.get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals(""))) {
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
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
					{
						tmpBlastInfo[2]=tmpUniGeneInfo.getSymbol().split("//")[0];
						tmpBlastInfo[3] = tmpUniGeneInfo.getDescription();
					}
					else {
						tmpBlastInfo[2]= AnnoQuery.getUniGenName(Quni2GoInfo.getUniID());
					}
					if (Quni2GoInfo.getLsUniGOInfo()!=null&&Quni2GoInfo.getLsUniGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = hashGo2Term.get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null &&  (GoInfo[3].equals(GOClass) || GoInfo[3].equals(""))) {
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
					if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
					{
						tmpBlastInfo[10]=tmpGeneInfo.getSymbol().split("//")[0];
						tmpBlastInfo[11]=tmpGeneInfo.getDescription();
					}
					else {
						tmpBlastInfo[10]= AnnoQuery.getGenName(Sgene2GoInfo.getGeneId());
					}
					if (Sgene2GoInfo.getLsGOInfo()!=null&&Sgene2GoInfo.getLsGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = hashGo2Term.get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))  {
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
					if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
					{
						tmpBlastInfo[10]=tmpUniGeneInfo.getSymbol().split("//")[0];
						tmpBlastInfo[11]=tmpUniGeneInfo.getDescription();
					}
					else {
						tmpBlastInfo[10]=AnnoQuery.getUniGenName(Suni2GoInfo.getUniID());
					}
					if (Suni2GoInfo.getLsUniGOInfo()!=null&&Suni2GoInfo.getLsUniGOInfo().size()>j) {
						//从hash表中获得go2term的信息
						String[] GoInfo = hashGo2Term.get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))  {
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
		return lsGoResult;
	}
	/**
	 * @param ncbiid geneID属性必须有
	 * @return
	 */
	public static ArrayList<Gene2Go> getGen2Go(NCBIID ncbiid)
	{
		long GeneID = ncbiid.getGeneId();
		return DaoFSGene2Go.queryGene2Go(GeneID);
	}
	
	/**
	 * @param uniProtID uniID属性必须有
	 * @return
	 */
	public static ArrayList<UniGene2Go> getUniGen2Go(UniProtID uniProtID)
	{
		return DaoFSUniGene2Go.queryUniGene2Go(uniProtID.getUniID());
	}
	
	

}
