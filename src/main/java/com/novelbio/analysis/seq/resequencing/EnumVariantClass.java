package com.novelbio.analysis.seq.resequencing;

/**
 * 用于MAF文件格式中的Variant Classification属性
 * Translational effect of variant allele.
 * @author novelbio
 *
 */
public enum EnumVariantClass {
	/**移码框缺失*/
	Frame_Shift_Del,
	/**移码框插入*/
	Frame_Shift_Ins,
	In_Frame_Del,
	In_Frame_Ins,
	/**错义突变*/
	Missense_Mutation,
	/**无义突变*/
	Nonsense_Mutation,
	/**沉默突变：是一种DNA突变，不显著改变生物表型，此类突变可以发生在non-coding 区域，也可以发生在exon区域，当发生在exon区域时，不引起氨基酸的改变 */
	Silent, 
	/** 间接位点突变 */
	Splice_Site,
	/** 转录起始位点突变 */
	Translation_Start_Site,
	/** 终止密码子突变后，变为非终止密码子 */
	Nonstop_Mutation,
	UTR3,
	/** 3'侧翼突变 */
	Flank3,
	UTR5,
	/** 5'侧翼突变 */
	Flank5,
	/** 基因间区突变 */
	IGR1,
	Intron,
	/** non-coding transcript 变异 */
	RNA,
	/** 靶区域突变 */
	Targeted_Region
}
