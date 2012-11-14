package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneNCBI;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ������NCBI�����ص����У���fasta��ʽ�е���������Ϊ�ļ���
 * Ȼ��ϲ�Ϊһ���ļ�
 * @author zong0jie
 */
public class NCBIchromFaChangeFormat {
	public static void main(String[] args) {
		String file = "/media/winE/Bioinformatics/genome/human/hg19_GRCh37/ChromFa";
		String out = file + "/all/chrAll.fa";
		FileOperate.createFolders(FileOperate.getParentPathName(out));
		NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
		ncbIchromFaChangeFormat.setChromFaPath(file, "");
		ncbIchromFaChangeFormat.writeToSingleFile(out);
	}
	
	String chrFile = ""; String regx = "\\bchr\\w*";
	PatternOperate patChrID;
	
	public void setChromFaPath(String chromFaPath, String regx) {
		this.chrFile = chromFaPath;
		this.regx = regx;
	}
	public void writeToSingleFile(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String chrFileName : initialAndGetFileList()) {
			TxtReadandWrite txtRead = new TxtReadandWrite(chrFileName, false);
			writeToFile(patChrID.getPatFirst(FileOperate.getFileName(chrFileName)), txtRead, txtWrite);
		}
		txtWrite.close();
	}
	
	/** ��ʼ���������ļ����е����з���������ʽ���ı���<br>
	 * string[2] 1:�ļ��� 2����׺ */
	private ArrayList<String> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx.equals("") || regx == null) {
			regx = "\\bchr\\w*";
		}
		patChrID = new PatternOperate(regx, false);
		
		final PatternOperate patNum = new PatternOperate("\\d+", false);
		ArrayList<String> lsFileName = FileOperate.getFoldFileNameLs(chrFile,regx, "*");
		//������Ž�������
		//�����Ϊ����GATK����˳������
		Collections.sort(lsFileName, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String id1 = patNum.getPatFirst(FileOperate.getFileName(o1));
				String id2 = patNum.getPatFirst(FileOperate.getFileName(o2));
				if (id1 == null && id2 == null) {
					return FileOperate.getFileName(o1).compareTo(FileOperate.getFileName(o2));
				} else if (id1 == null) {
					return 1;
				} else if (id2 == null) {
					return -1;
				} else {
					Integer num1 = Integer.parseInt(id1);
					Integer num2 = Integer.parseInt(id2);
					return num1.compareTo(num2);
				}
			}
		});
		return lsFileName;
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
