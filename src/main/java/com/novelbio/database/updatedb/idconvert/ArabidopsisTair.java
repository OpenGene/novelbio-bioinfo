package com.novelbio.database.updatedb.idconvert;
import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.updatedb.database.UpDateFriceDB;

public class ArabidopsisTair {
	private static Logger logger = Logger.getLogger(ArabidopsisTair.class);
	/**
	 * 将TAIR上的ID导入数据库
	 * 针对TAIR10_Locus_cDNA_associations和TAIR10_Model_cDNA_associations
	 * 第一列atID，第二列其他ID，之后的通通不管
	 */
	public void updateID(String IDfile) {
		int taxIDath = 3702;
		TxtReadandWrite txtArabID = new TxtReadandWrite(IDfile, false);
		ArrayList<String> lsID = txtArabID.readfileLs();
		for (String string : lsID) {
			String[] ss = string.split("\t");
			String atID = CopedID.removeDot(ss[0]);
			String otherID = CopedID.removeDot(ss[1]);
			//只取前两个ID
			String[] geneUniID = UpDateFriceDB.getGeneUniID(taxIDath,atID,otherID);
			ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
			lsAccIDInfo.add(new String[]{atID,NovelBioConst.DBINFO_ATH_TAIR});
			lsAccIDInfo.add(new String[]{otherID,""});
			if (!(geneUniID[0].equals("0") && (geneUniID[1]==null || geneUniID[1].equals("") ) )) {
				UpDateFriceDB.upDateNCBIUniID(Long.parseLong(geneUniID[0]), geneUniID[1], taxIDath, false, lsAccIDInfo, NovelBioConst.DBINFO_ATH_TAIR);
			}
			else {
				UpDateFriceDB.upDateNCBIUniID(0, atID, taxIDath, false, lsAccIDInfo, NovelBioConst.DBINFO_ATH_TAIR);
			}
		}
	}

	/**
	 * 给定TAIR10的arabidopsis的TAIR10_functional_descriptions.xls表，将annotation导入数据库
	 */
	public void addGenInfo(String annoFile)  {
		int taxID = 3702;
		TxtReadandWrite txtAnno = new TxtReadandWrite(annoFile, false);
		ArrayList<String> lsAnno = txtAnno.readfileLs();
		for (String string : lsAnno) {
			if (string.trim().equals("")) {
				continue;
			}
			String[] sstmp = string.split("\t");
			String[] ss = new String[5];
			for (int i = 0; i < ss.length; i++) {
				ss[i] = "";
			}
			for (int i = 0; i < sstmp.length; i++) {
				ss[i] = sstmp[i];
			}
			
			
			
			
			
			CopedID copedID = new CopedID(ss[0], taxID, false);
			if (copedID.getIDtype().equals(CopedID.IDTYPE_GENEID) )
			{
				GeneInfo geneInfo = new GeneInfo();
				geneInfo.setGeneID(Long.parseLong(copedID.getGenUniID()));
				GeneInfo geneInfoResult = DaoFSGeneInfo.queryGeneInfo(geneInfo);
				//如果没找到
				if (geneInfoResult == null) {
					geneInfo.setSymbol(CopedID.removeDot(ss[0]));
					geneInfo.setDescription(ss[1]+"//"+ss[2]);
					geneInfo.setOtherDesign(ss[3]);
					DaoFSGeneInfo.InsertGeneInfo(geneInfo);
				}
				//如果找到了
				else {
					geneInfoResult.setDescription(ss[1]+"//"+ss[2]);
						if (geneInfoResult.getOtherDesign() != null) {
							geneInfoResult.setOtherDesign(ss[3]
									+ geneInfoResult.getOtherDesign());
						} else
							geneInfoResult.setOtherDesign(ss[3]);
					DaoFSGeneInfo.upDateGeneInfo(geneInfoResult);
				}
			}
			else if(copedID.getIDtype().equals(CopedID.IDTYPE_UNIID) )
			{
				UniGeneInfo uniGeneInfo = new UniGeneInfo();
				uniGeneInfo.setGeneID(copedID.getGenUniID());
				
				UniGeneInfo uniGeneInfoResult = DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
				
				//如果没找到
				if (uniGeneInfoResult == null) {
					uniGeneInfo.setSymbol(CopedID.removeDot(ss[0]));
					uniGeneInfo.setDescription(ss[1]+"//"+ss[2]);
					uniGeneInfo.setOtherDesign(ss[3]);
					DaoFSUniGeneInfo.InsertUniGeneInfo(uniGeneInfo);
				}
				//如果找到了
				else {
					uniGeneInfoResult.setDescription(ss[1]+"//"+ss[2]);
						if (uniGeneInfoResult.getOtherDesign() != null) 
							uniGeneInfoResult.setOtherDesign(ss[3] + uniGeneInfoResult.getOtherDesign());
						else 
							uniGeneInfoResult.setOtherDesign(ss[3]);
					DaoFSUniGeneInfo.upDateUniGeneInfo(uniGeneInfoResult);
				}
			}
			else {
				logger.error("出现未知ID："+copedID.getAccID());
			}
		}
	}
	
	/**
	 * 读取ATH_GO_GOSLIM.txt文件，并导入数据库
	 * @throws Exception 
	 */
	public void addGO(String gotxt) throws Exception {
		int taxID = 3702;
		TxtReadandWrite txtGo = new TxtReadandWrite(gotxt, false);
		BufferedReader reader = txtGo.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			Gene2Go gene2Go = new Gene2Go();
			gene2Go.setDataBase(ss[13]);
			gene2Go.setEvidence(ss[9]);
			gene2Go.setGOID(ss[5]);
			gene2Go.setReference(ss[12]);
			gene2Go.setQualifier(ss[11] + "//"+ss[12]);
			
			CopedID copedID = new CopedID(ss[0], taxID, false);
			if (copedID.getIDtype().equals(CopedID.IDTYPE_GENEID)) {
				UpDateFriceDB.upDateGenGO(Long.parseLong(copedID.getGenUniID()), null, gene2Go);
			}
			else if(copedID.getIDtype().equals(CopedID.IDTYPE_UNIID))
				UpDateFriceDB.upDateGenGO(0, copedID.getGenUniID(), gene2Go);
			else {
				logger.error("数据库没有收录该基因："+ss[0]);
			}
		}		
		
	}
	
}
