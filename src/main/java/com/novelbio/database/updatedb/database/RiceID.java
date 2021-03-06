package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.GeneInfo;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;

public class RiceID {
	/** 下载的东西保存在哪里 */
	String downLoadPath = "";
	
	/** 具体下载的文件名 */
	String gffRapDB;
	String gffTIGR;
	String rapDBoutID;
	String tigrDBoutID;
	String rap2MSU;
	String tigrGoSlim;
	String rapLocusGff;
	public void setDownLoadPath(String downLoadPath) {
		this.downLoadPath = downLoadPath;
	}
	public void setRapLocusGff(String rapLocusGff) {
		this.rapLocusGff = rapLocusGff;
	}
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
	public void setRap2MSU(String rap2msu) {
		rap2MSU = rap2msu;
	}
	/**
	 * 将Tigr的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public void update() {
		RiceTIGRGFFID riceTIGRGFFID = new RiceTIGRGFFID();
		riceTIGRGFFID.setTxtWriteExcep(tigrDBoutID);
		riceTIGRGFFID.setInsertAccID(false);
		riceTIGRGFFID.updateFile(gffTIGR);
//		
		RiceRapDBLocus riceRapDBLocus = new RiceRapDBLocus();
		riceRapDBLocus.setTxtWriteExcep(rapLocusGff + "out");
		riceRapDBLocus.setInsertAccID(false);
		riceRapDBLocus.updateFile(rapLocusGff);
		
		RiceRap2MSU riceRap2MSU = new RiceRap2MSU();
		riceRap2MSU.updateFile(rap2MSU);
		
		riceRapDBLocus = new RiceRapDBLocus();
		riceRapDBLocus.setInsertAccID(true);
		riceRapDBLocus.updateFile(rapLocusGff + "out");
		
		RiceRapDBID riceRapDBID = new RiceRapDBID();
		riceRapDBID.setTxtWriteExcep(rapDBoutID);
		riceRapDBID.setInsertAccID(true);
		riceRapDBID.updateFile(gffRapDB);
		
		
		riceTIGRGFFID.setInsertAccID(true);
		riceTIGRGFFID.setTxtWriteExcep(tigrDBoutID + "_2");
		riceTIGRGFFID.updateFile(tigrDBoutID);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
		
		RiceRapDBInfo riceRapDBInfo = new RiceRapDBInfo();
		riceRapDBInfo.updateFile(gffRapDB);
		
		RiceTIGRInfo riceTIGRInfo = new RiceTIGRInfo();
		riceTIGRInfo.updateFile(gffTIGR);
		
		RiceTIGRGO riceTIGRGO = new RiceTIGRGO();
		riceTIGRGO.updateFile(tigrGoSlim);
	}
}


/**
 * 导入repdb的信息，需要设定outtxt，也就是查不到的写入另一个文本
 * @author zong0jie
 *
 */
class RiceRapDBLocus extends ImportPerLine {
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	boolean insertAccID = false;
	/**
	 * 没有的ID是否导入uniID
	 * 第一次不用第二次导入
	 * @param insertAccID
	 */
	public void setInsertAccID(boolean insertAccID) {
		this.insertAccID = insertAccID;
	}
	private static final Logger logger = Logger.getLogger(RiceRapDBID.class);
	
	/**
	 * 处理的时候记得将upDateNCBIUniID中的uniProtID代码的注释去掉，这里就是说如果导入NCBIID，那么就不将uniID导入NCBIID表了
	 * 将RapDB的Gff文件导入NCBIID数据库，仅仅导入ID，没有的倒入UniProt库
	 * 20110302更新程序
	 * @param gffRapDB  locus.gff
	 * @param outFile 没有GeneID的LOC基因，这个考虑导入UniProt表
	 * @param insertUniID true: 将没有搜到的项目插入UniProtID表，False：将没有搜到的项目输出到outFile
	 * 本项目第一次插入时先不用，先要将表都查找NCBIID和UniProtID,找不到的输出为outfile
	 * 等第一次各种查找都结束了，第二次再将outfile导入时，没有搜到的就可以导入UniProt表了
	 * @throws Exception  
	 */
	@Override
	public boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (!ss[2].equalsIgnoreCase("gene")) {
			return true;
		}
		
		String tmpInfo = ss[8];
		String[] tmpID = tmpInfo.split(";");
		//装载accID与相应数据库的list
		ArrayList<String[]> lsAccIDInfo2DB = new ArrayList<String[]>(); //保存全部的要导入数据库的信息,自动去重复
		ArrayList<String> lsRefID = new ArrayList<String>(); //保存查找的信息，就是说譬如DBINFO_NIAS_FLCDNA等不用来查找
		for (int i = 0; i < tmpID.length; i++) {
			tmpID[i] = StringOperate.decode(tmpID[i]);
			if (tmpID[i].contains("ID=")){
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					tmpRapID[1] = DBAccIDSource.IRGSP_rice.toString();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
				}
			}
			else if (tmpID[i].contains("Transcript variants=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					tmpRapID[1] = DBAccIDSource.IRGSP_rice.toString();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);			
				}
			}
			else if (tmpID[i].contains("Alias=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					tmpRapID[0] =   tmpAcc[j];
					tmpRapID[1] = DBAccIDSource.NCBI.name();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
				}
			}
			else if (tmpID[i].contains("Gene symbol=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j];
					tmpRapID[1] = DBAccIDSource.Symbol.name();
					lsAccIDInfo2DB.add(tmpRapID);
				}
			}
			else if (tmpID[i].contains("Gene Symbol Synonym(s)=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j].replace("\"", "").trim();
					tmpRapID[1] = DBAccIDSource.Synonyms.name();
					lsAccIDInfo2DB.add(tmpRapID);
				}
			}
		}
		if (lsRefID.size() == 0) {
			return true;
		}
		GeneID copedID = new GeneID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		//如果需要插入了，就不管是不是ncbiID都插入进去
		if (insertAccID || copedID.getIDtype() == GeneID.IDTYPE_GENEID) {
			for (String[] strings : lsAccIDInfo2DB) {
				copedID.setUpdateAccID(strings[0]);
				copedID.setUpdateDBinfo(strings[1], true);
				if (!copedID.update(insertAccID)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
}
/**
 * 导入repdb的信息，需要设定outtxt，也就是查不到的写入另一个文本
 * @author zong0jie
 *
 */
class RiceRapDBID extends ImportPerLine {
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	boolean insertAccID = false;
	/**
	 * 没有的ID是否导入uniID
	 * 第一次不用第二次导入
	 * @param insertAccID
	 */
	public void setInsertAccID(boolean insertAccID) {
		this.insertAccID = insertAccID;
	}
	private static final Logger logger = Logger.getLogger(RiceRapDBID.class);
	
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
		String[] ss = lineContent.split("\t");
		if (!ss[2].equalsIgnoreCase("mRNA") && !ss[2].equalsIgnoreCase("gene")) {
			return true;
		}
		
		String tmpInfo = ss[8];
		String[] tmpID = tmpInfo.split(";");
		//装载accID与相应数据库的list
		ArrayList<String[]> lsAccIDInfo2DB = new ArrayList<String[]>(); //保存全部的要导入数据库的信息,自动去重复
		ArrayList<String> lsRefID = new ArrayList<String>(); //保存查找的信息，就是说譬如DBINFO_NIAS_FLCDNA等不用来查找
		for (int i = 0; i < tmpID.length; i++) {
			tmpID[i] = StringOperate.decode(tmpID[i]);
			if (tmpID[i].contains("ID=")){
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[1] = DBAccIDSource.IRGSP_rice.toString();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
				}
			}
			else if (tmpID[i].contains("Name=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[1] = DBAccIDSource.IRGSP_rice.toString();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);			
				}
			}
			else if (tmpID[i].contains("Alias=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] = tmpAcc[j];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[1] = DBAccIDSource.NCBI.name();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(tmpAcc[j]);
				}
			}
			else if (tmpID[i].contains("Gene symbol=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[0] =  tmpAcc[j];
					tmpRapID[1] = DBAccIDSource.Symbol.name();
					lsAccIDInfo2DB.add(tmpRapID);
				}
			}
			else if (tmpID[i].contains("Gene Symbol Synonym(s)=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[1] = DBAccIDSource.Synonyms.name();
					lsAccIDInfo2DB.add(tmpRapID);
				}
			}
			//这个放到第一位，这样查询起来会比较好，也就是将OsID放到第一位
			else if (tmpID[i].contains("ID_converter=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					String[] tmpRapID =new String[2];
					tmpRapID[0] =  tmpAcc[j];
					if (tmpRapID[0].equals("_")) {
						continue;
					}
					tmpRapID[1] = DBAccIDSource.IRGSP_rice.name();
					lsAccIDInfo2DB.add(tmpRapID);
					lsRefID.add(0, tmpAcc[j]);
				}
			}
		}
		if (lsRefID.size() == 0) {
			return true;
		}
		GeneID copedID = new GeneID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		//如果需要插入了，就不管是不是ncbiID都插入进去
		if (insertAccID || copedID.getIDtype() == GeneID.IDTYPE_GENEID) {
			for (String[] strings : lsAccIDInfo2DB) {
				copedID.setUpdateAccID(strings[0]);
				copedID.setUpdateDBinfo(strings[1], true);
				if (!copedID.update(insertAccID)) {
					return false;
				}
			}
			return true;
		} 
		return false;
	}
	
}
/**
 * 将RapDB中Gff3文件中的Symbol与Description与GO导入数据库
 * 文件中含有%20C等符号，需要用url解码
 * @param fileName
 * @throws Exception
 */
class RiceRapDBInfo extends ImportPerLine {
	ManageDBInfo manageDBInfo = ManageDBInfo.getInstance();
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (!ss[2].equalsIgnoreCase("mRNA") && !ss[2].equalsIgnoreCase("gene")) {
			return true;
		}		
		String symbol = "";
		String description = "";

		String tmpInfo = ss[8];
		// 文件中含有%20C等符号，用url解码
		tmpInfo = StringOperate.decode(tmpInfo); 
		
		String[] tmpID = tmpInfo.split(";");
		// 装载accID与相应数据库的list
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
			} else if (tmpID[i].contains("Gene_symbols=")) {
				String tmp = tmpID[i].split("=")[1];
				String[] tmpAcc = tmp.split(",");
				for (int j = 0; j < tmpAcc.length; j++) {
					if (tmpAcc[j].contains("\"")) {
						continue;
					}
					String tmpRapID = tmpAcc[j];
					lsRefID.add(tmpRapID);

					if (symbol.trim().equals(""))
						symbol = tmpRapID;
					else
						symbol = symbol + "//" + tmpRapID;
				}
			} else if (tmpID[i].contains("Note=")) {
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
		
		GeneID copedID = new GeneID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.IRGSP_rice.toString()));
		geneInfo.setSymb(symbol);
		geneInfo.setDescrp(description);
		geneInfo.addPubID(lsPubmeds);
		for (String string : lsGOs) {
			copedID.addUpdateGO(string, DBAccIDSource.IRGSP_rice, null, null, null);
		}
		
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
	
	private ArrayList<String> setGOID(String GOInfo) {
		ArrayList<String> lsGOID = new ArrayList<String>();
		ArrayList<String[]> lsResult = PatternOperate.getPatLoc(GOInfo, "GO:\\d+", false);
		for (String[] strings : lsResult) {
			lsGOID.add(strings[0]);
		}
		return lsGOID;
	}
	
}
/**
 * 将Rap2MSU的信息--也就是Os2LOC的对照表--导入数据库,
 * 1. 导入RapDB的gff3文件 产生一个没有倒入的文件
 * 2. 导入Tigr的gff文件，产生一个没有倒入的文件
 * 3. 导入Rap2MSU，全部导入
 * 4. 导入RapDB的gff3文件 产生一个没有倒入的文件
 * 5. 导入Tigr的gff文件，产生一个没有倒入的文件
 * @throws Exception 
 */
class RiceRap2MSU extends ImportPerLine {
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.contains("None")) {
			return true;
		}
		String[] tmpID = lineContent.split("\t");
		if (tmpID.length<2) //说明该IRGSPID没有LOCID与之对应，那么可以跳过
			return true;
		
		String[] tmpLOC = tmpID[1].split(",");
		/////////装入list///////////
		ArrayList<String[]> lstmpLOC = new ArrayList<String[]>();
		ArrayList<String> lsRef = new ArrayList<String>();
		String[] tmpLOC2Info1 = new String[2];
		tmpLOC2Info1[0] = tmpID[0]; tmpLOC2Info1[1] = DBAccIDSource.IRGSP_rice.toString();
		lstmpLOC.add(tmpLOC2Info1);
		lsRef.add(tmpID[0]);
		for (String string : tmpLOC) {
			String[] tmpLOC2Info2 = new String[2];
			tmpLOC2Info2[0] = string; tmpLOC2Info2[1] = DBAccIDSource.TIGR_rice.toString();
			lstmpLOC.add(tmpLOC2Info2);
			lsRef.add(tmpLOC2Info2[0]);
		}
		////////////////////////////
		GeneID copedID = new GeneID("", 39947);
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

class RapDBGO extends ImportPerLine {
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	
	String enc="utf8";//文件中含有%20C等符号，用url解码
	Pattern pattern =Pattern.compile("\\((GO:\\d+)\\)", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
	PatternOperate patternOperate = new PatternOperate("\\((GO:\\d+)\\)", false);
	@Override
	public boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (!ss[2].equalsIgnoreCase("mRNA") && !ss[2].equalsIgnoreCase("gene")) {
			return true;
		}
		String tmpInfo = ss[8];
		String[] tmpID = tmpInfo.split(";");
		//装载accID与相应数据库的list
		ArrayList<String> lsRefID = new ArrayList<String>();
		//先搜NCBIID看有没有
		for (int i = 0; i < tmpID.length; i++) {
			tmpID[i] = StringOperate.decode(tmpID[i]);
			if (tmpID[i].contains("ID=")||tmpID[i].contains("Name=")||tmpID[i].contains("Alias=")||tmpID[i].contains("Gene_symbols=")||tmpID[i].contains("Locus_id=")) {
				String tmp = tmpID[i].split("=")[1];
				String tmpOsID= tmp.split(",")[0].trim();
				lsRefID.add(tmpOsID);
			}
		}
		GeneID copedID = new GeneID("", 39947);
		copedID.setUpdateRefAccID(lsRefID);
		if (copedID.getIDtype() == GeneID.IDTYPE_ACCID) {
			return false;
		}

		List<String> lsGOID = patternOperate.getPat(lineContent);
		for (String goID : lsGOID) {
			copedID.addUpdateGO(goID, DBAccIDSource.IRGSP_rice, "", null, "");
		}
		
		return copedID.update(false);
	}
}
/**
 * 将TIGR的Gff文件导入NCBIID数据库或UniProt库，不导入geneInfo表
 * @param gffTigrRice  tigrRice的gff文件
 * @param outFile 没有GeneID的LOC基因，这个考虑导入UniProt表
 * @param insertUniID true: 将没有搜到的项目插入UniProtID表，False：将没有搜到的项目输出到outFile
 * 本项目第一次插入时先不用，先要将表都查找NCBIID和UniProtID,找不到的输出为outfile
 * 等第一次各种查找都结束了，第二次再将outfile导入时，没有搜到的就可以导入UniProt表了
 * @throws Exception  
 */
class RiceTIGRGFFID extends ImportPerLine {
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	
	boolean insertAccID = false;
	/**
	 * 没有的ID是否导入uniID
	 * 第一次不用第二次导入
	 * @param insertAccID
	 */
	public void setInsertAccID(boolean insertAccID) {
		this.insertAccID = insertAccID;
	}
	@Override
	public boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#") || lineContent.equals(""))
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
		GeneID copedID = new GeneID(LOCID, 39947);
		copedID.setUpdateDBinfo(DBAccIDSource.TIGR_rice, true);
		return copedID.update(insertAccID);
	}
}

/**
 * 将TIGR的Gff文件导入geneInfo表
 * @param gffTigrRice  tigrRice的gff文件
 * @throws Exception  
 */
class RiceTIGRInfo extends ImportPerLine {
	ManageDBInfo manageDBInfo = ManageDBInfo.getInstance();
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

		description = StringOperate.decode(ssLOC[1].split("=")[1]);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.TIGR_rice.toString()));
		geneInfo.setSymb(LOCID); geneInfo.setDescrp(description);
		GeneID copedID = new GeneID("", 39947);
		copedID.setUpdateRefAccID(LOCID);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
}

/**
 * 将Tigr的all.GOSlim_assignment文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
 */
class RiceTIGRGO extends ImportPerLine {

	@Override
	public boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		String LocID = ss[0].trim();
		GeneID copedID = new GeneID(LocID, 39947);
		List<String> lsRefInfo = new ArrayList<String>();
		if (ss.length >= 6) {
			lsRefInfo.add(ss[5]);
		}
		copedID.addUpdateGO(ss[1].trim(), DBAccIDSource.TIGR_rice, ss[4], lsRefInfo, null);
		copedID.setUpdateDBinfo(DBAccIDSource.TIGR_rice, false);
		return copedID.update(false);
	}
	
}
