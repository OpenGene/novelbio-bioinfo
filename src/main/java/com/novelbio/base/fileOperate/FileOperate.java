package com.novelbio.base.fileOperate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class FileOperate {
	 private static String message;
	    public FileOperate() {
	    } 
	    
	    /**
	     * ��ȡ�ı��ļ�����
	     * @param filePathAndName ������������·�����ļ���
	     * @param encoding �ı��ļ��򿪵ı��뷽ʽ
	     * @return �����ı��ļ�������
	     */
	    public String readTxt(String filePathAndName,String encoding) throws IOException{
	     encoding = encoding.trim();
	     StringBuffer str = new StringBuffer("");
	     String st = "";
	     try{
	      FileInputStream fs = new FileInputStream(filePathAndName);
	      InputStreamReader isr;
	      if(encoding.equals("")){
	       isr = new InputStreamReader(fs);
	      }else{
	       isr = new InputStreamReader(fs,encoding);
	      }
	      BufferedReader br = new BufferedReader(isr);
	      try{
	       String data = "";
	       while((data = br.readLine())!=null){
	         str.append(data+" "); 
	       }
	      }catch(Exception e){
	       str.append(e.toString());
	      }
	      st = str.toString();
	     }catch(IOException es){
	      st = "";
	     }
	     return st;     
	    }
	    /**
	     * ����·��������������һ��·��������"/"
	     * ���Ը��������ڵ�·��
	     * @param fileName
	     * @return
	     */
	    public static String getParentName(String fileName) {
			File file = new File(fileName);
			return file.getParent();
		}
	    /**
	     * ����·����������������
	     * �����/home/zong0jie/��/home/zong0jie
	     * ������zong0jie
	     * ���Ը��������ڵ�·��
	     * @param fileName
	     * @return
	     */
	    public static String getName(String fileName) {
			File file = new File(fileName);
			return file.getName();
		}
	    /**
	     * ��ȡ�ļ����������ļ������׺,������·��
	     * 	     * ����ļ��������򷵻�null<br>
	     * @param filePath Ŀ¼·��,���Ҫ��\\��/
	     * @return arraylist ������string[2] 1:�ļ���     2����׺
	     */
	    public static ArrayList<String[]> getFoldFileName(String filePath)
	    {
	    	return getFoldFileName(filePath,"*","*");
	    }
	    
	    /**
	     * ��ȡ�ļ����°���ָ���ļ������׺�������ļ���,�ȴ����ӹ������ļ����µ��ļ���Ҳ����ѭ������ļ�<br>
	     * ����ļ��������򷵻�null<br>
	     * ��������ļ��У��򷵻ظ��ļ���<br>
	     * @param filePath Ŀ¼·��,���Ҫ��\\��/
	     * @param filename ָ���������ļ�������������ʽ ���� "*",������ʽ���Ӵ�Сд
	     * @param suffix ָ�������ĺ�׺������������ʽ<br>
	     *  �ļ� wfese.fse.fe���� "wfese.fse"��"fe"<br>
	     *  �ļ� wfese.fse.����	"wfese.fse."��""<br>
	     *  �ļ� wfese ���� "wfese"��""<br>
	     * @return ���ذ���Ŀ���ļ�����ArrayList��������string[2] 1:�ļ���     2����׺
	     */
	    public static ArrayList<String[]> getFoldFileName(String filePath,String filename,String suffix)
	    {
	    	//ƥ���ļ������׺��
	    	 Pattern pattern =Pattern.compile("(.*)\\.(\\w*)", Pattern.CASE_INSENSITIVE);
	    	 Matcher matcher;
	    	 String name=""; //�ļ���
	    	 String houzhuiming=""; //��׺��
	    	 String[] filenamefinal;
	    	 if(filename.equals("*"))
	    	 {
	    		 filename=".*";
	    	 }
	    	 if(suffix.equals("*"))
	    	 {
	    		 suffix=".*";
	    	 }
	    	 //================================================================//  
	    	   
	    	 ArrayList<String[]> ListFilename=new ArrayList<String[]>();
	    	 File file=new File(filePath);
	    	 if (!file.exists()) {//û���ļ����򷵻ؿ�
	    		 return null;
	    	 }
	    	 //���ֻ���ļ��򷵻��ļ���
	    	 if(!file.isDirectory())
	    	 {      //��ȡ�ļ������׺��
	    		 String fileName=file.getName();
	    		 if(!fileName.contains(".")||fileName.endsWith("."))
	    		 {
	    				name=fileName;
	    				houzhuiming="";
	    		 }
	    		 else {
	    			 matcher=pattern.matcher(fileName);
	    			 if(matcher.find())
	    			 {
	    				 name=matcher.group(1);
	    				 houzhuiming=matcher.group(2);
	    			 }
	    		 }
	    		 if(name.matches(filename)&&houzhuiming.matches(suffix))
	    		 {
	    				filenamefinal=new String[2];
	    				filenamefinal[0]=name;
	    				filenamefinal[1]=suffix;
	    				ListFilename.add(filenamefinal);
	    				return ListFilename;
	    		 }
	    			return null;
	    	}
	    	 //������ļ���
	    	String[] filenameraw=file.list();
	    	for (int i = 0; i < filenameraw.length; i++) 
	    	{
	    		 if(!filenameraw[i].contains(".")||filenameraw[i].endsWith("."))
	    		 {
	    				name=filenameraw[i];
	    				houzhuiming="";
	    		 }
	    		 else {
	    			 matcher=pattern.matcher(filenameraw[i]);
	    			 if(matcher.find())
	    			 {
	    				 name=matcher.group(1);
	    				 houzhuiming=matcher.group(2);
	    			 }
	    		 }
   		      //��ʼ�ж�
	    		 if(name.matches(filename)&&houzhuiming.matches(suffix))
	    		 {
	    			filenamefinal=new String[2];
	    			filenamefinal[0]=name;
	    			filenamefinal[1]=houzhuiming;
	    			ListFilename.add(filenamefinal);
	    		}
	    	}
	    	return  ListFilename;
	    }
	    
	    
	    
	    

	    /**
	     * �½�Ŀ¼,������ļ��д����򷵻�
	     * @param folderPath Ŀ¼·��,���Ҫ��\\��/
	     * @return ����Ŀ¼�������·��
	     */
	    public static String createFolder(String folderPath) {
	        String txt = folderPath;
	        try {
	            File myFilePath = new File(txt);
	            txt = folderPath;
	            if (!myFilePath.exists()) {
	                myFilePath.mkdir();
	            }
	        }
	        catch (Exception e) {
	            message = "����Ŀ¼��������";
	        }
	        return txt;
	    }
	    
	    /**
	     * �༶Ŀ¼����
	     * @param folderPath ׼��Ҫ�ڱ���Ŀ¼�´�����Ŀ¼��Ŀ¼·�� ���� c:myf
	     * @param paths ���޼�Ŀ¼����������Ŀ¼�Ե��������� ���� a|b|c
	     * @return ���ش����ļ����·�� ���� c:/myf/a/b/c
	     */
	    public static String createFolders(String folderPath, String paths){
	        String txts = folderPath;
	        try{
	            String txt;
	            txts = folderPath;
	            StringTokenizer st = new StringTokenizer(paths,"|");
	            for(int i=0; st.hasMoreTokens(); i++){
	                    txt = st.nextToken().trim();
	                    if(txts.endsWith(File.separator)){ 
	                        txts = createFolder(txts+txt);
	                    }else{
	                        txts = createFolder(txts+File.separator+txt);    
	                    }
	            }
	       }catch(Exception e){
	           message = "����Ŀ¼��������";
	       }
	        return txts;
	    }

	    
	    /**
	     * �½��ļ�
	     * @param filePathAndName �ı��ļ���������·�����ļ���
	     * @param fileContent �ı��ļ�����
	     * @return
	     */
	    public static void createFile(String filePathAndName, String fileContent) {
	     
	        try {
	            String filePath = filePathAndName;
	            filePath = filePath.toString();
	            File myFilePath = new File(filePath);
	            if (!myFilePath.exists()) {
	                myFilePath.createNewFile();
	            }
	            FileWriter resultFile = new FileWriter(myFilePath);
	            PrintWriter myFile = new PrintWriter(resultFile);
	            String strContent = fileContent;
	            myFile.println(strContent);
	            myFile.close();
	            resultFile.close();
	        }
	        catch (Exception e) {
	            message = "�����ļ���������";
	        }
	    }


	    /**
	     * �б��뷽ʽ���ļ�����
	     * @param filePathAndName �ı��ļ���������·�����ļ���
	     * @param fileContent �ı��ļ�����
	     * @param encoding ���뷽ʽ ���� GBK ���� UTF-8
	     * @return
	     */
	    public static void createFile(String filePathAndName, String fileContent, String encoding) {
	     
	        try {
	            String filePath = filePathAndName;
	            filePath = filePath.toString();
	            File myFilePath = new File(filePath);
	            if (!myFilePath.exists()) {
	                myFilePath.createNewFile();
	            }
	            PrintWriter myFile = new PrintWriter(myFilePath,encoding);
	            String strContent = fileContent;
	            myFile.println(strContent);
	            myFile.close();
	        }
	        catch (Exception e) {
	            message = "�����ļ���������";
	        }
	    } 


	    /**
	     * ɾ���ļ�
	     * @param filePathAndName �ı��ļ���������·�����ļ���
	     * �ļ��������򷵻�false
	     * @return Boolean �ɹ�ɾ������true�����쳣����false
	     */
	    public static boolean delFile(String filePathAndName) {
	     boolean bea = false;
	        try {
	            String filePath = filePathAndName;
	            File myDelFile = new File(filePath);
	            if(myDelFile.exists()){
	             myDelFile.delete();
	             bea = true;
	            }else{
	             bea = false;
	             //message = (filePathAndName+"ɾ���ļ���������");
	            }
	        }
	        catch (Exception e) {
	            message = e.toString();
	        }
	        return bea;
	    }
	    


	    /**
	     * ɾ���ļ���
	     * @param folderPath �ļ�����������·��
	     * @return
	     */
	    public static void delFolder(String folderPath) {
	        try {
	            delAllFile(folderPath); //ɾ����������������
	            String filePath = folderPath;
	            filePath = filePath.toString();
	            java.io.File myFilePath = new java.io.File(filePath);
	            myFilePath.delete(); //ɾ�����ļ���
	        }
	        catch (Exception e) {
	            message = ("ɾ���ļ��в�������");
	        }
	    }
	    
	    
	    /**
	     * ɾ��ָ���ļ����������ļ�,
	     * @param path �ļ�����������·��,�������ν�Ӳ���\\��/
	     * @return
	     * @return
	     */
	    public static boolean delAllFile(String path) {
	    	 if (!path.endsWith(File.separator)) {  
	    		 path = path + File.separator;  
		         }  
	     boolean bea = false;
	        File file = new File(path);
	        if (!file.exists()) {
	            return bea;
	        }
	        if (!file.isDirectory()) {
	            return bea;
	        }
	        String[] tempList = file.list();
	        File temp = null;
	        for (int i = 0; i < tempList.length; i++) {
	            if (path.endsWith(File.separator)) {
	                temp = new File(path + tempList[i]);
	            }else{
	                temp = new File(path + File.separator + tempList[i]);
	            }
	            if (temp.isFile()) {
	                temp.delete();
	            }
	            if (temp.isDirectory()) {
	                delAllFile(path + tempList[i]);//��ɾ���ļ���������ļ�
	                delFolder(path + tempList[i]);//��ɾ�����ļ���
	                bea = true;
	            }
	        }
	        return bea;
	    }


	    /**
	     * ���Ƶ����ļ�
	     * @param oldPathFile ׼�����Ƶ��ļ�Դ
	     * @param newPathFile �������¾���·�����ļ���
	     * @param cover �Ƿ񸲸�
	     * @return
	     */
	    public static boolean copyFile(String oldPathFile, String newPathFile,boolean cover) {
	        try {
	            int bytesum = 0;
	            int byteread = 0;
	            File oldfile = new File(oldPathFile);
	            File newfile= new File(newPathFile);

	            if (oldfile.exists()) 
	            { //�ļ�����ʱ
		            if(newfile.exists())
		            {
		            	if (!cover) {
							return false;
						}
		            	newfile.delete();
		            }
	                InputStream inStream = new FileInputStream(oldPathFile); //����ԭ�ļ�
	                FileOutputStream fs = new FileOutputStream(newPathFile);
	                byte[] buffer = new byte[1444];
	                while((byteread = inStream.read(buffer)) != -1){
	                    bytesum += byteread; //�ֽ��� �ļ���С
	                    //System.out.println(bytesum);
	                    fs.write(buffer, 0, byteread);
	                }
	                inStream.close();
	                return true;
	            }
	           else {
				return false;
	           }
	        }catch (Exception e) {
	            message = ("���Ƶ����ļ���������");
	            return false;
	        }
	    }
	    

	    /**
	     * ���������ļ��е�����,���Ҫ�ļ��Ѿ����ڣ�������
	     * @param oldPath ׼��������Ŀ¼���������ν�Ӳ���"/"
	     * @param newPath ָ������·������Ŀ¼
	     * @return
	     */
	    public static void copyFolder(String oldPath, String newPath, boolean cover) {
	    	 if (!newPath.endsWith(File.separator)) {  
	    		 newPath = newPath + File.separator;  
		         }  
	    	 if (!oldPath.endsWith(File.separator)) {  
	    		 oldPath = oldPath + File.separator;  
		         }  
	        try {
	            new File(newPath).mkdirs(); //����ļ��в����� �������ļ���
	            File a=new File(oldPath);
	            String[] file=a.list();
	            File temp=null;
	            for (int i = 0; i < file.length; i++) 
	            {
	            	temp=new File(oldPath+file[i]);
	                if(temp.isFile())
	                {  //���Ŀ���ļ����Ѿ������ļ���������
	                	File targetfile=new File(newPath + (temp.getName()).toString());
	                	 if(targetfile.exists())
	     	            {
	                		 if (!cover) {
	                			 continue;
							}
	     	            	targetfile.delete();
	     	            }
	                    FileInputStream input = new FileInputStream(temp);
	                    FileOutputStream output = new FileOutputStream(newPath + (temp.getName()).toString());
	                    byte[] b = new byte[1024 * 5];
	                    int len;
	                    while ((len = input.read(b)) != -1) 
	                    {
	                        output.write(b, 0, len);
	                    }
	                    output.flush();
	                    output.close();
	                    input.close();
	                }
	                if(temp.isDirectory()){//��������ļ���
	                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i],cover);
	                }
	            }
	        }catch (Exception e) {
	            message = "���������ļ������ݲ�������";
	        }
	    }

	    /**
	     * �ļ�����,�������ͬ���ļ����ڣ��򲻸���������
	     * @param oldName ����ȫ��·�����ļ���
	     * @param newName Ҫ�޸ĵ��ļ���,������·��
	     * @return
	     */
	    public static void changeFileName(String oldName, String newName) {
	    	//�ļ�ԭ��ַ
	    	File oldFile = new File(oldName);
	    	//�ļ��£�Ŀ�꣩��ַ
 
	    
	    	File fnew = new File(oldFile.getParentFile() +File.separator +newName);
	    	if (fnew.exists()) //������ļ����ڣ��򲻱�
            {
           	 return;
           	// fnew.delete();
            }
	    	oldFile.renameTo(fnew);
	    	
	     
	    }

	    /**
	     * �ƶ��ļ�������µ�ַ��ͬ���ļ������ƶ�������<br>
	      * ���Դ���һ�����ļ���<br>
	     * ���û���ļ��򷵻�<br>
	     * ע�⣺���ļ��к�Ҫ��\\<br>
	     * @param oldPath �ļ�·��
	     * @param newPath ���ļ����ڵ��ļ���
	     * @return
	     */
	    public static void moveFile(String oldPath, String newPath,boolean cover) {
	    	//�ļ�ԭ��ַ
	    	File oldFile = new File(oldPath);
	    	moveFile(oldPath, newPath, oldFile.getName(),cover);
	    }
	    
	    /**
	     * �ƶ��ļ�������µ�ַ��ͬ���ļ������ƶ�������<br>
	     * ���Դ���һ�����ļ���<br>
	     * ���û���ļ��򷵻�<br>
	     * ע�⣺���ļ��к�Ҫ��\\<br>
	     * @param oldPath �ļ�·��
	     * @param newPath ���ļ����ڵ��ļ���
	     * @param newName ���ļ����ļ���
	     * @param cover �Ƿ񸲸�
	     * @return
	     */
	    public static void moveFile(String oldPath, String newPath, String newName,boolean cover) {
	    	 if (!newPath.endsWith(File.separator)) {  
	    		 newPath = newPath + File.separator;  
		         }  
	    	//�ļ�ԭ��ַ
	    	File oldFile = new File(oldPath);
	    	//�ļ��£�Ŀ�꣩��ַ
	    	//newһ�����ļ���
	    	File fnewpath = new File(newPath);
	    	if (!oldFile.exists()) {
				return;
			}
	    	//�ж��ļ����Ƿ����
	    	if(!fnewpath.exists())
	    	fnewpath.mkdirs();//�������ļ�
	    	//���ļ��Ƶ����ļ���
	    	File fnew = new File(newPath +newName);
	    	if (fnew.exists()) 
            {
	    		if (!cover) {
	    			 return;
				}
	    		fnew.delete();
            }
	    	if (!oldFile.renameTo(fnew)) {
	    		if(copyFile(oldPath, newPath +newName,cover))
	    			oldFile.delete();
	    	}
	    }
	    
	    /**
	    * �ƶ�ָ���ļ����ڵ�ȫ���ļ������Ŀ���ļ������������ļ�����������ͬʱ����false<br/>
	    * ������ļ��в����ڣ��ʹ������ļ��У������ƺ�ֻ�ܴ���һ���ļ��С��ƶ�˳���򷵻�true
	    * @param oldfolderfile Ҫ�ƶ����ļ�Ŀ¼,Ŀ¼������ν�Ӳ���"/"
	    * @param newfolderfile    Ŀ���ļ�Ŀ¼
	    * @param prix ���ļ�ǰ���ϵ�ǰ׺
	    * @param cover �Ƿ񸲸�
	    * @throws Exception
	    */
	    public static boolean moveFoldFile(String oldfolderfile,String newfolderfile,String prix,boolean cover) throws Exception 
	    {
	        //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
	        if (!oldfolderfile.endsWith(File.separator)) {  
	        	oldfolderfile = oldfolderfile + File.separator;  
	         }  
	        if (!newfolderfile.endsWith(File.separator)) {  
	        	newfolderfile = newfolderfile + File.separator;  
	         }  
	    	boolean ok=true;
	      try 
	      {
	        File olddir = new File(oldfolderfile);
	        File[] files = olddir.listFiles(); // �ļ�һ��
	        if (files == null) return false;
	        File newDir = new File(newfolderfile);// Ŀ��
	        if (!newDir.exists()) 
	        {
	        	newDir.mkdirs();
	        }
	       // �ļ��ƶ�
	          for (int i = 0; i < files.length; i++) 
	          {
	        	if (files[i].isDirectory()) //������ļ����ļ��У���ݹ���ñ����������ʵ��÷�����
	        	{
	        		ok=moveFoldFile(files[i].getPath(),newfolderfile + files[i].getName(),prix,cover);
	        				  // �ɹ���ɾ��ԭ�ļ�
	        		if(ok)
	        		{
	        			files[i].delete();
	        		}
	        		continue;
	        	}
	            File fnew = new File(newfolderfile +prix+ files[i].getName());
	       // Ŀ���ļ����´��ڵĻ�������
	             if (fnew.exists()) 
	             {
	            	 if (!cover) {
	            		 ok=false;
		            	 continue;
					}
	            	 fnew.delete();
	             }
	          	if (!files[i].renameTo(fnew)) {
		    		if(copyFile(files[i].getAbsolutePath(), fnew.getAbsolutePath(),cover))
		    			files[i].delete();
		    		else {
						ok = false;
					}
		    	}
	          }
	      } catch (Exception e) {
	    	  throw e;
	      }
		return ok;
	    }
	    

	    /**
	     * �ƶ�Ŀ¼��Ҫ�ƶ����ļ�Ŀ¼,Ŀ¼������ν�Ӳ���"/"
	     * �����moveFoldFile����ò��ûɶ����һ�����������ԭ�ļ���ɾ��
	     * @param oldPath
	     * @param newPath û�лᴴ��һ���ļ��У����Ǻ���ֻ�ܴ���һ���ļ���
	      * @param cover �Ƿ񸲸�
	     * @return
	     */
	    public static void moveFolder(String oldPath, String newPath,boolean cover) 
	    {
	    	try {
	    		moveFoldFile(oldPath, newPath,"",cover);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    public String getMessage(){
	        return this.message;
	    }
	    
	    /**
	     * �ж��ļ��Ƿ���ڣ������Ǿ���·��
	     * @param fileName
	     * @return
	     */
	    public static boolean isFileExist(String fileName)
	    {
	    	if (fileName == null) {
				return false;
			}
	    	 File file=new File(fileName);
	    	 if (!file.exists()) {//û���ļ����򷵻ؿ�
	    		 return false;
	    	 }
	    	else {
				return true;
			} 
	    }
	    
	    /** 
	     * ɾ�������ļ� 
	     * @param   sPath    ��ɾ���ļ����ļ��� 
	     * @return �����ļ�ɾ���ɹ�����true�����򷵻�false 
	     */  
	    private  static boolean deleteFile(String sPath) 
	    {  
	      boolean flag = false;  
	       File file = new File(sPath);  
	        // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��  
	         if (file.isFile() && file.exists()) {  
	             file.delete();  
	             flag = true;  
	         }  
	         return flag;  
	     }  
	    
	    /** 
	     * ɾ��Ŀ¼���ļ��У��Լ�Ŀ¼�µ��ļ� 
	     * @param   sPath ��ɾ��Ŀ¼���ļ�·�����������ν�Ӳ���"/"
	     * @return  Ŀ¼ɾ���ɹ�����true�����򷵻�false 
	     */  
	    private static boolean deleteDirectory(String sPath) {  
	        //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
	        if (!sPath.endsWith(File.separator)) {  
	            sPath = sPath + File.separator;  
	         }  
	         File dirFile = new File(sPath);  
	         //���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
	         if (!dirFile.exists() || !dirFile.isDirectory()) {  
	             return false;  
	         }  
	       boolean flag = true;  
	         //ɾ���ļ����µ������ļ�(������Ŀ¼)  
	         File[] files = dirFile.listFiles();  
	         for (int i = 0; i < files.length; i++) {  
	             //ɾ�����ļ�  
	             if (files[i].isFile()) {  
	                 flag = deleteFile(files[i].getAbsolutePath());  
	                 if (!flag) break;  
	             } //ɾ����Ŀ¼  
	             else {  
	                 flag = deleteDirectory(files[i].getAbsolutePath());  
	                 if (!flag) break;  
	             }  
	         }  
	         if (!flag) return false;  
	         //ɾ����ǰĿ¼  
	         if (dirFile.delete()) {  
	             return true;  
	         } else {  
	             return false;  
	         }  
	     }     
	    /** 
	     *  ����·��ɾ��ָ����Ŀ¼���ļ������۴������ 
	     *@param sPath  Ҫɾ����Ŀ¼���ļ� 
	     *@return ɾ���ɹ����� true�����򷵻� false�� 
	     */  
	    public static boolean DeleteFolder(String sPath) {  
	      boolean flag = false;  
	       File file = new File(sPath);  
	        // �ж�Ŀ¼���ļ��Ƿ����  
	         if (!file.exists()) {  // �����ڷ��� false  
	             return flag;  
	         } else {  
	             // �ж��Ƿ�Ϊ�ļ�  
	             if (file.isFile()) {  // Ϊ�ļ�ʱ����ɾ���ļ�����  
	                 return deleteFile(sPath);  
	             } else {  // ΪĿ¼ʱ����ɾ��Ŀ¼����  
	                return deleteDirectory(sPath);  
	             }  
	         }  
	     }  
}
