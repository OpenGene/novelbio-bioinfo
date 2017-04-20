package com.novelbio.database.model.species;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 提取小RNA的一系列序列 */
public class ExtractSmallRNASeq {
	private static final Logger logger = LoggerFactory.getLogger(ExtractSmallRNASeq.class);

	String RNAdataFile = "";
	/** 用于在mir.dat文件中查找miRNA的物种名 */
	String miRNAdataSpeciesName = "";
	
	/** 提取ncRNA的正则表达式 */
	String regxNCrna  = "NR_\\d+|XR_\\d+";
	/** refseq的序列文件，要求是NCBI下载的文件 */
	String refseqFile = "";


	/** Rfam的正则 */
	int taxIDfram = 0;
	/** Rfam的名字regx */
	String regxRfamWrite = "(?<=\\>)\\S+";
	/** rfam的文件 */
	String rfamFile = "";
	
	/** 提取的rfam的文件 */
	String outRfamFile;
	/** 提取到的目标文件夹和前缀 */
	String outPathPrefix = "";
	String outHairpinRNA;
	String outMatureRNA;
	/** 从RefSeq中提取的ncRNA序列 */
	String outNcRNA;
	
	/**  需要提取的miRNA的名字，都为小写 */
	Set<String> setMiRNAname;
	
	public void setLsMiRNAname(Collection<String> lsMiRNAname) {
		if (lsMiRNAname == null) {
			return;
		}
		setMiRNAname = new LinkedHashSet<String>();
		for (String string : lsMiRNAname) {
			setMiRNAname.add(string.toLowerCase());
		}
	}
	/**
	 * 设定输出文件夹和前缀，这个设定了就不用设定别的了
	 * @param outPathPrefix
	 */
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	
	public void setOutNcRNA(String outNcRNA) {
		this.outNcRNA = outNcRNA;
	}
	public void setOutHairpinRNA(String outHairpinRNA) {
		this.outHairpinRNA = outHairpinRNA;
	}
	public void setOutMatureRNA(String outMatureRNA) {
		this.outMatureRNA = outMatureRNA;
	}
	public void setOutRfamFile(String outRfamFile) {
		this.outRfamFile = outRfamFile;
	}
	/**
	 * @param rnaDataFile
	 * @param speciesName 物种的拉丁名 两个单词
	 */
	public void setMiRNAdata(String rnaDataFile, String speciesName) {
		this.RNAdataFile = rnaDataFile;
		this.miRNAdataSpeciesName = speciesName;
	}
	/**
	 * 待提取的NCBI上下载的refseq文件
	 * @param refseqFile
	 */
	public void setRefseqFile(String refseqFile) {
		this.refseqFile = refseqFile;
	}
	/**
	 * 待提取某物中的rfam文件
	 * @param rfamFile
	 * @param regx rfam的物种名
	 */
	public void setRfamFile(String rfamFile, int taxIDrfam) {
		this.rfamFile = rfamFile;
		this.taxIDfram = taxIDrfam;//TODO 看这里是物种的什么名字
	}
	
	/** 提取序列 */
	public void getSeq() {
		if (FileOperate.isFileExistAndNotDir(refseqFile)) {
			if (outNcRNA == null)
				outNcRNA = outPathPrefix + "_ncRNA.fa";
			try {
				extractNCRNA(refseqFile, outNcRNA, regxNCrna);
			} catch (Exception e) {
				logger.error("abstract ncrna error " + refseqFile);
				throw new ExceptionNbcSpeciesFileAbstract("abstract ncrna error " + refseqFile, e);
			}
		}
		
		if (FileOperate.isFileExistAndNotDir(RNAdataFile)) {
			if (outHairpinRNA == null)
				outHairpinRNA = outPathPrefix + "_hairpin.fa";
			if (outMatureRNA == null)
				outMatureRNA = outPathPrefix + "_mature.fa";
			
			ListMiRNAdat listMiRNALocation = new ListMiRNAdat();
			try {
				listMiRNALocation.extractMiRNASeqFromRNAdata(setMiRNAname, miRNAdataSpeciesName, RNAdataFile, outHairpinRNA, outMatureRNA);
			} catch (Exception e) {
				logger.error("abstract miNRA error " + RNAdataFile);
				throw new ExceptionNbcSpeciesFileAbstract("abstract miNRA error " + RNAdataFile, e);
			}
			listMiRNALocation = null;
		}
		
		if (FileOperate.isFileExistAndNotDir(rfamFile)) {
			if (outRfamFile == null)
				outRfamFile = outPathPrefix + "_rfam.fa";
				
			try {
				extractRfam(rfamFile, outRfamFile, taxIDfram);
			} catch (Exception e) {
				logger.error("abstract rfam error " + rfamFile);
				throw new ExceptionNbcSpeciesFileAbstract("abstract miNRA error " + rfamFile, e);
			}
			
		}
	}
	/**
	 * 从NCBI的refseq.fa文件中提取NCRNA
	 * @param refseqFile
	 * @param outNCRNA
	 * @param regx 类似 "NR_\\d+|XR_\\d+";
	 */
	private void extractNCRNA(String refseqFile, String outNCRNA, String regx) {
		if (!FileOperate.isFileExistAndBigThanSize(refseqFile, 0)) {
			return;
		}
		SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false);
		seqFastaHash.writeToFile( regx ,outNCRNA );
		seqFastaHash.close();
	}
	
	private void extractRfam(String rfamFile, String outRfam, int taxIDquery) {
		if (!FileOperate.isFileExistAndBigThanSize(rfamFile, 0)) {
			return;
		}
		if (taxIDquery <= 0) {
			extractRfam(rfamFile, outRfam);
		} else {
			extractRfamTaxID(rfamFile, outRfam, taxIDquery);
		}
	}
	/**
	 * 从rfam.txt文件中提取指定物种的ncRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractRfamTaxID(String rfamFile, String outRfam, int taxIDquery) {
		String outRfamTmp = FileOperate.changeFileSuffix(outRfam, "_tmp", null);
		TxtReadandWrite txtOut = new TxtReadandWrite(outRfamTmp, true);
		SeqFastaReader seqFastaReader = new SeqFastaReader(rfamFile);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			 int taxID = 0;
			 try {
				 taxID = Integer.parseInt(seqFasta.getSeqName().trim().split(" +")[1].split(":")[0]);
			 } catch (Exception e) {
				 logger.error("本序列中找不到taxID：" + seqFasta.getSeqName());
			 }
			 
			 if (taxID == taxIDquery) {
				 SeqFasta seqFastaNew = seqFasta.clone();
				 String name = seqFasta.getSeqName().trim().split(" +")[0];
				 name = name.replace(";", "//");
				 seqFastaNew.setName(name);
				 txtOut.writefileln(seqFastaNew.toStringNRfasta());
			 }
		}
		 txtOut.close();
		 seqFastaReader.close();
		 FileOperate.moveFile(true, outRfamTmp, outRfam);
	}
	
	/**
	 * 修正rfam.txt文件中提取指定物种的ncRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractRfam(String rfamFile, String outRfam) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rfamFile, false);
		String outRfamTmp = FileOperate.changeFileSuffix(outRfam, "_tmp", null);
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(outRfamTmp, true);

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith(">")) {
				string = string.split(" +")[0].replace(";", "//");
				if (map.containsKey(string)) {
					int tmp = map.get(string) + 1;
					map.put(string, tmp);
				} else {
					map.put(string, 0);
				}
				int tmpNum = map.get(string);
				if (tmpNum == 0) {
					txtWrite.writefileln(string);
				} else {
					txtWrite.writefileln(string + "_new" + tmpNum);
				}
			} else {
				string = string.replace("U", "T");
				txtWrite.writefileln(string);
			}
		}
		txtRead.close();
		txtWrite.close();
		
		FileOperate.moveFile(true, outRfamTmp, outRfam);
	}
	
}
