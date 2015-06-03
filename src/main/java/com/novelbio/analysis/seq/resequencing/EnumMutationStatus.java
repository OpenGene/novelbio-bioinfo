package com.novelbio.analysis.seq.resequencing;

/**
 * 用于MAF文件格式中的Mutation Status属性
 * @author novelbio
 *
 */
public enum EnumMutationStatus {

	None,
	Germline,
	Somatic,
	LOH,
	PostTranModi,
	Unknown
}
