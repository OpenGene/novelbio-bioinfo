package com.novelBio.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.novelBio.annotation.pathway.kegg.pathEntity.KGen2Path;
import com.novelBio.annotation.pathway.kegg.pathEntity.KGng2Path;
import com.novelBio.annotation.pathway.kegg.prepare.KGprepare;
import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.ArrayOperate;


import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSGeneInfo;
import DAO.FriceDAO.DaoFSGo2Term;
import DAO.FriceDAO.DaoFSNCBIID;
import DAO.FriceDAO.DaoFSUniProtID;
import DAO.KEGGDAO.DaoKNIdKeg;
import DAO.KEGGDAO.DaoKPathway;

import entity.friceDB.GeneInfo;
import entity.friceDB.Go2Term;
import entity.friceDB.NCBIID;
import entity.friceDB.UniProtID;
import entity.kegg.KGentry;
import entity.kegg.KGpathway;
import entity.kegg.noGene.KGNIdKeg;

/**
 * �������򣬷���ÿ���������ڵ�Pathway��Pathway������������Pathway��������
 * @author zong0jie
 *
 */
public class PathEnrich {
	
	
	/**
	 * ����ÿ��̽���Ӧ��symbol/accessID��description��pathway,�ʼ�õ���ʱ��û�ӱ���
	 * String[]
	 * 0: probeID
	 * 1:pathID
	 * 2: path_Title
	 * 3: symbol/accessID
	 * 4: description
	 * 5: blastevalue
	 * 6: blastTaxID
	 * 7: subjectSymbol/accessID
	 * 8: subject description
	 */
	static ArrayList<String[]> lsGeneInfo;
	
	
	/**
	 * ��getPath2Kegʱ���ɣ����ص�ʱ�Ǹ������ArrayList<String[]> lsAcc2GenID
	 * �����к���pathway�Ļ���������Ϊfisher���������ֵ��
	 */
	static HashSet<String>hashGeneNum;
	
	
	
	public static void getPathRun(String geneFileXls, int QtaxID, int[] colID, double up,double down,String bgFile,String resultExcel2003,boolean blast,double evalue,int subTaxID) throws Exception
	{
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		String[][] accID = excelGeneID.ReadExcel(2, 1, excelGeneID.getRowCount(), excelGeneID.getColCount(2));
		ArrayList<String[]> lsUpGene =new ArrayList<String[]>();
		ArrayList<String[]> lsDownGene =new ArrayList<String[]>();
		ArrayList<String[]> lsBG =new ArrayList<String[]>();
		//��������Զ��жϣ����QtaxID<=0��ô�������symbol���������ж������ĸ�����
		for (String[] strings : accID) 
		{
			String accIDcope = KGprepare.removeDot(strings[colID[0]-1]);
			NCBIID ncbiid = new NCBIID(); ncbiid.setAccID(accIDcope);
			String geneID = "";
			if (QtaxID>0) 
			{
				ncbiid.setTaxID(QtaxID);
			}
			ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids!=null && lsNcbiids.size()>0) 
			{
				geneID = lsNcbiids.get(0).getGeneId() + "";
				if (QtaxID <= 0) {
					QtaxID = lsNcbiids.get(0).getTaxID();
				}
			}
			String[] tmpGeneInfo = new String[2];
			tmpGeneInfo[0] = accIDcope; tmpGeneInfo[1] = geneID;
			if (Double.parseDouble(strings[colID[1]-1]) >= up) 
			{
				lsUpGene.add(tmpGeneInfo);
			}
			else if(Double.parseDouble(strings[colID[1]-1]) <= down)
			{
				lsDownGene.add(tmpGeneInfo);
			}
		}
		
		String[] backGround = KGprepare.getAccID(bgFile, 1, 1);
		for (String strings : backGround) 
		{
			NCBIID ncbiid = new NCBIID(); ncbiid.setAccID(strings);
			String geneID = "";
			if (QtaxID>0) 
			{
				ncbiid.setTaxID(QtaxID);
			}
			ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids!=null && lsNcbiids.size()>0) 
			{
				geneID = lsNcbiids.get(0).getGeneId() + "";
				if (QtaxID <= 0) {
					QtaxID = lsNcbiids.get(0).getTaxID();
				}
			}
			String[] tmpGeneInfo = new String[2];
			tmpGeneInfo[0] = strings; tmpGeneInfo[1] = geneID;
			lsBG.add(tmpGeneInfo);
		}
		if (lsUpGene.size()>0) {
			getPathEnrich(lsUpGene, lsBG, QtaxID, blast, subTaxID,1e-10,resultExcel2003, "Up");
		}
		if (lsDownGene.size()>0) {
			getPathEnrich(lsDownGene, lsBG, QtaxID, blast, subTaxID,evalue,resultExcel2003, "Down");
		}
	}
	
	
	
	
	/**
	 * 
	 * @param lsGene
	 * @param lsBG
	 * @param queryTaxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix
	 * @throws Exception
	 */
	public static void getPathEnrich(ArrayList<String[]> lsGene, ArrayList<String[]> lsBG,int queryTaxID,boolean blast, int subTaxID,double evalue,String resultExcel2003,String prix) throws Exception
	{
		Hashtable<String, ArrayList<String[]>> hashGene = getPath2Keg(lsGene,queryTaxID,blast,subTaxID,evalue);
		int geneUpNum = hashGeneNum.size(); 
		ArrayList<String[]> lsThisGeneInfo = lsGeneInfo;

		Hashtable<String, ArrayList<String[]>> hashBGgene = getPath2Keg(lsBG,queryTaxID,blast,subTaxID,evalue);
		int geneBackGroundNum = hashGeneNum.size(); 
		ArrayList<String[]> lsResult = cope2HashForPvalue(hashGene,geneUpNum,hashBGgene,geneBackGroundNum);
		ArrayList<String[]> lsFisherResult = fisherTest(lsResult);
		ArrayList<String[]> lsPathInfoResult = ArrayOperate.combArrayListHash(lsFisherResult, lsThisGeneInfo, 0, 1);
		
		
		int[] colNum = new int[6]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		colNum[0] = lsThisGeneInfo.get(0).length+0;colNum[1] = lsThisGeneInfo.get(0).length+1;colNum[2] = lsThisGeneInfo.get(0).length+2;
		colNum[3] = lsThisGeneInfo.get(0).length+3;colNum[4] = lsThisGeneInfo.get(0).length+4;colNum[5] = lsThisGeneInfo.get(0).length+5;
		
		lsPathInfoResult = ArrayOperate.listCope(lsPathInfoResult, colNum, false);
		final int colpValue=lsThisGeneInfo.get(0).length+1;
		//����
        Collections.sort(lsPathInfoResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[colpValue]); Double b=Double.parseDouble(arg1[colpValue]);
                return a.compareTo(b);
            }
        });
        //�ӱ���
    	if (blast)
		{
			String[] title2=new String[13];
			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			title2[5]="BlastEvalue";title2[6]="BlastTaxID";title2[7]="SubjectSymbol/AccessID";title2[8]="Subject Description";title2[9]="P-Value";
			title2[10]="FDR";title2[11]="Enrichment";title2[12]="(-log2P)";
			lsPathInfoResult.add(0,title2);
		}
		else
		{
			String[] title2=new String[13];
			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			title2[5]="BlastEvalue";title2[6]="BlastTaxID";title2[7]="SubjectSymbol/AccessID";title2[8]="Subject Description";title2[9]="P-Value";
			title2[10]="FDR";title2[11]="Enrichment";title2[12]="(-log2P)";
			int[] colNum2 = new int[4]; colNum2[0] = 5;colNum2[1] = 6;colNum2[2] = 7;colNum2[3] = 8;
			lsGeneInfo = ArrayOperate.listCope(lsGeneInfo, colNum2, false);
			lsPathInfoResult.add(0,title2);
		}
    	
    	String[] title=new String[10];
		title[0]="PathID";title[1]="PathTitle";title[2]="DifGene";title[3]="AllDifGene";title[4]="GeneInPathID";
		title[5]="AllGene";title[6]="P-value";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		lsFisherResult.add(0,title);
		
		ExcelOperate excelGO = new ExcelOperate();
		excelGO.openExcel(resultExcel2003);
		excelGO.WriteExcel(prix+"PathAnalysis", 1, 1, lsFisherResult, true);
		excelGO.WriteExcel(prix+"Gene2Path", 1, 1, lsPathInfoResult, true);
	}
	

	static String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform/";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
	static String writeRFIle="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
	static String Rresult="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";

	/**
	 * ����fisher��Ҫ����Ϣ��������ϲ��󷵻�ΪArrayList - string�������pvalue����
	 * 0:pathID  1:pathTerm  2:difGene  3:AllDifGene  4:GeneInGoID  5:AllGene  6:Pvalue  7:FDR  8:enrichment  9:(-log2P) ;
	 * @param lsGOinfo
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<String[]> fisherTest(ArrayList<String[]> lsGOinfo) throws Exception {
		 
		TxtReadandWrite txtGoInfo=new TxtReadandWrite();
		txtGoInfo.setParameter(writeRFIle, true, false);
		int column[]=new int[4]; column[0]=2;column[1]=3;column[2]=4;column[3]=5;
		txtGoInfo.ExcelWrite(lsGOinfo, "\t", column, true, 1, 1);
		
		callR();
		
		TxtReadandWrite txtRresult=new TxtReadandWrite();
		txtRresult.setParameter(Rresult, false, true);
		
		String[][] RFisherResult=txtRresult.ExcelRead("\t", 2, 2, txtRresult.ExcelRows(), txtRresult.ExcelColumns(2, "\t"));
		ArrayList<String[]> lsFisherResult=new ArrayList<String[]>();
	
		
		for (int i = 0; i < lsGOinfo.size(); i++) {
			String[] tmp = lsGOinfo.get(i);
			String[] tmp2=new String[tmp.length+RFisherResult[i].length-4];
			for (int j = 0; j < tmp2.length; j++) {
				if( j<tmp.length)
				{
					tmp2[j]=tmp[j];
				}
				else 
				{
					tmp2[j]=RFisherResult[i][j-tmp.length+4];
				}
			}
			lsFisherResult.add(tmp2);
		
		}
		//����
        Collections.sort(lsFisherResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[6]); Double b=Double.parseDouble(arg1[6]);
                return a.compareTo(b);
            }
        });
		return lsFisherResult;
		//FileOperate.delFile(writeRFIle);
		//FileOperate.delFile(Rresult);
		
	}
	
	
	
	private static void callR() throws Exception{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+Rworkspace+ "GOfisherBHfdr.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
	
	
	
	
	/**
	 * ����lsAcc2GenID 0:AccID/geneID 1:geneID���Լ��費��Ҫ����blast�Ľ��<br>
	 * ����ÿ��pathway����Ӧ��gene�б�<br>
	 * key:pathID   value:arrayList: string[2] 0:accID/geneID  1:geneID<br>
	 * ��ʱ�Ȳ�����uniProt����Ϣ<br>
	 * ��getPath2Kegʱ<b>���hashGeneNum</b>�������ǵ�ʱ�Ǹ������ArrayList<String[]> lsAcc2GenID <br>
	 * ͬʱ<b>���lsGeneInfo</b>����ÿ��̽���Ӧ��symbol/accessID��description��pathway<br>
	 * �����к���pathway�Ļ���������Ϊfisher���������ֵ��
	 * @return
	 */
	private static Hashtable<String, ArrayList<String[]>> getPath2Keg(ArrayList<String[]> lsAcc2GenID,int queryTaxID,boolean blast, int subTaxID,double evalue) 
	{
		lsGeneInfo = new ArrayList<String[]>();
		hashGeneNum =new HashSet<String>();
		Hashtable<String, ArrayList<String[]>> hashPath2Gen=new Hashtable<String, ArrayList<String[]>>();
		
		for (String[] tmpAcc2Gen : lsAcc2GenID) {
		    String accID = tmpAcc2Gen[0];
		    String geneID = tmpAcc2Gen[1];
		    NCBIID ncbiid=new NCBIID();
		    boolean flagNcbi=true;
		    try {
		    	ncbiid.setGeneId(Integer.parseInt(geneID));
			} catch (Exception e) {
				//��ʱ�Ȳ�����UniProt����Ϣ
				flagNcbi=false;
			}
		    //�����ncbi��geneID�ţ���ô����ncbiidȥ�������ݿ⣬������UniProtIDȥ�������ݿ�
			if (flagNcbi) 
			{
				NCBIID ncbiidsub = DaoFSNCBIID.queryLsNCBIID(ncbiid).get(0);
				KGen2Path kGen2Path=QKegPath.qKegPath(ncbiidsub, blast,subTaxID, evalue);
				//�����������keggID
				if (kGen2Path.getKGCgen2Entry()!=null
								&&kGen2Path.getKGCgen2Entry().getLsKGentries()!=null
								     &&kGen2Path.getKGCgen2Entry().getLsKGentries().size()>0)
				{
					//////////////////��øû����geneInfo///////////////////////////////////
					GeneInfo tmpGeneInfo=new GeneInfo(); tmpGeneInfo.setGeneID(Long.parseLong(geneID));
					GeneInfo tmpGeneInfo2 =  DaoFSGeneInfo.queryGeneInfo(tmpGeneInfo);
					String symbol = ""; String description = "";
					if (tmpGeneInfo2 != null) {
						description = tmpGeneInfo2.getDescription();
						if ((symbol = tmpGeneInfo2.getSymbol().split("//")[0]).equals(""))
						{
							NCBIID ncbiid2 = new NCBIID(); ncbiid2.setGeneId(Long.parseLong(geneID));
							ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid2);
							symbol = lsNcbiids.get(0).getAccID(); 
						}
					}
					/////////////////////////////////////////////////////////////////////////////////////////
					
					//��ʼ�����geneID��Ӧ�˼���pathway
					ArrayList<KGentry> lsKGentry=kGen2Path.getKGCgen2Entry().getLsKGentries();
					//��Ϊһ��geneID�ܿ��ܲ�����һ��pathway�еĶ��entry����������Ҫ��pathwayȥ�ظ�
					HashSet<String> hashTmpPath = new HashSet<String>();
					
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////
					if (lsKGentry.size() > 0) {
						hashGeneNum.add(accID);
					}
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////

					for (int i = 0; i < lsKGentry.size(); i++) {
						//��hashTmpPath����һ��gene����Ӧ���ظ�pathwayȥ��
						if (hashTmpPath.contains(lsKGentry.get(i).getPathName())) {
							continue;
						}
						else {
							hashTmpPath.add(lsKGentry.get(i).getPathName());
							//����ÿ�������������pathway��װ�ػ�����Ϣ��pathway��Ϣ
							String[] geneInfo = new String[9];
							geneInfo[0] = accID;
							geneInfo[1] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[1]); 
							geneInfo[2] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							geneInfo[3] = symbol;
							geneInfo[4] = description;
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0]=accID;tmpGeneInfoSub[1]=geneID;
						addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
				}
				//����blast�Ľ�������Ӧ�˶���pathway
				if (kGen2Path.getLsBlastgen2Entry() !=null
						&&kGen2Path.getLsBlastgen2Entry() != null
					     &&kGen2Path.getLsBlastgen2Entry().size() > 0) 
				{
					
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////
						hashGeneNum.add(accID);
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////

					//////////////////��øû����geneInfo///////////////////////////////////
					GeneInfo tmpGeneInfo=new GeneInfo(); tmpGeneInfo.setGeneID(Long.parseLong(geneID));
					GeneInfo tmpGeneInfo2 =  DaoFSGeneInfo.queryGeneInfo(tmpGeneInfo);
					String symbol = ""; String description = "";
					if (tmpGeneInfo2 != null) {
						description = tmpGeneInfo2.getDescription();
						if ((symbol = tmpGeneInfo2.getSymbol().split("//")[0]).equals(""))
						{
							NCBIID ncbiid2 = new NCBIID(); ncbiid2.setGeneId(Long.parseLong(geneID));
							ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid2);
							symbol = lsNcbiids.get(0).getAccID(); 
						}
					}
					String thisevalue = kGen2Path.getBlastInfo().getEvalue()+"";
					String subTax = subTaxID + "";
					String subGeneID = kGen2Path.getBlastInfo().getSubjectID();
					
					
					GeneInfo qtmpGeneInfo=new GeneInfo(); qtmpGeneInfo.setGeneID(Long.parseLong(subGeneID));
					GeneInfo qtmpGeneInfo2 =  DaoFSGeneInfo.queryGeneInfo(qtmpGeneInfo);
					String symbol2 = ""; String description2 = "";
					if (tmpGeneInfo2 != null) {
						description2 = qtmpGeneInfo2.getDescription();
						if ((symbol2 = tmpGeneInfo2.getSymbol().split("//")[0]).equals(""))
						{
							NCBIID ncbiid2 = new NCBIID(); ncbiid2.setGeneId(Long.parseLong(subGeneID));
							ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid2);
							symbol2 = lsNcbiids.get(0).getAccID(); 
						}
					}
					/////////////////////////////////////////////////////////////////////////////////////////////////////////
					//��ʼ�����geneID��Ӧ�˼���pathway
					ArrayList<KGentry> lsKGentry=kGen2Path.getLsBlastgen2Entry();
					//��Ϊһ��geneID�ܿ��ܲ�����һ��pathway�еĶ��entry����������Ҫ��pathwayȥ�ظ�
					HashSet<String> hashTmpPath = new HashSet<String>();
					
					for (int i = 0; i < lsKGentry.size(); i++) {
						//��hashTmpPath����һ��gene����Ӧ���ظ�pathwayȥ��
						if (hashTmpPath.contains(lsKGentry.get(i).getPathName())) {
							continue;
						}
						else {
							hashTmpPath.add(lsKGentry.get(i).getPathName());
							//����ÿ�������������pathway��װ�ػ�����Ϣ��pathway��Ϣ
							String[] geneInfo = new String[9];
							geneInfo[0] = accID;
							geneInfo[1] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[1]); 
							geneInfo[2] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							
							geneInfo[3] = symbol;
							geneInfo[4] = description;
							geneInfo[5] = evalue + "";
							geneInfo[6] = subTaxID + "";
							geneInfo[7] = symbol2;
							geneInfo[8] = description2;
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0]=accID;tmpGeneInfoSub[1]=geneID;
						addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
				}
			}
			//compound�Լ�UniProtID��ѯ
			else 
			{
				if (accID.trim().equals("")) {
					continue;
				}
				//�����Ի������ѯ
				KGNIdKeg kgnIdKeg = new KGNIdKeg();
				kgnIdKeg.setUsualName(accID);
				KGNIdKeg kgnIdKegSub = DaoKNIdKeg.queryKGNIdKeg(kgnIdKeg);
				if (kgnIdKegSub != null) {
					KGng2Path kGng2Path=  QKegPath.qKegPath(queryTaxID, kgnIdKegSub);
					
					//��ʼ�����geneID��Ӧ�˼���pathway
					ArrayList<KGentry> lsKGentry=kGng2Path.getLsKGentry();
					//��Ϊһ��geneID�ܿ��ܲ�����һ��pathway�еĶ��entry����������Ҫ��pathwayȥ�ظ�
					HashSet<String> hashTmpPath = new HashSet<String>();
					
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////
					if (lsKGentry.size() > 0) {
						hashGeneNum.add(accID);
					}
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////

					for (int i = 0; i < lsKGentry.size(); i++) {
						//��hashTmpPath����һ��gene����Ӧ���ظ�pathwayȥ��
						if (hashTmpPath.contains(lsKGentry.get(i).getPathName())) {
							continue;
						}
						else {
							hashTmpPath.add(lsKGentry.get(i).getPathName());
							//����ÿ�������������pathway��װ�ػ�����Ϣ��pathway��Ϣ
							String[] geneInfo = new String[9];
							geneInfo[0] = accID;
							geneInfo[1] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[1]); 
							geneInfo[2] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							geneInfo[3] = kGng2Path.getKgnCompInfo().getUsualName().split("//")[0];
							geneInfo[4] = kGng2Path.getKgnCompInfo().getComment();
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0]=accID;tmpGeneInfoSub[1]=geneID;
						addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
				}
				else 
				{
					//TODO : UniProtID��ѯ
				}
			}
		}
		return hashPath2Gen;
	}
	
	/**
	 * ����Ѿ�������key����ô��tmpValue������listValue�ĺ���
	 * �������key����ô��value
	 * 	��� path-gene ��hash�������pathway�Ѿ�����
	 * @param hashPath2Gen
	 */
	private static void addHashInfo(Hashtable<String, ArrayList<String[]>> hashPath2Gen,String key, String[] tmpValue)
	{
		if (hashPath2Gen.containsKey(key)) {
			ArrayList<String[]> lsGeneID=hashPath2Gen.get(key);
			lsGeneID.add(tmpValue);
		}
		else	{
			ArrayList<String[]> lsGeneID=new ArrayList<String[]>();
			//��Ϣ������ȫ�棬��Ӿ���Ļ�����Ϣ
			lsGeneID.add(tmpValue);
			hashPath2Gen.put(key, lsGeneID);
		}
	}
	
	
	
	
	/**
	 * ��������hashtable��һ��Ϊ�������
	 * pathID---list-string[2] 0:accID/geneID  1:geneID<br>
	 * һ��Ϊ�ܻ���
	 * pathID---list-string[2] 0:accID/geneID  1:geneID<br>
	 * ע��������������ܻ�����
	 * 
	 * @return
	 * arrayList-string[6]
	 * 0:pathID
	 * 1:pathTerm
	 * 2:difInPathNum
	 * 3:difAllNum
	 * 4:PathGeneNum
	 * 5:allGene
	 */
	private static ArrayList<String[]> cope2HashForPvalue(Hashtable<String, ArrayList<String[]>> hashDif,int NumDif,Hashtable<String, ArrayList<String[]>> hashAll, int NumAll) {
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
		Enumeration<String> keysDif=hashDif.keys();
		//����ǽ���ļ������������½����
		//GOID,GOterm,��GOID�Ĳ������Num���ܲ������Num����GOID���ܻ���Num���ܻ����Num,
		while(keysDif.hasMoreElements()){
			String pathID=keysDif.nextElement();
			ArrayList<String[]> lsGeneID = hashDif.get(pathID);
		    String[] tmpResult=new String[6];
		    tmpResult[0]=pathID;
		    KGpathway path2Term=new KGpathway(); path2Term.setPathName(pathID);
		    KGpathway path2Term2=DaoKPathway.queryKGpathway(path2Term);
		    tmpResult[1]=path2Term2.getTitle();
		    tmpResult[2]=lsGeneID.size()+"";
		    tmpResult[3]=NumDif+"";
		    tmpResult[4]=hashAll.get(pathID).size()+"";
		    tmpResult[5]=NumAll+"";
		    lsResult.add(tmpResult);
		}
		return lsResult;
	}

	
}

