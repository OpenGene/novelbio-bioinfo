import com.novelbio.analysis.seq.genome.GffChrAbs;
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
				"/media/winF/NBC/Project/Project_FY/paper/mm10_from_cufflinks.gtf");
		System.out.println("finished reading GTF file");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		Species species = new Species(10090, "mm10_NCBI");
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		exonJunction.setSeqHash(gffChrAbs.getSeqHash());
		String parentFile = "/media/winF/NBC/Project/Project_FY/paper/";
		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed"); 
		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
		System.out.println("finished reading junction reads");
//		exonJunction.addBamFile_Sorted("KO", parentFile + "KOod.bam");
//		exonJunction.addBamFile_Sorted("WT", parentFile + "WT0d.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		System.out.println("finished reading bam file");

		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "KO_vs_WT7_NoBam_With_Seq.xls";
		exonJunction.writeToFile(outResult);
	}

}
	