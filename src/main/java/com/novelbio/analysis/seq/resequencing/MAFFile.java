package com.novelbio.analysis.seq.resequencing;
import java.util.ArrayList;
import java.util.Iterator;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class MAFFile {

	private String hugo_Symbol = "Unknown";
	private Integer entrez_Gene_Id = 0;
	private String center = "NovelBio";
	private String nCBI_Build = "GRCh37";
	private String chromosome;
	private String start_Pos;
	private String end_Pos;
	private char strand = '+';
	private String variant_Classification = "Non";
	private String variant_Type;
	private String reference_Allele;
	private String tumor_Seq_Allele1;
	private String tumor_Seq_Allele2;
	private String dbSNP_RS = "";
	private String dbSNP_Val_Status;
	private String tumor_Sample_Barcode = "TUMOR";
	private String matched_Norm_Sample_Barcode = "NORMAL";
	private String match_Norm_Seq_Allele1;
	private String match_Norm_Seq_Allele2;
	private String tumor_Validation_Allele1 = "";
	private String tumor_Validation_Allele2 = "";
	private String match_Norm_Validation_Allele1 = "";
	private String match_Norm_Validation_Allele2 = "";
	private String verification_Status = "Unknown";
	private String validation_Status = "Invalid";
	private String mutation_Status = "Somatic";
	private String sequencing_Phase;
	private String sequence_Source = "WES";
	private String validation_Method = "No";
	private String score;
	private String BAM_File;
	private String sequencer = "Illumina HiSeq";
	private String tumor_Sample_UUID = "Tumor";
	private String matched_Norm_Sample_UUID = "Normal";
	private String chromosome_name_WU;
	private String start_WU;
	private String stop_WU;
	private String reference_WU;
	private String variant_WU;
	private String type_WU;
	private String gene_name_WU;
	private String transcript_name_WU;
	private String transcript_species_WU;
	private String transcript_source_WU;
	private String transcript_version_WU;
	private String strand_WU;
	private String transcript_status_WU;
	private String trv_type_WU;
	private String c_position_WU;
	private String amino_acid_change_WU;
	private String ucsc_cons_WU;
	private String domain_WU;
	private String all_domains_WU;
	private String deletion_substructures_WU;
	private String transcript_error;
	
	public void setCenter(String center) {
		this.center = center;
	}
	public void setnCBI_Build(String nCBI_Build) {
		this.nCBI_Build = nCBI_Build;
	}
	
	public void VcfToMAFFile(String vcfFile,String mafPath) {
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
		
		String mafFileHead = org.apache.commons.lang.StringUtils.join(lsMAFHead.toArray(),"\t");
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(vcfFile), false);
		Iterator<VariantContext> it = vcfFileReader.iterator();
		MAFFile mafFile = new MAFFile();
		TxtReadandWrite txtWrite = new TxtReadandWrite(mafPath, true);
		txtWrite.writefile(mafFileHead + "\n");
		while(it.hasNext()) {
			VariantContext variantContext = it.next();
			txtWrite.writefile(mafFile.VCFtoMAFString(variantContext));
		}
		txtWrite.close();
	}
	
	public String VCFtoMAFString(VariantContext variantContext) {
		ArrayList<String> lsMAF = new ArrayList<String>();
		//TODO 需要根据注释的结果，获取SNP所在的基因名称;
		lsMAF.add(hugo_Symbol);
		//TODO 需要根据注释的结果，获取SNP所在的基因的ID;
		lsMAF.add(entrez_Gene_Id.toString());
		// 生成该MAF文件的机构名称，使用默认即可;
		lsMAF.add(center);
		//使用分析的参考基因组版本
		lsMAF.add(nCBI_Build);
		//SNP所在的染色体号
		lsMAF.add(variantContext.getContig());
		lsMAF.add(variantContext.getStart() + "");
		lsMAF.add(variantContext.getEnd() + "");
		//SNP所在的链，值为“+”或“-”
		lsMAF.add(strand + "");
		//TODO 根据注释结果，确定SNP变异类型
		lsMAF.add(variant_Classification);
		
		lsMAF.add(variantContext.getType().toString());
		lsMAF.add(variantContext.getReference().toString().replaceAll("\\*", "") + "");
		Genotype TumGenotype = variantContext.getGenotype(1);
		lsMAF.add(TumGenotype.getAllele(0).toString().replaceAll("\\*", "") + "");
		lsMAF.add(TumGenotype.getAllele(1).toString().replaceAll("\\*", "") + "");
		//TODO SNP在dbSNP中的注释结果
		lsMAF.add(dbSNP_RS);
		lsMAF.add(dbSNP_Val_Status);
		// 使用Tumor样品名称作为Tumor sample Barcode 
		lsMAF.add(TumGenotype.getSampleName());
		Genotype NorGenotype = variantContext.getGenotype(0);
		// 使用Normal样品名称作为Normal sample Barcode 
		lsMAF.add(NorGenotype.getSampleName());
		lsMAF.add(NorGenotype.getAllele(0).toString().replaceAll("\\*", ""));
		lsMAF.add(NorGenotype.getAllele(1).toString().replaceAll("\\*", "") + "");
		//使用默认的空值即可
		lsMAF.add(tumor_Validation_Allele1);
		//使用默认的空值即可
		lsMAF.add(tumor_Validation_Allele2);
		//使用默认的空值即可
		lsMAF.add(match_Norm_Validation_Allele1);
		//使用默认的空值即可
		lsMAF.add(match_Norm_Validation_Allele2);
		//SNP验证情况
		lsMAF.add(verification_Status);
		lsMAF.add(validation_Status);
		lsMAF.add(mutation_Status);
		lsMAF.add(sequencing_Phase);
		lsMAF.add(sequence_Source);
		lsMAF.add(validation_Method);
		lsMAF.add(score);
		lsMAF.add(BAM_File);
		lsMAF.add(sequencer);
		lsMAF.add(TumGenotype.getSampleName());
		lsMAF.add(NorGenotype.getSampleName());
		return org.apache.commons.lang.StringUtils.join(lsMAF.toArray(),"\t");
	}
	
	
	
	
}
