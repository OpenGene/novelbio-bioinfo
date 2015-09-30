package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.FtpFetch;
import com.novelbio.base.fileOperate.FileOperate;

/**
 *  自动化下载，就是自动从NCBI下载想要的文件，最后生成
 * @author zong0jie
 *
 */
public class DownloadFile {
	private static final Logger logger = Logger.getLogger(DownloadFile.class);
	public static void main(String[] args) {
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.setSaveto("/media/winE/NBCplatform/database/20150928");
		downloadFile.download();
	}
	String saveto;
	List<String> lsDownloadNCBI;
	List<String> lsGO;
	List<String> lsUniprot;
	List<String> lsEMBL;
	
	List<String> lsRice;
	
	
	public DownloadFile() {
		fillDownloadFile();
	}
	
	public void setSaveto(String saveto) {
		this.saveto = FileOperate.addSep(saveto);
		FileOperate.createFolders(saveto);
	}
	
	private void fillDownloadFile() {
		lsDownloadNCBI = new ArrayList<String>();
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2go.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2pubmed.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2ensembl.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2refseq.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz");
		lsDownloadNCBI.add("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_refseq_uniprotkb_collab.gz");
		
		lsGO = new ArrayList<String>();
		lsGO.add("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology_ext.obo");
		lsGO.add("http://www.geneontology.org/gene-associations/submission/gene_association.goa_uniprot.gz");
		
		lsUniprot = new ArrayList<String>();
		lsUniprot.add("ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping_selected.tab.gz");
		
		lsEMBL = new ArrayList<String>();
		lsEMBL.add("http://www.ensembl.org/info/data/ftp/index.html");
		
		lsRice = new ArrayList<String>();
		//GFF file
		lsRice.add("http://rapdb.dna.affrc.go.jp/download/archive/irgsp1/IRGSP-1.0_representative_2013-04-24.tar.gz");
		lsRice.add("http://rapdb.dna.affrc.go.jp/download/archive/RAP-MSU.txt.gz");
		lsRice.add("ftp://ftp.plantbiology.msu.edu/pub/data/Eukaryotic_Projects/o_sativa/annotation_dbs/pseudomolecules/version_7.0/all.dir/all.gff3");
		lsRice.add("ftp://ftp.plantbiology.msu.edu/pub/data/Eukaryotic_Projects/o_sativa/annotation_dbs/pseudomolecules/version_7.0/all.dir/all.GOSlim_assignment");
	}
	
	public void download() {
		for (String ftpFile : lsDownloadNCBI) {
			if (FileOperate.isFileExistAndBigThanSize(saveto + FileOperate.getFileName(ftpFile), 0)) {
				continue;
			}
			FtpFetch fetch = new FtpFetch();
			fetch.setDownLoadUrl(ftpFile);
			fetch.setSavePath(saveto);
			if (!fetch.downloadFile()) {
				logger.error("download error:" + ftpFile);
				FileOperate.DeleteFileFolder(saveto + FileOperate.getFileName(ftpFile));
			}
		}
		for (String ftpFile : lsUniprot) {
			if (FileOperate.isFileExistAndBigThanSize(saveto + FileOperate.getFileName(ftpFile), 0)) {
				continue;
			}
			FtpFetch fetch = new FtpFetch();
			fetch.setDownLoadUrl(ftpFile);
			fetch.setSavePath(saveto);
			if (!fetch.downloadFile()) {
				logger.error("download error:" + ftpFile);
				FileOperate.DeleteFileFolder(saveto + FileOperate.getFileName(ftpFile));
			}
		}
//		for (String gourl : lsGO) {
//			String fileName = FileOperate.getFileName(gourl);
//			if (FileOperate.isFileExistAndBigThanSize(saveto + fileName, 0)) {
//				continue;
//			}
//			HttpFetch httpFetch = HttpFetch.getInstance();
//			httpFetch.setUri(gourl);
//			httpFetch.query();
//			if (!httpFetch.download(saveto + fileName)) {
//				logger.error("download error:" + gourl);
//				FileOperate.DeleteFileFolder(saveto + fileName);
//			}
//		}
//		for (String rapDBurl : lsRice) {
//			String fileName = FileOperate.getFileName(rapDBurl);
//			if (FileOperate.isFileExistAndBigThanSize(saveto + fileName, 0)) {
//				continue;
//			}
//			HttpFetch httpFetch = HttpFetch.getInstance();
//			httpFetch.setUri(rapDBurl);
//			httpFetch.query();
//			if (!httpFetch.download(saveto + fileName)) {
//				logger.error("download error:" + rapDBurl);
//				FileOperate.DeleteFileFolder(saveto + fileName);
//			}
//		}
		
	}
}
