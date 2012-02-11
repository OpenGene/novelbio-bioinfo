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
				
				/////////�������ݿ�//////////////
				if (GeneID !=0) {
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setGeneID(GeneID);
					GeneInfo geneInfo2 = MapGeneInfoOld.queryGeneInfo(geneInfo);
					if (geneInfo2 == null) //���ݿ���û�У��Ͳ���
					{
						geneInfo.setSymb(symbol);
						geneInfo.setDescrp(description);
						MapGeneInfoOld.InsertGeneInfo(geneInfo);
					}
					else  //���ݿ�����
					{
						boolean flagUpdate = false;//�ж�����Ƿ��������ݿ�
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!geneInfo2.getSymb().contains(string)) {//��������µ�symbol
								geneInfo2.setSymb(string+"//"+geneInfo2.getSymb());
								flagUpdate = true;
							}
						}
						if (!geneInfo2.getDescrp().contains(description)) {//��������µ�description
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
					if (uniGeneInfo2 == null) //���ݿ���û�У��Ͳ���
					{
						uniGeneInfo.setSymb(symbol);
						uniGeneInfo.setDescrp(description);
						MapUniGeneInfoOld.InsertUniGeneInfo(uniGeneInfo);
					}
					else  //���ݿ�����
					{
						boolean flagUpdate = false;//�ж�����Ƿ��������ݿ�
						String[] tmpSymbol = symbol.split("//");
						for (String string : tmpSymbol) {
							if (!uniGeneInfo2.getSymb().contains(string)) {//��������µ�symbol
								uniGeneInfo2.setSymb(string+"//"+uniGeneInfo2.getSymb());
								flagUpdate = true;
							}
						}
						if (!uniGeneInfo2.getDescrp().contains(description)) {//��������µ�description
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
						if (geneInfo2.getSymb().trim().equals("")) {//���symbol
							geneInfo2.setSymb(LOCID);
							update = true;
						}
						if (!geneInfo2.getDescrp().contains(description)) {//���description
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
						if (uniGeneInfo2.getSymb().trim().equals("")) {//���symbol
							uniGeneInfo2.setSymb(LOCID);
							update = true;
						}
						if (!uniGeneInfo2.getDescrp().contains(description)) {//���description
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
			ArrayList<String> lsAccInfo = ServAnno.getNCBIUni(LocID, 39947);
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
/**
 * ����repdb����Ϣ����Ҫ�趨outtxt��Ҳ���ǲ鲻����д����һ���ı�
 * @author zong0jie
 *
 */
class RiceRapDB extends ImportPerLine
{	
	String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
	private static Logger logger = Logger.getLogger(RiceRapDB.class);
	public void importInfoPerLine(String rapdbGFF, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, rapdbGFF);
		else 
			txtGene2Acc = new TxtReadandWrite(rapdbGFF, false);
		//�ӵڶ��п�ʼ��ȡ
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
	@Override
	public boolean impPerLine(String lineContent) {
		String tmpInfo = lineContent.split("\t")[8];
		String[] tmpID = tmpInfo.split(";");
		//װ��accID����Ӧ���ݿ��list
		ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>(); //����ȫ����Ҫ�������ݿ����Ϣ,�Զ�ȥ�ظ�
		ArrayList<String> lsRefID = new ArrayList<String>(); //������ҵ���Ϣ������˵Ʃ��DBINFO_NIAS_FLCDNA�Ȳ���������
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
			//����ŵ���һλ��������ѯ������ȽϺã�Ҳ���ǽ�OsID�ŵ���һλ
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
 * ��Rap2MSU����Ϣ--Ҳ����Os2LOC�Ķ��ձ�--�������ݿ�,
 * 1. ����RapDB��gff3�ļ� ����һ��û�е�����ļ�
 * 2. ����Rap2MSU������һ��û�е�����ļ�
 * 3. ����Tigr��gff�ļ�������һ��û�е�����ļ�
 * 4. �������ţ�����2��1��3��˳���û�е�����ļ��������ݿ⣬û���ҵ��ĵ���uniID����
 * @throws Exception 
 */
class RiceRap2MSU extends ImportPerLine
{
	@Override
	boolean impPerLine(String lineContent) {
		String[] tmpID = lineContent.split("\t");
		if (tmpID.length<2) //˵����IRGSPIDû��LOCID��֮��Ӧ����ô��������
			return true;
		
		String[] tmpLOC = tmpID[1].split(",");
		/////////װ��list///////////
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
	String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
	Pattern pattern =Pattern.compile("\\((GO:\\d+)\\)", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
	Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������

	@Override
	public boolean impPerLine(String lineContent) {
		if (!(lineContent.contains("ID=")||lineContent.contains("Name=")||lineContent.contains("Alias=")||lineContent.contains("Gene_symbols=")||lineContent.contains("GO=")||lineContent.contains("Locus_id=")))
		{
			return true;
		}
			String tmpInfo = lineContent.split("\t")[8];
			String[] tmpID = tmpInfo.split(";");
			//װ��accID����Ӧ���ݿ��list
			long GeneID = 0; String uniID = null;
			ArrayList<String> lsRefID = new ArrayList<String>();
 			//����NCBIID����û��
			for (int i = 0; i < tmpID.length; i++) 
			{
				try {
					tmpID[i] = URLDecoder.decode(tmpID[i], enc);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}//�ļ��к���%20C�ȷ��ţ���url����
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
