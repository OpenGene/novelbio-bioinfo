package com.novelbio.analysis.seq.HanYanRebsome;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 给韩燕画散点图，两个时期的mapping结果进行比较
 * 
 * @author zong0jie
 * 
 */
public class ScatterPlot {
	GffChrMap gffChrMap1;
	GffChrMap gffChrMap2;

	/**
	 * @param gffType
	 * @param gffFile
	 * @param readsBed1
	 * @param readsBed2
	 * @param RefSeq
	 *            mapping文件是否是对refseq进行的mapping
	 */
	public ScatterPlot(String gffType, String gffFile, String chrFileLen ,String readsBed1,
			String readsBed2, boolean RefSeq) {
		gffChrMap1 = new GffChrMap(gffType, gffFile, chrFileLen, readsBed1, 1, false);
		gffChrMap2 = new GffChrMap(null, null, chrFileLen, readsBed2, 1, false);
		if (RefSeq) {
			gffChrMap1.setMapNormType(MapReads.NORMALIZATION_PER_GENE);
			// ipause为3
			// unique reads，soap为4，后来的都为7
			gffChrMap1.setFilter(true, 3, 4, true, true);

			gffChrMap2.setMapNormType(MapReads.NORMALIZATION_PER_GENE);
			// ipause为3
			// unique reads，soap为5，后来的都为7
			gffChrMap2.setFilter(true, 3, 4, true, true);
		} else {
			// TODO
		}
		gffChrMap1.readMapBed();
		gffChrMap2.readMapBed();
	}

	public static void main(String[] args) {
		String mapBed1 = "/media/winE/NBC/Project/Project_HY_Lab/TSC2+KO/bed/TSC2KOlargeSort.bed";
		String mapBed2 = "/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/fastq/largeResultSort"; 
		ScatterPlot scatterPlot = new ScatterPlot(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ,
				"/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/RefSeqFromChr.fa",
				mapBed1, mapBed2, true);
//		scatterPlot.getData("NM_003562");
		scatterPlot.getData(20, "/media/winE/NBC/Project/Project_HY_Lab/test");
		
	}
	
	
	
	
	
	
	/**
	 * 获得单个基因的坐标信息
	 * @param geneID
	 */
	public void getData(String geneID) {
		double[] x = gffChrMap1.getGeneReadsHYRefseq(geneID);
		double[] y = gffChrMap2.getGeneReadsHYRefseq(geneID);
		for (int i = 0; i < y.length; i++) {
			System.out.println(x[i]+"\t"+y[i]);
		}
	}
	/**
	 * 获得第一个实验排名前n位基因的坐标信息
	 * @param geneIDType
	 */
	public void getData(int geneNum,String outFile) {
		TxtReadandWrite txtOutx = null;
		TxtReadandWrite txtOuty = null;
		ArrayList<MapInfo> lsmaArrayList = gffChrMap1.getChrInfo();
		if (geneNum > lsmaArrayList.size()) {
			geneNum = lsmaArrayList.size();
		}
		for (int i = lsmaArrayList.size() - 1; i >=  lsmaArrayList.size() - geneNum; i--) {
			double[] x =lsmaArrayList.get(i).getDouble();
			double[] y = gffChrMap2.getGeneReadsHYRefseq(lsmaArrayList.get(i).getRefID());
			if (y == null) {
				y = new double[x.length];
			}
			txtOutx = new TxtReadandWrite(FileOperate.changeFileSuffix(outFile, "_"+lsmaArrayList.get(i).getRefID()+"_x", "txt"), true);
			txtOutx.Rwritefile(x);
			txtOuty = new TxtReadandWrite(FileOperate.changeFileSuffix(outFile, "_"+lsmaArrayList.get(i).getRefID()+"_y", "txt"), true);
			txtOuty.Rwritefile(y);
			txtOutx.close();
			txtOuty.close();
		}
	}
	
	private String combineDouble(double[] num)
	{
		String xx= "";
		for (double d : num) {
			xx = xx + "\t" + d;
		}
		xx.trim();
		return xx;
	}
	
}
