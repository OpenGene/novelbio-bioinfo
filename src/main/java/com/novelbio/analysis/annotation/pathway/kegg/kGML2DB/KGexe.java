package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;


public class KGexe {
	private static final Logger logger = Logger.getLogger(KGexe.class);
	/**
	 * @param args
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main2(String[] args) throws InterruptedException, ExecutionException {
		Options opts = new Options();
		opts.addOption("keggabbr", true, "keggabbr");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.exit(1);
		}
		String keggAbbrStr = cliParser.getOptionValue("keggabbr");
		try {
			for (String abbr : keggAbbrStr.split(",")) {
				abbr = abbr.trim();
				KGML2DB.readKGML("/home/novelbio/NBCsource/biodb/database20150530/kegg/" + abbr);
				KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/biodb/database20150530/kegg/genes_ncbi-geneid.list.gz", abbr);
				logger.info("finish kegg2geneId");
				KeggIDcvt.upDateKeg2Ko("/home/novelbio/NBCsource/biodb/database20150530/kegg/genes_ko.list.gz", abbr);
				logger.info("finish kegg2ko");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
//		List<String> lsGenID2 = new ArrayList<>();
//		lsGenID2.add("XNR_0641");
//		CtrlBlastPath ctrlPath = new CtrlBlastPath(false, 457425, 0, 1e-10);
//		ctrlPath.prepare(lsGenID2);
//		ctrlPath.execute();
//		List<String[]> lsAnno = ctrlPath.get();
//		GeneID geneID = new GeneID("XNR_0641", 457425);
//		List<KGpathway> lsPathway = geneID.getKeggInfo().getLsKegPath();
//		for (KGpathway kGpathway : lsPathway) {
//			System.out.println(kGpathway.getTitle());
//		}
//		System.out.println(ArrayOperate.cmbString(lsAnno.get(0), "\t"));
//		Options opts = new Options();
//		opts.addOption("keggabbr", true, "keggabbr");
//		CommandLine cliParser = null;
//		try {
//			cliParser = new GnuParser().parse(opts, args);
//		} catch (Exception e) {
//			System.exit(1);
//		}
//		String keggAbbrStr = cliParser.getOptionValue("keggabbr");
		String keggAbbrStr = "ser";
		try {
			for (String abbr : keggAbbrStr.split(",")) {
				abbr = abbr.trim();
				KGML2DB.readKGML("/home/novelbio/NBCsource/biodb/database/kegg/" + abbr);
				KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/biodb/database/kegg/genes_ncbi-geneid.list.gz", abbr);
				logger.info("finish kegg2geneId");
				KeggIDcvt.upDateKeg2Ko("/home/novelbio/NBCsource/biodb/database/kegg/genes_ko.list.gz", abbr);
				logger.info("finish kegg2ko");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 给定accID，查询该accID所对应的pathway
	 * 目前只能在NCBIID中查询，不能在UniProt中查询
	 * @param accID accID 需要去空格处理以及判断accID是否为空
	 * @param taxID
	 * @param blast
	 * @param subTaxID
	 * @param evalue
	 * @return 如果没查到则返回null
	 * 如果blast：
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 * 4:evalue
	 * 5:SubjectSymbol
	 * 6:PathID
	 * 7:PathName
	 * 如果没有blast
	 * 0:accID
	 * 1:Symbol/AccID
	 * 2:PathID
	 * 3:PathName
	 */
	public static ArrayList<String[]> getGenPath(String accID,int taxID,boolean blast,int subTaxID,double evalue) {
		String[] tmpAccIDInfo = new String[] { accID, ""};
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		GeneID copedID = new GeneID(accID, taxID);
//		if (copedID.getIDtype() == GeneID.IDTYPE_ACCID) {
//			return null;
//		}
		tmpAccIDInfo[1] = copedID.getSymbol();
		copedID.setBlastInfo(evalue, subTaxID);
		// 本基因的Path信息
		ArrayList<KGpathway> lsKGentrythis = copedID.getKegPath(blast);
		HashSet<String> hashPathID = new HashSet<String>();
		for (KGpathway kGpathway : lsKGentrythis) {
			String[] tmpResult = ArrayOperate.copyArray(tmpAccIDInfo, 4);
			if (hashPathID.contains(kGpathway.getMapNum())) {
				continue;
			}
			tmpResult[2] = "PATH:" + kGpathway.getMapNum();
			tmpResult[3] = kGpathway.getTitle();
			lsResult.add(tmpResult);
		}
		if (!blast) {
			if (lsResult.size() == 0) {
				return null;
			}
			return lsResult;
		}
		// blast基因的GO信息
		ArrayList<String[]> lsResultBlast = new ArrayList<String[]>();

		GeneID copedIDblast = copedID.getGeneIDBlast();
		if (copedIDblast == null) {
			for (String[] strings : lsResult) {
				String[] result = ArrayOperate.copyArray(strings, 8);
				lsResultBlast.add(result);
			}
			return lsResultBlast;
		}
		HashSet<String> hashPathIDBlast = new HashSet<String>();
		List<KGentry> lsPathBlast = copedIDblast.getKegEntity(false);
		int k = 0;
		for (int i = 0; i < lsPathBlast.size(); i++) {
			if (hashPathIDBlast.contains(lsPathBlast.get(i).getPathName())) {
				continue;
			}
			if (lsResult == null)
				lsResult = new ArrayList<String[]>();
			String[] tmpResultBlast = new String[8];
			// 初始化
			for (int j = 0; j < tmpResultBlast.length; j++) {
				tmpResultBlast[j] = "";
			}
			if (k < lsResult.size()) {
				for (int j = 0; j < lsResult.get(k).length; j++) {
					tmpResultBlast[j] = lsResult.get(k)[j];
				}
			} else {
				tmpResultBlast[0] = accID;
				tmpResultBlast[1] = copedID.getSymbol();
			}
			tmpResultBlast[4] = copedID.getLsBlastInfos().get(0).getEvalue() + "";
			tmpResultBlast[5] = copedIDblast.getSymbol();
			tmpResultBlast[6] = lsPathBlast.get(i).getPathName();
			tmpResultBlast[7] = lsPathBlast.get(i).getPathTitle();
			lsResultBlast.add(tmpResultBlast);
			k ++;
		}

		return lsResultBlast;
	}

}
