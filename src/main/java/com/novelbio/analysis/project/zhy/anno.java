package com.novelbio.analysis.project.zhy;

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
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE);
		gffChrAnno.setFilterTssTes(new int[]{-1500,0}, null);
		String parent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/peakCalling/PeakCalling_SICER/";
		String txtFile = parent + "2NseSort-W200-G200-E100.scoreisland.xls";
		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0", "xls"));
		
		txtFile = parent + "NseSort-W200-G200-E100.scoreisland.xls";
		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0", "xls"));
		
		txtFile = parent + "3NseSort-W200-G200-E100.scoreisland.xls";
		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0", "xls"));
		
		
		
//		gffChrAnno.setFilterTssTes(new int[]{-1500,0}, new int[]{0,1500});
//		txtFile = parent + "2NseSort-W200-G200-E100.scoreisland.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0Tss_0+1.5kTes", "xls"));
//		
//		txtFile = parent + "NseSort-W200-G200-E100.scoreisland.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0Tss_0+1.5kTes", "xls"));
//		
//		txtFile = parent + "3NseSort-W200-G200-E100.scoreisland.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-1.5k+0Tss_0+1.5kTes", "xls"));
		
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
