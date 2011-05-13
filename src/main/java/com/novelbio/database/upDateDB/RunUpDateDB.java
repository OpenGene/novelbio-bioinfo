package com.novelbio.database.upDateDB;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.upDateDB.dataBase.UpDateFriceDB;
import com.novelbio.database.upDateDB.dataBase.UpDateNBCDBFile;
import com.novelbio.database.upDateDB.idConvert.GeneInfoTaxIDgetSymbol;
import com.novelbio.database.upDateDB.idConvert.NCBIIDOperate;
import com.novelbio.database.upDateDB.idConvert.UniProtConvertID;

/**
 * �Զ����������ݿ�
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
	 * ����NCBI������ļ�
	 */
	public void copeNCBIID(String taxIDFile, 
			String pathGene2accessionID, String outGen2AccTaxID,
			String pathGene2enseb, String outGen2EnsembTaxID,
			String pathGen2Refseq,String outGen2RefStat,String outGen2RefID
	
	) {
	
		//NCBIID����
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
	 * ����UniProt������ļ�
	 * @param taxIDFile
	 * @param uniIDmapSelect
	 * @param outUniIDmapSelect
	 */
	public void copeUniID(String taxIDFile, 
			String uniIDmapSelect, String outUniIDmapSelectNCBIID,String outUniIDmapSelectUniID
			) 
	{
		//NCBIID����
		String parentNCBIFile = FileOperate.getParentName(uniIDmapSelect);
	
		try {
			String modUniIDmapSelect = parentNCBIFile+ "/modUniIDmapSelect";
			UniProtConvertID.getUniProtTaxID(taxIDFile, uniIDmapSelect, modUniIDmapSelect);
			UniProtConvertID.uniProtIdMapSelectGeneID(modUniIDmapSelect, outUniIDmapSelectNCBIID);
			UniProtConvertID.uniProtIdMapSelectDUniID(modUniIDmapSelect, outUniIDmapSelectUniID);
			
			
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	/**
	 * ��NCBIID����ļ�����NCBIID��
	 * @param taxIDFile
	 * @param outGen2AccTaxID NCBI�Ļ������ձ�
	 * @param outGen2EnsembTaxID NCBI��Ensembl���ձ������Ҫ��ʼ�滻NCBIID�е�DBINFO��
	 * @param outUniIDmapSelect UniProt�Ķ��ձ������滻DBINFO
	 * @param Ref2Uni RefSeq��UniID�Ķ��ձ����䵼��NCBIID
	 * @param outUniIDmapSelectUniID ������geneID��UniIDmapSelect��
	 * @param outUniIDmapSelectUniIDnone ������geneID��UniIDmapSelect��,���ҽ��������еĿ��Բ���NCBIID�Ķ�����һ��
	 */
	public void upDateNCBIID(String taxIDFile, 
			String outGen2AccTaxID, String outGen2EnsembTaxID,String outUniIDmapSelect,
			String Ref2Uni,
			String outUniIDmapSelectUniID,String outUniIDmapSelectUniIDnone
			) 
	{
		//NCBIID����
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
