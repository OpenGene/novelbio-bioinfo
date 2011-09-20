package com.novelbio.analysis.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.copeID.FisherTest;
import com.novelbio.analysis.annotation.copeID.ItemInfo;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KGen2Path;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KGng2Path;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.KEGGDAO.DaoKNIdKeg;
import com.novelbio.database.DAO.KEGGDAO.DaoKPathway;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;
import com.novelbio.database.service.ServAnno;

/**
 * �������򣬷���ÿ���������ڵ�Pathway��Pathway������������Pathway��������
 * @author zong0jie
 *
 */
public class PathEnrichNew {
	private static final Logger logger = Logger.getLogger(PathEnrichNew.class);
	
	/**
	 * ����ÿ��̽���Ӧ��symbol/accessID��description��pathway,�ʼ�õ���ʱ��û�ӱ���
	 * String[]
	 * 0: probeID <br>
	 * 1: geneID �ϲ�IDʱ���õ�<br>
	 * 2:pathID<br>
	 * 3: path_Title<br>
	 * 4: symbol/accessID<br>
	 * 5: description<br>
	 * 6: blastevalue<br>
	 * 7: blastTaxID<br>
	 * 8: subjectSymbol/accessID<br>
	 * 9: subject description<br>
	 */
	static ArrayList<String[]> lsGeneInfo;
	
	/**
	 * ��getPath2Kegʱ���ɣ����ص�ʱ�Ǹ������ArrayList<String[]> lsAcc2GenID
	 * �����к���pathway�Ļ���������Ϊfisher���������ֵ��
	 */
	static HashSet<String>hashGeneNum;
	
	public static HashSet<String> gethashGeneNum() {
		return hashGeneNum;
	}
	
	static String geneFile = "";
	
	public static void getPathRun(String geneFileXls, int QtaxID, int[] colID,boolean sepID, double up,double down,String[] prix,String bgFile,String resultExcel2003,boolean blast,double evalue,int subTaxID) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		geneFile = geneFileXls;
		int rowCount = excelGeneID.getRowCount();
		int colCount = excelGeneID.getColCount(2);
		String[][] geneID = excelGeneID.ReadExcel(2, 1,rowCount, colCount);
		
		ArrayList<String> lsGeneUp = new ArrayList<String>();
		ArrayList<String> lsGeneDown = new ArrayList<String>();
		for (int i = 0; i < geneID.length; i++) {
			if (colID[0] == colID[1]) {
				lsGeneUp.add(geneID[i][colID[0]]);
				continue;
			}
			else
			{
				if (Double.parseDouble(geneID[i][colID[1]])<=down) {
					lsGeneDown.add(geneID[i][colID[0]]);
				}
				else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
					lsGeneUp.add(geneID[i][colID[0]]);
				}
			}
		}
		ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(bgFile, 1, "\t");
		ArrayList<String> lsBGID = new ArrayList<String>();
		for (String[] strings : lsBGIDAll) {
			lsBGID.add(strings[0]);
		}
		ArrayList<String[]> lsGeneUpCope = CopeID.getGenID(prix[0],lsGeneUp, QtaxID,sepID);
		ArrayList<String[]> lsGeneDownCope = CopeID.getGenID(prix[1],lsGeneDown, QtaxID,sepID);
		ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG",lsBGID, QtaxID,sepID);
		
		HashMap<String, ArrayList<String[]>> hashBGgene = getPath2Keg(lsGeneBG,QtaxID,blast,subTaxID,evalue);
		int geneBackGroundNum = hashGeneNum.size();
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel2003);
		if (lsGeneUpCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult = getPathEnrich(prix[0],lsGeneUpCope, hashBGgene, sepID,QtaxID, blast, subTaxID,evalue,geneBackGroundNum);
			excelResult.WriteExcel(prix[0]+"PathAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[0]+"Gene2Path", 1, 1, lsResult.get(1), true);
		}
		if (lsGeneDownCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult = getPathEnrich(prix[1],lsGeneDownCope, hashBGgene, sepID,QtaxID, blast, subTaxID,evalue,geneBackGroundNum);
			excelResult.WriteExcel(prix[1]+"PathAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[1]+"Gene2Path", 1, 1, lsResult.get(1), true);
		}
	}
	
	
	
	
	/**
	 * @param lsGene
	 *  arrayList-string[3] :
0: ID���ͣ�"geneID"��"uniID"��"accID"
1: accID
2: ����ת����ID
	 * @param lsBG
	 *  arrayList-string[3] :
0: ID���ͣ�"geneID"��"uniID"��"accID"
1: accID
2: ����ת����ID
	 * @param sepID �Ƿ�ֿ�ID
	 * @param queryTaxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return û�оͷ���null <br> 
	 * ArrayList-ArrayList-String[]
	 * ����arraylist
	 * ��һ����lsFisherResult
	 * title[0]="PathID";title[1]="PathTitle";title[2]="DifGene";title[3]="AllDifGene";title[4]="GeneInPathID";
		title[5]="AllGene";title[6]="P-value";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
	 * �ڶ��������blast
	 * 			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			title2[5]="BlastEvalue";title2[6]="BlastTaxID";title2[7]="SubjectSymbol/AccessID";title2[8]="Subject Description";title2[9]="P-Value";
			title2[10]="FDR";title2[11]="Enrichment";title2[12]="(-log2P)";
			�����blast
			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			;title2[5]="P-Value";
			title2[6]="FDR";title2[7]="Enrichment";title2[8]="(-log2P)";
	 * 
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<String[]>> getPathEnrich(String condition,ArrayList<String[]> lsGene, HashMap<String, ArrayList<String[]>> hashBGgene,boolean sepID,int queryTaxID,boolean blast, int subTaxID,double evalue, int geneBackGroundNum) throws Exception
	{
		HashMap<String, ArrayList<String[]>> hashGene = getPath2Keg(lsGene, queryTaxID, blast, subTaxID, evalue);
		if (hashGene == null || hashGene.size() == 0) {
			return null;
		}
		int geneUpNum = hashGeneNum.size(); 
		ArrayList<String[]> lsThisGeneInfo = lsGeneInfo;
				
		ArrayList<String[]> lsFisherResult = FisherTest.getFisherResult(hashGene, geneUpNum, hashBGgene, geneBackGroundNum, 
				new ItemInfo() 
		{
			public String[] getItemName(String ItemID) {
				String[] tmpInfo = new String[1];
			    KGpathway path2Term=new KGpathway(); path2Term.setPathName(ItemID);
			    KGpathway path2Term2=DaoKPathway.queryKGpathway(path2Term);
			    tmpInfo[0]=path2Term2.getTitle();
				return tmpInfo;
			}
		});
		if (lsFisherResult == null) {
			logger.error("Hash���л����� Fisher����û�н�����ļ��� "+ geneFile+" �������� "+condition);
			return null;
		}
		ArrayList<String[]> lsPathInfoResult = ArrayOperate.combArrayListHash(lsFisherResult, lsThisGeneInfo, 0, 2);
		
		
			final int colpValue=lsThisGeneInfo.get(0).length+6;
		//����
        Collections.sort(lsPathInfoResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[colpValue]); Double b=Double.parseDouble(arg1[colpValue]);
                return a.compareTo(b);
            }
        });
        
        ArrayList<String[]> lsPathInfoResultFinal = CopeID.copeCombineID(condition,lsPathInfoResult, 1, 0, sepID);;

        //����ϲ�ID����ôҪ��ÿһ�������accID�Ե���Ӧ��geneIDǰ��
        int[] colNum = new int[7]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
        colNum[0] = 1;
        colNum[1] = lsThisGeneInfo.get(0).length+0;colNum[2] = lsThisGeneInfo.get(0).length+1;colNum[3] = lsThisGeneInfo.get(0).length+2;
		colNum[4] = lsThisGeneInfo.get(0).length+3;colNum[5] = lsThisGeneInfo.get(0).length+4;colNum[6] = lsThisGeneInfo.get(0).length+5;
		
		lsPathInfoResultFinal = ArrayOperate.listCope(lsPathInfoResultFinal, colNum, false);
	
        
        
        
        //�ӱ���
    	if (blast)
		{
			String[] title2=new String[13];
			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			title2[5]="BlastEvalue";title2[6]="BlastTaxID";title2[7]="SubjectSymbol/AccessID";title2[8]="Subject Description";title2[9]="P-Value";
			title2[10]="FDR";title2[11]="Enrichment";title2[12]="(-log2P)";
			lsPathInfoResultFinal.add(0,title2);
		}
		else
		{
			String[] title2=new String[13];
			title2[0]="AccessID";title2[1]="PathID";title2[2]="PathTitle";title2[3]="Symbol/AccessID";title2[4]="Description";
			title2[5]="BlastEvalue";title2[6]="BlastTaxID";title2[7]="SubjectSymbol/AccessID";title2[8]="Subject Description";title2[9]="P-Value";
			title2[10]="FDR";title2[11]="Enrichment";title2[12]="(-log2P)";
			int[] colNum2 = new int[4]; colNum2[0] = 5;colNum2[1] = 6;colNum2[2] = 7;colNum2[3] = 8;
			lsPathInfoResultFinal.add(0,title2);
			lsPathInfoResultFinal = ArrayOperate.listCope(lsPathInfoResultFinal, colNum2, false);
		}
    	
    	String[] title=new String[10];
		title[0]="PathID";title[1]="PathTitle";title[2]="DifGene";title[3]="AllDifGene";title[4]="GeneInPathID";
		title[5]="AllGene";title[6]="P-value";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		lsFisherResult.add(0,title);
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		lsResult.add(lsFisherResult);
		lsResult.add(lsPathInfoResultFinal);
		return lsResult;

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
	 * @param lsAcc2GenID
	 * arrayList-string[3] :<br>
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param queryTaxID ��������˻������ô����ָ������
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return
	 */
	public static HashMap<String, ArrayList<String[]>> getPath2Keg(ArrayList<String[]> lsAcc2GenID,int queryTaxID,boolean blast, int subTaxID,double evalue) 
	{
		lsGeneInfo = new ArrayList<String[]>();
		hashGeneNum =new HashSet<String>();
		HashMap<String, ArrayList<String[]>> hashPath2Gen = new HashMap<String, ArrayList<String[]>>();
		
		for (String[] tmpAcc2Gen : lsAcc2GenID) {
		    String dbType = tmpAcc2Gen[0];
		    String accID = tmpAcc2Gen[1];
		    String thisGeneID = tmpAcc2Gen[2];
		    if (!dbType.equals("uniID")) {
		    	boolean flagFind = false; //��accID�ҵ���path��flag�����û�ҵ��Ļ����ͽ���compound��������
		    	NCBIID ncbiid=new NCBIID();long geneID = 0;
		    	if (dbType.equals("geneID")) {
		    		geneID = Long.parseLong(tmpAcc2Gen[2]);
		    		ncbiid.setGeneId(geneID);
				}
		    	ncbiid.setAccID(accID); ncbiid.setTaxID(queryTaxID);
				KGen2Path kGen2Path = QKegPath.qKegPath(ncbiid, blast,subTaxID, evalue);
				String symbol = ""; String description = "";
				//�����������keggID
				if (kGen2Path.getKGCgen2Entry() != null
								&&kGen2Path.getKGCgen2Entry().getLsKGentries() != null
								     &&kGen2Path.getKGCgen2Entry().getLsKGentries().size() > 0)
				{
					//////////////////��ñ������geneInfo///////////////////////////////////
					String[] anno = ServAnno.getGenInfo(geneID);
					if (anno != null) {
						symbol = anno[0]; description = anno[1];
					}
					else {
						symbol = ""; description = "";
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
							String[] geneInfo = new String[10];
							geneInfo[0] = accID;
							geneInfo[1] = thisGeneID;
							geneInfo[2] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[2]); 
							geneInfo[3] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							geneInfo[4] = symbol;
							geneInfo[5] = description;
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0]=accID;tmpGeneInfoSub[1]=thisGeneID;
						FisherTest.addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
					flagFind = true;
				}
				//����blast�Ľ�������Ӧ�˶���pathway
				if (kGen2Path.getLsBlastgen2Entry() !=null
						&&kGen2Path.getLsBlastgen2Entry() != null
					     &&kGen2Path.getLsBlastgen2Entry().size() > 0) 
				{
					////////////////////////////ֻ�е�gene����pathwayʱ���ŻὫ�û�����м���///////////////////////////////////
					hashGeneNum.add(accID);
					//////////////////��ñ������geneInfo///////////////////////////////////
					String[] anno = ServAnno.getGenInfo(geneID);
					if (anno != null) {
						symbol = anno[0]; description = anno[1];
					}
					else {
						symbol = ""; description = "";
					}
					/////////////////////////////////////////////////////////////////////////////////////////
					String thisevalue = kGen2Path.getBlastInfo().getEvalue()+"";
					String subTax = subTaxID + "";
					String symbol2 = ""; String description2 = "";
					if (kGen2Path.getBlastInfo().getSubjectTab().equals("NCBIID")) {
						Long subGeneID = Long.parseLong(kGen2Path.getBlastInfo().getSubjectID());
						symbol2 = ""; description2 = "";
						String[] anno2 = ServAnno.getGenInfo(subGeneID);
						symbol2 = anno2[0]; description = anno2[1];
					}
					else if (kGen2Path.getBlastInfo().getSubjectTab().equals("UniprotID")) {
						String subUniID = kGen2Path.getBlastInfo().getSubjectID();
						symbol2 = ""; description2 = "";
						String[] anno2 = ServAnno.getUniGenInfo(subUniID);
						symbol2 = anno2[0]; description = anno2[1];
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
							String[] geneInfo = new String[10];
	
							geneInfo[0] = accID;
							geneInfo[1] = thisGeneID;
							geneInfo[2] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[2]); 
							geneInfo[3] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							geneInfo[4] = symbol;
							geneInfo[5] = description;
							geneInfo[6] = thisevalue;
							geneInfo[7] = subTax ;
							geneInfo[8] = symbol2;
							geneInfo[9] = description2;
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0] = accID;tmpGeneInfoSub[1] = thisGeneID;
						FisherTest.addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
					flagFind = true;
				}
				if (flagFind) {
					continue;
				}
			}
			//compound
		    if (dbType.equals("accID")) 
			{
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
							String[] geneInfo = new String[10];
							geneInfo[0] = accID;
							geneInfo[1] = thisGeneID;
							geneInfo[2] = lsKGentry.get(i).getPathName();
							//����pathway��Title
							KGpathway kGpathway = new KGpathway(); kGpathway.setPathName(geneInfo[2]); 
							geneInfo[3] = DaoKPathway.queryKGpathway(kGpathway).getTitle();
							geneInfo[4] = kGng2Path.getKgnCompInfo().getUsualName().split("//")[0];
							geneInfo[5] = kGng2Path.getKgnCompInfo().getComment();
							lsGeneInfo.add(geneInfo);
						}
						String[] tmpGeneInfoSub=new String[2];tmpGeneInfoSub[0]=accID;tmpGeneInfoSub[1]=thisGeneID;
						FisherTest.addHashInfo(hashPath2Gen,lsKGentry.get(i).getPathName(),tmpGeneInfoSub);
					}
				}
				continue;
			}
		    //UniProtID��ѯ
		    else if (dbType.equals("uniID")) {
				
			}
		    continue;
		}
		//System.out.println("pathEnrichNew.getPath2Keg error");
		return hashPath2Gen;
	}

	
}

