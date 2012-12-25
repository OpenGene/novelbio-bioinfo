import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

public class FengYing {
	public static void main(String[] args) {
		mouse();

	}
	
	public static void mouse() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"C:/Users/jie/Desktop/paper/mm10_from_cufflinks.gtf");
		System.out.println("finished reading GTF file");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "C:/Users/jie/Desktop/paper/";
		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed"); 
		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
		System.out.println("finished reading junction reads");
		exonJunction.addBamFile_Sorted("KO", parentFile + "KOod.bam");
		exonJunction.addBamFile_Sorted("WT", parentFile + "WT0d.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
		Species species = null;
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		System.out.println("finished reading bam file");

		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "KO_vs_WT.xls";
		exonJunction.writeToFile(outResult);
	}

}
	