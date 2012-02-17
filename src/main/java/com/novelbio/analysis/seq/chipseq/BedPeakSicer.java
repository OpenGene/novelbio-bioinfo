package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * SICER����Ҫ����SICER���Ľű���·������������������֣�effective genome size��
 * @author zong0jie
 *
 */
public class BedPeakSicer extends BedPeak implements PeakCalling{
	private static Logger logger = Logger.getLogger(BedPeakSicer.class);
	private static final String SICER_PATH = NovelBioConst.PEAKCALLING_SICER_PATH;
	/**
	 * �����Ҫ����
	 */
	public static final String SPECIES_RICE = "tair8";
	public static final String SPECIES_HUMAN = "hg19";
	public static final String SPECIES_C_ELEGAN = "ce";
	public static final String SPECIES_DROSOPHYLIA = "dm3";
	public static final String SPECIES_MOUSE = "mm9";
	public BedPeakSicer(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
	
	public static final int HISTONE_TYPE_H3K4 = 200;
	public static final int HISTONE_TYPE_H3K27 = 600;
	
	/**
	 * ���������ж���
	 */
	String cmdSingleCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER.sh ";
	/**
	 * ���������޶���
	 */
	String cmdSingle = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-rb.sh ";
	/**
	 * ���������Ƚϣ�ÿ�����ж���
	 */
	String cmdCompCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df.sh ";
	/**
	 * ���������Ƚϣ�ÿ��û����
	 */
	String cmdComp = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df-rb.sh ";
	/**
	 * ��ʾ��reads��һͷ����fragment���յ�ľ���
	 * SICER�ڼ����ʱ��Ὣreads�ƶ���fregment����һ��ĵط���������һ����˵Illumina��solexa��������Ϊ250-500������ȡ250-300�ǱȽϺ��ʵ�
	 */
	int fragment_size = 250;
	double effectiveGenomeSize = 0.82;
	double FDR = 0.01;
	/**
	 * E-value is not p-value. Suggestion for first try on histone modification data: E-
value=100. If you find ~10000 islands using this evalue, an empirical estimate of FDR
is 1E-2.
	 */
	int Evalue = 100;


	public static void main(String[] args) {
		String parString = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/Mapping/";
		BedPeakSicer bedPeakSicer = new BedPeakSicer(parString + "nature2007K27seSort.bed");
		bedPeakSicer.setGffFile(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		bedPeakSicer.setChIPType(BedPeakSicer.HISTONE_TYPE_H3K27);
		bedPeakSicer.setFilterTssTes(new int[]{-2000,2000}, null);
		bedPeakSicer.peakCallling(null, BedPeakSicer.SPECIES_MOUSE, "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER", "nature2007Sicer");
		
		
	}
	/**
	 * �趨fragment���ȣ�Ĭ��250
	 * @param fragment_size
	 */
	public void setFragmentSize(int fragment_size) {
		this.fragment_size = fragment_size;
	}
	
	/**
	 * ��readsһ������ʱ������ⱥ��������reads������reads���ȵ����ƣ�uniqmapping��reads��������ȫ����ȫ�����飬һ����˵���Խ�����ǵ����Լ��
	 * ��ôһ��25bp����Ϊ65%��35bp����75%��50bp 80% 100bp���ܸ�������������Ϊ100����
	 * @param effectiveGenomeSize
	 */
	public void setEffectiveGenomeSize(int effectiveGenomeSize) {
		this.effectiveGenomeSize = (double)effectiveGenomeSize/100;
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
	public void peakCallling(String bedCol, String species, String outFilePath, String fileName) {
		ArrayList<String[]> lsIn = null;
		if (bedCol == null || bedCol.trim().equals("")) {
			lsIn = peakCallingSingle(species, outFilePath);
		}
		else {
			lsIn = peakCallingCol(bedCol, species);
		}
		super.annoFilter(lsIn, 1, 2, 3, FileOperate.addSep(outFilePath)+fileName);
	}
	
	
	/**
	 * peak callingȻ��annotation����col
	 * @param bedTreat
	 * @param bedCol
	 * @param species
	 * @return
	 * ����peakcalling�Ľ������������
	 */
	private ArrayList<String[]> peakCallingSingle(String species, String outDir)
	{
		String bedFile = super.getSeqFile();
		String parentPath = FileOperate.deleteSep(FileOperate.getParentPathName(bedFile));
		
		String cmd = cmdSingle + parentPath + " " + FileOperate.getFileName(bedFile) + " ";
		cmd = cmd + outDir + " ";
		cmd = cmd + species + " ";
		cmd = cmd + redundancy_threshold + " " + windowSize + " " + fragment_size + " " + effectiveGenomeSize + " " + gapSIze + " " + Evalue + " ";
		
		logger.info(cmd);
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("SICER_Peak");
		
		String in = FileOperate.addSep(outDir) + FileOperate.getFileNameSep(bedFile)[0] 
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
		String bedFile = super.getSeqFile();
		String parentPath = FileOperate.deleteSep(FileOperate.getParentPathName(bedFile));
//		String parentCol = FileOperate.deleteSep(FileOperate.getParentPathName(bedCol));

		String cmd = cmdSingleCol + parentPath + " " + FileOperate.getFileName(bedFile) + " ";
		cmd = cmd + bedCol + " ";                   //FileOperate.getFileName(bedCol) + " ";
		cmd = cmd + parentPath + " ";
		cmd = cmd + species + " ";
		cmd = cmd + redundancy_threshold + " " + windowSize + " " + fragment_size + effectiveGenomeSize + gapSIze + FDR;
		CmdOperate cmdOperate = new CmdOperate(cmd);

		cmdOperate.doInBackground("SICER_Peak");
		//���������ļ������ƻ�������
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(bedFile)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";
		//TODO
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
			String outFilePath, String prix)
	{
		
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
	public void peakCallingComp(String bedTreat2, String species, String prix)
	{
		String bedFile = super.getSeqFile();
		String parentPath = FileOperate.deleteSep(FileOperate.getParentPathName(bedFile));
		
		String cmd = cmdComp +" " + super.getSeqFile() +" " + bedTreat2+ " " + species +" " + windowSize +" " + gapSIze +" " + Evalue +" " + FDR;
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("SICER_Peak");
		//���������ļ������ƻ�������
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(bedFile)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";
		//TODO
		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		//title�϶�������
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
	}
	
	
	public BedPeakSicer filterWYR(String filterOut) throws Exception {
		BedSeq bedSeq = super.filterWYR(filterOut);
		return new BedPeakSicer(bedSeq.getSeqFile());
	}

	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 */
	public BedPeakSicer sortBedFile(int chrID, String sortBedFile,int...arg) {
		super.sortBedFile(chrID, sortBedFile, arg);
		return new BedPeakSicer(super.getSeqFile());
	}
	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 */
	public BedPeakSicer sortBedFile(String sortBedFile) {
		super.sortBedFile(sortBedFile);
		return new BedPeakSicer(super.getSeqFile());
	}


	
	
	
	
	
}
