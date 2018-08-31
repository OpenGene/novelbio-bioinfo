package com.novelbio.bioinfo.mirna;

import java.util.List;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.annotation.blast.BlastNBC;
import com.novelbio.bioinfo.annotation.blast.BlastType;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.database.model.geneanno.BlastInfo;

public class MiRNAtargetPlant {	
//	String flag = DateUtil.getDateAndRandom();
	/** blast的输出文件 */
	String tmpBlastFile;
	
	/**miRNA和plant mRNA blast的相似度 */
	int indetity = 70;
	/** 最短比对长度 */
	int mirLen = 16;
	//为什么是这个参数？
	int extendmRNAUp = 17;
	int extendmRNADown = 13;
	
	String mirSeqFile;
	String mrnaSeqFile;
	
	String outPath;
	String outPathTmp;
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
		outPathTmp = FileOperate.addSep(outPath) + "tmp/";
	}
	/** miRNA和plant mRNA blast的最低相似度，默认70 */
	public void setIndetity(int indetity) {
		this.indetity = indetity;
	}
	/** 最短比对长度，默认16 */
	public void setMirLen(int mirLen) {
		this.mirLen = mirLen;
	}
	/** 输入的序列文件，务必是等长mRNA，并且不能有重复 */
	public void setSeqFile(String mirSeqFile, String mrnaSeqFile) {
		this.mrnaSeqFile = mrnaSeqFile;
	}
	public void setMirSeqFile(String mirSeqFile) {
		this.mirSeqFile = mirSeqFile;
	}
	
	private List<BlastInfo> blastMiRNA2Mrna() {
		String blastOut = outPathTmp + "miRNA_mRNA_blast";
		BlastNBC blastNBC = new BlastNBC();
		blastNBC.setBlastType(BlastType.blastn);
		blastNBC.setQueryFastaFile(mirSeqFile);
		blastNBC.setSubjectSeq(mirSeqFile);
		blastNBC.setResultSeqNum(1000);
		blastNBC.setResultType(BlastNBC.ResultType_Simple);
		blastNBC.setShortQuerySeq(true);
		blastNBC.setResultFile(blastOut);
		blastNBC.blast();
		return blastNBC.getResultInfo();
	}
	
	/**
	 * 产生别的软件所需的文件
	 */
	private void generateFile(List<BlastInfo> lsBlastInfo) {
		String fileRNAup = outPathTmp + "rnaUpInput";
		String fileRNAcofoldmiRNA = outPathTmp + "rnaCofoldmiRNA";
		String fileRNAcofoldmRNA = outPathTmp + "rnaCofoldmRNA";
		TxtReadandWrite txtRNAup = new TxtReadandWrite(fileRNAup, true);
		TxtReadandWrite txtRNAcofoldmiRNA = new TxtReadandWrite(fileRNAcofoldmiRNA, true);
		TxtReadandWrite txtRNAcofoldmRNA = new TxtReadandWrite(fileRNAcofoldmRNA, true);
		
		
		SeqHash seqHashMrna = new SeqHash(mrnaSeqFile, " ");
		SeqHash seqHashMiRNA = new SeqHash(mirSeqFile, " ");
		for (BlastInfo blastInfo : lsBlastInfo) {
			SeqFasta seqMRNA = seqHashMrna.getSeq(blastInfo.getSubjectID(), 
					blastInfo.getSStartAbs() -  extendmRNAUp, blastInfo.getSEndAbs() + extendmRNADown);
			seqMRNA.setName(blastInfo.getSubjectID() + SepSign.SEP_INFO+ blastInfo.getsStartLoc() + "_" + blastInfo.getsEndLoc() );
			
			//TODO 如果比对到mRNA序列上，表示该mRNA和miRNA序列一致，
			//那么实际上结合的就应该是mRNA的反相互补序列
			//但是这里就有疑问，miRNA与mRNA的反相互补序列结合，还能调控靶基因吗？
			if (blastInfo.isCis5to3()) {
				seqMRNA = seqMRNA.reservecom();
			}
			SeqFasta seqMiRNA = seqHashMiRNA.getSeq(blastInfo.getQueryID());
			String rnaUp = getRnaUpInfo(seqMiRNA, seqMRNA);
			txtRNAup.writefileln(rnaUp);
			txtRNAcofoldmiRNA.writefileln(getRnaCofold(seqMiRNA));
			txtRNAcofoldmRNA.writefileln(getRnaCofold(seqMRNA));
		}
		seqHashMiRNA.close();
		seqHashMrna.close();
		
		txtRNAcofoldmiRNA.close();
		txtRNAcofoldmRNA.close();
		txtRNAup.close();
	}
	
	private String getRnaUpInfo(SeqFasta seqMiRNA, SeqFasta seqMrna) {
		StringBuilder stringBuilder = new StringBuilder(">");
		stringBuilder.append(seqMiRNA.getSeqName());
		stringBuilder.append("&");
		stringBuilder.append(seqMrna.getSeqName());
		stringBuilder.append("\n");
		stringBuilder.append(seqMiRNA.toString());
		stringBuilder.append("&");
		stringBuilder.append(seqMrna.toString());
		return stringBuilder.toString();
	}
	
	private String getRnaCofold(SeqFasta seqFasta) {
		StringBuilder stringBuilder = new StringBuilder(">");
		stringBuilder.append(seqFasta.getSeqName());
		stringBuilder.append("\n");
		stringBuilder.append(seqFasta.toString());
		stringBuilder.append("&");
		stringBuilder.append(SeqFasta.complement(seqFasta.toString()));
		return stringBuilder.toString();
	}
}
