package com.novelbio.database.domain.kegg;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ����entry�����ж�����÷�����Ʃ��binding��activationͬʱ������ô����ͳ�������
 * @author zong0jie
 *
 */
public class KGrelation {
	
	/**
	 * ���໥���õĹ�ϵ������ָ����pathway�²���������
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
	 * <b>������ͬ��entryֻ��һ��type</b>
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	private String type;
	
	/**
	 * �������entry�໥���õ����ͣ�<b>������ͬ��entry���ж����ͬ��subtypeName</b>
	 * ������±�������Ϊname���ڶ���Ϊvalue<br>
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
	 * �������entry�໥���õ����ͣ�<b>������ͬ��entry���ж����ͬ��subtypeValue</b>������±�������Ϊname���ڶ���Ϊvalue<br>
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
	 * ���໥���õĹ�ϵ������ָ����pathway�²���������
	 */
	public String getPathName() 
	{
		return this.pathName;
	}
	/**
	 * already trim()
	 * ���໥���õĹ�ϵ������ָ����pathway�²���������
	 */
	public void setPathName(String pathName) 
	{
		this.pathName=pathName.trim();
	}
	
	/**
	 * �������entry�໥���õ����ͣ�������±�������Ϊname���ڶ���Ϊvalue<br>
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
	 * �������entry�໥���õ����ͣ�������±�������Ϊname���ڶ���Ϊvalue<br>
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
	 * �������entry�໥���õ����ͣ�������±�������Ϊname���ڶ���Ϊvalue<br>
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
	 * �������entry�໥���õ����ͣ�������±�������Ϊname���ڶ���Ϊvalue<br>
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
	
	public static int FLAG_ENTRYID_UNKNOWN = 0;
	public static int FLAG_ENTRYID1 = 1;
	public static int FLAG_ENTRYID2 = 2;
	int flagID = FLAG_ENTRYID_UNKNOWN;
	public void setFlag(int FLAG_ENTRYID) {
		flagID = FLAG_ENTRYID;
	}
	
	public int getFlag() {
		return flagID;
	}
	
	/**
	 * �ж�����KGrelation�Ƿ���ͬ��ֻ��Ҫ�Ƚ�entry1��entry2����
	 * ��ûд��
	 * @return
	 */
	private boolean equalsSimp(Object obj)
	{
		return false;
	}
	
	
	/**
	 * ��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����
	 * ȥ������󣬽�subtype��//�ָ
	 * @param lsKGrelations
	 * @return
	 */
	public static HashMap<String, KGrelation> removeRep(ArrayList<KGrelation> lsKGrelations) 
	{
		/////////////////////////////////////////��Ϊ��Щrelation�������������entry1��entry2��ȫһ�£�����subtype��һ�£�������ǽ�һ�µ�entry1��entry2ȥ�����////////////////////////////////////////////////////////////////
		HashMap<String, KGrelation> hashPathID2KGRelation = new HashMap<String, KGrelation>();
		for (int i = 0; i < lsKGrelations.size(); i++) 
		{
			KGrelation tmpKGrelation = lsKGrelations.get(i);
			//��ΪentryID1����һ���ģ����Բ�����entryID1
			String key = tmpKGrelation.getPathName() + tmpKGrelation.getEntry2ID(); //pathName��entryID2������һ���Ƕ�һ�޶����ˣ����������string��Ϊkey���Դ���ȥ�ظ�������Ļ�Ҫ��KGrelation������дhash��
			if (hashPathID2KGRelation.containsKey(key)) 
			{
				//��������ظ���entry1��entry2�ԣ� ��ô��subtypeName��subtypeValue�����ں���
				KGrelation kGrelationtmp = hashPathID2KGRelation.get(key);
				if ( kGrelationtmp.getSubtypeName().equals( tmpKGrelation.getSubtypeName() )  ) {
					continue;
				}
				kGrelationtmp.setSubtypeName(kGrelationtmp.getSubtypeName()+ "//" + tmpKGrelation.getSubtypeName());
				kGrelationtmp.setSubtypeValue(kGrelationtmp.getSubtypeValue()+ "//" + tmpKGrelation.getSubtypeValue());
			}
			else 
			{
				hashPathID2KGRelation.put(key, tmpKGrelation);
			}
		}
		return hashPathID2KGRelation;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}