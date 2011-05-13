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
	 * �����ʱ��ǵý�upDateNCBIUniID�е�uniProtID�����ע��ȥ�����������˵�������NCBIID����ô�Ͳ���uniID����NCBIID����
	 * ��RapDB��Gff�ļ�����NCBIID���ݿ⣬��������ID��û�еĵ���UniProt��
	 * 20110302���³���
	 * @param gffRapDB  RAP_genes.gff3
	 * @param outFile û��GeneID��LOC����������ǵ���UniProt��
	 * @param insertUniID true: ��û���ѵ�����Ŀ����UniProtID��False����û���ѵ�����Ŀ�����outFile
	 * ����Ŀ��һ�β���ʱ�Ȳ��ã���Ҫ��������NCBIID��UniProtID,�Ҳ��������Ϊoutfile
	 * �ȵ�һ�θ��ֲ��Ҷ������ˣ��ڶ����ٽ�outfile����ʱ��û���ѵ��ľͿ��Ե���UniProt����
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
			content = URLDecoder.decode(content, enc);//�ļ��к���%20C�ȷ��ţ���url����
			//����Ǳ�����
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//װ��accID����Ӧ���ݿ��list
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
					//����ŵ���һλ��������ѯ������ȽϺã�Ҳ���ǽ�OsID�ŵ���һλ
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
				/////Ҫô����NCBIID�У�Ҫô����UniProtID�У����������û�У���װ��UniProtID����///////
				long GeneID = 0;
				String uniID = null;
				////////////////���geneID��UniProtID//////////
				for (String[] strings : lsAccIDInfo) 
				{
					//���趨uniprotID
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
	 * ��Rap2MSU����Ϣ--Ҳ����Os2LOC�Ķ��ձ�--�������ݿ�,�ȵ���RapDB��gff3�ļ��������һ�Σ�����һ��û�е�����ļ���
	 * Ȼ����Tigr��gff�ļ����ٰ�û�е�����ļ��������ݿ�
	 * û�в鵽�ķ��� outFIle
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
			if (tmpID.length<2) //˵����IRGSPIDû��LOCID��֮��Ӧ����ô��������
			{
				continue;
			}
			
			String[] tmpLOC = tmpID[1].split(",");
			
			/////////װ��list///////////
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
			
			//������ncbiid���,RapDB��Tigr����һ��
			ArrayList<NCBIID> lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);//����RapDB��ID�ѣ�Ȼ����TigrID��
			if (lsNcbiid == null || lsNcbiid.size()<1) {
				ncbiid = new NCBIID();ncbiid.setAccID(lstmpLOC.get(1)[0]);
				lsNcbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			}
			////////////////////
			if (lsNcbiid != null && lsNcbiid.size()>0)//�ѵ���
			{
				geneID = lsNcbiid.get(0).getGeneId();
			}
			if (geneID == 0) //���NCBIID��û��
			{
				//��ʼ��UniProtID��
				ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);//����RapDB��ID�ѣ�Ȼ����TigrID��
				if (lsUniProtIDs == null || lsUniProtIDs.size()<1) {
					uniProtID = new UniProtID(); uniProtID.setAccID(lstmpLOC.get(1)[0]);
					lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
				}
				if (lsUniProtIDs != null && lsUniProtIDs.size()>0) 
				{
					uniID = lsUniProtIDs.get(0).getUniID();
				}
				else//û�ѵ��ͼ�����
				{
					txtOutFile.writefile(content+"\n");
					continue;
				}
			}
			//////��ʼ����//////////////////////////////
			UpDateFriceDB.upDateNCBIUniID(geneID, uniID, 39947, lstmpLOC, NovelBioConst.DBINFO_RICE_IRGSP, NovelBioConst.DBINFO_RICE_TIGR);		
		}
		txtOutFile.close();
		txtRap2MSU.close();
	}
	
	/**
	 * ��RapDB��Gff�ļ�����gene2GO���ݿ⣬����NCBIGO��UniGO������
	 * ��url�����˽���
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
		Pattern pattern =Pattern.compile("\\((GO:\\d+)\\)", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
		Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
		int mm=0;
		while ((content = reader.readLine()) != null) 
		{
			
			//����Ǳ�����
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//װ��accID����Ӧ���ݿ��list
				long GeneID = 0; String uniID = null;
				//����NCBIID����û��
				for (int i = 0; i < tmpID.length; i++) 
				{
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);//�ļ��к���%20C�ȷ��ţ���url����
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
				//û�еĻ�����UniProtID��
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
				//��û�ҵ���������
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
	 * ����ǰ�����affyidtolocid����Ϣ�������ݿ⣬�ܵ���NCBIID�ĵ���NCBIID�����ܵĵ���UniProtID
	 * û�в鵽�ķ��� outFIle
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
			//��� geneID �� uniID
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
	 * ��RapDB��Gff3�ļ��е�Symbol��Description�������ݿ�
	 * �ļ��к���%20C�ȷ��ţ��Ѿ���url������
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
			String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
			//����Ǳ�����
			if (content.contains("ID=")||content.contains("Name=")||content.contains("Alias=")||content.contains("Gene_symbols=")||content.contains("GO=")||content.contains("Locus_id="))
			{
				
				String tmpInfo = content.split("\t")[8];
				String[] tmpID = tmpInfo.split(";");
				//װ��accID����Ӧ���ݿ��list
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				for (int i = 0; i < tmpID.length; i++) 
				{
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);//�ļ��к���%20C�ȷ��ţ���url����
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
					//OsID���ڵ�һλ
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
				/////Ҫô����NCBIID�У�Ҫô����UniProtID�У����������û�У���װ��UniProtID����///////
				long GeneID = 0;
				String uniID = null;
				//////////���geneID��UniProtID///
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
				
				/////////�������ݿ�//////////////
				if (GeneID !=0) {
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setGeneID(GeneID);
					GeneInfo geneInfo2 = DaoFSGeneInfo.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) //���ݿ���û�У��Ͳ���
					{
						geneInfo.setSymbol(symbol);
						geneInfo.setDescription(description);
						DaoFSGeneInfo.InsertGeneInfo(geneInfo);
					}
					else  //���ݿ�����
					{
						boolean flagUpdate = false;//�ж�����Ƿ��������ݿ�
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!geneInfo2.getSymbol().contains(string)) {//��������µ�symbol
								geneInfo2.setSymbol(string+"//"+geneInfo2.getSymbol());
								flagUpdate = true;
							}
						}
						if (!geneInfo2.getDescription().contains(description)) {//��������µ�description
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
					if (uniGeneInfo2 == null) //���ݿ���û�У��Ͳ���
					{
						uniGeneInfo.setSymbol(symbol);
						uniGeneInfo.setDescription(description);
						DaoFSUniGeneInfo.InsertUniGeneInfo(uniGeneInfo);
					}
					else  //���ݿ�����
					{
						boolean flagUpdate = false;//�ж�����Ƿ��������ݿ�
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!uniGeneInfo2.getSymbol().contains(string)) {//��������µ�symbol
								uniGeneInfo2.setSymbol(string+"//"+uniGeneInfo2.getSymbol());
								flagUpdate = true;
							}
						}
						if (!uniGeneInfo2.getDescription().contains(description)) {//��������µ�description
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
	
	
	//////////���¿�ʼ����Tigr�����ݣ�����TIGRID��GO��Description����Ϣ��ͬʱ��Ҫ����RapDB��Description��Ϣ//////////////////////////////
	/**
	 * ��TIGR��Gff�ļ�����NCBIID���ݿ��UniProt�⣬������geneInfo��
	 * @param gffTigrRice  tigrRice��gff�ļ�
	 * @param outFile û��GeneID��LOC����������ǵ���UniProt��
	 * @param insertUniID true: ��û���ѵ�����Ŀ����UniProtID��False����û���ѵ�����Ŀ�����outFile
	 * ����Ŀ��һ�β���ʱ�Ȳ��ã���Ҫ��������NCBIID��UniProtID,�Ҳ��������Ϊoutfile
	 * �ȵ�һ�θ��ֲ��Ҷ������ˣ��ڶ����ٽ�outfile����ʱ��û���ѵ��ľͿ��Ե���UniProt����
	 * @throws Exception  
	 */
	public static void tigrNCBIID(String gffTigrRice, String outFile,boolean insertUniID) throws Exception
	{
		//�������� %20֮������룬��Ҫ��URLDecoder�����н���
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
	 * ��TIGR��Gff�ļ�����geneInfo��
	 * @param gffTigrRice  tigrRice��gff�ļ�
	 * @throws Exception  
	 */
	public static void tigrDescription(String gffTigrRice) throws Exception
	{
		//�������� %20֮������룬��Ҫ��URLDecoder�����н���
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
	
			
			String description = URLDecoder.decode(ssLOC[1].split("=")[1], enc);//�ļ��к���%20C�ȷ��ţ���url����
			
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
						if (geneInfo2.getSymbol().trim().equals("")) {//���symbol
							geneInfo2.setSymbol(LOCID);
							update = true;
						}
						if (!geneInfo2.getDescription().contains(description)) {//���description
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
						if (uniGeneInfo2.getSymbol().trim().equals("")) {//���symbol
							uniGeneInfo2.setSymbol(LOCID);
							update = true;
						}
						if (!uniGeneInfo2.getDescription().contains(description)) {//���description
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
	 * ��Tigr��Gff�ļ�����gene2GO���ݿ⣬����NCBIGO��UniGO������
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
					{//ÿ��GOID��װ��
						UpDateFriceDB.upDateGenGO(Long.parseLong(lsAccInfo.get(i)), null, ss[j].trim(), NovelBioConst.DBINFO_RICE_TIGR);
					}
				}
			}
			else if (lsAccInfo.get(0).equals("uniID"))
			{
				for (int i = 1; i < lsAccInfo.size(); i++)
				{
					for (int j = 1; j < ss.length; j++) 
					{//ÿ��GOID��װ��
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
