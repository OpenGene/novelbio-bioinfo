package com.novelbio.database.upDateDB.gOextract;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;



public class AffyChipGO {
	/**
	 * 从affy芯片中获取GO信息
	 * 保存格式如下
	 * geneID，GOID，GOterm，evidence,function
	 * @param affyInput
	 * @param rowstart
	 * @param output
	 * @param affyDBInfo affy探针的名字
	 * @throws Exception
	 */
	public static void getInfo(String affyInput,int rowstart, String output,String affyDBInfo) throws Exception {
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
		
		TxtReadandWrite txtAffyInfo=new TxtReadandWrite();
		txtAffyInfo.setParameter(output, true,false);
		ArrayList<String[]> result=new ArrayList<String[]>();
		String[][] affyInfo = excelAffy.ReadExcel(rowstart, 1, excelAffy.getRowCount(), excelAffy.getColCount());

		for (int i = 0; i < affyInfo.length; i++) {
			if (affyInfo[i][18]==null||affyInfo[i][18].contains("-")||affyInfo[i][18].trim().equals("")) {
				continue;
			}
	
			if (affyInfo[i][30]!=null&&!affyInfo[i][30].contains("-")&&!affyInfo[i][30].trim().equals("")) {
				if (affyInfo[i][30].contains("///")) {
					String[] ss=affyInfo[i][30].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] sssplit=ss[j].split("//");
						String[] tmp=new String[6];
						tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
						tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Process";tmp[5]=affyDBInfo;
						result.add(tmp);
					}
				}
				else {
					String[] sssplit=affyInfo[i][30].split("//");
					String[] tmp=new String[6];
					tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
					tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Process";tmp[5]=affyDBInfo;
					result.add(tmp);
				}
			}
			
			
			if (affyInfo[i][31]!=null&&!affyInfo[i][31].contains("-")&&!affyInfo[i][31].trim().equals("")) {
				if (affyInfo[i][31].contains("///")) {
					String[] ss=affyInfo[i][31].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] sssplit=ss[j].split("//");
						String[] tmp=new String[6];
						tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
						tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Component";tmp[5]=affyDBInfo;
						result.add(tmp);
					}
				}
				else {
					String[] sssplit=affyInfo[i][31].split("//");
					String[] tmp=new String[6];
					tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
					tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Component";tmp[5]=affyDBInfo;
					result.add(tmp);
				}
			}
			
			if (affyInfo[i][32]!=null&&!affyInfo[i][32].contains("-")&&!affyInfo[i][32].trim().equals("")) {
				if (affyInfo[i][32].contains("///")) {
					String[] ss=affyInfo[i][32].split("///");
					for (int j = 0; j < ss.length; j++) {
						String[] sssplit=ss[j].split("//");
						String[] tmp=new String[6];
						tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
						tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Function";tmp[5]=affyDBInfo;
						result.add(tmp);
					}
				}
				else {
					String[] sssplit=affyInfo[i][32].split("//");
					String[] tmp=new String[6];
					tmp[0]=affyInfo[i][18].split("///")[0].trim(); 
					tmp[1]="GO:"+sssplit[0].trim();   tmp[2]=sssplit[1].trim()  ;tmp[3]=hashEvidence.get(sssplit[2].trim().toLowerCase());tmp[4]="Function";tmp[5]=affyDBInfo;
					result.add(tmp);
				}
			}
			
			
		}
		txtAffyInfo.ExcelWrite(result, "\t", 1, 1);
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
		
		DaoFSGene2Go friceDAO=new DaoFSGene2Go();
		
		String content="";
		int i=0;
		while ((content=gene2GoReader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			Gene2Go gene2GoInfo=new Gene2Go();
			gene2GoInfo.setGeneId((long)Double.parseDouble(ss[0]));
			gene2GoInfo.setGOID(ss[1]);
			gene2GoInfo.setGOTerm(ss[2]);
			gene2GoInfo.setEvidence(ss[3]);
			gene2GoInfo.setFunction(ss[4]);
			gene2GoInfo.setDataBase(ss[5]);
			
			Gene2Go gene2GoInfo2=friceDAO.queryGene2Go(gene2GoInfo);
			if (gene2GoInfo2==null)
			{
				friceDAO.InsertGene2Go(gene2GoInfo);
			}
			else {
				//如果已经含有相应的GOID，那么看evidence是否也含有了，没有的话就upDate
				if(!gene2GoInfo2.getEvidence().contains(gene2GoInfo.getEvidence()))
				{
					gene2GoInfo.setEvidence(gene2GoInfo.getEvidence()+"/"+gene2GoInfo2.getEvidence());
					friceDAO.upDateGene2Go(gene2GoInfo);
				}
			}
			i++;
			if (i%10000==0) {
				System.out.println(i);
			}
		}
	}
}
