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
 * 使用前先用setParameter设置
 * 使用完毕后调用close关闭流
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
	 *            要读取或写入的文件名filepath
	 * @param createNew
	 *            当文本不存在时，是否需要新建文本
	 * @param append
	 *            是接着写入还是写新的。<b>读取文本时必须设置为true</b>
	 * @return true：成功设置文本参数<br>
	 *         false：没有设好文本参数
	 */
	public boolean setParameter(String filepath, boolean createNew,
			boolean append) {
		txtfile = new File(filepath);
		if (txtfile.exists() == false) {
			if (createNew)// 如果文本文件不存在则创建它
			{
				try {
					txtfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				txtfile = new File(filepath); // 重新实例化
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
	 * @param path输入文件名
	 * @return 返回BufferedReader，记得读完后要关闭Buffer流
	 * @throws Exception
	 */
	public BufferedReader readfile() throws Exception {
		fileread = new FileReader(txtfile);
		bufread = new BufferedReader(fileread);
		return bufread;
	}
	/**
	 * @param path输入文件名
	 * @return 返回List<String>，读完不用关闭Buffer流
	 * @throws Exception
	 */
	public ArrayList<String> readfileLs(){

		ArrayList<String> lsResult = new ArrayList<String>();
		String content = "";
		try {
			BufferedReader read = readfile();
			// 先跳过前面的好多行
			while ((content = read.readLine()) != null) {
				lsResult.add(content);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return lsResult;
	}
	
	/**
	 * 去除空格后文件的字符长度，不是文件大小，而是含有多少文字
	 * @return
	 */
	public long getTxtLen() {
		int Result = 0;
		String content = "";
		// 先跳过前面的好多行
		try {
			BufferedReader read = readfile();
			while ((content = read.readLine()) != null) {
				Result = Result + content.trim().length();
			}
		} catch (Exception e) {
			logger.error("读取出错");
			e.printStackTrace();
		}
		return Result;
	}

	/**
	 * @return 返回 String，读完不用关闭Buffer流
	 * @throws Exception
	 */
	public String readFirstLine() throws Exception {
		BufferedReader read = readfile();
		// 先跳过前面的好多行
		return  read.readLine();
	}
	
	/**
	 * @param Num 读取前几列，实际列。如果文本没有那么多列，那么只读取所有列
	 * @return 返回 String，读完不用关闭Buffer流
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) throws Exception {
		ArrayList<String> lsResult = new ArrayList<String>();
		BufferedReader read = readfile();
		String content = ""; int rownum = 1;
		// 先跳过前面的好多行
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
	 * 关闭buffer流
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
	 *            ，要写入文件内容
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
	 * 写入并换行
	 * @param content
	 *            ，要写入文件内容
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
	 * 写入并换行
	 * @param content
	 *            ，要写入文件内容
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
	 * 写入并将写入的序列换行，目前只能写入ascII文本
	 * @param content 输入的string，是没有换行的那种
	 * @param length 每多少行进行换行
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
	 *            ，要写入文件内容,并考虑是否刷新--也就是直接写入文件而不是进入缓存
	 * @throws Exception
	 */
	public void writefile(String content, boolean flush) throws Exception {
		filewriter.write(content);
		if (flush) {
			filewriter.flush();
		}
	}

	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
	 * 
	 * @param content
	 * @param colLen 每行写几个
	 * @param sep 分隔符是什么
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
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(double[] content) throws Exception {
		Rwritefile(content, 20, " ");
	}

	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
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
	 * 指定正则表达式，将文本中含有该正则表达式的行全部删除
	 * @param regx
	 */
	public void delLines(String regx, boolean isregx) {
		String tmpFileName = txtfile.getAbsolutePath()+"TmpOfZJJAVA";
		TxtReadandWrite txtNewFile = new TxtReadandWrite(tmpFileName,true);
		Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
		Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
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
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(int[] content) {
		Rwritefile(content, 20, " ");
	}

	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
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
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(String[] content) {
		Rwritefile(content, 20, " ");
	}

	/**
	 * @param lsContent-T 注意T只能是string interge等简单的能转化为string的类
	 *            ，要写入List--String文件内容,自动在每一行添加换行符"\r\n";
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
	 * 按照excel方法读取文本时使用，用于 获得txt文本的行数，如果最后一行是""，则忽略最后一行
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
	 * 按照excel方法读取文本时使用，用于 获得文本中最长行的列数，效率似乎有点低下
	 * 
	 * @param setRow
	 *            指定行数，如果指定行超过文本最大行，则将指定行设为最大行。
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
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
	 * 按照excel方法读取文本时使用，用于 获得txt文本指定行的列数
	 * 
	 * @param setRow
	 *            指定行数，为实际行数，如果指定行超过文本最大行，则将指定行设为最大行。
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
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
	 * 将规则的txt文本按照excel的方法读取
	 * 
	 * @param sep
	 *            txt文本的分割符
	 * @param rowNum
	 *            实际读取行
	 * @param columnNum
	 *            实际读取列
	 * @return 返回string,单个值,如果值为null则返回""
	 * @throws Exception
	 */
	public String ExcelRead(String sep, int rowNum, int columnNum)
			throws Exception {
		BufferedReader readasexcel = readfile();
		// 先跳过前面的好多行
		for (int i = 0; i < rowNum - 1; i++) {
			if (readasexcel.readLine() == null)// 如果文本中没有那么多行
			{
				return "";
			}
		}
		// 正式读取
		String content = "";
		String[] tmp;// 两个临时变量
		content = readasexcel.readLine();
		tmp = content.split(sep);
		if (tmp.length < columnNum)
			return "";
		return tmp[columnNum - 1];
	}

	/**
	 * 将规则的txt文本按照excel的方法读取
	 * 最后一行即使没东西也会用""表示
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum
	 *            实际读取终止行
	 * @param columnEndNum
	 *            实际读取终止列
	 * @return 返回string[] 数组,数组中null项用""替换
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

		// 先跳过前面的好多行
		for (int i = 0; i < rowStartNum - 1; i++) {
			if (readasexcel.readLine() == null)// 如果文本中没有那么多行
			{
				return null;
			}
		}
		// 正式读取
		String content = "";
		String[] tmp;// 两个临时变量
		for (int i = 0; i < readlines; i++) {
			if ((content = readasexcel.readLine()) == null)// 读完了
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
		for (int i = 0; i < result.length; i++)// 将所有为null的项通通赋值为""
		{
			for (int j = 0; j < result[0].length; j++) {
				if (result[i][j] == null)
					result[i][j] = "";
			}
		}
		return result;
	}

	/**
	 * 将规则的txt文本按照excel的方法读取,自动跳过空行
	 * 最后一行为空行的话会保留
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum
	 *            实际读取终止行
	 * @param columnEndNum
	 *            实际读取终止列,当该项=-1时，读取所有列，反正是ArrayList--String[]嘛<br>
	 *            如果该项大于最大列，那么就把本行都读取了
	 * @param colNotNone
	 *            主键列，该列不能为""，否则把该列为""的行删除，如果本项<=0，则不考虑
	 * @return 返回ArrayList<String[]> 数组,数组中null项用""替换
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


			// 先跳过前面的好多行
			bufread = readfile();
			for (int i = 0; i < rowStartNum - 1; i++) {
				if (bufread.readLine() == null)// 如果文本中没有那么多行
				{
					return null;
				}
			}
			// 正式读取
			String content = "";
			String[] tmp;// 两个临时变量
			for (int i = 0; i < readlines; i++) {
				if ((content = bufread.readLine()) == null)// 读完了
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
	 * 给定一个两列文件，将其中的结果按照Key-value导出
	 * 如果一列为空，如为很多空格，则跳过，如果有重复列，选择后出现的列
	 * @param chrLenFile
	 * @param keyCase key的大小写。 null 不改变大小写，false 小写，true大写
	 * @return
	 * 没东西则返回null
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
	 * 将数据按照excel的方法写入string[][],null和""都不写入，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
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
			filewriter.write("\r\n");// 换行
		}
		filewriter.flush();// 写入文本
	}

	/**
	 * 将数据按照excel的方法写入string[][],null和""都不写入，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
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
			filewriter.write("\r\n");// 换行
		}
		filewriter.flush();// 写入文本
	}

	/**
	 * 将数据按照excel的方法写入string[],null和""都不写入,最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param row
	 *            true时按行写入
	 * @throws Exception
	 */
	public void ExcelWrite(String[] content, boolean row, String sep)
			throws Exception {
		if (row == true)// 横着写入
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
		} else// 竖着写入
		{
			for (int i = 0; i < content.length; i++) {
				filewriter.write(content[i] + "\r\n");
			}
		}
		filewriter.flush();// 写入文本
	}

	/**
	 * 将数据按照excel的方法写入List<string[]>,null和""都不写入，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际写入起始行
	 * @param columnStartNum
	 *            实际写入起始列
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
				filewriter.write("\r\n");// 换行
			}
			filewriter.flush();// 写入文本
		} catch (Exception e) {
			logger.error("write list data error:"+getFileName());
			
		}
	
		
		
	}

	/**
	 * 将数据按照excel的方法写入List<string[]>,null和""都写为""，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param column
	 *            要写入content的哪几列，从0开始记数
	 * @param include
	 *            设置column，如果为true，仅仅写column的哪几列，如果为false，则将column的那几列去除
	 * @param rowStartNum
	 *            实际写入起始行
	 * @param columnStartNum
	 *            实际写入起始列
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
				filewriter.write("\r\n");// 换行
			}
			filewriter.flush();// 写入文本
		} else {
			ArrayList<Integer> lscolumn = new ArrayList<Integer>();
			for (int i = 0; i < column.length; i++) {
				lscolumn.add(column[i]);
			}

			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (lscolumn.contains(j)) // 当读取到column中的某一列时，跳过
						continue;
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						filewriter.write(content.get(i)[j] + sep);
					} else {
						filewriter.write(content.get(i)[j]);
					}
				}
				filewriter.write("\r\n");// 换行
			}
			filewriter.flush();// 写入文本
		}
	}
	/**
	 * 关闭流文件
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
