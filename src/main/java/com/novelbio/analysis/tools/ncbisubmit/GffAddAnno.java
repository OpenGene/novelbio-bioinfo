package com.novelbio.analysis.tools.ncbisubmit;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/** ϸ����glimmԤ���gff���򣬽����е�null�滻����ע�͵���Ϣ
 * ����֮ǰ�Ѿ��û���Խ�����������blast�����Կ��Ժܷ�����滻
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
	/** blast�ļ��������Ϣ����Ҫ���Ǳ�����ID �Է�����ID��ע�� */
	HashMap<String, String[]> mapGeneID_2_subjectIdProductNote = new HashMap<String, String[]>();
	boolean replaceAll = true;
	
	/** �Ƿ��滻���У�false�����滻null�� */
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
	/** ����ڰ˸��ļ������blast�������Ϣ
	 * ֻҪblast�н�����滻
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
	
	/** ����ڰ˸��ļ������blast�������Ϣ
	 * ֻ��null���滻
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
