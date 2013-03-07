import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

public class FengYing {
	public static void main(String[] args) {
		DateTime dateTime = new DateTime();
		chicken();
		mouse();
		dateTime.setStartTime();
//		topJunctionTest();
//		System.out.println(dateTime.getEclipseTime());
//		String parentFile = "C:/Users/jie/Desktop/paper/";
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
	
	public static void topJunctionTest() {
		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/Result/mapping/";
		TophatJunction tophatJunction = new TophatJunction();
		tophatJunction.setJunFile("KO", parentFile + "KOjunctions.bed");
		tophatJunction.setJunFile("WT",parentFile + "WTjunctions.bed");
		System.out.println(tophatJunction.getJunctionSite("KO", "chr2", 154821007));
		System.out.println(tophatJunction.getJunctionSite("KO", "chr2", 154821089));
		System.out.println(tophatJunction.getJunctionSite("WT", "chr2", 154821007));
		System.out.println(tophatJunction.getJunctionSite("WT", "chr2", 154821089));
		
		System.out.println(tophatJunction.getJunctionSite("KO", "chr2", 154791548, 154857269));
		System.out.println(tophatJunction.getJunctionSite("KO", "chr2", 154791231, 154857269));
		
		System.out.println(tophatJunction.getJunctionSite("WT", "chr2", 154791548, 154857269));
		System.out.println(tophatJunction.getJunctionSite("WT", "chr2", 154791231, 154857269));
	}
	
	public static void mouse() {
//		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/Result/mapping/";
		String parentFile = "/media/winF/NBC/Project/Project_FY/paper/";
		
//		String parentFile = "C:/Users/jie/Desktop/paper/";
		
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				parentFile + "mm10-ensemble-modified.gtf");
//		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
//				parentFile + "mm10-ensemble-modified.gtf");
		GffChrAbs gffChrAbs = new GffChrAbs(10090);
//		System.out.println("finished reading GTF file");

		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		exonJunction.setSeqHash(gffChrAbs.getSeqHash());
		exonJunction.setIsLessMemory(false);
		exonJunction.setOneGeneOneSpliceEvent(false);
//		Species species = new Species(10090, "mm10_NCBI");
//		GffChrAbs gffChrAbs = new GffChrAbs(species);
//		exonJunction.setSeqHash(gffChrAbs.getSeqHash());
//		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed");
//		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
//		System.out.println("finished reading junction reads");
		exonJunction.addBamSorted("KO", parentFile + "KOod.bam");
		exonJunction.addBamSorted("WT", parentFile + "WT0d.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.run();
		System.out.println("finished reading bam file");

		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "Mouse_KO_vs_WT_withexp_NewPvalue.txt";
		exonJunction.writeToFile(outResult);
	}

	
	public static void chicken() {
		String parentFile = "/home/zong0jie/Desktop/paper/chicken/";
		GffChrAbs gffChrAbs = new GffChrAbs(new Species(9031));
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				parentFile + "gal4-merged.gtf");
		
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		exonJunction.setSeqHash(gffChrAbs.getSeqHash());
		exonJunction.setIsLessMemory(false);
		exonJunction.setOneGeneOneSpliceEvent(false);
		
		exonJunction.addBamSorted("KO", parentFile + "DT40KO.bam");
		exonJunction.addBamSorted("WT", parentFile + "DT40WT.bam");
		exonJunction.run();
		System.out.println("finished reading bam file");

		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "Chicken_KO_vs_WT_withexp_NewPvalue.txt";
		exonJunction.writeToFile(outResult);
	}
	
	
}
	
