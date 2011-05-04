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
	 * �洢Go2Term����Ϣ
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, String[]> hashGo2Term = new HashMap<String, String[]>();
	
	/**
	 * ������GO��Ϣ��ȡ��������hash���У��������
	 * �洢Go2Term����Ϣ
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * ����Ѿ������һ�Σ��Զ�����
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
	 * @param accID ����geneID
	 * @param taxID taxID
	 * @param GOClass GO�ķ��ࣺ<br>
	 * <b>P</b>:Biological process <br>
	 * <b>C</b>:Cellular component <br>
	 * <b>F</b>:Molecular function
	 * @param blast �Ƿ�blast
	 * @param evalue blast����ֵ
	 * @param StaxID Ŀ������ID
	 * @return
	 * <b>��blastΪfalseʱ</b><br>
	 * ArrayList-String[] <br>
	 * 	* 0:queryID
			 * 1:uniID
			 * 2:symbol
			 * 3:GOID
			 * 4:GOTerm ���û�н�����򷵻�null<br>
	 * <b>��blastΪtrueʱ</b> <br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 	5: GO���Ŷ�<br>
	 * 6: blastEvalue<br>
	 * 7: taxID<br>
	 * 8: subjectGeneID<br>
	 * 	9: subjectSymbol<br>
	 * 10: GOID<br>
	 * 11: GOTerm<br>
	 * 12: GO���Ŷ�<br>
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
	 * ����queryID�����ظû����GO�Լ�BlastGO��Ϣ����Blast2GeneInfo����
	 * @param accID queryID
	 * @param taxID ����ID���������Symbol������Ϊ0
	 * @param StaxID blast��Ŀ������ID
	 * @param evalue blast��evalueֵ
	 * @return
	
	 */
	public static Blast2GeneInfo getGenGoBlast(String accID, int taxID,int StaxID,double evalue)
	{
		//��Ž��
		Blast2GeneInfo blast2GeneInfo=new Blast2GeneInfo();

		NCBIID ncbiid=new NCBIID();
		ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
		UniProtID uniProtID=new UniProtID();
		uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
		////////������accIDȥ����NCBIID��������ҵ������¼��queryGene��Ϣ��Ȼ����geneID����blastInfo��������ҵ�������subjectGeneID����NCBIID,���subjectGene��Ϣ�����β��ҽ���//////////////////////////////////////////////////////////
		ArrayList<Gene2GoInfo> lsGene2GoQueryInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
		
		if (lsGene2GoQueryInfo!=null && lsGene2GoQueryInfo.size() > 0)
		{
			Gene2GoInfo gene2GoQueryInfo=lsGene2GoQueryInfo.get(0);
			blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//ֱ��װ��
			BlastInfo blastInfo=new BlastInfo();
			blastInfo.setQueryID(gene2GoQueryInfo.getGeneId()+"");
			blastInfo.setQueryTax(taxID);blastInfo.setSubjectTax(StaxID);
			BlastInfo blastInforesult=DaoFSBlastInfo.queryBlastInfo(blastInfo);
			//��blast���������
			if (blastInforesult!=null && blastInforesult.getEvalue()<=evalue) 
			{
				blast2GeneInfo.setIdentities(blastInforesult.getIdentities());blast2GeneInfo.setEvalue(blastInforesult.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult.getBlastDate());
				//��blast��subjectGeneIDȥ����NCBIID��/UniProtID��
				try {
					long SubjectGeneID=Long.parseLong(blastInforesult.getSubjectID());
					//��Ϊֻ�õ���geneID����һ��geneID��NCBIID�����кö��У���������Ҫѡ��һ��Ȼ�������һ������
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
			//���û����blastInfo�������ҵ�����ô˵�����geneID��blast���в����ڣ������������accID�ܲ���ֱ����blastInfo���ҵ�
			//�����и�ǰ�ᣬaccID2Ŀ�Ļ����blast����Ҫ������Ŀǰ�� agilent��̽���human��Ӧ�ı��У�����Ҫ��ô��
			else 
			{
				BlastInfo blastInfo2=new BlastInfo();
				blastInfo2.setQueryID(accID);blastInfo2.setQueryTax(taxID);blastInfo2.setSubjectTax(StaxID);
				BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo2);
				if (blastInforesult2!=null && blastInforesult.getEvalue() <= evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
					//��blast��subjectGeneIDȥ����NCBIID��/UniProtID��
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
						//��Ϊֻ�õ���geneID����һ��geneID��NCBIID�����кö��У���������Ҫѡ��һ��Ȼ�������һ������
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
 
		//������accID��NCBIID���Ҳ�������ô����ID����UniProt�����ܲ����ҵ���
		else 
		{
			ArrayList<Uni2GoInfo> lsUni2GoQueryInfo=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
			if (lsUni2GoQueryInfo!=null && lsUni2GoQueryInfo.size() > 0) //UniGene���ѵ�GO��Ϣ
			{
				Uni2GoInfo uni2GoQueryInfo=lsUni2GoQueryInfo.get(0);
				blast2GeneInfo.setQueryUni2GoInfo(uni2GoQueryInfo);//ֱ��װ��
				BlastInfo blastInfo=new BlastInfo();
				blastInfo.setQueryID(uni2GoQueryInfo.getUniID());
				blastInfo.setQueryTax(taxID);blastInfo.setSubjectTax(StaxID);
				BlastInfo blastInforesult=DaoFSBlastInfo.queryBlastInfo(blastInfo);
				//��blast���������
				if (blastInforesult!=null && blastInforesult.getEvalue()<=evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult.getIdentities());blast2GeneInfo.setEvalue(blastInforesult.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult.getBlastDate());
					//��blast��subjectGeneIDȥ����NCBIID��/UniProtID��
					try 
					{
						long SubjectGeneID=Long.parseLong(blastInforesult.getSubjectID());
						//��Ϊֻ�õ���geneID����һ��geneID��NCBIID�����кö��У���������Ҫѡ��һ��Ȼ�������һ������
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
				//���û����blastInfo�������ҵ�����ô˵�����geneID��blast���в����ڣ������������accID�ܲ���ֱ����blastInfo���ҵ�
				//�����и�ǰ�ᣬaccID2Ŀ�Ļ����blast����Ҫ������Ŀǰ�� agilent��̽���human��Ӧ�ı��У�����Ҫ��ô��
				else 
				{
					BlastInfo blastInfo2=new BlastInfo();
					blastInfo2.setQueryID(accID);blastInfo2.setQueryTax(taxID);blastInfo2.setSubjectTax(StaxID);
					BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo2);
					if (blastInforesult2!=null && blastInforesult.getEvalue() <= evalue) 
					{
						blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
						//��blast��subjectGeneIDȥ����NCBIID��/UniProtID��
						try {
							long SubjectGeneID = Long.parseLong(blastInforesult2.getSubjectID());
							//��Ϊֻ�õ���geneID����һ��geneID��NCBIID�����кö��У���������Ҫѡ��һ��Ȼ�������һ������
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
			//���UniProt����Ҳû�еĻ�
			else
			{
				Gene2GoInfo gene2GoQueryInfo=new Gene2GoInfo();//��Ϊ������һ���µĶ������Բ������ô�����
				gene2GoQueryInfo.setQuaryID(accID);
				blast2GeneInfo.setQueryGene2GoInfo(gene2GoQueryInfo);//ֱ��װ��
				BlastInfo blastInfo3=new BlastInfo();
				blastInfo3.setQueryID(accID);blastInfo3.setQueryTax(taxID);blastInfo3.setSubjectTax(StaxID);
				BlastInfo blastInforesult2=DaoFSBlastInfo.queryBlastInfo(blastInfo3);
				if (blastInforesult2!=null && blastInforesult2.getEvalue()<=evalue) 
				{
					blast2GeneInfo.setIdentities(blastInforesult2.getIdentities());blast2GeneInfo.setEvalue(blastInforesult2.getEvalue());blast2GeneInfo.setBlastDate(blastInforesult2.getBlastDate());
					//��blast��subjectGeneIDȥ����NCBIID��/UniProtID��
					try {
						long SubjectGeneID=Long.parseLong(blastInforesult2.getSubjectID());
						//��Ϊֻ�õ���geneID����һ��geneID��NCBIID�����кö��У���������Ҫѡ��һ��Ȼ�������һ������
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
	 * ����queryID�����ظû����GO��Ϣ
	 * @param accID queryID
	 * @param taxID ����ID���������Symbol������Ϊ0
	 * @param lsGene2Go ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @return ArrayList-String[5]
	 			* 0:queryID
			 * 1:uniID
			 * 2:symbol
			 * 3:GOID
			 * 4:GOTerm
		���û�н�����򷵻�null
	 */
	public static  ArrayList<String[]> getGenGo(String accID, int taxID,ArrayList<String[]> lsGene2Go )
	{
		NCBIID ncbiid=new NCBIID();
		ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
		UniProtID uniProtID=new UniProtID();
		uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
		////////������accIDȥ����NCBIID��������ҵ������¼��queryGene��Ϣ��Ȼ����geneID����blastInfo��������ҵ�������subjectGeneID����NCBIID,���subjectGene��Ϣ�����β��ҽ���//////////////////////////////////////////////////////////
		ArrayList<Gene2GoInfo> lsGene2GoQueryInfo=DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
		
		if (lsGene2GoQueryInfo!=null && lsGene2GoQueryInfo.size() > 0)
		{
			Gene2GoInfo gene2GoQueryInfo=lsGene2GoQueryInfo.get(0);
			return copeGene2GoInfo(gene2GoQueryInfo, lsGene2Go);
		}
 
		//������accID��NCBIID���Ҳ�������ô����ID����UniProt�����ܲ����ҵ���
		else 
		{
			ArrayList<Uni2GoInfo> lsUni2GoQueryInfo=DaoFCGene2GoInfo.queryLsUniDetail(uniProtID);
			if (lsUni2GoQueryInfo!=null && lsUni2GoQueryInfo.size() > 0) //UniGene���ѵ�GO��Ϣ
			{
				Uni2GoInfo uni2GoQueryInfo=lsUni2GoQueryInfo.get(0);
				return copeUni2GoInfo(uni2GoQueryInfo, lsGene2Go);
			}
		}
		return null;
	}
//	
//	/**
//	 * �������򣬷��ظû�������Pathway
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
	 * ����Gene2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[5]
	 * ���У�0: queryID
	 * 1: geneID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * ���⣬����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @param Gene2GoInfos ��getGenGo����
	 * @param lsGene2Go
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 */
	private static ArrayList<String[]> copeGene2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		lsGene2Go=new ArrayList<String[]>();
		//���û���ҵ����� ���� �û���û��Go����Ϣ��������
		if (gene2GoInfo==null||gene2GoInfo.getLsGOInfo()==null||gene2GoInfo.getLsGOInfo().size()==0) 
			return null;
		
		ArrayList<Gene2Go> lsGoInfos=gene2GoInfo.getLsGOInfo();
		//����GeneID2Go�Ļ�����
		String[] strGene2Go=new String[2];
		strGene2Go[0]=gene2GoInfo.getGeneId()+"";
		strGene2Go[1]="";
		for (int j = 0; j < lsGoInfos.size(); j++) {
			if (j==0) 
				strGene2Go[1]=lsGoInfos.get(j).getGOID();
			else
				strGene2Go[1]=strGene2Go[1]+", "+lsGoInfos.get(j).getGOID();
			
			//ÿ�������Ӧ��Go��Ϣ��һ�������Ӧһ��GO��Ȼ��һ�Զ�Ļ��źܶ���
			String[] gene2Go=new String[5];
			for (int k = 0; k < gene2Go.length; k++)
				gene2Go[k]="";//����ֵ����Ϊ��ֵ
			
			gene2Go[0]=gene2GoInfo.getQuaryID();gene2Go[1]=gene2GoInfo.getGeneId()+"";gene2Go[2]=gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
			gene2Go[3]=lsGoInfos.get(j).getGOID();gene2Go[4]=lsGoInfos.get(j).getGOTerm();
			lsGoResult.add(gene2Go);
		}
		if (lsGene2Go != null) 
			lsGene2Go.add(strGene2Go);
		
		return lsGoResult;
	}
	
	/**
	 * ����Uni2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[5]
	 * ���У�0: queryID
	 * 1: uniID
	 * 2: symbol
	 * 3: GOID
	 * 4: GOTerm
	 * ���⣬����һ�� lsUniGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 *  ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @param Uni2GoInfos ��getGenGo����
	 * @param lsUniGene2Go ����GO������һ������������һ�� lsUniGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * 
	 */
	private static ArrayList<String[]>  copeUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go)
	{
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//���û���ҵ����� ���� �û���û��Go����Ϣ��������
		if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
			return null;
		ArrayList<UniGene2Go> lsUniGoInfos=uni2GoInfo.getLsUniGOInfo();
		//����GeneID2Go�Ļ�����
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
				uni2Go[k]="";//����ֵ����Ϊ��ֵ
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
	 * ����Gene2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[7]<br>
	 * ���У�0: queryID<br>
	 * 1: geneID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: geneID <br>
	 * 1��GOID,GOID,GOID...<br>
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @param Gene2GoInfos ��getGenGo����
	 * @param lsGene2Go
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ <br> 
	 * 0��sepID  true: accID  false:geneID <br> 
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null<br>
	 * <b>ע������ǰҪ��Gene2GoInfo�е�geneIDȥ�ظ�<b/>
	 */
	public static ArrayList<String[]> getGene2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go,boolean sep,String GOClass)
	{
		getHashGo2Term();
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//���û���ҵ����� ���� �û���û��Go����Ϣ��������
		if (gene2GoInfo == null || gene2GoInfo.getLsGOInfo() == null || gene2GoInfo.getLsGOInfo().size()==0) 
			return null;
		ArrayList<Gene2Go> lsGoInfos = gene2GoInfo.getLsGOInfo();
		
		//����GeneID2Go�Ļ�����
		String[] strGene2Go=new String[2];
		if (sep) 
			strGene2Go[0]=gene2GoInfo.getQuaryID();
		else 
			strGene2Go[0]=gene2GoInfo.getGeneId()+"";
		
		strGene2Go[1]=""; int NumGO = 0;//���ר�����������õ��˼���GOID
		for (int j = 0; j < lsGoInfos.size(); j++) 
		{
			String[] tmpGeneGoInfo = hashGo2Term.get(lsGoInfos.get(j).getGOID());

			String[] gene2Go=new String[7];
			for (int k = 0; k < gene2Go.length; k++) {
				gene2Go[k]="";//����ֵ����Ϊ��ֵ
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
			/////////////////////////////////���û��symbol�Ļ����������һ��accID����ȥ//////////////////////////////////////////////////
			else 
				gene2Go[2] = AnnoQuery.getUniGenName(gene2Go[1]);
			
			if (tmpGeneGoInfo != null && !tmpGeneGoInfo[1].trim().equals("") && (tmpGeneGoInfo[3].equals(GOClass) || GOClass.equals(""))) {
				
				gene2Go[4] = tmpGeneGoInfo[1];
				gene2Go[5] = tmpGeneGoInfo[2];
				gene2Go[6] = lsGoInfos.get(j).getEvidence();
				lsGoResult.add(gene2Go);
				//װ��lsUniGene2Go
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
	 * ����Uni2GoInfo���б���������Ϣ����Ϊ<br>
	 * ArrayList-String[7]<br>
	 * ���У�0: queryID<br>
	 * 1: uniID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: uniID <br>
	 * 1��GOID,GOID,GOID...<br>
	 * ��ô�����β�ѯ�Ľ�������ȥ<br>
	 *  ���Gene2GoInfo��û��GO��Ϣ����ô����null<br>
	 * @param Uni2GoInfos ��getGenGo����
	 * @param lsUniGene2Go ����GO������һ������������һ�� lsUniGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��sepID  true: accID  false:uniID <br> 
	 * 1��GOID,GOID,GOID...<br> 
	 * ��ô�����β�ѯ�Ľ�������ȥ<br> 
	 * <b>ע������ǰҪ��uni2GoInfo�е�uniIDȥ�ظ�<b/>
	 */
	public static ArrayList<String[]>  getUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go,boolean sep,String GOClass)
	{
		getHashGo2Term();
		ArrayList<String[]> lsGoResult=new ArrayList<String[]>();
		//���û���ҵ����� ���� �û���û��Go����Ϣ��������
		if (uni2GoInfo==null||uni2GoInfo.getLsUniGOInfo()==null||uni2GoInfo.getLsUniGOInfo().size()==0) 
			return null;
		ArrayList<UniGene2Go> lsUniGoInfos = uni2GoInfo.getLsUniGOInfo();
		
		//����GeneID2Go�Ļ�����
		String[] strGene2Go=new String[2];
		if (sep) 
			strGene2Go[0]=uni2GoInfo.getQuaryID();
		else 
			strGene2Go[0]=uni2GoInfo.getUniID();
		
		strGene2Go[1]=""; int NumGO = 0;//���ר�����������õ��˼���GOID
		for (int j = 0; j < lsUniGoInfos.size(); j++) 
		{
			String[] tmpGeneGoInfo = hashGo2Term.get(lsUniGoInfos.get(j).getGOID());
			


			String[] uni2Go=new String[7];
			for (int k = 0; k < uni2Go.length; k++) {
				uni2Go[k]="";//����ֵ����Ϊ��ֵ
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
			/////////////////////////////////���û��symbol�Ļ����������һ��accID����ȥ//////////////////////////////////////////////////
			else 
				uni2Go[2] = AnnoQuery.getUniGenName(uni2Go[1]);
			
			if  (tmpGeneGoInfo != null && !tmpGeneGoInfo[1].trim().equals("") && (tmpGeneGoInfo[3].equals(GOClass) || GOClass.equals(""))) {
				uni2Go[4] = tmpGeneGoInfo[1];
				uni2Go[5] = tmpGeneGoInfo[2];
				uni2Go[6] = lsUniGoInfos.get(j).getEvidence();
				lsGoResult.add(uni2Go);
				//װ��lsUniGene2Go
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
	 * ��ʱ����
	 * ������getGenGoBlast����������Blast2GeneInfos�࣬����evalue����ֵ����������Ϣ����Ϊ
	 * ArrayList-String[6]
	 * ���У�<br>
	 * 0: queryID<br>
	 * 1: querySymbol<br>
	 * 2: subjectSymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 5: GO���Ŷ�<br>
	 * ���⣬����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * @param Blast2GeneInfo ��getGenGoBlast����������Blast2GeneInfos��
	 * @param evalue ���ƶȵ���ֵ
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	private static ArrayList<String[]> TmpcopeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,List<String[]> lsGene2Go)
	{
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		Gene2GoInfo Qgene2GoInfo = blast2GeneInfo.getQueryGene2GoInfo();
		Uni2GoInfo Quni2GoInfo = blast2GeneInfo.getQueryUniGene2GoInfo();
		Gene2GoInfo Sgene2GoInfo = blast2GeneInfo.getSubjectGene2GoInfo();
		Uni2GoInfo Suni2GoInfo = blast2GeneInfo.getSubjectUni2GoInfo();
		//����¼���ĸ����������Щ����Щû�У���һ�У�ÿ���Ƿ���ֵ1���� 0��null
		//�ڶ��У�ÿ����go�������ж���
		//��������ΪQgene2GoInfo��Quni2GoInfo��Sgene2GoInfo��Suni2GoInfo
		int[][] flag=new int[4][2];
		/////��ʼ������Ϊ0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////������ĸ������������Ҽ�¼��������ʱ��Ҫ����evalue///////////////////////////////////////////////////////////////////////////////////////
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
		///////////�������棬GO�����Ƕ�����////////////////////////////////////////////////////
		int max=flag[0][1];
		for (int j = 0; j < flag.length; j++) {
			
			if (max<flag[j][1]) 
				max=flag[j][1];
		}
		//�����û��GO���Ǻܿ��ܸû����û����ȥ��
		if (max==0) 
		{
			/**
			 * ����¼����Ϣ������������
			 */
			String[] tmpBlastInfo=new String[6];
			//����ֵ
			for (int j = 0; j < tmpBlastInfo.length; j++) {
				tmpBlastInfo[j]="";
			}
			if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
			{
				tmpBlastInfo[0] = blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
				GeneInfo tmpGeneInfo = blast2GeneInfo.getQueryGene2GoInfo().getGeneInfo();
				if(tmpGeneInfo != null && tmpGeneInfo.getSymbol() != null)
					tmpBlastInfo[1] = tmpGeneInfo.getSymbol().split("//")[0];
				/////////////////////////////////���û��symbol�Ļ����������һ��accID����ȥ//////////////////////////////////////////////////
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
			/////////////////////////û��go�ľͲ����������//////////////////////////////////////////////////////////////////
			//lsGoResult.add(tmpBlastInfo);
		}
		else {
			String[] gene2Go = new String[2]; gene2Go[0] = ""; gene2Go[1] = "";
			Set<String> hashGene2Go=new HashSet<String>();//�Ȱ�go�����������ȥ�ظ���Ȼ����װ��gene2Go
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
					//���û��symbol�Ļ����������һ��accID����ȥ
					else {
						symbol = AnnoQuery.getGenName(blast2GeneInfo.getQueryGene2GoInfo().getGeneId());
					}
					//��Ϊ������max��Ҳ��������go������һ��Ϊj����������Ҫ�ж�ĳ�������go�Ƿ����j����������
					if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().size()>j) 
					{
						if (blast2GeneInfo.getQueryGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
							String[] tmpBlastInfo=new String[6];
							//����ֵ
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
					//���û��symbol�Ļ����������һ��accID����ȥ
					else {
						symbol = AnnoQuery.getUniGenName(blast2GeneInfo.getQueryUniGene2GoInfo().getUniID());
					}
					if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().size()>j) 
					{
						if (blast2GeneInfo.getQueryUniGene2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
							String[] tmpBlastInfo=new String[6];
							//����ֵ
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
					//���û��symbol�Ļ����������һ��accID����ȥ
					else {
						subSymbol = AnnoQuery.getGenName(blast2GeneInfo.getSubjectGene2GoInfo().getGeneId());
					}
					if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo()!=null&&blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().size()>j) {
						if (blast2GeneInfo.getSubjectGene2GoInfo().getLsGOInfo().get(j).getFunction().equals(GOClass)) {
							String[] tmpBlastInfo=new String[6];
							//����ֵ
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
					//���û��symbol�Ļ����������һ��accID����ȥ
					else {
						subSymbol = AnnoQuery.getUniGenName(blast2GeneInfo.getSubjectUni2GoInfo().getUniID());
					}
					if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo()!=null&&blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().size()>j) {
						if (blast2GeneInfo.getSubjectUni2GoInfo().getLsUniGOInfo().get(j).getFunction().equals(GOClass)) {
							String[] tmpBlastInfo=new String[6];
							//����ֵ
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
				//��treeset�еĲ��ظ���geneIDװ�����ļ�
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
	 * ������getGenGoBlast����������Blast2GeneInfos�࣬����evalue����ֵ����������Ϣ����Ϊ
	 * ArrayList-String[7]
	 * ���У�<br>
	 * 0: GOID<br>
	 * 1: GOTerm<br>
	 * 2: GO���Ŷ�<br>
	 * 3: queryID<br>
	 * 4: queryGeneID<br>
	 * 5: querySymbol<br>
	 * 6: subjectSymbol<br>
	 * ���⣬����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * @param Blast2GeneInfo ��getGenGoBlast����������Blast2GeneInfos��
	 * @param evalue ���ƶȵ���ֵ
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 */
	//public static ArrayList<String[]> copeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,List<String[]> lsGene2Go)
	public static ArrayList<String[]> copeBlastInfoSimple(Gene2GoInfo Qgene2GoInfo,Uni2GoInfo Quni2GoInfo,
			Gene2GoInfo Sgene2GoInfo,Uni2GoInfo Suni2GoInfo,BlastInfo blastInfo,double evalue,String GOClass,
			boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		getHashGo2Term();
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		//����¼���ĸ����������Щ����Щû�У���һ�У�ÿ���Ƿ���ֵ1���� 0��null
		//�ڶ��У�ÿ����go�������ж���
		//��������ΪQgene2GoInfo��Quni2GoInfo��Sgene2GoInfo��Suni2GoInfo
		int[][] flag=new int[4][2];
		/////��ʼ������Ϊ0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////������ĸ������������Ҽ�¼��������ʱ��Ҫ����evalue///////////////////////////////////////////////////////////////////////////////////////
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
		///////////�������棬GO�����Ƕ�����////////////////////////////////////////////////////
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
/////////////��accID��symbol��subSymbol����///////////////////////////////////////////////////////////
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
			//���û��symbol�Ļ����������һ��accID����ȥ
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
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				symbol = AnnoQuery.getUniGenName(Quni2GoInfo.getUniID());
			}
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) 
		 {
			GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymbol()!=null)
				subSymbol = tmpGeneInfo.getSymbol().split("//")[0];
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				subSymbol = AnnoQuery.getGenName(Sgene2GoInfo.getGeneId());
			}
		 }
		else if (Suni2GoInfo!=null && blastInfo.getEvalue()<=evalue)
		{
			UniGeneInfo tmpUniGeneInfo=Suni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymbol()!=null)
				subSymbol = tmpUniGeneInfo.getSymbol().split("//")[0];
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				subSymbol = AnnoQuery.getUniGenName(Suni2GoInfo.getUniID());
			}
		}
		//////////////////////////////////////////////////////////////////////
		//�����û��GO���Ǻܿ��ܸû����û����ȥ��
		if (max==0) 
		{
			if (getNoGo) 
			{
				return null;
			}
			/**
			 * ����¼����Ϣ������������
			 */
			String[] tmpBlastInfo=new String[7];
			
			
			//����ֵ
			for (int j = 0; j < tmpBlastInfo.length; j++) {
				tmpBlastInfo[j]="";
			}
			tmpBlastInfo[3] = accID;
			tmpBlastInfo[4] = geneID;
			tmpBlastInfo[5] = symbol;
			tmpBlastInfo[6] = subSymbol;
			/////////////////////////û��go�ľͲ����������//////////////////////////////////////////////////////////////////
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
					if (Qgene2GoInfo.getGeneId() == 0) //���п���ֻ���½���Qgene2GoInfo��û��geneID��Ŀ
						gene2Go[0]=Qgene2GoInfo.getQuaryID();
					else 
						gene2Go[0]=Qgene2GoInfo.getGeneId()+"";
				}
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getUniID();
			}
			
			Set<String> hashGene2Go=new HashSet<String>();//�Ȱ�go�����������ȥ�ظ���Ȼ����װ��gene2Go
			for (int j = 0; j < max; j++) {
				if (Qgene2GoInfo!=null) 
				{
					//��Ϊ������max��Ҳ��������go������һ��Ϊj����������Ҫ�ж�ĳ�������go�Ƿ����j����������
					if (Qgene2GoInfo.getLsGOInfo()!=null&&Qgene2GoInfo.getLsGOInfo().size()>j) 
					{
						//��hash���л��go2term����Ϣ
						String[] GoInfo = hashGo2Term.get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//����ֵ
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
						//��hash���л��go2term����Ϣ
						String[] GoInfo = hashGo2Term.get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//����ֵ
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
						//��hash���л��go2term����Ϣ
						String[] GoInfo = hashGo2Term.get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//����ֵ
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
						//��hash���л��go2term����Ϣ
						String[] GoInfo = hashGo2Term.get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
						if (GoInfo != null && (GoInfo[3].equals(GOClass) || GoInfo[3].equals("")))
						{
							String[] tmpBlastInfo=new String[7];
							//����ֵ
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
				//��treeset�еĲ��ظ���geneIDװ�����ļ�
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
	 * ����lsBlast2GeneInfos���б�����evalue����ֵ����������Ϣ����Ϊ
	 * ArrayList-String[13]
	 * ���У�<br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: GOID<br>
	 * 4: GOTerm<br>
	 * 5: GO���Ŷ�<br>
	 * 6: blastEvalue<br>
	 * 7: taxID<br>
	 * 8: subjectGeneID<br>
	 * 9: subjectSymbol<br>
	 * 10: GOID<br>
	 * 11: GOTerm<br>
	 * 12: GO���Ŷ�<br>
	 *  * ���⣬����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
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
		//����¼���ĸ����������Щ����Щû�У���һ�У�ÿ���Ƿ���ֵ1���� 0��null
		//�ڶ��У�ÿ����go�������ж���
		//��������ΪQgene2GoInfo��Quni2GoInfo��Sgene2GoInfo��Suni2GoInfo
		int[][] flag=new int[4][2];
		/////��ʼ������Ϊ0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////������ĸ������������Ҽ�¼��������ʱ��Ҫ����evalue///////////////////////////////////////////////////////////////////////////////////////
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
		///////////�������棬GO�����Ƕ�����////////////////////////////////////////////////////
		int max=flag[0][1];
		for (int j = 0; j < flag.length; j++) {
			
			if (max<flag[j][1]) 
				max=flag[j][1];
		}
		//�����û��GO���Ǻܿ��ܸû����û����ȥ��
		if (max==0) 
		{
			/**
			 * ����¼����Ϣ������������
			 */
			String[] tmpBlastInfo=new String[13];
			//����ֵ
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
				//���û��symbol�Ļ����������һ��accID����ȥ
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
			/////////////////////////û��go�ľͲ����������//////////////////////////////////////////////////////////////////
			//lsGoResult.add(tmpBlastInfo);
		}
		else {
			String[] gene2Go=new String[2];gene2Go[0]="";gene2Go[1]="";
			Set<String> hashGene2Go=new HashSet<String>();//�Ȱ�go�����������ȥ�ظ���Ȼ����װ��gene2Go
			if (blast2GeneInfo.getQueryGene2GoInfo()!=null) 
				gene2Go[0]=blast2GeneInfo.getQueryGene2GoInfo().getQuaryID();
			else if (blast2GeneInfo.getQueryUniGene2GoInfo()!=null) 
				gene2Go[0]=blast2GeneInfo.getQueryUniGene2GoInfo().getQuaryID();

			for (int j = 0; j < max; j++) {
				String[] tmpBlastInfo=new String[13];
				//����ֵ
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
	 * @param Qgene2GoInfo  ����Ŀ���벻��Ϊnull���������ʱ����ĿΪnull������˵û���ҵ���Ӧ��geneID����ô����Ŀ��QueryID����ΪaccID
	 * @param Quni2GoInfo
	 * @param Sgene2GoInfo
	 * @param Suni2GoInfo
	 * @param blastInfo 
	 * @param evalue
	 * @param GOClass Go������ P: biological Process F:molecular Function C: cellular Component ���GOClassΪ""��ô��ѡ��ȫ��
	 * @param SepID �Ƿ�ID�ֿ�������ϲ�ID��Ҳ���Ǳ���ΪFalse�Ļ���lsGene2Go�ĵ�0���ΪgeneID������ΪaccID
	 * @param lsGene2Go
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: geneID <br>
	 * 1��GOID,GOID,GOID...<br>
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * @param getNoGo ����ЩIDû��Go���Ƿ����ID�������� true:������
	 * @return
	 * ����evalue����ֵ����������Ϣ����Ϊ
	 * ArrayList-String[15]
	 * ���У�<br>
	 * 0: queryID<br>
	 * 1: queryGeneID<br>
	 * 2: querySymbol<br>
	 * 3: Description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: GO���Ŷ�<br>
	 * 7: blastEvalue<br>
	 * 8: taxID<br>
	 * 9: subjectGeneID<br>
	 * 10: subjectSymbol<br>
	 * 11: Description<br>
	 * 12: GOID<br>
	 * 13: GOTerm<br>
	 * 14: GO���Ŷ�<br>
	 */
	public static ArrayList<String[]> copeBlastInfo(Gene2GoInfo Qgene2GoInfo,Uni2GoInfo Quni2GoInfo,
			Gene2GoInfo Sgene2GoInfo,Uni2GoInfo Suni2GoInfo,BlastInfo blastInfo,double evalue,String GOClass,
			boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		getHashGo2Term();
		ArrayList<String[]> lsGoResult = new ArrayList<String[]>();
		//����¼���ĸ����������Щ����Щû�У���һ�У�ÿ���Ƿ���ֵ1���� 0��null
		//�ڶ��У�ÿ����go�������ж���
		//��������ΪQgene2GoInfo��Quni2GoInfo��Sgene2GoInfo��Suni2GoInfo
		int[][] flag=new int[4][2];
		/////��ʼ������Ϊ0/////////////////////////////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < flag.length; j++) {
			for (int j2 = 0; j2 < flag[0].length; j2++) {
				flag[j][j2]=0;
			}
		}
		//////////////������ĸ������������Ҽ�¼��������ʱ��Ҫ����evalue///////////////////////////////////////////////////////////////////////////////////////
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
		///////////�������棬GO�����Ƕ�����////////////////////////////////////////////////////
		int max=flag[0][1];
		for (int j = 0; j < flag.length; j++) {
			
			if (max<flag[j][1]) 
				max=flag[j][1];
		}
		//�����û��GO���Ǻܿ��ܸû����û����ȥ��
		if (max==0) 
		{
			if (getNoGo) {
				return null;
			}
			/**
			 * ����¼����Ϣ������������
			 */
			String[] tmpBlastInfo=new String[15];
			//����ֵ
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
				//���û��symbol�Ļ����������һ��accID����ȥ
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
			/////////////////////////û��go�ľͲ����������//////////////////////////////////////////////////////////////////
			lsGoResult.add(tmpBlastInfo);
		}
		else
		{
			String[] gene2Go=new String[2];gene2Go[0]="";gene2Go[1]="";
			Set<String> hashGene2Go=new HashSet<String>();//�Ȱ�go�����������ȥ�ظ���Ȼ����װ��gene2Go
			if (SepID) {
				if (Qgene2GoInfo!=null) 
					gene2Go[0]=Qgene2GoInfo.getQuaryID();
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getQuaryID();
			}
			else {
				if (Qgene2GoInfo!=null )
				{
					if (Qgene2GoInfo.getGeneId() == 0) //���п���ֻ���½���Qgene2GoInfo��û��geneID��Ŀ
						gene2Go[0]=Qgene2GoInfo.getQuaryID();
					else 
						gene2Go[0]=Qgene2GoInfo.getGeneId()+"";
				}
				else if (Quni2GoInfo!=null) 
					gene2Go[0]=Quni2GoInfo.getUniID();
			}

			for (int j = 0; j < max; j++) {
				String[] tmpBlastInfo=new String[15];
				//����ֵ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
	 * @param ncbiid geneID���Ա�����
	 * @return
	 */
	public static ArrayList<Gene2Go> getGen2Go(NCBIID ncbiid)
	{
		long GeneID = ncbiid.getGeneId();
		return DaoFSGene2Go.queryGene2Go(GeneID);
	}
	
	/**
	 * @param uniProtID uniID���Ա�����
	 * @return
	 */
	public static ArrayList<UniGene2Go> getUniGen2Go(UniProtID uniProtID)
	{
		return DaoFSUniGene2Go.queryUniGene2Go(uniProtID.getUniID());
	}
	
	

}
