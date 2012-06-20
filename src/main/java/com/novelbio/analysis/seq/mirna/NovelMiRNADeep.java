package com.novelbio.analysis.seq.mirna;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 新的miRNA的预测，基于mirDeep的算法
 * 注意bowtie必须在系统变量下。可以通过修改mapper.pl文件来设置bowtie的文件夹路径
 * @author zong0jie
 */
public class NovelMiRNADeep extends NovelMiRNApredict{
	/** 默认用bowtie1版本 */
	MapBowtie mapBowtie = new MapBowtie(MapBowtie.VERSION_BOWTIE1);
	/** miRNA最短18bp */
	int miRNAminLen = 18;
	/** mirDeep的文件夹 */
	String mirDeepPath = "";
	/** 输入的fasta格式，从bed文件转变而来 */
	String fastaIn = "";
	/** 成熟的本物中mirRNA序列 */
	String matureMiRNA = "";
	/** 成熟的近似物种miRNA序列，最好分成动物植物，线虫等等 */
	String matureRelateMiRNA = "";
	/** 本物种miRNA前体 */
	String precursorsMiRNA = "";
	String species = "";
	
	/** 设定待比对的短序列fasta文件名字，可以随便设定。如果不舍定，则默认为输入bed文件+_Potential_DenoveMirna.fasta;
	 * 推荐不设定
	 * @param fastaOut
	 * */
	public void setFastaOut(String fastaIn) {
		this.fastaIn = fastaIn;
	}
	/** 设定物种 */
	public void setSpecies(String species) {
		this.species = species;
	}
	/**
	 * 设定序列
	 * @param matureMiRNA 成熟的本物中miRNA
	 * @param matureRelateMiRNA 成熟的近似物种miRNA
	 * @param precursorsMiRNA 本物中miRNA前体
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
	 * 设定tophat所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
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
	/** 输入的reads文件 */
	private String getFastaMappingFile() {
		if (fastaIn == null || fastaIn.trim().equals("")) {
			fastaIn = FileOperate.changeFileSuffix(bedSeq.getFileName(), "_Potential_DenoveMirna", "fasta");
		}
		convertNoCDSbed2Fasta(fastaIn);
		return fastaIn + " ";
	}
	/**
	 * 将比对获得的bed文件转化为fasta文件
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
	/** 好像是输出的压缩的reads信息 */
	private String getCollapseReadsFa() {
		return FileOperate.changeFilePrefix(fastaIn, "_collapsed", "fasta") + " ";
	}
	/** 好像是输出的压缩的reads信息 */
	private String getMappingArf() {
		return FileOperate.changeFilePrefix(fastaIn, "_collapsed_mapping", "arf") + " ";
	}

	private String getReadsMinLen() {
		return "-l " + miRNAminLen + " ";
	}
	/**
	 * 设定miRNA的最短长度
	 * @param miRNAminLen 最短18bp
	 */
	public void setMiRNAminLen(int miRNAminLen) {
		this.miRNAminLen = miRNAminLen;
	}

	/** 比对序列 */
	private void mapping() {
		mapBowtie.IndexMakeBowtie();
		String cmdMapping = mirDeepPath + "mapper.pl " + getFastaMappingFile() +"-c -j " + getReadsMinLen();
		cmdMapping = cmdMapping + "-m -p " + getChromFaSeq() + "-s " + getCollapseReadsFa() + "-t " + getMappingArf() + "-v";
	}
	/**
	 * 预测新miRNA
	 */
	private void predictNovelMiRNA() {
		String cmdPredict = mirDeepPath + "miRDeep2.pl " + getCollapseReadsFa() + getChromFaSeq() + getMappingArf() 
				+ getMatureMiRNA() + getMatureRelateMiRNA() + " " + getPrecursorsMiRNA() + getSpecies() + " 2> report.log";
		//TODO
	}
	
}
