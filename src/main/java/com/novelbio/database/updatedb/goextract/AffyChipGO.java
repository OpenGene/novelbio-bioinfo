package com.novelbio.database.updatedb.goextract;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.GOQuery;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniGene2Go;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServGo;
import com.novelbio.database.updatedb.idconvert.NCBIIDOperate;



public class AffyChipGO {
	/**
	 * 从affy芯片中获取GO信息,必须在affyid已经都导入了NCBIID和UniProtID后才能使用
	 * 保存格式如下
	 * geneID，GOID，GOterm，evidence,function
	 * @param affyInput
	 * @param rowstart
	 * @param output
	 * @param affyDBInfo affy探针的名字
	 * @throws Exception
	 */
	public static void getInfo(String affyInput,int rowstart, String outputNCBIGO,String outputUniGo,String affyDBInfo,int taxID) throws Exception {
		Hashtable<String, String> hashEvidence=new Hashtable<String, String>();
		 hashEvidence.put("Inferred from Experiment".toLowerCase(), "EXP");
		 hashEvidence.put("Inferred from Direct Assay".toLowerCase(), "IDA");
		 hashEvidence.put("Inferred from Physical Interaction".toLowerCase(), "IPI");
		 hashEvidence.put("Inferred from Mutant Phenotype".toLowerCase(), "IMP");
		 hashEvidence.put("Inferred from Genetic Interaction".toLowerCase(), "IGI");
		 hashEvidence.put("Inferred from Expression Pattern".toLowerCase(), "IEP");
		 hashEvidence.put("Inferred from Sequence or Structural Similarity".toLowerCase(), "ISS");
		 hashEvidence.put("Inferred from Sequence Orthology".toLowerCase(), "ISO");
		 hashEvidence.put("Inferred from Sequence Alignment".toLowerCase(), "ISA");
		 hashEvidence.put("Inferred from Sequence Model".toLowerCase(), "ISM");
		 hashEvidence.put("Inferred from Genomic Context".toLowerCase(), "IGC");
		 hashEvidence.put("inferred from Reviewed Computational Analysis".toLowerCase(), "RCA");
		 hashEvidence.put("Traceable Author Statement".toLowerCase(), "TAS");
		 hashEvidence.put("Non-traceable Author Statement".toLowerCase(), "NAS");
		 hashEvidence.put("Inferred by Curator".toLowerCase(), "IC");
		 hashEvidence.put("No biological Data available".toLowerCase(), "ND");
		 hashEvidence.put("Inferred from Electronic Annotation".toLowerCase(), "IEA");
		 hashEvidence.put("Not Recorded ".toLowerCase(), "NR");
		
		ExcelOperate excelAffy = new ExcelOperate();
		excelAffy.openExcel(affyInput);
		
		TxtReadandWrite txtAffyNCBIGo=new TxtReadandWrite();
		txtAffyNCBIGo.setParameter(outputNCBIGO, true,false);
		
		TxtReadandWrite txtAffyUniGo=new TxtReadandWrite();
		txtAffyUniGo.setParameter(outputUniGo, true,false);
		
		ArrayList<String[]> resultNCBI=new ArrayList<String[]>();
		ArrayList<String[]> resultUniProt=new ArrayList<String[]>();
		String[][] affyInfo = excelAffy.ReadExcel(rowstart, 1, excelAffy.getRowCount(), excelAffy.getColCount());

		for (int i = 0; i < affyInfo.length; i++) {
			ArrayList<String> lsAccID = ServAnno.getNCBIUni(affyInfo[i][0], taxID);
			if (lsAccID.get(0).equals("accID")) {
				continue;
			}
			if (lsAccID.get(0).equals("geneID")) 
			{
				//每个geneID都装一遍go
				for (int m = 1; m < lsAccID.size(); m++) 
				{
					if (affyInfo[i][30]!=null&&!affyInfo[i][30].contains("-")&&!affyInfo[i][30].trim().equals("")) {
						if (affyInfo[i][30].contains("///")) {
							String[] ss=affyInfo[i][30].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="P";tmp[5]=affyDBInfo;
								resultNCBI.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][30].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="P";tmp[5]=affyDBInfo;
							resultNCBI.add(tmp);
						}
					}
					
					
					if (affyInfo[i][31]!=null&&!affyInfo[i][31].contains("-")&&!affyInfo[i][31].trim().equals("")) {
						if (affyInfo[i][31].contains("///")) {
							String[] ss=affyInfo[i][31].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="C";tmp[5]=affyDBInfo;
								resultNCBI.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][31].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="C";tmp[5]=affyDBInfo;
							resultNCBI.add(tmp);
						}
					}
					
					if (affyInfo[i][32]!=null&&!affyInfo[i][32].contains("-")&&!affyInfo[i][32].trim().equals("")) {
						if (affyInfo[i][32].contains("///")) {
							String[] ss=affyInfo[i][32].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="F";tmp[5]=affyDBInfo;
								resultNCBI.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][32].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="F";tmp[5]=affyDBInfo;
							resultNCBI.add(tmp);
						}
					}
				}
			}
			if (lsAccID.get(0).equals("uniID"))
			{
				//每个geneID都装一遍go
				for (int m = 1; m < lsAccID.size(); m++) 
				{
					if (affyInfo[i][30]!=null&&!affyInfo[i][30].contains("-")&&!affyInfo[i][30].trim().equals("")) {
						if (affyInfo[i][30].contains("///")) {
							String[] ss=affyInfo[i][30].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="P";tmp[5]=affyDBInfo;
								resultUniProt.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][30].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="P";tmp[5]=affyDBInfo;
							resultUniProt.add(tmp);
						}
					}
					
					
					if (affyInfo[i][31]!=null&&!affyInfo[i][31].contains("-")&&!affyInfo[i][31].trim().equals("")) {
						if (affyInfo[i][31].contains("///")) {
							String[] ss=affyInfo[i][31].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="C";tmp[5]=affyDBInfo;
								resultUniProt.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][31].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="C";tmp[5]=affyDBInfo;
							resultUniProt.add(tmp);
						}
					}
					
					if (affyInfo[i][32]!=null&&!affyInfo[i][32].contains("-")&&!affyInfo[i][32].trim().equals("")) {
						if (affyInfo[i][32].contains("///")) {
							String[] ss=affyInfo[i][32].split("///");
							for (int j = 0; j < ss.length; j++) {
								String[] sssplit=ss[j].split("//");
								String[] tmp=new String[6];
								tmp[0]=lsAccID.get(m); 
								tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="F";tmp[5]=affyDBInfo;
								resultUniProt.add(tmp);
							}
						}
						else {
							String[] sssplit=affyInfo[i][32].split("//");
							String[] tmp=new String[6];
							tmp[0]=lsAccID.get(m); 
							tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="F";tmp[5]=affyDBInfo;
							resultUniProt.add(tmp);
						}
					}
				}
			}
		}
		txtAffyNCBIGo.ExcelWrite(resultNCBI, "\t", 1, 1);
		txtAffyUniGo.ExcelWrite(resultUniProt, "\t", 1, 1);
	}
	
	/**
	 * 专门将affyID的go插入数据库中
	 * @author zong0jie
	 *
	 */
	public static void upDateGenetoGo(String Affygene2GoFile) throws Exception
	{
		TxtReadandWrite txtgene2Go=new TxtReadandWrite();
		txtgene2Go.setParameter(Affygene2GoFile,false,true);
		BufferedReader gene2GoReader=txtgene2Go.readfile();
		HashMap<String, String[]> hashGo2Term = ServGo.getHashGo2Term();
		String content="";
		int i=0;
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			Gene2Go gene2GoInfo=new Gene2Go();
			gene2GoInfo.setGeneId((long)Double.parseDouble(ss[0]));
			String[] goIDinfo = hashGo2Term.get(ss[1]);
			String goID = goIDinfo[1];
			String goTerm = goIDinfo[2];
			String goFun = goIDinfo[3];
			gene2GoInfo.setGOID(goID);
			gene2GoInfo.setGOTerm(goTerm);
			gene2GoInfo.setEvidence(ss[3]);
			gene2GoInfo.setFunction(goFun);
			gene2GoInfo.setDataBase(ss[5]);
			
			Gene2Go gene2GoInfo2=(Gene2Go) MapGene2Go.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null)
			{
				MapGene2Go.InsertGene2Go(gene2GoInfo);
			}
			else {
				//如果已经含有相应的GOID，那么看evidence是否也含有了，没有的话就upDate
				if(gene2GoInfo2.getEvidence() != null && gene2GoInfo.getEvidence() != null && !gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"/"+gene2GoInfo2.getEvidence());
					MapGene2Go.upDateGene2Go(gene2GoInfo);
				}
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
	}
	
	/**
	 * 专门将affyID的go插入数据库中
	 * @author zong0jie
	 *
	 */
	public static void upDateGenetoUniGo(String Affygene2GoFile) throws Exception
	{
		TxtReadandWrite txtgene2Go=new TxtReadandWrite();
		txtgene2Go.setParameter(Affygene2GoFile,false,true);
		BufferedReader gene2GoReader=txtgene2Go.readfile();
		HashMap<String, String[]> hashGo2Term = ServGo.getHashGo2Term();
		String content="";
		int i=0;
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			UniGene2Go gene2GoInfo=new UniGene2Go();
			gene2GoInfo.setUniProtID(ss[0]);
			String[] goIDinfo = hashGo2Term.get(ss[1]);
			String goID = goIDinfo[1];
			String goTerm = goIDinfo[2];
			String goFun = goIDinfo[3];
			gene2GoInfo.setGOID(goID);
			gene2GoInfo.setGOTerm(goTerm);
			gene2GoInfo.setEvidence(ss[3]);
			gene2GoInfo.setFunction(goFun);
			gene2GoInfo.setDataBase(ss[5]);
			
			AGene2Go gene2GoInfo2=MapUniGene2Go.queryUniGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null)
			{
				MapUniGene2Go.InsertUniGene2Go(gene2GoInfo);
			}
			else {
				//如果已经含有相应的GOID，那么看evidence是否也含有了，没有的话就upDate
				if(gene2GoInfo2.getEvidence() != null && gene2GoInfo.getEvidence() != null && !gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"/"+gene2GoInfo2.getEvidence());
					MapUniGene2Go.upDateUniGene2Go(gene2GoInfo);
				}
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
	}
}
