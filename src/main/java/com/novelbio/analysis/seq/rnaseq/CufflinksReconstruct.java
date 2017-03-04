package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffHashModifyNewGffORF;
import com.novelbio.analysis.seq.genome.GffHashModifyOldGffUTR;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 将cufflinks的结果进行合并，并注释，获得最后的转录本 */
public class CufflinksReconstruct implements IntCmdSoft {
	GffChrAbs gffChrAbs = new GffChrAbs();
	String outPrefix;
	int thread = 4;
	String gtfRefFile;
	String chrSeq;
	String outStatistics;
	String prefixNewGene;
	List<String> lsResultGTF = new ArrayList<>();
	boolean isUseOldResult = true;
	
	/** 是否修正新的Gtf文件 */
	boolean isModifyNewGTF = true;
	boolean isAddUtrToRefGtf = true;
	
	List<String> lsCmd = new ArrayList<>();
	/** 用额外的GTF辅助重建转录本<br>
	 * 和{@link #setGffChrAbs(gffChrAbs)} 两者选一
	 * 优先级高于gffChrAbs
	 * @param gtfFile
	 */
	public void setGTFfile(String gtfFile) {
		this.gtfRefFile = gtfFile;
	}
	
	public void setLsResultGTF(List<String> lsResultGTF) {
		this.lsResultGTF = lsResultGTF;
	}
	
	/** 新基因的前缀 */
	public void setGenePrefixNew(String prefixNewGene) {
		if (prefixNewGene == null || prefixNewGene.trim().equals("")) {
			return;
		}
		prefixNewGene = prefixNewGene.trim();
		if (!prefixNewGene.endsWith("_")) {
			prefixNewGene += "_";
		}
		this.prefixNewGene = prefixNewGene;
	}
	/** 使用上次跑出来的结果
	 * 同样的参数重跑，遇到上次跑出来的结果是否可以直接使用而不重跑
	 *  */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	/** 是否修正新的GTF文件
	 * true修正cufflinks产生的gtf文件
	 * false修正原来的gtf文件，主要是加上utr区域，一般用于低等生物
	 * @param isModifyNewGTF
	 */
	public void setModifyNewGTF(boolean isModifyNewGTF) {
		this.isModifyNewGTF = isModifyNewGTF;
	}
	/** 是否修正新的GTF文件
	 * true修正cufflinks产生的gtf文件
	 * false修正原来的gtf文件，主要是加上utr区域，一般用于低等生物
	 * @param isModifyNewGTF
	 */
	public void setAddUtrToRefGtf(boolean isAddUtrToRefGtf) {
		this.isAddUtrToRefGtf = isAddUtrToRefGtf;
	}
	/** 输入已有的物种信息<br>
	 * 注意如果还输入了gff文件和chr文件，本类会修改该gffChrAbs
	 * 和{@link #setGTFfile(String)} 两者选一
	 * @param gffChrAbs 注意结束后会关闭流
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * species的chr文件优先级高于该文件
	 * @param chrSeq
	 */
	public void setChrSeq(String chrSeq) {
		this.chrSeq = chrSeq;
	}

	public void setOutPathPrefix(String resultPath) {
		this.outPrefix = resultPath;
	}
	
	public void run() {
		lsCmd.clear();
		reconstruct();
		close();
	}
	
	public void reconstruct() {
		setGffHashRef();
		setSeqHash();
		
		outStatistics = outPrefix + "NBCTranscriptomStatistics.txt";
		String resultGtf = null;
		String outMergePrefix = outPrefix + "tmpMerge";
		if (lsResultGTF.size() > 1) {
			CuffMerge cuffMerge = new CuffMerge();
			cuffMerge.setIsUseOldResult(isUseOldResult);
			cuffMerge.setLsGtfTobeMerged(lsResultGTF);
			cuffMerge.setRefGtf(gtfRefFile);
			cuffMerge.setOutputPrefix(outMergePrefix);
			try { cuffMerge.setRefChrFa(gffChrAbs.getSpecies().getChromSeq()); } catch (Exception e) { }
			cuffMerge.setThreadNum(thread);
			try {
				resultGtf = cuffMerge.runCuffmerge();
			} catch (Throwable e) {
				lsCmd.addAll(cuffMerge.getCmdExeStr());
				throw e;
			}
			lsCmd.addAll(cuffMerge.getCmdExeStr());
		} else if (lsResultGTF.size() == 1) {
			resultGtf = lsResultGTF.get(0);
		}
		FileOperate.copyFile(resultGtf, outPrefix + "CuffMerge.gtf", true);
		if (isModifyNewGTF) {
			modifyCufflinksGtf(resultGtf, outPrefix + "NBCTranscriptom.gtf");
		}
		if (isAddUtrToRefGtf) {
			modifyOldGtf(resultGtf, outPrefix + "RefAddUtrTranscriptom.gtf");
		}
	}
	
	/** 
	 * @param resultGtf 重建完转录本的gtf文件
	 * @param outGtf 输出结果文件
	 */
	private void modifyCufflinksGtf(String resultGtf, String outGtf) {
		//TODO 需要增加预测ncRNA和orf的模块
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			return;
		}
		
		//注释orf
		GffHashGene gffHashGeneThis = new GffHashGene(GffType.GTF, resultGtf);
		GffHashModifyNewGffORF gffHashModifyORF = new GffHashModifyNewGffORF();
		gffHashModifyORF.setGffHashGeneRaw(gffHashGeneThis);

		gffHashModifyORF.setGffHashGeneRef(gffChrAbs.getGffHashGene());
		gffHashModifyORF.setRenameGene(true);
		
		gffHashModifyORF.setRenameIso(true);//TODO 可以考虑不换iso的名字
		gffHashModifyORF.modifyGff();
		GffHashGene gffHashGeneModify = gffHashModifyORF.getGffResult();
		List<String> lsChrName = (gffChrAbs.getSeqHash() != null)? gffChrAbs.getSeqHash().getLsSeqName() : null;
		gffHashGeneModify.writeToGTF(lsChrName, outGtf);
		
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setSeqHash(gffChrAbs.getSeqHash());
		gffHashMerge.setGffHashGeneRef(gffChrAbs.getGffHashGene());
		gffHashMerge.addGffHashGene(gffHashGeneModify);
		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(outStatistics, true);
		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
		txtOut.close();
	}
	
	/** 
	 * @param resultGtf 重建完转录本的gtf文件
	 * @param outGtf 输出结果文件
	 */
	private void modifyOldGtf(String resultGtf, String outGtf) {
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			return;
		}
		
		GffHashModifyOldGffUTR gffHashModifyOldGffUTR = new GffHashModifyOldGffUTR();
		GffHashGene gffHashGeneThis = new GffHashGene(GffType.GTF, resultGtf);
		gffHashModifyOldGffUTR.setGffHashGeneRaw(gffHashGeneThis);
		gffHashModifyOldGffUTR.setGffHashGeneRef(gffChrAbs.getGffHashGene());
		gffHashModifyOldGffUTR.modifyGff();
		
		List<String> lsChrName = (gffChrAbs.getSeqHash() != null)? gffChrAbs.getSeqHash().getLsSeqName() : null;
		gffChrAbs.getGffHashGene().writeToGTF(lsChrName, outGtf);
	}
		
	private void setGffHashRef() {
		if (!StringOperate.isRealNull(gtfRefFile) && !gtfRefFile.equals(gffChrAbs.getGtfFile())) {
			GffHashGene gffHashGene = new GffHashGene(GffType.GTF, gtfRefFile);
			gffChrAbs.setGffHash(gffHashGene);
		}
		gtfRefFile = gffChrAbs.getGtfFile();
	}
	
	private void setSeqHash() {
		if (FileOperate.isFileExistAndBigThanSize(chrSeq, 10)) {
			SeqHash seqHash = new SeqHash(chrSeq, null);
			if (gffChrAbs.getSeqHash() != null) {
				gffChrAbs.getSeqHash().close();
				gffChrAbs.setSeqHash(seqHash);
			}
		}
	}
	/** 关闭gffChrAbs中的seqhash信息 */
	private void close() {
		gffChrAbs.close();
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}

}
