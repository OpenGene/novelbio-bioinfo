package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;
import java.util.HashMap;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.condHostInfoEnt;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;

public class anno {
	public static void main(String[] args) {
		anno();
	}
	
	private static void anno() {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		gffChrAnno.setFilterTssTes(new int[]{-2000,2000}, null);
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_WT_final_anno/";
		String txtFile = parent + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-3k+2k", "xls"));
		
		txtFile = parent + "W0sort-W200-G200-E100.scoreisland_score35.xls";
		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
//		String parent1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentKO_K4K27Down_yulu_method20111208/";
//		String parent2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentWT_K4K27Down_yulu_method20111208/";
//
//		anno anno = new anno();
//		anno.cmpAccID(10090, parent1 + "KO_0d_bivalent_-2k+2k.xls",
//				parent2 + "bivalent_anno_WE_-2k+2kTss.xls"
//		);
//		anno.getAccID(10090, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/TKO-D4 vs FH-D42.txt", "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/TKO-D4 vs FH-D4_changeID.txt");
		
		
	}
	
	
	HashMap<String, ArrayList<String>> hashGeID2String = null;
	public void cmpAccID(int taxID, String... file)
	{
		hashGeID2String = new HashMap<String, ArrayList<String>>();
		for (String string : file) {
			ArrayList<String[]> lsTmp = ExcelTxtRead.readLsExcelTxt(string, new int[]{1}, 1, -1);
			for (String[] strings : lsTmp) {
				String[] accID = strings[0].split("///");
				for (String string2 : accID) {
					if (string2.equals("")) {
						continue;
					}
					CopedID copedID = new CopedID(string2, taxID, false);
					String genID = copedID.getGenUniID();
					if (hashGeID2String.containsKey(genID)) {
						ArrayList<String> lsAccID = hashGeID2String.get(genID);
						if (lsAccID.contains(strings[0])) {
							continue;
						}
						else {
							lsAccID.add(strings[0]);
						}
					}
					else {
						ArrayList<String> lsAccID = new ArrayList<String>();
						lsAccID.add(strings[0]);
						hashGeID2String.put(genID, lsAccID);
					}
				}
			}
		}
	}
	
	public void getAccID(int taxID,String txtFile,String txtOut) {
		TxtReadandWrite txtOutFile = new TxtReadandWrite(txtOut, true);
		ArrayList<String[]> lsTmp = ExcelTxtRead.readLsExcelTxt(txtFile, 1, 0, 1, 0);
		for (String[] strings : lsTmp) {
			CopedID copedID = new CopedID(strings[0], taxID, false);
			ArrayList<String> lsAccID = hashGeID2String.get(copedID.getGenUniID());
			if (lsAccID == null || lsAccID.size() == 0) {
				strings[0] = strings[6];
				txtOutFile.writefileln(strings);
				continue;
			}
			for (String string : lsAccID) {
				strings[0] = string;
				txtOutFile.writefileln(strings);
			}
		}
		txtOutFile.close();
		
	}
	
	
	
	
	
	
	
	
	
}
