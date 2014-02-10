package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.bed.BedRecord;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 新的miRNA的预测，基于miReap的算法
 * @author zong0jie
 */
public class NovelMiRNAReap extends NovelMiRNApredict {
	/** 读取mireap的gff和aln文件，将其装入listmirna，方便后面算表达 */
	ListMiRNAdat listMiRNALocation = new ListMiRNAdat();
	/** 给mireap准备的文件 */
	String outMapFile = "";
	/** 给mireap准备的文件 */
	String outSeqFile = "";
	/** mireap的结果文件 */
	String mireapAln = "";
	/** mireap的结果文件 */
	String mireapGff = "";
	/** 待比对的序列，必须是一个完整的fasta文件 */
	String chromfaSeq = "";
	
	public void setOutPath(String outPath) {
		//TODO
	}
	/**
	 * 设定待比对的序列，必须是一个完整的fasta文件
	 * @param chromfaSeq
	 */
	public void setChromfaSeq(String chromfaSeq) {
		this.chromfaSeq = chromfaSeq;
	}
	/**
	 * 设定输入miReap程序的文件
	 * @param outSeqFile mireap所需的序列文件
	 * @param outMapFile mireap所需的坐标定位文件
	 */
	public void setNovelMiRNAMiReapInputFile(String outSeqFile, String outMapFile) {
		this.outSeqFile = outSeqFile;
		this.outMapFile = outMapFile;
	}
	/**
	 * 将输入的bed文件排序，合并重复，然后mapping至genome上，获得所有在反向外显子和内含子的序列，
	 * 然后将序列整理成mireap能识别的格式
	 */
	public void runBedFile() {
		getNovelMiRNASeq(outMapFile, outSeqFile);
	}
	/**
	 * 将没有mapping至外显子或者mapping至内含子的序列整理成mireap识别的格式
	 * @param mapFile 类似bed文件，t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta格式，如下：<br>
	 * >t0000035 3234<br>
	GAATGGATAAGGATTAGCGATGATACA<br>
	 */
	private void getNovelMiRNASeq(String mapFile, String seqFile) {
		String out = FileOperate.changeFileSuffix(lsAlignSeqFile.iterator().next().getFileName(), "_Potential_DenoveMirna", "fasta");
		BedSeq bedSeq = getReadsNotOnCDS(out);
		bedSeq = bedSeq.sort();
		writeMireapFormat(bedSeq, mapFile, seqFile);
	}
	/**
	 * 将文件整理成mireap识别的格式
	 * @param bedSeq 输入的bedseq，必须是排过序的，必须里面包含有序列
	 * @param mapFile 类似bed文件，t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta格式，如下：<br>
	 * >t0000035 3234<br>
	 * GAATGGATAAGGATTAGCGATGATACA<br>
	 */
	private void writeMireapFormat(BedSeq bedSeq, String mapFile, String seqFile) {
		int readsName_TNum = 1;//名字，写成t00001这种类型
		TxtReadandWrite txtOutMapInfo = new TxtReadandWrite(mapFile, true);
		TxtReadandWrite txtOutSeq = new TxtReadandWrite(seqFile, true);
		BedRecord bedRecordLast = bedSeq.readFirstLine();
		for (BedRecord bedRecord : bedSeq.readLines(2)) {
			if (bedRecordLast.equalsLoc(bedRecord)) {
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
			}
			else {
				writeMiReapMapInfoAndSeq(txtOutMapInfo, txtOutSeq, bedRecordLast, readsName_TNum);
				bedRecordLast = bedRecord;
				readsName_TNum++;
			}
		}
		writeMiReapMapInfoAndSeq(txtOutMapInfo, txtOutSeq, bedRecordLast, readsName_TNum);
		
		txtOutMapInfo.close();
		txtOutSeq.close();
	}
	/**
	 * 将bed文件写入txt文本中
	 * @param txtOutMapInfo
	 * @param txtOutSeq
	 * @param bedRecord
	 * @param readsName_TNum 类似t0000035这种格式的num
	 */
	private void writeMiReapMapInfoAndSeq(TxtReadandWrite txtOutMapInfo, TxtReadandWrite txtOutSeq, BedRecord bedRecord, int readsName_TNum) {
		txtOutMapInfo.writefileln(getID(readsName_TNum) + "\t" + bedRecord.getRefID() + "\t" + bedRecord.getStartAbs() + "\t" + bedRecord.getEndAbs() + "\t" + bedRecord.getStrand());
		SeqFasta seqFasta = bedRecord.getSeqFasta();
		seqFasta.setName(getID(readsName_TNum) + " " + bedRecord.getReadsNum());
		txtOutSeq.writefileln(seqFasta.toStringNRfasta());
	}
	/**
	 * 给定一个int，返回ID
	 * @param i
	 * @return
	 */
	private String getID(int i) {
		int max = 100000000 + i;
		String result = max + "";
		return "t"+result.substring(1);
	}
	//////////////////// miReap 预测完之后的处理 ////////////////////////////////////////////////////////////////////////////////////
	/**
	 * miReap 预测完之后的处理
	 * 将预测到的新miRNA写入一个文本
	 * @param alnFile miReap的aln结果
	 * @param outFilePre 输出的前体序列
	 * @param outFileMature 输出的成熟体序列
	 */
	public void writeNovelMiRNASeq(String outFilePre, String outFileMature) {
		ArrayList<SeqFasta> lsSeqFastas = readReapResultPre();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFilePre, true);
		for (SeqFasta seqFasta : lsSeqFastas) {
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
		ArrayList<SeqFasta> lsSeqFastasMature = readReapResultMature();
		TxtReadandWrite txtWriteMature = new TxtReadandWrite(outFileMature, true);
		for (SeqFasta seqFasta : lsSeqFastasMature) {
			txtWriteMature.writefileln(seqFasta.toStringNRfasta());
		}
		txtWriteMature.close();
		txtWrite.close();
	}
	/**
	 * 给定miReap文件，提取其中的序列
	 * @param alnFile mirReap产生的结果序列文件
	 * @param outSeq 提取出来的序列，方便后续做差异计算
	 * @return 返回序列文件
	 */
	private ArrayList<SeqFasta> readReapResultPre() {
		ArrayList<SeqFasta> lsSeqFastas = new ArrayList<SeqFasta>();
		TxtReadandWrite txtReadAln = new TxtReadandWrite(mireapAln, false);
		int countStart = 1;
		SeqFasta seqFasta = new SeqFasta();
		for (String string : txtReadAln.readlines()) {
			if (countStart == 2) {
				seqFasta = new SeqFasta();
				seqFasta.setName(string.split(" ")[0]);
			}
			else if (countStart == 3) {
				seqFasta.setSeq(string.split(" ")[0]);
				lsSeqFastas.add(seqFasta);
			}
			else if (string.equals("//")) {
				countStart = 0;
			}
			countStart ++;
		}
		txtReadAln.close();
		return lsSeqFastas;
	}
	/**
	 * 给定miReap文件，提取其中的序列
	 * @param alnFile mirReap产生的结果序列文件
	 * @param outSeq 提取出来的序列，方便后续做差异计算
	 * @return 返回序列文件
	 */
	private ArrayList<SeqFasta> readReapResultMature() {
		ArrayList<SeqFasta> lsSeqFastas = new ArrayList<SeqFasta>();
		TxtReadandWrite txtReadAln = new TxtReadandWrite(mireapAln, false);
		int countStart = 1;
		SeqFasta seqFasta = new SeqFasta();
		for (String string : txtReadAln.readlines()) {
			if (countStart >= 5 && string.contains("**")) {
				seqFasta = new SeqFasta();
				seqFasta.setName(string.split(" ")[1]);
				seqFasta.setSeq(string.split(" ")[0].replace("*", ""));
				lsSeqFastas.add(seqFasta);
			}
			else if (string.equals("//")) {
				countStart = 0;
			}
			countStart ++;
		}
		txtReadAln.close();
		return lsSeqFastas;
	}
	/**
	 * miReap 预测完之后的处理
	 * 将mireap的gff里面记载的，aln里面的rnafold格式的文本提取出来
	 * @param mireapAln
	 * @param mireapGff
	 * @param out 输出文件，用于画图的东西
	 */
	public void getRNAfoldInfo(String outPlot) {
		TxtReadandWrite txtReadGff = new TxtReadandWrite(mireapGff, false);
		TxtReadandWrite txtReadMirAln = new TxtReadandWrite(mireapAln, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outPlot, true);
		
		HashSet<String> hashID = new HashSet<String>();
		for (String string : txtReadGff.readlines()) {
//			chr10	mireap	precursor	99546111	99546203	.	-	.	ID=xxx-m0001;Count=3;mfe=-29.50
			String mirID = string.split("\t")[8].split(";")[0].split("=")[1];
			hashID.add(mirID);
		}

		int i = 1; boolean flagNewID = true; boolean flagWriteIn = true;
		String tmpOut = "";
		for (String string : txtReadMirAln.readlines()) {
			if (string.startsWith("//")) {
				flagNewID = true;
				i = 1;
				continue;
			}
			if (flagNewID) {
				if (i == 2) {
					tmpOut = ">" + string.split(" ")[0];
					if (hashID.contains(string.split(" ")[0])) 
						flagWriteIn = true;
					else
						flagNewID = false;
				}
				else if (i == 3) {
					tmpOut = tmpOut + TxtReadandWrite.ENTER_LINUX + string.split(" ")[0];
				}
				else if (i == 4) {
					tmpOut = tmpOut + TxtReadandWrite.ENTER_LINUX + string;
					if (flagWriteIn) {
						txtOut.writefileln(tmpOut);
					}
				}
			}
			i ++;
		}
		txtReadMirAln.close();
		txtOut.close();
	}
}
