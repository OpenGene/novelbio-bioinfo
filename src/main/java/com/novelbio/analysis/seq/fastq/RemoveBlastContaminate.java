package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 
 * 给定fastq文件，和blast结果，删除比对到其他物种的序列
 * @author novelbio
 *
 */
public class RemoveBlastContaminate {
	private static final Logger logger = Logger.getLogger(RemoveBlastContaminate.class);
	
	String blastFile;
	String seqFaFile;
	SeqFastaHash seqFastaHash;
	
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	
	/** 随机数发生器，用于产生正态分布的插入 */
	Random random = new Random();

	FastQ fqLeft, fqRight;
	FastQ fqLeftModify, fqRightModify;
//	FastQ fqLeftFiltered, fqRightFiltered;
	boolean isTest = false;
	
	/** 读了多少行，计数器 */
	int readCounts = 0;
	
	public static void main(String[] args) {
		
//		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/下载/blastn.txt");
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/下载/blastn_modify.txt", true);
//		int i = 0;
//		for (String string : txtRead.readlines()) {
//			if (i++ > 10000) {
//				break;
//			}
//			txtWrite.writefileln(string);
//		}
//		txtRead.close();
//		txtWrite.close();
		
//		String blast = args[0];
//		String fastqL = args[1];
//		String fastqR = args[2];
//		String seqFaFile = args[3];
		
//		String blast = "/home/novelbio/bianlianle/RemoveContaminate/V26-3.common_carp.filter2.10.txt";
//		String fastqL = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_V26-3_combined_test_R1.fastq";
//		String fastqR = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_V26-3_combined_test_R2.fastq";
		
		String blast = "/home/novelbio/bianlianle/RemoveContaminate/CYR32-3.common_carp.filter2.txt";
		String fastqL = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_CYR32-3_combined_R1.fastq.gz";
		String fastqR = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_CYR32-3_combined_R2.fastq.gz";
		String seqFaFile = "/home/novelbio/bianlianle/RemoveContaminate/Puccinia_striiformis.rrna.fasta";
		
		RemoveBlastContaminate remove = new RemoveBlastContaminate();
		remove.setBlastFile(blast);
		remove.setFastqLeft(fastqL);
		remove.setFastqRight(fastqR);
		remove.setSeqFaFile(seqFaFile);
		remove.initial();
		remove.runRemove();
		
		
		
	}
	
	public void setSeqFastaHash(SeqFastaHash seqFastaHash) {
		this.seqFastaHash = seqFastaHash;
	}
	public void setFastqLeft(String fastqLeft) {
		this.fqLeft = new FastQ(fastqLeft);
		fqLeftModify = new FastQ(FileOperate.changeFileSuffix(fastqLeft, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	
	/** 单端测序不用设置这个 */
	public void setFastqRight(String fastqRight) {
		this.fqRight = new FastQ(fastqRight);
		fqRightModify = new FastQ(FileOperate.changeFileSuffix(fastqRight, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	public void setSeqFaFile(String seqFaFile) {
		this.seqFaFile = seqFaFile;
	}
	
	public void setBlastFile(String blastFile) {
		this.blastFile = blastFile;
	}
	/** 是否将结果写入fastq，主要用在测试环境，如仅测 {@link #getFqExistNameAndWriteSeqNotExistInBlast(String)}
	 * 默认为false
	 *  */
	protected void setIsWriteFastq(boolean isTest) {
		this.isTest = isTest;
	}
	public void initial() {
		if (fqRight != null) {
			itFqPE =  fqLeft.readlinesPE(fqRight).iterator();
		} else {
			itFqSE = fqLeft.readlines().iterator();
		}
	}
	
	public void runRemove() {
		TxtReadandWrite txtReadBlast = new TxtReadandWrite(blastFile);		
		seqFastaHash = new SeqFastaHash(seqFaFile);
		String fqNameLast = "";
		readCounts = 0;
		for (String blastInfo : txtReadBlast.readlines()) {
			String fqNameToDelete = blastInfo.split("\t")[0];
			if (fqNameToDelete.equals(fqNameLast)) {
				continue;
			}
			getFqExistNameAndWriteSeqNotExistInBlast(fqNameToDelete);
			fqNameLast = fqNameToDelete;
		}
		while (itFqPE.hasNext()) {
			FastQRecord[] pe = itFqPE.next();
			writeFastq(pe);
		}
		
		fqLeftModify.close();
		if (fqRightModify != null) {
			fqRightModify.close();
		}
		
		txtReadBlast.close();
	
	}
	
	protected String getFqExistNameAndWriteSeqNotExistInBlast(String fqNameToDelete) {
		String seqNameExist = null;
		FastqAddSnp fastqAddSnp = new FastqAddSnp();
		while (true) {
			if (readCounts++ % 100000 == 0) {
				logger.info(readCounts);
			}
			String fqName = null;
			FastQRecord[] fqPE = null;
			if (itFqPE != null) {
				fqPE = itFqPE.next();
				fqName = fqPE[0].getName().split(" ")[0];
			} else {
				FastQRecord fqSE = itFqSE.next();
				fqPE = new FastQRecord[]{fqSE, null};
				fqName = fqSE.getName().split(" ")[0];
			}
			
			if (fqName.equals(fqNameToDelete)) {
				String chrIdRandom = getRandomChrId();
				int[] startEnd = getRandomStartEnd(chrIdRandom);
				int seqLen1 = fqPE[0].getSeqFasta().Length();
				int seqLen2 = fqPE[1].getSeqFasta().Length();
				String[] seq1_2 = getSeqPairend(seqFastaHash, chrIdRandom, startEnd, seqLen1, seqLen2);
				
				fqPE[0].setSeq(seq1_2[0]);
				fastqAddSnp.modifyFastsq(fqPE[0]);
				
				if (itFqPE != null) {
					fastqAddSnp.modifyFastsq(fqPE[1]);
				}
				writeFastq(fqPE);
				seqNameExist = fqName;
				break;
			} else {				
				if (!isTest) {
					writeFastq(fqPE);
				}
				
			}
		}
		return seqNameExist;
	}
	
	protected void writeFastq(FastQRecord[] fqPE) {
		if (fqPE[0] != null) {
			fqLeftModify.writeFastQRecord(fqPE[0]);
		}
	
		
		if (fqPE[1] != null) {
			fqRightModify.writeFastQRecord(fqPE[1]);
		}
	}
	
	/** remove结束后才可使用 */
	public FastQ getResultFqLeft() {
		return fqLeftModify;
	}
	/** remove结束后才可使用 */
	public FastQ getResultFqRight() {
		return fqRightModify;
	}
	
	protected static String[] getSeqPairend(SeqFastaHash seqFastaHash, String chrIdRandom, int[] startEnd, int seqLen1, int seqLen2) {
		String first = seqFastaHash.getSeq(chrIdRandom, startEnd[0], startEnd[0] + seqLen1 - 1).toString();
		String second = seqFastaHash.getSeq(chrIdRandom, startEnd[1], startEnd[1] + seqLen2 - 1).reservecom().toString();
		return new String[]{first, second};
	}
	
	protected String getRandomChrId() {
		List<String> lsChrIdList = seqFastaHash.getLsSeqName();
		int chrIdRandome = (int)(Math.random()*lsChrIdList.size());
		String chrId = lsChrIdList.get(chrIdRandome);
		return chrId;
	} 
       
	protected int[] getRandomStartEnd(String chrId) {
		long seqLen = seqFastaHash.getChrLength(chrId);
//		long seqLen = seqFastaHash.getMapChrLength().get(chrId);
		long seqLenSub = seqLen - 151;
		if (seqLenSub < 0) seqLenSub = 1;
		int start = (int)(Math.random()*seqLenSub + 1);
		int end =(int) (random.nextGaussian() * 80 + 300);
		if (end < start) {
			end = start + 90;
		}
		if (end + 125 > seqLen) {
			end = (int) (seqLen - 125);
		}
		return new int[]{start, end};
	}
 
	
	protected String getRandomSeq(int readLength) {
		List<String> lsChrIdList = seqFastaHash.getLsSeqName();
		int chrIdRandome = (int)(Math.random()*lsChrIdList.size());
		String sequence = seqFastaHash.getSeq(lsChrIdList.get(chrIdRandome)).toString();
		int startLocation = (int)(Math.random()*10000);
		if (startLocation > (startLocation - readLength)) {
			startLocation = sequence.length() - readLength;
			if (startLocation < 0) {				
				startLocation = 1;
			}
		}
		String seq = sequence.substring(startLocation);
		return seq;
	}
	
	protected String getRandomSeq(String chrId, int readLength) {
		List<String> lsChrIdList = seqFastaHash.getLsSeqName();
		int chrIdRandome = (int)(Math.random()*lsChrIdList.size());
		String sequence = seqFastaHash.getSeq(lsChrIdList.get(chrIdRandome)).toString();
		int startLocation = (int)(Math.random()*10000);
		if (startLocation > (startLocation - readLength)) {
			startLocation = sequence.length() - readLength;
			if (startLocation < 0) {				
				startLocation = 1;
			}
		}
		String seq = sequence.substring(startLocation);
		return seq;
	}
}

class FastqAddSnp {
	Random random = new Random();
	
	/** 计数器,每100条序列添加一个snp */
	int count = 0;
	int nextCount = 0;
	List<Character> lsCharacters = new ArrayList<Character>();
	
	public FastqAddSnp() {
		nextCount = random.nextInt(200);
		lsCharacters.add('A'); lsCharacters.add('T'); lsCharacters.add('G'); lsCharacters.add('C'); lsCharacters.add('N');
	}
	
	public void modifyFastsq(FastQRecord fastQRecord) {
		count++;
		if (count >= nextCount) {
			addSnp(fastQRecord);
			count = 0;
			nextCount = random.nextInt(200);
		}
	}
	
	private void addSnp(FastQRecord fastQRecord) {
		int snpNum = (int)(random.nextDouble() * fastQRecord.getLength());
		String seq = fastQRecord.getSeqFasta().toString();
		char[] seqChar = seq.toCharArray();
		seqChar[snpNum] = getRandomBp();
		seq = String.valueOf(seqChar);		
		fastQRecord.setSeq(seq);
	}
	
	private char getRandomBp() {
		int chrNum = (int) (random.nextDouble() * 5);
		return lsCharacters.get(chrNum);
	}
}
