package com.novelbio.database.upDateDB.idConvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.SvDBNCBIUni;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;


/**
 * 将从affy下载的cvs格式excel转化成为导入数据库的格式
 * 格式如下
 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
 * 注意affy芯片需要给出芯片型号
 * @author zong0jie
 *
 */
public class AffyIDmodify 
{
	static HashMap<Integer, String> hashAffyDBinfo = new HashMap<Integer, String>();
	public static HashMap<Integer, String> getHashAffyDBinfo()
	{
		if (hashAffyDBinfo.size() == 0) {
			hashAffyDBinfo.put(9606, NovelBioConst.DBINFO_AFFY_HUMAN_U133_PLUS2);
			hashAffyDBinfo.put(39947, NovelBioConst.DBINFO_AFFY_RICE_31);
			hashAffyDBinfo.put(9913, NovelBioConst.DBINFO_AFFY_COW);
			hashAffyDBinfo.put(9823, NovelBioConst.DBINFO_AFFY_PIG);
			hashAffyDBinfo.put(3702, NovelBioConst.DBINFO_AFFY_ATH);
		}
		return hashAffyDBinfo;
	}
	
	
	/**
	 * 首先删除control探针
	 * 将从affy下载的cvs格式excel转化成为导入数据库的格式
	 * 格式如下
	 * 物种 \t  NCBIGeneID \t  accessID \t  DataBaseInfo \n
	 * 注意affy芯片需要给出芯片型号
	 * @param taxID 物种编号ncbi的taxID
	 * @param affyDBID affy芯片型号,最后写入数据库
	 * @author zong0jie
	 *
	 */
	public static void getInfo(int taxID,String affyInput,int rowstart, String output,String affyDBID) throws Exception {
		HashMap<Integer, String> hashAffyDBinfo = getHashAffyDBinfo();
		String dbinfo = hashAffyDBinfo.get(taxID);
		
		ExcelOperate excelAffy = new ExcelOperate();
		excelAffy.openExcel(affyInput);
		
		TxtReadandWrite txtAffyInfo=new TxtReadandWrite();
		txtAffyInfo.setParameter(output, true,false);
		ArrayList<String[]> result=new ArrayList<String[]>();
		String[][] affyInfo = excelAffy.ReadExcel(rowstart, 1, excelAffy.getRowCount(), excelAffy.getColCount());
		
		Pattern patgbTransMambrance =Pattern.compile("([A-Z_]+?\\d+?)(\\.\\d){0,1}\\s//", Pattern.CASE_INSENSITIVE);  
	    //Pattern patgbIRP =Pattern.compile("([A-Za-z_]|\\d)+", Pattern.CASE_INSENSITIVE);  
	    Matcher matID;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
	    
		for (int i = 0; i < affyInfo.length; i++) {
			
			if (affyInfo[i][4].contains("control")) {
				continue;
			}
			String[] affyIDinfo = null;
			//没有entrzID存在，那么就用其他的方法看能不能找到entrzID
			if (affyInfo[i][18]==null||affyInfo[i][18].contains("-")||affyInfo[i][18].trim().equals(""))
			{
				
				boolean geneIDflag = false;
				boolean uniIDflag = false;
				///////////////////////////////////////////
				affyIDinfo = getGenUniID(affyInfo[i][8], taxID);
				if (!affyIDinfo[0].equals("geneID")) {
					geneIDflag = true;
				}
				else if (!affyIDinfo[0].equals("uniID")) {
					uniIDflag = true;
				}
				if (uniIDflag || geneIDflag) {
					upDateGenUniID(affyIDinfo, affyInfo[i][0].trim(), dbinfo, taxID, geneIDflag, uniIDflag);
					//将Hs.153360等导入数据库
					if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID, geneIDflag, uniIDflag);
					}
					continue;
				}
				/////////////////////////////////////////////////////////
				affyIDinfo = getGenUniID(affyInfo[i][22], taxID);
				if (!affyIDinfo[0].equals("geneID")) {
					geneIDflag = true;
				}
				else if (!affyIDinfo[0].equals("uniID")) {
					uniIDflag = true;
				}
				if (uniIDflag || geneIDflag) {
					upDateGenUniID(affyIDinfo, affyInfo[i][0].trim(), dbinfo, taxID, geneIDflag, uniIDflag);
					//将Hs.153360等导入数据库
					if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID, geneIDflag, uniIDflag);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////
				affyIDinfo = getGenUniID(affyInfo[i][23], taxID);
				if (!affyIDinfo[0].equals("geneID")) {
					geneIDflag = true;
				}
				else if (!affyIDinfo[0].equals("uniID")) {
					uniIDflag = true;
				}
				if (uniIDflag || geneIDflag) {
					upDateGenUniID(affyIDinfo, affyInfo[i][0].trim(), dbinfo, taxID, geneIDflag, uniIDflag);
					//将Hs.153360等导入数据库
					if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID, geneIDflag, uniIDflag);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////
				affyIDinfo = getGenUniID(affyInfo[i][19], taxID);
				if (!affyIDinfo[0].equals("geneID")) {
					geneIDflag = true;
				}
				else if (!affyIDinfo[0].equals("uniID")) {
					uniIDflag = true;
				}
				if (uniIDflag || geneIDflag) {
					upDateGenUniID(affyIDinfo, affyInfo[i][0].trim(), dbinfo, taxID, geneIDflag, uniIDflag);
					//将Hs.153360等导入数据库
					if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID, geneIDflag, uniIDflag);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////
			}
			//有entrzID存在，直接导入NCBIID
			else
			{
				String[] geneIDstr = affyInfo[i][18].split("///");
				for (String string : geneIDstr)
				{
					int geneID = Integer.parseInt(string);
					NCBIID ncbiid = new NCBIID();
					ncbiid.setAccID(affyInfo[i][0]); ncbiid.setDBInfo(dbinfo);
					ncbiid.setGeneId(geneID);
					ncbiid.setTaxID(taxID);
					SvDBNCBIUni.upDateNCBIUni(ncbiid, false);
					String[] ss = affyInfo[i][10].split("///");
					for (String string2 : ss) {
						ncbiid.setAccID(string2.trim()); ncbiid.setDBInfo(dbinfo);
						ncbiid.setGeneId(geneID);
						ncbiid.setTaxID(taxID);
						SvDBNCBIUni.upDateNCBIUni(ncbiid, false);
					}
				}
			}
		}
		txtAffyInfo.ExcelWrite(result, "\t", 1, 1);
		}
		
	/**
	 * 
	 * @param affyInfo 含有affyID的一个cell，里面可能有"///"分割
	 * @param taxID
	 * @return
	 * string[2] 0:为"geneID"或"uniID"或"accID"
	 * 1: 具体的ID
	 * 如果0为accID则1为“”
	 */
	private static String[] getGenUniID(String affyInfo,int taxID) {
		String[] result = new String[2];
		result[0] = "accID";result[1] = "";
		if (affyInfo!=null && !affyInfo.equals("---") && !affyInfo.trim().equals("")) {
			String[] ss=affyInfo.split("///");
			for (int j = 0; j < ss.length; j++) {
				ArrayList<String> lsInfo = AnnoQuery.getNCBIUni(CopeID.removeDot(ss[j]), taxID);
				if(!lsInfo.get(0).equals("accID"))
				{
					result[0] = lsInfo.get(0);
					result[1] = lsInfo.get(1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 将某列affy信息导入数据库，如果该id在数据库中已经存在了，则跳过
	 * 内部不包含cope.removedot
	 * @param affyIDinfo 该affyID的信息，0：accID、geneID、uniID  1：具体geneID或uniID
	 * @param affyInfo 要插入数据库的某个affy表中的cell
	 * @param dbinfo 数据库名称
	 * @param taxID 物种名
	 * @param geneIDflag 是否是geneID
	 * @param uniIDflag 是否是uniID
	 * @return
	 */
	private static void upDateGenUniID(String[] affyIDinfo,String affyInfo,String dbinfo,int taxID,boolean geneIDflag, boolean uniIDflag) {
		if (geneIDflag || uniIDflag) {
			if (geneIDflag ) {
				NCBIID ncbiid = new NCBIID();
				String affyID[] = affyInfo.split("///");
				for (String string : affyID) {
					ncbiid.setAccID(string.trim()); ncbiid.setDBInfo(dbinfo);
					ncbiid.setGeneId(Integer.parseInt(affyIDinfo[1]));
					ncbiid.setTaxID(taxID);
					SvDBNCBIUni.upDateNCBIUni(ncbiid, false);
				}
			}
			else if(uniIDflag) {
				UniProtID uniProtID = new UniProtID();
				String affyID[] = affyInfo.split("///");
				for (String string : affyID) {
					uniProtID.setAccID(string); uniProtID.setDBInfo(dbinfo);
					uniProtID.setTaxID(taxID);
					uniProtID.setUniID(affyIDinfo[1]);
					SvDBNCBIUni.upDateNCBIUni(uniProtID, false);
				}
			}
		}
	}
	
}
