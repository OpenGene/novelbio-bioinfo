package com.novelbio.analysis.seq.resequencing;
import java.util.ArrayList;
import java.util.Iterator;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class MAFFile {

	private String center = "NovelBio";
	private String nCBI_Build = "GRCh37";
//	private String sequencer = "Illumina HiSeq";
//	private String tumor_Sample_UUID = "Tumor";
//	private String matched_Norm_Sample_UUID = "Normal";
//	private String chromosome_name_WU;
//	private String start_WU;
//	private String stop_WU;
//	private String reference_WU;
//	private String variant_WU;
//	private String type_WU;
//	private String gene_name_WU;
//	private String transcript_name_WU;
//	private String transcript_species_WU;
//	private String transcript_source_WU;
//	private String transcript_version_WU;
//	private String strand_WU;
//	private String transcript_status_WU;
//	private String trv_type_WU;
//	private String c_position_WU;
//	private String amino_acid_change_WU;
//	private String ucsc_cons_WU;
//	private String domain_WU;
//	private String all_domains_WU;
//	private String deletion_substructures_WU;
//	private String transcript_error;
	
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
			MAFRecord mafRecord = new MAFRecord (variantContext);
			txtWrite.writefile(mafRecord.toString());
		}
		txtWrite.close();
	}
	private String MAFFileHead() {
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
		return org.apache.commons.lang.StringUtils.join(lsMAFHead.toArray(),"\t");
	}
	
}
