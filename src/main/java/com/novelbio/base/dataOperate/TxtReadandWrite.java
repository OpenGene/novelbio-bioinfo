package com.novelbio.base.dataOperate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

/**
 * 使用前先用setParameter设置 使用完毕后调用close关闭流
 * 
 * @author zong0jie
 * 
 */
public class TxtReadandWrite {
	private static Logger logger = Logger.getLogger(TxtReadandWrite.class);
	
	public final static String GZIP = "gz";
	public final static String BZIP2 = "bzip2";
	public final static String ZIP = "zip";
	public final static String TXT = "txt";
	private String filetype = TXT;
	public final static String huiche = "\n";
	
	@Deprecated
	public TxtReadandWrite () {
		
	}
	public TxtReadandWrite (String fileType, String filepath, boolean createNew) {
		if (createNew) {
			setParameter(fileType, filepath, createNew, false);
		}
		else {
			setParameter(fileType, filepath, createNew, true);
		}
	}
	
	public TxtReadandWrite (String filepath, boolean createNew) {
		if (createNew) {
			setParameter(filepath, createNew, false);
		}
		else {
			setParameter(filepath, createNew, true);
		}
	}
	/**
	 * 待测试
	 * 读取压缩文件，文件中只能有一个压缩文件，并且不能是子文件夹
	 * @param zip
	 * @param filePath
	 */
	public TxtReadandWrite (String fileType, String filePath) {
		this.filetype = fileType;
		setParameter(fileType, filePath, false, true);
	}
	
	
	File txtfile;
	BufferedInputStream inputStream;
	FileReader fileread;
	BufferedOutputStream outputStream;
	BufferedReader bufread;
	BufferedWriter bufwriter;
	boolean createNew = false;
	boolean append = true;
	/**
	 * 仅仅为了最后关闭zip用
	 */
	ArchiveOutputStream zipOutputStream;
	
	static int bufferLen = 10000;
	/**
	 * 设定缓冲长度，默认为10000
	 * @param bufferLen
	 */
	public static void setBufferLen(int bufferLen) {
		TxtReadandWrite.bufferLen = bufferLen;
	}
	
	public String getFileName() {
		return txtfile.getAbsolutePath();
	}
	/**
	 * 默认产生txt文本
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
		return setParameter(TXT, filepath, createNew, append);
	}
	/**
	 * 按照最初的设定，重新设定各类信息，类似setParameter()
	 * @return
	 */
	public boolean reSetInfo() {
		return setParameter(this.filetype, txtfile.getAbsolutePath(), createNew, append);
	}
	
	/**
	 * @param fileType 压缩格式
	 * @param filepath 要读取或写入的文件名filepath
	 * @param createNew 当文本不存在时，是否需要新建文本
	 * @param append 是接着写入还是写新的。<b>读取文本时必须设置为true</b>
	 * @return true：成功设置文本参数<br>
	 *         false：没有设好文本参数
	 */
	public boolean setParameter(String fileType, String filepath, boolean createNew, boolean append) {
		close();
		this.filetype = fileType;
		txtfile = new File(filepath);
		this.createNew = createNew;
		this.append = append;
		try {
			if (createNew) {
				createFile(fileType, filepath, append);
				return true;
			}
			else if (txtfile.exists()) {
				try { createFile(filetype, txtfile.getAbsolutePath(), append); } catch (Exception e) { }
				try { setReadFile(filetype); } catch (Exception e) { }
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 仅设定压缩格式,没有测试
	 * @param filetype
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
		try { createFile(filetype, txtfile.getAbsolutePath(), this.append); } catch (Exception e) {}
		try {
			setReadFile(filetype);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void createFile(String fileType, String fileName, boolean append) throws Exception
	{
		outputStream = new BufferedOutputStream(new FileOutputStream(txtfile,append));
		if (fileType.equals(TXT)) {
			return;
		}
		
		else if (fileType.equals(ZIP)) {
			zipOutputStream = new ZipArchiveOutputStream(txtfile);
			ZipArchiveEntry entry = new ZipArchiveEntry(FileOperate.getFileNameSep(fileName)[0]);

			zipOutputStream.putArchiveEntry(entry);
//			filewriterzip.createArchiveEntry(txtfile, FileOperate.getFileNameSep(fileName)[0]+".txt");
			outputStream = new BufferedOutputStream(zipOutputStream, bufferLen);
		}
		else if (fileType.equals(GZIP)) {
			outputStream = new BufferedOutputStream(new GZIPOutputStream(outputStream), bufferLen);
		}
		else if (fileType.equals(BZIP2)) {
			outputStream = new BufferedOutputStream(new BZip2CompressorOutputStream(outputStream), bufferLen);
		}
	}
	
	private void setReadFile(String fileType) throws Exception {
		inputStream = new BufferedInputStream(new FileInputStream(txtfile), bufferLen);
		fileread = new FileReader(txtfile);
		if (fileType.equals(TXT)) {
			return;
		}
		if (fileType.equals(ZIP)) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = new BufferedInputStream(zipArchiveInputStream, bufferLen);
		}
		else if (fileType.equals(GZIP)) {
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStream), bufferLen);
		}
		else if (fileType.equals(BZIP2)) {
			inputStream = new BufferedInputStream(new BZip2CompressorInputStream(inputStream), bufferLen);
		}
	}
	
	/**
	 * 这个内部使用，外部用@readlines代替
	 * @param path输入文件名
	 * @return 返回BufferedReader，记得读完后要关闭Buffer流
	 * @throws Exception
	 */
	@Deprecated
	public BufferedReader readfile() throws Exception {
		inputStream = new BufferedInputStream(new FileInputStream(txtfile), bufferLen);
		if (filetype.equals(ZIP)) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = new BufferedInputStream(zipArchiveInputStream, bufferLen);
		}
		else if (filetype.equals(GZIP)) {
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStream), bufferLen);
		}
		else if (filetype.equals(BZIP2)) {
			inputStream = new BufferedInputStream(new BZip2CompressorInputStream(inputStream), bufferLen);
		}
		bufread = new BufferedReader(new   InputStreamReader(inputStream));
		return bufread;
	}
	
	public Iterable<String> readlines()
	{
		try {
			return readPerlines();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<String> readlines(int lines)
	{
		lines = lines - 1;
		try {
			Iterable<String> itContent = readPerlines();
			if (lines > 0) {
				for (int i = 0; i < lines; i++) {
					itContent.iterator().hasNext();
				}
			}
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<String> readPerlines() throws Exception {
		 final BufferedReader bufread =  readfile(); 
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}

					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					String getLine() {
						String line = null;
						try {
							line = bufread.readLine();
						} catch (IOException ioEx) {
							line = null;
						}
						return line;
					}
					String line = getLine();
				};
			}
		};
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
			e.printStackTrace();
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
	 * 
	 */
	public String readFirstLine() {
		BufferedReader read;
		try {
			read = readfile();
			String str = read.readLine();
			close();
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// 先跳过前面的好多行
		
	}
	
	/**
	 * @param Num 读取前几列，实际列。如果文本没有那么多列，那么只读取所有列
	 * @return 返回 String，读完不用关闭Buffer流
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) {
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
			outputStream.write(content.getBytes());
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * 写入并换行，通通没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String content) {
		try {
			outputStream.write((content+huiche).getBytes());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * 写入一行数组并换行，用"\t"隔开
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String[] content) {
		String content2 = content[0];
		for (int i = 1; i < content.length; i++) {
			content2 = content2 + "\t" + content[i];
		}
		try {
			outputStream.write((content2+huiche).getBytes());
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
			outputStream.write(huiche.getBytes());
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
					outputStream.write(huiche.getBytes());
				}
				outputStream.write(mychar[i]);
			}
			outputStream.flush();
		} catch (Exception e) {
		}
	}
	
	/**
	 * @param content
	 *            ，要写入文件内容,并考虑是否刷新--也就是直接写入文件而不是进入缓存
	 * @throws Exception
	 */
	public void writefile(String content, boolean flush) {
		try {
			outputStream.write(content.getBytes());
			if (flush) {
				outputStream.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private void Rwritefile(double[] content, int colLen, String sep) throws Exception {
		for (int i = 0; i < content.length; i++) {
			outputStream.write((content[i] + "" + sep).getBytes());
			if ((i + 1) % colLen == 0) {
				outputStream.write(huiche.getBytes());
			}
		}

		outputStream.flush();
	}

	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(double[] content) {
		try {
			Rwritefile(content, 20, " ");
		} catch (Exception e) {
			// TODO: handle exception
		}
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
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write(huiche.getBytes());
				}
			}
			outputStream.flush();
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
		
		close();
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
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write(huiche.getBytes());
				}
			}

			outputStream.flush();
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
	 *            ，要写入List--String文件内容,自动在每一行添加换行符huiche;
	 *            内部close 流
	 * @throws Exception
	 */
	public<T> void writefile(List<T> lsContent){
		try {
			for (int i = 0; i < lsContent.size(); i++) {
				outputStream.write(lsContent.get(i).toString().getBytes());
				outputStream.write(huiche.getBytes());
			}
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
		close();
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
		if (rowEndNum <= 0) {
			rowEndNum = ExcelRows();
		}
		if (rowStartNum < 0) {
			rowStartNum = 1;
		}
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
	 * 内部close
	 * 将规则的txt文本按照excel的方法读取,自动跳过空行
	 * 最后一行为空行的话会保留
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum 
	 *            实际读取终止行 ,当该项=-1时，读取所有行
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
			if (columnEndNum <= 0) {
				columnEndNum =	ExcelColumns(sep);
			}
			if (rowEndNum <= 0) {
				rowEndNum = ExcelRows();
			}
			int readlines = rowEndNum - rowStartNum + 1;
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
			close();
		}
		close();
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
	 * 给定一个两列文件，将其中的结果按照Key-value导出,value为double类型
	 * 如果一列为空，如为很多空格，则跳过，如果有重复列，选择后出现的列
	 * @param chrLenFile
	 * @param keyCase key的大小写。 null 不改变大小写，false 小写，true大写
	 * @return
	 * 没东西则返回null
	 */
	public LinkedHashMap<String, Double> getKey2ValueDouble(String sep, Boolean keyCase) {
		LinkedHashMap<String, Double> lkhashResult = new LinkedHashMap<String, Double>();
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
				lkhashResult.put(ss[0], 0.0);
			}
			else {
				lkhashResult.put(ss[0], Double.parseDouble(ss[1]));
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
					outputStream.write((tmp + sep).getBytes());
				} else {
					outputStream.write(tmp.getBytes());
				}
			}
			outputStream.write(huiche.getBytes());// 换行
		}
		outputStream.flush();// 写入文本
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
					outputStream.write((content[i][j] + sep).getBytes());
				} else {
					outputStream.write(content[i][j].getBytes());
				}
			}
			outputStream.write(huiche.getBytes());// 换行
		}
		outputStream.flush();// 写入文本
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
					outputStream.write((content[i] + sep).getBytes());
				} else {
					outputStream.write(content[i].getBytes());
				}
			}
			outputStream.write(huiche.getBytes());
		} else// 竖着写入
		{
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + huiche).getBytes());
			}
		}
		outputStream.flush();// 写入文本
	}

	/**
	 * 效率太低，待修正
	 * 将数据按照excel的方法写入List<string[]>,null和""都不写入，最后写入一个换行
	 * 内部close()
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
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(huiche.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		} catch (Exception e) {
			logger.error("write list data error:"+getFileName());
		}
		close();
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
						outputStream.write((content.get(i)[column[j]] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[column[j]].getBytes());
					}
				}
				outputStream.write(huiche.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
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
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(huiche.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		}
	}
	/**
	 * 获得txt的文本，如果没压缩，则将文件改名，如果压缩了，则返回OutTxt的解压缩文件
	 * @param OutTxt
	 */
	public void unZipFile(String OutTxt)
	{
		if (this.filetype.equals(TXT)) {
			FileOperate.moveFile(txtfile.getAbsolutePath(), FileOperate.getParentPathName(OutTxt), FileOperate.getFileName(OutTxt), true);
			return;
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(OutTxt, true);
		for (String string : readlines()) {
			txtOut.writefileln(string);
		}
		close();
		txtOut.close();
	}
	
	String grepContent = "";
	Pattern pattern = null;
	Matcher matcher = null;
	/**
	 * 设定待抓取本文件中的特定文字
	 * @param grepContent
	 */
	public void setGrepContent(String grepContent) {
		this.grepContent = grepContent;
	}
	
	/**
	 * 获取抓取信息以及其前后几行的信息
	 * @param txtFile
	 * @param zipType
	 * @param grepContent 可以是正则表达式
	 * @param range
	 * @param regx 是否是正则表达式，如果是正则表达式那么速度会慢
	 * @return
	 */
	public ArrayList<String> grepInfo(int range, boolean caseSensitive, boolean regx)
	{
		//如果是正则表达式，那么就先初始化正则表达式
		if (regx) {
			if (caseSensitive) {
				pattern = Pattern.compile(grepContent);
			}
			else {
				pattern = Pattern.compile(grepContent, Pattern.CASE_INSENSITIVE);
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		/**
		 * 存储获得string上面的string
		 * 本来想用list存储的，但是考虑效率问题，所以用string数组来存储
		 * 依次保存上面的几行信息，循环保存
		 */
		String[] tmpContent = new String[range]; int i = 0;
		//保存最后的结果
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
			String content = "";
			BufferedReader reader = readfile();
			while ((content = reader.readLine()) != null) {
				if (grepInfo(content, caseSensitive, regx)) {
					int num = 0;//计数器，将前面的几行全部加入list
					//加入前面保存的文字
					while (num < range) {
						if (i >= range) {
							i = 0;
						}
						lsResult.add(tmpContent[i]);
						num ++; i++;
					}
					//加入本行文字
					lsResult.add(content);
					//将后几行加入list，然后结束
					int rest = 0;
					while ((content = reader.readLine()) != null) {
						if (rest >= range) {
							close();
							return lsResult;
						}
						lsResult.add(content);
						rest++;
					}
					close();
					return lsResult;
				}
				tmpContent[i] = content; i++;
				if (i >= range) {
					i = 0;
				}
			}
		} catch (Exception e) { e.printStackTrace(); 	}
		close();
		return null;
	}
	/**
	 * 给定文字，以及是否大小写，然后看是不是含有需要的文字
	 * @param content
	 * @param caseSensitive
	 * @param regx
	 * @return
	 */
	private boolean grepInfo(String content, boolean caseSensitive, boolean regx)
	{
		if (!regx) {
			if (!caseSensitive)
				if (content.toLowerCase().contains(grepContent))
					return true;
			else
				if (content.contains(grepContent)) 
					return true;
		}
		else {
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 必须关闭
	 * 关闭流文件
	 */
	public void close() {
		try {
			outputStream.flush();
		} catch (Exception e) {
		}
		try {
			fileread.close();
		} catch (Exception e) {
		}
		try {
			bufread.close();
		} catch (Exception e) {
		}
		try {
			bufwriter.close();
		} catch (Exception e) {
		}
		try {
			if (filetype.equals(ZIP)) {
				zipOutputStream.closeArchiveEntry();
			}
		} catch (Exception e) {
		}
		try {
			outputStream.close();
		} catch (Exception e) {
		}
	}
	   protected void finalize() {
         close();
         try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

       }
}
////大文件排
 class TestCountWords {  
       public static void main(String[] args) {  
           File wf = new File("words.txt");  
           final CountWords cw1 = new CountWords(wf, 0, wf.length()/2);  
           final CountWords cw2 = new CountWords(wf, wf.length()/2, wf.length());  
           final Thread t1 = new Thread(cw1);  
           final Thread t2 = new Thread(cw2);  
           //开辟两个线程分别处理文件的不同片段  
           t1.start();  
           t2.start();  
           Thread t = new Thread() {  
               public void run() {  
                   while(true) {  
                       //两个线程均运行结束  
                       if(Thread.State.TERMINATED==t1.getState() && Thread.State.TERMINATED==t2.getState()) {  
                           //获取各自处理的结果  
                           HashMap<String, Integer> hMap1 = cw1.getResult();  
                           HashMap<String, Integer> hMap2 = cw2.getResult();  
                           //使用TreeMap保证结果有序  
                           TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();  
                           //对不同线程处理的结果进行整合  
                           tMap.putAll(hMap1);  
                           tMap.putAll(hMap2);  
                           //打印输出，查看结果  
                           for(Entry<String, Integer> entry : tMap.entrySet()) {  
                               String key = entry.getKey();    
                               int value = entry.getValue();    
                               System.out.println(key+":\t"+value);    
                           }  
                           //将结果保存到文件中  
                           mapToFile(tMap, new File("result.txt"));  
                       }  
                       return;  
                   }  
               }  
           };  
           t.start();  
       }  
       //将结果按照 "单词：次数" 格式存在文件中  
       private static void mapToFile(Map<String, Integer> src, File dst) {  
           try {  
               //对将要写入的文件建立通道  
               FileChannel fcout = new FileOutputStream(dst).getChannel();  
               //使用entrySet对结果集进行遍历  
               for(Map.Entry<String,Integer> entry : src.entrySet()) {  
                   String key = entry.getKey();  
                   int value = entry.getValue();  
                   //将结果按照指定格式放到缓冲区中  
                   ByteBuffer bBuf = ByteBuffer.wrap((key+":\t"+value).getBytes());  
                   fcout.write(bBuf);  
                   bBuf.clear();  
               }  
           } catch (FileNotFoundException e) {  
               e.printStackTrace();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
       }  
   }  
     
   class CountWords implements Runnable {  
         
       private FileChannel fc;  
       private FileLock fl;  
       private MappedByteBuffer mbBuf;  
       private HashMap<String, Integer> hm;  
         
       public CountWords(File src, long start, long end) {  
           try {  
               //得到当前文件的通道  
               fc = new RandomAccessFile(src, "rw").getChannel();  
               //锁定当前文件的部分  
               fl = fc.lock(start, end, false);  
               //对当前文件片段建立内存映射，如果文件过大需要切割成多个片段  
               mbBuf = fc.map(FileChannel.MapMode.READ_ONLY, start, end);  
               //创建HashMap实例存放处理结果  
               hm = new HashMap<String,Integer>();  
           } catch (FileNotFoundException e) {  
               e.printStackTrace();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
       }  
       @Override  
       public void run() {  
           String str = Charset.forName("UTF-8").decode(mbBuf).toString();  
           //使用StringTokenizer分析单词  
           StringTokenizer token = new StringTokenizer(str);  
           String word;  
           while(token.hasMoreTokens()) {  
               //将处理结果放到一个HashMap中，考虑到存储速度  
               word = token.nextToken();  
               if(null != hm.get(word)) {  
                   hm.put(word, hm.get(word)+1);  
               } else {  
                   hm.put(word, 1);  
               }  
           }  
           try {  
               //释放文件锁  
               fl.release();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
           return;  
       }  
         
       //获取当前线程的执行结果  
       public HashMap<String, Integer> getResult() {  
           return hm;  
       }
}