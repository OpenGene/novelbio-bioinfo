package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
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
	
	public final static String GZIP = "gz";
	public final static String BZIP2 = "bzip2";
	public final static String ZIP = "zip";
	public final static String TXT = "txt";
	private String filetype = TXT;
	
	
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
	InputStream inputStream;
	FileReader fileread;
	OutputStream outputStream;
	BufferedReader bufread;
	BufferedWriter bufwriter;
	boolean createNew = false;
	boolean append = true;
	
	
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
	public boolean setParameter(String fileType, String filepath, boolean createNew,
			boolean append) {
		close();
		this.filetype = fileType;
		txtfile = new File(filepath);
		this.createNew = createNew;
		this.append = append;
		try {
			if (createNew) {
				createFile(fileType, filepath);
				return true;
			}
			else if (txtfile.exists()) {
				getFile(fileType, filepath, append);
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	/**
	 * ���趨ѹ����ʽ
	 * @param filetype
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	
	private void createFile(String fileType, String fileName) throws Exception
	{
		outputStream = new FileOutputStream(txtfile,false);
		if (fileType.equals(TXT)) {
			return;
		}
		
		else if (fileType.equals(ZIP)) {
			ZipArchiveOutputStream filewriterzip = new ZipArchiveOutputStream(txtfile);
			ZipArchiveEntry entry = new ZipArchiveEntry(FileOperate.getFileNameSep(fileName)[0]);

			filewriterzip.putArchiveEntry(entry);
//			filewriterzip.createArchiveEntry(txtfile, FileOperate.getFileNameSep(fileName)[0]+".txt");
			outputStream = filewriterzip;
		}
		else if (fileType.equals(GZIP)) {
			outputStream = new GZIPOutputStream(outputStream);
		}
		else if (fileType.equals(BZIP2)) {
			outputStream = new BZip2CompressorOutputStream(outputStream);
		}
	}
	
	private void getFile(String fileType, String fileName, boolean append) throws Exception
	{
		outputStream = new FileOutputStream(txtfile,append);
		inputStream = new FileInputStream(txtfile);
		fileread = new FileReader(txtfile);
		if (fileType.equals(TXT)) {
			return;
		}
		if (fileType.equals(ZIP)) {
			ZipArchiveOutputStream filewriterzip = new ZipArchiveOutputStream(outputStream);
//			ZipArchiveEntry archiveEntry = new ZipArchiveEntry(name);
//			filewriterzip.putArchiveEntry(archiveEntry);
			outputStream = filewriterzip;
			
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = zipArchiveInputStream;
		}
		else if (fileType.equals(GZIP)) {
			inputStream = new GZIPInputStream(inputStream);
			outputStream = new GZIPOutputStream(outputStream);
		}
		else if (fileType.equals(BZIP2)) {
			inputStream = new BZip2CompressorInputStream(inputStream);
			outputStream = new BZip2CompressorOutputStream(outputStream);
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
		inputStream = new FileInputStream(txtfile);
		if (filetype.equals(ZIP)) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = zipArchiveInputStream;
		}
		else if (filetype.equals(GZIP)) {
			inputStream = new GZIPInputStream(inputStream);
		}
		else if (filetype.equals(BZIP2)) {
			inputStream = new BZip2CompressorInputStream(inputStream);
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
			// TODO Auto-generated catch block
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
			// TODO: handle exception
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
	 * д�벢����
	 * @param content
	 *            ��Ҫд���ļ�����
	 * @throws Exception
	 */
	public void writefileln(String content) {
		try {
			outputStream.write(content.getBytes());
			outputStream.write("\r\n".getBytes());
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
		String content2 = "";
		for (String string : content) {
			content2 = content2 + "\t" + string;
		}
		content2.trim();
		try {
			outputStream.write(content2.getBytes());
			outputStream.write("\r\n".getBytes());
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
			outputStream.write("\r\n".getBytes());
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
					outputStream.write("\r\n".getBytes());
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
	public void writefile(String content, boolean flush) throws Exception {
		outputStream.write(content.getBytes());
		if (flush) {
			outputStream.flush();
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
			outputStream.write((content[i] + "" + sep).getBytes());
			if ((i + 1) % colLen == 0) {
				outputStream.write("\r\n".getBytes());
			}
		}

		outputStream.flush();
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
					outputStream.write("\r\n".getBytes());
				}
			}
			outputStream.flush();
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
		
		close();
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
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write("\r\n".getBytes());
				}
			}

			outputStream.flush();
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
	public<T> void writefile(List<T> lsContent){
		try {
			for (int i = 0; i < lsContent.size(); i++) {
				outputStream.write(lsContent.get(i).toString().getBytes());
				outputStream.write("\r\n".getBytes());
			}
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
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
		if (rowEndNum <= 0) {
			rowEndNum = ExcelRows();
		}
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
					outputStream.write((tmp + sep).getBytes());
				} else {
					outputStream.write(tmp.getBytes());
				}
			}
			outputStream.write("\r\n".getBytes());// ����
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
			outputStream.write("\r\n".getBytes());// ����
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
			outputStream.write("\r\n".getBytes());
		} else// ����д��
		{
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + "\r\n").getBytes());
			}
		}
		outputStream.flush();// д���ı�
	}

	/**
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
				outputStream.write("\r\n".getBytes());// ����
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
				outputStream.write("\r\n".getBytes());// ����
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
				outputStream.write("\r\n".getBytes());// ����
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * �ر����ļ�
	 */
	public void close() {
		try {
			outputStream.flush();
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
			if (filetype.equals(ZIP)) {
				ArchiveOutputStream fileOutputStream = (ArchiveOutputStream) outputStream;
				fileOutputStream.closeArchiveEntry();
			}
		} catch (Exception e) {
		}
		try {
			outputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
}
