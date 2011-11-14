package com.novelbio.analysis.project.fy;

import com.novelbio.analysis.generalConf.NovelBioConst;
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
		
		GffHashGene gffHashGeneRef = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				"/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chickenEnsemblGenes");
		GffHashGene gffHashGeneCufflink = new GffHashGene();//(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/OutPut/OutK0noGTF/transcripts.gtf");
		gffHashGeneCufflink.setParam(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF);
		gffHashGeneCufflink.setGffHash(gffHashGeneRef);
		gffHashGeneCufflink.setTaxID(9031);
		gffHashGeneCufflink.readGffFile( "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/OutPut/OutK0noGTF/transcripts.gtf");
		GffGeneCluster.geneInso("/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/K0/junctions.bed");
		
		@SuppressWarnings("unused")
		GffHashGene gffHashGene2 = gffHashGeneCufflink.compHashGene(gffHashGeneCufflink, gffHashGeneRef);
		gffHashGene2.writeToGTF("/media/winE/NBC/Project/Project_FY_Lab/Result/cufflink_evaluate/tophat/OutPut/OutK0noGTF/novelbioModify_High.GTF", "novelbio");
		System.out.println("ok");
	}
}
