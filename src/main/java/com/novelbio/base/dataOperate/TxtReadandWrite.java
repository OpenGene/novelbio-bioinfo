package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;
/**
 * ʹ��ǰ����setParameter����
 * ʹ����Ϻ����close�ر���
 * @author zong0jie
 *
 */
public class TxtReadandWrite {
	private static Logger logger = Logger.getLogger(TxtReadandWrite.class);
	@Deprecated
	public TxtReadandWrite () {
		
	}
	public TxtReadandWrite (String filepath, boolean createNew) {
		if (createNew) {
			setParameter(filepath, createNew, false);
		}
		else {
			setParameter(filepath, createNew, true);
		}
	}
	
	File txtfile;
	FileReader fileread;
	FileWriter filewriter;
	BufferedReader bufread;
	BufferedWriter bufwriter;
	public String getFileName() {
		return txtfile.getAbsolutePath();
	}
	/**
	 * 
	 * @param filepath
	 *            Ҫ��ȡ��д����ļ���filepath
	 * @param createNew
	 *            ���ı�������ʱ���Ƿ���Ҫ�½��ı�
	 * @param append
	 *            �ǽ���д�뻹��д�µġ�<b>��ȡ�ı�ʱ��������Ϊtrue</b>
	 * @return true���ɹ������ı�����<br>
	 *         false��û������ı�����
	 */
	public boolean setParameter(String filepath, boolean createNew,
			boolean append) {
		txtfile = new File(filepath);
		if (txtfile.exists() == false) {
			if (createNew)// ����ı��ļ��������򴴽���
			{
				try {
					txtfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				txtfile = new File(filepath); // ����ʵ����
			} else {
				return false;
			}
		}
		try {
			filewriter = new FileWriter(txtfile, append);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param path�����ļ���
	 * @return ����BufferedReader���ǵö����Ҫ�ر�Buffer��
	 * @throws Exception
	 */
	public BufferedReader readfile() throws Exception {
		fileread = new FileReader(txtfile);
		bufread = new BufferedReader(fileread);
		return bufread;
	}
	/**
	 * @param path�����ļ���
	 * @return ����List<String>�����겻�ùر�Buffer��
	 * @throws Exception
	 */
	public ArrayList<String> readfileLs(){

		ArrayList<String> lsResult = new ArrayList<String>();
		String content = "";
		try {
			BufferedReader read = readfile();
			// ������ǰ��ĺö���
			while ((content = read.readLine()) != null) {
				lsResult.add(content);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return lsResult;
	}
	
	/**
	 * ȥ���ո���ļ����ַ����ȣ������ļ���С�����Ǻ��ж�������
	 * @return
	 */
	public long getTxtLen() {
		int Result = 0;
		String content = "";
		// ������ǰ��ĺö���
		try {
			BufferedReader read = readfile();
			while ((content = read.readLine()) != null) {
				Result = Result + content.trim().length();
			}
		} catch (Exception e) {
			logger.error("��ȡ����");
			e.printStackTrace();
		}
		return Result;
	}

	/**
	 * @return ���� String�����겻�ùر�Buffer��
	 * @throws Exception
	 */
	public String readFirstLine() throws Exception {
		BufferedReader read = readfile();
		// ������ǰ��ĺö���
		return  read.readLine();
	}
	
	/**
	 * @param Num ��ȡǰ���У�ʵ���С�����ı�û����ô���У���ôֻ��ȡ������
	 * @return ���� String�����겻�ùر�Buffer��
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) throws Exception {
		ArrayList<String> lsResult = new ArrayList<String>();
		BufferedReader read = readfile();
		String content = ""; int rownum = 1;
		// ������ǰ��ĺö���
		while ((content = read.readLine()) != null) {
			if (rownum > Num) {
				break;
			}
			lsResult.add(content);
			rownum ++;
		}
		close();
		return lsResult;
	}
	
	/**
	 * �ر�buffer��
	 * 
	 * @throws IOException
	 */
	public void closeBufferRead() {
		try {
			bufread.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefile(String content) {
		try {
			filewriter.write(content);
			filewriter.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	/**
	 * д�벢����
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln(String content) {
		try {
			filewriter.write(content);
			filewriter.write("\r\n");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * д�벢����
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln() {
		try {
			filewriter.write("\r\n");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * д�벢��д������л��У�Ŀǰֻ��д��ascII�ı�
	 * @param content �����string����û�л��е�����
	 * @param length ÿ�����н��л���
	 * @throws Exception
	 */
	public void writefilePerLine(String content, int length) {
		try {
			char[] mychar = content.toCharArray();
			for (int i = 0; i < mychar.length; i++) {
				if (i>0 && i%length == 0) {
					filewriter.write("\r\n");
				}
				filewriter.write(mychar[i]);
			}
			filewriter.flush();
		} catch (Exception e) {
		}
	}
	
	/**
	 * @param content
	 *            ��Ҫд���ļ�����,�������Ƿ�ˢ��--Ҳ����ֱ��д���ļ������ǽ��뻺��
	 * @throws Exception
	 */
	public void writefile(String content, boolean flush) throws Exception {
		filewriter.write(content);
		if (flush) {
			filewriter.flush();
		}
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ
	 * 
	 * @param content
	 * @param colLen ÿ��д����
	 * @param sep �ָ�����ʲô
	 * @throws Exception
	 */
	private void Rwritefile(double[] content, int colLen, String sep)
			throws Exception {
		for (int i = 0; i < content.length; i++) {
			filewriter.write(content[i] + "" + sep);
			if ((i + 1) % colLen == 0) {
				filewriter.write("\r\n");
			}
		}

		filewriter.flush();
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
	 * 
	 * @param content
	 */
	public void Rwritefile(double[] content) throws Exception {
		Rwritefile(content, 20, " ");
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ
	 * 
	 * @param content
	 * @param colLen
	 * @param sep
	 * @throws Exception
	 */
	private void Rwritefile(int[] content, int colLen, String sep) {
		try {
			for (int i = 0; i < content.length; i++) {
				filewriter.write(content[i] + "" + sep);
				if ((i + 1) % colLen == 0) {
					filewriter.write("\r\n");
				}
			}
			filewriter.flush();
		} catch (Exception e) {
			logger.error("file error: "+ getFileName());
		}
		
	}
	/**
	 * ָ��������ʽ�����ı��к��и�������ʽ����ȫ��ɾ��
	 * @param regx
	 */
	public void delLines(String regx, boolean isregx) {
		String tmpFileName = txtfile.getAbsolutePath()+"TmpOfZJJAVA";
		TxtReadandWrite txtNewFile = new TxtReadandWrite(tmpFileName,true);
		Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
		Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		try {
			String content = "";
			readfile();
			while ((content = bufread.readLine()) != null) {
				if (isregx) {
					matcher = pattern.matcher(content);
					if (matcher.find()) {
						continue;
					}
				}
				else {
					if (content.contains(regx)) {
						continue;
					}
				}
				txtNewFile.writefileln(content);
			}
			txtNewFile.close();
			FileOperate.delFile(txtfile.getAbsolutePath());
			FileOperate.changeFileName(tmpFileName, FileOperate.getFileName(txtfile.getAbsolutePath()),true);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fileread.close();
		} catch (Exception e) {
		}
		try {
			filewriter.close();
		} catch (Exception e) {
		}
	}
	
	
	
	
	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
	 * 
	 * @param content
	 */
	public void Rwritefile(int[] content) {
		Rwritefile(content, 20, " ");
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ
	 * 
	 * @param content
	 * @param colLen
	 * @param sep
	 * @throws Exception
	 */
	private void Rwritefile(String[] content, int colLen, String sep) {
		try {
			for (int i = 0; i < content.length; i++) {
				filewriter.write(content[i] + "" + sep);
				if ((i + 1) % colLen == 0) {
					filewriter.write("\r\n");
				}
			}

			filewriter.flush();
		} catch (Exception e) {
			logger.error("file error: "+getFileName());
		}
		
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
	 * 
	 * @param content
	 */
	public void Rwritefile(String[] content) {
		Rwritefile(content, 20, " ");
	}

	/**
	 * @param lsContent-T ע��Tֻ����string interge�ȼ򵥵���ת��Ϊstring����
	 *            ��Ҫд��List--String�ļ�����,�Զ���ÿһ����ӻ��з�"\r\n";
	 * @throws Exception
	 */
	public<T> void writefile(List<T> lsContent) throws Exception {
		
		for (int i = 0; i < lsContent.size(); i++) {
			filewriter.write(lsContent.get(i).toString());
			filewriter.write("\r\n");
		}
		filewriter.flush();
	}

	/**
	 * ����excel������ȡ�ı�ʱʹ�ã����� ���txt�ı���������������һ����""����������һ��
	 * 
	 * @return
	 * @throws Exception
	 */
	public int ExcelRows() {
		try {
			int rowNum = 0;
			BufferedReader readasexcel = readfile();
			String content = "";
			String content2 = "";
			while ((content = readasexcel.readLine()) != null) {
				rowNum++;
				content2 = content;
			}
			
			if (content2.equals(""))
			{
				readasexcel.close();
				return rowNum - 1;
			}
			else
			{
				readasexcel.close();
				return rowNum;
			}
		} catch (Exception e) {
			logger.error("excelRows error: "+ getFileName());
			return -1;
		}
		
	}

	/**
	 * ����excel������ȡ�ı�ʱʹ�ã����� ����ı�����е�������Ч���ƺ��е����
	 * 
	 * @param setRow
	 *            ָ�����������ָ���г����ı�����У���ָ������Ϊ����С�
	 * @param sep
	 *            ���еķָ�����Ϊ������ʽ��tabΪ"\t"
	 * @return ����ָ���е�����
	 * @throws Exception
	 */
	public int ExcelColumns(String sep){
		int excelRows = ExcelRows();
		try {
			BufferedReader readasexcel = readfile();
			int colNum=0;
			for (int i = 0; i < excelRows - 1; i++) {
				String tmpstr = readasexcel.readLine();
				int TmpColNum = tmpstr.split(sep).length;
				if (TmpColNum>colNum) {
					colNum=TmpColNum;
				}
			}
			return colNum;
		} catch (Exception e) {
			logger.error("get Columns error: "+ getFileName());
			return -1;
		}
		
	}

	/**
	 * ����excel������ȡ�ı�ʱʹ�ã����� ���txt�ı�ָ���е�����
	 * 
	 * @param setRow
	 *            ָ��������Ϊʵ�����������ָ���г����ı�����У���ָ������Ϊ����С�
	 * @param sep
	 *            ���еķָ�����Ϊ������ʽ��tabΪ"\t"
	 * @return ����ָ���е�����
	 * @throws Exception
	 */
	public int ExcelColumns(int setRow, String sep) throws Exception {
		int excelRows = ExcelRows();
		if (setRow > excelRows) {
			setRow = excelRows;
		}
		BufferedReader readasexcel = readfile();
		for (int i = 0; i < setRow - 1; i++) {
			readasexcel.readLine();
		}
		String tmpstr = readasexcel.readLine();
		String[] tmp = tmpstr.split(sep);
		return tmp.length;
	}

	/**
	 * �������txt�ı�����excel�ķ�����ȡ
	 * 
	 * @param sep
	 *            txt�ı��ķָ��
	 * @param rowNum
	 *            ʵ�ʶ�ȡ��
	 * @param columnNum
	 *            ʵ�ʶ�ȡ��
	 * @return ����string,����ֵ,���ֵΪnull�򷵻�""
	 * @throws Exception
	 */
	public String ExcelRead(String sep, int rowNum, int columnNum)
			throws Exception {
		BufferedReader readasexcel = readfile();
		// ������ǰ��ĺö���
		for (int i = 0; i < rowNum - 1; i++) {
			if (readasexcel.readLine() == null)// ����ı���û����ô����
			{
				return "";
			}
		}
		// ��ʽ��ȡ
		String content = "";
		String[] tmp;// ������ʱ����
		content = readasexcel.readLine();
		tmp = content.split(sep);
		if (tmp.length < columnNum)
			return "";
		return tmp[columnNum - 1];
	}

	/**
	 * �������txt�ı�����excel�ķ�����ȡ
	 * ���һ�м�ʹû����Ҳ����""��ʾ
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param columnStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param rowEndNum
	 *            ʵ�ʶ�ȡ��ֹ��
	 * @param columnEndNum
	 *            ʵ�ʶ�ȡ��ֹ��
	 * @return ����string[] ����,������null����""�滻
	 * @throws Exception
	 */
	public String[][] ExcelRead(String sep, int rowStartNum,
			int columnStartNum, int rowEndNum, int columnEndNum)
			throws Exception {
		BufferedReader readasexcel = readfile();
		int readlines = rowEndNum - rowStartNum + 1;
		int readcolumns = columnEndNum - columnStartNum + 1;
		// System.out.println(readlines);
		// System.out.println(readcolumns);
		String[][] result = new String[readlines][readcolumns];

		// ������ǰ��ĺö���
		for (int i = 0; i < rowStartNum - 1; i++) {
			if (readasexcel.readLine() == null)// ����ı���û����ô����
			{
				return null;
			}
		}
		// ��ʽ��ȡ
		String content = "";
		String[] tmp;// ������ʱ����
		for (int i = 0; i < readlines; i++) {
			if ((content = readasexcel.readLine()) == null)// ������
			{
				break;
			}
			tmp = content.split(sep);
			for (int j = 0; j < readcolumns; j++) {
				if (tmp.length >= columnStartNum + j) {
					result[i][j] = tmp[columnStartNum - 1 + j];
				}
			}
		}
		for (int i = 0; i < result.length; i++)// ������Ϊnull����ͨͨ��ֵΪ""
		{
			for (int j = 0; j < result[0].length; j++) {
				if (result[i][j] == null)
					result[i][j] = "";
			}
		}
		return result;
	}

	/**
	 * �������txt�ı�����excel�ķ�����ȡ,�Զ���������
	 * ���һ��Ϊ���еĻ��ᱣ��
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param columnStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param rowEndNum
	 *            ʵ�ʶ�ȡ��ֹ��
	 * @param columnEndNum
	 *            ʵ�ʶ�ȡ��ֹ��,������=-1ʱ����ȡ�����У�������ArrayList--String[]��<br>
	 *            ��������������У���ô�Ͱѱ��ж���ȡ��
	 * @param colNotNone
	 *            �����У����в���Ϊ""������Ѹ���Ϊ""����ɾ�����������<=0���򲻿���
	 * @return ����ArrayList<String[]> ����,������null����""�滻
	 * @throws Exception
	 */
	public ArrayList<String[]> ExcelRead(String sep, int rowStartNum,
			int columnStartNum, int rowEndNum, int columnEndNum, int colNotNone)
	{

		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			int readlines = rowEndNum - rowStartNum + 1;
			if (columnEndNum<0) {
				columnEndNum =	ExcelColumns(sep);
			}
			// System.out.println(readlines);
			// System.out.println(readcolumns);


			// ������ǰ��ĺö���
			bufread = readfile();
			for (int i = 0; i < rowStartNum - 1; i++) {
				if (bufread.readLine() == null)// ����ı���û����ô����
				{
					return null;
				}
			}
			// ��ʽ��ȡ
			String content = "";
			String[] tmp;// ������ʱ����
			for (int i = 0; i < readlines; i++) {
				if ((content = bufread.readLine()) == null)// ������
				{
					break;
				}
				if (content.trim().equals("")) {
					continue;
				}
				tmp = content.split(sep);
				int tmpLength = tmp.length;
				if (colNotNone > 0 && (tmp[colNotNone - 1] == null || tmp[colNotNone - 1].trim().equals(""))) {
					continue;
				}
				String[] tmpResult = null;
				if (columnEndNum > tmpLength) {
					tmpResult = new String[tmpLength - columnStartNum + 1];
				} else {
					tmpResult = new String[columnEndNum - columnStartNum + 1];
				}
				for (int j = 0; j < tmpResult.length; j++) {
					tmpResult[j] = "";
				}
				for (int j = 0; j < tmpResult.length; j++) {
					int colNum = columnStartNum - 1 + j;
					if (tmp[colNum] == null) {
						tmpResult[j] = "";
					} else {
						tmpResult[j] = tmp[colNum];
					}
				}
				result.add(tmpResult);
			}
		} catch (Exception e) {
			logger.error("read Excel Error: "+ getFileName());
		}
	
		return result;
	}

	
	
	
	/**
	 * ����һ�������ļ��������еĽ������Key-value����
	 * ���һ��Ϊ�գ���Ϊ�ܶ�ո���������������ظ��У�ѡ�����ֵ���
	 * @param chrLenFile
	 * @param keyCase key�Ĵ�Сд�� null ���ı��Сд��false Сд��true��д
	 * @return
	 * û�����򷵻�null
	 */
	public LinkedHashMap<String, String> getKey2Value(String sep, Boolean keyCase) {
		LinkedHashMap<String, String> lkhashResult = new LinkedHashMap<String, String>();
		ArrayList<String> lstmp = readfileLs();
		for (String string : lstmp) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.trim().split("\t");
			if (keyCase != null) {
				 ss[0] = keyCase == true ? ss[0].toUpperCase():ss[0].toLowerCase();  
			}
			if (ss.length < 2) {
				lkhashResult.put(ss[0], "");
			}
			else {
				lkhashResult.put(ss[0], ss[1]);
			}
		}
		if (lkhashResult.size() == 0) {
			return null;
		}
		return lkhashResult;
	}
	
	/**
	 * �����ݰ���excel�ķ���д��string[][],null��""����д�룬���д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @throws Exception
	 */
	public<T> void ExcelWrite(T[][] content, String sep) throws Exception {
		String tmp = "";
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[0].length; j++) {
				if (content[i][j] == null)
					tmp = "";
				else {
					tmp = content[i][j].toString();
				}
				if (j < (content[0].length - 1)) {
					filewriter.write(tmp + sep);
				} else {
					filewriter.write(tmp);
				}
			}
			filewriter.write("\r\n");// ����
		}
		filewriter.flush();// д���ı�
	}

	/**
	 * �����ݰ���excel�ķ���д��string[][],null��""����д�룬���д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @throws Exception
	 */
	public void ExcelWrite(String[][] content, String sep, int rowStart,
			int colStart) throws Exception {

		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[0].length; j++) {
				if (content[i][j] == null)
					content[i][j] = "";
				if (j < (content[0].length - 1)) {
					filewriter.write(content[i][j] + sep);
				} else {
					filewriter.write(content[i][j]);
				}
			}
			filewriter.write("\r\n");// ����
		}
		filewriter.flush();// д���ı�
	}

	/**
	 * �����ݰ���excel�ķ���д��string[],null��""����д��,���д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param row
	 *            trueʱ����д��
	 * @throws Exception
	 */
	public void ExcelWrite(String[] content, boolean row, String sep)
			throws Exception {
		if (row == true)// ����д��
		{
			for (int i = 0; i < content.length; i++) {
				if (content[i] == null)
					content[i] = "";
				if (i < (content.length - 1)) {
					filewriter.write(content[i] + sep);
				} else {
					filewriter.write(content[i]);
				}
			}
			filewriter.write("\r\n");
		} else// ����д��
		{
			for (int i = 0; i < content.length; i++) {
				filewriter.write(content[i] + "\r\n");
			}
		}
		filewriter.flush();// д���ı�
	}

	/**
	 * �����ݰ���excel�ķ���д��List<string[]>,null��""����д�룬���д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ��д����ʼ��
	 * @param columnStartNum
	 *            ʵ��д����ʼ��
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content, String sep,
			int rowStartNum, int columnStartNum) {
		if (content == null || content.size() == 0) {
			return;
		}
		try {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						filewriter.write(content.get(i)[j] + sep);
					} else {
						filewriter.write(content.get(i)[j]);
					}
				}
				filewriter.write("\r\n");// ����
			}
			filewriter.flush();// д���ı�
		} catch (Exception e) {
			logger.error("write list data error:"+getFileName());
			
		}
	
		
		
	}

	/**
	 * �����ݰ���excel�ķ���д��List<string[]>,null��""��дΪ""�����д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param column
	 *            Ҫд��content���ļ��У���0��ʼ����
	 * @param include
	 *            ����column�����Ϊtrue������дcolumn���ļ��У����Ϊfalse����column���Ǽ���ȥ��
	 * @param rowStartNum
	 *            ʵ��д����ʼ��
	 * @param columnStartNum
	 *            ʵ��д����ʼ��
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content, String sep,
			int[] column, boolean include, int rowStartNum, int columnStartNum)
			throws Exception {
		if (include) {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < column.length; j++) {
					if (content.get(i)[column[j]] == null)
						content.get(i)[column[j]] = "";
					if (j < (column.length - 1)) {
						filewriter.write(content.get(i)[column[j]] + sep);
					} else {
						filewriter.write(content.get(i)[column[j]]);
					}
				}
				filewriter.write("\r\n");// ����
			}
			filewriter.flush();// д���ı�
		} else {
			ArrayList<Integer> lscolumn = new ArrayList<Integer>();
			for (int i = 0; i < column.length; i++) {
				lscolumn.add(column[i]);
			}

			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (lscolumn.contains(j)) // ����ȡ��column�е�ĳһ��ʱ������
						continue;
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						filewriter.write(content.get(i)[j] + sep);
					} else {
						filewriter.write(content.get(i)[j]);
					}
				}
				filewriter.write("\r\n");// ����
			}
			filewriter.flush();// д���ı�
		}
	}
	/**
	 * �ر����ļ�
	 */
	public void close() {
		try {
			filewriter.flush();

		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		try {
			fileread.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		try {
			bufread.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		try {
			bufwriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		try {
			filewriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
}
