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
 * ʹ��ǰ����setParameter���� ʹ����Ϻ����close�ر���
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
	public final static String ENTER_LINUX = "\n";
	public final static String ENTER_WINDOWS = "\r\n";
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
	 * ������
	 * ��ȡѹ���ļ����ļ���ֻ����һ��ѹ���ļ������Ҳ��������ļ���
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
	 * ����Ϊ�����ر�zip��
	 */
	ArchiveOutputStream zipOutputStream;
	
	static int bufferLen = 10000;
	/**
	 * �趨���峤�ȣ�Ĭ��Ϊ10000
	 * @param bufferLen
	 */
	public static void setBufferLen(int bufferLen) {
		TxtReadandWrite.bufferLen = bufferLen;
	}
	
	public String getFileName() {
		return txtfile.getAbsolutePath();
	}
	/**
	 * Ĭ�ϲ���txt�ı�
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
		return setParameter(TXT, filepath, createNew, append);
	}
	/**
	 * ����������趨�������趨������Ϣ������setParameter()
	 * @return
	 */
	public boolean reSetInfo() {
		return setParameter(this.filetype, txtfile.getAbsolutePath(), createNew, append);
	}
	
	/**
	 * @param fileType ѹ����ʽ
	 * @param filepath Ҫ��ȡ��д����ļ���filepath
	 * @param createNew ���ı�������ʱ���Ƿ���Ҫ�½��ı�
	 * @param append �ǽ���д�뻹��д�µġ�<b>��ȡ�ı�ʱ��������Ϊtrue</b>
	 * @return true���ɹ������ı�����<br>
	 *         false��û������ı�����
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
	 * ���趨ѹ����ʽ,û�в���
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
	 * ����ڲ�ʹ�ã��ⲿ��@readlines����
	 * @param path�����ļ���
	 * @return ����BufferedReader���ǵö����Ҫ�ر�Buffer��
	 * @throws Exception
	 */
	@Deprecated
	public BufferedReader readfile() throws Exception {
		if (inputStream != null) {
			inputStream.close();
		}
		if (bufread != null) {
			bufread.close();
		}
		
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
	
	public Iterable<String> readlines() {
		try {
			return readPerlines();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * ������ȡ�ļ�
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
						if (line == null) {
							close();
						}
						return line;
					}
					String line = getLine();
				};
			}
		};
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
			e.printStackTrace();
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
		// ������ǰ��ĺö���
		
	}
	
	/**
	 * @param Num ��ȡǰ���У�ʵ���С�����ı�û����ô���У���ôֻ��ȡ������
	 * @return ���� String�����겻�ùر�Buffer��
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) {
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
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
	public void writefile(String content) {
		try {
			outputStream.write(content.getBytes());
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * д�벢���У�ͨͨû��flush
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln(String content) {
		try {
			outputStream.write((content+ENTER_LINUX).getBytes());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * д��һ�����鲢���У���"\t"����
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln(String[] content) {
		String content2 = content[0];
		for (int i = 1; i < content.length; i++) {
			content2 = content2 + "\t" + content[i];
		}
		try {
			outputStream.write((content2+ENTER_LINUX).getBytes());
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
			outputStream.write(ENTER_LINUX.getBytes());
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
					outputStream.write(ENTER_LINUX.getBytes());
				}
				outputStream.write(mychar[i]);
			}
			outputStream.flush();
		} catch (Exception e) {
		}
	}
	
	/**
	 * @param content
	 *            ��Ҫд���ļ�����,�������Ƿ�ˢ��--Ҳ����ֱ��д���ļ������ǽ��뻺��
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
		
		close();
	}
	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
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
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
	 * 
	 * @param content
	 */
	public void Rwritefile(int[] content) {
		Rwritefile(content, 20, " ");
	}

	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ,Ĭ��ÿ��20��Ԫ�أ��ÿո����
	 * �ڲ�close
	 * @param content
	 */
	public void Rwritefile(String[] content) {
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
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write(ENTER_LINUX.getBytes());
				}
			}
			outputStream.flush();
		} catch (Exception e) {
			logger.error("file error: "+ getFileName());
		}
		close();
	}
	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ
	 * �ڲ�close
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
					outputStream.write(ENTER_LINUX.getBytes());
				}
			}

			outputStream.flush();
		} catch (Exception e) {
			logger.error("file error: "+getFileName());
		}
		close();
	}
	/**
	 * �������ݣ�д���ı������д��Ķ������Ը�R������scan��ȡ
	 * @param content
	 * @param colLen ÿ��д����
	 * @param sep �ָ�����ʲô
	 * @throws Exception
	 */
	private void Rwritefile(double[] content, int colLen, String sep) throws Exception {
		for (int i = 0; i < content.length; i++) {
			outputStream.write((content[i] + "" + sep).getBytes());
			if ((i + 1) % colLen == 0) {
				outputStream.write(ENTER_LINUX.getBytes());
			}
		}
		outputStream.flush();
		close();
	}
	/**
	 * @param lsContent-T ע��Tֻ����string interge�ȼ򵥵���ת��Ϊstring����
	 *            ��Ҫд��List--String�ļ�����,�Զ���ÿһ����ӻ��з�huiche;
	 *            �ڲ�close ��
	 * @throws Exception
	 */
	public<T> void writefile(List<T> lsContent){
		try {
			for (int i = 0; i < lsContent.size(); i++) {
				outputStream.write(lsContent.get(i).toString().getBytes());
				outputStream.write(ENTER_LINUX.getBytes());
			}
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
		close();
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
			
			if (content2.equals("")) {
				close();
				return rowNum - 1;
			}
			else {
				close();
				return rowNum;
			}
		} catch (Exception e) {
			logger.error("excelRows error: "+ getFileName());
			close();
			return -1;
		}
	}

	/**
	 * ����excel������ȡ�ı�ʱʹ�ã����� ����ı���ǰ5000������е�����
	 * @param sep
	 *            ���еķָ�����Ϊ������ʽ��tabΪ"\t"
	 * @return ����ָ���е�����
	 * @throws Exception
	 */
	public int ExcelColumns(String sep){
		int colNum=0;

		int excelRows = 5000; int rowNum = 0;
		for (String tmpstr : readlines()) {
			if (rowNum > excelRows) {
				break;
			}
			rowNum++;
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
			if (readasexcel.readLine() == null) {
				return "";
			}
		}
		// ��ʽ��ȡ
		String content = "";
		String[] tmp;// ������ʱ����
		content = readasexcel.readLine();
		tmp = content.split(sep);
		
		close();
		
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
		BufferedReader readasexcel = readfile();

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
		close();
		return result;
	}

	/**
	 * �ڲ�close
	 * �������txt�ı�����excel�ķ�����ȡ,�Զ���������
	 * ���һ��Ϊ���еĻ��ᱣ��
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param columnStartNum
	 *            ʵ�ʶ�ȡ��ʼ��
	 * @param rowEndNum 
	 *            ʵ�ʶ�ȡ��ֹ�� ,������=-1ʱ����ȡ������
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
		if (columnEndNum <= 0) {
			columnEndNum =	ExcelColumns(sep);
		}
		
		int readlines = rowEndNum - rowStartNum + 1;
		int countRows = 0;
				
		String[] tmp;// ������ʱ����
		for (String content : readlines(rowStartNum)) {
			if (rowEndNum > 0 && countRows > readlines ) {
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
			countRows ++;
		}
		close();
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
	 * ����һ�������ļ��������еĽ������Key-value����,valueΪdouble����
	 * ���һ��Ϊ�գ���Ϊ�ܶ�ո���������������ظ��У�ѡ�����ֵ���
	 * @param chrLenFile
	 * @param keyCase key�Ĵ�Сд�� null ���ı��Сд��false Сд��true��д
	 * @return
	 * û�����򷵻�null
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
					outputStream.write((tmp + sep).getBytes());
				} else {
					outputStream.write(tmp.getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());// ����
		}
		outputStream.flush();// д���ı�
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
					outputStream.write((content[i][j] + sep).getBytes());
				} else {
					outputStream.write(content[i][j].getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());// ����
		}
		outputStream.flush();// д���ı�
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
					outputStream.write((content[i] + sep).getBytes());
				} else {
					outputStream.write(content[i].getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());
		} else// ����д��
		{
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + ENTER_LINUX).getBytes());
			}
		}
		outputStream.flush();// д���ı�
	}
	/**
	 * Ч��̫�ͣ�������
	 * �����ݰ���excel�ķ���д��List<string[]>,null��""����д�룬���д��һ������
	 * �ڲ�close()
	 * @param sep
	 *            txt�ı��ķָ��,Ϊ������ʽ��tab��"\t"
	 * @param rowStartNum
	 *            ʵ��д����ʼ��
	 * @param columnStartNum
	 *            ʵ��д����ʼ��
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content) {
		ExcelWrite(content, "\t", 1, 1);
	}
	/**
	 * Ч��̫�ͣ�������
	 * �����ݰ���excel�ķ���д��List<string[]>,null��""����д�룬���д��һ������
	 * �ڲ�close()
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
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// ����
			}
			outputStream.flush();// д���ı�
		} catch (Exception e) {
			logger.error("write list data error:"+getFileName());
		}
		close();
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
						outputStream.write((content.get(i)[column[j]] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[column[j]].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// ����
			}
			outputStream.flush();// д���ı�
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
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// ����
			}
			outputStream.flush();// д���ı�
		}
	}
	/**
	 * ���txt���ı������ûѹ�������ļ����������ѹ���ˣ��򷵻�OutTxt�Ľ�ѹ���ļ�
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
	 * �趨��ץȡ���ļ��е��ض�����
	 * @param grepContent
	 */
	public void setGrepContent(String grepContent) {
		this.grepContent = grepContent;
	}
	
	/**
	 * ��ȡץȡ��Ϣ�Լ���ǰ���е���Ϣ
	 * @param txtFile
	 * @param zipType
	 * @param grepContent ������������ʽ
	 * @param range
	 * @param regx �Ƿ���������ʽ�������������ʽ��ô�ٶȻ���
	 * @return
	 */
	public ArrayList<String> grepInfo(int range, boolean caseSensitive, boolean regx)
	{
		//�����������ʽ����ô���ȳ�ʼ��������ʽ
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
		 * �洢���string�����string
		 * ��������list�洢�ģ����ǿ���Ч�����⣬������string�������洢
		 * ���α�������ļ�����Ϣ��ѭ������
		 */
		String[] tmpContent = new String[range]; int i = 0;
		//�������Ľ��
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
			String content = "";
			BufferedReader reader = readfile();
			while ((content = reader.readLine()) != null) {
				if (grepInfo(content, caseSensitive, regx)) {
					int num = 0;//����������ǰ��ļ���ȫ������list
					//����ǰ�汣�������
					while (num < range) {
						if (i >= range) {
							i = 0;
						}
						lsResult.add(tmpContent[i]);
						num ++; i++;
					}
					//���뱾������
					lsResult.add(content);
					//�����м���list��Ȼ�����
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
	 * �������֣��Լ��Ƿ��Сд��Ȼ���ǲ��Ǻ�����Ҫ������
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
	 * ����ر�
	 * �ر����ļ�
	 */
	public void close() {
		try { outputStream.flush(); } catch (Exception e) {}
		try { fileread.close(); } catch (Exception e) {}
		try { bufread.close(); } catch (Exception e) {}
		try { bufwriter.close(); } catch (Exception e) {}
		try { inputStream.close(); } catch (Exception e) {}
		try { outputStream.close(); } catch (Exception e) {}
		try {
			if (filetype.equals(ZIP)) {
				zipOutputStream.closeArchiveEntry();
			}
		} catch (Exception e) { }
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
////���ļ���
 class TestCountWords {  
       public static void main(String[] args) {  
           File wf = new File("words.txt");  
           final CountWords cw1 = new CountWords(wf, 0, wf.length()/2);  
           final CountWords cw2 = new CountWords(wf, wf.length()/2, wf.length());  
           final Thread t1 = new Thread(cw1);  
           final Thread t2 = new Thread(cw2);  
           //���������̷ֱ߳����ļ��Ĳ�ͬƬ��  
           t1.start();  
           t2.start();  
           Thread t = new Thread() {  
               public void run() {  
                   while(true) {  
                       //�����߳̾����н���  
                       if(Thread.State.TERMINATED==t1.getState() && Thread.State.TERMINATED==t2.getState()) {  
                           //��ȡ���Դ���Ľ��  
                           HashMap<String, Integer> hMap1 = cw1.getResult();  
                           HashMap<String, Integer> hMap2 = cw2.getResult();  
                           //ʹ��TreeMap��֤�������  
                           TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();  
                           //�Բ�ͬ�̴߳���Ľ����������  
                           tMap.putAll(hMap1);  
                           tMap.putAll(hMap2);  
                           //��ӡ������鿴���  
                           for(Entry<String, Integer> entry : tMap.entrySet()) {  
                               String key = entry.getKey();    
                               int value = entry.getValue();    
                               System.out.println(key+":\t"+value);    
                           }  
                           //��������浽�ļ���  
                           mapToFile(tMap, new File("result.txt"));  
                       }  
                       return;  
                   }  
               }  
           };  
           t.start();  
       }  
       //��������� "���ʣ�����" ��ʽ�����ļ���  
       private static void mapToFile(Map<String, Integer> src, File dst) {  
           try {  
               //�Խ�Ҫд����ļ�����ͨ��  
               FileChannel fcout = new FileOutputStream(dst).getChannel();  
               //ʹ��entrySet�Խ�������б���  
               for(Map.Entry<String,Integer> entry : src.entrySet()) {  
                   String key = entry.getKey();  
                   int value = entry.getValue();  
                   //���������ָ����ʽ�ŵ���������  
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
               //�õ���ǰ�ļ���ͨ��  
               fc = new RandomAccessFile(src, "rw").getChannel();  
               //������ǰ�ļ��Ĳ���  
               fl = fc.lock(start, end, false);  
               //�Ե�ǰ�ļ�Ƭ�ν����ڴ�ӳ�䣬����ļ�������Ҫ�и�ɶ��Ƭ��  
               mbBuf = fc.map(FileChannel.MapMode.READ_ONLY, start, end);  
               //����HashMapʵ����Ŵ�����  
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
           //ʹ��StringTokenizer��������  
           StringTokenizer token = new StringTokenizer(str);  
           String word;  
           while(token.hasMoreTokens()) {  
               //���������ŵ�һ��HashMap�У����ǵ��洢�ٶ�  
               word = token.nextToken();  
               if(null != hm.get(word)) {  
                   hm.put(word, hm.get(word)+1);  
               } else {  
                   hm.put(word, 1);  
               }  
           }  
           try {  
               //�ͷ��ļ���  
               fl.release();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
           return;  
       }  
         
       //��ȡ��ǰ�̵߳�ִ�н��  
       public HashMap<String, Integer> getResult() {  
           return hm;  
       }
}