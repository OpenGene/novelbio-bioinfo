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
 * �µ�miRNA��Ԥ�⣬����miReap���㷨
 * @author zong0jie
 *
 */
public class NovelMiRNAReap {
	GffChrAbs gffChrAbs = null;
	/** ��ȡmireap��gff��aln�ļ�������װ��listmirna������������� */
	ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
	/** ��mireap׼�����ļ� */
	String outMapFile = "";
	/** ��mireap׼�����ļ� */
	String outSeqFile = "";
	/** �����һ��bedseq�ļ� */
	BedSeq bedSeq = null;
	/** mireap�Ľ���ļ� */
	String mireapAln = "";
	/** mireap�Ľ���ļ� */
	String mireapGff = "";
	/**
	 * @param gffChrAbs �趨gff����
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * �趨����miReap������ļ�
	 * @param outSeqFile mireap����������ļ�
	 * @param outMapFile mireap��������궨λ�ļ�
	 */
	public void setNovelMiRNAMiReapInputFile(String outSeqFile, String outMapFile) {
		this.outSeqFile = outSeqFile;
		this.outMapFile = outMapFile;
	}
	/**
	 * ��setBedSeq(String bedFile) ��ѡһ
	 * ����������õ���mapping �� genome�ϵ�bed�ļ��ϲ�������Ϊ����mireap���ļ�
	 * @param outFile
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, bedSeqFile);
		setBedSeq(bedSeq.getFileName());
	}
	/**
	 * ��setBedSeq(String bedFile) ��ѡһ
	 * ����������õ���mapping �� genome�ϵ�bed�ļ��ϲ�������Ϊ����mireap���ļ�
	 * @param outFile ��úϲ���bed�ļ���
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, ArrayList<String> lsBedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, lsBedSeqFile);
		setBedSeq(bedSeq.getFileName());
	}
	/**
	 * ��setBedSeq(String outFile, String... bedSeqFile) ��ѡһ
	 * �����õ���bed�ļ�
	 * @param bedFile
	 */
	public void setBedSeq(String bedFile) {
		bedSeq = new BedSeq(bedFile);
	}
	/**
	 * �������bed�ļ����򣬺ϲ��ظ���Ȼ��mapping��genome�ϣ���������ڷ��������Ӻ��ں��ӵ����У�
	 * Ȼ�����������mireap��ʶ��ĸ�ʽ
	 */
	public void runBedFile() {
		bedSeq = bedSeq.sortBedFile().combBedOverlap();
		getNovelMiRNASeq(outMapFile, outSeqFile);
	}
	/**
	 * ��û��mapping�������ӻ���mapping���ں��ӵ����������mireapʶ��ĸ�ʽ
	 * @param mapFile ����bed�ļ���t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta��ʽ�����£�<br>
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
	 * ���ļ������mireapʶ��ĸ�ʽ
	 * @param bedSeq �����bedseq���������Ź���ģ������������������
	 * @param mapFile ����bed�ļ���t0000035	nscaf1690	4798998	4799024	+
	 * @param seqFile fasta��ʽ�����£�<br>
	 * >t0000035 3234<br>
	 * GAATGGATAAGGATTAGCGATGATACA<br>
	 */
	private void writeMireapFormat(BedSeq bedSeq, String mapFile, String seqFile) {
		int i = 1;//���֣�д��t00001��������
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
	 * ����һ��int������ID
	 * @param i
	 * @return
	 */
	private String getID(int i) {
		int max = 100000000 + i;
		String result = max + "";
		return "t"+result.substring(1);
	}
	/**
	 * ����bed�ļ������reads���ڻ����ϵ�����
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
	 * �ж������reads�Ƿ�λ��intron��gene�����exon��
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
	//////////////////// miReap Ԥ����֮��Ĵ��� ////////////////////////////////////////////////////////////////////////////////////
	/**
	 * miReap Ԥ����֮��Ĵ���
	 * ��Ԥ�⵽����miRNAд��һ���ı�
	 * @param alnFile miReap��aln���
	 * @param outFilePre �����ǰ������
	 * @param outFileMature ����ĳ���������
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
	 * ����miReap�ļ�����ȡ���е�����
	 * @param alnFile mirReap�����Ľ�������ļ�
	 * @param outSeq ��ȡ���������У�����������������
	 * @return ���������ļ�
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
	 * ����miReap�ļ�����ȡ���е�����
	 * @param alnFile mirReap�����Ľ�������ļ�
	 * @param outSeq ��ȡ���������У�����������������
	 * @return ���������ļ�
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
	 * miReap Ԥ����֮��Ĵ���
	 * ��mireap��gff������صģ�aln�����rnafold��ʽ���ı���ȡ����
	 * @param mireapAln
	 * @param mireapGff
	 * @param out ����ļ������ڻ�ͼ�Ķ���
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
