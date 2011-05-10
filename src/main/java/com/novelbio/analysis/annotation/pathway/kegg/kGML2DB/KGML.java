package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.util.ArrayList;
import java.util.HashMap;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Validate;

@Root(name="pathway")
public class KGML {
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	@Attribute(name="name") 
	private String pathName;
	
	
	/**
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	@Attribute(name="org")
	private String species;
	
	/**
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	@Attribute(name="number")
	private String mapNum;
	
	/**
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	@Attribute(name="title",required=false)
	private String title;
	
	/**
	 * the resource location of the image file of this pathway map.  example:<br>
	 * <b>URL</b>  ex) image="http://www.genome.jp/kegg/pathway/ko/ko00010.png"
	 */
	@Attribute(name="image",required=false)
	private String imageUrl;
	
	/**
	 * the resource location of the information about this pathway map.  example:<br>
	 * <b>URL</b>  ex) link="http://www.genome.jp/kegg-bin/show_pathway?ko00010"
	 */
	@Attribute(name="link",required=false)
	private String linkUrl;
	
	/**
	 * ebtryList
	 */
	@ElementList(entry="entry",inline=true,required=false)
	private ArrayList<Entry> lsEntries;
	
	
	/**
	 * relationList
	 */
	@ElementList(entry="relation",inline=true,required=false)
	private ArrayList<Relation> lsRelations;
	
	/**
	 * reactionList
	 */
	@ElementList(entry="reaction",inline=true,required=false)
	private ArrayList<Reaction> lsReactions;
	
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	public String getPathName()
	{
		if (this.pathName==null) {
			return "";
		}
		return this.pathName.trim();
	}
	
	/**
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	public String getSpecies() 
	{
		if (this.species==null) {
			return "";
		}
		return this.species.trim();
	}
	/**
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	public String getMapNum() 
	{
		if (this.mapNum==null) {
			return "";
		}
		return this.mapNum.trim();
	}
	
	/**
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	public String getTitle() 
	{
		if (this.title==null) {
			return "";
		}
		return this.title.trim();
	}
	
	/**
	 * the resource location of the information about this pathway map.  example:<br>
	 * <b>URL</b>  ex) link="http://www.genome.jp/kegg-bin/show_pathway?ko00010"
	 */
	public String getLinkUrl() 
	{
		if (this.linkUrl==null) {
			return "";
		}
		return this.linkUrl;
	}

	/**
	 * ebtryList
	 * The entry element contains information about a node of the pathway. 
	 */
	public ArrayList<Entry> getLsEntries() {
		return this.lsEntries;
	}
	/**
	 * The relation element specifies relationship between two proteins (gene products) or two KOs (ortholog groups) or protein and compound, 
	 * which is indicated by an arrow or a line connecting two nodes in the KEGG pathways. 
	 * The relation element has a subelement named the subtype element. 
	 * When the name attribute value of the subtype element is a value with directionality like "activation",
	 *  the direction of the interaction is from entry1 to entry2.
	 * @return
	 */
	public ArrayList<Relation> getLsRelations() 
	{
		return this.lsRelations;
	}
	/**
	 * The reaction element specifies chemical reaction between a substrate and a product indicated by an arrow connecting two circles in the KEGG pathways. 
	 * The reaction element has the substrate element and the product element as subelements.
	 * @return
	 */
	public ArrayList<Reaction> getLsrReactions() {
		return this.lsReactions;
	}
	
   private HashMap<Integer, Entry> hashEntry;

 
   public KGML() {
      this.hashEntry = new HashMap<Integer, Entry>();
   }

   @Validate
   public void validate() throws PersistenceException {
      ArrayList<Integer> lsID = new ArrayList<Integer>();

      for(Entry entry : lsEntries) {
         int id = entry.getID();

         if(lsID.contains(id)) {
            throw new PersistenceException("Duplicate key %s", id);
         }
         lsID.add(id);         
      }
   }

   @Commit
   public void build() {
      for(Entry entry : lsEntries) {
    	  hashEntry.put(entry.getID(), entry); 
      }     
   }

   /**
    * 给定id，返回该id所对应的entry信息
    * @param id
    * @return
    */
   public Entry getEntry(int id) {
      return hashEntry.get(id);
   }
}



@Root(name="entry")
class Entry
{	
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	@Attribute(name="id")
	private int id=-1;
	
	/**
	 * the KEGGID of this entry，用空格分隔
	 * example： name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
	 * detail:<br>
	 * <b>path:(accession)</b>  	pathway map
	 * ex) name="path:map00040"
	 *<b> ko:(accession)</b> 	KO (ortholog group)
	 * ex) name="ko:E3.1.4.11"
	 *<b> ec:(accession)</b> 	enzyme
	 * ex) name="ec:1.1.3.5"
	 * <b>rn:(accession)</b> 	reaction
	 * ex) name="rn:R00120"
	 * <b>cpd:(accession)</b> 	chemical compound
	 * ex) name="cpd:C01243"
	 * <b>gl:(accession)</b> 	glycan
	 * ex) name="gl:G00166"
	 * <b>[org prefix]:(accession)</b> 	gene product of a given organism
	 * ex) name="eco:b1207"
	 * <b>group:(accession)</b> 	complex of KOs
	 * If accession is undefined, "undefined" is specified.
	 * ex) name="group:ORC"
	 */
	@Attribute(name="name")
	private String name;
	
	/**
	 * 当type为map且不为本pathway时，将本pathway和该map组成source--target并且放入KGRelation类中
	 * 当type为group时，将component中涉及到的所有entry两两遍历组成source--target并且放入KGReaction类中
	 * the type of this entry. detail:<br>
	 * <b>ortholog</b> 	the node is a KO (ortholog group)<br>
	 * <b>enzyme</b> 	the node is an enzyme<br>
	 * <b>reaction</b> 	the node is a reaction<br>
	 * <b>gene</b> 	the node is a gene product (mostly a protein)<br>
	 * <b>group</b> 	the node is a complex of gene products (mostly a protein complex)<br>
     *<b>compound</b> 	the node is a chemical compound (including a glycan)<br>
     *<b>map</b> 	the node is a linked pathway map<br>
	 */
	@Attribute(name="type")
	private String type;
	
	
	/**
	 * the resource location of the information about this entry  example:<br>
	 * <b>URL</b> 	ex)link="http://www.genome.jp/dbget-bin/www_bget?eco+b1207"
	 */
	@Attribute(name="link",required=false)
	private String linkEntry;
	
	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	@Attribute(name="reaction",required=false)
	private String reaction;
	
	/**
	 * The graphics element is a subelement of the entry element, specifying drawing information about the graphics object.
	 */
	@ElementList(entry="graphics",required=false,inline=true)
	private ArrayList<Graphics> lsGraphics;

	/**
	 * 如果出现component，也就是type为group时，则将该项放入KGRelation中,并且将type设置为pathway2pathway，component有几项就拆分为几个sorce-target项目
	 * The component element is a subelement of the entry element, 
	 * and is used when the entry element is a complex node; 
	 * namely, when the type attribute value of the entry element is "group". 
	 * The nodes that constitute the complex are specified by recurrent calls. 
	 * For example, when the complex is composed of two nodes, two component elements are specified. 
	 * The attribute of this element is as follows.
	 */
	@ElementList(entry="component",required=false,inline=true)
	private ArrayList<Component> lsComponents;
	/**
	 * the ID of this entry in the pathway map <br>
	 *  the identification number of this entry，从1开始记数
	 */
	public int getID()
	{
		return this.id;
	}
	
	/**
	 * the resource location of the information about this entry  example:<br>
	 * <b>URL</b> 	ex)link="http://www.genome.jp/dbget-bin/www_bget?eco+b1207"
	 */
	public String getLinkEntry()
	{
		if (this.linkEntry==null) {
			return "";
		}
		return this.linkEntry.trim();
	}
	
	/**
	 * the KEGGID of this entry，用空格分隔，所以在装入KGentry类时要将ID全部分开
	 * example： name="sma:SAV_2461 sma:SAV_3026 sma:SAV_3027"<br>
	 * detail:<br>
	 * <b>path:(accession)</b>  	pathway map
	 * ex) name="path:map00040"
	 *<b> ko:(accession)</b> 	KO (ortholog group)
	 * ex) name="ko:E3.1.4.11"
	 *<b> ec:(accession)</b> 	enzyme
	 * ex) name="ec:1.1.3.5"
	 * <b>rn:(accession)</b> 	reaction
	 * ex) name="rn:R00120"
	 * <b>cpd:(accession)</b> 	chemical compound
	 * ex) name="cpd:C01243"
	 * <b>gl:(accession)</b> 	glycan
	 * ex) name="gl:G00166"
	 * <b>[org prefix]:(accession)</b> 	gene product of a given organism
	 * ex) name="eco:b1207"
	 * <b>group:(accession)</b> 	complex of KOs
	 * If accession is undefined, "undefined" is specified.
	 * ex) name="group:ORC"
	 */
	public String getEntryName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
	
	/**
	 * the type of this entry. detail:<br>
	 * <b>ortholog</b> 	the node is a KO (ortholog group)<br>
	 * <b>enzyme</b> 	the node is an enzyme<br>
	 * <b>reaction</b> 	the node is a reaction<br>
	 * <b>gene</b> 	the node is a gene product (mostly a protein)<br>
	 * <b>group</b> 	the node is a complex of gene products (mostly a protein complex)<br>
     *<b>compound</b> 	the node is a chemical compound (including a glycan)<br>
     *<b>map</b> 	the node is a linked pathway map<br>
	 */
	public String getType() {
		if (this.type==null) {
			return "";
		}
		return this.type.trim();
	}
	
	/**
	 * the KEGGID of corresponding reaction.  example:<br>
	 * ex)reaction="rn:R02749"
	 */
	public String getReaction() {
		if (this.reaction==null) {
			return "";
		}
		return this.reaction.trim();
	}
	
	/**
	 * 如果出现component，也就是type为group时，则将该项放入KGReaction中，component有几项就拆分为几个sorce-target项目，也就是entry1--entry2的关系
	 * The component element is a subelement of the entry element, and is used when the entry element is a complex node; 
	 * namely, when the type attribute value of the entry element is "group". The nodes that constitute the complex are specified by recurrent calls.
	 * For example, when the complex is composed of two nodes, two component elements are specified.
	 * @author zong0jie
	 *
	 */
	public ArrayList<Component> getLsComponent() {
		return this.lsComponents;
	}

}

/**
 * 暂时不会去使用的东西
 * @author zong0jie
 *
 */
@Root(name="graphics")
class Graphics
{
	@Attribute(required=false)
	private String name;
	@Attribute(required=false)
	private int x;
	@Attribute(required=false)
	private int y;
	@Attribute(required=false)
	private String coords;
	@Attribute(required=false)
	private String type;
	@Attribute(required=false)
	private int width;
	@Attribute(required=false)
	private int height;
	@Attribute(required=false)
	private String fgcolor;
	@Attribute(required=false)
	private String bgcolor;
}
/**
 * 如果出现component，也就是type为group时，则将该项放入KGReaction中，component有几项就拆分为几个sorce-target项目，也就是entry1--entry2的关系
 * The component element is a subelement of the entry element, and is used when the entry element is a complex node; 
 * namely, when the type attribute value of the entry element is "group". The nodes that constitute the complex are specified by recurrent calls.
 * For example, when the complex is composed of two nodes, two component elements are specified.
 * @author zong0jie
 *
 */
@Root(name="component")
class Component
{
	@Attribute
	private int id;
	
	public int getComID() {
		return this.id;
	}
}

/**
 * The relation element specifies relationship between two proteins (gene products) or two KOs (ortholog groups) or protein and compound, 
 * which is indicated by an arrow or a line connecting two nodes in the KEGG pathways. 
 * The relation element has a subelement named the subtype element. 
 * When the name attribute value of the subtype element is a value with directionality like "activation", 
 * the direction of the interaction is from entry1 to entry2. 
 * @author zong0jie
 *
 */
@Root(name="relation")
class Relation
{
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	@Attribute(name="entry1")
	private int entry1;
	
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 */
	@Attribute(name="entry2")
	private int entry2;
	
	/**
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	@Attribute(name="type")
	private String type;
	
	@ElementList(entry="subtype",inline=true,required=false)
	private ArrayList<Subtype> lssSubtypes;
	
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	public int getEntry1ID() 
	{
		return this.entry1;
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
	 * the type of this relation. detail:<br>
	 * <b> ECrel</b>  	enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps                 <br> 
	 * <b>PPrel</b> 	protein-protein interaction, such as binding and modification							     <br>
	 * <b>GErel</b> 	gene expression interaction, indicating relation of transcription factor and target gene product	     <br>
	 * <b>PCrel</b> 	protein-compound interaction												     <br>
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	public String getType() 
	{
		if (this.type==null) {
			return "";
		}
		return this.type.trim();
	}

	/**
	 * The subtype element specifies more detailed information about the nature of the interaction or the relation. 
	 * @return
	 */
	public ArrayList<Subtype> getLsSubtype() {
		return this.lssSubtypes;
	}
}

/**
 * The subtype element specifies more detailed information about the nature of the interaction or the relation. 
 * @author zong0jie
 *
 */
@Root(name="subtype")
class Subtype
{
	/**
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
	@Attribute(name="name")
	private String name;
	
	/**
	 * Interaction/relation property value <br>
	 *      <b>name</b>  	value  	ECrel  	PPrel  	GErel  	Explanation <br>
*      <b>compound</b> 	Entry element id attribute value for compound. 	ECrel 	PPrel 		shared with two successive reactions (ECrel) or intermediate of two interacting proteins (PPrel) <br>
*      <b>hidden compound</b> 	Entry element id attribute value for hidden compound. 	ECrel 			shared with two successive reactions but not displayed in the pathway map <br>
*      <b>activation</b> 	--> --&gt;		PPrel 		positive and negative effects which may be associated with molecular information below <br>
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
	@Attribute(name="value")
	private String value;
	

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
	public String getName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
	/**
	 * 获得两个entry相互作用的类型，具体见下表：黑体为name，第二行为value，其中value只有在name为compound和hidden compound时才有作用<br>
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
	public String getValue() {
		if (this.value==null) {
			return "";
		}
		return this.value.trim();
	}
	
	
}


@Root(name="reaction")
class Reaction
{
	/**
	 * the ID of this reaction,和Entry的ID是同一个
	 */
	@Attribute(name="id")
	private int id=-1;
	 
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	@Attribute(name="name")
	private String name;
	
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	@Attribute(name="type")
	private String type;
	
	/**
	 * The substrate element specifies the substrate node of this reaction.
	 */
	@ElementList(entry="substrate",required=false,inline=true)
	private ArrayList<Substrate> lsSubstrate;
	
	/**
	 * The product element specifies the product node of this reaction. 
	 */
	@ElementList(entry="product",required=false,inline=true)
	private ArrayList<Product> lsProduct;
	
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	@Element(name="alt",required=false)
	private Alt alt;
	
	/**
	 * the ID of this reaction,和Entry的ID是同一个
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	public String getName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	public String getType() {
		if (this.type==null) {
			return "";
		}
		return this.type.trim();
	}
	/**
	 * The substrate element specifies the substrate node of this reaction.
	 */
	public ArrayList<Substrate> getLsSubstrate() {
		return this.lsSubstrate;
	}
	/**
	 * The product element specifies the product node of this reaction. 
	 */
	public ArrayList<Product> getLsProduct() {
		return this.lsProduct;
	}
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	public Alt getAlt() {
		return this.alt;
	}
}

/**
 * The substrate element specifies the substrate node of this reaction. 
 * @author zong0jie
 *
 */
@Root(name="substrate")
class Substrate
{
	/**
	 * the ID of this substrate
	 * the identification number of this substrate
	 */
	@Attribute(name="id")
	private int id=-1;
	
	/**
	 * KEGGID of substrate node
	 * ex) cpd:C05378   gl:G00037
	 */
	@Attribute(name="name")
	private String name;
	/**
	 * the ID of this substrate
	 * the identification number of this substrate
	 */
	public int getID() {
		return this.id;
	}
	/**
	 * KEGGID of substrate node
	 * ex) cpd:C05378   gl:G00037
	 */
	public String getName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
}

/**
 * The product element specifies the product node of this reaction. 
 * @author zong0jie
 *
 */
@Root(name="product")
class Product
{
	/**
	 * the ID of this product
	 * the identification number of this product
	 */
	@Attribute(name="id")
	private int id=-1;
	
	/**
	 * KEGGID of product node
	 * ex) cpd:C05378   gl:G00037
	 */
	@Attribute(name="name")
	private String name;
	
	/**
	 * the ID of this product
	 * the identification number of this product
	 */
	public int getID() {
		return this.id;
	}
	/**
	 * KEGGID of product node
	 * ex) cpd:C05378   gl:G00037
	 */
	public String getName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
}

/**
 * The alt element specifies the alternative name of its parent element.
 * @author zong0jie
 *
 */
@Root(name="alt")
class Alt
{
	/**
	 * the KEGGID of node
	 * ex) cpd:C05378   gl:G00037
	 */
	@Attribute(name="name")
	private String name;
	
	public String getName() {
		if (this.name==null) {
			return "";
		}
		return this.name.trim();
	}
}










