package com.novelbio.database.updatedb.idconvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class MaizeGDB {
	private static Logger logger = Logger.getLogger(MaizeGDB.class);

	public static void main(String[] args) {
		MaizeGDB maizeGDB = new MaizeGDB();
		String zmBxref = "/home/zong0jie/桌面/zeamaize/ZmB73_5a_xref.txt";
//		maizeGDB.upDateGeneInfo(4577, zmBxref);
		maizeGDB.upDateGeneGo(4577, zmBxref);
	}
	/**
	 * www.maizeseq
	 * uence.org
	 * 上的ZmB73_5a_xref.txt
	 */
	public void getMaizeAccID(int maizeTaxID, String ZmBxref)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(ZmBxref, false);
		String tmpMaizeID = "";
		HashMap<String, String> hashAccDB = new HashMap<String, String>();
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if (ss[1].equals("database")) {
				continue;
			}
			String tmpAccID = null;
			if (ss[0].startsWith("AC")) {
				tmpAccID = ss[0];
			}
			else {
				tmpAccID = ss[0].split("_")[0];
			}
			if (!tmpAccID.equals(tmpMaizeID)) {
				upDateNCBIID(maizeTaxID, hashAccDB);
				tmpMaizeID = tmpAccID;
				hashAccDB = new HashMap<String, String>();
				hashAccDB.put(tmpAccID, NovelBioConst.DBINFO_MAIZE_MGDB);
			}
			if (ss[1].equals("GO")) {
				continue;
			}
			else if (ss[1].equals("RefSeq_dna")) {
				ss[1] = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
			}
			else if (ss[1].equals("RefSeq_peptide")) {
				ss[1] = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN;
			}
			else if (ss[1].equals("Uniprot/SPTREMBL")) {
				ss[1] = NovelBioConst.DBINFO_UNIPROT_GenralID;
			}
			hashAccDB.put(ss[2], ss[1]);
		}
	}
	
	/**
	 * 将一个玉米基因对应的ID装入数据库
	 * @param hashAccDB
	 */
	private void upDateNCBIID(int maizeTaxID, HashMap<String, String> hashAccDB) {
		if (hashAccDB.size() == 0) {
			return;
		}
		ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
		int geneID = 0;
		String uniID = "";
		for (Entry<String, String> entry : hashAccDB.entrySet()) {
			String accID = entry.getKey();
			String DBinfo = entry.getValue();
			if (DBinfo.equals("EntrezGene")) {
				geneID = Integer.parseInt(accID);
			}
			else if (DBinfo.equals(NovelBioConst.DBINFO_MAIZE_MGDB)) {
				uniID = accID;
				String[] accIDInfo = new String[]{accID, DBinfo};
				lsAccIDInfo.add(accIDInfo);
			}
			else {
				String[] accIDInfo = new String[]{accID, DBinfo};
				lsAccIDInfo.add(accIDInfo);
			}
		}
		if (geneID == 0) {
			String[] tmpID = new String[lsAccIDInfo.size()];
			for (int i = 0; i < tmpID.length; i++) {
				tmpID[i] = lsAccIDInfo.get(i)[0];
			}
			String[] geneUniID = UpDateFriceDB.getGeneUniID(maizeTaxID, tmpID);
			geneID = Integer.parseInt(geneUniID[0]);
		}
		
		
		UpDateFriceDB.upDateNCBIUniID(geneID, uniID, maizeTaxID, false, lsAccIDInfo,
				NovelBioConst.DBINFO_MAIZE_MGDB, 
				NovelBioConst.DBINFO_NCBI_ACC_REFSEQ,NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN, NovelBioConst.DBINFO_UNIPROT_GenralID);
	}
	
	public void upDateGeneInfo(int maizeTaxID, String ZmBxref) {
		TxtReadandWrite txtRead = new TxtReadandWrite(ZmBxref, false);
		String tmpMaizeID = ""; String tmpDescription = "";
 		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if (ss.length < 4) {
				continue;
			}
			String tmpAccID = ss[0].split("_")[0];
			if (!tmpAccID.equals(tmpMaizeID)) {
				upDateGeneInfo(maizeTaxID, tmpMaizeID, tmpDescription);
				tmpMaizeID = tmpAccID;
 				tmpDescription = "";
			}
			if (!ss[1].equals("GO")) {
				if (tmpDescription.equals("") ) {
					tmpDescription = ss[3];
				}
				else {
					if (ss[3].length() > tmpDescription.length() && (!ss[3].contains("uncharacterized") && ss[3].contains("hypothetical"))) {
						tmpDescription = ss[3];
					}
				}
			}
 		}
	}
	
	public void upDateGeneInfo(int maizeTaxID, String tmpMaizeID, String tmpDescription) {
		GeneID copedID = new GeneID(tmpMaizeID, maizeTaxID, false);
		AGeneInfo geneInfo = null;
		if (copedID.getIDtype().equals(GeneID.IDTYPE_GENEID)) {
			geneInfo = new GeneInfo();
		}
		else {
			geneInfo = new UniGeneInfo();
		}
		geneInfo.setGeneUniID(copedID.getGenUniID());
		geneInfo.setDescrp(tmpDescription);
		geneInfo.setSymb(tmpMaizeID);
		geneInfo.setIDType(copedID.getIDtype());
		UpDateFriceDB.upDateGenInfo(geneInfo, false);
	}
	
	public void upDateGeneGo(int maizeTaxID, String ZmBxref) {
		TxtReadandWrite txtRead = new TxtReadandWrite(ZmBxref, false);
		String tmpMaizeID = ""; String tmpDescription = "";
 		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String tmpAccID = ss[0].split("_")[0];
			if (!tmpAccID.equals(tmpMaizeID)) {
 				tmpMaizeID = tmpAccID;
 			}
			if (ss[1].equals("GO")) {
				GeneID copedID = new GeneID(tmpMaizeID, maizeTaxID, false);
				UpDateFriceDB.upDateGenGO(copedID, ss[2], NovelBioConst.DBINFO_MAIZE_MGDB);
			}
 		}
	}
 
	
	
}
