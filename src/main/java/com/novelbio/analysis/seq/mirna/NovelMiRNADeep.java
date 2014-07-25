package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.bed.BedRecord;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.mapping.MapDNA;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
//TODO 移动文件还不够好
/**
 * 新的miRNA的预测，基于mirDeep的算法
 * 注意bowtie必须在系统变量下。可以通过修改mapper.pl文件来设置bowtie的文件夹路径
 * @author zong0jie
 */
public class NovelMiRNADeep extends NovelMiRNApredict implements IntCmdSoft {
	Logger logger = Logger.getLogger(NovelMiRNADeep.class);
	
	/** miRDeep2是调用bowtie实现的 */
	static SoftWare softWareMap = SoftWare.bowtie;
	
	MapDNAint mapBowtie;
	int miRNAminLen = 18;
	String mirDeepPath = "";
	/** 输入的fasta格式，从bed文件转变而来，也可直接设定 */
	String fastaInput = "";
	String matureMiRNA = "";
	/** 成熟的近似物种miRNA序列，最好分成动物植物，线虫等等 */
	String matureRelateMiRNA;
	/** 本物种miRNA前体 */
	String hairpinMiRNA = "";
	String species = "";
	String chromFaIndexBowtie;
	/** 输出报告文件，通过生成随机的该文件名，来找到本次mirDeep所在的路径 */
	String reportFile;
	boolean createReportFile = true;
	/** 已经加过/了 */
	String outPath = null;
	
	String novelMiRNAhairpin = "";
	String novelMiRNAmature = "";
	String novelMiRNAdeepMrdFile = "";
	
	List<String> lsCmd = new ArrayList<>();
	boolean isFastq = false;
	
	public NovelMiRNADeep() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.mirDeep);
		mirDeepPath = softWareInfo.getExePathRun();
		mapBowtie = MapDNA.creatMapDNA(softWareMap);
	}
	
	@Override
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
		FileOperate.createFolders(outPath);
	}
	/** 是否为fastq文件，默认是fasta文件，为false */
	public void setFastq(boolean isFastq) {
		this.isFastq = isFastq;
	}
	
	/** 如果文件存在，则直接读取该mrd文件
	 * 如果文件不存在，则最后返回生成该mir文件
	 * @param novelMiRNAdeepMrdFile
	 */
	public void setNovelMiRNAdeepMrdFile(String novelMiRNAdeepMrdFile) {
		this.novelMiRNAdeepMrdFile = novelMiRNAdeepMrdFile;
	}
	
	/**
	 * 从bed文件转变为fasta格式，或直接设定fasta文件
	 * 设定待比对的短序列fasta文件名字，可以随便设定。如果不设定，则默认为输入bed文件+_Potential_DenoveMirna.fasta;
	 * 推荐不设定
	 * @param fastaOut
	 */
	public void setFastaInput(String fastaIn) {
		this.fastaInput = fastaIn;
	}
	/** 设定物种名 */
	public void setSpeciesName(String species) {
		this.species = species.replace(" ", "_");
	}
	
	/**
	 * 设定待比对的物种
	 * @param species 某物种
	 */
	public void setSpeciesChrIndex(Species species) {
		this.chromFaIndexBowtie = species.getIndexChr(softWareMap);
		mapBowtie.setChrIndex(chromFaIndexBowtie);
	}
	/**
	 * 设定待比对的物种index文件路径
	 * @param chromFaIndexBowtie 某物种序列的bowtie1索引
	 */
	public void setSpeciesChrIndex(String chromFaIndexBowtie) {
		this.chromFaIndexBowtie = chromFaIndexBowtie;
		mapBowtie.setChrIndex(chromFaIndexBowtie);
	}
	/**
	 * 设定一个随机的report的类型，采用日期时间+随机数的方式
	 * @return 
	 */
	private String getReportFileRandom() {
		if (createReportFile) {
			Random random = new Random();
			int randomInt = (int)(random.nextDouble() * 1000);
			reportFile = "report" + DateUtil.getDateDetail() + randomInt + ".log";
		}
		return reportFile;
	}
	/**
	 * 设定序列
	 * @param matureMiRNA 成熟的本物中miRNA
	 * @param matureRelateMiRNA 成熟的近似物种miRNA
	 * @param hairpinMiRNA 本物中miRNA前体
	 */
	public void setMiRNASeq(String matureMiRNA, String matureRelateMiRNA, String hairpinMiRNA) {
		this.matureMiRNA = matureMiRNA;
		this.matureRelateMiRNA = matureRelateMiRNA;
		this.hairpinMiRNA = hairpinMiRNA;
	}
	
	private String[] getSpecies() {
		if (species == null || species.equals("")) {
			return new String[]{"none"};
		}
		return new String[]{"-t", species};
	}
	private String getMatureMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(matureMiRNA, 0)) {
			return "none";
		}
		return matureMiRNA;
	}
	private String getMatureRelateMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(matureRelateMiRNA, 0)) {
			return "none";
		}
		return matureRelateMiRNA;
	}
	private String getPrecursorsMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(hairpinMiRNA, 0)) {
			return "none";
		}
		return hairpinMiRNA;
	}
	
	private String[] getMirBaseMrd() {
		if (!FileOperate.isFileExistAndBigThanSize(matureMiRNA, 0)) {
			return null;
		}
		return new String[]{"-q", "/media/winD/miRBase.mrd"};
	}

	private String getChromFaSeq() {
		return chromFaIndexBowtie;
	}
	private String getChromFaIndex() {
		String result = FileOperate.getParentPathNameWithSep(chromFaIndexBowtie) + FileOperate.getFileNameSep(chromFaIndexBowtie)[0];
		return result;
	}
	/**
	 * 将输入的bed文件比对基因组，获得没有mapping至正向exon的序列，然后写入文本并转化为fastq文件
	 * 然后转化为fastq文件以便进行后续分析
	 * @param fastaOut
	 */
	private void convertNoCDSbed2Fasta(BedSeq bedSeq, String fastaOut) {
		String out = FileOperate.changeFileSuffix(lsAlignSeqFile.iterator().next().getFileName(), "_Predict_Mirna", "bed");
		out = outPath + FileOperate.getFileName(out);
		TxtReadandWrite txtOut = new TxtReadandWrite(fastaOut, true);
		for (BedRecord bedRecord : bedSeq.readLines()) {
			txtOut.writefileln(bedRecord.getSeqFasta().toStringNRfasta());
		}
		txtOut.close();
		bedSeq.close();
	}
	/** 只生成文件名，并不生成实际文件 */
	private String getFastaMapFileName() {
		if (fastaInput == null || fastaInput.trim().equals("")) {
			fastaInput = FileOperate.changeFileSuffix(lsAlignSeqFile.iterator().next().getFileName(), "_Potential_DenoveMirna" + DateUtil.getDateAndRandom(), "fasta");
			fastaInput = outPath + FileOperate.getFileName(fastaInput);
		}
		return fastaInput;
	}
	private BedSeq getBedFile() {
		String out = FileOperate.changeFileSuffix(lsAlignSeqFile.iterator().next().getFileName(), "_Predict_Mirna", "bed.gz");
		out = outPath + FileOperate.getFileName(out);
		BedSeq bedSeq = getReadsNotOnCDS(out);
		return bedSeq;
	}
	/** 好像是输出的压缩的reads信息 */
	private String getCollapseReadsFa() {
		String fileName = FileOperate.changeFileSuffix(fastaInput, "_collapsed", "fasta");
		String resultName = outPath + FileOperate.getFileName(fileName);
		return resultName;
	}
	/** 好像是输出的压缩的reads信息 */
	private String getMappingArf() {
		String fileName = FileOperate.changeFileSuffix(fastaInput, "_collapsed_mapping", "arf");
		String resultName = outPath + FileOperate.getFileName(fileName);
		return resultName;
	}

	private String[] getReadsMinLen() {
		return new String[]{"-l", miRNAminLen + ""};
	}
	/**
	 * 设定miRNA的最短长度
	 * @param miRNAminLen 默认最短18bp
	 */
	public void setMiRNAminLen(int miRNAminLen) {
		this.miRNAminLen = miRNAminLen;
	}
	
	/** 默认看到存在mrd文件就会跳过去不执行 */
	public void predict() {
		if (StringOperate.isRealNull(novelMiRNAdeepMrdFile)) {
			novelMiRNAdeepMrdFile = outPath + "run" + "/output.mrd";
		}
		if (!FileOperate.isFileExistAndBigThanSize(novelMiRNAdeepMrdFile, 0)) {
			predictNovel();
		}
		readExistMrd();
	}
	
	private void predictNovel() {
		lsCmd.clear();
		mapping();
		mirDeep2Pl();
		moveAndCopeFile();
	}
	
	private void mapping() {
		mapBowtie.IndexMake();
		String fastaInput = getFastaMapFileName();
		String bedSeqFileName = "";
		if (!FileOperate.isFileExistAndBigThanSize(fastaInput, 0)) {
			BedSeq bedSeq = getBedFile();
			convertNoCDSbed2Fasta(bedSeq, fastaInput);
			bedSeqFileName = bedSeq.getFileName();
		}
		List<String> lsCmdRun = new ArrayList<>();
		lsCmdRun.add(mirDeepPath + "mapper.pl");
		String collapseReadsFa = getCollapseReadsFa();
		String arfFile = getMappingArf();
		if (FileOperate.isFileExistAndBigThanSize(arfFile, 0)) {
			return;
		}
		
		if (isFastq) {
			fastaInput = convert2Fasta(fastaInput);
		}

		String collapseTmp = FileOperate.changeFileSuffix(collapseReadsFa, "_tmp", null);
		String arfTmp = FileOperate.changeFileSuffix(arfFile, "_tmp", null);
		
		FileOperate.DeleteFileFolder(collapseTmp);
		FileOperate.delFile(arfTmp);
		
		lsCmdRun.add(fastaInput);
		lsCmdRun.add("-c"); 
		lsCmdRun.add("-j");
		ArrayOperate.addArrayToList(lsCmdRun, getReadsMinLen());
		lsCmdRun.add("-m");
		lsCmdRun.add("-p"); lsCmdRun.add(getChromFaIndex());
		lsCmdRun.add("-s"); lsCmdRun.add(collapseTmp);
		lsCmdRun.add("-t"); lsCmdRun.add(arfTmp);
		lsCmdRun.add("-v");
		CmdOperate cmdOperate = new CmdOperate(lsCmdRun);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("miRNAdeep2 mapper.pl error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		moveFile(collapseTmp, collapseReadsFa);
		moveFile(arfTmp, arfFile);
		
		lsCmd.add(cmdOperate.getCmdExeStr());
		FileOperate.DeleteFileFolder(fastaInput);
		FileOperate.DeleteFileFolder(bedSeqFileName);
		createReportFile = true;
	}
	
	private void moveFile(String oldFile, String newFile) {
		if (FileOperate.isFileExistAndBigThanSize(oldFile, 0)) {
			FileOperate.moveFile(true, oldFile, newFile);
		}
	}
	
	private String convert2Fasta(String fastqFile) {
		FastQ fastQ = new FastQ(fastaInput);
		String fastaOut = outPath + FileOperate.getFileName(fastaInput);
		fastaOut = FileOperate.changeFileSuffix(fastaOut, "", "fa");
		//遇到文件改名，处理完后再改回来
		if (FileOperate.isFileExistAndBigThanSize(fastaOut, 1_000_000)) {
			return fastaOut;
		}
		
		String fastaOutTmp = FileOperate.changeFileSuffix(fastaOut, "_tmp", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fastaOutTmp , true);
		for (FastQRecord fastQRecord : fastQ.readlines()) {
			txtWrite.writefileln(fastQRecord.getSeqFasta().toStringNRfasta(1000));
		}
		fastQ.close();
		txtWrite.close();
		FileOperate.moveFile(true, fastaOutTmp, fastaOut);
		return fastaOut;
	}
	
	private void mirDeep2Pl() {
		List<String> lsCmdRun = new ArrayList<>();
		lsCmdRun.add(mirDeepPath + "miRDeep2.pl");
		lsCmdRun.add(getCollapseReadsFa());
		lsCmdRun.add(getChromFaSeq());
		lsCmdRun.add(getMappingArf());
		lsCmdRun.add(getMatureMiRNA());
		lsCmdRun.add(getMatureRelateMiRNA());
		lsCmdRun.add(getPrecursorsMiRNA());
		ArrayOperate.addArrayToList(lsCmdRun, getSpecies());
//		ArrayOperate.addArrayToList(lsCmdRun, getMirBaseMrd());
		lsCmdRun.add("2>"); lsCmdRun.add(getReportFileRandom());
		CmdOperate cmdOperate = new CmdOperate(lsCmdRun);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			StringBuilder stringBuilder = new StringBuilder("miRNAdeep2 miRDeep2.pl error:\n");
			stringBuilder.append("cmdline: " + cmdOperate.getCmdExeStrReal()+"\n");
			if (!cmdOperate.getLsErrOut().isEmpty()) {
				stringBuilder.append("detail: ");
				for (String string : cmdOperate.getLsErrOut()) {
					stringBuilder.append(string);
				}
			}
			throw new ExceptionCmd(stringBuilder.toString());
		}
		lsCmd.add(cmdOperate.getCmdExeStr());
		createReportFile = false;
	}
	
	/**
	 * 将结果文件移动到指定位置
	 * 同时处理结果文件为指定格式
	 */
	private void moveAndCopeFile() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		String suffix = null;
		try {
			suffix = getResultFileSuffixFromReportLog();
		} catch (Exception e) {
			return;
		}
		String expression_html = "expression_" + suffix + ".html";
		String result_html = "result_" + suffix + ".html";
		String miRNAs_expressed_all_samples = "miRNAs_expressed_all_samples_" + suffix + ".csv";
		String mirDeep_result_Path = "result_" + suffix + ".csv";
		
		String expression_analyses_Path = "expression_analyses/expression_analyses_" + suffix;
		String mirDeep_runs_Path = "mirdeep_runs/run_" + suffix;
		String mirDeep_pdfs_Path = "pdfs_" + suffix;
		
		FileOperate.DeleteFileFolder(mirDeep_runs_Path + "/tmp");
		
		lsFileName.add(expression_html);
		lsFileName.add(result_html);
		lsFileName.add(miRNAs_expressed_all_samples);
		lsFileName.add(mirDeep_result_Path);
		
		lsFileName.add(expression_analyses_Path);
		lsFileName.add(mirDeep_runs_Path);
		lsFileName.add(mirDeep_pdfs_Path);
		
		lsFileName.add(getReportFileRandom());
		
		for (String string : lsFileName) {
			String fileName = FileOperate.getFileName(string);
			FileOperate.moveFile(string, outPath, fileName.replace("_" + suffix, ""), true);
			logger.info("move:" + string + "\t" + "to:" +fileName.replace("_" + suffix, ""));
		}
		
		//删除3天前做的项目，但是不删除本次做的东西，以防删错
		List<String> lsOldPrepareTmp = FileOperate.getFoldFileNameLs("", "dir_prepare_signature", null);
		for (String string : lsOldPrepareTmp) {
			long time = DateUtil.getNowTimeLong() - FileOperate.getTimeLastModify(string);
			if (time > 86400000 * 3) {
				FileOperate.DeleteFileFolder(string);
			}
		}
		lsOldPrepareTmp = FileOperate.getFoldFileNameLs("mirdeep_runs", "run", null);
		for (String string : lsOldPrepareTmp) {
			if (DateUtil.getNowTimeLong() - FileOperate.getTimeLastModify(string) > 86400000 * 3) {
				FileOperate.DeleteFileFolder(string);
			}
		}
	}
	/**
	 * 读取已有的mrd文件
	 * 仅用于测试
	 */
	private void readExistMrd() {
		novelMiRNAhairpin =  FileOperate.getParentPathNameWithSep(outPath) + "novelMiRNA/hairpin.fa";
		novelMiRNAmature =  FileOperate.getParentPathNameWithSep(outPath) + "novelMiRNA/mature.fa";
//		HashSet<String> setMirPredictName = getSetMirPredictName(outFinal + "result.csv");
		ListMiRNAdeep.extractHairpinSeqMatureSeq(novelMiRNAdeepMrdFile, novelMiRNAmature, novelMiRNAhairpin);
	}
	
	/** 查看reportlog，返回结果的后缀 */
	private String getResultFileSuffixFromReportLog() {
		String suffix = null;
		TxtReadandWrite txtReport = new TxtReadandWrite(getReportFileRandom(), false);
		for (String string : txtReport.readlines()) {
			string = string.trim();
			if (string.startsWith("mkdir")) {
				suffix = string.replace("mkdir mirdeep_runs/run_", "");
				break;
			}
		}
		txtReport.close();
		if (suffix == null) {
			logger.error("没有找到report里面的文件名:" + getReportFileRandom());
		}
		return suffix;
	}
	
	/**
	 * 从mirDeep的结果文件中获得新miRNA的名字
	 * @param mirDeepResultCvs
	 * @return
	 */
	private HashSet<String> getSetMirPredictName(String mirDeepResultCvs) {
		TxtReadandWrite txtReadResult = new TxtReadandWrite(mirDeepResultCvs, false);
		HashSet<String> setMirPredictName = new HashSet<String>();
		boolean flagGetPredictMiRNA = false;
		for (String string : txtReadResult.readlines()) {
			if (string.equals("novel miRNAs predicted by miRDeep2")) {
				flagGetPredictMiRNA = true;
			}
			if (string.trim().equals("") && flagGetPredictMiRNA == true) {
				break;
			}
			if (flagGetPredictMiRNA) {
				String[] ss = string.split("\t");
				setMirPredictName.add(ss[0]);
			}
		}
		txtReadResult.close();
		return setMirPredictName;
	}
	

	
	public String getNovelMiRNAhairpin() {
		return novelMiRNAhairpin;
	}
	public String getNovelMiRNAmature() {
		return novelMiRNAmature;
	}
	/** mrd文件是mirDeep的新miRNA序列信息 */
	public String getNovelMiRNAdeepMrdFile() {
		return novelMiRNAdeepMrdFile;
	}
	/**
	 * 测试用
	 * @param novelMiRNAhairpin
	 * @param novelMiRNAmature
	 * @param novelMiRNAdeepMrdFile
	 */
	protected void setCalNovelMiRNACountNovelMiRNASeq(String novelMiRNAhairpin, String novelMiRNAmature, String novelMiRNAdeepMrdFile) {
		this.novelMiRNAhairpin = novelMiRNAhairpin;
		this.novelMiRNAmature = novelMiRNAmature;
		this.novelMiRNAdeepMrdFile = novelMiRNAdeepMrdFile;
	}

	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
	public void clear() {
		miRNAminLen = 18;
		mirDeepPath = "";
		/** 输入的fasta格式，从bed文件转变而来，也可直接设定 */
		fastaInput = "";
		matureMiRNA = "";
		/** 成熟的近似物种miRNA序列，最好分成动物植物，线虫等等 */
		matureRelateMiRNA = null;
		/** 本物种miRNA前体 */
		hairpinMiRNA = "";
		species = "";
		chromFaIndexBowtie = null;
		/** 输出报告文件，通过生成随机的该文件名，来找到本次mirDeep所在的路径 */
		reportFile = null;
		createReportFile = true;
		/** 已经加过/了 */
		outPath = null;
		
		novelMiRNAhairpin = "";
		novelMiRNAmature = "";
		novelMiRNAdeepMrdFile = "";
		
		lsCmd = new ArrayList<>();
		isFastq = false;
	}

}
