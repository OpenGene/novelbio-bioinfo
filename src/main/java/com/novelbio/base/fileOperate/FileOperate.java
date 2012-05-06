package com.novelbio.base.fileOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class FileOperate {
	private static Logger logger = Logger.getLogger(FileOperate.class);
	
	public FileOperate() {
	}
	/**
	 * ��ȡ�ı��ļ�����
	 * 
	 * @param filePathAndName
	 *            ������������·�����ļ���
	 * @param encoding
	 *            �ı��ļ��򿪵ı��뷽ʽ
	 * @return �����ı��ļ�������
	 */
	public String readTxt(String filePathAndName, String encoding)
			throws IOException {
		encoding = encoding.trim();
		StringBuffer str = new StringBuffer("");
		String st = "";
		try {
			FileInputStream fs = new FileInputStream(filePathAndName);
			InputStreamReader isr;
			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + " ");
				}
			} catch (Exception e) {
				str.append(e.toString());
			}
			st = str.toString();
		} catch (IOException es) {
			st = "";
		}
		return st;
	}

	/**
	 * ����·��������������һ��·������"/" ����� /wer/fw4e/sr/frw/s3er.txt ���� /wer/fw4e/sr/frw/
	 * ���Ϊ���·�������ϲ㣬Ʃ���������soap �򷵻ء���
	 * ���Ը��������ڵ�·��
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getParentPathName(String fileName) {
		File file = new File(fileName);
		String fileParent =  file.getParent();
		if (fileParent == null) {
			return "";
		}
		else {
			return addSep(fileParent);
		}
	}
	/**
	 * �����ļ��������Ϻ�׺
	 * @param fileName ���԰���·�����������·�����򷵻�ȫ��·�����ͺ�׺��
	 * ������к�׺������ӡ�
	 * @suffix ����ӵĺ�׺��
	 * @return
	 */
	public static String addSuffix(String fileName,String suffix) {
		String[] thisFileName = getFileNameSep(fileName);
		if (thisFileName[1].equals(suffix)) {
			return fileName;
		}
		if (fileName.endsWith(".")) {
			return fileName + suffix;
		}
		return fileName+"."+suffix;
	}
	/**
	 * ����·���������������� �����/home/zong0jie/��/home/zong0jie ������zong0jie ���Ը��������ڵ�·��
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName) {
		File file = new File(fileName);
		return file.getName();
	}
	/**
	 * <b>δ������</b>
	 * �����ļ�·�������ش�С����λΪK
	 * @param filePath
	 * @return
	 * û���ļ�����0��������-1000000000
	 */
	public static double getFileSize(String filePath) {
		File file = new File(filePath);
		double totalsize = 0;
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			   FileInputStream fis = null;
               try{
                   fis = new FileInputStream(file);  
                   return fis.available()/1024;
               }catch(Exception e1){
            	   logger.error("IO����");
                   return -1000000000;
               }
		}
		else if (file.isDirectory()) {
			ArrayList<String[]> lsFileName = getFoldFileName(filePath);
			
			for (String[] strings : lsFileName) {
				String fileName = null;
				//����ļ���
				if (strings[1].equals("")) {
					fileName = addSep(filePath) + strings[0];
				}
				else {
					fileName = addSep(filePath) + strings[0] + "." + strings[1];
				}
				totalsize = totalsize + getFileSize(fileName);
			}
			return totalsize;
		}
		else {
     	   logger.error("����");
           return -1000000000;
       }
	}
	
	/**
	 * ����·����������������,������׺��<br>
	 * �����/home/zong0jie.aa.txt/��/home/zong0jie.aa.txt<br>
	 * ������zong0jie.aa �� txt<br>
	 * ���Ը��������ڵ�·��<br>
	 * 
	 * @param fileName
	 * @return string[2] 0:�ļ��� 1:�ļ���׺
	 */
	public static String[] getFileNameSep(String fileName) {
		String[] result = new String[2];

		File file = new File(fileName);
		String filename = file.getName();
		int endDot = filename.lastIndexOf(".");
		if (endDot > 0) {
			result[0] = (String) filename.subSequence(0, endDot);
			result[1] = (String) filename.subSequence(endDot + 1,
					filename.length());
		} else {
			result[0] = filename;
			result[1] = "";
		}
		return result;
	}

	/**
	 * ��ȡ�ļ����������ļ������׺,������·�� * ����ļ��������򷵻�null<br>
	 * 
	 * @param filePath
	 *            Ŀ¼·��,���Ҫ��\\��/
	 * @return arraylist ������string[2] 1:�ļ��� 2����׺
	 *     �ļ� wfese.fse.fe���� "wfese.fse"��"fe"<br>
	 *            �ļ� wfese.fse.���� "wfese.fse."��""<br>
	 *            �ļ� wfese ���� "wfese"��""<br>
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath) {
		return getFoldFileName(filePath, "*", "*");
	}

	/**
	 * ��ȡ�ļ����°���ָ���ļ������׺�������ļ���,�ȴ����ӹ������ļ����µ��ļ���Ҳ����ѭ������ļ�<br>
	 * ����ļ��������򷵻�null<br>
	 * ��������ļ��У��򷵻ظ��ļ���<br>
	 * 
	 * @param filePath
	 *            Ŀ¼·��,���Ҫ��\\��/
	 * @param filename
	 *            ָ���������ļ�������������ʽ ���� "*",������ʽ���Ӵ�Сд
	 * @param suffix
	 *            ָ�������ĺ�׺������������ʽ<br>
	 *            �ļ� wfese.fse.fe���� "wfese.fse"��"fe"<br>
	 *            �ļ� wfese.fse.���� "wfese.fse."��""<br>
	 *            �ļ� wfese ���� "wfese"��""<br>
	 * @return ���ذ���Ŀ���ļ�����ArrayList��������string[2] 1:�ļ��� 2����׺
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath,
			String filename, String suffix) {
		// ƥ���ļ������׺��
		Pattern pattern = Pattern.compile("(.*)\\.(\\w*)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		String name = ""; // �ļ���
		String houzhuiming = ""; // ��׺��
		String[] filenamefinal;
		filePath = deleteSep(filePath);
		if (filename == null || filename.equals("*") )//|| filename.equals(""))
		{
			filename = ".*";
		}
		if (suffix.equals("*")) {
			suffix = ".*";
		}
		// ================================================================//

		ArrayList<String[]> ListFilename = new ArrayList<String[]>();
		File file = new File(filePath);
		if (!file.exists()) {// û���ļ����򷵻ؿ�
			return null;
		}
		// ���ֻ���ļ��򷵻��ļ���
		if (!file.isDirectory()) { // ��ȡ�ļ������׺��
			String fileName = file.getName();
			if (!fileName.contains(".") || fileName.endsWith(".")) {
				name = fileName;
				houzhuiming = "";
			} else {
				matcher = pattern.matcher(fileName);
				if (matcher.find()) {
					name = matcher.group(1);
					houzhuiming = matcher.group(2);
				}
			}
			if (name.matches(filename) && houzhuiming.matches(suffix)) {
				filenamefinal = new String[2];
				filenamefinal[0] = name;
				filenamefinal[1] = suffix;
				ListFilename.add(filenamefinal);
				return ListFilename;
			}
			return null;
		}
		// ������ļ���
		String[] filenameraw = file.list();
		for (int i = 0; i < filenameraw.length; i++) {
			if (!filenameraw[i].contains(".") || filenameraw[i].endsWith(".")) {
				name = filenameraw[i];
				houzhuiming = "";
			} else {
				matcher = pattern.matcher(filenameraw[i]);
				if (matcher.find()) {
					name = matcher.group(1);
					houzhuiming = matcher.group(2);
				}
			}
			// ��ʼ�ж�
			if (name.matches(filename) && houzhuiming.matches(suffix)) {
				filenamefinal = new String[2];
				filenamefinal[0] = name;
				filenamefinal[1] = houzhuiming;
				ListFilename.add(filenamefinal);
			}
		}
		return ListFilename;
	}

	/**
	 * �½�Ŀ¼,������ļ��д����򷵻�
	 * 
	 * @param folderPath
	 *            Ŀ¼·��,���Ҫ��\\��/
	 * @return ����Ŀ¼�������·��
	 */
	private static String createFolder(String folderPath) {
		String txt = folderPath;
		try {
			File myFilePath = new File(txt);
			txt = folderPath;
			if (!myFilePath.exists()) {
				myFilePath.mkdir();
			}
		} catch (Exception e) {
			logger.error("����Ŀ¼��������");
		}
		return txt;
	}

	/**
	 * �༶Ŀ¼����
	 * 
	 * @param folderPath
	 *            ׼��Ҫ�ڱ���Ŀ¼�´�����Ŀ¼��Ŀ¼·�� ���� c:myf
	 * @param paths
	 *            ���޼�Ŀ¼����������Ŀ¼��/��\\���� ���� a/b/c
	 * @return ���ش����ļ����·�� ���� c:/myf/a/b/c
	 */
	public static boolean createFolders(String folderPath) {
		if (isFileExist(folderPath)) {
			if (isFileDirectory(folderPath))
				return true;
			else
				return false;
		}
		String foldUpper = folderPath;
		String creatPath = "";
		boolean flag = true;
		while (flag) {
			if (isFileExist(foldUpper)) {
				flag = false;
				break;
			}
			creatPath = getFileName(foldUpper) + File.separator + creatPath;
			foldUpper = getParentPathName(foldUpper);
		}
		String txts = addSep(foldUpper);
		try {
			String txt;
			StringTokenizer st = new StringTokenizer(creatPath, "/\\");
			for (int i = 0; st.hasMoreTokens(); i++) {
				txt = st.nextToken().trim();
				txts = createFolder(txts + txt);
			}
			return true;
		} catch (Exception e) {
			logger.error("����Ŀ¼��������");
			return false;
		}
	}

	/**
	 * �½��ļ�
	 * 
	 * @param filePathAndName
	 *            �ı��ļ���������·�����ļ���
	 * @param fileContent
	 *            �ı��ļ�����
	 * @return
	 */
	public static void createFile(String filePathAndName, String fileContent) {

		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath);
			PrintWriter myFile = new PrintWriter(resultFile);
			String strContent = fileContent;
			myFile.println(strContent);
			myFile.close();
			resultFile.close();
		} catch (Exception e) {
			logger.error("�����ļ���������");
		}
	}

	/**
	 * �б��뷽ʽ���ļ�����
	 * 
	 * @param filePathAndName
	 *            �ı��ļ���������·�����ļ���
	 * @param fileContent
	 *            �ı��ļ�����
	 * @param encoding
	 *            ���뷽ʽ ���� GBK ���� UTF-8
	 * @return
	 */
	public static void createFile(String filePathAndName, String fileContent,
			String encoding) {

		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			PrintWriter myFile = new PrintWriter(myFilePath, encoding);
			String strContent = fileContent;
			myFile.println(strContent);
			myFile.close();
		} catch (Exception e) {
			logger.error("�����ļ���������");
		}
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param filePathAndName
	 *            �ı��ļ���������·�����ļ��� �ļ��������򷵻�false
	 * @return Boolean �ɹ�ɾ������true�����쳣����false
	 */
	public static boolean delFile(String filePathAndName) {
		boolean bea = false;
		try {
			String filePath = filePathAndName;
			File myDelFile = new File(filePath);
			if (myDelFile.exists()) {
				myDelFile.delete();
				bea = true;
			} else {
				bea = false;
				// message = (filePathAndName+"ɾ���ļ���������");
			}
		} catch (Exception e) {
			logger.error( e.toString());
		}
		return bea;
	}

	/**
	 * ɾ���ļ���
	 * 
	 * @param folderPath
	 *            �ļ�����������·��
	 * @return
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // ɾ����������������
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // ɾ�����ļ���
		} catch (Exception e) {
			logger.error("ɾ���ļ��в�������");
		}
	}

	/**
	 * ɾ��ָ���ļ����������ļ�,
	 * 
	 * @param path
	 *            �ļ�����������·��,�������ν�Ӳ���\\��/
	 * @return
	 * @return
	 */
	public static boolean delAllFile(String path) {
		path = addSep(path);
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			temp = new File(path + tempList[i]);
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + tempList[i]);// ��ɾ���ļ���������ļ�
				delFolder(path + tempList[i]);// ��ɾ�����ļ���
				bea = true;
			}
		}
		return bea;
	}

	/**
	 * ���Ƶ����ļ�
	 * 
	 * @param oldPathFile
	 *            ׼�����Ƶ��ļ�Դ
	 * @param newPathFile
	 *            �������¾���·�����ļ���
	 * @param cover
	 *            �Ƿ񸲸�
	 * @return
	 */
	public static boolean copyFile(String oldPathFile, String newPathFile,
			boolean cover) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			File newfile = new File(newPathFile);

			if (oldfile.exists()) { // �ļ�����ʱ
				if (newfile.exists()) {
					if (!cover) {
						return false;
					}
					newfile.delete();
				}
				InputStream inStream = new FileInputStream(oldPathFile); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error ("���Ƶ����ļ���������");
			return false;
		}
	}

	/**
	 * ���������ļ��е�����,���Ҫ�ļ��Ѿ����ڣ�������
	 * 
	 * @param oldPath
	 *            ׼��������Ŀ¼���������ν�Ӳ���"/"
	 * @param newPath
	 *            ָ������·������Ŀ¼
	 * @return
	 */
	public static void copyFolder(String oldPath, String newPath, boolean cover) {
		newPath = addSep(newPath);
		oldPath = addSep(oldPath);
		try {
			new File(newPath).mkdirs(); // ����ļ��в����� �������ļ���
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				temp = new File(oldPath + file[i]);
				if (temp.isFile()) { // ���Ŀ���ļ����Ѿ������ļ���������
					File targetfile = new File(newPath
							+ (temp.getName()).toString());
					if (targetfile.exists()) {
						if (!cover) {
							continue;
						}
						targetfile.delete();
					}
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// ��������ļ���
					copyFolder(oldPath + "/" + file[i],
							newPath + "/" + file[i], cover);
				}
			}
		} catch (Exception e) {
			logger.error("���������ļ������ݲ�������");
		}
	}

	/**
	 * �ļ�����,�������ͬ���ļ����ڣ��򲻸���������
	 * 
	 * @param oldName
	 *            ����ȫ��·�����ļ���
	 * @param newName
	 *            Ҫ�޸ĵ��ļ���,������·��
	 * @return
	 */
	public static void changeFileName(String oldName, String newName) {
		changeFileName(oldName, newName,false);
	}
	
	
	/**
	 * ֻ��������ļ���������ֱ�Ӳ����ļ�
	 * �ļ���Ӻ�׺���ĺ�׺�������һ�����޸�
	 * @param FileName ԭ���ļ���ȫ��
	 * @param append Ҫ��ӵĺ�׺��Ʃ��_1��_new�����Ϊnull�������
	 * @param suffix Ҫ��ӵĺ�׺����Ʃ�� txt�� jpg ���Զ�ȥ�ո�
	 * suffix == null�򲻸ı��׺����suffix = "" ��ȥ����׺��
	 */
	public static String changeFileSuffix(String FileName, String append, String suffix) {
		String resultFile = "";
		if (append == null) {
			append = "";
		}
		String parentPath = addSep(getParentPathName(FileName));
		String[] fileName = getFileNameSep(FileName);
		resultFile = parentPath + fileName[0] + append;
		if (suffix == null ) {
			if (fileName[1] == null || fileName[1].equals("")) {
				return resultFile;
			}
			else {
				return resultFile + "."+ fileName[1];
			}
		}
		else if (suffix.trim().equals("")) {
			return resultFile;
		}
		else {
			return resultFile + "."+ suffix;
		}
	}
	
	
	/**
	 * ֱ�Ӳ����ļ�
	 * �ļ���Ӻ�׺���ĺ�׺�������һ�����޸�
	 * @param FileName ԭ���ļ���ȫ��
	 * @param append Ҫ��ӵĺ�׺��Ʃ��_1��_new�����Ϊnull�������
	 * @param suffix Ҫ��ӵĺ�׺����Ʃ�� txt�� jpg ���Զ�ȥ�ո�
	 * suffix == null�򲻸ı��׺����suffix = "" ��ȥ����׺��
	 */
	public static String changeFileSuffixReal(String FileName, String append, String suffix) {
		String newFile = changeFileSuffix(FileName, append, suffix);
		moveFile(FileName, getParentPathName(newFile), getFileName(newFile), true);
		return newFile;
	}
	
	/**
	 * �ļ�����,�������ͬ���ļ����ڣ��򲻸���������
	 * 
	 * @param oldName
	 *            ����ȫ��·�����ļ���
	 * @param newName
	 *            Ҫ�޸ĵ��ļ���,������·��
	 * @return
	 */
	public static void changeFileName(String oldName, String newName,boolean cover) {
		// �ļ�ԭ��ַ
		File oldFile = new File(oldName);
		// �ļ��£�Ŀ�꣩��ַ

		File fnew = new File(oldFile.getParentFile() + File.separator + newName);
		if (fnew.exists()&&!cover) // ������ļ����ڣ��򲻱�
		{
			return;
			// fnew.delete();
		}
		else {
			fnew.delete();
		}
		oldFile.renameTo(fnew);
	}
	
	
	
	
	
	
	/**
	 * �ƶ��ļ�������µ�ַ��ͬ���ļ������ƶ�������<br>
	 * ���Դ���һ�����ļ���<br>
	 * ���û���ļ��򷵻�<br>
	 * ע�⣺���ļ��к�Ҫ��\\<br>
	 * 
	 * @param oldPath
	 *            �ļ�·��
	 * @param newPath
	 *            ���ļ����ڵ��ļ���
	 * @return ���ļ���·����
	 * null:û�гɹ�
	 */
	public static String moveFile(String oldPath, String newPath, boolean cover) {
		// �ļ�ԭ��ַ
		File oldFile = new File(oldPath);
		if (moveFile(oldPath, newPath, oldFile.getName(), cover)) {
			return newPath+getFileName(oldPath);
		}
		return null;
	}
	
	/**
	 * �ƶ��ļ�������µ�ַ��ͬ���ļ������ƶ�������<br>
	 * ���Դ���һ�����ļ���<br>
	 * ���û���ļ��򷵻�<br>
	 * ע�⣺���ļ��к�Ҫ��\\<br>
	 * 
	 * @param oldPath
	 *            �ļ�·��
	 * @param newPath
	 *            ���ļ����ڵ��ļ���
	 * @param newName
	 *            ���ļ����ļ���
	 * @param cover
	 *            �Ƿ񸲸�
	 * @return true �ɹ�
	 * false ʧ��
	 */
	public static boolean moveFile(String oldFileName, String newPath, String newName,
			boolean cover) {
		newPath = addSep(newPath);
		// �ļ�ԭ��ַ
		File oldFile = new File(oldFileName);
		// �ļ��£�Ŀ�꣩��ַ
		// newһ�����ļ���
		File fnewpath = new File(newPath);
		if (!oldFile.exists()) {
			return false;
		}
		// �ж��ļ����Ƿ����
		if (!fnewpath.exists())
			fnewpath.mkdirs();// �������ļ�
		// ���ļ��Ƶ����ļ���
		File fnew = new File(newPath + newName);
		if (fnew.exists()) {
			if (!cover) {
				return false;
			}
			fnew.delete();
		}
		if (!oldFile.renameTo(fnew)) {
			if (copyFile(oldFileName, newPath + newName, cover))
			{
				oldFile.delete();
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * �ƶ�ָ���ļ����ڵ�ȫ���ļ������Ŀ���ļ������������ļ�����������ͬʱ����false<br/>
	 * ������ļ��в����ڣ��ʹ������ļ��У������ƺ�ֻ�ܴ���һ���ļ��С��ƶ�˳���򷵻�true
	 * 
	 * @param oldfolderfile
	 *            Ҫ�ƶ����ļ�Ŀ¼,Ŀ¼������ν�Ӳ���"/"
	 * @param newfolderfile
	 *            Ŀ���ļ�Ŀ¼
	 * @param prix
	 *            ���ļ�ǰ���ϵ�ǰ׺
	 * @param cover
	 *            �Ƿ񸲸�
	 * @throws Exception
	 */
	public static boolean moveFoldFile(String oldfolderfile,
			String newfolderfile, String prix, boolean cover) throws Exception {
		// ���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
		oldfolderfile = addSep(oldfolderfile);
		newfolderfile = addSep(newfolderfile);

		boolean ok = true;
		try {
			File olddir = new File(oldfolderfile);
			File[] files = olddir.listFiles(); // �ļ�һ��
			if (files == null)
				return false;
			File newDir = new File(newfolderfile);// Ŀ��
			if (!newDir.exists()) {
				newDir.mkdirs();
			}
			// �ļ��ƶ�
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) // ������ļ����ļ��У���ݹ���ñ����������ʵ��÷�����
				{
					ok = moveFoldFile(files[i].getPath(), newfolderfile
							+ files[i].getName(), prix, cover);
					// �ɹ���ɾ��ԭ�ļ�
					if (ok) {
						files[i].delete();
					}
					continue;
				}
				File fnew = new File(newfolderfile + prix + files[i].getName());
				// Ŀ���ļ����´��ڵĻ�������
				if (fnew.exists()) {
					if (!cover) {
						ok = false;
						continue;
					}
					fnew.delete();
				}
				if (!files[i].renameTo(fnew)) {
					if (copyFile(files[i].getAbsolutePath(),
							fnew.getAbsolutePath(), cover))
						files[i].delete();
					else {
						ok = false;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return ok;
	}

	/**
	 * �ƶ�Ŀ¼��Ҫ�ƶ����ļ�Ŀ¼,Ŀ¼������ν�Ӳ���"/" �����moveFoldFile����ò��ûɶ����һ�����������ԭ�ļ���ɾ��
	 * 
	 * @param oldPath
	 * @param newPath
	 *            û�лᴴ��һ���ļ��У����Ǻ���ֻ�ܴ���һ���ļ���
	 * @param cover
	 *            �Ƿ񸲸�
	 * @return
	 */
	public static void moveFolder(String oldPath, String newPath, boolean cover) {
		try {
			moveFoldFile(oldPath, newPath, "", cover);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * �ж��ļ��Ƿ���ڣ������Ǿ���·��
	 * @param fileName ���Ϊnull, ֱ�ӷ���false
	 * @return
	 */
	public static boolean isFileExist(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = new File(fileName);
		if (file.exists()) {// û���ļ����򷵻ؿ�
			return true;
		} else {
			return false;
		}
	}
	/**
	 * �ж��ļ��Ƿ�Ϊ�ļ���,nullֱ�ӷ���false
	 * @param fileName
	 * @return
	 */
	public static boolean isFileDirectory(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = new File(fileName);
		if (file.isDirectory()) {// û���ļ����򷵻ؿ�
			return true;
		} else {
			return false;
		}
	}
	/**
	 * �ж��ļ��Ƿ�Ϊ�ļ�
	 * @param fileName
	 * @return
	 */
	public static boolean isFile(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = new File(fileName);
		if (file.isFile()) {// û���ļ����򷵻ؿ�
			return true;
		} else {
			return false;
		}
	}
	/**
	 * ɾ�������ļ�
	 * 
	 * @param sPath
	 *            ��ɾ���ļ����ļ���
	 * @return �����ļ�ɾ���ɹ�����true�����򷵻�false
	 */
	private static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * ɾ��Ŀ¼���ļ��У��Լ�Ŀ¼�µ��ļ�
	 * 
	 * @param sPath
	 *            ��ɾ��Ŀ¼���ļ�·�����������ν�Ӳ���"/"
	 * @return Ŀ¼ɾ���ɹ�����true�����򷵻�false
	 */
	private static boolean deleteDirectory(String sPath) {
		// ���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
		sPath = addSep(sPath);
		File dirFile = new File(sPath);
		// ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// ɾ���ļ����µ������ļ�(������Ŀ¼)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// ɾ�����ļ�
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // ɾ����Ŀ¼
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// ɾ����ǰĿ¼
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ����·��ɾ��ָ����Ŀ¼���ļ������۴������
	 * 
	 * @param sPath
	 *            Ҫɾ����Ŀ¼���ļ�
	 * @return ɾ���ɹ����� true�����򷵻� false��
	 */
	public static boolean DeleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// �ж�Ŀ¼���ļ��Ƿ����
		if (!file.exists()) { // �����ڷ��� false
			return flag;
		} else {
			// �ж��Ƿ�Ϊ�ļ�
			if (file.isFile()) { // Ϊ�ļ�ʱ����ɾ���ļ�����
				return deleteFile(sPath);
			} else { // ΪĿ¼ʱ����ɾ��Ŀ¼����
				return deleteDirectory(sPath);
			}
		}
	}
	/**
	 * ����ļ��ָ��
	 * @param path
	 * @return
	 */
	public static String addSep(String path) {
		path = path.trim();
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		return path;
	}
	
	/**
	 * ɾ���ļ��ָ��
	 * @param path
	 * @return
	 */
	public static String deleteSep(String path) {
		path = path.trim();
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() -1);
		}
		return path;
	}
}
