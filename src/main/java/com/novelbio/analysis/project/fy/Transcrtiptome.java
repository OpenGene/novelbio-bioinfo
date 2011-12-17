package com.novelbio.analysis.project.fy;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;

/**
 * 重建转录本
 * @author zong0jie
 *
 */
public class Transcrtiptome {
	public static void main(String[] args) {
		Transcrtiptome transcrtiptome = new Transcrtiptome();
		transcrtiptome.reconstruct();
		
//		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF,
//				"/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/a15m1bf.combined.gtf");
//		gffHashGene.writeToGTF("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/novelbioModify_a15m1bf.GTF", "novelbio");
	}
	
	
	public void reconstruct() {
		
		GffHashGene gffHashGeneRef = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				"/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/mouse_mm9_UCSC_ensembl_sorted");
		GffGeneCluster.geneInso("/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/a14m1sep/junctions.bed");

		GffHashGene gffHashGeneCufflink = new GffHashGene();//(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/OutPut/OutK0noGTF/transcripts.gtf");
		gffHashGeneCufflink.setParam(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF);
		gffHashGeneCufflink.setGffHash(gffHashGeneRef);
		gffHashGeneCufflink.setTaxID(10090);
		String trasnGTFPath = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/mouseRefGTF/";
		gffHashGeneCufflink.readGffFile( trasnGTFPath + "NovelBio20111212.combined.gtf");
		@SuppressWarnings("unused")
		GffHashGene gffHashGene2 = gffHashGeneCufflink.compHashGene(gffHashGeneCufflink, gffHashGeneRef, 
				"/media/winE/Bioinformatics/GenomeData/checken/chromFaLen",
				"/media/winF/NBC/Project/Project_FY/冯英组小鼠测序数据20111122/tophata9m0/Alla15m1/accept.bed",100);
//		GffHashGene gffHashGene2 = gffHashGeneCufflink.compHashGene(gffHashGeneCufflink, gffHashGeneRef, 
//				"/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/ChromFa_chrLen.list", null, 30);

		gffHashGene2.writeToGTF(trasnGTFPath + "novelbioModify_a15m1bf_All_highAll.GTF", "novelbio");
		System.out.println("ok");
	}
	
	public void getFileLine(){
		FastQ fastQ1 = new FastQ("/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_All_1.fq", FastQ.QUALITY_LOW);
		FastQ fastQ2 = new FastQ("/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_All_2.fq", FastQ.QUALITY_LOW);
		System.out.println(fastQ1.getSeqNum());
		System.out.println(fastQ2.getSeqNum());
	}
	
	
	
}
