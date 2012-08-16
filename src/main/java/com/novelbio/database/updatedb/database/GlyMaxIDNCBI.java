package com.novelbio.database.updatedb.database;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGenePlant;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 用id对应的方法，将gm的ID对应到NCBI上去
 * @author zong0jie
 */
public class GlyMaxIDNCBI {

	public static void main(String[] args) {
		String path = "/media/winE/Bioinformatics/GenomeData/soybean/";
		String gbsFilePath = path + "ncbi/chrgbs";
		String gffFile = path + "/Gmax_109_gene_exons2.gff3";
		String txtOut = path + "dbxref";
		GlyMaxIDNCBI gmidncbi = new GlyMaxIDNCBI();
		gmidncbi.readNCBI(gbsFilePath);
		gmidncbi.readGFFFile(gffFile, txtOut);
	}
	/**
	 * 给定大豆的GBS文件名，
	 * @param gbsFilePath
	 */
	private void readNCBI(String gbsFilePath) {
		ArrayList<String[]> lsFileName = FileOperate.getFoldFileName(gbsFilePath, "chr\\w+", "gbs");
		for (String[] strings : lsFileName) {
			try {
				readNCBItxt(gbsFilePath + "/" + strings[0] + "." + strings[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	private void readNCBItxt(String gbsFile) throws Exception
	{
		String chrID = FileOperate.getFileNameSep(gbsFile)[0].toLowerCase();
		ArrayList<ListDetailBin> lsGffGene = new ArrayList<ListDetailBin>();
		hashLsGffGene.put(chrID, lsGffGene);
		TxtReadandWrite txtGBS = new TxtReadandWrite(gbsFile, false);
		BufferedReader reader = txtGBS.readfile();
		String content = ""; String tmpGene = "";
		while ((content = reader.readLine()) != null) {
			content = content.trim();
			if (content.startsWith("gene     ")) {
				tmpGene = content;
				while (!(content = reader.readLine()).trim().startsWith("/db_xref")) {
					tmpGene = tmpGene + "\r\n" + content.trim();
				}
				tmpGene = tmpGene + "\r\n" + content.trim();
				addGffGene(tmpGene, chrID, lsGffGene);
			}
		}
	}
	
	HashMap<String, ArrayList<ListDetailBin>> hashLsGffGene = new HashMap<String, ArrayList<ListDetailBin>>();
	String sep = "@@";
	private void addGffGene(String txtIn, String chrID, ArrayList<ListDetailBin> lsGffGene)
	{
		String locString = "";
		String[] tmp = txtIn.split("\r\n");
		String locationTmp = tmp[0].replace("gene", "").replace("complement(", "").replace(")", "").replace(">", "").replace("<", "").trim();
		String[] location = locationTmp.split("\\.\\.");
		int start = Integer.parseInt(location[0]);
		int end = Integer.parseInt(location[1]);
		for (String string : tmp) {
			if (string.trim().startsWith("/gene")) {
				locString = string.trim().split("=")[1].replace("\"", "");
			}
			else if (string.trim().startsWith("/db_xref")) {
				locString = locString + sep + string.trim().split("=")[1].replace("\"", "").replace("GeneID:", "");
				if (!string.contains("GeneID")) {
					System.out.println("出现未知ID："+string);
				}
			}
		}
		ListDetailBin gffDetailGene = new ListDetailBin(chrID, locString, true);
		gffDetailGene.setStartCis(start);
		gffDetailGene.setEndCis(end);
		lsGffGene.add(gffDetailGene);
	}
	
	public void readGFFFile(String gffFile, String txtOut) {
		TxtReadandWrite txtOutGene = new TxtReadandWrite(txtOut, true);
		GffHashGenePlant gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_PLANT);
		gffHashGene.ReadGffarray(gffFile);
		for (ArrayList<ListDetailBin> lsGffGene : hashLsGffGene.values()) {
			for (ListDetailBin gffDetailGene : lsGffGene) {
				GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(gffDetailGene.getParentName(), (int)gffDetailGene.getStartAbs(), (int)gffDetailGene.getEndAbs());
				ArrayList<GffDetailGene> lsOverlapGffGene = gffCodGeneDU.getAllGffDetail();
				if (lsOverlapGffGene.size() > 3) {
					lsOverlapGffGene = gffCodGeneDU.getLsGffDetailMid();
				}
				for (GffDetailGene gffDetailGene2 : lsOverlapGffGene) {
					txtOutGene.writefileln(gffDetailGene2.getName() + "\t" + gffDetailGene.getName().get(0) + "\t" + gffDetailGene.getName().get(1));
				}
			}
		}
	}
}
