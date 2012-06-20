package com.novelbio.analysis.seq.mirna;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �µ�miRNA��Ԥ�⣬����mirDeep���㷨
 * ע��bowtie������ϵͳ�����¡�����ͨ���޸�mapper.pl�ļ�������bowtie���ļ���·��
 * @author zong0jie
 */
public class NovelMiRNADeep extends NovelMiRNApredict{
	/** Ĭ����bowtie1�汾 */
	MapBowtie mapBowtie = new MapBowtie(MapBowtie.VERSION_BOWTIE1);
	/** miRNA���18bp */
	int miRNAminLen = 18;
	/** mirDeep���ļ��� */
	String mirDeepPath = "";
	/** �����fasta��ʽ����bed�ļ�ת����� */
	String fastaIn = "";
	/** ����ı�����mirRNA���� */
	String matureMiRNA = "";
	/** ����Ľ�������miRNA���У���÷ֳɶ���ֲ��߳�ȵ� */
	String matureRelateMiRNA = "";
	/** ������miRNAǰ�� */
	String precursorsMiRNA = "";
	String species = "";
	
	/** �趨���ȶԵĶ�����fasta�ļ����֣���������趨��������ᶨ����Ĭ��Ϊ����bed�ļ�+_Potential_DenoveMirna.fasta;
	 * �Ƽ����趨
	 * @param fastaOut
	 * */
	public void setFastaOut(String fastaIn) {
		this.fastaIn = fastaIn;
	}
	/** �趨���� */
	public void setSpecies(String species) {
		this.species = species;
	}
	/**
	 * �趨����
	 * @param matureMiRNA ����ı�����miRNA
	 * @param matureRelateMiRNA ����Ľ�������miRNA
	 * @param precursorsMiRNA ������miRNAǰ��
	 */
	public void setMiRNASeq(String matureMiRNA, String matureRelateMiRNA, String precursorsMiRNA) {
		this.matureMiRNA = matureMiRNA;
		this.matureRelateMiRNA = matureMiRNA;
		this.precursorsMiRNA = precursorsMiRNA;
	}
	
	private String getSpecies() {
		if (species == null || species.equals("")) {
			return "none ";
		}
		return "-t " + species + " ";
	}
	private String getMatureMiRNA() {
		if (matureMiRNA ==  null || matureMiRNA.equals("")) {
			return "none ";
		}
		return matureMiRNA + " ";
	}
	private String getMatureRelateMiRNA() {
		if (matureRelateMiRNA ==  null || matureRelateMiRNA.equals("")) {
			return "none ";
		}
		return matureRelateMiRNA + " ";
	}
	private String getPrecursorsMiRNA() {
		if (precursorsMiRNA ==  null || precursorsMiRNA.equals("")) {
			return "none ";
		}
		return precursorsMiRNA + " ";
	}
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePath, String chrFile) {
		if (exePath != null && !exePath.trim().equals("")) {
			this.mirDeepPath = FileOperate.addSep(exePath);
		}
		super.chromfaSeq = chrFile;
		mapBowtie.setExePath("", chrFile);
	}
	private String getChromFaSeq() {
		return chromfaSeq + " ";
	}
	/** �����reads�ļ� */
	private String getFastaMappingFile() {
		if (fastaIn == null || fastaIn.trim().equals("")) {
			fastaIn = FileOperate.changeFileSuffix(bedSeq.getFileName(), "_Potential_DenoveMirna", "fasta");
		}
		convertNoCDSbed2Fasta(fastaIn);
		return fastaIn + " ";
	}
	/**
	 * ���ȶԻ�õ�bed�ļ�ת��Ϊfasta�ļ�
	 * @param fastaOut
	 */
	private void convertNoCDSbed2Fasta(String fastaOut) {
		String out = FileOperate.changeFileSuffix(bedSeq.getFileName(), "_Potential_DenoveMirna", null);
		BedSeq bedSeq = getBedReadsNotOnCDS(out);
		TxtReadandWrite txtOut = new TxtReadandWrite(fastaOut, true);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			txtOut.writefileln(bedRecord.getSeqFasta().toStringNRfasta());
		}
		txtOut.close();
	}
	/** �����������ѹ����reads��Ϣ */
	private String getCollapseReadsFa() {
		return FileOperate.changeFilePrefix(fastaIn, "_collapsed", "fasta") + " ";
	}
	/** �����������ѹ����reads��Ϣ */
	private String getMappingArf() {
		return FileOperate.changeFilePrefix(fastaIn, "_collapsed_mapping", "arf") + " ";
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

	/** �ȶ����� */
	private void mapping() {
		mapBowtie.IndexMakeBowtie();
		String cmdMapping = mirDeepPath + "mapper.pl " + getFastaMappingFile() +"-c -j " + getReadsMinLen();
		cmdMapping = cmdMapping + "-m -p " + getChromFaSeq() + "-s " + getCollapseReadsFa() + "-t " + getMappingArf() + "-v";
	}
	/**
	 * Ԥ����miRNA
	 */
	private void predictNovelMiRNA() {
		String cmdPredict = mirDeepPath + "miRDeep2.pl " + getCollapseReadsFa() + getChromFaSeq() + getMappingArf() 
				+ getMatureMiRNA() + getMatureRelateMiRNA() + " " + getPrecursorsMiRNA() + getSpecies() + " 2> report.log";
		//TODO
	}
	
}
