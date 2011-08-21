package com.novelbio.database.updatedb;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.updatedb.database.UpDateNBCDBFile;
import com.novelbio.database.updatedb.goextract.AffyChipGO;
import com.novelbio.database.updatedb.idconvert.AffyIDmodify;
import com.novelbio.database.updatedb.idconvert.GeneInfoTaxIDgetSymbol;
import com.novelbio.database.updatedb.idconvert.NCBIIDOperate;
import com.novelbio.database.updatedb.idconvert.RiceID;
import com.novelbio.database.updatedb.idconvert.UniProtConvertID;

/**
 * 自动化升级数据库
 * @author zong0jie
 *
 */
public class RunUpDateDB {
	/**
	 * update log
	 */
   static Logger logupdate  =  Logger.getLogger(RunUpDateDB. class );
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		upDate();
	}

	/**
	 * agilent没有导入，也是需要1.导入数据表 2.blast并导入
	 */
	public static void upDate() {
		try {
//			/////////////////////////////////////////////////////////////////////////////////////////////////////////
			String fold = "/media/winE/Bioinformatics/UpDateDB/";
			String taxIDFile = fold + "常见物种IDKEGG.txt";
			String pathGene2accessionID = fold + "gene2accession";
			String outGen2AccTaxID = fold + "outTaxgene2accession";
			String pathGene2enseb = fold + "gene2ensembl";
			String outGen2EnsembTaxID = fold + "outTaxgene2ensembl";
			String pathGen2Refseq = fold + "gene2refseq";
			String outGen2RefStat = fold + "outTaxgene2refseq";
			String outGen2RefID = fold + "outTaxGen2RefID";
			copeNCBIID(taxIDFile, pathGene2accessionID, outGen2AccTaxID, 
					pathGene2enseb, outGen2EnsembTaxID, pathGen2Refseq, outGen2RefStat, outGen2RefID);
			System.out.println("copeNCBIID OK");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String uniIDmapSelect = fold + "idmapping_selected.tab";
			String taxuniIDmapSelect = fold + "taxuniIDmapSelect";
			String outUniIDmapSelectNCBIID = fold + "outUniIDmapSelectNCBIID";
			String outUniIDmapSelectUniID =  fold + "outUniIDmapSelectUniID";
		copeUniID(taxIDFile, uniIDmapSelect, taxuniIDmapSelect, outUniIDmapSelectNCBIID, outUniIDmapSelectUniID);
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String Ref2Uni = fold + "gene_refseq_uniprotkb_collab.txt";
			String outUniIDmapSelectUniIDnone = fold + "outUniIDmapSelectUniIDnone";//先用outUniIDmapSelectUniID查找NCBIID，没找到的写入该文本
			upDateNCBIID(taxIDFile, outGen2AccTaxID, outGen2EnsembTaxID, outUniIDmapSelectNCBIID, Ref2Uni, outUniIDmapSelectUniID, outUniIDmapSelectUniIDnone);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String geneInfoFile = fold + "gene_info";
			String taxGeneInfoFile = fold + "taxGeneInfoFile";
			upDateNCBIIDSymbol(taxIDFile, geneInfoFile, taxGeneInfoFile);
			upDateUniID(outUniIDmapSelectUniIDnone);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String gene_associationgoa_unipro = fold + "gene_association.goa_uniprot";
			String taxgene_associationgoa_unipro = fold + "taxgene_associationgoa_unipro";
			upDateUniIDgene_associationgoa_uniprot(taxIDFile, gene_associationgoa_unipro, taxgene_associationgoa_unipro, taxGeneInfoFile);
			UpDateNBCDBFile.upDateGeneInfo(taxGeneInfoFile);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String affyFold = "/media/winE/Bioinformatics/Affymetrix/";
			String AffyFileHuman = affyFold + "Human/Human Genome U133 Plus 2.0/HG-U133_Plus_2.na31.annot.csv/HG-U133_Plus_2.na31.annotModify.xls";
//			upDateAffy(9606, AffyFileHuman);
			String AffyFileMouse = affyFold + "Mouse/Mouse Genome 430 2.0 Array/Mouse430_2.na31.annot.csvTT/Mouse430_2.na31.annotModify.xls";
//			upDateAffy(10090, AffyFileMouse);
			String AffyFileCow = affyFold + "Bovine/Bovine.na31.annot.csv/Bovine.na31.annotModify.xls";
//			upDateAffy(9913, AffyFileCow);
			String AffyFilePig = affyFold + "Pig Porcine/Porcine.na31.annot.csv/Porcine.na31.annotModify.xls";
//			upDateAffy(9823, AffyFilePig);
			String AffyFileRice = affyFold + "rice/Rice.na31.annot.csv/Rice.na31.annotModify.xls";
//			upDateAffy(39947, AffyFileRice);
			String AffyFileArabdopsis = "Arabidopsis/ATH1-121501.na31.annot.csv/ATH1-121501.na31.annotModify.xls";
//			upDateAffy(3702, AffyFileArabdopsis);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//将blast的ncbiid导入数据库
			//有问题
			String blastAffyPig = "/media/winE/Bioinformatics/BLAST/result/pig/susAff2PigNTNCBIID";
//			UpDateNBCDBFile.upDateNCBIIDBlast(blastAffyPig);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String goterm = fold + "GO.terms_alt_ids";
			String gene2GoFile = fold + "gene2go";
//			upDateGen2Go(goterm, taxIDFile, gene2GoFile, taxgene_associationgoa_unipro, taxuniIDmapSelect);
//			
//			upDateAffyGo(AffyFileHuman, NovelBioConst.DBINFO_AFFY_HUMAN_U133_PLUS2, 9606);
//			upDateAffyGo(AffyFileMouse, NovelBioConst.DBINFO_AFFY_MOUSE_430_2, 10090);
//			upDateAffyGo(AffyFileCow, NovelBioConst.DBINFO_AFFY_COW, 9913);
//			upDateAffyGo(AffyFilePig, NovelBioConst.DBINFO_AFFY_PIG, 9823);
//			upDateAffyGo(AffyFileRice, NovelBioConst.DBINFO_AFFY_RICE_31, 39947);
//			upDateAffyGo(AffyFileArabdopsis, NovelBioConst.DBINFO_AFFY_ATH, 3702);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			
			
			String Rap2MSUFile ="/media/winE/Bioinformatics/GenomeData/Rice/RapDB/RAP-MSU.txt";
			String affyidtolocid ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/affyidtolocidnew.txt";
			String gffTigrRice ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.gff3Cope";
			String tigrGoSlim ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.GOSlim_assignment";
//			upDateRice(NovelBioConst.GENOME_PATH_RICE_RAPDB_GFF_GENE, Rap2MSUFile, affyidtolocid, gffTigrRice, tigrGoSlim);
			
			
			String agilentBta1 = "/media/winE/Bioinformatics/Agilent/caw/AgilentBta015354.txt";
//			UpDateNBCDBFile.upDateNCBIIDBlast(agilentBta1);
			
			String agilentBta2 = "/media/winE/Bioinformatics/Agilent/caw/AgilentBta023647.txt";
//			UpDateNBCDBFile.upDateNCBIIDBlast(agilentBta2);
			
			String blastFile1 = "/media/winE/Bioinformatics/BLAST/result/cow/cope/btaAgilent2Refseq061130";
			String blastFile2 = "/media/winE/Bioinformatics/BLAST/result/cow/cope/btaAgilent2Refseq0904";
//			UpDateNBCDBFile.upDateNCBIIDBlast(blastFile1);
//			UpDateNBCDBFile.upDateNCBIIDBlast(blastFile2);
			
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理NCBI的相关文件
	 * @throws  
	 */
	public static void copeNCBIID(String taxIDFile, 
			String pathGene2accessionID, String outGen2AccTaxID,
			String pathGene2enseb, String outGen2EnsembTaxID,
			String pathGen2Refseq,String outGen2RefStat,String outGen2RefID
	
	) {
		String method = "RunUpDateDB.copeNCBIID";
		//NCBIID处理
		String parentNCBIFile = FileOperate.getParentPathName(pathGene2accessionID);
	

			String modGen2Acc = parentNCBIFile+ "/gene2accIDmodify";
			try {
				NCBIIDOperate.gene2acID(pathGene2accessionID, modGen2Acc);
				System.out.println(modGen2Acc+" ok");
			} catch (Exception e) {
				logupdate.fatal(method+".gene2acID error:"+ pathGene2accessionID);
			}
			try {
				NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2Acc, outGen2AccTaxID);
				System.out.println(outGen2AccTaxID+" ok");
			} catch (Exception e) {
				logupdate.fatal(method+".tableGetTaxID error:"+ taxIDFile);
			}
			
			String modGen2Ens = parentNCBIFile+ "/gene2ensembmodify";
			try {
				NCBIIDOperate.gene2enseb(pathGene2enseb, modGen2Ens);
				System.out.println(modGen2Ens+" ok");
			} catch (Exception e) {
				logupdate.fatal(method + ".gene2enseb error:"+ pathGene2enseb);
			}
			try {
				NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2Ens, outGen2EnsembTaxID);
				System.out.println(outGen2EnsembTaxID+" ok");
			} catch (Exception e) {
				logupdate.fatal(method + ".tableGetTaxID error:"+ taxIDFile);
			}
			
			String modGen2RefStat = parentNCBIFile+ "/gene2RefseqStatmodify";
			String modGen2RefID = parentNCBIFile+ "/gene2RefIDmodify";
			try {
				NCBIIDOperate.gene2ref(pathGen2Refseq, modGen2RefStat,modGen2RefID);
				System.out.println(modGen2RefStat+" ok");
			} catch (Exception e) {
				logupdate.fatal(method + ".gene2ref error:"+ pathGen2Refseq);
			}
			try {
				NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2RefStat, outGen2RefStat);
				System.out.println(modGen2RefStat+" ok");
			} catch (Exception e) {
				logupdate.fatal(method + ".tableGetTaxID error:"+ taxIDFile);
			}
			try {
				NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2RefID, outGen2RefID);
				System.out.println(outGen2RefID+" ok");
			} catch (Exception e) {
				logupdate.fatal(method + ".tableGetTaxID error:"+ taxIDFile);
			}		
		
	}
	/**
	 * 
	 * 处理UniProt的相关文件
	 * @param taxIDFile 物种文件
	 * @param uniIDmapSelect  idmapping_selected.tab
	 * @param taxuniIDmapSelect 将所需的taxID选出来
	 * @param outUniIDmapSelectNCBIID 能在NCBIID中找到的
	 * @param outUniIDmapSelectUniID 不能在NCBIID中找到的
	 */
	public static void copeUniID(String taxIDFile, 
			String uniIDmapSelect,
			String taxuniIDmapSelect,
			String outUniIDmapSelectNCBIID,String outUniIDmapSelectUniID
			) 
	{
		String method = "RunUpDateDB.copeUniID";
		//NCBIID处理	
			try {
				UniProtConvertID.getUniProtTaxID(taxIDFile, uniIDmapSelect, taxuniIDmapSelect);
				System.out.println(taxuniIDmapSelect+"ok");
			} catch (Exception e) {
				logupdate.fatal(method +".getUniProtTaxID error:" +uniIDmapSelect);
			}
			try {
				UniProtConvertID.uniProtIdMapSelectGeneID(taxuniIDmapSelect, outUniIDmapSelectNCBIID);
				System.out.println(outUniIDmapSelectNCBIID+"ok");
			} catch (Exception e) {
				logupdate.fatal(method +".uniProtIdMapSelectGeneID error:" +taxuniIDmapSelect);
			}
			try {
				UniProtConvertID.uniProtIdMapSelectDUniID(taxuniIDmapSelect, outUniIDmapSelectUniID);
				System.out.println(outUniIDmapSelectUniID+"ok");
			} catch (Exception e) {
				logupdate.fatal(method +".uniProtIdMapSelectDUniID error:" +taxuniIDmapSelect);
			}			
	}
	
	/**
	 * 将NCBIID相关文件导入NCBIID表
	 * @param taxIDFile
	 * @param outGen2AccTaxID NCBI的基础对照表
	 * @param outGen2EnsembTaxID NCBI的Ensembl对照表，这个就要开始替换NCBIID中的DBINFO了
	 * @param outUniIDmapSelectNCBIID UniProt的对照表，继续替换DBINFO
	 * @param Ref2Uni RefSeq和UniID的对照表，将其导入NCBIID
	 * @param outUniIDmapSelectUniID 不含有geneID的UniIDmapSelect表
	 * @param outUniIDmapSelectUniIDnone 不含有geneID的UniIDmapSelect表,并且将里面所有的可以插入NCBIID的都插了一遍
	 */
	public static void upDateNCBIID(String taxIDFile, 
			String outGen2AccTaxID, String outGen2EnsembTaxID,String outUniIDmapSelectNCBIID,
			String Ref2Uni,
			String outUniIDmapSelectUniID,String outUniIDmapSelectUniIDnone
			) 
	{
		//NCBIID处理
		try {
			UpDateNBCDBFile.upDateNCBIID(outGen2AccTaxID,false);
			System.out.println(outGen2AccTaxID+" ok");
			UpDateNBCDBFile.upDateNCBIID(outGen2EnsembTaxID,true);
			System.out.println(outGen2EnsembTaxID+" ok");
			UpDateNBCDBFile.upDateNCBIID(outUniIDmapSelectNCBIID,true);
			System.out.println(outUniIDmapSelectNCBIID+ "ok");
			UpDateNBCDBFile.upDateNCBIIDRef2Uni(Ref2Uni);
			System.out.println(Ref2Uni+" ok");
			UpDateNBCDBFile.upDateUniProtID(outUniIDmapSelectUniID, true, outUniIDmapSelectUniIDnone);
			System.out.println(outUniIDmapSelectUniIDnone + " ok");
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	public static void upDateNCBIIDSymbol(String taxIDFile,String geneInfoFile,String taxGeneInfoFile) throws Exception
	{
		NCBIIDOperate.tableGetTaxID(taxIDFile, geneInfoFile, taxGeneInfoFile);
		String parentNCBIFile = FileOperate.getParentPathName(geneInfoFile);
		String modUniIDmapSelect = parentNCBIFile+ "/modSymbol";
		GeneInfoTaxIDgetSymbol.getSymbol(taxGeneInfoFile, modUniIDmapSelect);
		UpDateNBCDBFile.upDateNCBIID(modUniIDmapSelect, false);
		System.out.println(modUniIDmapSelect+" ok");
	}
	
	public static void upDateUniID(String outUniIDmapSelectUniIDnone) throws Exception {
		UpDateNBCDBFile.upDateUniProtID(outUniIDmapSelectUniIDnone, false, null);
		System.out.println(outUniIDmapSelectUniIDnone+" ok");
	}
	/**
	 * 升级gene_associationgoa_uniprot中的NCBI和uniProtID
	 * 完了后直接升级geneInfo表
	 * @param taxIDFile
	 * @param gene_associationgoa_unipro
	 * @throws Exception
	 */
	public static void upDateUniIDgene_associationgoa_uniprot(String taxIDFile,String gene_associationgoa_unipro,String taxgene_associationgoa_unipro,
			String taxGeneInfoFile
			) throws Exception {
		String fold = FileOperate.getParentPathName(gene_associationgoa_unipro)+"/";
		UpDateNBCDBFile.getUniProtGoInfoTaxIDgene_associationgoa_uniprot(taxIDFile, gene_associationgoa_unipro, taxgene_associationgoa_unipro);
		System.out.println(taxgene_associationgoa_unipro+ " ok");
		String outUniqGene_associationgoa_unipro = fold+ "outUniqGene_associationgoa_unipro";
		ExcelTxtRead.uniq(taxgene_associationgoa_unipro, "\t", 2, outUniqGene_associationgoa_unipro);
		
		String outNCBIID = fold+ "outNCBIID";
		String outGeneInfo = fold+ "outGeneInfo";
		String outUniProtID = fold+ "outUniProtID";
		String outUniGeneInfo = fold+ "outUniGeneInfo";
		String remain = fold+ "remain";
		UpDateNBCDBFile.getUniProtGoInfogene_associationgoa_uniprot(outUniqGene_associationgoa_unipro, 
				outNCBIID, outGeneInfo, outUniProtID, outUniGeneInfo, remain);
		System.out.println(outUniProtID+ " ok");
		UpDateNBCDBFile.upDateNCBIID(outNCBIID, false);
		System.out.println(outNCBIID+ " ok");
		UpDateNBCDBFile.upDateUniProtID(outUniProtID, false, "");
		System.out.println(outUniProtID+ " ok");
		UpDateNBCDBFile.upDateGeneInfo(taxGeneInfoFile);
		System.out.println(taxGeneInfoFile+ " ok");
		UpDateNBCDBFile.upDateGeneInfoUniProtgene_associationgoa_uniprot(outGeneInfo);
		System.out.println(outGeneInfo+ " ok");
		UpDateNBCDBFile.upDateUniGeneInfoUniProtgene_associationgoa_uniprot(outUniGeneInfo);
		System.out.println(outUniGeneInfo+ " ok");
	}
	
	public static void upDateAffy(int taxID, String AffyFile) throws Exception {
		AffyIDmodify.getInfo(taxID, AffyFile, 2);
		System.out.println(AffyFile+" ok");
	}
	
	public static void upDateGen2Go(String goterm,String taxIDfile,String gene2GoFile,String taxgene_associationgoa_unipro,
			String taxuniIDmapSelect) throws Exception {
//		UpDateNBCDBFile.upDateGoTerm(goterm);
		
		String parentNCBIFile = FileOperate.getParentPathName(gene2GoFile);
		String taxGeneInfoFile = parentNCBIFile+ "/taxGene2GoFile";
//		NCBIIDOperate.tableGetTaxID(taxIDfile, gene2GoFile, taxGeneInfoFile);
		System.out.println(taxGeneInfoFile+ " ok");
//		UpDateNBCDBFile.upDateGene2Go(taxGeneInfoFile);
		System.out.println(taxGeneInfoFile+ " ok");
		UpDateNBCDBFile.upDateGene2GoUniProtgene_associationgoa_uniprot(taxgene_associationgoa_unipro);
		System.out.println(taxgene_associationgoa_unipro+ " ok");
		UniProtConvertID.upDateUniGo(taxuniIDmapSelect);
		System.out.println(taxuniIDmapSelect+ " ok");
	}
	public static void upDateAffyGo(String affyFile,String affyDBInfo,int taxID) throws Exception
	{
		String fold = FileOperate.getParentPathName(affyFile)+"/";
		String outputGo = fold +affyDBInfo +"OutputGo";
		String outputUniGo = fold +affyDBInfo +"OutputUniGo";
		AffyChipGO.getInfo(affyFile, 2, outputGo, outputUniGo, affyDBInfo, taxID);
		System.out.println(outputUniGo+ " ok");
		AffyChipGO.upDateGenetoGo(outputGo);
		System.out.println(outputGo+ " ok");
		AffyChipGO.upDateGenetoUniGo(outputUniGo);
		System.out.println(outputUniGo+ " ok");
	}
	public static void upDateRice(
			String gffRapDB,
			String Rap2MSUFile,
			String affyidtolocid,
			String gffTigrRice,
			String tigrGoSlim
	) {
//		String Rap2MSUFile ="/media/winE/Bioinformatics/GenomeData/Rice/RapDB/RAP-MSU.txt";
//		String affyidtolocid ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/affyidtolocidnew.txt";
//		String gffTigrRice ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.gff3Cope";
//		String tigrGoSlim ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.GOSlim_assignment";
		

		String fold = FileOperate.getParentPathName(gffRapDB)+"/";
		String outFile = fold+"out";
		try {
			RiceID.getAffyID2LOC(affyidtolocid, outFile);
			System.out.println(affyidtolocid+"ok");
			RiceID.tigrNCBIID(gffTigrRice, outFile, false);
			System.out.println(gffTigrRice+"ok");
			RiceID.rapDBNCBIID(gffRapDB, outFile, false);
			System.out.println(gffRapDB+"ok");
			RiceID.getRAP2MSU(Rap2MSUFile, outFile);
			System.out.println(Rap2MSUFile+"ok");
			RiceID.rapDBNCBIID(gffRapDB, outFile, false);
			System.out.println(gffRapDB+"ok");
			RiceID.tigrNCBIID(gffTigrRice, outFile, false);
			System.out.println(gffTigrRice+"ok");
			RiceID.getRAP2MSU(Rap2MSUFile, outFile);
			System.out.println(Rap2MSUFile+"ok");
			RiceID.rapDBNCBIID(gffRapDB, outFile, true);
			System.out.println(gffRapDB+"ok");
			RiceID.tigrNCBIID(gffTigrRice, outFile, true);
			System.out.println(gffTigrRice+"ok");
			RiceID.getAffyID2LOC(affyidtolocid, outFile);
			System.out.println(affyidtolocid+"ok");
			RiceID.upDateRapDBGeneInfo(gffRapDB);
			System.out.println(gffRapDB+"ok");
			RiceID.tigrDescription(gffTigrRice);
			System.out.println(gffTigrRice+"ok");
			RiceID.rapDBGO(gffRapDB);
			System.out.println(gffRapDB+"ok");
			RiceID.tigrGO(tigrGoSlim);
			
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}
