package com.novelbio.base.dataOperate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.novelbio.base.fileOperate.FileOperate;

/**
 * ������Ҫʵ��������ʹ��
 * ��ȡexcel�ļ�,ע���ȡǰ��ý�excel�����и�ʽȫ���������Ȼ���ܻ������⡣
 * �����������excel������txt�ļ����ٿ�������
 * ���Կ�ƽ̨ʹ��
 * ��ȡ�ٶȿ���	
 * ��ȡ�鷵�ص���һ����ά���飬Ȼ�������ά����������[0][0]����ͬ��C#�е�[1,1]
 * �����ƺ��޷��������е���Ŀ,������Կ��ǲ���һά��Ŀ��ͬ�Ķ�ά���飬���ʵ�ֿ��Կ�����foreach������
 * ������ԭʼ���� caihua ��Zong Jie�޸�
 */
public class ExcelOperate {   
	public static final int EXCEL2003 = 2003;
	public static final int EXCEL2007 = 2007;
	public static final int EXCEL_NOT = 100;
	public static final int EXCEL_NO_FILE = 0;

	 private Workbook wb;
	 private Sheet sheet;
	 private int sheetNum = 0; // ��sheetnum��������
     private String filename="";
     /** excel 2003 ���� excel 2007 */
	 int versionXls = 0;
	 public ExcelOperate() {}
	 /**
	  * ��excel��û�о��½�excel2003
	  * @param imputfilename
	  */
	 public ExcelOperate(String imputfilename) {
		 openExcel(imputfilename, false);
	 }
	 public ExcelOperate(String imputfilename, boolean excel2003) {
		 boolean excel2007 = !excel2003;
		 openExcel(imputfilename,excel2007);
	 }
	 
	 /**
	  * ��ȡexcel�ļ����HSSFWorkbook����,Ĭ���½�2003
	  * ���ʹ�õ�ʱ��Ҫ��try���Χ
	  * �ܶ�ȡ����true����Ȼ����false
	  * @param imputfilename
	  */
	 public boolean openExcel(String imputfilename) {  
		 return openExcel(imputfilename,false);
	 }
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel(String filename) {
		 try {
			 if (isExcelVersion(filename) == EXCEL2003 || isExcelVersion(filename) == EXCEL2007 )
				 return true;
		 } catch (Exception e) {}
		return false;
	 }
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	 * @throws FileNotFoundException 
	  */
	 private static int isExcelVersion(String filename) {
		 if (!FileOperate.isFileExist(filename))
			return EXCEL_NO_FILE;
		 File f = new File(filename);
		 FileInputStream fos = null;
		try {
			fos = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return EXCEL_NO_FILE;
		}
		 if (isExcel2003(fos))
			return EXCEL2003;
		 else if (isExcel2007(fos))
			return EXCEL2007;
		return EXCEL_NOT;
	 }
	  private static boolean isExcel2003(FileInputStream fos) {
		  Workbook wb = null;
		  try {
			  wb = new HSSFWorkbook(fos);
		  } catch (Exception e) {  }
		  if (wb != null) 
			  return true;
		  return false;
	 }
	 public static boolean isExcel2007(FileInputStream fos) {
		 Workbook wb = null;
		try {
			wb = new XSSFWorkbook(fos);
		} catch (Exception e) {  }
		if (wb != null) 
			 return true;
		return false;
	 }
	 /**
	  * ��ȡexcel�ļ����Workbook����,Ĭ�Ͼ۽��ڵ�һ��sheet��
	  * ���ʹ�õ�ʱ��Ҫ��try���Χ
	  * �ܶ�ȡ����true����Ȼ����false
	  * @param imputfilename
	  * @param excel2007 �Ƿ���2007��excel��true����
	 * @throws FileNotFoundException 
	  */
	 public boolean openExcel(String imputfilename,boolean excel2007) {
		 filename=imputfilename;
		 if (!FileOperate.isFileExist(imputfilename)) {
			 return newExcelOpen(imputfilename, excel2007);
		 }
		 versionXls = isExcelVersion(filename);
		 return initialExcel();
	 }
	 
	 private boolean initialExcel() {
		try {
			return resetExcelExp();
		} catch (Exception e) {
			return false;
		}
	 }
	 
	 private boolean resetExcelExp() throws Exception {
		 if (versionXls != EXCEL2003 && versionXls != EXCEL2007)
			 return false; 
		 
		 File f = new File(filename);
		 FileInputStream fos = new FileInputStream(f);
		 if (versionXls == EXCEL2003)
			  wb= new HSSFWorkbook(fos);
		 else if (versionXls == EXCEL2007)
			  wb= new HSSFWorkbook(fos);
		 
		  sheet = wb.getSheetAt(0);
		  sheetNum = 0;
	 	  fos.close();
    	  return true;
	 }
	 /**
	  * Ĭ���½�03��excel
	  * @param filenameinput
	  * @return
	  */
	 public boolean newExcelOpen(String filenameinput) {
		 return newExcelOpen(filenameinput,false);
	 }
	 
	 public boolean newExcelOpen(String filenameinput, boolean excel2007) {
		filename = filenameinput;
		if (!excel2007) {
			wb = new HSSFWorkbook();
			versionXls = EXCEL2003;
		} else {
			wb = new XSSFWorkbook();
			versionXls = EXCEL2007;
		}
		return true;
	}

	// ///////////////////////excel�ĸ������ԣ�����sheet��Ŀ��ĳsheet�µ�����///////////////////////
	 /**
	  * ����sheet����Ŀ,Ϊʵ��sheet��Ŀ
	  * @return int
	  */
	 public int getSheetCount() {
		 int sheetCount = -1;
		 sheetCount =  wb.getNumberOfSheets();//�����õ���ʵ�ʵ�sheet��
		 return sheetCount;
	 }

	 /**
	  * ���Ĭ��sheetNum�µļ�¼����,Ϊʵ������
	  * @return int ʵ�����������û���У��򷵻�1
	  */
	 public int getRowCount() {
		 return getRowCount(this.sheetNum+1);//�����õ�row����ʵ����һ�����Բ���
	 }

	 /**
	  * ���ָ��sheetNum��rowCount,Ϊʵ������
	  * @param sheetNum,sheet����Ϊʵ��sheet��
	  * @return ʵ�����������û���У��򷵻�1
	  */
	 public int getRowCount(int sheetNum) {
		 sheetNum--;
		 if (wb == null)
			 return 0;
		
		 Sheet sheet = wb.getSheetAt(sheetNum);
		 if (sheet == null) {
			 return 0;
		 }
		 int rowCount = -1;
		 rowCount = sheet.getLastRowNum()+1;
		 return rowCount;
	 }
	 /**
	  * ���Ĭ��sheetNum��ǰ20���������
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ�������,������в����ڣ��򷵻�0
	  */
	 public int getColCount() {
		 int maxColNum = 0;
		 for (int i = 0; i < 20; i++) {
			 int tmpColCount = getColCount(this.sheetNum+1,i);
			if (tmpColCount > maxColNum) 
				maxColNum = tmpColCount;
		}
		return maxColNum;
	 }
	 
	 /**
	  * ���Ĭ��sheetNum��ǰ20���������
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ�������,������в����ڣ��򷵻�0
	  */
	 public int getColCountSheet(int sheet) {
		 int maxColNum = 0;
		 for (int i = 0; i < 20; i++) {
			 int tmpColCount = getColCount(sheet,i);
			if (tmpColCount > maxColNum) 
				maxColNum = tmpColCount;
		}
		return maxColNum;
	 }
	 /**
	  * ��õ�һ��sheetNum�ĵ�rowNum�е�����
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ���������������в����ڣ��򷵻�0
	  */
	 public int getColCount(int rownum) {    
		 return getColCount(1,rownum);
	 }
	 /**
	  * ���ָ��sheetNum��rowNum�µ�����
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ���������������в����ڣ��򷵻�0
	  */
	 public int getColCount(int sheetNum,int rowNum) {
		 rowNum--; sheetNum--;
	   if (wb == null)
		 return 0;
	   Sheet sheet = wb.getSheetAt(sheetNum);
	   if (sheet == null)
		   return 0;
	   Row row=sheet.getRow(rowNum);
	   if (row == null)
		   return 0;
	   
	   int ColCount = -1;
	   ColCount = row.getLastCellNum();
	   return ColCount;
	 }
	 /**
	  * ��ȡĬ��sheet��ָ���������,����м��п��У���һ����ȡ<br/>
	  *ֱ��ָ����׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(int rowStartNum, int rowEndNum, int[] columnNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return ReadLsExcel(this.sheetNum+1, rowStartNum, rowEndNum, columnNum);
	 }
	 /**
	  * ��ȡָ���������,ͬʱ������ŵ���sheet��,����arrayList����м��п��У�������<br/>
	  *ָ������ȡsheet���ƣ���׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param sheetName������ȡsheet����<br/> 
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			 sheetNum = 0;
		 }
		 sheet = wb.getSheetAt(sheetNum);
		 return  ReadLsExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 
	 
	 /**
	  * ��ȡָ��������,����arrayList,����м��п��У���һ����ȡ<br/>
	  * ָ����������ʼ����������������ֹ����������<br/>
	  * ֱ��ָ����׼��sheet������������������1��ʼ���������ô�0��<br/>
	  * ��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param sheetNum��ʵ��sheet��<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[]
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 //�������������������������
		 if (rowEndNum <= 0)
			 rowEndNum = getRowCount(sheetNum);
		 if (columnEndNum <= 0)
			columnEndNum = getColCountSheet(sheetNum);
		 
		sheetNum--;rowStartNum--;columnStartNum--;rowEndNum--;columnEndNum--;
		
		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;
		
		int[] readColumn = new int[columnEndNum - columnStartNum + 1];
		for (int readColNum = columnStartNum; readColNum <= columnEndNum; readColNum++) {
			readColumn[readColNum] = readColNum;
		}
		
		return ReadLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	 }
	 /**
	  * ��ȡָ���������,ͬʱ������ŵ���sheet��,����arrayList����м��п��У�������<br/>
	  *ָ������ȡsheet���ƣ���׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param sheetName������ȡsheet����<br/> 
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadLsExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 /**
	  * ��ȡָ��������,����arrayList,����м��п��У���һ����ȡ<br/>
	  * ָ����������ʼ����������������ֹ����������<br/>
	  * ֱ��ָ����׼��sheet������������������1��ʼ���������ô�0��<br/>
	  * ��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param sheetNum��ʵ��sheet��<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[]
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
		 //�������������������������
		 if (rowEndNum <= 0)
			 rowEndNum = getRowCount(sheetNum);
		 
		sheetNum--; rowStartNum--; rowEndNum--;
		
		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;
	
		int[] readColumn = new int[readColNum.length - 1];
		for (int i = 0; i < readColumn.length; i++) {
			readColumn[i] = readColNum[i] - 1;
		}
		
		return ReadLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	 }
	 
	 private ArrayList<String[]> ReadLsExcelDetail(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
		 ArrayList<String[]> LsExcelLine = new ArrayList<String[]>();
		 sheet = wb.getSheetAt(sheetNum);
		 Row row = sheet.getRow(rowStartNum);

		 for (int readLines = rowStartNum; readLines <= rowEndNum; readLines++) {
			 row = sheet.getRow(readLines);
			 String[] tmpLine = new String[readColNum.length];
			 if (row == null) {
				 for (int j = 0; j < tmpLine.length; j++) {
					 tmpLine[j] = "";
				 }
				 LsExcelLine.add(tmpLine);
				 continue;
			 }
			 for (int j = 0; j < readColNum.length; j++) {
				 Cell cell = row.getCell((readColNum[j]));
				 tmpLine[j] = getCellInfo(cell);
			 }
			 LsExcelLine.add(tmpLine);
		 }
		 return LsExcelLine;
	 }
	 /**
	  *  ��ȡ�������ݣ�Ĭ�Ϲ�����ָ���С��С�
	  *  ����ָ�����кź��кŶ�ֻҪ��ʵ��ţ�����Ҫ��ȥ1
	  * @param rowNum
	  * @param cellNum
	  * @return String
	  */
	 public String ReadExcel(int rowNum, int cellNum) {
		 return  ReadExcel(this.sheetNum+1, rowNum, cellNum);
	 }
	 /**
	  * ��ȡ�������ݣ�ָ��������sheetNum���С��С�
	  * ����ָ����Sheet��ţ��кź��кŶ�ֻҪ��ʵ��ţ�����Ҫ��ȥ1
	  * @param sheetNum
	  * @param rowNum
	  * @param cellNum
	  * @return String
	  */
	 public String ReadExcel(int sheetNum, int rowNum, int cellNum) {
		 sheetNum--;rowNum--;cellNum--;
		 if (sheetNum < 0) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 if (rowNum < 0)
			 return "";
		 sheet = wb.getSheetAt(sheetNum);
		 Row row = sheet.getRow(rowNum);
		 Cell cell = row.getCell(cellNum);
		 return getCellInfo(cell);
	 }
	 
	 private String getCellInfo(Cell cellExcel) {
		 String result = "";
		 if (cellExcel != null) { // add this condition
			 switch (cellExcel.getCellType()) {
			 case Cell.CELL_TYPE_FORMULA:
				 result = getExcelNumeric(cellExcel.getNumericCellValue());
				 break;
			 case Cell.CELL_TYPE_NUMERIC:  //�����Ԫ�������������Ϊ����  
				 result = getExcelNumeric(cellExcel.getNumericCellValue());
				 break;
			 case Cell.CELL_TYPE_STRING:
				 result = cellExcel.getStringCellValue().trim();
				 break;
			 case Cell.CELL_TYPE_BOOLEAN://�����Ԫ�������������Ϊ Boolean                     
				 result = String.valueOf(cellExcel.getBooleanCellValue()).trim();
				 break;
			 case Cell.CELL_TYPE_BLANK:
				 result = "";
				 break;
			 default:
				 result = "error";
				 break;
			 }
		 }
		 return result;
	 }
	 /** ��excel�л�õ�����ת��Ϊ�ַ����������Ƿ���С���㣬����ת�� */
	 private String getExcelNumeric(double value) {
		 if (value == Math.ceil(value)) {
			 Long result = (long) value;
			 return result + "";
		 }
		 return value + "";
	 }
	 /**
	  * Ĭ�ϱ���
	  * ������ֵд�뵥��excel�ļ�,Ĭ��д��sheet1,д������sheet���ı�exceloperate����
	  * ����д�����������������ݣ�д�������Ĭ��ΪString
	  * ������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * @param rowNum
	  * @param cellNum
	  * @param content
	  */
	 public boolean WriteExcel(int rowNum, int cellNum,String content) {
		 return WriteExcel(null, 1,rowNum, cellNum,content);
	 }
 
	 /**
	     * ���ļ�д��excel�ļ������趨sheetName�����û�и�sheetName����ô���½�һ��
	     * ����д���sheet���֣����������������ݣ�д�������Ĭ��ΪString[][]
	     * String[][]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param sheetName
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,List<String[]> content) {
		 return WriteExcel(sheetName, -1, rowNum, cellNum, content);
	 }
	 /**
	  * ���ļ�д��excel�ļ�
	  * ����д���sheet�������������������ݣ�д�������Ĭ��ΪList<String[]>,����String[]Ϊ�У�list.get(i)Ϊ��
	  * String[]�е�null���Զ�����
	  * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	  * @param rowNum ʵ����
	  * @param cellNum ʵ����
	  * @param content
	  */
	 public boolean WriteExcel(int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, 1, rowNum, cellNum, content);
	}
	 /**
	  * ���ļ�д��excel�ļ�
	  * ����д���sheet�������������������ݣ�д�������Ĭ��ΪList<String[]>,����String[]Ϊ�У�list.get(i)Ϊ��
	  * String[]�е�null���Զ�����
	  * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	  * @param sheetNum
	  * @param rowNum
	  * @param cellNum
	  * @param content
	  */
	 public boolean WriteExcel(int sheetNum, int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, sheetNum, rowNum, cellNum, content);
	}
	 /**
	  * ����Ԫ��д��excel�ļ�
	  * ����д���sheet����sheetName������ֻҪ����һ����Ĭ�����趨sheetName
	  * ���������������ݣ�д�������Ĭ��ΪList<String[]>,����String[]Ϊ�У�list.get(i)Ϊ��
	  * String[]�е�null���Զ�����
	  * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum ʵ����
	  * @param cellNum ʵ����
	  * @param content
	  * @return
	  */
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, String content) {
		initialExcel(); 
		if ((sheetNum <= -1 && sheetName == null) || rowNum < 0)
			return false;

		Sheet sheet = getSheet(sheetName, sheetNum);
		writeExcel(sheet, rowNum, cellNum, content);
		if (filename != "")
			Save();
		return true;
	}
	 /**
	  * ���ļ�д��excel�ļ�
	  * ����д���sheet����sheetName������ֻҪ����һ����Ĭ�����趨sheetName
	  * ���������������ݣ�д�������Ĭ��ΪList<String[]>,����String[]Ϊ�У�list.get(i)Ϊ��
	  * String[]�е�null���Զ�����
	  * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum ʵ����
	  * @param cellNum ʵ����
	  * @param content
	  * @return
	  */
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, List<String[]> content) {
		initialExcel();
		if ((sheetNum <= -1 && sheetName == null) || rowNum < 0)
			return false;

		Sheet sheet = getSheet(sheetName, sheetNum);
		writeExcel(sheet, rowNum, cellNum, content);
		if (filename != "")
			Save();
		return true;
	}
	 /**
	  * ����д���sheet����sheetName������ֻҪ����һ����Ĭ�����趨sheetName
	  * @param sheetName û����Ϊnull
	  * @param sheetNum û����ΪС��1
	  * @return
	  */
	private Sheet getSheet(String sheetName, int sheetNum) {
		sheetNum--;
		Sheet sheet = null;
		if (sheetName != null) {
			sheet = wb.getSheet(sheetName);
			if (sheet == null) {
				sheet = wb.createSheet(sheetName);
				sheetNum = wb.getSheetIndex(sheetName);
			}
		} else if (sheetNum >= 0) {
			try {
				sheet = wb.getSheetAt(sheetNum);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (sheet == null) {
				sheet = wb.createSheet("sheet" + (getSheetCount() + 1));// �½�sheet
			}
		}
		return sheet;
	}
	/**
	 * д�뵥��Ԫ��
	 * @param sheet
	 * @param rowNum ʵ����
	 * @param cellNum ʵ����
	 * @param content
	 * @return
	 */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, String content) {
		rowNum--;
		cellNum--;// ��sheet�����ж���ԭΪ��״̬
		if (rowNum < 0)
			return false;
		try {
			// row = sheet.createRow(rowNum);
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				row = sheet.createRow(rowNum);
			}
			Cell cell = row.createCell((short) cellNum);
			try {
				double tmpValue = Double.parseDouble(content);
				// cell.setCellType(0);
				cell.setCellValue(tmpValue);
			} catch (Exception e) {
				cell.setCellValue(content);
			}
			if (filename != "")
				Save();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	 /**
	  * д��list��
	  * @param sheet
	  * @param rowNum ʵ����
	  * @param cellNum ʵ����
	  * @param content
	  * @return
	  */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, Iterable<String[]> content) {
		rowNum--;
		cellNum--;// ��sheet�����ж���ԭΪ��״̬
		if (rowNum < 0)
			return false;
		try {
			int i = 0;
			for (String[] rowcontent : content) {
				int writerow = i + rowNum;// д�������
				Row row = sheet.getRow(writerow);
				if (row == null) {
					row = sheet.createRow(writerow);
				}
				if (rowcontent == null)
					continue;
				for (int j = 0; j < rowcontent.length; j++) // д��
				{
					if (rowcontent[j] == null)
						continue; // ������ֵ
					Cell cell = row.createCell((short) (cellNum + j));
					try {
						double tmpValue = Double.parseDouble(rowcontent[j]);
						// cell.setCellType(0);
						cell.setCellValue(tmpValue);
					} catch (Exception e) {
						cell.setCellValue(rowcontent[j]);
					}
				}
				i ++;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	 /**
	     * ���ļ�д��excel�ļ���
	     * ����д���sheet�������������������ݣ�д�������Ĭ��ΪString[],�趨д����/��
	     * String[]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw trueΪд��ĳһ�У��趨falseΪд��ĳһ��
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, String[] content, boolean raw) {
		 initialExcel();
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeNumber=content.length;//������������һά������
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
	    		 }
	    		  
	    		 if(raw==true) {//����д��һ��
	    			 Row row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((short)(cellNum+i));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else {
	    			 for(int i=0;i<writeNumber;i++) {
	    				 Row row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((short)(cellNum));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 if(filename!=""&&save)	Save();
	    		 return true;
	    	 }
	    	 catch (Exception e) {
	    		 e.printStackTrace();
	    		 return false;
	    	 }
	 }
	 
	 /**
	     * ���ļ�д��excel�ļ���
	     * ����д���sheet�������������������ݣ�д�������Ĭ��ΪList<String>,�趨д����/��
	     * String[]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw trueΪд��ĳһ�У��趨falseΪд��ĳһ��
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, List<String> content, boolean raw) {
		 initialExcel();
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeNumber=content.size();//������������һά������
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
	    		 }
	    		  
	    		 if(raw==true)//����д��һ��
	    		 {	
	    			 Row row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((cellNum+i));
	    				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else {
	    			 for(int i=0;i<writeNumber;i++) {
	    				 Row row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((cellNum));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 if(filename!=""&&save)	Save();
	    		 return true;
	    	 }
	    	 catch (Exception e) {
	    		 e.printStackTrace();
	    		 return false;
	    	 }
	 }
	 
///////////////////////�����ļ�����/////////////////////////////////////	 
	    /**
	     * ����excel�ļ���ʹ����ǰ���ļ�����������
		 */
	 public boolean Save() {
		 if(filename=="") return false;
		 try {
			 FileOutputStream out = new FileOutputStream(filename);
			 wb.write(out);
			 out.close();
			 return true;
		 } catch (Exception e) {
			 // TODO: handle exception
			 e.printStackTrace();
			 return false;
		}
	}
	     /**
	      * �����ļ���
	     * ����excel�ļ������Ϊ
		 */
	 public boolean Save(String newfilename) {
		 try {
			 FileOutputStream out = new FileOutputStream(newfilename);
			    wb.write(out);
			    out.close();
			    return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
///////////////////�رն���////////////////////////////////
	 /**
	  * ��ʱû����
	  */
	 public void Close() {//��ʱ����
		 wb = null;// book [includes sheet]
		 sheet = null;
	 }
}

	 
 
