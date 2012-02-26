package com.novelbio.test;

import java.util.ArrayList;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.Rplot;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 专门给冯英做项目
 * @author zong0jie
 *
 */
public class FYrna {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String gffFile = "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chickenEnsemblGenes";
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, gffFile);
		ArrayList<Integer> lsIntronAll = new ArrayList<Integer>();
		ArrayList<Integer> lsExonAll = new ArrayList<Integer>();
		ArrayList<String>lsID = gffHashGene.getLOCChrHashIDList();
		for (String string : lsID) {
			GffDetailGene gffdetail = gffHashGene.searchLOC(string.split("/")[0]);
			ArrayList<int[]> lsExon = gffdetail.getLongestSplit().getIsoInfo();
			for (int i = 0; i < lsExon.size(); i++) {
				lsExonAll.add(Math.abs(lsExon.get(i)[1] - lsExon.get(i)[0]) + 1);
			}
			for (int i = 0; i < lsExon.size() - 1; i++) {
				lsIntronAll.add(Math.abs(  lsExon.get(i+1)[0] -  lsExon.get(i)[1]  ) -1 );
			}
		}
		
		try {
			Rplot.plotHist(lsExonAll, 0, 10000, "exon", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "ExonLen0-10k");
			Rplot.plotHist(lsExonAll, 10000, 100000, "exon", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "ExonLen10k-100k");
			Rplot.plotHist(lsExonAll, 0, 500, "exon", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "ExonLen0-500");
			Rplot.plotHist(lsIntronAll, 0, 10000, "intron", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "IntronLen0-10k");
			Rplot.plotHist(lsIntronAll, 10000, 100000, "intron", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "IntronLen10k-100k");
			Rplot.plotHist(lsIntronAll, 0, 500, "intron", "num", "length", "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/", "IntronLen0-500");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 *  过滤reads 
	 */
	public static void cleanReads(String seqFile1, String seqFile2,String out)
	{
		FastQ fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setReadsLenMin(50);
		fastQ.filterReads(out);
		
		
		
		
	}
}
