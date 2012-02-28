package com.novelbio.analysis.project.ztj;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailCG;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGenePlant;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.Species;

/**
 * 用id对应的方法，将gm的ID对应到NCBI上去
 * @author zong0jie
 */
public class GMIDNCBI {

	public static void main(String[] args) {
		String gbsFilePath = "/home/zong0jie/桌面/大豆/ncbi/chrgbs";
		String gffFile = "/home/zong0jie/桌面/大豆/Gmax_109_gene_exons2.gff3";
		String txtOut = "/home/zong0jie/桌面/大豆/ncbi/dbxref";
		GMIDNCBI gmidncbi = new GMIDNCBI();
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
		ArrayList<GffDetailCG> lsGffGene = new ArrayList<GffDetailCG>();
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
	
	HashMap<String, ArrayList<GffDetailCG>> hashLsGffGene = new HashMap<String, ArrayList<GffDetailCG>>();
	String sep = "@@";
	private void addGffGene(String txtIn, String chrID, ArrayList<GffDetailCG> lsGffGene)
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
		GffDetailCG gffDetailGene = new GffDetailCG(chrID, locString, true);
		gffDetailGene.setStartCis(start);
		gffDetailGene.setEndCis(end);
		lsGffGene.add(gffDetailGene);
	}
	
	public void readGFFFile(String gffFile, String txtOut)
	{
		TxtReadandWrite txtOutGene = new TxtReadandWrite(txtOut, true);
		GffHashGenePlant gffHashGene = new GffHashGenePlant(Species.Gmax);
		gffHashGene.ReadGffarray(gffFile);
		for (ArrayList<GffDetailCG> lsGffGene : hashLsGffGene.values()) {
			for (GffDetailCG gffDetailGene : lsGffGene) {
				GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(gffDetailGene.getParentName(), (int)gffDetailGene.getStartAbs(), (int)gffDetailGene.getEndAbs());
				ArrayList<GffDetailGene> lsOverlapGffGene = gffCodGeneDU.getAllGffDetail();
				if (lsOverlapGffGene.size() > 3) {
					lsOverlapGffGene = gffCodGeneDU.getLsGffDetailMid();
				}
				for (GffDetailGene gffDetailGene2 : lsOverlapGffGene) {
					txtOutGene.writefileln(gffDetailGene2.getName() + "\t" + gffDetailGene.getName().split(sep)[0] + "\t" + gffDetailGene.getName().split(sep)[1]);
				}
			}
		}
	}
}
