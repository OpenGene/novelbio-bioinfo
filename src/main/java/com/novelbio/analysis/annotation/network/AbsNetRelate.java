package com.novelbio.analysis.annotation.network;

import java.util.Set;

/**
 * 保存网络图中两个节点之间的关系<br>
 * @author zong0jie
 */
public class AbsNetRelate {
	/**
	 * KEGG：
	 * 与另一个蛋白组成了一个大的复合物，然后该复合物会参与后续的pathway，所以遇到该情况需要将这两个蛋白合并ID
	 */
	public static String RELATE_GROUP = "group"; 
	/**
	 * KEGG：
	 * enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps
	 */
	public static String RELATE_ECREL = "ECrel"; 
	/**
	 * KEGG：
	 * protein-protein interaction, such as binding and modification
	 */
	public static String RELATE_PPREL = "PPrel"; 
	/**
	 * KEGG：
	 * gene expression interaction, indicating relation of transcription factor and target gene product
	 */
	public static String RELATE_GEREL = "GErel"; 
	/**
	 * KEGG：
	 * protein-compound interaction
	 */
	public static String RELATE_PCREL = "PCrel"; 
	/**
	 * KEGG：
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
	
	
	///////////////以上是各种常量//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	AbsNetEntity absNetEntity1;
	AbsNetEntity absNetEntity2;
	
	/**
	 * RelateType，RelateTpye必须在本类的常量RELATE系列中
	 * @param absNetRelate
	 */
	String relateType = "";
	/**
	 * 设定第一个节点
	 * @param absNetRelate
	 */
	public void setNetEntity1(AbsNetEntity absNetEntity1) {
		this.absNetEntity1 = absNetEntity1;
	}
	/**
	 * 获得第一个节点
	 * @param absNetRelate
	 */
	public AbsNetEntity getNetEntity1() {
		return this.absNetEntity1;
	}

	/**
	 * 设定第二个节点
	 * @param absNetRelate
	 */
	public void setNetEntity2(AbsNetEntity absNetEntity2) {
		this.absNetEntity2 = absNetEntity2;
	}
	/**
	 * 获得第二个节点
	 * @param absNetRelate
	 */
	public AbsNetEntity getNetEntity2() {
		return this.absNetEntity2;
	}
	
	/**
	 * 设定RelateType，RelateTpye必须在本类的常量RELATE系列中
	 * @param absNetRelate
	 */
	public void setRelateType(String relateType) {
		this.relateType = relateType;
	}
	/**
	 * 获得RelateType，RelateTpye必须在本类的常量RELATE系列中
	 * @param absNetRelate
	 */
	public String getRelateType() {
		return this.relateType;
	}
	
	
	
	
	
	
	
	
	
	
}
