package entity.kegg;

/**
 * 两个entry可能有多个作用方法，譬如binding和activation同时存在那么这个就出现两次
 * @author zong0jie
 *
 */
public class KGrelation {
	
	/**
	 * 本相互作用的关系必须在指定的pathway下才能有作用
	 */
	private String pathName;
	
	
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	private int entry1;
	
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 */
	private int entry2;
	
	/**
	 * <b>两个相同的entry只有一个type</b>
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	private String type;
	
	/**
	 * 获得两个entry相互作用的类型，<b>两个相同的entry会有多个不同的subtypeName</b>
	 * 具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	private String subtypeName;
	
	/**
	 * 获得两个entry相互作用的类型，<b>两个相同的entry会有多个不同的subtypeValue</b>具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	private String subtypeValue;
	
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	public int getEntry1ID() 
	{
		return this.entry1;
	}
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	public void setEntry1ID(int entry1) 
	{
		this.entry1=entry1;
	}
	
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 */
	public int getEntry2ID() 
	{
		return this.entry2;
	}
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 */
	public void setEntry2ID(int entry2) 
	{
		this.entry2=entry2;
	}
	
	/**
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	public String getType() 
	{
		return this.type;
	}
	/**
	 * already trim()
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	public void setType(String type)
	{
		this.type=type.trim();
	}
	
	/**
	 * 本相互作用的关系必须在指定的pathway下才能有作用
	 */
	public String getPathName() 
	{
		return this.pathName;
	}
	/**
	 * already trim()
	 * 本相互作用的关系必须在指定的pathway下才能有作用
	 */
	public void setPathName(String pathName) 
	{
		this.pathName=pathName.trim();
	}
	
	/**
	 * 获得两个entry相互作用的类型，具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	public String getSubtypeName() 
	{
		return this.subtypeName;
	}
	/**
	 * already trim()
	 * 获得两个entry相互作用的类型，具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	public void setSubtypeName(String subtypeName) 
	{
		this.subtypeName=subtypeName.trim();
	}
	
	/**
	 * 获得两个entry相互作用的类型，具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	public String getSubtypeValue() 
	{
		return this.subtypeValue;
	}
	/**
	 * already trim()
	 * 获得两个entry相互作用的类型，具体见下表：黑体为name，第二行为value<br>
	 * Interaction/relation information <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>inhibition</b> 	--| 		PPrel 		positive and negative effects which may be associated with molecular information below <br>
*      <b>expression</b> 	--> 			GErel 	interactions via DNA binding <br>
*      <b>repression</b> 	--| 			GErel	interactions via DNA binding <br>
*      <b>indirect effect</b> 	..> 		PPrel 	GErel 	indirect effect without molecular details <br> 
*      <b>state change</b> 	... 		PPrel 		state transition <br>
*      <b>binding/association</b> 	--- 		PPrel 		association and dissociation <br>
*      <b>dissociation</b> 	-+- 		PPrel 	association and dissociation <br>
*      <b>missing interaction</b> 	-/- 		PPrel	GErel 	missing interaction due to mutation, etc. <br>
*      <b>phosphorylation</b> 	+p 		PPrel 		molecular events <br>
*      <b>dephosphorylation</b> 	-p 		PPrel 	molecular events <br>
*      <b>glycosylation</b> 	+g 		PPrel 	molecular events <br>
*      <b>ubiquitination</b> 	+u 		PPrel 	molecular events <br>
*      <b>methylation</b> 	+m 		PPrel 	molecular events <br>
	 */
	public void setSubtypeValue(String subtypeValue) 
	{
		this.subtypeValue=subtypeValue.trim();
	}
}
