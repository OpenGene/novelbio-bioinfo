package com.novelbio.analysis.seq.mirna;

import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 新的miRNA的预测，基于miReap的算法
 * @author zong0jie
 *
 */
public class NovelMiRNAReap {
	GffChrAbs gffChrAbs = null;
	/** 读取mireap的gff和aln文件，将其装入listmirna，方便后面算表达 */
	ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
	/** 给mireap准备的文件 */
	String outMapFile = "";
	/** 给mireap准备的文件 */
	String outSeqFile = "";
	/** 输入的一个bedseq文件 */
	BedSeq bedSeq = null;
	/** mireap的结果文件 */
	String mireapAln = "";
	/** mireap的结果文件 */
	String mireapGff = "";
	/**
	 * @param gffChrAbs 设定gff即可
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
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
	 * 与setBedSeq(String bedFile) 二选一
	 * 将多个样本得到的mapping 至 genome上的bed文件合并，并作为输入mireap的文件
	 * @param outFile
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, bedSeqFile);
		setBedSeq(bedSeq.getFileName());
	}
	/**
	 * 与setBedSeq(String bedFile) 二选一
	 * 将多个样本得到的mapping 至 genome上的bed文件合并，并作为输入mireap的文件
	 * @param outFile 获得合并的bed文件名
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, ArrayList<String> lsBedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, lsBedSeqFile);
		setBedSeq(bedSeq.getFileName());
	}
	/**
	 * 与setBedSeq(String outFile, String... bedSeqFile) 二选一
	 * 样本得到的bed文件
	 * @param bedFile
	 */
	public void setBedSeq(String bedFile) {
		bedSeq = new BedSeq(bedFile);
	}
	/**
	 * 将输入的bed文件排序，合并重复，然后mapping至genome上，获得所有在反向外显子和内含子的序列，
	 * 然后将序列整理成mireap能识别的格式
	 */
	public void runBedFile() {
		bedSeq = bedSeq.sortBedFile().combBedOverlap();
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
		String out = FileOperate.changeFileSuffix(bedSeq.getFileName(), "_Potential_DenoveMirna", null);
		BedSeq bedSeq = getBedReadsNotOnCDS(out);
		bedSeq = bedSeq.sortBedFile();
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
		int i = 1;//名字，写成t00001这种类型
		TxtReadandWrite txtOutMapInfo = new TxtReadandWrite(mapFile, true);
		TxtReadandWrite txtOutSeq = new TxtReadandWrite(seqFile, true);
		BedRecord bedRecordLast = bedSeq.readFirstLine();
		for (BedRecord bedRecord : bedSeq.readlines(2)) {
			if (bedRecordLast.equalsLoc(bedRecord)) {
				bedRecordLast.setReadsNum(bedRecordLast.getReadsNum() + 1);
			}
			else {
				txtOutMapInfo.writefileln(getID(i) + "\t" + bedRecordLast.getRefID() + "\t" + bedRecordLast.getStart() + "\t" + bedRecordLast.getEnd() + "\t" + bedRecordLast.getStrand());
				SeqFasta seqFasta = bedRecordLast.getSeqFasta();
				seqFasta.setName(getID(i) + " " + bedRecordLast.getReadsNum());
				txtOutSeq.writefileln(seqFasta.toStringNRfasta());
				bedRecordLast = bedRecord;
			}
			i++;
		}
		txtOutMapInfo.writefileln(getID(i) + "\t" + bedRecordLast.getRefID() + "\t" + bedRecordLast.getStart() + "\t" + bedRecordLast.getEnd() + "\t" + bedRecordLast.getStrand());
		SeqFasta seqFasta = bedRecordLast.getSeqFasta();
		seqFasta.setName(getID(i) + " " + bedRecordLast.getReadsNum());
		txtOutSeq.writefileln(seqFasta.toStringNRfasta());
		
		txtOutMapInfo.close();
		txtOutSeq.close();
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
	/**
	 * 遍历bed文件，获得reads不在基因上的序列
	 */
	private BedSeq getBedReadsNotOnCDS(String outBed) {
		BedSeq bedResult = new BedSeq(outBed, true);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			GffCodGene gffCod = gffChrAbs.getGffHashGene().searchLocation(bedRecord.getRefID(), bedRecord.getMidLoc());
			if (readsNotOnCDS(gffCod, bedRecord.isCis5to3()))
				bedResult.writeBedRecord(bedRecord);
		}
		bedResult.closeWrite();
		return bedResult;
	}
	/**
	 * 判定输入的reads是否位于intron或gene外或反向exon上
	 * @param gffCodGene
	 * @param bedCis
	 * @return
	 */
	private boolean readsNotOnCDS(GffCodGene gffCodGene, boolean bedCis) {
		if (gffCodGene == null) {
			return true;
		}
		if (!gffCodGene.isInsideLoc()) {
			return true;
		}
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		int locInfo = 0;
		try {
			locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		} catch (Exception e) {
			locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		}
		if (locInfo == GffGeneIsoInfo.COD_LOC_INTRON 
				|| locInfo == GffGeneIsoInfo.COD_LOC_OUT
				|| bedCis != gffDetailGene.getLongestSplit().isCis5to3()
				) {
			return true;
		}
		return false;
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
		HashSet<String> hashID = new HashSet<String>();
		for (String string : txtReadGff.readlines()) {
//			chr10	mireap	precursor	99546111	99546203	.	-	.	ID=xxx-m0001;Count=3;mfe=-29.50
			String mirID = string.split("\t")[8].split(";")[0].split("=")[1];
			hashID.add(mirID);
		}
		TxtReadandWrite txtReadMirAln = new TxtReadandWrite(mireapAln, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outPlot, true);
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
