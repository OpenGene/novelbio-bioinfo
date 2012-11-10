package com.novelbio.base.dataOperate;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.fileOperate.FileOperate;

public class FtpFetch {
	public static void main(String[] args) throws MalformedURLException {
		FtpFetch fetch = new FtpFetch();
		fetch.setDownLoadUrl("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz");
		System.out.println("stop");
	}
	FTPClient ftp;
	int port = 21;

	/** ftp��������ַ */
	String url;
	/** FTP�������ϵ����·�� */
	String remotePath;
	
	String username = "anonymous";
	String password = "a@a.com";
	
	/** ������ļ��� */
	String ftpFileName;
	/** ���浽�ı���·�� */
	String savePath;

	/**
	 * �趨ftp������
	 * @param url
	 */
	public void setUrl(String url, String username, String password) {
		URL urlThis = null;
		try {
			urlThis = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		initial(urlThis.getHost(), username, password);
	}
	/**
	 * �趨��Ҫ���ص����·��
	 * @param ftpFileName
	 */
	public void setRemotePath(String remotePath) {
		if (remotePath.endsWith("/")) {
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		this.remotePath = remotePath;
	}
	public void setSavePath(String savePath) {
		this.savePath = FileOperate.addSep(savePath);
	}
	/**
	 * �趨��Ҫ���ص��ļ���
	 * @param ftpFileName
	 */
	public void setFtpFileName(String ftpFileName) {
		this.ftpFileName = ftpFileName;
	}
	
	/** ������Ǿ�����ļ�url */
	public void setDownLoadUrl(String urlAll) {
		try {
			String urlPath = FileOperate.getParentPathName(urlAll).replace(":/", "://");
			URL decodeUrl = new URL(urlPath);
			this.ftpFileName = FileOperate.getFileName(urlAll);
			String remotePath = decodeUrl.getPath();
			setRemotePath(remotePath);
			initial(decodeUrl.getHost(), username, password);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/** �Ƿ��ʼ���ɹ� */
	private boolean initial(String url, String username, String password) {
		//����Ҫ��ʼ��
		if (ftp != null && this.url.equals(url)
				&&
				(username == null || this.username.equals(username)) 
				&& 
				(password == null || this.password.equals(password))
		) {
			return true;
		}
		
		ftp = new FTPClient();
        boolean reply;
		try {
			ftp.connect(url, port);
			// �������Ĭ�϶˿ڣ�����ʹ�� ftp.connect(url)�ķ�ʽֱ������FTP������
			ftp.login(username, password);// ��¼
			reply = ftp.isAuthenticated();
			if (!reply) {
				ftp.logout();
				ftp.disconnect(true);
				ftp = null;
				return false;
			}
		} catch (Exception e) {
			try { ftp.disconnect(false); } catch (Exception e1) {}
			ftp = null;
			e.printStackTrace();
			return false;
		}
        return true;
	}
	
	/**
	 * ���ظ��ļ����µ�ȫ���ļ��������������ļ���
	 * @return �����򷵻�null
	 */
	public List<FTPFile> getLsFiles() {
        try {
			ftp.changeDirectory(remotePath);
			FTPFile[] fs = ftp.list();
			ArrayList<FTPFile> lsFile = new ArrayList<FTPFile>();
			for (FTPFile ftpFile : fs) {
				lsFile.add(ftpFile);
			}
			return lsFile;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//ת�Ƶ�FTP������Ŀ¼  
        return null;
	}
	/** �����ļ� */
	public boolean downloadFile() {
		List<FTPFile> lsAllFiles = getLsFiles();
		if (lsAllFiles == null) {
			return false;
		}
        for(FTPFile ff : lsAllFiles){  
            if(ff.getName().equals(ftpFileName)){  
                File localFile = new File(savePath + ff.getName());  
				try {
					ftp.download(ff.getName(), localFile);
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
            }  
        }
        return false;
	}
}
