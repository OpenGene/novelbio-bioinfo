package com.novelbio.base.dataOperate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




	
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
	//**
	 //* @author caihua //ò����ԭ���ߣ������뾭��Zong Jie�޸�
	 //*/
	////////////////////�������////////////////////////
	 private Workbook wb = null;// book [includes sheet]
	 private Sheet sheet = null;
	 private Row row = null;
	 private Cell cell= null;
	 private int sheetNum = 0; // ��sheetnum��������
	 private int rowNum = 0;
	 private FileInputStream fis = null;
     private String filename="";
		
	
    
	 public ExcelOperate()//���캯������ʱ��֪����ʲô��
	 {
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
		    	  if (excel2007) {
		    		  wb=new XSSFWorkbook(fos);
		    		  sheet = wb.getSheetAt(0);
		    		  sheetNum = 0;
		    	  }
		    	  else {
		    		  wb= new HSSFWorkbook(fos);       //�õ� excel ��������Ӧ�� HSSFWorkbook ����  
		    		  sheet = wb.getSheetAt(0);
		    		  sheetNum = 0;
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
	 
	 /**
	  * Ĭ���½�03��excel
	  * @param filenameinput
	  * @return
	  */
	 public boolean newExcelOpen(String filenameinput)
	 {
		 return newExcelOpen(filenameinput,false);

	 }
	 
	 /**
	  * �½�excel Ĭ���½�03��
	  * @return
	  */
	 /**
	 public boolean newExcelOpen()
	 {
		 wb=new HSSFWorkbook();  
		 //�� Excel �������н���һ��������,����Ϊȱʡֵ sheet1  
		 sheet =wb.createSheet("sheet1");  

		   return true;
	 }
	 
	  */
	 
	 public boolean newExcelOpen(String filenameinput,boolean excel2007) 
	 {   
		 filename=filenameinput;
	      if (!excel2007) {
				wb=new HSSFWorkbook();  
				  //�� Excel �������н���һ��������,����Ϊȱʡֵ sheet1  
				// sheet = wb.createSheet("sheet1");  
			}
		      else {
		    	  wb=new XSSFWorkbook();
				//wb= new XSSFWorkbook(filenameinput);
		    	  //�� Excel �������н���һ��������,����Ϊȱʡֵ sheet1  
				//	 sheet = wb.createSheet("sheet1");  
			}
	
		 return true;
	 }
/////////////////////////excel�ĸ������ԣ�����sheet��Ŀ��ĳsheet�µ�����///////////////////////
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
	  * ���Ĭ��sheetNum�ĵ�һ�е�����
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ�������,������в����ڣ��򷵻�0
	  */
	 public int getColCount()
	 {    
		 int rownum=1;
		return getColCount(this.sheetNum+1,rownum);
	 }
	 
	 /**
	  * ���Ĭ��sheetNum�ĵ�rowNum�е�����
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ���������������в����ڣ��򷵻�0
	  */
	 public int getColCount(int rownum)
	 {    
		 return getColCount(this.sheetNum+1,rownum);
	 }
	 
	 
	 
	 
	 
	 /**
	  * ���ָ��sheetNum��rowNum�µ�����
	  * @param sheetNum ָ��ʵ��sheet��
	  * @param rowNum ָ��ʵ������
	  * @return ���ظ���������������в����ڣ��򷵻�0
	  */
	 public int getColCount(int sheetNum,int rowNum)
	 {     rowNum--; sheetNum--;
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
	   Row row=sheet.getRow(rowNum);
	   if (row == null)
		 {
			 System.out.println("=============>rowΪ��");
			 return 0;
		 }
	  
	   int ColCount = -1;
	   ColCount = row.getLastCellNum()+1;
	   return ColCount;
	 }
///////////////////////�½�sheet///////////////////////////////
	
	 /**
	  * �½�sheet��ָ��sheet�����½���excel���㻹����ԭ����sheet��
	  * ����Sheetʵ�ʱ��
	  */
	 public int createNewSheet()
	 {
		  int sheetNum=wb.getNumberOfSheets()+1;
		  String sheetname="Sheet"+sheetNum;
		  wb.createSheet(sheetname);
		  return wb.getSheetIndex(sheetname)+1;
	 }
	 /**
	  * �½�sheetͬʱָ��sheet�����½���excel���㻹����ԭ����sheet��
	  * ����Sheetʵ�ʱ��
	  */
	 public int createNewSheet(String sheetName)
	 {
		 if (wb.getSheetIndex(sheetName) >= 0) {
			return wb.getSheetIndex(sheetName);
		}
		  wb.createSheet(sheetName);
		  return wb.getSheetIndex(sheetName)+1;
	 }
	 /**
	  * �۽���ʵ��sheet�ϣ����Դ�createNewSheet���,���sheetNum����������sheet�����򷵻�
	  * @param sheetNum
	  */
	 public boolean setSheetNum(int sheetNum) {
		 if ( wb.getNumberOfSheets() <= sheetNum) {
			return false;
		}
		sheet = wb.getSheetAt(sheetNum);
		this.sheetNum = sheetNum;
		return true;
	}
	 
	 /**
	  * �۽���ʵ��sheet�ϣ����Դ�createNewSheet���,���sheetNum����������sheet�����򷵻�
	  * @param sheetNum
	  */
	 public boolean setSheetNum(String sheetName) {
		 if ( wb.getSheetIndex(sheetName) < 0) {
			return false;
		}
		sheet = wb.getSheet(sheetName);
		sheetNum = wb.getSheetIndex(sheetName);
		return true;
	}
/////////////////////��ȡexcel///////////////////////////////////////////////////////
	 
	 /**
	  * ��ȡĬ��sheet��ָ���������,����м��п��У�������<br/>
	  *ֱ��ָ����׼����������������1��ʼ���������ô�0��<br/>
	  *��������õ���������Ǵ�0��ʼ�ģ���ͬ��C#<br/>
	  * @param rowStartNum�����ʵ������<br/> 
	  * @param columnStartNum�����ʵ������<br/> 
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public String[][]  ReadExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
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
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]>  ReadLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
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
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public String[][]  ReadExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
		
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
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]>  ReadLsExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
		
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
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[]
	  * ������
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public String[][] ReadExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
		sheetNum--;rowStartNum--;columnStartNum--;rowEndNum--;columnEndNum--;
		if (sheetNum < 0)
		{
			sheetNum = wb.getSheetIndex(sheet);
		}
		if (rowStartNum < 0) {
			rowStartNum = 0;
		}
		String[][] strExcelLine = null;
		try 
		{
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(rowStartNum);
	   
	  // int cellCount;//Ҫ��ȡ���е�cell��
	   //��������ȥ��
	   /////////////////////////
	  // if(columnEndNum<row.getLastCellNum())//���Ҫ��ȡ������С�ڸ��к��е�cell��Ŀ����ô�Ͷ��ٵ�
	   //{
	    //cellCount = columnEndNum;
	  // }
	   //else                                //��Ȼ��ȫ��ȡ
	  // {
		// cellCount=row.getLastCellNum();
	  // }
	   ////////////////////////
	   //�����ȡ����������ʵ����������ô����ʵ��������ȡ����ȡ������
	  // if (rowEndNum>sheet.getLastRowNum())
		// {
		//	 rowEndNum=sheet.getLastRowNum();
		// }
	   
	   int readrownum=rowEndNum-rowStartNum+1;//����ʵ������
	   int readcolumnnum=columnEndNum-columnStartNum+1;//��ȡ��ʵ������
	   strExcelLine = new String[readrownum][readcolumnnum];
	   for(int i=0;i<readrownum;i++)
	   {
           row=sheet.getRow(rowStartNum+i);
           if (row==null)//�м��п��У����row����null
           {
        	   continue;
           }
		   for (int j = 0; j < readcolumnnum; j++) //���ǽ�cellcount����readcolumnnum
	      {
		 try {  
			   if (row.getCell((short)(j+columnStartNum)) != null) 
			   { // add this condition
				    // judge
				    switch (row.getCell((short)(j+columnStartNum)).getCellType())
				    {
				    case Cell.CELL_TYPE_FORMULA:
				    	//strExcelLine[i][j] = "FORMULA";
				    	strExcelLine[i][j] =  String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
				     break;
				    case Cell.CELL_TYPE_NUMERIC:  //�����Ԫ�������������Ϊ����  
				    	strExcelLine[i][j]= String.valueOf(row.getCell((short)(j+columnStartNum)).getNumericCellValue()).trim();
				        break;
				    case Cell.CELL_TYPE_STRING:
				    	strExcelLine[i][j] = row.getCell((short)(j+columnStartNum)).getStringCellValue().trim();
				     break;
				    case Cell.CELL_TYPE_BOOLEAN://�����Ԫ�������������Ϊ Boolean                     
				    	strExcelLine[i][j] = String.valueOf(row.getCell((short)(j+columnStartNum)).getBooleanCellValue()).trim();  
				    case Cell.CELL_TYPE_BLANK:
				    	strExcelLine[i][j] = "";
				     break;
				    default:
				    	strExcelLine[i][j] = "error";
				     break;
				    }
				   
			   }
				    
		    }
				   catch (Exception e) 
				   {
				   e.printStackTrace();
				  }	  
	      }
	   }
	  
	   }
	        catch (Exception e) 
	    {
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
	  * @param rowEndNum���յ�ʵ������<br/> 
	  * @param columnEndNum���յ�ʵ������<br/>
	  * ������������ļ�ʵ������������������������Ϊnull<br/>
	  * @return String[]
	  * ������
	  */
	//��ȡһ��excel��ÿ�ζ�һ��,ѭ����
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) 
	 {
		sheetNum--;rowStartNum--;columnStartNum--;rowEndNum--;columnEndNum--;
		if (sheetNum < 0)
		{
			sheetNum = wb.getSheetIndex(sheet);
		}
		if (rowStartNum < 0) {
			rowStartNum = 0;
		}
		ArrayList<String[]> LsExcelLine = new ArrayList<String[]>();
		try 
		{
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(rowStartNum);
	   
			// int cellCount;//Ҫ��ȡ���е�cell��
			//��������ȥ��
			/////////////////////////
			// if(columnEndNum<row.getLastCellNum())//���Ҫ��ȡ������С�ڸ��к��е�cell��Ŀ����ô�Ͷ��ٵ�
			//{
			//cellCount = columnEndNum;
			// }
			//else                                //��Ȼ��ȫ��ȡ
			// {
			// cellCount=row.getLastCellNum();
			// }
			////////////////////////
			//�����ȡ����������ʵ����������ô����ʵ��������ȡ����ȡ������
			// if (rowEndNum>sheet.getLastRowNum())
			// {
			//	 rowEndNum=sheet.getLastRowNum();
			// }
			
			int readrownum=rowEndNum-rowStartNum+1;//����ʵ������
			int readcolumnnum=columnEndNum-columnStartNum+1;//��ȡ��ʵ������
			//LsExcelLine = new String[readrownum][readcolumnnum];
			
			for(int i=0;i<readrownum;i++)
			{
				row=sheet.getRow(rowStartNum+i);
				if (row==null)//�м��п��У����row����null
				{
					continue;
				}
				String[] tmpLine = new String[readcolumnnum];
				for (int j = 0; j < readcolumnnum; j++) //���ǽ�cellcount����readcolumnnum
				{
					
					try {
						if (row.getCell((short)(j+columnStartNum)) != null) 
						{ // add this condition
							// judge
							switch (row.getCell((short)(j+columnStartNum)).getCellType())
							{
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
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
				LsExcelLine.add(tmpLine);
			}
			
		}
		catch (Exception e) 
		{
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
		 if (sheetNum < 0)
		 {
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
	    switch (row.getCell((short) cellNum).getCellType()) 
	    {
	    case Cell.CELL_TYPE_FORMULA:
	     strExcelCell = "FORMULA ";
	    strExcelCell = String.valueOf(row.getCell((short) cellNum).getNumericCellValue()).trim();
	    break;
	    case Cell.CELL_TYPE_NUMERIC: {
	     strExcelCell = String.valueOf(row.getCell((short) cellNum)
	       .getNumericCellValue());
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
	 public boolean WriteExcel(int rowNum, int cellNum,String content) 
	 {
		 if ( wb.getNumberOfSheets() == 0) {
			 sheet = wb.createSheet("sheet1"); 
			 sheetNum = wb.getSheetIndex("sheet1");
		}
		 return WriteExcel(true,this.sheetNum+1,rowNum, cellNum,content);
	 }
	    /**
	     * ������ֵд�뵥��excel�ļ�,Ĭ��д��sheet1,д������sheet���ı�exceloperate����
	     * ����д�����������������ݣ�д�������Ĭ��ΪString
	     * ������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(boolean save,int rowNum, int cellNum,String content) 
	 {
		 if ( wb.getNumberOfSheets() == 0) {
			 sheet = wb.createSheet("sheet1");  
			 sheetNum = wb.getSheetIndex("sheet1");
		}
		 return WriteExcel(save,this.sheetNum+1,rowNum, cellNum,content);
	 }
	 
	    /**
	     * ������ֵд�뵥��excel�ļ�,����д��sheet������,���û�и�sheetName����ô���½�һ��
	     * д������sheet���ı�exceloperate����
	     * ����д�����������������ݣ�д�������Ĭ��ΪString
	     * ������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * @param save �Ƿ񱣴�
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(boolean save,String sheetName,int rowNum, int cellNum,String content) 
	 {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			 sheet = wb.createSheet(sheetName); 
			 sheetNum = wb.getSheetIndex(sheetName);
		}
		 return WriteExcel(save,sheetNum+1,rowNum, cellNum,content);
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
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,String[][] content) 
	 {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) 
		 {
			 sheet = wb.createSheet(sheetName);  
			 sheetNum = wb.getSheetIndex(sheetName);
		}
		 return WriteExcel(true,sheetNum+1,rowNum, cellNum,  content); 
	 }
	 
	 /**
	     * ���ļ�д��excel�ļ������趨sheetName�����û�и�sheetName����ô���½�һ��
	     * ����д���sheet���֣����������������ݣ�д�������Ĭ��ΪString[][]
	     * String[][]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param save �Ƿ񱣴�
	     * @param sheetName
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(boolean save,String sheetName, int rowNum, int cellNum,String[][] content) 
	 { 
		 sheetNum = wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			 sheet = wb.createSheet(sheetName);
			 sheetNum = wb.getSheetIndex(sheetName);
		}
		 return WriteExcel(save,sheetNum+1,rowNum, cellNum,  content); 
	 }
	 
	 
	 /**
	     * ���ļ�д��excel�ļ�,Ĭ�ϱ���
	     * ����д������������������ݣ�д�������Ĭ��ΪString[][]
	     * String[][]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel( int rowNum, int cellNum,String[][] content) 
	 {
		 return WriteExcel(true,this.sheetNum+1,rowNum, cellNum,  content); 
	 }
	 
	 /**
	     * ���ļ�д��excel�ļ�,Ĭ�ϱ���
	     * ����д������������������ݣ�д�������Ĭ��ΪString[][]
	     * String[][]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(boolean save, int rowNum, int cellNum,String[][] content) 
	 {
		 return WriteExcel(save,this.sheetNum+1,rowNum, cellNum,  content); 
	 }
	 /**
	     * ���ļ�д��excel�ļ�
	     * ����д������������������ݣ�д�������Ĭ��ΪList<String[]>, ����String[]Ϊ�У�list.get(i)Ϊ��
	     * String[]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param save �Ƿ񱣴�
	     * @param rowNum
	     * @param cellNum
	     * @param content

		 */
	 public boolean WriteExcel(boolean save, int rowNum, int cellNum,List<String[]> content) 
	 {
		 return WriteExcel(save,this.sheetNum+1,rowNum, cellNum,  content); 
	 }

	 /**
	     * ���ļ�д��excel�ļ�
	     * ����д������������������ݣ�д�������Ĭ��ΪList<String[]>, ����String[]Ϊ�У�list.get(i)Ϊ��
	     * String[]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param save �Ƿ񱣴�
		 */
	 public boolean WriteExcel( int rowNum, int cellNum,List<String[]> content,boolean save) 
	 {
		 return WriteExcel(save,this.sheetNum+1,rowNum, cellNum,  content); 
	 }
	 
	 /**
	     * ���ļ�д��excel�ļ�
	     * ����д������������������ݣ�д�������Ĭ��ΪList<String[]>, ����String[]Ϊ�У�list.get(i)Ϊ��
	     * String[]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param save �Ƿ񱣴�
		 */
	 public boolean WriteExcel( String sheetName,int rowNum, int cellNum,List<String[]> content,boolean save) 
	 {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) 
		 {
			 sheet = wb.createSheet(sheetName);  
			 sheetNum = wb.getSheetIndex(sheetName);
		}
		 return WriteExcel(save,this.sheetNum+1,rowNum, cellNum,  content); 
	 }

    /**
     * ������ֵд�뵥��excel�ļ���д������sheet���ı�exceloperate����
     * ����д���sheet�������������������ݣ�д�������Ĭ��ΪString
     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
     * @param save �Ƿ񱣴�
     * @param sheetNum
     * @param rowNum
     * @param cellNum
     * @param content
	 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum,String content) 
    {
    	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
    	 if (sheetNum < 0 || rowNum < 0)
    		   return false;
    	 
    	 if((sheet = wb.getSheetAt(sheetNum))==null)
    	 {
    		 sheet=wb.createSheet();
    	 }
    	 try {
  			  //row = sheet.createRow(rowNum);
  			 row=sheet.getRow(rowNum);
		     if(row==null)
		       {
			    row=sheet.createRow(rowNum); 
	           }
  			  cell=row.createCell((short)cellNum);
  			  try
  			  {
  				  double tmpValue = Double.parseDouble(content);
  				  //cell.setCellType(0);
  				  cell.setCellValue(tmpValue);
  			  } catch (Exception e) {
  				  cell.setCellValue(content);
  			  }
  			  if(filename!=""&&save)Save();
  			  return true;
    	 }
    	 catch (Exception e) 
    	 {
    		 e.printStackTrace();
    		 return false;
    	 }
	}
	 
	 /**
	     * ���ļ�д��excel�ļ�
	     * ����д���sheet�������������������ݣ�д�������Ĭ��ΪString[][],String[][]�е�null���Զ�����
	     * ����sheet������������������Ϊʵ����Ŀ�����ü�ȥ1
	     * ��sheetNum���ó����Ѵ���sheet��Ŀʱ����Ϊ�½�sheetд��
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(boolean save, int sheetNum, int rowNum, int cellNum,String[][] content) 
	 {
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeRowNumber=content.length-1;//������������һά������
		 	int writeColiumNubmer=content[0].length-1;//����ڶ�ά������
		 	boolean flag;
		 
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		  try {
	    			   
						  sheet=wb.getSheetAt(sheetNum);  						 
					  } 
	    		  catch (Exception e) 
				     {
					      sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
				     }
		
				  for (int i=0;i<=writeRowNumber;i++) 
				  {  int writerow=i+rowNum;//д�������
				     row=sheet.getRow(writerow);
				     if(row==null)
				       {
					    row=sheet.createRow(writerow); 
			           }
					   for (int j = 0; j <=writeColiumNubmer; j++) //д��
					   {   
						   if(content[i][j]==null) continue;//������ֵ
						   cell=row.createCell((short)(cellNum+j));
			 				 try
		    				 {
									double tmpValue = Double.parseDouble(content[i][j]);
									//cell.setCellType(0);
									cell.setCellValue(tmpValue);
							} catch (Exception e) {
								cell.setCellValue(content[i][j]);
							}
					   }
				  }
	  			  if(filename!=""&&save)Save();
	  			  return true;
	    		  }
	    	 catch (Exception e) 
	    		  {
	    		   e.printStackTrace();
	    		   return false;
	    		  }
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
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, List<String[]> content) 
	 {
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeRowNumber=content.size();//������������һά������
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		  try {
						  sheet=wb.getSheetAt(sheetNum);  						 
					  } 
	    		  catch (Exception e) 
				     {
					      sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
				     }
		
				  for (int i=0; i <writeRowNumber;i++) 
				  {  int writerow=i+rowNum;//д�������
				     row=sheet.getRow(writerow);
				     if(row==null)
				       {
					    row=sheet.createRow(writerow); 
			           }
				      String[] rowcontent=content.get(i);
				      if(rowcontent==null) continue;
					   for (int j = 0; j <rowcontent.length; j++) //д��
					   {   
						   if(rowcontent[j]==null) continue; //������ֵ
						   cell=row.createCell((short)(cellNum+j));
			 				 try
		    				 {
									double tmpValue = Double.parseDouble(rowcontent[j]);
									//cell.setCellType(0);
									cell.setCellValue(tmpValue);
							} catch (Exception e) {
								cell.setCellValue(rowcontent[j]);
							}
					   }
				  }
	  			  if(filename!=""&&save)Save();
	  			  return true;
	    		  }
	    	 catch (Exception e) 
	    		  {
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
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, String[] content, boolean raw) 
	 {
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeNumber=content.length;//������������һά������
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) 
	    		 {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
	    		 }
	    		  
	    		 if(raw==true)//����д��һ��
	    		 {	
	    			 row=sheet.getRow(rowNum);
	    			 if(row==null)
	    			 {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++)
	    			 {
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum+i));
	     				 try
	    				 {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else 
	    		 {
	    			 for(int i=0;i<writeNumber;i++)
	    			 {
	    				 row=sheet.getRow(rowNum+i);
	    				 if(row==null)
	    				 {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum));
	     				 try
	    				 {
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
	    	 catch (Exception e) 
	    	 {
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
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, List<String> content, boolean raw) 
	 {
		 	sheetNum--;rowNum--;cellNum--;//��sheet�����ж���ԭΪ��״̬
		 	int writeNumber=content.size();//������������һά������
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) 
	    		 {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//�½�sheet					 
	    		 }
	    		  
	    		 if(raw==true)//����д��һ��
	    		 {	
	    			 row=sheet.getRow(rowNum);
	    			 if(row==null)
	    			 {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++)
	    			 {
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum+i));
	    				 try
	    				 {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else 
	    		 {
	    			 for(int i=0;i<writeNumber;i++)
	    			 {
	    				 row=sheet.getRow(rowNum+i);
	    				 if(row==null)
	    				 {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 cell=row.createCell((short)(cellNum));
	     				 try
	    				 {
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
	    	 catch (Exception e) 
	    	 {
	    		 e.printStackTrace();
	    		 return false;
	    	 }
	 }
	 
	 
	 
	 
	 
	 
///////////////////////�����ļ�����/////////////////////////////////////	 
	    /**
	     * ����excel�ļ���ʹ����ǰ���ļ�����������
		 */
	 public boolean Save() 
	 {    if(filename=="") return false;
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
	 public boolean Save(String newfilename) 
	 {
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
	 public void Close() //��ʱ����
	 {
		 wb = null;// book [includes sheet]
		 sheet = null;
		 row = null;
		 cell= null;
	}
  }

	 
 
