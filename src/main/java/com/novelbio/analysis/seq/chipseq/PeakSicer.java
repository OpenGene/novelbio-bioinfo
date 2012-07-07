package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;
/**
 * SICER����Ҫ����SICER���Ľű���·������������������֣�effective genome size��
 * @author zong0jie
 *
 */
public class PeakSicer extends PeakCalling {
	public static void main(String[] args) {
		String bedFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.KO.D4.sorted-1-removed.bed";
		String outPrefix = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/sicer-df-K4/single/K4_K4";
		PeakSicer peakSicer = new PeakSicer(bedFile);
		peakSicer.setSpecies(SPECIES_MOUSE);
		peakSicer.setEffectiveGenomeSize(85);
		peakSicer.setChIPType(HISTONE_TYPE_H3K4);
		peakSicer.setOutPathPrefix(outPrefix);
		peakSicer.peakCallling();
		
		bedFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.WT.D0.sorted-1-removed.bed";
		outPrefix = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/sicer-df-K4/single/K4_WT0";
		peakSicer.setFile(bedFile);
		peakSicer.setOutPathPrefix(outPrefix);
		peakSicer.peakCallling();
		
		bedFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.WT.D4.sorted-1-removed.bed";
		outPrefix = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/sicer-df-K4/single/K4_WT4";
		peakSicer.setFile(bedFile);
		peakSicer.setOutPathPrefix(outPrefix);
		peakSicer.peakCallling();
	}
	
	private static Logger logger = Logger.getLogger(PeakSicer.class);
	private static final String SICER_PATH = NovelBioConst.PEAKCALLING_SICER_PATH;
	/**
	 * �����Ҫ����
	 */
	public static final String SPECIES_RICE = "tigr10";
	public static final String SPECIES_ARABIDOPSIS = "tair8";
	public static final String SPECIES_HUMAN = "hg19";
	public static final String SPECIES_C_ELEGAN = "ce";
	public static final String SPECIES_DROSOPHYLIA = "dm3";
	public static final String SPECIES_MOUSE = "mm9";
	public PeakSicer(String bedFile) {
		super(bedFile);
	}
	
	public static final int HISTONE_TYPE_H3K4 = 200;
	public static final int HISTONE_TYPE_H3K27 = 600;
	
	/** ���������ж��� */
	String cmdSingleCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER.sh ";
	/**  ���������޶��� */
	String cmdSingle = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-rb.sh ";
	/** ���������Ƚϣ�ÿ�����ж��� */
	String cmdCompCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df.sh ";
	/** ���������Ƚϣ�ÿ��û���� */
	String cmdComp = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df-rb.sh ";
	/**
	 * ��ʾ��reads��һͷ����fragment���յ�ľ���
	 * SICER�ڼ����ʱ��Ὣreads�ƶ���fregment����һ��ĵط���������һ����˵Illumina��solexa��������Ϊ250-500������ȡ250-300�ǱȽϺ��ʵ�
	 */
	int fragment_size = 250;
	double FDR = 0.01;
	/**
	 * E-value is not p-value. Suggestion for first try on histone modification data: E-
	value=100. If you find ~10000 islands using this evalue, an empirical estimate of FDR
	is 1E-2.
	 */
	int Evalue = 100;
	String species = "";
	String bedCol = "";
	/**
	 * �趨����
	 * @param bedCol
	 */
	public void setBedCol(String bedCol) {
		this.bedCol = bedCol;
	}

	/**
	 * �趨fragment���ȣ�Ĭ��250
	 * @param fragment_size
	 */
	public void setFragmentSize(int fragment_size) {
		this.fragment_size = fragment_size;
	}
	
	int redundancy_threshold = 1;
	public void setRedundancy_threshold(int redundancy_threshold) {
		this.redundancy_threshold = redundancy_threshold;
	}
	
	int windowSize = 200;
	int gapSIze = 200;

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	public void setGapSIze(int gapSIze) {
		this.gapSIze = gapSIze;
	}
	/**
	 * Ĭ��evalue 100
	 * FDR 0.01
	 * @param evalue
	 * @param FDR
	 */
	public void setEvalueFDR(int evalue, double FDR) {
		this.Evalue = evalue;
		this.FDR = FDR;
	}
	/**
	 * ͬһ��λ��һģһ����reads�����������ּ���
	 * �����Ϊ�˱��������������Ĭ��Ϊ1
	 * @param redundancy
	 */
	public void setRedundancy(int redundancy) {
		this.redundancy_threshold = redundancy;
	}
	/**
	 * ����趨��������Ͳ��趨windowSize��gapSIze
	 * H3K27����H3K4
	 * HISTONE_TYPE_H3K4
	 */
	public void setChIPType(int Type) {
		if (Type == HISTONE_TYPE_H3K27) {
			setWindowSize(200);
			setGapSIze(600);
		}
		else if (Type == HISTONE_TYPE_H3K4) {
			setWindowSize(200);
			setGapSIze(200);
		}
	}
	 
	@Override
	public void peakCallling() {
		ArrayList<String[]> lsIn = null;
		if (bedCol == null || bedCol.trim().equals("")) {
			lsIn = peakCallingSingle();
		}
		else {
			lsIn = peakCallingCol(bedCol, species);
		}
	}
	
	
	/**
	 * peak callingȻ��annotation����col
	 * @param bedTreat
	 * @param bedCol
	 * @param species
	 * @return
	 * ����peakcalling�Ľ������������
	 */
	private ArrayList<String[]> peakCallingSingle()
	{
		String parentPath = FileOperate.removeSep(FileOperate.getParentPathName(file));
		String outDir = FileOperate.removeSep(FileOperate.getParentPathName(outPrefix));
		String cmd = cmdSingle + parentPath + " " + FileOperate.getFileName(file) + " ";
		cmd = cmd + outDir + " ";
		cmd = cmd + species + " ";
		cmd = cmd + redundancy_threshold + " " + windowSize + " " + fragment_size + " " + effectiveGenomeSize + " " + gapSIze + " " + Evalue + " ";
		logger.info(cmd);
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd, "SICER_Peak");
		cmdOperate.run();
		
		String in = FileOperate.addSep(outDir) + FileOperate.getFileNameSep(file)[0] 
		+ "-W"+windowSize+"-G"+gapSIze + "-E"+ Evalue + ".scoreisland";
		
		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
		return lsIn;
	}
	/**
	 * δ����֤
	 * <b>Col��bed�ļ������bed�ļ���ͬһ���ļ�����</b>
	 * parameters.
       $ sh DIR/SICER.sh ["InputDir"] ["bed file"] ["control file"] ["OutputDir"] ["Species"] ["redundancy threshold"] 
       ["window size (bp)"] ["fragment size"] ["effective genome fraction"] ["gap size (bp)"]
       ["FDR"]

	 * peak callingȻ��annotation����col
	 * @param bedTreat
	 * @param bedCol
	 * @param species
	 * @return
	 */
	private ArrayList<String[]> peakCallingCol(String bedCol, String species)
	{
		String parentPath = FileOperate.removeSep(FileOperate.getParentPathName(file));
//		String parentCol = FileOperate.deleteSep(FileOperate.getParentPathName(bedCol));

		String cmd = cmdSingleCol + parentPath + " " + FileOperate.getFileName(file) + " ";
		cmd = cmd + bedCol + " ";                   //FileOperate.getFileName(bedCol) + " ";
		cmd = cmd + parentPath + " ";
		cmd = cmd + species + " ";
		cmd = cmd + redundancy_threshold + " " + windowSize + " " + fragment_size + effectiveGenomeSize + gapSIze + FDR;
		CmdOperate cmdOperate = new CmdOperate(cmd, "SICER_Peak");

		cmdOperate.run();
		//���������ļ������ƻ�������
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(file)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";

		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		//title�϶�������
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
		return lsIn;
	}
	/**
	 * ����֮��Ƚϣ�ÿһ�鶼��control
	 * @param bedTreat1
	 * @param bedCol1
	 * @param bedTreat2
	 * @param bedCol2
	 * @param species
	 * @param outFilePath
	 * @param prix
	 */
	public void peakCallingComp(String bedCol1, String bedTreat2, String bedCol2, String species,
			String outFilePath, String prix) {
		
	}
	/**
	 * <b>bedTreat2�ļ�������bed�ļ���ͬһ���ļ�����</b>
	 * $sh SICER-df-rb.sh ["KO bed file"] ["WT bed file"] ["window size (bp)"] ["gap size (bp)"] ["E-value"] ["FDR"]
	 * ����ͨ���޸ĸýű��ķ�ʽ���ڲ���ѡ��
	 * ����֮��Ƚϣ�ÿһ�鶼û����control
	 * @param bedTreat2
	 * @param species
	 * @param outFilePath
	 * @param prix
	 */
	public void peakCallingComp(String bedTreat2, String species, String prix) {
		String parentPath = FileOperate.removeSep(FileOperate.getParentPathName(file));
		
		String cmd = cmdComp +" " + file +" " + bedTreat2+ " " + species +" " + windowSize +" " + gapSIze +" " + Evalue +" " + FDR;
		CmdOperate cmdOperate = new CmdOperate(cmd, "SICER_Peak");
		cmdOperate.run();
		//���������ļ������ƻ�������
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(file)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";
		//TODO
		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		//title�϶�������
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
	}
	@Override
	public boolean setFileFormat(FormatSeq fileformat) {
		if (fileformat != FormatSeq.BED) {
			logger.error("SICER��֧�ֳ�bed�ļ�����ĸ�ʽ����");
			return false;
		}
		return true;
	}
	@Override
	public void setSpecies(String species) {
		this.species = species;
	}
	
}
