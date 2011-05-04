package com.novelBio.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import entity.kegg.KGentry;
import entity.kegg.KGrelation;

/**
 * 用kegg的entry查找relation表后，获得的结果保存在该类里面<br>
 * 一个entry对应一个relation，返回一个信息。<br>
 * 本类保存与查找entry相关entry的KGentry信息<br>
 * relation关系<br>
 * subtypeName<br>
 * subtypeValue<br>
 * @author zong0jie
 *
 */
public class KGpathScr2Trg {
	
	/**
	 * 保存查找的entry信息
	 */
	private KGentry qkGentry;
	public void setQKGentry(KGentry qkGentry) {
		this.qkGentry=qkGentry;
	}
	public KGentry getQKGentry() {
		return this.qkGentry;
	}
	
	/**
	 * 保存与查找entry相关的entry信息
	 */
	private KGentry skGentry;
	public void setSKGentry(KGentry skGentry) {
		this.skGentry=skGentry;
	}
	public KGentry getSKGentry() {
		return this.skGentry;
	}
	
	/**
	 * 两个entry互做的类型<br>
	 * <b>两个相同的entry只有一个type</b><br>
	 * <b>component</b>当为component时<br>
	 * 当之间的关系为 relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink 暂时先不考虑</b> 	link to another map				
	 */
	private String type;
	/**
	 * 两个entry互做的类型<br>
	 * <b>两个相同的entry只有一个type</b><br>
	 * <b>component</b>当为component时<br>
	 * 当之间的关系为 relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink 暂时先不考虑</b> 	link to another map				
	 */
	public String getType(){
		return this.type;
	}
	
	/**
	 * 两个entry互做的类型<br>
	 * <b>两个相同的entry只有一个type</b><br>
	 * <b>component</b>当为component时<br>
	 * 当之间的关系为 relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink 暂时先不考虑</b> 	link to another map				
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 保存这两个entry之间的subtype关系<br>
	 * 因为两个entry之间有不止一个关系，如同时有binding和activation之类的，那么它们之间用//分割<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	String[] subtypeInfo = new String[2];
	/**
	 * 保存这两个entry之间的subtype关系<br>
	 * 因为两个entry之间有不止一个关系，如同时有binding和activation之类的，那么它们之间用//分割<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	public void setSubtypeInfo(String[] subtypeInfo) {
		this.subtypeInfo = subtypeInfo;
	}
	/**
	 * 保存这两个entry之间的subtype关系<br>
	 * 因为两个entry之间有不止一个关系，如同时有binding和activation之类的，那么它们之间用//分割<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	public String[] getSubtypeInfo() {
		return this.subtypeInfo ;
	}
	
	/**
	 * 保存这个关系所在的pathway，如果这个关系处在两个不同的pathway,那么用"//"分割
	 */
	String pathName;
	/**
	 * 保存这个关系所在的pathway，如果这个关系处在两个不同的pathway,那么用"//"分割
	 */
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}
	/**
	 * 保存这个关系所在的pathway，如果这个关系处在两个不同的pathway,那么用"//"分割
	 */
	public String getPathName() {
		return this.pathName ;
	}
	
	
}
