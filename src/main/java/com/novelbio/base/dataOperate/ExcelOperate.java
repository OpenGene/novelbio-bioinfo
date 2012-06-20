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
 * 本类需要实例化才能使用
 * 读取excel文件,注意读取前最好将excel的所有格式全部清除，不然可能会有问题。
 * 清除方法，将excel拷贝入txt文件，再拷贝回来
 * 可以跨平台使用
 * 读取速度可以	
 * 读取块返回的是一个二维数组，然后这个二维数组的起点是[0][0]，不同于C#中的[1,1]
 * 本类似乎无法获得最大列的数目,这里可以考虑采用一维数目不同的二维数组，这个实现可以考虑用foreach来遍历
 * 本代码原始作者 caihua ，Zong Jie修改
 */
public class ExcelOperate //目前是从网上搞的读取代码
{   
	public static final int EXCEL2003 = 2003;
	public static final int EXCEL2007 = 2007;
	public static final int EXCEL_NOT = 100;
	public static final int EXCEL_NO_FILE = 0;
	
	//**
	 //* @author caihua //貌似是原作者，本代码经过Zong Jie修改
	 //*/
	////////////////////定义变量////////////////////////
	 private Workbook wb = null;// book [includes sheet]
	 private Sheet sheet = null;
	 private Row row = null;
	 private Cell cell= null;
	 private int sheetNum = 0; // 第sheetnum个工作表
//	 private int rowNum = 0;
	 private FileInputStream fis = null;
     private String filename="";
		
	
    
	 public ExcelOperate()//构造函数，暂时不知道做什么用
	 {
	 }
	 /**
	  * 打开excel，没有就新建excel2003
	  * @param imputfilename
	  */
	 public ExcelOperate(String imputfilename)//构造函数，暂时不知道做什么用
	 {
		 openExcel(imputfilename, false);
	 }
	 public ExcelOperate(String imputfilename, boolean excel2003)//构造函数，暂时不知道做什么用
	 {
		 boolean excel2007 = !excel2003;
		 openExcel(imputfilename,excel2007);
	 }
	 
	 public static void main(String[] args) {
//		System.out.println(isExcelVersion("/home/zong0jie/桌面/1471-2164-8-242-s4.xx"));
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel("/home/zong0jie/桌面/mytest4.xlsx");
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
	  * 读取excel文件获得HSSFWorkbook对象,默认新建2003
	  * 这个使用的时候要用try块包围
	  * 能读取返回true，不然返回false
	  * @param imputfilename
	  */
	 public boolean openExcel(String imputfilename) 
	 {  
		 return openExcel(imputfilename,false);
	 }
	 
	 /**
	  * 判断是否为excel2003或2007
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
	  * 判断是否为excel2003或2007
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
	  * 判断是否为excel2003或2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel2003(String filename) 
	 {
		 File f = new File(filename);
		 FileInputStream fos = null;
		 Workbook wb = null;
		 //读文件
		try { fos = new FileInputStream(f); }  catch (FileNotFoundException e)  {}
		try {
			wb = new HSSFWorkbook(fos);
		} catch (Exception e) {  }
		if (wb != null) 
			 return true;
		return false;
	 }
	 
	 /**
	  * 判断是否为excel2003或2007
	  * @param imputfilename
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel2007(String filename) 
	 {
		 File f = new File(filename);
		 FileInputStream fos = null;
		 Workbook wb = null;
		 //读文件
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
	  * 读取excel文件获得Workbook对象,默认聚焦在第一个sheet上
	  * 这个使用的时候要用try块包围
	  * 能读取返回true，不然返回false
	  * @param imputfilename
	  * @param excel2007 是否是2007版excel，true：是
	  */
	 ////////////////////打开已有excel文件//////////////////////////////////
	 public boolean openExcel(String imputfilename,boolean excel2007) 
	 {
		 try {
		      filename=imputfilename;
		      File f = new File(filename);  
		      //如果文件存在，则打开该文件
		      if (f.exists()) 
		      {
		    	  FileInputStream fos = new FileInputStream(f);   //把要读取的 .xls 文件 包装起来  
		    	  if (isExcelVersion(imputfilename) == EXCEL2003) {
		    		  wb= new HSSFWorkbook(fos);       //得到 excel 工作簿对应的 HSSFWorkbook 对象  
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
		      //否则创建新文件
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
			// 如果文件存在，则打开该文件
			FileInputStream fos = new FileInputStream(f); // 把要读取的 .xls 文件 包装起来
			if (versionXls == EXCEL2003) {
				wb = new HSSFWorkbook(fos); // 得到 excel 工作簿对应的 HSSFWorkbook 对象
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
	  * 默认新建03版excel
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

	// ///////////////////////excel的各个属性，包括sheet数目，某sheet下的行数///////////////////////
	 /**
	  * 返回sheet表数目,为实际sheet数目
	  * @return int
	  */
	 public int getSheetCount() {
	  int sheetCount = -1;
	  sheetCount =  wb.getNumberOfSheets();//这里获得的是实际的sheet数
	  return sheetCount;
	 }

	 /**
	  * 获得默认sheetNum下的记录行数,为实际行数
	  * @return int 实际行数，如果没有行，则返回1
	  */
	 public int getRowCount() //默认sheet下的row
	 {
		 return getRowCount(this.sheetNum+1);//这里获得的row数比实际少一，所以补上
	 }

	 /**
	  * 获得指定sheetNum的rowCount,为实际行数
	  * @param sheetNum,sheet数，为实际sheet数
	  * @return 实际行数，如果没有行，则返回1
	  */
	 public int getRowCount(int sheetNum) {
		 sheetNum--;
		 if (wb == null)
		 {
			 System.out.println("=============>WorkBook为空");
			 return 0;
		 }
	  Sheet sheet = wb.getSheetAt(sheetNum);
	  if (sheet == null)
		 {
			 System.out.println("=============>sheet为空");
			 return 0;
		 }
	  int rowCount = -1;
	  rowCount = sheet.getLastRowNum()+1;
	  return rowCount;
	 }
	 
	 
	 
	 /**
	  * 获得默认sheetNum的前20行最长的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数,如果该行不存在，则返回0
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
	  * 获得默认sheetNum的前20行最长的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数,如果该行不存在，则返回0
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
	  * 获得第一个sheetNum的第rowNum行的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数，如果该行不存在，则返回0
	  */
	 public int getColCount(int rownum) {    
		 return getColCount(1,rownum);
	 }
	 /**
	  * 获得指定sheetNum的rowNum下的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数，如果该行不存在，则返回0
	  */
	 public int getColCount(int sheetNum,int rowNum) {
		 rowNum--; sheetNum--;
	   if (wb == null) {
		 System.out.println("=============>WorkBook为空");
		 return 0;
	   }
	   Sheet sheet = wb.getSheetAt(sheetNum);
	   if (sheet == null) {
		   System.out.println("=============>sheet为空");
		   return 0;
	   }
	   Row row=sheet.getRow(rowNum);
	   if (row == null) {
		   System.out.println("=============>row为空");
		   return 0;
	   }
	  
	   int ColCount = -1;
	   ColCount = row.getLastCellNum();
	   return ColCount;
	 }
/////////////////////读取excel///////////////////////////////////////////////////////
	 
	 /**
	  * 读取默认sheet的指定块的内容,如果中间有空行，则跳过<br/>
	  *直接指定标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[][]<br/>
	  */
	 public String[][]  ReadExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadExcel(this.sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 
	 
	 /**
	  * 读取默认sheet的指定块的内容,如果中间有空行，则跳过<br/>
	  *直接指定标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]>  ReadLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadLsExcel(this.sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 

	 /**
	  * 读取指定块的内容,同时将焦点放到该sheet上,如果中间有空行，则跳过<br/>
	  *指定待读取sheet名称，标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetName：待读取sheet名字<br/> 
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
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
	  * 读取指定块的内容,同时将焦点放到该sheet上,返回arrayList如果中间有空行，则跳过<br/>
	  *指定待读取sheet名称，标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetName：待读取sheet名字<br/> 
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
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
	  * 读取指定块内容,如果中间有空行，则跳过<br/>
	  * 指定工作表，起始的行数，列数，终止行数，列数<br/>
	  * 直接指定标准的sheet数，行数和列数，从1开始计数，不用从0起<br/>
	  * 但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetNum：实际sheet数<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[]
	  * 有重载
	  */
	//读取一块excel，每次读一行,循环读
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

			// int cellCount;//要读取的行的cell数
			// 本处考虑去掉
			// ///////////////////////
			// if(columnEndNum<row.getLastCellNum())//如果要读取的列数小于该列含有的cell数目，那么就读少的
			// {
			// cellCount = columnEndNum;
			// }
			// else //不然就全读取
			// {
			// cellCount=row.getLastCellNum();
			// }
			// //////////////////////
			// 如果读取的列数大于实际列数，那么就用实际列数来取代读取的列数
			// if (rowEndNum>sheet.getLastRowNum())
			// {
			// rowEndNum=sheet.getLastRowNum();
			// }

			int readrownum = rowEndNum - rowStartNum + 1;// 读的实际行数
			int readcolumnnum = columnEndNum - columnStartNum + 1;// 读取的实际列数
			strExcelLine = new String[readrownum][readcolumnnum];
			for (int i = 0; i < readrownum; i++) {
				row = sheet.getRow(rowStartNum + i);
				if (row == null)// 中间有空行，这个row就是null
				{
					continue;
				}
				for (int j = 0; j < readcolumnnum; j++) // 考虑将cellcount换成readcolumnnum
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
							case Cell.CELL_TYPE_NUMERIC: // 如果单元格里的数据类型为数据
								strExcelLine[i][j] = String.valueOf(row.getCell((short) (j + columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_STRING:
								strExcelLine[i][j] = row.getCell((short) (j + columnStartNum)).getStringCellValue().trim();
								break;
							case Cell.CELL_TYPE_BOOLEAN:// 如果单元格里的数据类型为 Boolean
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
	  * 读取指定块内容,返回arrayList,如果中间有空行，则跳过<br/>
	  * 指定工作表，起始的行数，列数，终止行数，列数<br/>
	  * 直接指定标准的sheet数，行数和列数，从1开始计数，不用从0起<br/>
	  * 但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetNum：实际sheet数<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[]
	  * 有重载
	  */
	//读取一块excel，每次读一行,循环读
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
			int readrownum=rowEndNum-rowStartNum+1;//读的实际行数
			int readcolumnnum=columnEndNum-columnStartNum+1;//读取的实际列数
			//LsExcelLine = new String[readrownum][readcolumnnum];
			
			for(int i=0;i<readrownum;i++) {
				row=sheet.getRow(rowStartNum+i);
				if (row==null) {//中间有空行，这个row就是null
					continue;
				}
				String[] tmpLine = new String[readcolumnnum];
				for (int j = 0; j < readcolumnnum; j++) {//考虑将cellcount换成readcolumnnum
					try {
						if (row.getCell((short)(j+columnStartNum)) != null) 
						{ // add this condition
							// judge
							switch (row.getCell((short)(j+columnStartNum)).getCellType()) {
							case Cell.CELL_TYPE_FORMULA:
								//strExcelLine[i][j] = "FORMULA";
								tmpLine[j] =  String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_NUMERIC:  //如果单元格里的数据类型为数据  
								tmpLine[j]= String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
								break;
							case Cell.CELL_TYPE_STRING:
								tmpLine[j] = row.getCell((short)(j+columnStartNum)).getStringCellValue().trim();
								break;
							case Cell.CELL_TYPE_BOOLEAN://如果单元格里的数据类型为 Boolean                     
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
	  *  读取单个内容，默认工作表，指定行、列。
	  *  所有指定的行号和列号都只要真实编号，不需要减去1
	  * @param rowNum
	  * @param cellNum
	  * @return String
	  */
	 public String  ReadExcel(int rowNum, int cellNum) {
		 return  ReadExcel(this.sheetNum+1, rowNum, cellNum);
	 }
	 /**
	  * 读取单个内容，指定工作表sheetNum、行、列。
	  * 所有指定的Sheet编号，行号和列号都只要真实编号，不需要减去1
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
////////////////////写入excel方法 /////////////////////////////////////
	    /**
	     * 默认保存
	     * 单个数值写入单个excel文件,默认写入sheet1,写入其他sheet不改变exceloperate焦点
	     * 设置写入行数，列数和内容，写入的内容默认为String
	     * 其中行数，列数，都为实际数目，不用减去1
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(int rowNum, int cellNum,String content) {
		 return WriteExcel(null, 1,rowNum, cellNum,content);
	 }
 
	 /**
	     * 块文件写入excel文件，并设定sheetName，如果没有该sheetName，那么就新建一个
	     * 设置写入的sheet名字，行数，列数和内容，写入的内容默认为String[][]
	     * String[][]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetName
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,List<String[]> content) {
		 return WriteExcel(sheetName, -1, rowNum, cellNum, content);
	 }
	 /**
	     * 块文件写入excel文件，并设定sheetName，如果没有该sheetName，那么就新建一个
	     * 设置写入的sheet名字，行数，列数和内容，写入的内容默认为String[][]
	     * String[][]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetName
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,String[][] content) {
		 return WriteExcel(sheetName, -1, rowNum, cellNum, content);
	 }
	/**
	 * 块文件写入excel文件,默认写入第一个sheet 设置写入的行数，列数和内容，写入的内容默认为String[][]
	 * String[][]中的null会自动跳过 其中sheet数，行数，列数，都为实际数目，不用减去1
	 * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	 * 
	 * @param rowNum 实际行
	 * @param cellNum 实际列
	 * @param content
	 */
	public boolean WriteExcel(int rowNum, int cellNum, String[][] content) {
		return WriteExcel(null, 1, rowNum, cellNum, content);
	}
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  */
	 public boolean WriteExcel(int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, 1, rowNum, cellNum, content);
	}
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param rowNum
	  * @param cellNum
	  * @param content
	  */
	 public boolean WriteExcel(int sheetNum, int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, sheetNum, rowNum, cellNum, content);
	}
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * 行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum 实际行
	  * @param cellNum 实际列
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
	  * 块文件写入excel文件
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * 行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum 实际行
	  * @param cellNum 实际列
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
	  * 块文件写入excel文件
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * 行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum 实际sheet数，也就是必须大于等于1
	  * @param sheetName
	  * @param rowNum 实际行
	  * @param cellNum 实际列
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
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * @param sheetName 没有设为null
	  * @param sheetNum 没有设为小于1
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
				sheet = wb.createSheet("sheet" + (getSheetCount() + 1));// 新建sheet
			}
		}
		return sheet;
	}
	/**
	 * 写入单个元素
	 * @param sheet
	 * @param rowNum 实际行
	 * @param cellNum 实际列
	 * @param content
	 * @return
	 */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, String content) {
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
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
	  * 写入list等
	  * @param sheet
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  * @return
	  */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, Iterable<String[]> content) {
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
		boolean flag;
		if (rowNum < 0)
			return false;
		try {
			int i = 0;
			for (String[] rowcontent : content) {
				int writerow = i + rowNum;// 写入的行数
				Row row = sheet.getRow(writerow);
				if (row == null) {
					row = sheet.createRow(writerow);
				}
				if (rowcontent == null)
					continue;
				for (int j = 0; j < rowcontent.length; j++) // 写入
				{
					if (rowcontent[j] == null)
						continue; // 跳过空值
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
	 * 写入数组等
	 * @param sheet
	 * @param rowNum 实际行
	 * @param cellNum 实际列
	 * @param content
	 * @return
	 */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, String[][] content) {
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
		boolean flag;
		if (rowNum < 0)
			return false;
		try {
			int i = 0;
			for (String[] rowcontent : content) {
				int writerow = i + rowNum;// 写入的行数
				Row row = sheet.getRow(writerow);
				if (row == null) {
					row = sheet.createRow(writerow);
				}
				if (rowcontent == null)
					continue;
				for (int j = 0; j < rowcontent.length; j++) // 写入
				{
					if (rowcontent[j] == null)
						continue; // 跳过空值
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
	     * 条文件写入excel文件，
	     * 设置写入的sheet数，行数，列数和内容，写入的内容默认为String[],设定写入行/列
	     * String[]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw true为写入某一行，设定false为写入某一列
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, String[] content, boolean raw) {
		 resetExcel();
		 	sheetNum--;rowNum--;cellNum--;//将sheet和行列都还原为零状态
		 	int writeNumber=content.length;//这个就是数组第一维的数量
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//新建sheet					 
	    		 }
	    		  
	    		 if(raw==true) {//横着写入一行
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
	     * 条文件写入excel文件，
	     * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String>,设定写入行/列
	     * String[]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw true为写入某一行，设定false为写入某一列
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, List<String> content, boolean raw) {
		 resetExcel();
		 	sheetNum--;rowNum--;cellNum--;//将sheet和行列都还原为零状态
		 	int writeNumber=content.size();//这个就是数组第一维的数量
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//新建sheet					 
	    		 }
	    		  
	    		 if(raw==true)//横着写入一行
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
	 
	 
	 
	 
	 
	 
///////////////////////保存文件方法/////////////////////////////////////	 
	    /**
	     * 保存excel文件，使用以前的文件名。有重载
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
	      * 输入文件名
	     * 保存excel文件，另存为
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
///////////////////关闭对象////////////////////////////////
	 /**
	  * 暂时没功能
	  */
	 public void Close() {//暂时不会
		 wb = null;// book [includes sheet]
		 sheet = null;
		 row = null;
		 cell= null;
	}
  }

	 
 
