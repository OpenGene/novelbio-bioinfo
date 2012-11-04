package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import net.sf.picard.sam.SamAlignmentMerger;

import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Cuffdiff {
	public static void main(String[] args) {
		Cuffdiff cuffdiff = new Cuffdiff();
		Species species = new Species(9606);
		cuffdiff.setGtfFile("/media/winF/NBC/Project/Project_FY/20120920/cufflinks/finalTranscript.gtf");
		cuffdiff.setSeqFasta(species.getChromFaPath());
		cuffdiff.setOutPath("/media/winF/NBC/Project/Project_FY/20120920/cufflinks/");
		cuffdiff.getModifiedGtf();
	}
	
	String seqFastaPath = "";
	String gtfFile = "";
	
	String outPath = "";
	/** �Ƿ�ɾ��һЩδ֪�ļ� */
	boolean clearFile = false;
	
	//cuffdiff�Ĳ���
	String exePath = "";
	int threadNum = 4;
	/** 
	 * װ����������Sam���ļ�
	 * ÿ������listSam��ʾһϵ�е��ظ�
	 *  */
	ArrayList<ArrayList<String>> lsSample2lsSamFile;
	/** ������ */
	ArrayList<String> lsSampleName;
	
	MapLibrary mapLibrary;
	
	/** �Ƿ�ɾ��һЩδ֪�ļ� */
	public void setClearFile(boolean clearFile) {
		this.clearFile = clearFile;
	}
	
	/**
	 * �趨cuffdiff���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.exePath = "";
		} else {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	
	public void setSeqFasta(String seqFastaPath) {
		this.seqFastaPath = seqFastaPath;
	}
	/** ������cuffcompare�������gtf�ļ� */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getThreadNum() {
		return " -p " + threadNum + " ";
	}
	
	private String getPathAndOtherParam() {
		return " -N -b " + seqFastaPath + " ";
	}

	private String getMapLibrary() {
		//TODO ���Ƿ��ؽ��ⷽʽ
		return "";
	}
	
	private String getGtfFile() {
		return CmdOperate.addQuot(gtfFile);
	}
	/** ����������� */
	private String getSampleName() {
		String out = " -L " + CmdOperate.addQuot(lsSampleName.get(0));
		for (int i = 1; i < lsSampleName.size(); i++) {
			out = out + "," + CmdOperate.addQuot(lsSampleName.get(i));
		}
		return out;
	}
	
	private String getSamleFile() {
		String sampleFile = "";
		for (ArrayList<String> lsFile : lsSample2lsSamFile) {
			sampleFile = sampleFile + " " + CmdOperate.addQuot(lsFile.get(0));
			for (int i = 1; i < lsFile.size(); i++) {
				sampleFile = sampleFile + "," + CmdOperate.addQuot(lsFile.get(i));
			}
		}
		return sampleFile;
	}
	/**
	 * ����޸ĵ�gtf�ļ���
	 */
	public String runCompareGtf() {
		String cmd = exePath + "cuffdiff " + getMapLibrary() + getPathAndOtherParam() + getThreadNum() + getSampleName();
		cmd = cmd + getGtfFile() + getSamleFile();
		CmdOperate cmdOperate = new CmdOperate(cmd, "cuffdiff");
		cmdOperate.run();
		//TODO ��÷��صĽ��
	}
}
