package com.novelbio.analysis.seq.resequencing;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class MAFFile {

	private String center = "NovelBio";
	private String nCBI_Build = "GRCh37";
	/** 序列测序策略， 值可以为：WGS,WGA,WXS,RNA-Seq,Other */
	protected EnumSeqSource sequence_Source = EnumSeqSource.WGS;
	
	/** 序列测序平台，值可以为： IlluminaGAllx, IlluminaHiSeq, SOLID, FourFiveFour, IonTorrentPGM, IonTorrentProton, IlluminaHiSeq2500 */
	protected EnumSequencer sequencer = EnumSequencer.IlluminaHiSeq;
	
	public static void main(String[] args) {
		
		String vcfFilePath = "/home/novelbio/VCF";
		ArrayList<String[]> lsFile = new ArrayList<>();
		lsFile = FileOperate.getFoldFileName(vcfFilePath, "format", "vcf");
		for (String[] arrFile:lsFile) {
			String realFileName = arrFile[0];
			String filePath = vcfFilePath + "/" + realFileName + ".vcf";
			File file = FileOperate.getFile(filePath);
			VCFFileReader reader = new VCFFileReader(file, false);
			Species species = new Species(9606, "hg19_GRCh37");
			GffChrAbs gffChrAbs = new GffChrAbs(species);
			String mafFilePath = "//home//novelbio//VCF//" + file.getName() + ".maf";  // S084103_IPVSS084103_Ca.maf
			TxtReadandWrite txtWrite = new TxtReadandWrite(mafFilePath, true);
			MAFFile mafFile = new MAFFile ();
			txtWrite.writefile(mafFile.MAFFileHead() + "\n");
			int i=0;
			for (VariantContext variantContext : reader) {
//				if (i++ > 300) {
//					break;
//				}
				MAFRecord mafRecord = new MAFRecord();
				MAFRecord mAFRecord = mafRecord.generateMafRecord(variantContext, gffChrAbs);
				if (mAFRecord != null) {
					
					txtWrite.writefile(mAFRecord.toString() + "\n");
				}	
//				System.out.println(mafRecord.toString());
			}
			txtWrite.close();
			System.out.println("Finished !!!");
		}

		
//		GffHashGene gffHashGene = new GffHashGene("/home/novelbio/NBCsource/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gff3.gz");
//		gffHashGene.writeToGTF("/home/novelbio/NBCsource/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gtf");

	}
	
	public void setCenter(String center) {
		this.center = center;
	}
	public String getCenter() {
		return center;
	}
	public void setnCBI_Build(String nCBI_Build) {
		this.nCBI_Build = nCBI_Build;
	}
	public String getnCBI_Build() {
		return nCBI_Build;
	}

	public void VcfToMAFFile(String vcfFile,String mafPath) {
		
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(vcfFile), false);
		Iterator<VariantContext> it = vcfFileReader.iterator();		
		TxtReadandWrite txtWrite = new TxtReadandWrite(mafPath, true);
		txtWrite.writefile(this.MAFFileHead() + "\n");
		while(it.hasNext()) {
			VariantContext variantContext = it.next();
//			MAFRecord mafRecord = new MAFRecord (variantContext);
//			txtWrite.writefile(mafRecord.toString());
		}
		txtWrite.close();
	}
	public String MAFFileHead() {
		//MAF文件，title 放在lsMAFHead中;
		ArrayList<String> lsMAFHead = new ArrayList<String>();
		lsMAFHead.add("Hugo_Symbol");
		lsMAFHead.add("Entrez_Gene_Id");
		lsMAFHead.add("Center");
		lsMAFHead.add("NCBI_Build");
		lsMAFHead.add("Chromosome");
		lsMAFHead.add("Start_Position");
		lsMAFHead.add("End_Position");
		lsMAFHead.add("Strand");
		lsMAFHead.add("Variant_Classification");
		lsMAFHead.add("Variant_Type");
		lsMAFHead.add("Reference_Allele");
		lsMAFHead.add("Tumor_Seq_Allele1");
		lsMAFHead.add("Tumor_Seq_Allele2");
		lsMAFHead.add("dbSNP_RS");
		lsMAFHead.add("dbSNP_Val_Status");
		lsMAFHead.add("Tumor_Sample_Barcode");
		lsMAFHead.add("Matched_Norm_Sample_Barcode");
		lsMAFHead.add("Match_Norm_Seq_Allele1");
		lsMAFHead.add("Match_Norm_Seq_Allele2");
		lsMAFHead.add("Tumor_Validation_Allele1");
		lsMAFHead.add("Tumor_Validation_Allele2");
		lsMAFHead.add("Match_Norm_Validation_Allele1");
		lsMAFHead.add("Match_Norm_Validation_Allele2");
		lsMAFHead.add("Verification_Status");
		lsMAFHead.add("Validation_Status");
		lsMAFHead.add("Mutation_Status");
		lsMAFHead.add("Sequencing_Phase");
		lsMAFHead.add("Seqence_Source");
		lsMAFHead.add("Validation_Method");
		lsMAFHead.add("Score");
		lsMAFHead.add("BAM_File");
		lsMAFHead.add("Sequencer");
		lsMAFHead.add("Tumor_Sample_UUID");
		lsMAFHead.add("Matched_Norm_Sample_UUID");
		lsMAFHead.add("chromosome_name_WU");
		lsMAFHead.add("start_WU");
		lsMAFHead.add("stop_WU");
		lsMAFHead.add("reference_WU");
		lsMAFHead.add("variant_WU");
		lsMAFHead.add("type_WU");
		lsMAFHead.add("gene_name_WU");
		lsMAFHead.add("transcript_name_WU");
		lsMAFHead.add("transcript_species_WU");
		lsMAFHead.add("transcript_source_WU");
		lsMAFHead.add("transcript_version_WU");
		lsMAFHead.add("strand_WU");
		lsMAFHead.add("transcript_status_WU");
		lsMAFHead.add("trv_type_WU");
		lsMAFHead.add("c_position_WU");
		lsMAFHead.add("amino_acid_change_WU");
		lsMAFHead.add("ucsc_cons_WU");
		lsMAFHead.add("domain_WU");
		lsMAFHead.add("all_domains_WU");
		lsMAFHead.add("deletion_substructures_WU");
		lsMAFHead.add("transcript_error");
		return org.apache.commons.lang.StringUtils.join(lsMAFHead.toArray(),"\t");
	}
	
}
