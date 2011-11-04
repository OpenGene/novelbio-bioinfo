package com.novelbio.database.updatedb.idconvert;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.updatedb.database.UpDateFriceDB;

public class Yeast {
	/**
	 * 酵母的DBxREF，modify的那个，将第一列为数字的，除了protein gi全部删除
	 * @param txtDbxRef
	 * @param outFile
	 * @param UpDate 搜不到的怎么办 False，写入文本， True，用SGD作为目标写入UniID
	 */
	public static void copeID(String txtDbxRef,String outFile, boolean UpDate) {
		TxtReadandWrite txtDB = new TxtReadandWrite(txtDbxRef, false);
		ArrayList<String> lsString = txtDB.readfileLs();
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		
		for (String string : lsString) {
			if (string.startsWith("NC_")) {
				continue;
			}
			String[] ss = string.split("\t");
			String[] genUniID; 
			if (ss.length == 6) {
				genUniID = UpDateFriceDB.getGeneUniID(4932, ss[4],ss[5],ss[0],ss[3]);
			}
			else {
				genUniID = UpDateFriceDB.getGeneUniID(4932, ss[4], ss[0],ss[3]);
			}
			int geneID = Integer.parseInt(genUniID[0]);
			ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
			String[] id = new String[2];
			
			id[0] = ss[0];
			if (ss[2].equals("UniProt/Swiss-Prot ID")) {
				id[1] = NovelBioConst.DBINFO_UNIPROT_GenralID;
			}
			else if (ss[2].equals("UniProt/TrEMBL ID")) {
				id[1] = NovelBioConst.DBINFO_ENSEMBL_TRS;
			}
			else if (ss[2].equals("UniParc ID")) {
				id[1] = NovelBioConst.DBINFO_UNIPROT_UNIPARC;
			}
			else if (ss[2].equals("NCBI protein GI")) {
				id[1] = NovelBioConst.DBINFO_NCBI_ACC_PROGI;
			}
			else if (ss[2].equals("RefSeq Accession")) {
				id[1] = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
			}
			else if (ss[2].equals("RefSeq protein version ID")) {
				id[1] = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
			}
			else if (ss[1].equals("NCBI") && ss[2].equals("Gene ID")) {
				geneID = Integer.parseInt(ss[0]); id[0] = "";
			}
			else {
				id[1] = NovelBioConst.DBINFO_NCBI;
			}
			lsAccIDInfo.add(id);
			
			String[] id1 = new String[2];
			id1[0] = ss[3]; id1[1] = NovelBioConst.DBINFO_NCBI;
			lsAccIDInfo.add(id1);
			
			String[] id2 = new String[2];
			id2[0] = ss[4]; id2[1] = NovelBioConst.DBINFO_SSC_ID;
			lsAccIDInfo.add(id2);
			
			if (ss.length == 6) {
				String[] id3 = new String[2];
				id3[0] = ss[5]; id3[1] = NovelBioConst.DBINFO_SYMBOL;
				lsAccIDInfo.add(id3);
			}
			
			if (geneID != 0 || (genUniID[1] != null && !genUniID[1].equals(""))) {
				UpDateFriceDB.upDateNCBIUniID(geneID, genUniID[1], 4932, false, lsAccIDInfo, NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
			}
			else {
				if (UpDate) {
					UpDateFriceDB.upDateNCBIUniID(geneID, ss[4], 4932, false, lsAccIDInfo, NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
				}
				else {
					txtOut.writefileln(string);
				}
			}
		}
		txtOut.close();
	}
	/**
	 * 将SGD_features.tab.txt装入geneInfo，如果没有查到accID，那么先装入accID（装入uniID），再写入geneInfo
	 */
	public static void copeID(String SGD_featuresFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(SGD_featuresFile, false);
		ArrayList<String> lsInfo = txtReadandWrite.readfileLs();
		for (String string : lsInfo) {
			String[] ss = string.split("\t");
			if (ss.length < 16) {
				continue;
			}
			String[] genUniID = UpDateFriceDB.getGeneUniID(4932, ss[0],ss[3],ss[4]);
			//没有该ID，就在数据库中加上
			if (genUniID[0].equals("0") && (genUniID[1] == null || genUniID[1].equals(""))) {
				ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
				String[] id = new String[2];
				id[0] = ss[0]; id[1] = NovelBioConst.DBINFO_SSC_ID;
				lsAccIDInfo.add(id);
				
				String[] id1 = new String[2];
				id1[0] = ss[3]; id1[1] = NovelBioConst.DBINFO_NCBI;
				lsAccIDInfo.add(id1);
				
				String[] id2 = new String[2];
				id2[0] = ss[4]; id2[1] = NovelBioConst.DBINFO_SYMBOL;
				lsAccIDInfo.add(id2);
				
				UpDateFriceDB.upDateNCBIUniID(0, ss[0], 4932, false, lsAccIDInfo, NovelBioConst.DBINFO_SSC_ID, NovelBioConst.DBINFO_NCBI, NovelBioConst.DBINFO_SYMBOL);
				
			}
			String[] genUniID2 = UpDateFriceDB.getGeneUniID(4932, ss[0]);
			
			AGeneInfo aGeneInfo;
			if (!genUniID2[0].equals("0")) {
				aGeneInfo = new GeneInfo();
				aGeneInfo.setGeneUniID(genUniID2[0]);
				aGeneInfo.setIDType(CopedID.IDTYPE_GENEID);
			}
			else {
				aGeneInfo = new UniGeneInfo();
				aGeneInfo.setGeneUniID(genUniID2[1]);
				aGeneInfo.setIDType(CopedID.IDTYPE_UNIID);
			}
			
			//Symbol
			if (!ss[4].equals("")) {
				aGeneInfo.setSymbol(ss[4]);
			}
			else if (!ss[3].equals("")) {
				aGeneInfo.setSymbol(ss[3]);
			}
			
			if (!ss[5].equals("")) {
				aGeneInfo.setSynonyms(ss[5]);
			}
			
			if (!ss[2].equals("")) {
				aGeneInfo.setTypeOfGene(ss[2]);
			}
			
			if (!ss[13].equals("")) {
				aGeneInfo.setModDate(ss[13]);
			}
			
			if (!ss[15].equals("")) {
				aGeneInfo.setDescription(ss[15]);
			}
			UpDateFriceDB.upDateGenInfo(aGeneInfo);
		}
	}
	/**
	 * 导入Sc的GO数据
	 * gene_association.sgd
	 * @param GOfile
	 */
	public static void setGO(String GOfile) {
		TxtReadandWrite txtGO = new TxtReadandWrite(GOfile, false);
		ArrayList<String> lsGO = txtGO.readfileLs();
		for (String string : lsGO) {
			if (string.startsWith("!")) {
				continue;
			}
			
			String[] ss = string.split("\t");
			String[] genUniID = UpDateFriceDB.getGeneUniID(4932, ss[1]);
			Gene2Go gene2Go = new Gene2Go();
			gene2Go.setDataBase(ss[14]);
			gene2Go.setEvidence(ss[6]);
			gene2Go.setFunction(ss[8]);
			gene2Go.setGOID(ss[4]);
			gene2Go.setReference(ss[5]);
			UpDateFriceDB.upDateGenGO(Long.parseLong(genUniID[0]), genUniID[1], gene2Go);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//以下是毕赤酵母的数据库
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void PpaID(String fucctionFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(fucctionFile, false);
		String[][] lsFun = null;
		try {
			lsFun = txtReadandWrite.ExcelRead("\t", 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns(1, "\t"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 1; i < lsFun.length; i++) {
			String[] ss = lsFun[i];
			String[] genUniID = null;
			genUniID = UpDateFriceDB.getGeneUniID(644223, ss[0],ss[1],ss[2]);

			ArrayList<String[]> lsAccIDInfo = new ArrayList<String[]>();
			String[] id = new String[2];
			id[0] = ss[0]; id[1] = NovelBioConst.DBINFO_PPA_ID;
			lsAccIDInfo.add(id);
			
			String[] id1 = new String[2];
			id1[0] = ss[1]; id1[1] = NovelBioConst.DBINFO_NCBI;
			lsAccIDInfo.add(id1);
			
			String[] id2 = new String[2];
			id2[0] = ss[2]; id2[1] = NovelBioConst.DBINFO_PPA_NCBI_ID;
			lsAccIDInfo.add(id2);
			
			if (!genUniID[0].equals("0") || (genUniID[1] != null && !genUniID[1].equals(""))) {
				UpDateFriceDB.upDateNCBIUniID(Long.parseLong(genUniID[0]), genUniID[1], 644223, false, lsAccIDInfo, NovelBioConst.DBINFO_PPA_NCBI_ID);
			}
			else {
				UpDateFriceDB.upDateNCBIUniID(Long.parseLong(genUniID[0]), ss[0], 644223, false, lsAccIDInfo, NovelBioConst.DBINFO_PPA_NCBI_ID);
			}
		}
		
		for (int i = 1; i < lsFun.length; i++) {
			String[] ss = lsFun[i];
			String[] genUniID = null;
			genUniID = UpDateFriceDB.getGeneUniID(644223, ss[0],ss[1],ss[2]);
			AGeneInfo aGeneInfo = null;
			if (!genUniID[0].equals("0")) {
				aGeneInfo = new GeneInfo();
				aGeneInfo.setIDType(CopedID.IDTYPE_GENEID);
			}
			else if (genUniID[1] != null || !genUniID[1].equals("")) {
				aGeneInfo = new UniGeneInfo();
				aGeneInfo.setIDType(CopedID.IDTYPE_UNIID);
			}
			aGeneInfo.setGeneUniID(genUniID[0]);
			aGeneInfo.setDescription(ss[3]);
			UpDateFriceDB.upDateGenInfo(aGeneInfo);
		}
	}
	
	/**
	 * GO数据
	 * @param GOfile
	 */
	public static void PpaGO(String GOfile) {
		TxtReadandWrite txtGo = new TxtReadandWrite(GOfile,false);
		ArrayList<String> lsGo = txtGo.readfileLs();
		for (String string : lsGo) {
			String[] ss = string.split("\t");
			ss[0] = "PAS_"+ss[0];
			String[] genUniID = UpDateFriceDB.getGeneUniID(644223, ss[0]);
			if (!genUniID[0].equals("0") || (genUniID[1] != null && !genUniID[1].equals(""))) {
				
				String[] ssGo = ss[1].split(";");
				for (String string2 : ssGo) {
					Gene2Go gene2Go = new Gene2Go();
					gene2Go.setGOID(string2);
					UpDateFriceDB.upDateGenGO(Long.parseLong(genUniID[0]), genUniID[1], gene2Go);
				}
			}
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
