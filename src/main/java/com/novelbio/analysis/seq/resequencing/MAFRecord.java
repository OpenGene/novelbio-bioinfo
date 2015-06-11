package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

public class MAFRecord {
	private static final String maf_verification_Status_validate = "Veridied";
	private static final String maf_verification_Status_invalidate = "Unknown";
	private static final String sequencing_Phase = "No";
	private static final String validation_Method = "No";
	private static final String score = "No";
	private static final String BAM_File = "No";
	private static final char cis5to3 = '+';
	private static final char cis3to5 = '-';
	/** 当case sample 的GenoType 不存在时，使用该默认值 */
	private static final String genoTypeName = "NovelBio";
	/** Entrez 基因ID */
	private int entrez_Gene_Id = 0;
	
	/** HUGO gene symbol 被HUGO(Human Genome Organisation)认可的已知人类基因名称的缩写*/
	private String hugo_Symbol = "Unknown";
	
	/** Secondary data from orthogonal technology, Tumor genotyping for allele 1, 值可以为： A, T, C, G, and/or - */
	private String tumor_Validation_Allele1 = "-";
	
	/** 暂时走默认 Secondary data from orthogonal technology, Tumor genotyping for allele 2, 值可以为： A, T, C, G, and/or - */
	private String tumor_Validation_Allele2 = "-";
	
	/** 暂时走默认 Secondary data from orthogonal technology, Matched normal genotyping for allele 1, 值可以为： A, T, C, G, and/or - */
	private String match_Norm_Validation_Allele1 = "-";
	
	/** 暂时走默认 Secondary data from orthogonal technology, Matched normal genotyping for allele 2, 值可以为： A, T, C, G, and/or - */
	private String match_Norm_Validation_Allele2 = "-";
	
	/** 暂时走默认 */
	private boolean isVerified = false;
	/** 暂时走默认 */
	private boolean isCis5to3 = true; 
	
	/** 暂时走默认 Mutation的验证状态，值可以为：Untested, Inconclusive, Valid, Invalid */
	private EnumValidStatus validation_Status = EnumValidStatus.Invalid;
	
	/** 暂时走默认 Mutation分类，值可以为：None, Germline, Somatic, LOH, Post-transcriptional modification和Unknown */
	private EnumMutationStatus mutation_Status = EnumMutationStatus.Somatic;

	private MAFFile mafFile = new MAFFile();
	private VariantContext variantContext;
	//TODO 只暂时做测试用
	
	private Set<EnumVariantClass> setVariantClasses = new HashSet<>();
	private String dbSNP_Val_Status = "by1000Genomes";
	private boolean isSomatic = false;
	
	public MAFRecord(VariantContext variantContext, GffChrAbs gffChrAbs) {
		this.variantContext = variantContext;
		
//		if (input.contains("120317577")) {
//			logger.debug("stop");
//		}
		String refId = variantContext.getContig();
		String referenceSeq = variantContext.getReference().toString().replaceAll("\\*", "");
		String altSeq = variantContext.getAlternateAllele(0).toString().replaceAll("\\*", "");
		RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, variantContext.getContig(), variantContext.getStart());
		SiteSnpIndelInfo siteSnpIndelInfo = refSiteSnpIndel.getAndAddAllenInfo(variantContext.getReference().toString().replaceAll("\\*", "") + "",
				variantContext.getAlternateAllele(0).toString().replaceAll("\\*", ""));
		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndel.getGffIso();
		if (siteSnpIndelInfo == null || gffGeneIsoInfo == null) {
			return;
		}
		List<String> lsInfo = new LinkedList<String>();
		if (refSiteSnpIndel.getGffIso() == null) {
			return;
		}
		
		lsInfo.add(siteSnpIndelInfo.getSnpIndelType());
		lsInfo.add(getRefAAnr().toStringAA3());
		lsInfo.add(getThisAAnr().toString());
		lsInfo.add(getThisAAnr().toStringAA3());
		if (this instanceof SiteSnpIndelInfoSnp && this.refSiteSnpIndelParent.getAffectAANum() > 0) {
			lsInfo.add(getRefAAnr().toStringAA3() + this.refSiteSnpIndelParent.getAffectAANum() + getThisAAnr().toStringAA3());
		} else {
			lsInfo.add("");
		}
		lsInfo.add(getSplitTypeEffected());
		lsInfo.add(getAAchamicalConvert());
		return lsInfo;
		
	}
	
	private void initail() {
		//TODO 
//		hugo_Symbol
//		entrez_Gene_Id
//		dbSNP_RS
//		variant_Classification
//		dbSNP_Val_Status
	}
	
	public void addVarationClass(EnumVariantClass enumVariantClass) {
		setVariantClasses.add(enumVariantClass);
	}
	
	public String toString() {
		ArrayList<String> lsMAF = new ArrayList<String>();
		//TODO 需要根据注释的结果，获取SNP所在的基因名称;no
		lsMAF.add(hugo_Symbol);
		//TODO 需要根据注释的结果，获取SNP所在的基因的ID;
		lsMAF.add(entrez_Gene_Id + "");
		// 生成该MAF文件的机构名称，使用默认即可;
		lsMAF.add(mafFile.getCenter());
		//使用分析的参考基因组版本
		lsMAF.add(mafFile.getnCBI_Build());
		//SNP所在的染色体号
		lsMAF.add(variantContext.getContig());
		lsMAF.add(variantContext.getStart() + "");
		lsMAF.add(variantContext.getEnd() + "");
		//SNP所在的链，值为“+”或“-”，好像一直为true
		if (isCis5to3) {
			lsMAF.add(cis5to3 + "");
		} else {
			lsMAF.add(cis3to5 + "");
		}
	
		//TODO 根据注释结果，来确定SNP变异类型是什么？ Frame_Shift_Del？Frame_Shift_Ins？ In_Frame_Del or other?
		lsMAF.add(getVarClass());
		
		lsMAF.add(variantContext.getType().toString());
		lsMAF.addAll(getTumorType());
		lsMAF.addAll(getGenoType());
		//使用默认的空值即可
		lsMAF.add(tumor_Validation_Allele1);
		//使用默认的空值即可
		lsMAF.add(tumor_Validation_Allele2);
		//使用默认的空值即可
		lsMAF.add(match_Norm_Validation_Allele1);
		//使用默认的空值即可
		lsMAF.add(match_Norm_Validation_Allele2);
		//SNP是否验证
		if (isVerified) {
			lsMAF.add(maf_verification_Status_validate);
		} else {
			lsMAF.add(maf_verification_Status_invalidate);
		}
		lsMAF.add(validation_Status.toString());
		lsMAF.add(mutation_Status.toString());
		lsMAF.add(sequencing_Phase);
		lsMAF.add(mafFile.sequence_Source.toString());
		lsMAF.add(validation_Method);
		lsMAF.add(score);
		lsMAF.add(BAM_File);
		lsMAF.add(mafFile.sequencer.toString());
		lsMAF.add(getTumSampleName());
		lsMAF.add(getNorSampleName());
		return org.apache.commons.lang.StringUtils.join(lsMAF.toArray(),"\t");
	}
	
	private String getTumSampleName() {
		Genotype tumType = null;
		if (isSomatic) {
			tumType = variantContext.getGenotype(1);
		} else {
			tumType = variantContext.getGenotype(0);
		}
		return tumType.getSampleName();
	}
	
	private String getNorSampleName() {
		String norName = genoTypeName;
		if (isSomatic) {
			Genotype norType = variantContext.getGenotype(0);
			norName = norType.getSampleName();
		}
		return norName;
	}
	
	private List<String> getTumorType() {
		List<String> lsResult = new ArrayList<>();
		Genotype tumType = null;
		if (isSomatic) {
			tumType = variantContext.getGenotype(1);
		} else {
			tumType = variantContext.getGenotype(0);
		}
		
		lsResult.add(tumType.getAllele(0).toString().replaceAll("\\*", "") + "");
		lsResult.add(tumType.getAllele(1).toString().replaceAll("\\*", "") + "");
		lsResult.add(variantContext.getID());
		lsResult.add(dbSNP_Val_Status);
		lsResult.add(tumType.getSampleName());
		return lsResult;
	}
	private List<String> getGenoType() {
		List<String> lsResult = new ArrayList<>();
		Genotype genoType = null;
		if (isSomatic) {
			genoType = variantContext.getGenotype(0);
			lsResult.add(genoType.getSampleName());
			lsResult.add(genoType.getAllele(0).toString().replaceAll("\\*", "") + "");
			lsResult.add(genoType.getAllele(1).toString().replaceAll("\\*", "") + "");
		} else {
			lsResult.add(genoTypeName);
			lsResult.add(variantContext.getReference().toString().replaceAll("\\*", "") + "");
			lsResult.add(variantContext.getReference().toString().replaceAll("\\*", "") + "");
		}
		
		return lsResult;
	}
	private String getVarClass() {
		String result = "";
		for (EnumVariantClass enumVariantClass : setVariantClasses) {
			result = result + enumVariantClass.toString() + ";";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
}
