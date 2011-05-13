package com.novelbio.database.upDateDB;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.upDateDB.dataBase.UpDateFriceDB;
import com.novelbio.database.upDateDB.dataBase.UpDateNBCDBFile;
import com.novelbio.database.upDateDB.idConvert.GeneInfoTaxIDgetSymbol;
import com.novelbio.database.upDateDB.idConvert.NCBIIDOperate;
import com.novelbio.database.upDateDB.idConvert.UniProtConvertID;

/**
 * 自动化升级数据库
 * @author zong0jie
 *
 */
public class RunUpDateDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	/**
	 * 处理NCBI的相关文件
	 */
	public void copeNCBIID(String taxIDFile, 
			String pathGene2accessionID, String outGen2AccTaxID,
			String pathGene2enseb, String outGen2EnsembTaxID,
			String pathGen2Refseq,String outGen2RefStat,String outGen2RefID
	
	) {
	
		//NCBIID处理
		String parentNCBIFile = FileOperate.getParentName(pathGene2accessionID);
	
		try {
			String modGen2Acc = parentNCBIFile+ "/gene2accIDmodify";
			NCBIIDOperate.gene2acID(pathGene2accessionID, modGen2Acc);
			NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2Acc, outGen2AccTaxID);
			
			String modGen2Ens = parentNCBIFile+ "/gene2ensembmodify";
			NCBIIDOperate.gene2enseb(pathGene2enseb, modGen2Ens);
			NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2Ens, outGen2EnsembTaxID);
			
			String modGen2RefStat = parentNCBIFile+ "/gene2RefseqStatmodify";
			String modGen2RefID = parentNCBIFile+ "/gene2RefIDmodify";
			NCBIIDOperate.gene2ref(pathGen2Refseq, modGen2RefStat,modGen2RefID);
			NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2RefStat, outGen2RefStat);
			NCBIIDOperate.tableGetTaxID(taxIDFile, modGen2RefID, outGen2RefID);
			
		} catch (Exception e) {	e.printStackTrace();}
		
		
	}
	/**
	 * 处理UniProt的相关文件
	 * @param taxIDFile
	 * @param uniIDmapSelect
	 * @param outUniIDmapSelect
	 */
	public void copeUniID(String taxIDFile, 
			String uniIDmapSelect, String outUniIDmapSelectNCBIID,String outUniIDmapSelectUniID
			) 
	{
		//NCBIID处理
		String parentNCBIFile = FileOperate.getParentName(uniIDmapSelect);
	
		try {
			String modUniIDmapSelect = parentNCBIFile+ "/modUniIDmapSelect";
			UniProtConvertID.getUniProtTaxID(taxIDFile, uniIDmapSelect, modUniIDmapSelect);
			UniProtConvertID.uniProtIdMapSelectGeneID(modUniIDmapSelect, outUniIDmapSelectNCBIID);
			UniProtConvertID.uniProtIdMapSelectDUniID(modUniIDmapSelect, outUniIDmapSelectUniID);
			
			
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	/**
	 * 将NCBIID相关文件导入NCBIID表
	 * @param taxIDFile
	 * @param outGen2AccTaxID NCBI的基础对照表
	 * @param outGen2EnsembTaxID NCBI的Ensembl对照表，这个就要开始替换NCBIID中的DBINFO了
	 * @param outUniIDmapSelect UniProt的对照表，继续替换DBINFO
	 * @param Ref2Uni RefSeq和UniID的对照表，将其导入NCBIID
	 * @param outUniIDmapSelectUniID 不含有geneID的UniIDmapSelect表
	 * @param outUniIDmapSelectUniIDnone 不含有geneID的UniIDmapSelect表,并且将里面所有的可以插入NCBIID的都插了一遍
	 */
	public void upDateNCBIID(String taxIDFile, 
			String outGen2AccTaxID, String outGen2EnsembTaxID,String outUniIDmapSelect,
			String Ref2Uni,
			String outUniIDmapSelectUniID,String outUniIDmapSelectUniIDnone
			) 
	{
		//NCBIID处理
		try {
			UpDateNBCDBFile.upDateNCBIID(outGen2AccTaxID,false);
			UpDateNBCDBFile.upDateNCBIID(outGen2EnsembTaxID,true);
			UpDateNBCDBFile.upDateNCBIID(outUniIDmapSelect,true);
			UpDateNBCDBFile.upDateNCBIIDRef2Uni(Ref2Uni);
			UpDateNBCDBFile.upDateUniProtID(outUniIDmapSelectUniID, true, outUniIDmapSelectUniIDnone);
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	public void upDateNCBIIDSymbol(String taxIDfile,String geneInfoFile,String taxGeneInfoFile) throws Exception
	{
		NCBIIDOperate.tableGetTaxID(taxIDfile, geneInfoFile, taxGeneInfoFile);
		String parentNCBIFile = FileOperate.getParentName(geneInfoFile);
		String modUniIDmapSelect = parentNCBIFile+ "/modSymbol";
		GeneInfoTaxIDgetSymbol.getSymbol(taxGeneInfoFile, modUniIDmapSelect);
		UpDateNBCDBFile.upDateNCBIID(modUniIDmapSelect, false);
	}
	
	public void upDateGenInfo(String taxGeneInfoFile) throws Exception 
	{
		UpDateNBCDBFile.upDateGeneInfo(taxGeneInfoFile);
	}
	
	public void upDateGen2Go(String taxIDfile,String gene2GoFile) throws Exception {
		String parentNCBIFile = FileOperate.getParentName(gene2GoFile);
		String taxGeneInfoFile = parentNCBIFile+ "/taxGene2GoFile";
		NCBIIDOperate.tableGetTaxID(taxIDfile, gene2GoFile, taxGeneInfoFile);
		UpDateNBCDBFile.upDateGene2Go(taxGeneInfoFile);
	}
}
