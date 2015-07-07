package com.novelbio.analysis.seq.fastq;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
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
	
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	
	FastQ fqLeft, fqRight;
	FastQ fqLeftModify, fqRightModify;
	
	boolean isTest = false;
	
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
//		String parentPath = "/home/novelbio/";
		String blast = args[0];
		String fastqL = args[1];
		String fastqR = args[2];
		
		RemoveBlastContaminate remove = new RemoveBlastContaminate();
		remove.setBlastFile(blast);
		remove.setFastqLeft(fastqL);
		remove.setFastqRight(fastqR);
		remove.initial();
		remove.runRemove();
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
		
		String fqNameLast = "";
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
		while (true) {
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
		fqLeftModify.writeFastQRecord(fqPE[0]);
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
}
