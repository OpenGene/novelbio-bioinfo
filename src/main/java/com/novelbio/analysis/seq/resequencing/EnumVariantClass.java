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
	/***/
	Silent, 
	/** 间接位点突变 */
	Splice_Site,
	/** 转录起始位点突变 */
	Translation_Start_Site,
	/** 终止密码子突变 */
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
	RNA,
	/** 靶区域突变 */
	Targeted_Region
}
