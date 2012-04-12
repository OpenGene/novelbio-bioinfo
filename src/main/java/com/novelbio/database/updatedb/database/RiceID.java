package com.novelbio.database.updatedb.database;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

public class RiceID{
	String gffRapDB = "";
	String gffTIGR = "";
	String rapDBoutID = "";
	String tigrDBoutID = "";
	String rap2MSU = "";
	String tigrGoSlim = "";
	public void setRiceRap2MSU(String rap2MSU) {
		this.rap2MSU = rap2MSU;
	}
	public void setGffRapDB(String gffRapDB) {
		this.gffRapDB = gffRapDB;
	}
	public void setGffTIGR(String gffTIGR) {
		this.gffTIGR = gffTIGR;
	}
	public void setRapDBoutID(String rapDBoutID) {
		this.rapDBoutID = rapDBoutID;
	}
	public void setTigrDBoutID(String tigrDBoutID) {
		this.tigrDBoutID = tigrDBoutID;
	}
	public void setTigrGoSlim(String tigrGoSlim) {
		this.tigrGoSlim = tigrGoSlim;
	}
	/**
	 * ��Tigr��Gff�ļ�����gene2GO���ݿ⣬����NCBIGO��UniGO������
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public void update()
	{
		RiceTIGRGFFID riceTIGRGFFID = new RiceTIGRGFFID();
		riceTIGRGFFID.setTxtWriteExcep(tigrDBoutID);
		riceTIGRGFFID.setInsertAccID(false);
		riceTIGRGFFID.updateFile(gffTIGR, false);
		
		RiceRapDBID riceRapDBID = new RiceRapDBID();
		riceRapDBID.setTxtWriteExcep(rapDBoutID);
		riceRapDBID.setInsertAccID(false);
		riceRapDBID.updateFile(gffRapDB, false);
		
		RiceRap2MSU riceRap2MSU = new RiceRap2MSU();
		riceRap2MSU.updateFile(rap2MSU, false);
		
		riceRapDBID.setInsertAccID(true);
		riceRapDBID.setTxtWriteExcep(rapDBoutID + "_2");
		riceRapDBID.updateFile(rapDBoutID, false);
		
		riceTIGRGFFID.setInsertAccID(true);
		riceTIGRGFFID.setTxtWriteExcep(tigrDBoutID + "_2");
		riceTIGRGFFID.updateFile(tigrDBoutID, false);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
		
		RiceRapDBInfo riceRapDBInfo = new RiceRapDBInfo();
		riceRapDBInfo.updateFile(gffRapDB, false);
		
		RiceTIGRInfo riceTIGRInfo = new RiceTIGRInfo();
		riceTIGRInfo.updateFile(gffTIGR, false);
		
		RiceTIGRGO riceTIGRGO = new RiceTIGRGO();
		riceTIGRGO.updateFile(tigrGoSlim, false);
	}
}
/**
 * ����repdb����Ϣ����Ҫ�趨outtxt��Ҳ���ǲ鲻����д����һ���ı�
 * @author zong0jie
 *
 */
class RiceRapDBID extends ImportPerLine
{	
	protected void setReadFromLine()
	{
		this.readFromLine = 1;
	}
	boolean insertAccID = false;
	/**
	 * û�е�ID�Ƿ���uniID
	 * ��һ�β��õڶ��ε���
	 * @param insertAccID
	 */
	public void setInsertAccID(boolean insertAccID) {
		this.insertAccID = insertAccID;
	}
	String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
	private static Logger logger = Logger.getLogger(RiceRapDBID.class);
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
					tmpRapID[0] = tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_RICE_RAPDB;
					lsAccIDInfo.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
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
					lsRefID.add(tmpAcc[j]);			
				}
			}
			else if (tmpID[i].contains("Alias=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =   tmpAcc[j];
					tmpRapID[1] = NovelBioConst.DBINFO_NCBIID;
					lsAccIDInfo.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
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
					lsRefID.add(0, tmpAcc[j]);
				}
			}
			else if (tmpID[i].contains("ORF_evidence=")) {
				String tmp = tmpID[i].split("=")[1];
				if (!tmp.equals("NONE")) {
					String[] tmpAcc = tmp.split(",");
					for (int j = 0; j < tmpAcc.length; j++) {
						String[] tmpRapID =new String[2];
						tmpRapID[0] =  tmpAcc[j].replaceAll("\\(.*\\)", "").trim();
						tmpRapID[1] = NovelBioConst.DBINFO_UNIPROT_GenralID;
						lsRefID.add(tmpAcc[j]);
					}
				}
			}
		}
		if (lsRefID.size() == 0) {
			return true;
		}
		CopedID copedID = new CopedID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		//�����Ҫ�����ˣ��Ͳ����ǲ���ncbiID�������ȥ
		if (insertAccID) {
			for (String[] strings : lsAccIDInfo) 
			{
				copedID.setUpdateAccID(strings[0]);
				copedID.setUpdateDBinfo(strings[1], true);
				if (!copedID.update(insertAccID)) {
					return false;
				}
			}
			return true;
		}
		//����ֻ����ncbi����
		else {
			if (copedID.getIDtype().equals(CopedID.IDTYPE_GENEID)) {
				for (String[] strings : lsAccIDInfo) 
				{
					copedID.setUpdateAccID(strings[0]);
					copedID.setUpdateDBinfo(strings[1], true);
					if (!copedID.update(insertAccID)) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
	
}
/**
 * ��Rap2MSU����Ϣ--Ҳ����Os2LOC�Ķ��ձ�--�������ݿ�,
 * 1. ����RapDB��gff3�ļ� ����һ��û�е�����ļ�
 * 2. ����Tigr��gff�ļ�������һ��û�е�����ļ�
 * 3. ����Rap2MSU��ȫ������
 * 4. ����RapDB��gff3�ļ� ����һ��û�е�����ļ�
 * 5. ����Tigr��gff�ļ�������һ��û�е�����ļ�
 * @throws Exception 
 */
class RiceRap2MSU extends ImportPerLine
{
	protected void setReadFromLine()
	{
		this.readFromLine = 1;
	}
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
			if (!copedID.update(true))
				return false;
		}
		return true;
	}
}

class RapDBGO extends ImportPerLine
{
	protected void setReadFromLine()
	{
		this.readFromLine = 1;
	}
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
/**
 * ��TIGR��Gff�ļ�����NCBIID���ݿ��UniProt�⣬������geneInfo��
 * @param gffTigrRice  tigrRice��gff�ļ�
 * @param outFile û��GeneID��LOC����������ǵ���UniProt��
 * @param insertUniID true: ��û���ѵ�����Ŀ����UniProtID��False����û���ѵ�����Ŀ�����outFile
 * ����Ŀ��һ�β���ʱ�Ȳ��ã���Ҫ��������NCBIID��UniProtID,�Ҳ��������Ϊoutfile
 * �ȵ�һ�θ��ֲ��Ҷ������ˣ��ڶ����ٽ�outfile����ʱ��û���ѵ��ľͿ��Ե���UniProt����
 * @throws Exception  
 */
class RiceTIGRGFFID extends ImportPerLine
{
	protected void setReadFromLine()
	{
		this.readFromLine = 1;
	}
	boolean insertAccID = false;
	/**
	 * û�е�ID�Ƿ���uniID
	 * ��һ�β��õڶ��ε���
	 * @param insertAccID
	 */
	public void setInsertAccID(boolean insertAccID) {
		this.insertAccID = insertAccID;
	}
	@Override
	public boolean impPerLine(String lineContent) {
			if (lineContent.startsWith("#"))
				return true;
			String[] ss = lineContent.split("\t");
			if (!ss[2].trim().equals("gene"))
				return true;
			////////////////////////////
			String[] ssLOC = ss[8].split(";");
			if (!ssLOC[ssLOC.length - 1].contains("Alias=")) {
				return true;
			}
			String LOCID = ssLOC[ssLOC.length-1].split("=")[1];
			CopedID copedID = new CopedID(LOCID, 39947);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_RICE_TIGR, true);
			return copedID.update(insertAccID);
	}
}
/**
 * ��RapDB��Gff3�ļ��е�Symbol��Description��GO�������ݿ�
 * �ļ��к���%20C�ȷ��ţ���Ҫ��url����
 * @param fileName
 * @throws Exception
 */
class RiceRapDBInfo extends ImportPerLine
{
	protected void setReadFromLine()
	{
		this.readFromLine = 1;
	}
	String enc = "utf8";// �ļ��к���%20C�ȷ��ţ���url����
	@Override
	boolean impPerLine(String lineContent) {
		String symbol = "";
		String description = "";

		// ����Ǳ�����
		if (!(lineContent.contains("ID=") || lineContent.contains("Name=")
				|| lineContent.contains("Alias=")
				|| lineContent.contains("Gene_symbols=")
				|| lineContent.contains("GO=")
				|| lineContent.contains("Locus_id="))) {
			return true;
		}
		String tmpInfo = lineContent.split("\t")[8];
		// �ļ��к���%20C�ȷ��ţ���url����
		try { tmpInfo = URLDecoder.decode(tmpInfo, enc); } catch (UnsupportedEncodingException e) { return false; 	}
		
		String[] tmpID = tmpInfo.split(";");
		// װ��accID����Ӧ���ݿ��list
		ArrayList<String> lsRefID = new ArrayList<String>();
		ArrayList<String> lsPubmeds = new ArrayList<String>();
		ArrayList<String> lsGOs = new ArrayList<String>();
		for (int i = 0; i < tmpID.length; i++) {
			if (tmpID[i].contains("ID=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsRefID.add(tmpRapID);
				}
			} else if (tmpID[i].contains("Name=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsRefID.add(tmpRapID);
				}
			} else if (tmpID[i].contains("Alias=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsRefID.add(tmpRapID);
				}
			} else if (tmpID[i].contains("Gene_symbols=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsRefID.add(tmpRapID);

					if (symbol.trim().equals(""))
						symbol = tmpRapID;
					else
						symbol = symbol + "//" + tmpRapID;
				}
			}
			// OsID���ڵ�һλ
			else if (tmpID[i].contains("ID_converter=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsRefID.add(0, tmpRapID);
				}
			}
			else if (tmpID[i].contains("Note=")) {
				description = tmpID[i].split("=")[1];
			}
			else if (tmpID[i].contains("References=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String tmpRapID = tmpAcc[j];
					lsPubmeds.add(0, tmpRapID);
				}
			}
			else if (tmpID[i].contains("GO=")) {
				String tmp = tmpID[i].split("=")[1];
				lsGOs = setGOID(tmp);
			}
		}
		
		if (symbol.trim().equals("") && description.trim().equals("")) {
			return true;
		}
		
		CopedID copedID = new CopedID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(symbol);
		geneInfo.setDescrp(description);
		geneInfo.setPubIDs(lsPubmeds);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_RICE_RAPDB);
		for (String string : lsGOs) {
			copedID.setUpdateGO(string, NovelBioConst.DBINFO_RICE_RAPDB, null, null, null);
		}
		
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
	
	private ArrayList<String> setGOID(String GOInfo)
	{
		ArrayList<String> lsGOID = new ArrayList<String>();
		ArrayList<String[]> lsResult = PatternOperate.getPatLoc(GOInfo, "GO:\\d+", false);
		for (String[] strings : lsResult) {
			lsGOID.add(strings[0]);
		}
		return lsGOID;
	}
	
}
/**
 * ��TIGR��Gff�ļ�����geneInfo��
 * @param gffTigrRice  tigrRice��gff�ļ�
 * @throws Exception  
 */
class RiceTIGRInfo extends ImportPerLine
{
	String enc="utf8";//�ļ��к���%20C�ȷ��ţ���url����
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#")) {
			return true;
		}
		
		String[] ss = lineContent.split("\t");
		if (!ss[2].trim().equals("gene"))
			return true;
		
		String[] ssLOC = ss[8].split(";");
		if (ssLOC.length < 3 && !ssLOC[ssLOC.length - 1].contains("Alias=")) {
			return true;
		}
		String LOCID = ssLOC[2].split("=")[1];
		String description;
		try {
			description = URLDecoder.decode(ssLOC[1].split("=")[1], enc);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}//�ļ��к���%20C�ȷ��ţ���url����
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(LOCID); geneInfo.setDescrp(description);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_RICE_TIGR);
		CopedID copedID = new CopedID("", 39947);
		copedID.setUpdateRefAccID(LOCID);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
}

/**
 * ��Tigr��all.GOSlim_assignment�ļ�����gene2GO���ݿ⣬����NCBIGO��UniGO������
 */
class RiceTIGRGO extends ImportPerLine
{

	@Override
	public boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		String LocID = ss[0].trim();
		CopedID copedID = new CopedID(LocID, 39947);
		for (int j = 1; j < ss.length; j++) 
		{//ÿ��GOID��װ��
			copedID.setUpdateGO(ss[j].trim(), NovelBioConst.DBINFO_RICE_TIGR, null, null, null);
		}
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_RICE_TIGR, false);
		return copedID.update(false);
	}
	
}
