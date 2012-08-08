package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.analysis.tools.compare.runCompSimple;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/**
 * �µ�miRNA��Ԥ�⣬����mirDeep���㷨
 * ע��bowtie������ϵͳ�����¡�����ͨ���޸�mapper.pl�ļ�������bowtie���ļ���·��
 * @author zong0jie
 */
public class NovelMiRNADeep extends NovelMiRNApredict{
	public static void main(String[] args) {
		String parent = "/media/winF/NBC/Project/Project_LFJ_Lab/miRNA/result/tmpBed/";
		calNovelMiRNAexp(parent + "LFJ2BW1_CGATGT_L002_R1_001_filtered_Genome.bed", "2BW1");
		calNovelMiRNAexp(parent + "LFJ2BW2_TTAGGC_L002_R1_001_filtered_Genome.bed", "2BW2");
		calNovelMiRNAexp(parent + "LFJA3_ATCACG_L002_R1_001_filtered_Genome.bed", "A3");
		calNovelMiRNAexp(parent + "LFJmjj_TGACCA_L002_R1_001_filtered_Genome.bed", "mjj");

	}
	public static void calNovelMiRNAexp(String bedFile, String prefix) {		
		NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
		novelMiRNADeep.setBedSeqInput(bedFile);
		novelMiRNADeep.setOutPath("/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/");
		novelMiRNADeep.setOutPrefix(prefix);
		novelMiRNADeep.setCalNovelMiRNACountNovelMiRNASeq("/media/winF/NBC/Project/Project_LFJ_Lab/miRNA/result/LFJmiRNApredictDeep/novelMiRNA/hairpin.fa", 
				"/media/winF/NBC/Project/Project_LFJ_Lab/miRNA/result/LFJmiRNApredictDeep/novelMiRNA/mature.fa", 
				"/media/winF/NBC/Project/Project_LFJ_Lab/miRNA/result/LFJmiRNApredictDeep/run/output.mrd");
		novelMiRNADeep.getMirCount();
	}
	public static void pipeline(String[] args) {
		Species species = new Species(9606);
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bowtie);
		String bedFile = "/home/zong0jie/Desktop/platformtest/testCR_miRNA_Filtered_Genome_Tmp.bed";
		NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
		novelMiRNADeep.setBedSeqInput(bedFile);
		novelMiRNADeep.setOutPath("/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/");
		novelMiRNADeep.setFastaOut("/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/CR_predict_Tmp.fa");
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setOutPrefix("LFJ");
		novelMiRNADeep.predict();
		novelMiRNADeep.getMirCount();
	}
	Logger logger = Logger.getLogger(NovelMiRNADeep.class);
	
	MapBowtie mapBowtie = new MapBowtie(MapBowtie.VERSION_BOWTIE1);
	int miRNAminLen = 18;
	String mirDeepPath = "";
	/** �����fasta��ʽ����bed�ļ�ת�������Ҳ��ֱ���趨 */
	String fastaInput = "";
	String matureMiRNA = "";
	/** ����Ľ�������miRNA���У���÷ֳɶ���ֲ��߳�ȵ� */
	String matureRelateMiRNA;
	/** ������miRNAǰ�� */
	String hairpinMiRNA = "";
	String species = "";
	String chromFaIndexBowtie;
	/** ��������ļ���ͨ����������ĸ��ļ��������ҵ�����mirDeep���ڵ�·�� */
	String reportFile;
	boolean createReportFile = true;
	String outPath = null;
	String outPrefix = "";
	
	String novelMiRNAhairpin = "";
	String novelMiRNAmature = "";
	String novelMiRNAdeepMrdFile = "";
	
	@Override
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
	}
	public void setOutPrefix(String outPrefix) {
		if (outPrefix != null && !outPrefix.trim().equals("")) {
			this.outPrefix = outPrefix.trim() + "_";
		}
	}
	/**
	 * ��bed�ļ�ת��Ϊfasta��ʽ����ֱ���趨fasta�ļ�
	 * �趨���ȶԵĶ�����fasta�ļ����֣���������趨��������ᶨ����Ĭ��Ϊ����bed�ļ�+_Potential_DenoveMirna.fasta;
	 * �Ƽ����趨
	 * @param fastaOut
	 * */
	public void setFastaOut(String fastaIn) {
		this.fastaInput = fastaIn;
	}
	/** �趨���� */
	public void setSpecies(String species) {
		this.species = species.replace(" ", "_");
	}
	/**
	 * �趨һ�������report�����ͣ���������ʱ��+������ķ�ʽ
	 * @return 
	 */
	private String getReportFileRandom() {
		if (createReportFile) {
			Random random = new Random();
			int randomInt = (int)(random.nextDouble() * 1000);
			reportFile = "report" + DateTime.getDateDetail() + randomInt + ".log";
		}
		return reportFile;
	}
	/**
	 * �趨����
	 * @param matureMiRNA ����ı�����miRNA
	 * @param matureRelateMiRNA ����Ľ�������miRNA
	 * @param hairpinMiRNA ������miRNAǰ��
	 */
	public void setMiRNASeq(String matureMiRNA, String matureRelateMiRNA, String hairpinMiRNA) {
		this.matureMiRNA = matureMiRNA;
		this.matureRelateMiRNA = matureRelateMiRNA;
		this.hairpinMiRNA = hairpinMiRNA;
	}
	
	private String getSpecies() {
		if (species == null || species.equals("")) {
			return "none ";
		}
		return "-t " + species + " ";
	}
	private String getMatureMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(matureMiRNA, 1)) {
			return "none ";
		}
		return matureMiRNA + " ";
	}
	private String getMatureRelateMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(matureRelateMiRNA, 1)) {
			return "none ";
		}
		return matureRelateMiRNA + " ";
	}
	private String getPrecursorsMiRNA() {
		if (!FileOperate.isFileExistAndBigThanSize(hairpinMiRNA, 1)) {
			return "none ";
		}
		return hairpinMiRNA + " ";
	}
	/**
	 * �趨bowtie���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chromFaIndexBowtie ĳ�������е�bowtie����
	 */
	public void setExePath(String exePath, String chromFaIndexBowtie) {
		if (exePath != null && !exePath.trim().equals("")) {
			this.mirDeepPath = FileOperate.addSep(exePath);
		}
		this.chromFaIndexBowtie = chromFaIndexBowtie;
		mapBowtie.setExePath("", chromFaIndexBowtie);
	}
	private String getChromFaSeq() {
		return chromFaIndexBowtie + " ";
	}
	/** �����reads�ļ� */
	private String getFastaMappingFile() {
		if (fastaInput == null || fastaInput.trim().equals("")) {
			fastaInput = FileOperate.changeFileSuffix(bedSeqInput.getFileName(), "_Potential_DenoveMirna", "fasta");
		}
		if (!FileOperate.isFileExist(fastaInput)) {
			convertNoCDSbed2Fasta(fastaInput);
		}
		return fastaInput + " ";
	}
	/**
	 * ���ȶԻ�õ�bed�ļ�ת��Ϊfasta�ļ�
	 * @param fastaOut
	 */
	private void convertNoCDSbed2Fasta(String fastaOut) {
		String out = FileOperate.changeFileSuffix(bedSeqInput.getFileName(), "_Potential_DenoveMirna", null);
		BedSeq bedSeq = getBedReadsNotOnCDS(out);
		TxtReadandWrite txtOut = new TxtReadandWrite(fastaOut, true);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			txtOut.writefileln(bedRecord.getSeqFasta().toStringNRfasta());
		}
		txtOut.close();
	}
	/** �����������ѹ����reads��Ϣ */
	private String getCollapseReadsFa() {
		return FileOperate.changeFileSuffix(fastaInput, "_collapsed", "fasta") + " ";
	}
	/** �����������ѹ����reads��Ϣ */
	private String getMappingArf() {
		return FileOperate.changeFileSuffix(fastaInput, "_collapsed_mapping", "arf") + " ";
	}

	private String getReadsMinLen() {
		return "-l " + miRNAminLen + " ";
	}
	/**
	 * �趨miRNA����̳���
	 * @param miRNAminLen ���18bp
	 */
	public void setMiRNAminLen(int miRNAminLen) {
		this.miRNAminLen = miRNAminLen;
	}
	public void predict() {
		mapping();
		predictNovelMiRNA();
		moveFile();
	}
	private void mapping() {
		mapBowtie.IndexMakeBowtie();
		String cmdMapping = mirDeepPath + "mapper.pl " + getFastaMappingFile() +"-c -j " + getReadsMinLen();
		cmdMapping = cmdMapping + "-m -p " + getChromFaSeq() + "-s " + getCollapseReadsFa() + "-t " + getMappingArf() + "-v";
		CmdOperate cmdOperate = new CmdOperate(cmdMapping, "mirDeepMapping_" + species);
		cmdOperate.run();
		
		createReportFile = true;
	}
	private void predictNovelMiRNA() {
		String cmdPredict = mirDeepPath + "miRDeep2.pl " + getCollapseReadsFa() + getChromFaSeq() + getMappingArf() 
				+ getMatureMiRNA() + getMatureRelateMiRNA() + " " + getPrecursorsMiRNA() + getSpecies() + " 2> " + getReportFileRandom();
		CmdOperate cmdOperate = new CmdOperate(cmdPredict, "mirDeepPredict_" + species);
		cmdOperate.run();
		
		createReportFile = false;
	}
	/** �鿴reportlog�����ؽ���ĺ�׺ */
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
			logger.error("û���ҵ�report������ļ���:" + getReportFileRandom());
		}
		return suffix;		
	}
	/** ������ļ��ƶ���ָ��λ�� */
	private void moveFile() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		String suffix = getResultFileSuffixFromReportLog();
		
		String expression_html = "expression_" + suffix + ".html";
		String result_html = "result_" + suffix + ".html";
		String miRNAs_expressed_all_samples = "miRNAs_expressed_all_samples_" + suffix + ".csv";
		String mirDeep_result_Path = "result_" + suffix + ".csv";
		
		String expression_analyses_Path = "expression_analyses/expression_analyses_" + suffix;
		String mirDeep_runs_Path = "mirdeep_runs/run_" + suffix;
		String mirDeep_pdfs_Path = "pdfs_" + suffix;
		
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
			FileOperate.moveFile(string, outPath, outPrefix +fileName.replace("_" + suffix, ""), true);
			System.out.println("move:" + string + "     to:" + outPrefix +fileName.replace("_" + suffix, ""));
		}
		String outFinal = outPath + outPrefix;
		
		novelMiRNAdeepMrdFile = outFinal + "run" + "/output.mrd";
		novelMiRNAhairpin = outFinal + "novelMiRNA/hairpin.fa";
		novelMiRNAmature = outFinal + "novelMiRNA/mature.fa";
		
		extractHairpinSeqMatureSeq(novelMiRNAdeepMrdFile, outFinal + "result.csv", novelMiRNAmature, novelMiRNAhairpin);
	}
	private void extractHairpinSeqMatureSeq(String run_output_mrd, String result_csv, String outMatureSeq, String outPreSeq) {
		FileOperate.createFolders(FileOperate.getParentPathName(outMatureSeq));
		FileOperate.createFolders(FileOperate.getParentPathName(outPreSeq));
		
		TxtReadandWrite txtReadResult = new TxtReadandWrite(result_csv, false);
		TxtReadandWrite txtReadMrd = new TxtReadandWrite(run_output_mrd, false);
		
		TxtReadandWrite txtWriteMature = new TxtReadandWrite(outMatureSeq, true);
		TxtReadandWrite txtWritePre = new TxtReadandWrite(outPreSeq, true);
		
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
		boolean flagFindMiRNA = false;
		String mirName ="", mirSeq = "", mirModel = "";
		for (String string : txtReadMrd.readlines()) {
			if (string.startsWith(">")) {
				flagFindMiRNA = false;
				mirName = string.substring(1).trim();
				if (setMirPredictName.contains(mirName)) {
					flagFindMiRNA = true;
				}
			}
			if (flagFindMiRNA && string.startsWith("exp")) {
				mirModel = string.replace("exp", "").trim();
			}
			if (flagFindMiRNA && string.startsWith("pri_seq ")) {
				mirSeq = string.replace("pri_seq ", "").trim();
				ArrayList<SeqFasta> lSeqFastas = getMirDeepSeq(mirName, mirModel, mirSeq);
				txtWritePre.writefileln(lSeqFastas.get(0).toStringNRfasta());
				txtWriteMature.writefileln(lSeqFastas.get(1).toStringNRfasta());
				txtWriteMature.writefileln(lSeqFastas.get(2).toStringNRfasta());
			}
		}
		txtReadMrd.close();
		txtReadResult.close();
		txtWriteMature.close();
		txtWritePre.close();
	}
	/**
	 * ����RNAdeep�Ľ���ļ�����������ȡ����
	 * @param seqName
	 * @param mirModel
	 * @param mirSeq
	 * @return
	 * 0: precess
	 * 1: mature
	 * 2: star
	 */
	private ArrayList<SeqFasta> getMirDeepSeq(String seqName, String mirModel, String mirSeq) {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		
		SeqFasta seqFasta = new SeqFasta(seqName, mirSeq);
		seqFasta.setDNA(true);
		
		int startS = mirModel.indexOf("S"); int endS = mirModel.lastIndexOf("S");
		int startM = mirModel.indexOf("M"); int endM = mirModel.lastIndexOf("M");
		
		SeqFasta seqFastaMature = new SeqFasta(seqName + "_mature", mirSeq.substring(startM, endM));
		seqFastaMature.setDNA(true);
		SeqFasta seqFastaStar = new SeqFasta(seqName + "_star", mirSeq.substring(startS, endS));
		seqFastaStar.setDNA(true);
		
		lsResult.add(seqFasta);
		lsResult.add(seqFastaMature);
		lsResult.add(seqFastaStar);
		return lsResult;
	}
	public void setCalNovelMiRNACountNovelMiRNASeq(String novelMiRNAhairpin, String novelMiRNAmature, String novelMiRNAdeepMrdFile) {
		this.novelMiRNAhairpin = novelMiRNAhairpin;
		this.novelMiRNAmature = novelMiRNAmature;
		this.novelMiRNAdeepMrdFile = novelMiRNAdeepMrdFile;
	}
	public void getMirCount() {
		FastQ fastQ = bedSeqInput.getFastQ();
		
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa);
		MiRNAmapPipline miRNAmapPipline = new MiRNAmapPipline();
		
		miRNAmapPipline.setExePath(softWareInfo.getExePath());
		miRNAmapPipline.setMiRNApreSeq(novelMiRNAhairpin);
		miRNAmapPipline.setOutPath(outPath, outPath +"novelMiRNAmapping", outPath + "novelMiRNAbed");
		
		miRNAmapPipline.setSample(outPrefix, fastQ.getReadFileName());
		miRNAmapPipline.mappingMiRNA();
		String bedSeqMiRNAnovel = miRNAmapPipline.getOutMiRNAbed();
		
		MiRNACount miRNACount = new MiRNACount();
		miRNACount.setBedSeqMiRNA(bedSeqMiRNAnovel);
		miRNACount.setMiRNAfile(novelMiRNAhairpin, novelMiRNAmature);
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_MIRDEEP, 0, novelMiRNAdeepMrdFile);
		miRNACount.writeResultToOut(outPath + outPrefix);
	}
}
