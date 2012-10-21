package com.novelbio.analysis.tools.ncbisubmit;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 细菌用glimm预测的gff基因，将其中的null替换成有注释的信息
 * 在这之前已经用基因对近似物种做了blast，所以可以很方便的替换
 * @author zong0jie
 *
 */
public class GffAddAnno {
	public static void main(String[] args) {
		String gffFile = "/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno.gff";
		String blastFile = "/media/winF/NBC/Project/Project_WZF/blastNR2NC_009443Result";
		String subjectDescription = "/media/winF/NBC/Project/Project_WZF/geneName.txt";
		GffAddAnno gffAddAnno = new GffAddAnno();
		gffAddAnno.setGffFile(gffFile);
		gffAddAnno.setReplaceAll(true);
		gffAddAnno.readBlastFile(blastFile, subjectDescription);
		
		gffAddAnno.generateGffOutFile("/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All.gff");
	}
	String gffFile;
	/** blast文件里面的信息，主要就是本物中ID 对方物种ID和注释 */
	HashMap<String, String[]> mapGeneID_2_subjectIdProductNote = new HashMap<String, String[]>();
	boolean replaceAll = true;
	
	/** 是否替换所有，false：仅替换null的 */
	public void setReplaceAll(boolean replaceAll) {
		this.replaceAll = replaceAll;
	}
	public void setGffFile(String gffFile) {
		this.gffFile = gffFile;
	}
	public void readBlastFile(String blastFile, String subjectDescription) {
		HashMap<String, String[]> mapGeneID_2_ID_Product_Note = new HashMap<String, String[]>();
		TxtReadandWrite txtReadSubjectDescription = new TxtReadandWrite(subjectDescription, false);
		for (String content : txtReadSubjectDescription.readlines()) {
			String[] ss = content.split("\t");
			String geneID = ss[0];
			String product = ss[2];
			String note = "";
			try {
				note = ss[3];
			} catch (Exception e) {
				note = "";
			}
			mapGeneID_2_ID_Product_Note.put(geneID, new String[]{geneID, product, note});
		}
		
		TxtReadandWrite txtReadBlast = new TxtReadandWrite(blastFile, false);
		String subjectGeneIDlasts = "";
		for (String blastInfo : txtReadBlast.readlines()) {
			String[] ss = blastInfo.split("\t");
			String queryGeneID = ss[0];
			String subjectGeneID = ss[1];
			if (subjectGeneID.equals(subjectGeneIDlasts)) {
				continue;
			}
			if (Double.parseDouble(ss[2]) < 50 || Double.parseDouble(ss[10]) > 1e-20) {
				subjectGeneIDlasts = subjectDescription;
				continue;
			}
			String[] ID_Product_Note = mapGeneID_2_ID_Product_Note.get(subjectGeneID);
			mapGeneID_2_subjectIdProductNote.put(queryGeneID, ID_Product_Note);
		}
	}
	
	public void generateGffOutFile(String OutFile) {
		TxtReadandWrite txtReadGff = new TxtReadandWrite(gffFile, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(OutFile, true);
		for (String gffInfo : txtReadGff.readlines()) {
			String[] ss = gffInfo.split("\t");
			if (replaceAll) {
				ss[8] = replaceGeneDescripAll(ss[8]);
			} else {
				ss[8] = replaceGeneDescripNull(ss[8]);
			}
			txtWrite.writefileln(ss);
		}
		txtWrite.close();
	}
	/** 输入第八个文件，获得blast填充后的信息
	 * 只要blast有结果就替换
	 *  */
	private String replaceGeneDescripAll(String ss8) {
		String geneID = ss8.split(";")[0].replace("ID=", "");
		String[] gene2Descirp = mapGeneID_2_subjectIdProductNote.get(geneID);
		if (gene2Descirp == null || gene2Descirp[1].contains("hypothetical protein")) {
			return ss8;
		}
		String result = "ID=" + geneID + "; Name=" + gene2Descirp[0] + " Product=" + gene2Descirp[1];
		if (!gene2Descirp[2].equals("")) {
			result = result + "; Note=" + gene2Descirp[2];
		}
		return result;
	}
	
	/** 输入第八个文件，获得blast填充后的信息
	 * 只有null才替换
	 *  */
	private String replaceGeneDescripNull(String ss8) {
		String geneID = ss8.split(";")[0].replace("ID=", "");
		String descirp = ss8.split(";")[1].trim().replace("Name=", "").trim();
		if (!descirp.equalsIgnoreCase("null")) {
			return ss8;
		}
		String[] gene2Descirp = mapGeneID_2_subjectIdProductNote.get(geneID);
		if (gene2Descirp == null) {
			return ss8;
		}
		String result = "ID=" + geneID + "; Name=" + gene2Descirp[0] + " Product=" + gene2Descirp[1] + "; Note=" + gene2Descirp[2];
		return result;
	}
}
