package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftMapping;
import com.novelbio.database.model.species.Species;

/**
 * �µ�miRNA��Ԥ�⣬����mirDeep���㷨
 * ע��bowtie������ϵͳ�����¡�����ͨ���޸�mapper.pl�ļ�������bowtie���ļ���·��
 * @author zong0jie
 */
public class NovelMiRNADeep extends NovelMiRNApredict{
	public static void main(String[] args) {
		Species species = new Species(9606);
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftMapping.bowtie);
		String bedFile = "/home/zong0jie/Desktop/platformtest/testCR_miRNA_Filtered_Genome.bed";
		NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
		novelMiRNADeep.setBedSeqInput(bedFile);
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftMapping.bowtie));
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.predict();
	}
	
	
	MapBowtie mapBowtie = new MapBowtie(MapBowtie.VERSION_BOWTIE1);
	int miRNAminLen = 18;
	String mirDeepPath = "";
	/** �����fasta��ʽ����bed�ļ�ת����� */
	String fastaInput = "";
	/** ����ı�����mirRNA���� */
	String matureMiRNA = "";
	/** ����Ľ�������miRNA���У���÷ֳɶ���ֲ��߳�ȵ� */
	String matureRelateMiRNA = "";
	/** ������miRNAǰ�� */
	String hairpinMiRNA = "";
	String species = "";
	String chromFaIndexBowtie;
	/**
	 * ��bed�ļ�ת��Ϊfasta��ʽ 
	 * �趨���ȶԵĶ�����fasta�ļ����֣���������趨��������ᶨ����Ĭ��Ϊ����bed�ļ�+_Potential_DenoveMirna.fasta;
	 * �Ƽ����趨
	 * @param fastaOut
	 * */
	public void setBed2FastaOut(String fastaIn) {
		this.fastaInput = fastaIn;
	}
	/** �趨���� */
	public void setSpecies(String species) {
		this.species = species;
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
		if (hairpinMiRNA ==  null || hairpinMiRNA.equals("")) {
			return "none ";
		}
		return hairpinMiRNA + " ";
	}
	/**
	 * �趨bowtie���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
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
		convertNoCDSbed2Fasta(fastaInput);
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
	}
	/** �ȶ����� */
	private void mapping() {
		mapBowtie.IndexMakeBowtie();
		String cmdMapping = mirDeepPath + "mapper.pl " + getFastaMappingFile() +"-c -j " + getReadsMinLen();
		cmdMapping = cmdMapping + "-m -p " + getChromFaSeq() + "-s " + getCollapseReadsFa() + "-t " + getMappingArf() + "-v";
		CmdOperate cmdOperate = new CmdOperate(cmdMapping, "mirDeepMapping_" + species);
		cmdOperate.run();
	}
	/**
	 * Ԥ����miRNA
	 */
	private void predictNovelMiRNA() {
		String cmdPredict = mirDeepPath + "miRDeep2.pl " + getCollapseReadsFa() + getChromFaSeq() + getMappingArf() 
				+ getMatureMiRNA() + getMatureRelateMiRNA() + " " + getPrecursorsMiRNA() + getSpecies() + " 2> report.log";
		CmdOperate cmdOperate = new CmdOperate(cmdPredict, "mirDeepPredict_" + species);
		cmdOperate.run();
	}
	
}
