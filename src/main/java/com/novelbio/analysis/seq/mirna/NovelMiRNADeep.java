package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

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
public class NovelMiRNADeep extends NovelMiRNApredict {
	Logger logger = Logger.getLogger(NovelMiRNADeep.class);
	
	MapBowtie mapBowtie = new MapBowtie(SoftWare.bowtie);
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
	/** �Ѿ��ӹ�/�� */
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
	/** ���������reads�ļ�
	 * �Ὣ�����bed�ļ��ȶԻ����飬���û��mapping������exon�����У�Ȼ��д���ı���ת��Ϊfastq�ļ�
	 *  */
	private String creatFastaMappingFile() {
		if (fastaInput == null || fastaInput.trim().equals("")) {
			fastaInput = FileOperate.changeFileSuffix(bedSeqInput.getFileName(), "_Potential_DenoveMirna", "fasta");
			fastaInput = outPath + FileOperate.getFileName(fastaInput);
		}
		if (!FileOperate.isFileExist(fastaInput)) {
			convertNoCDSbed2Fasta(fastaInput);
		}
		return fastaInput + " ";
	}
	/**
	 * �������bed�ļ��ȶԻ����飬���û��mapping������exon�����У�Ȼ��д���ı���ת��Ϊfastq�ļ�
	 * Ȼ��ת��Ϊfastq�ļ��Ա���к�������
	 * @param fastaOut
	 */
	private void convertNoCDSbed2Fasta(String fastaOut) {
		String out = FileOperate.changeFileSuffix(bedSeqInput.getFileName(), "_Potential_DenoveMirna", null);
		out = outPath + FileOperate.getFileName(out);
		BedSeq bedSeq = getBedReadsNotOnCDS(out);
		TxtReadandWrite txtOut = new TxtReadandWrite(fastaOut, true);
		for (BedRecord bedRecord : bedSeq.readLines()) {
			txtOut.writefileln(bedRecord.getSeqFasta().toStringNRfasta());
		}
		txtOut.close();
	}
	/** �����������ѹ����reads��Ϣ */
	private String getCollapseReadsFa() {
		String fileName = FileOperate.changeFileSuffix(fastaInput, "_collapsed", "fasta");
		String resultName = outPath + FileOperate.getFileName(fileName);
		return resultName + " ";
	}
	/** �����������ѹ����reads��Ϣ */
	private String getMappingArf() {
		String fileName = FileOperate.changeFileSuffix(fastaInput, "_collapsed_mapping", "arf");
		String resultName = outPath + FileOperate.getFileName(fileName);
		return resultName + " ";
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
		moveAndCopeFile();
	}
	private void mapping() {
		mapBowtie.IndexMakeBowtie();
		String cmdMapping = mirDeepPath + "mapper.pl " + creatFastaMappingFile() +"-c -j " + getReadsMinLen();
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

	/**
	 * ������ļ��ƶ���ָ��λ��
	 * ͬʱ�������ļ�Ϊָ����ʽ
	 */
	private void moveAndCopeFile() {
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
		HashSet<String> setMirPredictName = getSetMirPredictName(outFinal + "result.csv");
		extractHairpinSeqMatureSeq(setMirPredictName, novelMiRNAdeepMrdFile, novelMiRNAmature, novelMiRNAhairpin);
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
	/**
	 * ��mirDeep�Ľ���ļ��л����miRNA������
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
	
	/**
	 * @param setMirPredictName ��miRNA������
	 * @param run_output_mrd ����ȡ���ļ�
	 * @param outMatureSeq ���
	 * @param outPreSeq ���
	 */
	private void extractHairpinSeqMatureSeq(Set<String> setMirPredictName, String run_output_mrd, String outMatureSeq, String outPreSeq) {
		FileOperate.createFolders(FileOperate.getParentPathName(outMatureSeq));
		FileOperate.createFolders(FileOperate.getParentPathName(outPreSeq));
		
		TxtReadandWrite txtReadMrd = new TxtReadandWrite(run_output_mrd, false);
		TxtReadandWrite txtWriteMature = new TxtReadandWrite(outMatureSeq, true);
		TxtReadandWrite txtWritePre = new TxtReadandWrite(outPreSeq, true);
		
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
	
	public String getNovelMiRNAhairpin() {
		return novelMiRNAhairpin;
	}
	public String getNovelMiRNAmature() {
		return novelMiRNAmature;
	}
	/** mrd�ļ���mirDeep����miRNA������Ϣ */
	public String getNovelMiRNAdeepMrdFile() {
		return novelMiRNAdeepMrdFile;
	}
	/**
	 * ������
	 * @param novelMiRNAhairpin
	 * @param novelMiRNAmature
	 * @param novelMiRNAdeepMrdFile
	 */
	@Deprecated
	public void setCalNovelMiRNACountNovelMiRNASeq(String novelMiRNAhairpin, String novelMiRNAmature, String novelMiRNAdeepMrdFile) {
		this.novelMiRNAhairpin = novelMiRNAhairpin;
		this.novelMiRNAmature = novelMiRNAmature;
		this.novelMiRNAdeepMrdFile = novelMiRNAdeepMrdFile;
	}

}
