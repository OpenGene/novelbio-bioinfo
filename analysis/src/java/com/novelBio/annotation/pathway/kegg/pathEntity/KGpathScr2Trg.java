package com.novelBio.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import entity.kegg.KGentry;
import entity.kegg.KGrelation;

/**
 * ��kegg��entry����relation��󣬻�õĽ�������ڸ�������<br>
 * һ��entry��Ӧһ��relation������һ����Ϣ��<br>
 * ���ౣ�������entry���entry��KGentry��Ϣ<br>
 * relation��ϵ<br>
 * subtypeName<br>
 * subtypeValue<br>
 * @author zong0jie
 *
 */
public class KGpathScr2Trg {
	
	/**
	 * ������ҵ�entry��Ϣ
	 */
	private KGentry qkGentry;
	public void setQKGentry(KGentry qkGentry) {
		this.qkGentry=qkGentry;
	}
	public KGentry getQKGentry() {
		return this.qkGentry;
	}
	
	/**
	 * ���������entry��ص�entry��Ϣ
	 */
	private KGentry skGentry;
	public void setSKGentry(KGentry skGentry) {
		this.skGentry=skGentry;
	}
	public KGentry getSKGentry() {
		return this.skGentry;
	}
	
	/**
	 * ����entry����������<br>
	 * <b>������ͬ��entryֻ��һ��type</b><br>
	 * <b>component</b>��Ϊcomponentʱ<br>
	 * ��֮��Ĺ�ϵΪ relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink ��ʱ�Ȳ�����</b> 	link to another map				
	 */
	private String type;
	/**
	 * ����entry����������<br>
	 * <b>������ͬ��entryֻ��һ��type</b><br>
	 * <b>component</b>��Ϊcomponentʱ<br>
	 * ��֮��Ĺ�ϵΪ relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink ��ʱ�Ȳ�����</b> 	link to another map				
	 */
	public String getType(){
		return this.type;
	}
	
	/**
	 * ����entry����������<br>
	 * <b>������ͬ��entryֻ��һ��type</b><br>
	 * <b>component</b>��Ϊcomponentʱ<br>
	 * ��֮��Ĺ�ϵΪ relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink ��ʱ�Ȳ�����</b> 	link to another map				
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * ����������entry֮���subtype��ϵ<br>
	 * ��Ϊ����entry֮���в�ֹһ����ϵ����ͬʱ��binding��activation֮��ģ���ô����֮����//�ָ�<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	String[] subtypeInfo = new String[2];
	/**
	 * ����������entry֮���subtype��ϵ<br>
	 * ��Ϊ����entry֮���в�ֹһ����ϵ����ͬʱ��binding��activation֮��ģ���ô����֮����//�ָ�<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	public void setSubtypeInfo(String[] subtypeInfo) {
		this.subtypeInfo = subtypeInfo;
	}
	/**
	 * ����������entry֮���subtype��ϵ<br>
	 * ��Ϊ����entry֮���в�ֹһ����ϵ����ͬʱ��binding��activation֮��ģ���ô����֮����//�ָ�<br>
	 * 0: subtypeName<br>
	 * 1: subtypeValue<br>
	 */
	public String[] getSubtypeInfo() {
		return this.subtypeInfo ;
	}
	
	/**
	 * ���������ϵ���ڵ�pathway����������ϵ����������ͬ��pathway,��ô��"//"�ָ�
	 */
	String pathName;
	/**
	 * ���������ϵ���ڵ�pathway����������ϵ����������ͬ��pathway,��ô��"//"�ָ�
	 */
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}
	/**
	 * ���������ϵ���ڵ�pathway����������ϵ����������ͬ��pathway,��ô��"//"�ָ�
	 */
	public String getPathName() {
		return this.pathName ;
	}
	
	
}
