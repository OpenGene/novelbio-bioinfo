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
 * 新的miRNA的预测
 * @author zong0jie
 *
 */
public class NovelMiRNA extends GffChrAbs{
	public static void main(String[] args) {
		NovelMiRNA.getRNAfoldInfo("/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/CR_mireap-xxx.aln",
				"/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/CR_mireap-xxx.gff",
				"/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/CR_mireap.aln");
	}
	public static void main2(String[] args) {
		String bedFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/TG_Genomic.bed";
		String outMapFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/TG_MapFile.txt";
		String outSeqFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/TG_SeqFile.txt";
		NovelMiRNA novelMiRNA = new NovelMiRNA(NovelBioConst.GENOME_GFF_TYPE_UCSC,
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		novelMiRNA.setBedSeq(bedFile);
		novelMiRNA.getNovelMiRNASeq( outMapFile, outSeqFile);
	}
	/**
	 * 给定mireap的aln文件和Gff文件，将gff里面记载的，aln里面的rnafold格式的文本提取出来
	 * @param mireapAln
	 */
	public static void getRNAfoldInfo(String mireapAln, String mireapGff, String out) {
		TxtReadandWrite txtReadGff = new TxtReadandWrite(mireapGff, false);
		HashSet<String> hashID = new HashSet<String>();
		for (String string : txtReadGff.readlines()) {
//			chr10	mireap	precursor	99546111	99546203	.	-	.	ID=xxx-m0001;Count=3;mfe=-29.50
			String mirID = string.split("\t")[8].split(";")[0].split("=")[1];
			hashID.add(mirID);
		}
		
		TxtReadandWrite txtReadMirAln = new TxtReadandWrite(mireapAln, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
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
	
	
	public NovelMiRNA(String gffType, String gffFile, String chrFile) {
		super(gffType, gffFile, chrFile, null, 10);
		// TODO Auto-generated constructor stub
	}

	BedSeq bedSeq = null;
	public void setBedSeq(String bedFile) {
		bedSeq = new BedSeq(bedFile);
	}
	/**
	 * 将没有mapping至外显子或者mapping至内含子的序列整理成mireap识别的格式
	 * @param mapFile 类似bed文件，t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta格式，如下：<br>
	 * >t0000035 3234<br>
GAATGGATAAGGATTAGCGATGATACA<br>
	
	 */
	public void getNovelMiRNASeq(String mapFile, String seqFile) {
		String out = FileOperate.changeFileSuffix(bedSeq.getFileName(), "_Potential_DenoveMirna", null);
		BedSeq bedSeq = getBedReadsNotOnCDS(out);
		bedSeq = bedSeq.sortBedFile();
//		bedSeq = bedSeq.combBedFile();
		writeMireapFormat(bedSeq, mapFile, seqFile);
	}
	/**
	 * 将文件整理成mireap识别的格式
	 * @param bedSeq 输入的bedseq，必须是排过序的，必须里面包含有序列
	 * @param mapFile 类似bed文件，t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta格式，如下：<br>
	 * >t0000035 3234<br>
GAATGGATAAGGATTAGCGATGATACA<br>
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
				seqFasta.setSeqName(getID(i) + " " + bedRecordLast.getReadsNum());
				txtOutSeq.writefileln(seqFasta.toStringNRfasta());
				bedRecordLast = bedRecord;
			}
			i++;
		}
		txtOutMapInfo.writefileln(getID(i) + "\t" + bedRecordLast.getRefID() + "\t" + bedRecordLast.getStart() + "\t" + bedRecordLast.getEnd() + "\t" + bedRecordLast.getStrand());
		SeqFasta seqFasta = bedRecordLast.getSeqFasta();
		seqFasta.setSeqName(getID(i) + " " + bedRecordLast.getReadsNum());
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
//	/**
//	 * 开始用mireap来代替本方法
//	 * 给定合并的bed文件，获得序列
//	 * @param bedSeq
//	 * @return
//	 */
//	@Deprecated
//	private void writeBedSeq(BedSeq bedSeq, int readsNum, String outTxt) {
//		TxtReadandWrite txtOut = new TxtReadandWrite(outTxt, true);
//		for (BedRecord bedRecord : bedSeq.readlines()) {
//			if (bedRecord.getReadsNum() < readsNum) {
//				continue;
//			}
//			bedRecord.extendCenter(100);
//			getSeqHash().getSeq(bedRecord);
//			//设定名字
//			bedRecord.getSeqFasta().setSeqName(bedRecord.getName() + "_" + bedRecord.getReadsNum());
//			txtOut.writefileln(bedRecord.getSeqFasta().toStringNRfasta());
//		}
//	}
	
	
	/**
	 * 获得reads不在基因上的序列
	 */
	private BedSeq getBedReadsNotOnCDS(String outBed) {
		BedSeq bedResult = new BedSeq(outBed, true);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			GffCodGene gffCod = getGffHashGene().searchLocation(bedRecord.getRefID(), bedRecord.getMidLoc());
			if (readsNotOnCDS(gffCod, bedRecord.isCis5to3()))
				bedResult.writeBedRecord(bedRecord);
		}
		bedResult.closeWrite();
		return bedResult;
	}
	
	private boolean readsNotOnCDS(GffCodGene gffCodGene, boolean bedCis) {
		if (gffCodGene == null) {
			return true;
		}
		if (!gffCodGene.isInsideLoc()) {
			return true;
		}
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		int locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		if (locInfo == GffGeneIsoInfo.COD_LOC_INTRON 
				|| locInfo == GffGeneIsoInfo.COD_LOC_OUT
				|| bedCis != gffDetailGene.getLongestSplit().isCis5to3()
				) {
			return true;
		}
		return false;
	}
}
