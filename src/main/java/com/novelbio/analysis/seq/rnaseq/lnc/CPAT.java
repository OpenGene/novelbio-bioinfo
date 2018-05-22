package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene.GeneStructure;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

/**
 * simple script : cpat.py -g lncRNA_candiate.fa -x *_hexamer.table -d *_logit.RData -o  *.result.xls
 * @author bll
 */
public class CPAT implements IntCmdSoft {
	static Set<Integer> setModelSpecies = new HashSet<>();
	static {
		// 先写在这里把，不方便写入配置文件
		setModelSpecies.add(9606);
		setModelSpecies.add(10090);
		setModelSpecies.add(7227);
		setModelSpecies.add(7955);
	}
	
	List<String> lsCmd = new ArrayList<String>();
	/** 用来做训练集的mRNA序列 */
	List<String> lsmRNAseq = new ArrayList<String>();
	/** 用来做训练集的ncRNA序列 */
	List<String> lsncRNAseq = new ArrayList<String>();
	
	String speciesName = "Other";
	String fastaNeedPredict;
	String mRNAfile;
	String outFile;
	Species species;
	static double threshold = 0.0;
	boolean isModelSpecies;
	
	/** 设定物种信息，设定后会设定speciesName、isModelSpecies这些参数 */
	public void setSpecies(Species species) {
		this.species = species;
		if (species == null || species.getTaxID() == 0) return;
		if (setModelSpecies.contains(species.getTaxID())) {
			this.isModelSpecies = true;
			this.speciesName = species.getCommonName();
		}
	}
	/** 设置需要分析的物种信息 */
	public Species getSpecies() {
		return species;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public static double getThreshold() {
		return threshold;
	}
	/** 添加用来作训练集的mRNA序列，可添加多条 */
	public void addmRNAfile(String mRNAfile) {
		FileOperate.checkFileExistAndBigThanSize(mRNAfile, 0);
		lsmRNAseq.add(mRNAfile);
	}
	/** 添加用来作训练集的ncRNA序列，可添加多条 */
	public void addncRNAfile(String ncRNAfile) {
		FileOperate.checkFileExistAndBigThanSize(ncRNAfile, 0);
		lsncRNAseq.add(ncRNAfile);
	}
	/** 设定需要预测的序列，fasta格式 */
	public void setFastaNeedPredict(String fastaNeedPredict) {
		FileOperate.checkFileExistAndBigThanSize(fastaNeedPredict, 0);
		this.fastaNeedPredict = fastaNeedPredict;
	}
	/** 输出文件名<b>必须在第一时间设定</b> */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/** 添加用来做训练集的物种，<b>注意不要重复添加物种</b> */
	public void addSpeciesInfo(Species species) {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		if (!checkGffHash(gffChrAbs)) {
			gffChrAbs.close();
			return;
		}
		String mRNAseq = getmRNAFile(species, gffChrAbs);
		String ncRNAseq = getNcRNAFile(species, gffChrAbs);
		if (!FileOperate.isFileExistAndBigThanSize(mRNAseq, 0)) {
			lsmRNAseq.add(mRNAseq);
		}
		if (!FileOperate.isFileExistAndBigThanSize(ncRNAseq, 0)) {
			lsncRNAseq.add(ncRNAseq);
		}
	}
	
	private boolean checkGffHash(GffChrAbs gffChrAbs) {
		if (gffChrAbs.getGffHashGene() == null || !gffChrAbs.getGffHashGene().isContainNcRNA()) {
			return false;
		}
		return true;
	}
	
	private String getNcRNAFile(Species species, GffChrAbs gffChrAbs) {
		String ncFile = species.getRefseqNCfile();
		if (FileOperate.isFileExistAndBigThanSize(ncFile, 0)) {
			return ncFile;
		}
		String ncRNAfile = getOutFileTmp() + species.getCommonName().replace(" ", "_") + "ncRNA.fa";
		getSeqFile(gffChrAbs, GeneType.ncRNA, ncRNAfile);
		return ncRNAfile;
	}
	
	private String getmRNAFile(Species species, GffChrAbs gffChrAbs) {
		String mRNAfile = getOutFileTmp() + species.getCommonName().replace(" ", "_") + "mRNA.fa";
		getSeqFile(gffChrAbs, GeneType.mRNA, mRNAfile);
		gffChrAbs.close();
		return mRNAfile;
	}
	
	private void getSeqFile(GffChrAbs gffChrAbs, GeneType geneType, String outFile) {
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		gffChrSeq.setGeneStructure(GeneStructure.EXON);
		gffChrSeq.setGeneType(geneType);
		gffChrSeq.setGetAAseq(false);
		gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
		gffChrSeq.setGetAllIso(true);
		gffChrSeq.setGetIntron(false);
		gffChrSeq.setGetSeqGenomWide();
		gffChrSeq.setOutPutFile(outFile);
		gffChrSeq.run();
		gffChrSeq = null;
	}
	
	/**
	 * 准备序列<br>
	 * 0: mRNA序列<br>
	 * 1: ncRNA序列
	 * @return
	 */
	private String[] prepareFile() {
		if (isModelSpecies) return new String[]{"", ""};
		if (lsmRNAseq.isEmpty()) {
			throw new FileOperate.ExceptionFileNotExist("mRNA is not exist");
		}
		if (lsncRNAseq.isEmpty()) {
			throw new FileOperate.ExceptionFileNotExist("ncRNA is not exist");
		}
		String mRNAfile = getOutFileTmp() + speciesName + "mRNAfileRun";
		combFile(lsmRNAseq, mRNAfile);
		String ncRNAfile = getOutFileTmp() + speciesName + "ncRNAfileRun";
		combFile(lsncRNAseq, ncRNAfile);
		return new String[]{mRNAfile, ncRNAfile};
	}
	
	private void combFile(List<String> lsNcRNAfile, String outFile) {
		if (lsNcRNAfile.size() == 1) {
			FileOperate.copyFile(lsNcRNAfile.get(0), outFile, true);
		} else {
			TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
			for (String tmp : lsNcRNAfile) {
				TxtReadandWrite txtRead = new TxtReadandWrite(tmp);
				for (String string : txtRead.readlines()) {
					txtWrite.writefileln(string);
				}
				txtRead.close();
			}
			txtWrite.close();
		}
	}
	
	public void predict() {
		lsCmd.clear();
		//临时文件夹
		String outFileTmp = getOutFileTmp();	
		String hexFile = null, rDataFile = null;
		String[] mRNA2ncRNA = prepareFile();
		String mRNAseq = mRNA2ncRNA[0], ncRNAseq = mRNA2ncRNA[1];
		if (!isModelSpecies) {
			//首先运行得到分析物种的Hexamer.table文件
			MakeHexamerTab makeHexamerTab = new MakeHexamerTab();
			makeHexamerTab.setmRNAseq(mRNAseq);
			makeHexamerTab.setNcRNAseq(ncRNAseq);
			makeHexamerTab.setOutHexName(outFileTmp + speciesName + "_Hexamer.table");
			makeHexamerTab.run();
			hexFile = makeHexamerTab.getOutHexFileName();
			lsCmd.addAll(makeHexamerTab.getCmdExeStr());
			//然后运行得到分析物种的logit.RData文件
			MakeLogitModel makeLogitModel = new MakeLogitModel();
			makeLogitModel.setmRNAseq(mRNAseq);
			makeLogitModel.setNcRNAseq(ncRNAseq);
			makeLogitModel.setHexamerTable(makeHexamerTab.getOutHexFileName());
			makeLogitModel.setOutPrefix(outFileTmp + speciesName);
			makeLogitModel.run();
			rDataFile = makeLogitModel.getOutRDataFile();
			lsCmd.addAll(makeLogitModel.getCmdExeStr());
		}
		//运行主程序cpat.py，进行序列编码能力预测的分析
		CPATmain cpaTmain = new CPATmain();
		cpaTmain.setSpeciesName(speciesName);
		cpaTmain.setFastaNeedPredict(fastaNeedPredict);
		cpaTmain.setHexTab(hexFile);
		cpaTmain.setLogRData(rDataFile);
		cpaTmain.setModelSpecies(isModelSpecies);
		cpaTmain.setOutPrefix(outFileTmp + speciesName + "_result.xls");
		cpaTmain.run();
		lsCmd.addAll(cpaTmain.getCmdExeStr());
		
		//过滤CPAT分析结果，得到编码能力低的序列，即为最终预测的新lncRNA
		FilterCPATResult filterCPATResult = new FilterCPATResult();
		filterCPATResult.filterCPATResult(outFileTmp + speciesName + "_result.xls");
	}
	
	private String getOutFileTmp() {
		String outPath = FileOperate.getPathName(outFile) + "cpatTmp" + FileOperate.getSepPath();
		FileOperate.createFolders(outPath);
		return outPath;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
}

/**
 * cpat的预先格式化工作<br>
 * 对于人、小鼠、斑马鱼、果蝇这四个物种不需要运行这两个程序
 * @author novelbio
 */
abstract class CPATformatAbs implements IntCmdSoft {
	String exePath;
	String mRNAseq;
	String ncRNAseq;

	public CPATformatAbs() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cpat);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	public void setmRNAseq(String mRNAseq) {
		this.mRNAseq = mRNAseq;
	}
	
	public void setNcRNAseq(String ncRNAseq) {
		this.ncRNAseq = ncRNAseq;
	}
	
	protected String[] getmRNASeq() {
		return new String[]{"-c", mRNAseq};
	}
	
	protected String[] getNcRNASeq() {
		return new String[]{"-n", ncRNAseq};
	}
	
	public void run() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.runWithExp("cpat error:");
	}
	
	protected abstract List<String> getLsCmd();
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<String>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
}
	
/**
 * 对于人、小鼠、斑马鱼、果蝇这四个物种不需要运行这两个程序
 * 输入protein的nr序列和ncRNA的nr序列,产生Hex文件
 * 格式类似 python make_hexamer_tab.py  -c rna_modify.fa -n ../data/NONCODEv4_tair.fa > Rice_hexamer.table
 * @author zongjie
 */
class MakeHexamerTab extends CPATformatAbs {
	String outHexFileName;
	
	/** 设定输出的hax文件名,包括路径 */
	public void setOutHexName(String outPath) {
		this.outHexFileName = outPath;
	}
	/** 获得输出文件名包含全路径 */
	public String getOutHexFileName() {
		return outHexFileName;
	}
	
	@Override
	protected List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("python");
		lsCmd.add(exePath + "make_hexamer_tab.py");
		ArrayOperate.addArrayToList(lsCmd, getmRNASeq());
		ArrayOperate.addArrayToList(lsCmd, getNcRNASeq());
		lsCmd.add(">"); lsCmd.add(outHexFileName);
		return lsCmd;
	}
}

/** 设定回归模型 */
class MakeLogitModel extends CPATformatAbs {
	String hexamerTable;
	String outPrefix;
	
	/** 通过{@link MakeHexamerTab}所获得的输出文件 */
	public void setHexamerTable(String hexamerTable) {
		this.hexamerTable = hexamerTable;
	}
	/** 设定输出前缀 */
	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}
	/** 运行完毕后用这个方法来获得那个有用的RData文件 */
	public String getOutRDataFile() {
		return outPrefix + "_train.RData";
	}
	@Override
	protected List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("python");
		lsCmd.add(exePath + "make_logitModel.py");
		ArrayOperate.addArrayToList(lsCmd, getHexTable());
		ArrayOperate.addArrayToList(lsCmd, getmRNASeq());
		ArrayOperate.addArrayToList(lsCmd, getNcRNASeq());
		lsCmd.add("-o"); lsCmd.add(getOutRDataFile());
		return lsCmd;
	}
	private String[] getHexTable() {
		return new String[]{"-x", hexamerTable};
	}
}

/** CPAT的主程序 */
class CPATmain implements IntCmdSoft {
	String exePath;
	
	/** 需要预测的序列文件，fasta格式文件 */
	String fastaNeedPredict;
	/** 预先构建的六聚体频率表，由 {@link MakeHexamerTab} 产生 */
	String hexTab;
	/** 预先构建的训练对数模型，该文件为二进制文件 */
	String logRData;
	/** 被分析物种名称，用于给生成的训练集名称 */
	String speciesName;
	/** 输出文件路径及名称 */
	String outPrefix;
	/** 编码阈值设置  */
	double codProb;
	
	/** 是否为模式物种，即为人、小鼠、斑马鱼、果蝇 */
	boolean isModelSpecies = false;
	
	public CPATmain() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cpat);
		this.exePath = softWareInfo.getExePathRun();
	}
	/** 是否为模式物种，即为人、小鼠、斑马鱼、果蝇 */
	public void setModelSpecies(boolean isModelSpecies) {
		this.isModelSpecies = isModelSpecies;
	}
	/** 需要预测的序列文件，fasta格式文件 */
	public void setFastaNeedPredict(String fastaNeedPredict) {
		this.fastaNeedPredict = fastaNeedPredict;
	}
	/** 预先构建的六聚体频率表，由 {@link MakeHexamerTab} 产生 */
	public void setHexTab(String hexTab) {
		this.hexTab = hexTab;
	}
	/** 预先构建的训练对数模型，该文件为二进制文件，由{@link MakeLogitModel} */
	public void setLogRData(String logRData) {
		this.logRData = logRData;
	}
	/** 输出文件夹 */
	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}
	
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	
	public void run() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(outPrefix);
		cmdOperate.runWithExp("CPAT error");
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
//		lsCmd.add("python");
		lsCmd.add(exePath + "cpat.py");
		ArrayOperate.addArrayToList(lsCmd, getFastaNeedPredict());
		if (isModelSpecies) {
			hexTab = exePath + "../dat/" + speciesName + "_Hexamer.tab";
			logRData = exePath + "../dat/" + speciesName + "_train.RData";		
		}
		ArrayOperate.addArrayToList(lsCmd, getHexTable());
		ArrayOperate.addArrayToList(lsCmd, getRDataFile());
		ArrayOperate.addArrayToList(lsCmd, getOutPath());
		return lsCmd;
	}
	
	private String[] getFastaNeedPredict() {
		return new String[]{"-g", fastaNeedPredict};
	}
	private String[] getHexTable() {
		return new String[]{"-x", hexTab};
	}
	private String[] getRDataFile() {
		return new String[]{"-d", logRData};
	}
	private String[] getOutPath() {
		return new String[]{"-o", outPrefix};
	}
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<String>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
}

class FilterCPATResult {
	String excelFileName;
	String outFileName;
	
	public void filterCPATResult(String excelFileName) {
		this.excelFileName = excelFileName;
		this.outFileName = FileOperate.changeFileSuffix(excelFileName, "_filter", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		List<String[]> lsAll = ExcelTxtRead.readLsExcelTxt(excelFileName,1);
		txtWrite.writefileln(lsAll.get(0));
		for (String[] content : lsAll.subList(1, lsAll.size())) {
			if (filter(content)) {
				txtWrite.writefileln(content);
			}
		}
		txtWrite.close();
	}
	private boolean filter(String[] info) {
		for (int i = 0; i <info.length; i++) {
			if (Double.parseDouble(info[info.length - 1])< CPAT.getThreshold()) {
				return true;
			}
		}
		return false;
	}
}
