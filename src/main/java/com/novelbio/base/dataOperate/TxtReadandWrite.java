package com.novelbio.base.dataOperate;

import info.monitorenter.cpdetector.CharsetPrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


import com.google.common.io.Files;
/**
 * ʹ��ǰ����setParameter����
 * ʹ����Ϻ����close�ر���
 * @author zong0jie
 *
 */
public class TxtReadandWrite {

	File txtfile;
	FileReader fileread;
	FileWriter filewriter;
	BufferedReader bufread;
	BufferedWriter bufwriter;
	String filepath;// �õ��ı��ļ���·��

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
	public ArrayList<String> readfileLs() throws Exception {
		ArrayList<String> lsResult = new ArrayList<String>();
		BufferedReader read = readfile();
		String content = "";
		// ������ǰ��ĺö���
		while ((content = read.readLine()) != null) {
			lsResult.add(content);
		}
		return lsResult;
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
	public void writefile(String content) throws Exception {
		filewriter.write(content);
		filewriter.flush();
	}
	/**
	 * д�벢����
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln(String content) throws Exception {
		filewriter.write(content+"\n");
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
	private void Rwritefile(int[] content, int colLen, String sep)
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
	public void Rwritefile(int[] content) throws Exception {
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
	private void Rwritefile(String[] content, int colLen, String sep)
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
	public void Rwritefile(String[] content) throws Exception {
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
	public int ExcelRows() throws Exception {
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
	public int ExcelColumns(String sep) throws Exception {
		int excelRows = ExcelRows();
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
	}

	/**
	 * ����excel������ȡ�ı�ʱʹ�ã����� ���txt�ı�ָ���е�����
	 * 
	 * @param setRow
	 *            ָ�����������ָ���г����ı�����У���ָ������Ϊ����С�
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
			throws Exception {
		
		int readlines = rowEndNum - rowStartNum + 1;
		if (columnEndNum<0) {
			columnEndNum =	ExcelColumns(sep);
		}
		// System.out.println(readlines);
		// System.out.println(readcolumns);

		ArrayList<String[]> result = new ArrayList<String[]>();

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
			if (colNotNone > 0
					&& (tmp[colNotNone - 1] == null || tmp[colNotNone - 1]
							.trim().equals(""))) {
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
		return result;
	}

	/**
	 * �����ݰ���excel�ķ���д��string[][],null��""����д�룬���д��һ������
	 * 
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @throws Exception
	 */
	public void ExcelWrite(String[][] content, String sep) throws Exception {
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
			int rowStartNum, int columnStartNum) throws Exception {
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
