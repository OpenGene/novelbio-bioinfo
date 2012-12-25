import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

class FengYing {
	public static void main(String[] args) {
		mouse();
	}
	
	public static void mouse() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/TOSHIBA EXT/fengying/mouse/paper/mm10_from_cufflinks_New.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/TOSHIBA EXT/fengying/mouse/paper/";
		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed"); 
		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
		exonJunction.addBamFile_Sorted("KO", parentFile + "KOod.bam");
		exonJunction.addBamFile_Sorted("WT", parentFile + "WT0d.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
		Species species = new Species(10090);
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "KO_vs_WT.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void mouse2() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_1/junctions.bed"); 
		exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_2/junctions.bed");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_2/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_2/accepted_hits.bam");

		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K0vsWT0outDifResult_test_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void mouseHeart() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_1/junctions.bed");
		exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_2/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_2/accepted_hits.bam");

		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);

		String outResult = "/media/wipublic static void main(String[] args) {
		mouse();
	}
	
	public static void mouse() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/TOSHIBA EXT/fengying/mouse/paper/mm10_from_cufflinks_New.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/TOSHIBA EXT/fengying/mouse/paper/";
		exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed"); 
		exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
		exonJunction.addBamFile_Sorted("KO", parentFile + "KOod.bam");
		exonJunction.addBamFile_Sorted("WT", parentFile + "WT0d.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
		Species species = new Species(10090);
//		System.out.println(species.getVersionAll().get(1));
//		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = parentFile +  "KO_vs_WT.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void mouse2() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_1/junctions.bed"); 
		exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_2/junctions.bed");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_2/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_2/accepted_hits.bam");

		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K0vsWT0outDifResult_test_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void mouseHeart() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_1/junctions.bed");
		exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_2/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_2/accepted_hits.bam");

		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		exonJunction.setOneGeneOneSpliceEvent(false);

		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/HeartK0vsWT0outDifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}

	public static void chicken() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_2/accepted_hits.bam");

		exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT0a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sopublic static void main(String[] args) {
			mouse();
		}
		
		public static void mouse() {
			GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
					"/media/TOSHIBA EXT/fengying/mouse/paper/mm10_from_cufflinks_New.gtf");
			ExonJunction exonJunction = new ExonJunction();
			exonJunction.setGffHashGene(gffHashGene);
			
			String parentFile = "/media/TOSHIBA EXT/fengying/mouse/paper/";
			exonJunction.setIsoJunFile("KO", parentFile + "KOjunctions.bed"); 
			exonJunction.setIsoJunFile("WT",parentFile + "WTjunctions.bed");
			exonJunction.addBamFile_Sorted("KO", parentFile + "KOod.bam");
			exonJunction.addBamFile_Sorted("WT", parentFile + "WT0d.bam");
//			exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
//			exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
			Species species = new Species(10090);
//			System.out.println(species.getVersionAll().get(1));
//			species.setVersion(species.getVersionAll().get(1));
			exonJunction.loadingBamFile(species);
			exonJunction.setOneGeneOneSpliceEvent(false);
			String outResult = parentFile +  "KO_vs_WT.xls";
			exonJunction.writeToFile(outResult);
		}
		public static void mouse2() {
			GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
					"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
			ExonJunction exonJunction = new ExonJunction();
			exonJunction.setGffHashGene(gffHashGene);
			
			String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
			exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_1/junctions.bed"); 
			exonJunction.setIsoJunFile("K0", parentFile + "MEFK00da14m1_2/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_1/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "MEFWT0da14m1_2/junctions.bed");
			exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_2/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_2/accepted_hits.bam");

			Species species = new Species(10090);
			species.setVersion(species.getVersionAll().get(1));
			exonJunction.loadingBamFile(species);
			exonJunction.setOneGeneOneSpliceEvent(false);
			String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K0vsWT0outDifResult_test_bam.xls";
			exonJunction.writeToFile(outResult);
		}
		public static void mouseHeart() {
			GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
					"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
			ExonJunction exonJunction = new ExonJunction();
			exonJunction.setGffHashGene(gffHashGene);

			String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
			exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_1/junctions.bed");
			exonJunction.setIsoJunFile( "K0", parentFile + "heartK0a14m1_2/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_1/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "heartWTa14m1_2/junctions.bed");
			
			exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_2/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_2/accepted_hits.bam");

			Species species = new Species(10090);
			species.setVersion(species.getVersionAll().get(1));
			exonJunction.loadingBamFile(species);
			exonJunction.setOneGeneOneSpliceEvent(false);

			String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/HeartK0vsWT0outDifResult_bam.xls";
			exonJunction.writeToFile(outResult);
		}

		public static void chicken() {
			GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
					"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
			ExonJunction exonJunction = new ExonJunction();
			exonJunction.setGffHashGene(gffHashGene);

			String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
			exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_1/junctions.bed");
			exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_2/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_1/junctions.bed");
			exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_2/junctions.bed");
			
			exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_2/accepted_hits.bam");

			exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT0a15m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT0a15m1_2/accepted_hits.bam");

			exonJunction.loadingBamFile(new Species(9013));
			
			String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
			exonJunction.writeToFile(outResult);
		}
		public static void chicken5() {
			GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
					"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
			ExonJunction exonJunction = new ExonJunction();
			exonJunction.setGffHashGene(gffHashGene);

			String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
			exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_1/junctions.bed");
			exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_2/junctions.bed");
			exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_1/junctions.bed");
			exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_2/junctions.bed");
			
			exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_2/accepted_hits.bam");

			exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_1/accepted_hits.bam");
			exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_2/accepted_hits.bam");

			exonJunction.loadingBamFile(new Species(9013));
			
			String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
			exonJunction.writeToFile(outResult);
		}
		
		public static void mouseGTFreconstruct() {
			String gffhashGeneCuf = "/home/zong0jie/Desktop/transcripts.gtf";
			String gffFinal = "/home/zong0jie/Desktop/finalTranscript.gtf";
			String gffFinalStatistics = "/home/zong0jie/Desktop/transcriptomeStatistics.txt";
			Species species = new Species(9606);
			GffHashMerge gffHashMerge = new GffHashMerge();
			gffHashMerge.setSpecies(species);
			gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
			gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));
			GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
			gffHashGene.removeDuplicateIso();
			gffHashGene.writeToGTF(gffFinal, "novelbio");

			gffHashMerge = new GffHashMerge();
			gffHashMerge.setSpecies(species);
			gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
			gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFinal));

			TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
			TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);

			txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
		}
rted("WT0", parentFile + "tophatWT0a15m1_2/accepted_hits.bam");

		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void chicken5() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_2/accepted_hits.bam");

		exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_2/accepted_hits.bam");

		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	
	public static void mouseGTFreconstruct() {
		String gffhashGeneCuf = "/home/zong0jie/Desktop/transcripts.gtf";
		String gffFinal = "/home/zong0jie/Desktop/finalTranscript.gtf";
		String gffFinalStatistics = "/home/zong0jie/Desktop/transcriptomeStatistics.txt";
		Species species = new Species(9606);
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.removeDuplicateIso();
		gffHashGene.writeToGTF(gffFinal, "novelbio");

		gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFinal));

		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);

		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}
nF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/HeartK0vsWT0outDifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}

	public static void chicken() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("K0", parentFile + "tophatK0a15m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT0", parentFile + "tophatWT0a15m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK0a15m1_2/accepted_hits.bam");

		exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT0a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT0a15m1_2/accepted_hits.bam");

		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	public static void chicken5() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("K5", parentFile + "tophatK5a15m1_2/junctions.bed");
		exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_1/junctions.bed");
		exonJunction.setIsoJunFile("WT5", parentFile + "tophatWT5a15m1_2/junctions.bed");
		
		exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("K5", parentFile + "tophatK5a15m1_2/accepted_hits.bam");

		exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT5", parentFile + "tophatWT5a15m1_2/accepted_hits.bam");

		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5DifResult_bam.xls";
		exonJunction.writeToFile(outResult);
	}
	
	public static void mouseGTFreconstruct() {
		String gffhashGeneCuf = "/home/zong0jie/Desktop/transcripts.gtf";
		String gffFinal = "/home/zong0jie/Desktop/finalTranscript.gtf";
		String gffFinalStatistics = "/home/zong0jie/Desktop/transcriptomeStatistics.txt";
		Species species = new Species(9606);
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.removeDuplicateIso();
		gffHashGene.writeToGTF(gffFinal, "novelbio");

		gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFinal));

		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);

		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}

}
	