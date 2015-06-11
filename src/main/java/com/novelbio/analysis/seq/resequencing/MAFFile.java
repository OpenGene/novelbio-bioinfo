package com.novelbio.analysis.seq.resequencing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
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
		VCFFileReader reader = new VCFFileReader(FileOperate.getFile("/home/novelbio/下载/CL.vcf"), false);
		for (VariantContext variantContext : reader) {
			System.out.println(variantContext.getReference().toString().replaceAll("\\*", "") + "");
			List<Allele> lsAlleles = variantContext.getAlternateAlleles();
			GenotypesContext TumGenotype = variantContext.getGenotypes();
			for (String sampleName : TumGenotype.getSampleNames()) {
				System.out.println(sampleName);
			}
		}
		System.out.println();
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
