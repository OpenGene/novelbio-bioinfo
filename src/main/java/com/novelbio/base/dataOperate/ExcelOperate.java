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
public class ExcelOperate //Ŀǰ�Ǵ����ϸ�Ķ�ȡ����
{   
	public static final int EXCEL2003 = 2003;
	public static final int EXCEL2007 = 2007;
	public static final int EXCEL_NOT = 100;
	public static final int EXCEL_NO_FILE = 0;
	
	//**
	 //* @author caihua //ò����ԭ���ߣ������뾭��Zong Jie�޸�
	 //*/
	////////////////////�������////////////////////////
	 private Workbook wb = null;// book [includes sheet]
	 private Sheet sheet = null;
	 private Row row = null;
	 private Cell cell= null;
	 private int sheetNum = 0; // ��sheetnum��������
//	 private int rowNum = 0;
	 private FileInputStream fis = null;
     private String filename="";
		
	
    
	 public ExcelOperate()//���캯������ʱ��֪����ʲô��
	 {
	 }
	 /**
	  * ��excel��û�о��½�excel2003
	  * @param imputfilename
	  */
	 public ExcelOperate(String imputfilename)//���캯������ʱ��֪����ʲô��
	 {
		 openExcel(imputfilename, false);
	 }
	 public ExcelOperate(String imputfilename, boolean excel2003)//���캯������ʱ��֪����ʲô��
	 {
		 boolean excel2007 = !excel2003;
		 openExcel(imputfilename,excel2007);
	 }
	 
	 public static void main(String[] args) {
//		System.out.println(isExcelVersion("/home/zong0jie/����/1471-2164-8-242-s4.xx"));
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel("/home/zong0jie/����/mytest4.xlsx");
//		excelOperate.WriteExcel(2, 2, "test");
		ArrayList<String[]> lsTest = new ArrayList<String[]>();
		lsTest.add(new String[]{"sfe","feget","feget","feget"});
		lsTest.add(new String[]{"sfe","feget","feget","feget"});
		lsTest.add(new String[]{"sfe","feget","feget","feget"});
		lsTest.add(new String[]{"sfe","feget","feget","feget"});
		excelOperate.WriteExcel("testsheet4", 3, 5, lsTest);
		excelOperate.WriteExcel("testsheet4", 8, 10, lsTest);
		excelOperate.WriteExcel("testsheet4", 18, 20, lsTest);
	}
	 
	 
	 /**
	  * ��ȡexcel�ļ����HSSFWorkbook����,Ĭ���½�2003
	  * ���ʹ�õ�ʱ��Ҫ��try���Χ
	  * �ܶ�ȡ����true����Ȼ����false
	  * @param imputfilename
	  */
	 public boolean openExcel(String imputfilename) 
	 {  
		 return openExcel(imputfilename,false);
	 }
	 
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 private static int isExcelVersion(String filename) 
	 {
		 if (!FileOperate.isFileExist(filename)) {
			return EXCEL_NO_FILE;
		 }
		 if (isExcel2003(filename)) {
			return EXCEL2003;
		}
		 else if (isExcel2007(filename)) {
			return EXCEL2007;
		}
		return EXCEL_NOT;
	 }
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel(String filename) 
	 {
		 if (isExcelVersion(filename) == EXCEL2003 || isExcelVersion(filename) == EXCEL2007 ) {
			return true;
		}
		return false;
	 }
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel2003(String filename) 
	 {
		 File f = new File(filename);
		 FileInputStream fos = null;
		 Workbook wb = null;
		 //���ļ�
		try { fos = new FileInputStream(f); }  catch (FileNotFoundException e)  {}
		try {
			wb = new HSSFWorkbook(fos);
		} catch (Exception e) {  }
		if (wb != null) 
			 return true;
		return false;
	 }
	 
	 /**
	  * �ж��Ƿ�Ϊexcel2003��2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel2007(String filename) 
	 {
		 File f = new File(filename);
		 FileInputStream fos = null;
		 Workbook wb = null;
		 //���ļ�
		try { fos = new FileInputStream(f); }  catch (FileNotFoundException e)  {}
		try {
			wb = new XSSFWorkbook(fos);
		} catch (Exception e) {  }
		if (wb != null) 
			 return true;
		return false;
	 }
	 
	 int versionXls = 0;;
	 /**
	  * ��ȡexcel�ļ����Workbook����,Ĭ�Ͼ۽��ڵ�һ��sheet��
	  * ���ʹ�õ�ʱ��Ҫ��try���Χ
	  * �ܶ�ȡ����true����Ȼ����false
	  * @param imputfilename
	  * @param excel2007 �Ƿ���2007��excel��true����
	  */
	 ////////////////////������excel�ļ�//////////////////////////////////
	 public boolean openExcel(String imputfilename,boolean excel2007) 
	 {
		 try {
		      filename=imputfilename;
		      File f = new File(filename);  
		      //����ļ����ڣ���򿪸��ļ�
		      if (f.exists()) 
		      {
		    	  FileInputStream fos = new FileInputStream(f);   //��Ҫ��ȡ�� .xls �ļ� ��װ����  
		    	  if (isExcelVersion(imputfilename) == EXCEL2003) {
		    		  wb= new HSSFWorkbook(fos);       //�õ� excel ��������Ӧ�� HSSFWorkbook ����  
		    		  sheet = wb.getSheetAt(0);
		    		  sheetNum = 0;
		    		  versionXls = EXCEL2003;
		    	  }
		    	  else if (isExcelVersion(imputfilename) == EXCEL2007) {
		    		  wb=new XSSFWorkbook(fos);
		    		  sheet = wb.getSheetAt(0);
		    		  sheetNum = 0;
		    		  versionXls = EXCEL2007;
		    	  }
		    	  else {
		    		 return false;
		    	  }
		    	  fos.close();
		    	  return true;
		      }
		      //���򴴽����ļ�
		      else 
		      {
		    	  return newExcelOpen(imputfilename, excel2007);
		      }
	         }
		 catch (Exception e) 
		 {
			 e.printStackTrace();
			 return false;
		  }
	 }
	 
	 private boolean resetExcel() 
	 {
		 
		 
		 
		 try {
			 if (!FileOperate.isFileExist(filename)) {
				return true;
			}
			File f = new File(filename);
			// ����ļ����ڣ���򿪸��ļ�
			FileInputStream fos = new FileInputStream(f); // ��Ҫ��ȡ�� .xls �ļ� ��װ����
			if (versionXls == EXCEL2003) {
				wb = new HSSFWorkbook(fos); // �õ� excel ��������Ӧ�� HSSFWorkbook ����
			} else if (versionXls == EXCEL2007) {
				wb = new XSSFWorkbook(fos);
			} else {
				return false;
			}
			fos.close();
			return true;
		 }
		 catch (Exception e) 
		 {
			 e.printStackTrace();
			 return false;
		  }
	 }
	 /**
	  * Ĭ���½�03��excel
	  * @param filenameinput
	  * @return
	  */
	 public boolean newExcelOpen(String filenameinput)
	 {
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
	 public int getRowCount() //Ĭ��sheet�µ�row
	 {
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
		 {
			 System.out.println("=============>WorkBookΪ��");
			 return 0;
		 }
	  Sheet sheet = wb.getSheetAt(sheetNum);
	  if (sheet == null)
		 {
			 System.out.println("=============>sheetΪ��");
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
	 public int getColCount()
	 {
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
	 public int getColCountSheet(int sheet)
	 {
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
	   if (wb == null) {
		 System.out.println("=============>WorkBookΪ��");
		 return 0;
	   }
	   Sheet sheet = wb.getSheetAt(sheetNum);
	   if (sheet == null) {
		   System.out.println("=============>sheetΪ��");
		   return 0;
	   }
	   Row row=sheet.getRow(rowNum);
	   if (row == null) {
		   System.out.println("=============>rowΪ��");
		   return 0;
	   }
	  
	   int ColCount = -1;
	   ColCount = row.getLastCellNum();
	   return ColCount;
	 }
/////////////////////��ȡexcel///////////////////////////////////////////////////////
	 
	 /**
	  * ��ȡĬ��sheet��ָ���������,����м��п��У�������<br/>
	  *ֱ��ָ����׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public String[][]  ReadExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadExcel(this.sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 
	 
	 /**
	  * ��ȡĬ��sheet��ָ���������,����м��п��У�������<br/>
	  *ֱ��ָ����׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/> 
	  * @param columnEndNum���յ�ʵ��������С�ڵ���0���ȡ��β��<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]>  ReadLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadLsExcel(this.sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 

	 /**
	  * ��ȡָ���������,ͬʱ������ŵ���sheet��,����м��п��У�������<br/>
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
	 public String[][]  ReadExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			sheetNum = 0;
		}
		 sheet = wb.getSheetAt(sheetNum);
		 return  ReadExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
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
	 public ArrayList<String[]>  ReadLsExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			 sheetNum = 0;
		 }
		 sheet = wb.getSheetAt(sheetNum);
		 return  ReadLsExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 
	 
	 /**
	  * ��ȡָ��������,����м��п��У�������<br/>
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
	  * ������
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public String[][] ReadExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (rowEndNum <= 0) {
			 rowEndNum = getRowCount(sheetNum);
		 }
		 if (columnEndNum <= 0) {
			 columnEndNum = getColCountSheet(sheetNum);
		 }
		 sheetNum--; rowStartNum--; columnStartNum--; rowEndNum--; columnEndNum--;

		if (sheetNum < 0) {
			sheetNum = wb.getSheetIndex(sheet);
		}
		if (rowStartNum < 0) {
			rowStartNum = 0;
		}
		String[][] strExcelLine = null;
		try {
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(rowStartNum);

			// int cellCount;//Ҫ��ȡ���е�cell��
			// ��������ȥ��
			// ///////////////////////
			// if(columnEndNum<row.getLastCellNum())//���Ҫ��ȡ������С�ڸ��к��е�cell��Ŀ����ô�Ͷ��ٵ�
			// {
			// cellCount = columnEndNum;
			// }
			// else //��Ȼ��ȫ��ȡ
			// {
			// cellCount=row.getLastCellNum();
			// }
			// //////////////////////
			// �����ȡ����������ʵ����������ô����ʵ��������ȡ����ȡ������
			// if (rowEndNum>sheet.getLastRowNum())
			// {
			// rowEndNum=sheet.getLastRowNum();
			// }

			int readrownum = rowEndNum - rowStartNum + 1;// ����ʵ������
			int readcolumnnum = columnEndNum - columnStartNum + 1;// ��ȡ��ʵ������
			strExcelLine = new String[readrownum][readcolumnnum];
			for (int i = 0; i < readrownum; i++) {
				row = sheet.getRow(rowStartNum + i);
				if (row == null)// �м��п��У����row����null
				{
					continue;
				}
				for (int j = 0; j < readcolumnnum; j++) // ���ǽ�cellcount����readcolumnnum
				{
					try {
						if (row.getCell((short) (j + columnStartNum)) != null) { // add
																					// this
																					// condition
																					// judge
							switch (row.getCell((short) (j + columnStartNum)).getCellType()) {
							case Cell.CELL_TYPE_FORMULA:
								// strExcelLine[i][j] = "FORMULA";
								strExcelLine[i][j] = String.valueOf(row.getCell((short) (j + columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_NUMERIC: // �����Ԫ�������������Ϊ����
								strExcelLine[i][j] = String.valueOf(row.getCell((short) (j + columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_STRING:
								strExcelLine[i][j] = row.getCell((short) (j + columnStartNum)).getStringCellValue().trim();
								break;
							case Cell.CELL_TYPE_BOOLEAN:// �����Ԫ�������������Ϊ Boolean
								strExcelLine[i][j] = String.valueOf(row.getCell((short) (j + columnStartNum)).getBooleanCellValue()).trim();
							case Cell.CELL_TYPE_BLANK:
								strExcelLine[i][j] = "";
								break;
							default:
								strExcelLine[i][j] = "error";
								break;
							}

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return strExcelLine;
	}
	 
	 /**
	  * ��ȡָ��������,����arrayList,����м��п��У�������<br/>
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
	  * ������
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (rowEndNum <= 0) {
			 rowEndNum = getRowCount(sheetNum);
		}
		 if (columnEndNum <= 0) {
			columnEndNum = getColCountSheet(sheetNum);
		}
		sheetNum--;rowStartNum--;columnStartNum--;rowEndNum--;columnEndNum--;

		if (sheetNum < 0) {
			sheetNum = wb.getSheetIndex(sheet);
		}
		if (rowStartNum < 0) {
			rowStartNum = 0;
		}
		ArrayList<String[]> LsExcelLine = new ArrayList<String[]>();
		try {
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(rowStartNum);
			int readrownum=rowEndNum-rowStartNum+1;//����ʵ������
			int readcolumnnum=columnEndNum-columnStartNum+1;//��ȡ��ʵ������
			//LsExcelLine = new String[readrownum][readcolumnnum];
			
			for(int i=0;i<readrownum;i++) {
				row=sheet.getRow(rowStartNum+i);
				if (row==null) {//�м��п��У����row����null
					continue;
				}
				String[] tmpLine = new String[readcolumnnum];
				for (int j = 0; j < readcolumnnum; j++) {//���ǽ�cellcount����readcolumnnum
					try {
						if (row.getCell((short)(j+columnStartNum)) != null) 
						{ // add this condition
							// judge
							switch (row.getCell((short)(j+columnStartNum)).getCellType()) {
							case Cell.CELL_TYPE_FORMULA:
								//strExcelLine[i][j] = "FORMULA";
								tmpLine[j] =  String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_NUMERIC:  //�����Ԫ�������������Ϊ����  
								tmpLine[j]= String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_STRING:
								tmpLine[j] = row.getCell((short)(j+columnStartNum)).getStringCellValue().trim();
								break;
							case Cell.CELL_TYPE_BOOLEAN://�����Ԫ�������������Ϊ Boolean                     
								tmpLine[j] = String.valueOf(row.getCell((short)(j+columnStartNum)).getBooleanCellValue()).trim();  
							case Cell.CELL_TYPE_BLANK:
								tmpLine[j] = "";
								break;
							default:
								tmpLine[j] = "error";
								break;
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				LsExcelLine.add(tmpLine);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
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
	 public String  ReadExcel(int rowNum, int cellNum) {
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
	 public String ReadExcel(int sheetNum, int rowNum, int cellNum) 
	 {
		 sheetNum--;rowNum--;cellNum--;
		 if (sheetNum < 0) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 if (rowNum < 0)
			 return "";
		 String strExcelCell = "";
		 try {
			 sheet = wb.getSheetAt(sheetNum);
			 row = sheet.getRow(rowNum);

			 if (row.getCell((short) cellNum) != null) { // add this condition
				 // judge
				 switch (row.getCell((short) cellNum).getCellType()) {
				 case Cell.CELL_TYPE_FORMULA:
					 strExcelCell = "FORMULA ";
					 strExcelCell = String.valueOf(row.getCell((short) cellNum).getNumericCellValue()).trim();
					 break;
				 case Cell.CELL_TYPE_NUMERIC: {
					 strExcelCell = String.valueOf(row.getCell((short) cellNum).getNumericCellValue());
				 }
				 break;
				 case Cell.CELL_TYPE_STRING:
					 strExcelCell = row.getCell((short) cellNum).getStringCellValue().trim();//
					 break;
				 case Cell.CELL_TYPE_BLANK:
					 strExcelCell = "";
					 break;
				 default:
		    strExcelCell = "error";
		    break;
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return strExcelCell;
	 }
////////////////////д��excel���� /////////////////////////////////////
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
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,String[][] content) {
		 return WriteExcel(sheetName, -1, rowNum, cellNum, content);
	 }
	/**
	 * ���ļ�д��excel�ļ�,Ĭ��д���һ��sheet ����д������������������ݣ�д�������Ĭ��ΪString[][]
	 * String[][]�е�null���Զ����� ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	 * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	 * 
	 * @param rowNum ʵ����
	 * @param cellNum ʵ����
	 * @param content
	 */
	public boolean WriteExcel(int rowNum, int cellNum, String[][] content) {
		return WriteExcel(null, 1, rowNum, cellNum, content);
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
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, String content) {
		resetExcel(); 
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
		resetExcel();
		if ((sheetNum <= -1 && sheetName == null) || rowNum < 0)
			return false;

		Sheet sheet = getSheet(sheetName, sheetNum);
		writeExcel(sheet, rowNum, cellNum, content);
		if (filename != "")
			Save();
		return true;
	}
	 /**
	  * 
	  * ���ļ�д��excel�ļ�
	  * ����д���sheet����sheetName������ֻҪ����һ����Ĭ�����趨sheetName
	  * ���������������ݣ�д�������Ĭ��ΪList<String[]>,����String[]Ϊ�У�list.get(i)Ϊ��
	  * String[]�е�null���Զ�����
	  * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	  * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	  * @param sheetNum ʵ��sheet����Ҳ���Ǳ�����ڵ���1
	  * @param sheetName
	  * @param rowNum ʵ����
	  * @param cellNum ʵ����
	  * @param content
	  * @return
	  */
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, String[][] content) {
		resetExcel();
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
		boolean flag;
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
	 * д�������
	 * @param sheet
	 * @param rowNum ʵ����
	 * @param cellNum ʵ����
	 * @param content
	 * @return
	 */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, String[][] content) {
		rowNum--;
		cellNum--;// ��sheet�����ж���ԭΪ��״̬
		boolean flag;
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
		 resetExcel();
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeNumber=content.length;//������������һά������
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
	    		  
	    		 if(raw==true) {//����д��һ��
	    			 row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum+i));
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
	    				 row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum));
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
		 resetExcel();
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
	    			 row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum+i));
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
	    				 row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum));
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
		 row = null;
		 cell= null;
	}
  }

	 
 
