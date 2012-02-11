package com.novelbio.database.updatedb.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.ServAnno;


public class RiceID{
	/**
	 * 将RapDB中Gff3文件中的Symbol与Description导入数据库
	 * 文件中含有%20C等符号，已经用url解码了
	 * @param fileName
	 * @throws Exception
	 */
	public static void upDateRapDBGeneInfo(String fileName) throws Exception {
		TxtReadandWrite txtReadGff = new TxtReadandWrite();
		txtReadGff.setParameter(fileName, false, true);
		BufferedReader reader = txtReadGff.readfile();
		
		String content = "";
		while ((content=reader.readLine())!=null) {
			String symbol = "";
			String description = "";
			String enc="utf8";//文件中含有%20C等符号，用url解码
			//如果是标题行
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//装载accID与相应数据库的list
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 0; i < tmpID.length; i++) 
				{
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);//文件中含有%20C等符号，用url解码
					if (tmpID[i].contains("ID=")){
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] = CopeID.removeDot(tmpAcc[i]);
							tmpRapID[1] = NovelBioConst.DBINFO_RICE_RAPDB;
							lsAccIDInfo.add(tmpRapID);
						}
					}
					else if (tmpID[i].contains("Name=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] = CopeID.removeDot( tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_RICE_RAPDB;
							lsAccIDInfo.add(tmpRapID);
						}
					}
					else if (tmpID[i].contains("Alias=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  CopeID.removeDot( tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_NCBIID;
							lsAccIDInfo.add(tmpRapID);
						}
					}
					else if (tmpID[i].contains("Gene_symbols=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  CopeID.removeDot(tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_SYMBOL;
							lsAccIDInfo.add(tmpRapID);
							
							if (symbol.trim().equals(""))
								symbol = tmpRapID[0];
							else
								symbol = symbol + "//" + tmpRapID[0];
							
						}
					}
					//OsID放在第一位
					else if (tmpID[i].contains("ID_converter=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  CopeID.removeDot(tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_RICE_IRGSP;
							lsAccIDInfo.add(0,tmpRapID);
						}
					}
					else if (tmpID[i].contains("NIAS_FLcDNA=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  CopeID.removeDot(tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_NIAS_FLCDNA;
							lsAccIDInfo.add(tmpRapID);
						}
					}
				
					else if (tmpID[i].contains("ORF_evidence=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  tmpAcc[j].replaceAll("\\(.*\\)", "").trim();
							tmpRapID[1] = NovelBioConst.DBINFO_UNIPROT_GenralID;
							lsAccIDInfo.add(tmpRapID);
						}
					}
					else if (tmpID[i].contains("Expression=")) {
						String tmp = tmpID[i].split("=")[1];
						String[] tmpAcc = tmp.split(",");
						for (int j = 0; j < tmpAcc.length; j++) {
							String[] tmpRapID =new String[2];
							tmpRapID[0] =  CopeID.removeDot(tmpAcc[j]);
							tmpRapID[1] = NovelBioConst.DBINFO_NCBIID;
							lsAccIDInfo.add(tmpRapID);
						}
					}
					else if (tmpID[i].contains("Note=")) {
						description = tmpID[i].split("=")[1];
					}
				}
				/////要么落在NCBIID中，要么落在UniProtID中，如果两个都没有，就装入UniProtID表中///////
				long GeneID = 0;
				String uniID = null;
				//////////获得geneID或UniProtID///
				for (String[] strings : lsAccIDInfo) 
				{
					NCBIID ncbiid = new NCBIID();
					ncbiid.setAccID(strings[0]); ncbiid.setTaxID(39947);
					ArrayList<NCBIID> lsNcbiid = MapNCBIID.queryLsNCBIID(ncbiid);
					if (lsNcbiid != null && lsNcbiid.size()>0)
					{
						GeneID = lsNcbiid.get(0).getGeneId();
						break;
					}
				}
				if (GeneID == 0) {
					for (String[] strings : lsAccIDInfo) 
					{
						UniProtID uniProtID = new UniProtID();
						uniProtID.setAccID(strings[0]);uniProtID.setTaxID(39947);
						ArrayList<UniProtID> lsUniProtIDs = MapUniProtIDOld.queryLsUniProtID(uniProtID);
						if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
						{
							uniID = lsUniProtIDs.get(0).getUniID();
							break;
						}
					}
				}
				
				if (symbol.trim().equals("") && description.trim().equals("")) {
					continue;
				}
				
				/////////倒入数据库//////////////
				if (GeneID !=0) {
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setGeneID(GeneID);
					GeneInfo geneInfo2 = MapGeneInfoOld.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) //数据库中没有，就插入
					{
						geneInfo.setSymb(symbol);
						geneInfo.setDescrp(description);
						MapGeneInfoOld.InsertGeneInfo(geneInfo);
					}
					else  //数据库中有
					{
						boolean flagUpdate = false;//判断最后是否升级数据库
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!geneInfo2.getSymb().contains(string)) {//如果含有新的symbol
								geneInfo2.setSymb(string+"//"+geneInfo2.getSymb());
								flagUpdate = true;
							}
						}
						if (!geneInfo2.getDescrp().contains(description)) {//如果含有新的description
							geneInfo2.setDescrp(description+"//"+geneInfo2.getDescrp());
							flagUpdate = true;
						}
						if (flagUpdate) {
							MapGeneInfoOld.upDateGeneInfo(geneInfo2);
						}
					}
				}
				else if (GeneID ==0 && uniID != null) {
					UniGeneInfo uniGeneInfo = new UniGeneInfo();
					uniGeneInfo.setUniProtID(uniID);
					UniGeneInfo uniGeneInfo2 = MapUniGeneInfoOld.queryUniGeneInfo(uniGeneInfo);
					if (uniGeneInfo2 == null) //数据库中没有，就插入
					{
						uniGeneInfo.setSymb(symbol);
						uniGeneInfo.setDescrp(description);
						MapUniGeneInfoOld.InsertUniGeneInfo(uniGeneInfo);
					}
					else  //数据库中有
					{
						boolean flagUpdate = false;//判断最后是否升级数据库
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!uniGeneInfo2.getSymb().contains(string)) {//如果含有新的symbol
								uniGeneInfo2.setSymb(string+"//"+uniGeneInfo2.getSymb());
								flagUpdate = true;
							}
						}
						if (!uniGeneInfo2.getDescrp().contains(description)) {//如果含有新的description
							uniGeneInfo2.setDescrp(description+"//"+uniGeneInfo2.getDescrp());
							flagUpdate = true;
						}
						
						if (flagUpdate) {
							MapUniGeneInfoOld.upDateUniGeneInfo(uniGeneInfo2);
						}
					}
				}
			}
		}
	}
	
	
	//////////以下开始导入Tigr的数据，包括TIGRID和GO和Description的信息，同时还要导入RapDB的Description信息//////////////////////////////
	/**
	 * 将TIGR的Gff文件导入NCBIID数据库或UniProt库，不导入geneInfo表
	 * @param gffTigrRice  tigrRice的gff文件
	 * @param outFile 没有GeneID的LOC基因，这个考虑导入UniProt表
	 * @param insertUniID true: 将没有搜到的项目插入UniProtID表，False：将没有搜到的项目输出到outFile
	 * 本项目第一次插入时先不用，先要将表都查找NCBIID和UniProtID,找不到的输出为outfile
	 * 等第一次各种查找都结束了，第二次再将outfile导入时，没有搜到的就可以导入UniProt表了
	 * @throws Exception  
	 */
	public static void tigrNCBIID(String gffTigrRice, String outFile,boolean insertUniID) throws Exception
	{
		//里面会出现 %20之类的乱码，需要用URLDecoder来进行解码
		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		txtOutFile.setParameter(outFile, true, false);
		
		TxtReadandWrite txtRapDB = new TxtReadandWrite();
		txtRapDB.setParameter(gffTigrRice, false, true);
		BufferedReader reader = txtRapDB.readfile();
		String content = "";
		
		while ((content = reader.readLine())!=null) {
			if (content.startsWith("#")) {
				continue;
			}
			
			String[] ss = content.split("\t");
			if (!ss[2].trim().equals("gene"))
				continue;
			
			String[] ssLOC = ss[8].split(";");
			String LOCID = ssLOC[ssLOC.length-1].split("=")[1];
			
			ArrayList<String> lsaccID = ServAnno.getNCBIUni(LOCID, 39947);
			
			String dbType = lsaccID.get(0);
			if (dbType.equals("geneID")) 
			{
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(Long.parseLong(lsaccID.get(i)), null, 39947,false, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
				}
			}
			if (dbType.equals("uniID")) {
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(0, lsaccID.get(i), 39947,false, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
				}
			}
			if (dbType.equals("accID")) {
				if (insertUniID) {
					ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(0,lsaccID.get(1), 39947,false, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
				}
				else {
					txtOutFile.writefile(content+"\n");
				}
			}
		}
		txtRapDB.close();
		txtOutFile.close();
	}
	
	
	
	/**
	 * 将TIGR的Gff文件导入geneInfo表
	 * @param gffTigrRice  tigrRice的gff文件
	 * @throws Exception  
	 */
	public static void tigrDescription(String gffTigrRice) throws Exception
	{
		//里面会出现 %20之类的乱码，需要用URLDecoder来进行解码
		String enc="utf8";
		
		TxtReadandWrite txtRapDB = new TxtReadandWrite();
		txtRapDB.setParameter(gffTigrRice, false, true);
		BufferedReader reader = txtRapDB.readfile();
		String content = "";
		
		while ((content = reader.readLine())!=null) {
			if (content.startsWith("#")) {
				continue;
			}
			
			String[] ss = content.split("\t");
			if (!ss[2].trim().equals("gene"))
				continue;
			
			String[] ssLOC = ss[8].split(";");
			String LOCID = ssLOC[2].split("=")[1];
	
			
			String description = URLDecoder.decode(ssLOC[1].split("=")[1], enc);//文件中含有%20C等符号，用url解码
			
			ArrayList<String> lsaccID = ServAnno.getNCBIUni(LOCID, 39947);
			
			String dbType = lsaccID.get(0);
			if (dbType.equals("geneID")) {
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setGeneID(Long.parseLong(lsaccID.get(i)));
					GeneInfo geneInfo2 = MapGeneInfoOld.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) {
						geneInfo.setSymb(LOCID); geneInfo.setDescrp(description);
						MapGeneInfoOld.InsertGeneInfo(geneInfo);
					}
					else 
					{
						boolean update = false;
						if (geneInfo2.getSymb().trim().equals("")) {//添加symbol
							geneInfo2.setSymb(LOCID);
							update = true;
						}
						if (!geneInfo2.getDescrp().contains(description)) {//添加description
							geneInfo2.setDescrp(geneInfo2.getDescrp()+"//"+description);
							update = true;
						}
						if (update) {
							MapGeneInfoOld.upDateGeneInfo(geneInfo2);
						}
					}
				}	
			}
			if (dbType.equals("uniID")) {
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					UniGeneInfo uniGeneInfo = new UniGeneInfo();
					uniGeneInfo.setUniProtID(lsaccID.get(i));
					UniGeneInfo uniGeneInfo2 = MapUniGeneInfoOld.queryUniGeneInfo(uniGeneInfo);
					if (uniGeneInfo2 == null) {
						uniGeneInfo.setSymb(LOCID); uniGeneInfo.setDescrp(description);
						MapUniGeneInfoOld.InsertUniGeneInfo(uniGeneInfo);
					}
					else 
					{
						boolean update = false;
						if (uniGeneInfo2.getSymb().trim().equals("")) {//添加symbol
							uniGeneInfo2.setSymb(LOCID);
							update = true;
						}
						if (!uniGeneInfo2.getDescrp().contains(description)) {//添加description
							uniGeneInfo2.setDescrp(uniGeneInfo2.getDescrp()+"//"+description);
							update = true;
						}
						if (update) {
							MapUniGeneInfoOld.upDateUniGeneInfo(uniGeneInfo2);
						}
					}
				}
			}
		}
		txtRapDB.close();
	}
	
	
	/**
	 * 将Tigr的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public static void tigrGO(String gffTigr) throws Exception 
	{
		TxtReadandWrite txtRapDB = new TxtReadandWrite();
		txtRapDB.setParameter(gffTigr, false, true);
		BufferedReader reader = txtRapDB.readfile();
		String content = "";	
		int mm=0;
		while ((content = reader.readLine()) != null) 
		{
			String[] ss = content.split("\t");
			String LocID = ss[0].trim();
			ArrayList<String> lsAccInfo = ServAnno.getNCBIUni(LocID, 39947);
			if (lsAccInfo.get(0).equals("geneID"))
			{
				for (int i = 1; i < lsAccInfo.size(); i++)
				{
					for (int j = 1; j < ss.length; j++) 
					{//每个GOID都装入
						UpDateFriceDB.upDateGenGO(Long.parseLong(lsAccInfo.get(i)), null, ss[j].trim(), NovelBioConst.DBINFO_RICE_TIGR);
					}
				}
			}
			else if (lsAccInfo.get(0).equals("uniID"))
			{
				for (int i = 1; i < lsAccInfo.size(); i++)
				{
					for (int j = 1; j < ss.length; j++) 
					{//每个GOID都装入
						UpDateFriceDB.upDateGenGO(0,lsAccInfo.get(i), ss[j].trim(), NovelBioConst.DBINFO_RICE_TIGR);
					}
				}
			}
			else {
				continue;
			}
		}
		System.out.println(mm);
		txtRapDB.close();
	}
}
/**
 * 导入repdb的信息，需要设定outtxt，也就是查不到的写入另一个文本
 * @author zong0jie
 *
 */
class RiceRapDB extends ImportPerLine
{	
	String enc="utf8";//文件中含有%20C等符号，用url解码
	private static Logger logger = Logger.getLogger(RiceRapDB.class);
	public void importInfoPerLine(String rapdbGFF, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, rapdbGFF);
		else 
			txtGene2Acc = new TxtReadandWrite(rapdbGFF, false);
		//从第二行开始读取
		int num = 0;
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id=")) {
				impPerLine(content);
			}
			num++;
			if (num%10000 == 0) {
				logger.info("import line number:" + num);
			}
		}
		impEnd();
		logger.info("finished import file " + rapdbGFF);
		txtGene2Acc.close();
		if (txtWriteExcep != null) {
			txtWriteExcep.close();
		}
	}
	/**
	 * 处理的时候记得将upDateNCBIUniID中的uniProtID代码的注释去掉，这里就是说如果导入NCBIID，那么就不将uniID导入NCBIID表了
	 * 将RapDB的Gff文件导入NCBIID数据库，仅仅导入ID，没有的倒入UniProt库
	 * 20110302更新程序
	 * @param gffRapDB  RAP_genes.gff3
	 * @param outFile 没有GeneID的LOC基因，这个考虑导入UniProt表
	 * @param insertUniID true: 将没有搜到的项目插入UniProtID表，False：将没有搜到的项目输出到outFile
	 * 本项目第一次插入时先不用，先要将表都查找NCBIID和UniProtID,找不到的输出为outfile
	 * 等第一次各种查找都结束了，第二次再将outfile导入时，没有搜到的就可以导入UniProt表了
	 * @throws Exception  
	 */
	@Override
	public boolean impPerLine(String lineContent) {
		String tmpInfo = lineContent.split("\t")[8];
		String[] tmpID = tmpInfo.split(";");
		//装载accID与相应数据库的list
		ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>(); //保存全部的要导入数据库的信息,自动去重复
		ArrayList<String> lsRefID = new ArrayList<String>(); //保存查找的信息，就是说譬如DBINFO_NIAS_FLCDNA等不用来查找
		for (int i = 0; i < tmpID.length; i++) 
		{
			try {
				tmpID[i] = URLDecoder.decode(tmpID[i], enc);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
			if (tmpID[i].contains("ID=")){
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[i];
					tmpRapID[1] = NovelBioConst.DBINFO_RICE_RAPDB;
					lsAccIDInfo.add(tmpRapID);
					lsRefID.add(tmpAcc[i]);
				}
			}
			else if (tmpID[i].contains("Name=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_RICE_RAPDB;
					lsAccIDInfo.add(tmpRapID);
					lsRefID.add(tmpAcc[i]);				}
			}
			else if (tmpID[i].contains("Alias=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =   tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_NCBIID;
					lsRefID.add(tmpAcc[i]);
				}
			}
			else if (tmpID[i].contains("Gene_symbols=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_SYMBOL;
					lsAccIDInfo.add(tmpRapID);
				}
			}
			//这个放到第一位，这样查询起来会比较好，也就是将OsID放到第一位
			else if (tmpID[i].contains("ID_converter=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_RICE_IRGSP;
					lsAccIDInfo.add(tmpRapID);
					lsRefID.add(0, tmpAcc[i]);
				}
			}
			else if (tmpID[i].contains("ORF_evidence=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j].replaceAll("\\(.*\\)", "").trim();
					tmpRapID[1] = NovelBioConst.DBINFO_UNIPROT_GenralID;
					lsRefID.add(tmpAcc[i]);
				}
			}
		}
		CopedID copedID = new CopedID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_GENEID)) {
			for (String[] strings : lsAccIDInfo) 
			{
				copedID.setUpdateAccID(strings[0]);
				copedID.setUpdateDBinfo(strings[1], true);
				copedID.update(false);
			}
			return true;
		}
		else {
			return false;
		}
	}
	
}
/**
 * 将Rap2MSU的信息--也就是Os2LOC的对照表--导入数据库,
 * 1. 导入RapDB的gff3文件 产生一个没有倒入的文件
 * 2. 导入Rap2MSU，产生一个没有倒入的文件
 * 3. 导入Tigr的gff文件，产生一个没有倒入的文件
 * 4. 依上面标号，按照2，1，3的顺序把没有导入的文件导入数据库，没有找到的导入uniID表中
 * @throws Exception 
 */
class RiceRap2MSU extends ImportPerLine
{
	@Override
	boolean impPerLine(String lineContent) {
		String[] tmpID = lineContent.split("\t");
		if (tmpID.length<2) //说明该IRGSPID没有LOCID与之对应，那么可以跳过
			return true;
		
		String[] tmpLOC = tmpID[1].split(",");
		/////////装入list///////////
		ArrayList<String[]> lstmpLOC = new ArrayList<String[]>();
		ArrayList<String> lsRef = new ArrayList<String>();
		String[] tmpLOC2Info1 = new String[2];
		tmpLOC2Info1[0] = tmpID[0]; tmpLOC2Info1[1] = NovelBioConst.DBINFO_RICE_IRGSP;
		lstmpLOC.add(tmpLOC2Info1);
		lsRef.add(tmpID[0]);
		for (String string : tmpLOC) {
			String[] tmpLOC2Info2 = new String[2];
			tmpLOC2Info2[0] = string; tmpLOC2Info2[1] = NovelBioConst.DBINFO_RICE_TIGR;
			lstmpLOC.add(tmpLOC2Info2);
			lsRef.add(tmpLOC2Info2[0]);
		}
		////////////////////////////
		CopedID copedID = new CopedID("", 39947);
		copedID.setUpdateRefAccID(lsRef);
		for (String[] strings : lstmpLOC) {
			copedID.setUpdateAccID(strings[0]);
			copedID.setUpdateDBinfo(strings[1], true);
			if (!copedID.update(false))
				return false;
		}
		return true;
	}
}

class RapDBGO extends ImportPerLine
{
	String enc="utf8";//文件中含有%20C等符号，用url解码
	Pattern pattern =Pattern.compile("\\((GO:\\d+)\\)", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。

	@Override
	public boolean impPerLine(String lineContent) {
		if (!(lineContent.contains("ID=")||lineContent.contains("Name=")||lineContent.contains("Alias=")||lineContent.contains("Gene_symbols=")||lineContent.contains("GO=")||lineContent.contains("Locus_id=")))
		{
			return true;
		}
			String tmpInfo = lineContent.split("\t")[8];
			String[] tmpID = tmpInfo.split(";");
			//装载accID与相应数据库的list
			long GeneID = 0; String uniID = null;
			ArrayList<String> lsRefID = new ArrayList<String>();
 			//先搜NCBIID看有没有
			for (int i = 0; i < tmpID.length; i++) 
			{
				try {
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}//文件中含有%20C等符号，用url解码
				if (tmpID[i].contains("ID=")||tmpID[i].contains("Name=")||tmpID[i].contains("Alias=")||tmpID[i].contains("Gene_symbols=")||tmpID[i].contains("Locus_id="))
				{
					String tmp = tmpID[i].split("=")[1];
					String tmpOsID= tmp.split(",")[0].trim();
					NCBIID ncbiid = new NCBIID();
					lsRefID.add(tmpOsID);
				}
			}
			CopedID copedID = new CopedID("", 39947);
			copedID.setUpdateRefAccID(lsRefID);
			if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
				return false;
			}
			
			matcher = pattern.matcher(lineContent);
			while (matcher.find()) {
				String tmpGOID = matcher.group(1);
				copedID.setUpdateGO(tmpGOID, NovelBioConst.DBINFO_RICE_RAPDB, "", "", "");
			}
		return copedID.update(false);
	}
}

class AffyID2LOC extends ImportPerLine
{

	@Override
	boolean impPerLine(String lineContent) {
		//TODO
		
	}
	
}
