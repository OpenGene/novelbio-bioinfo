import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileReading;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

public class FengYing {
	public static void main(String[] args) {
		DateTime dateTime = new DateTime();
		dateTime.setStartTime();
		mouse();
		System.out.println(dateTime.getEclipseTime());
//		String parentFile = "C:/Users/jie/Desktop/paper/";
//		
//		
//		TophatJunction tophatJunction = new TophatJunction();
//		tophatJunction.setCondition("KO");
//		tophatJunction.setJunFile("KO", parentFile + "KOjunctions.bed");
////		SamFile samFile = new SamFile( parentFile + "KOod.bam");
////		SamFileReading samFileReading = new SamFileReading(samFile);
////		samFileReading.addAlignmentRecorder(tophatJunction);
////		samFileReading.run();
//		tophatJunction.writeTo("KO", parentFile + "KOod_jun_junc.txt");
	}
	
	public static void mouse() {
		String parentFile = "/media/winF/NBC/Project/Project_FY/paper/";
//		String parentFile = "C:/Users/jie/Desktop/paper/";
		
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				parentFile + "mm10-ensemble-modified.gtf");
		System.out.println("finished reading GTF file");

		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		exonJunction.setIsLessMemory(false);
		Species species = new Species(10090, "mm10_NCBI");
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		exonJunction.setSeqHash(gffChrAbs.getSeqHash());
		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed");
		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
//		System.out.println("finished reading junction reads");
		exonJunction.addBamSorted("KO", parentFile + "KOod.bam");
		exonJunction.addBamSorted("WT", parentFile + "WT0d.bam");
		exonJunction.setMapreadsBin(15);
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadBamFile();
		System.out.println("finished reading bam file");

		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "KO_vs_WT10_Bam_Jun_UseMemory_Seq_2.xls";
		exonJunction.writeToFile(outResult);
	}

}
	