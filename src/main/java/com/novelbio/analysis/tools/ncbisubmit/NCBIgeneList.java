package com.novelbio.analysis.tools.ncbisubmit;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class NCBIgeneList {
	public static void main(String[] args) {
		NCBIgeneList script = new NCBIgeneList();
		
		script.readNCBIgeneList("/media/winF/NBC/Project/Project_WZF/NC_009443geneAnnotation.txt");
		script.writeOutGeneName("/media/winF/NBC/Project/Project_WZF/geneName.txt");
		script.writeOutSequence("/media/winF/NBC/Project/Project_WZF/geneSeq.txt");
	}
	
	
	String chrID = "NC_009443";
	
	/** 读取获得的genelist */
	ArrayList<Gene> lsGene = new ArrayList<Gene>();

	/** NCBI的基因列表，类似以下:<br>
	 *    gene            1528..2460
                     /locus_tag="SSU98_0002"
         CDS             1528..2460
                     /locus_tag="SSU98_0002"
                     /note="PCNA homolog"
                     /codon_start=1
                     /transl_table=11
                     /product="DNA polymerase sliding clamp subunit"
                     /protein_id="ABP91162.1"
                     /db_xref="GI:145690657"
	 */
	/** 给定NCBI的基因列表，然后获得每个基因的信息并保存 */
	public void readNCBIgeneList(String ncbiGeneListFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(ncbiGeneListFile, false);
		Position position = Position.gene;
		PositionCDS pCds = PositionCDS.note;
		Gene gene = null;
		for (String content : txtRead.readlines(45)) {
			if (content.startsWith("ORIGIN")) {
				break;
			}
			content = content.trim();
			String[] ss = content.split("         ");
			if (ss[0].equals("gene")) {
				gene = new Gene();
				lsGene.add(gene);
				position = Position.gene;
				continue;
			}
			if (ss.length > 1 && !ss[0].trim().equals("")) {
				position = Position.cdsrrnatrna;
				continue;
			}
			if (position == Position.gene) {
				abstractGene(gene, content);
			}
			else {
				if (content.startsWith("/note=")) {
					String note = content.replace("/note=", "").replace("\"", "");
					gene.addNote(note);
					pCds = PositionCDS.note;
				}
				else if (content.startsWith("/product=")) {
					String product= content.replace("/product=", "").replace("\"", "");
					gene.addProduct(product);
					pCds = PositionCDS.prouct;
				}
				else if (content.startsWith("/translation=")) {
					String seq= content.replace("/translation=", "").replace("\"", "");
					gene.addSeq(seq);
					pCds = PositionCDS.sequence;
				}
				else if (!content.startsWith("/")) {
					if (pCds == PositionCDS.note) {
						gene.addNote(content.replace("\"", ""));
					} else if (pCds == PositionCDS.prouct) {
						gene.addProduct(content.replace("\"", ""));
					} else if (pCds == PositionCDS.sequence) {
						gene.addSeq(content.replace("\"", ""));
					}
				}
			}
		}
	}
	
	private void abstractGene(Gene gene, String content) {
		if (content.contains("/locus_tag")) {
			String name = content.replace("/locus_tag=", "").replace("\"", "").trim();
			gene.setGeneName(name);
		} else if (content.contains("/db_xref")) {
			String geneID = content.replace("/db_xref=", "").replace("\"", "").replace("GeneID:", "").trim();
			gene.setGeneID(geneID);
		}
	}
	/**
	 * 输出gene信息
	 * @param outGeneInfoFile
	 */
	public void writeOutGeneName(String outGeneInfoFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outGeneInfoFile, true);
		for (Gene gene : lsGene) {
			txtOut.writefileln(gene.toString());
		}
		txtOut.close();
	}
	/**
	 * 输出序列
	 * @param outFile
	 */
	public void writeOutSequence(String outSequenceFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outSequenceFile, true);
		for (Gene gene : lsGene) {
			String out = gene.toSequence();
			if (!out.equals("")) {
				txtOut.writefileln(gene.toSequence());
			}
		}
		txtOut.close();
	}
	
}
enum Position {
	gene, cdsrrnatrna
}
enum PositionCDS {
	note, prouct, sequence
}

class Gene {
	String geneName;
	String geneID;
	String description = "";
	String note = "";
	String sequence = "";;
	
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setGeneID(String geneID) {
		this.geneID = geneID;
	}
	public void addNote(String note) {
		if (note.equals("")) {
			this.note = note;
		} else {
			this.note = this.note + " " + note;
		}
	}
	public void addProduct(String description) {
		if (description.equals("")) {
			this.description = description;
		} else {
			this.description = this.description + " " + description;
		}
	}
	
	public void addSeq(String sequence) {
		if (sequence.equals("")) {
			this.sequence = sequence;
		} else {
			this.sequence = this.sequence + sequence;
		}
	}
	public String toString() {
		return geneName + "\t" + geneID + "\t" + description + "\t" + note + "\t";
	}
	public String toSequence() {
		if (sequence.equals("")) {
			return "";
		}
		SeqFasta seqFasta = new SeqFasta(geneName, sequence);
		return seqFasta.toStringNRfasta();
	}
}
