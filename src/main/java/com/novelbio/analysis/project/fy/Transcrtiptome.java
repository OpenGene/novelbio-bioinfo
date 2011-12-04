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
	}
	
	
	public void reconstruct() {
		GffGeneCluster.geneInso("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/tophatDifParam/a9m0/junctions.bed");
		GffHashGene gffHashGeneRef = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				"/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chickenEnsemblGenes");
				
		GffHashGene gffHashGeneCufflink = new GffHashGene();//(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/OutPut/OutK0noGTF/transcripts.gtf");
		gffHashGeneCufflink.setParam(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF);
		gffHashGeneCufflink.setGffHash(gffHashGeneRef);
		gffHashGeneCufflink.setTaxID(9031);
		String trasnGTFPath = "/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla9m0_newParam/";
		gffHashGeneCufflink.readGffFile( trasnGTFPath + "transcripts.gtf");
		@SuppressWarnings("unused")
		GffHashGene gffHashGene2 = gffHashGeneCufflink.compHashGene(gffHashGeneCufflink, gffHashGeneRef, "/media/winE/Bioinformatics/GenomeData/checken/chromFaLen","/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/tophatDifParam/a9m0/accepted.bed");
//		GffHashGene gffHashGene2 = gffHashGeneCufflink.compHashGene(gffHashGeneCufflink, gffHashGeneRef, "/media/winE/Bioinformatics/GenomeData/checken/chromFaLen", null);

		gffHashGene2.writeToGTF(trasnGTFPath + "novelbioModify_20111201_ML10000_removeDup_All.GTF", "novelbio");
		System.out.println("ok");
	}
	
	public void getFileLine(){
		FastQ fastQ1 = new FastQ("/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_All_1.fq", FastQ.QUALITY_LOW);
		FastQ fastQ2 = new FastQ("/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_All_2.fq", FastQ.QUALITY_LOW);
		System.out.println(fastQ1.getSeqNum());
		System.out.println(fastQ2.getSeqNum());
	}
	
	
	
}
