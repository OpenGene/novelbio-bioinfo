package com.novelbio.analysis.seq.resequencing;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.novelbio.analysis.seq.fasta.CodeInfo;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.EnumMrnaLocate;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

public class MAFRecord {
	private static final String maf_verification_Status_validate = "Veridied";
	private static final String maf_verification_Status_invalidate = "Unknown";
	private static final String sequencing_Phase = "No";
	private static final String validation_Method = "No";
	private static final String score = "No";
	private static final String BAM_File = "No";
	private static final char cis5to3 = '+';
	private static final char cis3to5 = '-';
	/** 可供用的默认值 */
	private static final char NullString = '-';
	/** 当case sample 的GenoType 不存在时，使用该默认值 */
	private static final String genoTypeName = "NovelBio";
	/** Entrez 基因ID */
	private int entrez_Gene_Id = 0;
	
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
	
	private Set<EnumVariantClass> setVariantClasses = new HashSet<>();
	private String dbSNP_Val_Status = "by1000Genomes";
	private boolean isSomatic = true;
	
	/** transcript_status_WU 暂时走默认 */
	private String transcriptStatus = "reviewed";
	
	/** c_position_WU 暂时走默认 */
	private String cPosition = "-";
	
	/** amino_acid_WU 暂时走默认 */
	private String aAChange = "-";
	
	/** ucsc_cons_WU 暂时走默认 */
	private double ucscConsValue = 1;
	
	/** transcript_error 暂时走默认 */
	private String transcriptError = "no_errors";
	
	GffGeneIsoInfo gffGeneIsoInfo;
	RefSiteSnpIndel refSiteSnpIndel;
	
	Species species = new Species();
	public static MAFRecord generateMafRecord(VariantContext variantContext, GffChrAbs gffChrAbs) {
		
		RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, variantContext.getContig(), variantContext.getStart());
		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndel.getGffIso();
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MAFRecord mafRecord = new MAFRecord();
		mafRecord.variantContext = variantContext;
		mafRecord.species = gffChrAbs.getSpecies();
		mafRecord.refSiteSnpIndel = refSiteSnpIndel;
		mafRecord.gffGeneIsoInfo = gffGeneIsoInfo;
		SnpRefAltInfo siteSnpIndelInfo = refSiteSnpIndel.getAndAddAllenInfo(variantContext.getReference().toString().replaceAll("\\*", "") + "",
				variantContext.getAlternateAllele(0).toString().replaceAll("\\*", ""));
		
		if (siteSnpIndelInfo == null) {
			mafRecord.setVarClass(gffGeneIsoInfo);
		} else if (siteSnpIndelInfo != null) {
			mafRecord.setVarClass(siteSnpIndelInfo);
		}	
//		if (refSiteSnpIndel.getAffectAANum() > 0) {
//			cPosition = "c." + siteSnpIndelInfo.getAffectCdsInfo();
//			aAChange = "p." + siteSnpIndelInfo.getAffectAAInfo();
//		}	
		if (gffChrAbs.getTaxID() > 0) {
			
			GeneID geneID = new GeneID(gffGeneIsoInfo.getParentGeneName(), gffChrAbs.getTaxID());
			if (geneID.getIDtype() == GeneID.IDTYPE_GENEID) {
				mafRecord.entrez_Gene_Id = Integer.parseInt(geneID.getGeneUniID());
			}
		}
		return mafRecord;
	}
	
	private void setVarClass(SnpRefAltInfo siteSnpIndelInfo) {
		if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.cds) {
			String refAA = siteSnpIndelInfo.getRefAAnr().toStringAA3();
			String thisAA = siteSnpIndelInfo.getThisAAnr().toStringAA3();
			if (!refAA.equals(thisAA)) {
				if (!refAA.equals(CodeInfo.AA3_STOP) && thisAA.equals(CodeInfo.AA3_STOP)) {
					setVariantClasses.add(EnumVariantClass.Nonsense_Mutation); 
				} else if (refAA.equals(CodeInfo.AA3_STOP) && !thisAA.equals(CodeInfo.AA3_STOP)) {
					setVariantClasses.add(EnumVariantClass.Nonstop_Mutation);
				} else if (!refAA.equals(CodeInfo.AA3_STOP) && !thisAA.equals(CodeInfo.AA3_STOP)) {
					setVariantClasses.add(EnumVariantClass.Missense_Mutation);
				}
			} else {
				setVariantClasses.add(EnumVariantClass.Silent);
			}
		}
		if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.exon) {
			setVariantClasses.add(EnumVariantClass.RNA);
		} else if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.utr5) {
			setVariantClasses.add(EnumVariantClass.UTR5);
		} else if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.utr3) {
			setVariantClasses.add(EnumVariantClass.UTR3);
		} else if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.intergenic) {
			setVariantClasses.add(EnumVariantClass.IGR1);
		} else if (siteSnpIndelInfo.getEnumMrnaLocate() == EnumMrnaLocate.intron) {
			setVariantClasses.add(EnumVariantClass.Intron);
		}
		
		if (siteSnpIndelInfo.getSplitType() != SplitType.NONE) {
			setVariantClasses.add(EnumVariantClass.Splice_Site);
		}
		
		if (siteSnpIndelInfo instanceof SiteSnpIndelInfoInsert && siteSnpIndelInfo.getOrfShift() != 0) {
			setVariantClasses.add(EnumVariantClass.Frame_Shift_Ins);
		} else if (siteSnpIndelInfo instanceof SiteSnpIndelInfoDeletion && siteSnpIndelInfo.getOrfShift() != 0) {
			setVariantClasses.add(EnumVariantClass.Frame_Shift_Ins);
		}
	}
	
	private void setVarClass(GffGeneIsoInfo gffGeneIsoInfo) {
		EnumMrnaLocate enumMrnaLocate = gffGeneIsoInfo.getCodLocate(variantContext.getStart());
		if (enumMrnaLocate == EnumMrnaLocate.intergenic) {
			setVariantClasses.add(EnumVariantClass.IGR1);
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			setVariantClasses.add(EnumVariantClass.Intron);
		} else if (enumMrnaLocate == EnumMrnaLocate.exon) {
			setVariantClasses.add(EnumVariantClass.RNA);
		}
	}
	
	public String toString() {	
		ArrayList<String> lsMAF = new ArrayList<String>();
		if (gffGeneIsoInfo == null) {
			return null;
		} 
		lsMAF.add(gffGeneIsoInfo.getParentGeneName());
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
		lsMAF.add(variantContext.getContig());
		lsMAF.add(variantContext.getStart() + "");
		lsMAF.add(variantContext.getEnd() + "");
		lsMAF.add(getTumorType().get(0));
		lsMAF.add(getTumorType().get(2));
		lsMAF.add(variantContext.getType().toString());
		/** 基因名称 */
		lsMAF.add(gffGeneIsoInfo.getParentGeneName());
		/** 转录本名称 */
		lsMAF.add(gffGeneIsoInfo.getName());
		/** 物种 */
		lsMAF.add(species.getNameLatin());
		/** 转录本来源 */
		lsMAF.add(species.getGffDB());
		/** 转录本版本 */
		lsMAF.add(species.getVersion());
		/** 转录本所在的正负链情况 */
		if (gffGeneIsoInfo.isCis5to3()) {
			lsMAF.add("1");
		} else {
			lsMAF.add("-1");
		}
		lsMAF.add(transcriptStatus);
		/** 突变类型 */
		lsMAF.add(getVarClass());
		lsMAF.add(cPosition);
		lsMAF.add(aAChange);
		/** ucsc_cons_WU */
		lsMAF.add(ucscConsValue + "");
		lsMAF.add(NullString + "");
		lsMAF.add(NullString + "");
		lsMAF.add(NullString + "");
		lsMAF.add(transcriptError);
		return StringUtils.join(lsMAF.toArray(),"\t");
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
		if (setVariantClasses.isEmpty()) {
			return result;
		}
		for (EnumVariantClass enumVariantClass : setVariantClasses) {
			result = result + enumVariantClass.toString() + ";";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}	
}
