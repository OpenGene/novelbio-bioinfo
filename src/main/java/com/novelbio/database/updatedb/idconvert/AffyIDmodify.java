package com.novelbio.database.updatedb.idconvert;

import java.util.ArrayList;
import java.util.HashMap;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modcopeid.CopeID;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServUpDBNCBIUni;


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
			hashAffyDBinfo.put(10090, NovelBioConst.DBINFO_AFFY_MOUSE_430_2);
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
	public static void getInfo(int taxID,String affyInput,int rowstart) throws Exception {
		HashMap<Integer, String> hashAffyDBinfo = getHashAffyDBinfo();
		String dbinfo = hashAffyDBinfo.get(taxID);
		
		ExcelOperate excelAffy = new ExcelOperate();
		excelAffy.openExcel(affyInput);
		
		String[][] affyInfo = excelAffy.ReadExcel(rowstart, 1, excelAffy.getRowCount(), excelAffy.getColCount());
		
//		Pattern patgbTransMambrance =Pattern.compile("([A-Z_]+?\\d+?)(\\.\\d){0,1}\\s//", Pattern.CASE_INSENSITIVE);  
//	    //Pattern patgbIRP =Pattern.compile("([A-Za-z_]|\\d)+", Pattern.CASE_INSENSITIVE);  
//	    Matcher matID;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
//	    
		for (int i = 0; i < affyInfo.length; i++) {
			
			if (affyInfo[i][4].contains("control")) {
				continue;
			}
			String[] affyIDinfo = null;
			//没有entrzID存在，那么就用其他的方法看能不能找到entrzID
			if (affyInfo[i][18]==null||affyInfo[i][18].contains("--")||affyInfo[i][18].trim().equals(""))
			{
				///////////////////////////////////////////
				String[] tmppublicID = affyInfo[i][8].split("///");
				for (String string : tmppublicID) {
					affyIDinfo = getGenUniID(string, taxID);}
					if (!affyIDinfo[0].equals("accID")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][0], dbinfo, taxID,true);
						//将Hs.153360等导入数据库
						if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
							upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID,true);
						}
						continue;
					}				
				/////////////////////////////////////////////////////////
				String[] tmpRefPro = affyInfo[i][22].split("///");
				for (String string : tmpRefPro) {
					affyIDinfo = getGenUniID(string, taxID);
					if (!affyIDinfo[0].equals("accID")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][0], dbinfo, taxID,true);
						//将Hs.153360等导入数据库
						if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
							upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID,true);
						}
						continue;
					}
				}

				////////////////////////////////////////////////////////////////////////
				String[] tmpRefRNA = affyInfo[i][23].split("///");
				for (String string : tmpRefRNA) {
					affyIDinfo = getGenUniID(string, taxID);
					if (!affyIDinfo[0].equals("accID")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][0], dbinfo, taxID,true);
						//将Hs.153360等导入数据库
						if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
							upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID,true);
						}
						continue;
					}
				}
				
				////////////////////////////////////////////////////////////////////////
				String[] tmpUni = affyInfo[i][19].split("///");
				for (String string : tmpUni) {
					affyIDinfo = getGenUniID(string, taxID);
					if (!affyIDinfo[0].equals("accID")) {
						upDateGenUniID(affyIDinfo, affyInfo[i][0], dbinfo, taxID,true);
						//将Hs.153360等导入数据库
						if (!affyInfo[i][10].trim().equals("---") &&  !affyInfo[i][10].trim().trim().equals("")) {
							upDateGenUniID(affyIDinfo, affyInfo[i][10], dbinfo, taxID,true);
						}
						continue;
					}
				}
				////////////////////////////////////////////////////////////////////////
			}
			//有entrzID存在，直接导入NCBIID
			else
			{
				String[] geneIDstr = affyInfo[i][18].split("///");
				for (String string : geneIDstr)
				{
					int geneID =(int) Double.parseDouble(string);
					NCBIID ncbiid = new NCBIID();
					ncbiid.setAccID(affyInfo[i][0]); ncbiid.setDBInfo(dbinfo);
					ncbiid.setGeneId(geneID);
					ncbiid.setTaxID(taxID);
					ServUpDBNCBIUni.upDateNCBIUni(ncbiid, true,false);
					String[] ss = affyInfo[i][10].split("///");
					for (String string2 : ss) {
						ncbiid.setAccID(string2.trim()); ncbiid.setDBInfo(dbinfo);
						ncbiid.setGeneId(geneID);
						ncbiid.setTaxID(taxID);
						ServUpDBNCBIUni.upDateNCBIUni(ncbiid,true, false);
					}
				}
			}
		}
	}
	/**
	 * 
	 * @param affyInfo 含有affyID的一个cell，里面不能有"///"分割
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
			ArrayList<String> lsInfo = ServAnno.getNCBIUni(CopeID.removeDot(affyInfo), taxID);
			if(!lsInfo.get(0).equals("accID"))
			{
				result[0] = lsInfo.get(0);
				result[1] = lsInfo.get(1);
			}	
		}
		return result;
	}
	
	/**
	 * 将某列affy信息导入数据库，如果该id在数据库中已经存在了，则跳过
	 * 内部包含cope.removedot
	 * @param affyIDinfo 该affyID的信息，0：accID、geneID、uniID  1：具体geneID或uniID
	 * @param affyInfo 要插入数据库的某个affyID
	 * @param dbinfo 数据库名称
	 * @param taxID 物种名
	 * @param geneID 是否将geneID纳入查找范围
	 * @return
	 */
	private static void upDateGenUniID(String[] affyIDinfo,String affyInfo,String dbinfo,int taxID,boolean geneID) {
			if (affyIDinfo[0].equals("geneID") ) {
				NCBIID ncbiid = new NCBIID();
				String[] tmp = affyInfo.split("///");
				for (String string : tmp) {
					ncbiid.setAccID(CopeID.removeDot(string)); ncbiid.setDBInfo(dbinfo);
					ncbiid.setGeneId(Integer.parseInt(affyIDinfo[1]));
					ncbiid.setTaxID(taxID);
					ServUpDBNCBIUni.upDateNCBIUni(ncbiid, geneID,true);
				}
				
			}
			else if(affyIDinfo[0].equals("uniID") ) {
				UniProtID uniProtID = new UniProtID();
				String[] tmp = affyInfo.split("///");
				for (String string : tmp) {
					uniProtID.setAccID(CopeID.removeDot(string)); uniProtID.setDBInfo(dbinfo);
					uniProtID.setTaxID(taxID);
					uniProtID.setUniID(affyIDinfo[1]);
					ServUpDBNCBIUni.upDateNCBIUni(uniProtID, geneID,true);
				}
			}
		
	}
	
}
