package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import org.apache.velocity.runtime.directive.Foreach;

import com.novelbio.base.dataOperate.FtpFetch;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;

/**
 *  自动化下载，就是自动从NCBI下载想要的文件，最后生成
 * @author zong0jie
 *
 */
public class DownloadFile {
	public static void main(String[] args) {
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.setSaveto("/media/winE/Bioinformatics/DataBase/dbnew");
		downloadFile.download();
	}
	String saveto;
	ArrayList<String> lsDownloadNCBI;
	ArrayList<String> lsGO;
	ArrayList<String> lsUniprot;
	ArrayList<String> lsEMBL;
		
	public DownloadFile() {
		fillDownloadFile();
	}
	
	public void setSaveto(String saveto) {
		this.saveto = FileOperate.addSep(saveto);
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
	}
	
	public void download() {
		for (String ftpFile : lsDownloadNCBI) {
			FtpFetch fetch = new FtpFetch();
			fetch.setDownLoadUrl(ftpFile);
			fetch.setSavePath(saveto);
			fetch.downloadFile();
		}
		for (String ftpFile : lsUniprot) {
			FtpFetch fetch = new FtpFetch();
			fetch.setDownLoadUrl(ftpFile);
			fetch.setSavePath(saveto);
			fetch.downloadFile();
		}
		for (String gourl : lsGO) {
			String fileName = FileOperate.getFileName(gourl);
			HttpFetch httpFetch = HttpFetch.getInstance();
			httpFetch.setUrl(gourl);
			httpFetch.query();
			httpFetch.download(saveto + fileName);
		}
	}
}
