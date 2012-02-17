package com.novelbio.analysis.annotation.network;

import java.util.Set;

/**
 * ��������ͼ�������ڵ�֮��Ĺ�ϵ<br>
 * @author zong0jie
 */
public class AbsNetRelate {
	/**
	 * KEGG��
	 * ����һ�����������һ����ĸ����Ȼ��ø��������������pathway�����������������Ҫ�����������׺ϲ�ID
	 */
	public static String RELATE_GROUP = "group"; 
	/**
	 * KEGG��
	 * enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps
	 */
	public static String RELATE_ECREL = "ECrel"; 
	/**
	 * KEGG��
	 * protein-protein interaction, such as binding and modification
	 */
	public static String RELATE_PPREL = "PPrel"; 
	/**
	 * KEGG��
	 * gene expression interaction, indicating relation of transcription factor and target gene product
	 */
	public static String RELATE_GEREL = "GErel"; 
	/**
	 * KEGG��
	 * protein-compound interaction
	 */
	public static String RELATE_PCREL = "PCrel"; 
	/**
	 * KEGG��
	 * enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps
	 */
	public static String RELATE_MAPLINK = "maplink"; 
	/**
	 * Reactome 
	 */
	public static String RELATE_NB_REACT = "neighbouring_reaction";
	/**
	 * Reactome 
	 */
	public static String RELATE_REACT = "reaction";
	/**
	 * Reactome 
	 */
	public static String RELATE_DIRECT_COMPLEX = "direct_complex";
	/**
	 * Reactome 
	 */
	public static String RELATE_INDIRECT_COMPLEX = "indirect_complex";
	
	
	///////////////�����Ǹ��ֳ���//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	AbsNetEntity absNetEntity1;
	AbsNetEntity absNetEntity2;
	
	/**
	 * RelateType��RelateTpye�����ڱ���ĳ���RELATEϵ����
	 * @param absNetRelate
	 */
	String relateType = "";
	/**
	 * �趨��һ���ڵ�
	 * @param absNetRelate
	 */
	public void setNetEntity1(AbsNetEntity absNetEntity1) {
		this.absNetEntity1 = absNetEntity1;
	}
	/**
	 * ��õ�һ���ڵ�
	 * @param absNetRelate
	 */
	public AbsNetEntity getNetEntity1() {
		return this.absNetEntity1;
	}

	/**
	 * �趨�ڶ����ڵ�
	 * @param absNetRelate
	 */
	public void setNetEntity2(AbsNetEntity absNetEntity2) {
		this.absNetEntity2 = absNetEntity2;
	}
	/**
	 * ��õڶ����ڵ�
	 * @param absNetRelate
	 */
	public AbsNetEntity getNetEntity2() {
		return this.absNetEntity2;
	}
	
	/**
	 * �趨RelateType��RelateTpye�����ڱ���ĳ���RELATEϵ����
	 * @param absNetRelate
	 */
	public void setRelateType(String relateType) {
		this.relateType = relateType;
	}
	/**
	 * ���RelateType��RelateTpye�����ڱ���ĳ���RELATEϵ����
	 * @param absNetRelate
	 */
	public String getRelateType() {
		return this.relateType;
	}
	
	
	
	
	
	
	
	
	
	
}
