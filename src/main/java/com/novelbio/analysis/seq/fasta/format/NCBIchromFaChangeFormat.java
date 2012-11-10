package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneNCBI;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ������NCBI�����ص����У���fasta��ʽ�е���������Ϊ�ļ���
 * Ȼ��ϲ�Ϊһ���ļ�
 * @author zong0jie
 */
public class NCBIchromFaChangeFormat {
	public static void main(String[] args) {
//		String file = "/media/winE/Bioinformatics/genome/rat/rnor5/ChromFa";
//		String out = file + "/all/chrAll.fa";
//		FileOperate.createFolders(FileOperate.getParentPathName(out));
//		NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
//		ncbIchromFaChangeFormat.setChromFaPath(file, "");
//		ncbIchromFaChangeFormat.writeToSingleFile(out);
//		GffHashGeneNCBI.modifyNCBIgffFile("/media/winE/Bioinformatics/genome/rat/rnor5/gff/ref_Rnor_5.0_top_level.gff3");
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_NCBI, "/media/winE/Bioinformatics/genome/rat/rnor5/gff/ref_Rnor_5.0_top_level_modify.gff3");
		System.out.println(gffHashGene.searchISO("Ttn").getATGsite());
	
	}
	
	String chrFile = ""; String regx = null;
	public void setChromFaPath(String chromFaPath, String regx) {
		this.chrFile = chromFaPath;
		this.regx = regx;
	}
	public void writeToSingleFile(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String[] chrFileName : initialAndGetFileList()) {
			String fileName = getFileName(chrFileName);
			TxtReadandWrite txtRead = new TxtReadandWrite(fileName, false);
			writeToFile(chrFileName[0], txtRead, txtWrite);
		}
		txtWrite.close();
	}
	
	/** ��ʼ���������ļ����е����з���������ʽ���ı���<br>
	 * string[2] 1:�ļ��� 2����׺ */
	private ArrayList<String[]> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx.equals("") || regx == null)
			regx = "\\bchr\\w*";
		return FileOperate.getFoldFileName(chrFile,regx, "*");
	}
	private String getFileName(String[] chrFileName) {
		String fileNam;
		if (chrFileName[1].equals(""))
			fileNam = chrFile + chrFileName[0];
		else
			fileNam = chrFile + chrFileName[0] + "." + chrFileName[1];
		return fileNam;
	}
	/**
	 * ��chr�ϲ����������ҽ���һ�е����ָ�ΪchrID
	 * @param txtRead
	 * @param txtWrite
	 */
	private void writeToFile(String chrID, TxtReadandWrite txtRead, TxtReadandWrite txtWrite) {
		txtWrite.writefileln(">" + chrID);
		for (String seq : txtRead.readlines(2)) {
			txtWrite.writefileln(seq);
		}
		txtRead.close();
	}
}
