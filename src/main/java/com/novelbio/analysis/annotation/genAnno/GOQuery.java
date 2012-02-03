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
	 * ArrayList-String[7] <br>
���У�0: queryID<br>
1: uniID<br>
2: symbol<br>
3: description<br>
4: GOID<br>
5: GOTerm<br>
6: Evidence<br>
	 * <b>���û�ҵ����򷵻�null�����᷵��һ���յ�list</b><br>
	 * 	 * <b>��blastΪtrueʱ</b><br>
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
	 * <b>���û�ҵ�GO��Ϣ���򷵻�null�����᷵��һ���յ�list</b>
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
	 * ����queryID�����ظû����GO��Ϣ
	 * @param accID string[3]
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"
		1: accID
		2: ����ת����ID
	 * @param taxID ����ID���������Symbol������Ϊ0
	 * @param lsGene2Go ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ
	 * 0��accID
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @return ArrayList-String[7]
���У�0: queryID<br>
1: uniID<br>
2: symbol<br>
3: description<br>
4: GOID<br>
5: GOTerm<br>
6: Evidence<br>
	 * ���û�ҵ����򷵻�null�����᷵��һ���յ�list<br>
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
	 * ����Gene2GoInfo���б���������Ϣ����Ϊ
	 * ArrayList-String[7]<br>
	 * ���У�0: queryID<br>
	 * 1: geneID<br>
	 * 2: symbol<br>
	 * 3: description<br>
	 * 4: GOID<br>
	 * 5: GOTerm<br>
	 * 6: Evidence<br>
	 * ���û�ҵ����򷵻�null�����᷵��һ���յ�list<br>
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ<br>
	 * 0��sepID-true: accID sepID-false: geneID <br>
	 * 1��GOID,GOID,GOID...<br>
	 * ��ô�����β�ѯ�Ľ�������ȥ<br>
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null
	 * @param gene2GoInfo ��getGenGo����
	 * @param lsGene2Go
	 * ����һ�� lsGene2Go-Stirng[]������String[]�ĽṹΪ <br> 
	 * 0��sepID  true: accID  false:geneID <br> 
	 * 1��GOID,GOID,GOID...
	 * ��ô�����β�ѯ�Ľ�������ȥ
	 * ���Gene2GoInfo��û��GO��Ϣ����ô����null<br>
	 * <b>ע������ǰҪ��Gene2GoInfo�е�geneIDȥ�ظ�<b/>
	 * @param sep �Ƿ�ָ�ID����Ӧ��lsGene2Go������Ӧ�ı仯
	 * @param GOClass P��F��C�����Ϊ""��ô������Ҫ
	 */
	public static ArrayList<String[]> copeGenUni2GoInfo(Gene2GoInfo gene2GoInfo, ArrayList<String[]> lsGene2Go,boolean sep,String GOClass)
	{
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
			String[] tmpGeneGoInfo = ServGo.getHashGo2Term().get(lsGoInfos.get(j).getGOID());

			String[] gene2Go=new String[7];
			for (int k = 0; k < gene2Go.length; k++) {
				gene2Go[k]="";//����ֵ����Ϊ��ֵ
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
			/////////////////////////////////���û��symbol�Ļ����������һ��accID����ȥ//////////////////////////////////////////////////
			else 
				gene2Go[2] = ServAnno.getUniGenName(gene2Go[1]);
			
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
		if (lsGoResult.size() == 0) {
			return null;
		}
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
	 * ���û�ҵ����򷵻�null�����᷵��һ���յ�list<br>
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
	public static ArrayList<String[]>  copeGenUni2GoInfo(Uni2GoInfo uni2GoInfo,ArrayList<String[]> lsUniGene2Go,boolean sep,String GOClass)
	{
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
			String[] tmpGeneGoInfo = ServGo.getHashGo2Term().get(lsUniGoInfos.get(j).getGOID());
			


			String[] uni2Go=new String[7];
			for (int k = 0; k < uni2Go.length; k++) {
				uni2Go[k]="";//����ֵ����Ϊ��ֵ
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
			/////////////////////////////////���û��symbol�Ļ����������һ��accID����ȥ//////////////////////////////////////////////////
			else 
				uni2Go[2] = ServAnno.getUniGenName(uni2Go[1]);
			
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
		
		if (lsGoResult.size() == 0) {
			return null;
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
	public static ArrayList<String[]> copeBlastInfoSimple(Blast2GeneInfo blast2GeneInfo,double evalue,String GOClass,boolean SepID,List<String[]> lsGene2Go, boolean getNoGo)
	{
		Gene2GoInfo Qgene2GoInfo = blast2GeneInfo.getQueryGene2GoInfo();
		Uni2GoInfo Quni2GoInfo = blast2GeneInfo.getQueryUniGene2GoInfo();
		Gene2GoInfo Sgene2GoInfo = blast2GeneInfo.getSubjectGene2GoInfo();
		Uni2GoInfo Suni2GoInfo = blast2GeneInfo.getSubjectUni2GoInfo();
		BlastInfo blastInfo = blast2GeneInfo.getBlastInfo();
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
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
				symbol = tmpGeneInfo.getSymb().split("//")[0];
			//���û��symbol�Ļ����������һ��accID����ȥ
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
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				symbol = ServAnno.getUniGenName(Quni2GoInfo.getUniID());
			}
		}
		if (Sgene2GoInfo!=null&&blastInfo.getEvalue()<=evalue) 
		 {
			GeneInfo tmpGeneInfo=Sgene2GoInfo.getGeneInfo();
			if(tmpGeneInfo!=null&&tmpGeneInfo.getSymb()!=null)
				subSymbol = tmpGeneInfo.getSymb().split("//")[0];
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				subSymbol = ServAnno.getGenName(Sgene2GoInfo.getGeneId());
			}
		 }
		else if (Suni2GoInfo!=null && blastInfo.getEvalue()<=evalue)
		{
			UniGeneInfo tmpUniGeneInfo=Suni2GoInfo.getUniGeneInfo();
			if(tmpUniGeneInfo!=null&&tmpUniGeneInfo.getSymb()!=null)
				subSymbol = tmpUniGeneInfo.getSymb().split("//")[0];
			//���û��symbol�Ļ����������һ��accID����ȥ
			else {
				subSymbol = ServAnno.getUniGenName(Suni2GoInfo.getUniID());
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
						String[] GoInfo = ServGo.getHashGo2Term().get(Qgene2GoInfo.getLsGOInfo().get(j).getGOID());
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
						String[] GoInfo = ServGo.getHashGo2Term().get(Quni2GoInfo.getLsUniGOInfo().get(j).getGOID());
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
						String[] GoInfo = ServGo.getHashGo2Term().get(Sgene2GoInfo.getLsGOInfo().get(j).getGOID());
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
						String[] GoInfo = ServGo.getHashGo2Term().get(Suni2GoInfo.getLsUniGOInfo().get(j).getGOID());
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
	 * <b>���û�ҵ�GO��Ϣ���򷵻�null�����᷵��һ���յ�list</b>
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
				if(tmpGeneInfo != null && tmpGeneInfo.getSymb() != null)
				{
					tmpBlastInfo[2] = tmpGeneInfo.getSymb().split("//")[0];
					tmpBlastInfo[3] = tmpGeneInfo.getDescrp();
				}
				//���û��symbol�Ļ����������һ��accID����ȥ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
						//��hash���л��go2term����Ϣ
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
