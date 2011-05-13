package com.novelbio.database.upDateDB.idConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.upDateDB.dataBase.UpDateFriceDB;


public class RiceID {
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
	public static void rapDBNCBIID(String gffRapDB,String outFile,boolean insertUniID) throws Exception 
	{
		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		txtOutFile.setParameter(outFile, true, false);
		
		
		TxtReadandWrite txtRapDB = new TxtReadandWrite();
		txtRapDB.setParameter(gffRapDB, false, true);
		BufferedReader reader = txtRapDB.readfile();
		String content = "";
		String enc = "utf8";
		while ((content = reader.readLine()) != null) 
		{
			content = URLDecoder.decode(content, enc);//文件中含有%20C等符号，用url解码
			//如果是标题行
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//装载accID与相应数据库的list
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 0; i < tmpID.length; i++) 
				{
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
						}
					}
					//这个放到第一位，这样查询起来会比较好，也就是将OsID放到第一位
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
				}
				/////要么落在NCBIID中，要么落在UniProtID中，如果两个都没有，就装入UniProtID表中///////
				long GeneID = 0;
				String uniID = null;
				////////////////获得geneID或UniProtID//////////
				for (String[] strings : lsAccIDInfo) 
				{
					//先设定uniprotID
					if (insertUniID && strings[1].equals(NovelBioConst.DBINFO_RICE_IRGSP)) {
						uniID = strings[0];
					}
					NCBIID ncbiid = new NCBIID();
					ncbiid.setAccID(strings[0]);ncbiid.setTaxID(39947);
					ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
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
						ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
						if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
						{
							uniID = lsUniProtIDs.get(0).getUniID();
							break;
						}
					}
				}
				//////////////////////////////////////////
				boolean insert = UpDateFriceDB.upDateNCBIUniID(GeneID, uniID, 39947, lsAccIDInfo, NovelBioConst.DBINFO_RICE_RAPDB,NovelBioConst.DBINFO_RICE_IRGSP);
				if (!insert) {
					txtOutFile.writefile(content + "\n");
				}
			}
		}
		txtOutFile.close();
		txtRapDB.close();
	}

	
	/**
	 * 将Rap2MSU的信息--也就是Os2LOC的对照表--导入数据库,先倒入RapDB的gff3文件用这个导一次，产生一个没有倒入的文件，
	 * 然后倒入Tigr的gff文件，再把没有倒入的文件倒入数据库
	 * 没有查到的放入 outFIle
	 * @throws Exception 
	 */
	public static void getRAP2MSU(String Rap2MSUFile,String outFile) throws Exception 
	{
		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		txtOutFile.setParameter(outFile, true, false);
		
		TxtReadandWrite txtRap2MSU = new TxtReadandWrite();
		txtRap2MSU.setParameter(Rap2MSUFile, false, true);
		BufferedReader reader = txtRap2MSU.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) 
		{
			String[] tmpID = content.split("\t");
			if (tmpID.length<2) //说明该IRGSPID没有LOCID与之对应，那么可以跳过
			{
				continue;
			}
			
			String[] tmpLOC = tmpID[1].split(",");
			
			/////////装入list///////////
			ArrayList<String[]> lstmpLOC = new ArrayList<String[]>();
			String[] tmpLOC2Info1 = new String[2];
			tmpLOC2Info1[0] = CopeID.removeDot(tmpID[0]);tmpLOC2Info1[1] = NovelBioConst.DBINFO_RICE_IRGSP;
			lstmpLOC.add(tmpLOC2Info1);
			
			for (String string : tmpLOC) {
				String[] tmpLOC2Info2 = new String[2];
				tmpLOC2Info2[0] = CopeID.removeDot(string);tmpLOC2Info2[1] = NovelBioConst.DBINFO_RICE_TIGR;
				lstmpLOC.add(tmpLOC2Info2);
			}
			////////////////////////////
			
			NCBIID ncbiid = new NCBIID();ncbiid.setAccID(CopeID.removeDot(tmpID[0])); ncbiid.setTaxID(39947);
			UniProtID uniProtID = new UniProtID(); uniProtID.setAccID(ncbiid.getAccID()); uniProtID.setTaxID(39947);
			long geneID = 0;  String uniID = null;
			
			//先搜索ncbiid表格,RapDB和Tigr都搜一下
			ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);//先用RapDB的ID搜，然后用TigrID搜
			if (lsNcbiid == null || lsNcbiid.size()<1) {
				ncbiid = new NCBIID();ncbiid.setAccID(lstmpLOC.get(1)[0]);
				lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			}
			////////////////////
			if (lsNcbiid != null && lsNcbiid.size()>0)//搜到了
			{
				geneID = lsNcbiid.get(0).getGeneId();
			}
			if (geneID == 0) //如果NCBIID中没有
			{
				//开始搜UniProtID表
				ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);//先用RapDB的ID搜，然后用TigrID搜
				if (lsUniProtIDs == null || lsUniProtIDs.size()<1) {
					uniProtID = new UniProtID(); uniProtID.setAccID(lstmpLOC.get(1)[0]);
					lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
				}
				if (lsUniProtIDs != null && lsUniProtIDs.size()>0) 
				{
					uniID = lsUniProtIDs.get(0).getUniID();
				}
				else//没搜到就记下来
				{
					txtOutFile.writefile(content+"\n");
					continue;
				}
			}
			//////开始倒入//////////////////////////////
			UpDateFriceDB.upDateNCBIUniID(geneID, uniID, 39947, lstmpLOC, NovelBioConst.DBINFO_RICE_IRGSP, NovelBioConst.DBINFO_RICE_TIGR);		
		}
		txtOutFile.close();
		txtRap2MSU.close();
	}
	
	/**
	 * 将RapDB的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * 用url进行了解码
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public static void rapDBGO(String gffRapDB) throws Exception 
	{
		TxtReadandWrite txtRapDB = new TxtReadandWrite();
		txtRapDB.setParameter(gffRapDB, false, true);
		BufferedReader reader = txtRapDB.readfile();
		String content = "";
		Pattern pattern =Pattern.compile("\\((GO:\\d+)\\)", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
		Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
		String enc="utf8";//文件中含有%20C等符号，用url解码
		int mm=0;
		while ((content = reader.readLine()) != null) 
		{
			
			//如果是标题行
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//装载accID与相应数据库的list
				long GeneID = 0; String uniID = null;
				//先搜NCBIID看有没有
				for (int i = 0; i < tmpID.length; i++) 
				{
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);//文件中含有%20C等符号，用url解码
					if (tmpID[i].contains("ID=")||tmpID[i].contains("Name=")||tmpID[i].contains("Alias=")||tmpID[i].contains("Gene_symbols=")||tmpID[i].contains("Locus_id="))
					{
						String tmp = tmpID[i].split("=")[1];
						String tmpOsID= tmp.split(",")[0].trim();
						NCBIID ncbiid = new NCBIID();
						ncbiid.setAccID(tmpOsID);ncbiid.setTaxID(39947);
						ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
						if (lsNcbiid != null && lsNcbiid.size()>0)
						{
							GeneID = lsNcbiid.get(0).getGeneId();
							break;
						}
					}
				}
				//没有的话就搜UniProtID表
				if (GeneID == 0) 
				{
					for (int i = 0; i < tmpID.length; i++) 
					{
						if (tmpID[i].contains("ID=")||tmpID[i].contains("Name=")||tmpID[i].contains("Alias=")||tmpID[i].contains("Gene_symbols=")||tmpID[i].contains("Locus_id="))
						{
							String tmp = tmpID[i].split("=")[1];
							String tmpOsID= tmp.split(",")[0];
							UniProtID uniProtID = new UniProtID();
							uniProtID.setAccID(tmpOsID); uniProtID.setTaxID(39947);
							ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
							if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
							{
								uniID = lsUniProtIDs.get(0).getUniID();
								break;
							}
						}
					}
				}
				//都没找到，就跳过
				if (GeneID == 0 && uniID ==null) {
					continue;
				}
				matcher = pattern.matcher(content);
				while (matcher.find()) {
					String tmpGOID = matcher.group(1);
					UpDateFriceDB.upDateGenGO(GeneID, uniID, tmpGOID, NovelBioConst.DBINFO_RICE_RAPDB);
					mm++;
				}
			}
		}
		System.out.println(mm);
		txtRapDB.close();
	}
	
	/**
	 * 将以前整理的affyidtolocid的信息导入数据库，能导入NCBIID的导入NCBIID，不能的导入UniProtID
	 * 没有查到的放入 outFIle
	 * @throws Exception 
	 */
	public static void getAffyID2LOC(String affyidtolocid,String outFile) throws Exception {

		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		txtOutFile.setParameter(outFile, true, false);
		
		TxtReadandWrite txtRap2MSU = new TxtReadandWrite();
		txtRap2MSU.setParameter(affyidtolocid, false, true);
		
		BufferedReader reader = txtRap2MSU.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) 
		{
			String[] tmpID = content.split("\t");
			if (tmpID.length<2) {
				continue;
			}
			//获得 geneID 或 uniID
			long geneID = 0; String uniID = null;
			for (int i = 1; i < tmpID.length; i++) {
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(CopeID.removeDot(tmpID[i])); ncbiid.setTaxID(39947);
				ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
				if (lsNcbiid != null && lsNcbiid.size()>0)
				{
					geneID = lsNcbiid.get(0).getGeneId();
					break;
				}
			}
			if (geneID ==0 ) 
			{
				for (int i = 1; i < tmpID.length; i++) {
					UniProtID uniProtID = new UniProtID();
					uniProtID.setAccID(CopeID.removeDot(tmpID[i])); uniProtID.setTaxID(39947);
					ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
					if (lsUniProtIDs != null && lsUniProtIDs.size()>0)
					{
						uniID = lsUniProtIDs.get(0).getUniID();
						break;
					}
				}
			}
			
			ArrayList<String[]> lstmpLOC = new ArrayList<String[]>();
			for (int i = 0; i < tmpID.length; i++) 
			{
				String[] tmpLOC2Info2 = new String[2];
				tmpLOC2Info2[0] = CopeID.removeDot(tmpID[i]);
				if (i == 0) {
					tmpLOC2Info2[1] = NovelBioConst.DBINFO_AFFY_RICE_31;
				}
				else if (i == 1) {
					tmpLOC2Info2[1] = NovelBioConst.DBINFO_NCBIID;
				}
				else if (i == 2) {
					tmpLOC2Info2[1] = NovelBioConst.DBINFO_RICE_TIGR;
				}
				lstmpLOC.add(tmpLOC2Info2);
			}
			//////////////////////////////////////////
			boolean insert = UpDateFriceDB.upDateNCBIUniID(geneID, uniID, 39947, lstmpLOC, NovelBioConst.DBINFO_AFFY_RICE_31,NovelBioConst.DBINFO_RICE_RAPDB,NovelBioConst.DBINFO_RICE_IRGSP);
			if (!insert) {
				txtOutFile.writefile(content + "\n");
			}
		}
		txtOutFile.close();
		txtRap2MSU.close();
	}
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
					ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
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
						ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
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
					GeneInfo geneInfo2 = DaoFSGeneInfo.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) //数据库中没有，就插入
					{
						geneInfo.setSymbol(symbol);
						geneInfo.setDescription(description);
						DaoFSGeneInfo.InsertGeneInfo(geneInfo);
					}
					else  //数据库中有
					{
						boolean flagUpdate = false;//判断最后是否升级数据库
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!geneInfo2.getSymbol().contains(string)) {//如果含有新的symbol
								geneInfo2.setSymbol(string+"//"+geneInfo2.getSymbol());
								flagUpdate = true;
							}
						}
						if (!geneInfo2.getDescription().contains(description)) {//如果含有新的description
							geneInfo2.setDescription(description+"//"+geneInfo2.getDescription());
							flagUpdate = true;
						}
						if (flagUpdate) {
							DaoFSGeneInfo.upDateGeneInfo(geneInfo2);
						}
					}
				}
				else if (GeneID ==0 && uniID != null) {
					UniGeneInfo uniGeneInfo = new UniGeneInfo();
					uniGeneInfo.setGeneID(uniID);
					UniGeneInfo uniGeneInfo2 = DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
					if (uniGeneInfo2 == null) //数据库中没有，就插入
					{
						uniGeneInfo.setSymbol(symbol);
						uniGeneInfo.setDescription(description);
						DaoFSUniGeneInfo.InsertUniGeneInfo(uniGeneInfo);
					}
					else  //数据库中有
					{
						boolean flagUpdate = false;//判断最后是否升级数据库
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!uniGeneInfo2.getSymbol().contains(string)) {//如果含有新的symbol
								uniGeneInfo2.setSymbol(string+"//"+uniGeneInfo2.getSymbol());
								flagUpdate = true;
							}
						}
						if (!uniGeneInfo2.getDescription().contains(description)) {//如果含有新的description
							uniGeneInfo2.setDescription(description+"//"+uniGeneInfo2.getDescription());
							flagUpdate = true;
						}
						
						if (flagUpdate) {
							DaoFSUniGeneInfo.upDateUniGeneInfo(uniGeneInfo2);
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
			String LOCID = ssLOC[2].split("=")[1];
			
			ArrayList<String> lsaccID = AnnoQuery.getNCBIUni(LOCID, 39947);
			
			String dbType = lsaccID.get(0);
			if (dbType.equals("geneID")) 
			{
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(Long.parseLong(lsaccID.get(i)), null, 39947, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
				}
			}
			if (dbType.equals("uniID")) {
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(0, lsaccID.get(i), 39947, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
				}
			}
			if (dbType.equals("accID")) {
				if (insertUniID) {
					ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
					String[] tmpAccIDInfo = new String[2];
					tmpAccIDInfo[0] = LOCID; tmpAccIDInfo[1] = NovelBioConst.DBINFO_RICE_TIGR;
					lsAccIDInfo.add(tmpAccIDInfo);
					UpDateFriceDB.upDateNCBIUniID(0,lsaccID.get(1), 39947, lsAccIDInfo, NovelBioConst.DBINFO_RICE_TIGR);
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
			
			ArrayList<String> lsaccID = AnnoQuery.getNCBIUni(LOCID, 39947);
			
			String dbType = lsaccID.get(0);
			if (dbType.equals("geneID")) {
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setGeneID(Long.parseLong(lsaccID.get(i)));
					GeneInfo geneInfo2 = DaoFSGeneInfo.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) {
						geneInfo.setSymbol(LOCID); geneInfo.setDescription(description);
						DaoFSGeneInfo.InsertGeneInfo(geneInfo);
					}
					else 
					{
						boolean update = false;
						if (geneInfo2.getSymbol().trim().equals("")) {//添加symbol
							geneInfo2.setSymbol(LOCID);
							update = true;
						}
						if (!geneInfo2.getDescription().contains(description)) {//添加description
							geneInfo2.setDescription(geneInfo2.getDescription()+"//"+description);
							update = true;
						}
						if (update) {
							DaoFSGeneInfo.upDateGeneInfo(geneInfo2);
						}
					}
				}	
			}
			if (dbType.equals("uniID")) {
				for (int i = 1; i < lsaccID.size(); i++) 
				{
					UniGeneInfo uniGeneInfo = new UniGeneInfo();
					uniGeneInfo.setGeneID(lsaccID.get(i));
					UniGeneInfo uniGeneInfo2 = DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
					if (uniGeneInfo2 == null) {
						uniGeneInfo.setSymbol(LOCID); uniGeneInfo.setDescription(description);
						DaoFSUniGeneInfo.InsertUniGeneInfo(uniGeneInfo);
					}
					else 
					{
						boolean update = false;
						if (uniGeneInfo2.getSymbol().trim().equals("")) {//添加symbol
							uniGeneInfo2.setSymbol(LOCID);
							update = true;
						}
						if (!uniGeneInfo2.getDescription().contains(description)) {//添加description
							uniGeneInfo2.setDescription(uniGeneInfo2.getDescription()+"//"+description);
							update = true;
						}
						if (update) {
							DaoFSUniGeneInfo.upDateUniGeneInfo(uniGeneInfo2);
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
			ArrayList<String> lsAccInfo = AnnoQuery.getNCBIUni(LocID, 39947);
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
