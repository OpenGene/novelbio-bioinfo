package com.novelbio.database.updatedb.database;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffHashGenePlant;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.bioinfo.gff.ListDetailBin;

/**
 * 用id对应的方法，将gm的ID对应到NCBI上去
 * @author zong0jie
 */
public class GlyMaxIDNCBI {
	HashMap<String, ArrayList<ListDetailBin>> hashLsGffGene = new HashMap<String, ArrayList<ListDetailBin>>();
			
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
		ArrayList<String> lsFileName = FileOperate.getLsFoldFileName(gbsFilePath, "chr\\w+", "gbs");
		for (String fileName : lsFileName) {
			try {
				readNCBItxt(fileName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void readNCBItxt(String gbsFile) throws Exception {
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
		txtGBS.close();
	}

	private void addGffGene(String txtIn, String chrID, ArrayList<ListDetailBin> lsGffGene) {
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
				locString = locString + SepSign.SEP_ID + string.trim().split("=")[1].replace("\"", "").replace("GeneID:", "");
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
		GffHashGene gffHashGene = new GffHashGene(gffFile);
		for (ArrayList<ListDetailBin> lsGffGene : hashLsGffGene.values()) {
			for (ListDetailBin gffDetailGene : lsGffGene) {
				GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(gffDetailGene.getRefID(), (int)gffDetailGene.getStartAbs(), (int)gffDetailGene.getEndAbs());
				List<GffGene> lsOverlapGffGene = gffCodGeneDU.getAllGffDetail();
				if (lsOverlapGffGene.size() > 3) {
					lsOverlapGffGene = gffCodGeneDU.getLsGffDetailMid();
				}
				for (GffGene gffDetailGene2 : lsOverlapGffGene) {
					txtOutGene.writefileln(gffDetailGene2.getName() + "\t" + gffDetailGene.getNameSingle() + "\t" + gffDetailGene.getName().get(1));
				}
			}
		}
		txtOutGene.close();
	}
}
