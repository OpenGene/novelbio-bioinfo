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
 * SICER首先要配置SICER他的脚本的路径，设置他里面的物种，effective genome size等
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
	 * 这个需要更新
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
	
	/** 单个样本有对照 */
	String cmdSingleCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER.sh ";
	/**  单个样本无对照 */
	String cmdSingle = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-rb.sh ";
	/** 两个样本比较，每个都有对照 */
	String cmdCompCol = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df.sh ";
	/** 两个样本比较，每个没对照 */
	String cmdComp = "sh "+ FileOperate.addSep(SICER_PATH) + "SICER-df-rb.sh ";
	/**
	 * 表示从reads的一头到该fragment的终点的距离
	 * SICER在计算的时候会将reads移动到fregment长度一半的地方做修正。一般来说Illumina的solexa上样长度为250-500，所以取250-300是比较合适的
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
	 * 设定对照
	 * @param bedCol
	 */
	public void setBedCol(String bedCol) {
		this.bedCol = bedCol;
	}

	/**
	 * 设定fragment长度，默认250
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
	 * 默认evalue 100
	 * FDR 0.01
	 * @param evalue
	 * @param FDR
	 */
	public void setEvalueFDR(int evalue, double FDR) {
		this.Evalue = evalue;
		this.FDR = FDR;
	}
	/**
	 * 同一个位置一模一样的reads，最多允许出现几次
	 * 这个是为了避免非线性扩增。默认为1
	 * @param redundancy
	 */
	public void setRedundancy(int redundancy) {
		this.redundancy_threshold = redundancy;
	}
	/**
	 * 如果设定了这个，就不设定windowSize和gapSIze
	 * H3K27或者H3K4
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
	 * peak calling然后annotation，无col
	 * @param bedTreat
	 * @param bedCol
	 * @param species
	 * @return
	 * 返回peakcalling的结果，包含标题
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
	 * 未经验证
	 * <b>Col的bed文件必须和bed文件在同一个文件夹下</b>
	 * parameters.
       $ sh DIR/SICER.sh ["InputDir"] ["bed file"] ["control file"] ["OutputDir"] ["Species"] ["redundancy threshold"] 
       ["window size (bp)"] ["fragment size"] ["effective genome fraction"] ["gap size (bp)"]
       ["FDR"]

	 * peak calling然后annotation，有col
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
		//输出结果的文件名估计会有问题
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(file)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";

		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		//title肯定有问题
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
		return lsIn;
	}
	/**
	 * 两组之间比较，每一组都有control
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
	 * <b>bedTreat2文件必须与bed文件在同一个文件夹下</b>
	 * $sh SICER-df-rb.sh ["KO bed file"] ["WT bed file"] ["window size (bp)"] ["gap size (bp)"] ["E-value"] ["FDR"]
	 * 可以通过修改该脚本的方式调节参数选择
	 * 两组之间比较，每一组都没有有control
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
		//输出结果的文件名估计会有问题
		String in = FileOperate.addSep(parentPath) + " " + FileOperate.getFileNameSep(file)[0] + "-W"+windowSize+"-G"+gapSIze + "-E" + ".scoreisland";
		//TODO
		TxtReadandWrite txtRead = new TxtReadandWrite(in, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		//title肯定有问题
		String[] title = new String[]{"ChrID","StartLoc","EndLoc","Score"};
		lsIn.add(0,title);
	}
	@Override
	public boolean setFileFormat(FormatSeq fileformat) {
		if (fileformat != FormatSeq.BED) {
			logger.error("SICER不支持除bed文件以外的格式类型");
			return false;
		}
		return true;
	}
	@Override
	public void setSpecies(String species) {
		this.species = species;
	}
	
}
