package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

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
	//cuffcompare �Ĳ���
	String ExePathCuffCompare = "";
	
	String seqFasta = "";
	String gtfFile = "";
	
	String outPath = "";
	/** �Ƿ�ɾ��һЩδ֪�ļ� */
	boolean clearFile = false;
	
	//cuffdiff�Ĳ���
	String exePathCuffDiff = "";
	int threadNum = 4;
	/** 
	 * װ����������Sam���ļ�
	 * ÿ������listSam��ʾһϵ�е��ظ�
	 *  */
	ArrayList<ArrayList<String>> lsSampleSamFile;
	/** ������ */
	ArrayList<String> lsSampleName;
	
	/** �Ƿ�ɾ��һЩδ֪�ļ� */
	public void setClearFile(boolean clearFile) {
		this.clearFile = clearFile;
	}
	/**
	 * �趨samtools���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 */
	public void setExePathCuffCompare(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePathCuffCompare = "";
		} else {
			this.ExePathCuffCompare = FileOperate.addSep(ExePathCuffCompare);
		}
	}
	
	/**
	 * �趨samtools���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 */
	public void setExePathCuffDiff(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePathCuffCompare = "";
		} else {
			this.ExePathCuffCompare = FileOperate.addSep(ExePathCuffCompare);
		}
	}
	
	public void setSeqFasta(String seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getOutPath() {
		return " -o " + outPath;
	}
	private String getInputGtf() {
		return " -r " + CmdOperate.addQuot(gtfFile) + " " + CmdOperate.addQuot(gtfFile);
	}
	public String getSeqFasta() {
		return " -s " + CmdOperate.addQuot(seqFasta);
	}
	
	/**
	 * ����޸ĵ�gtf�ļ���
	 */
	private String getModifiedGtf() {
		String cmd = ExePathCuffCompare + "cuffcompare " + getSeqFasta() + getOutPath() + " -CG " + getInputGtf();
		CmdOperate cmdOperate = new CmdOperate(cmd, "cuffcompare");
		cmdOperate.run();
		if (clearFile) {
			clearUnknownFile();
		}
		return changeGtfFileName();
	}
	
	/** ɾ��һЩδ֪�ļ� */
	private void clearUnknownFile() {
		String fileRefmap = outPath + ".finalTranscript.gtf.refmap";
		String fileTmap = outPath + ".finalTranscript.gtf.tmap";
		String fileLoci = outPath + ".loci";
		String fileStats = outPath + ".stats";
		String fileTracking = outPath + ".tracking";
		
		FileOperate.delFile(fileRefmap);
		FileOperate.delFile(fileTmap);
		FileOperate.delFile(fileLoci);
		FileOperate.delFile(fileStats);
		FileOperate.delFile(fileTracking);		
	}
	
	/**
	 * �޸�cuffcompare������gtf�ļ�����ͬʱ�����޸ĺ���ļ���
	 * @return
	 */
	private String changeGtfFileName() {
		String fileName = outPath + ".combined.gtf";
		String newFileName = FileOperate.changeFileSuffix(fileName, "", ".gtf");
		FileOperate.changeFileName(fileName, outPath + ".gtf", true);
		return newFileName;
	}
}
