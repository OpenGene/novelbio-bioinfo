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
	     * 读取文本文件内容
	     * @param filePathAndName 带有完整绝对路径的文件名
	     * @param encoding 文本文件打开的编码方式
	     * @return 返回文本文件的内容
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
	     * 给定路径名，返回其上一层路径，不带"/"
	     * 可以给定不存在的路径
	     * @param fileName
	     * @return
	     */
	    public static String getParentName(String fileName) {
			File file = new File(fileName);
			return file.getParent();
		}
	    /**
	     * 给定路径名，返回其名字
	     * 如给定/home/zong0jie/和/home/zong0jie
	     * 都返回zong0jie
	     * 可以给定不存在的路径
	     * @param fileName
	     * @return
	     */
	    public static String getName(String fileName) {
			File file = new File(fileName);
			return file.getName();
		}
	    /**
	     * 获取文件夹下所有文件名与后缀,不包含路径
	     * 	     * 如果文件不存在则返回null<br>
	     * @param filePath 目录路径,最后不要加\\或/
	     * @return arraylist 里面是string[2] 1:文件名     2：后缀
	     */
	    public static ArrayList<String[]> getFoldFileName(String filePath)
	    {
	    	return getFoldFileName(filePath,"*","*");
	    }
	    
	    /**
	     * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	     * 如果文件不存在则返回null<br>
	     * 如果不是文件夹，则返回该文件名<br>
	     * @param filePath 目录路径,最后不要加\\或/
	     * @param filename 指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小写
	     * @param suffix 指定包含的后缀名，是正则表达式<br>
	     *  文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	     *  文件 wfese.fse.认作	"wfese.fse."和""<br>
	     *  文件 wfese 认作 "wfese"和""<br>
	     * @return 返回包含目标文件名的ArrayList。里面是string[2] 1:文件名     2：后缀
	     */
	    public static ArrayList<String[]> getFoldFileName(String filePath,String filename,String suffix)
	    {
	    	//匹配文件名与后缀名
	    	 Pattern pattern =Pattern.compile("(.*)\\.(\\w*)", Pattern.CASE_INSENSITIVE);
	    	 Matcher matcher;
	    	 String name=""; //文件名
	    	 String houzhuiming=""; //后缀名
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
	    	 if (!file.exists()) {//没有文件，则返回空
	    		 return null;
	    	 }
	    	 //如果只是文件则返回文件名
	    	 if(!file.isDirectory())
	    	 {      //获取文件名与后缀名
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
	    	 //如果是文件夹
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
   		      //开始判断
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
	     * 新建目录,如果新文件夹存在则返回
	     * @param folderPath 目录路径,最后不要加\\或/
	     * @return 返回目录创建后的路径
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
	            message = "创建目录操作出错";
	        }
	        return txt;
	    }
	    
	    /**
	     * 多级目录创建
	     * @param folderPath 准备要在本级目录下创建新目录的目录路径 例如 c:myf
	     * @param paths 无限级目录参数，各级目录以单数线区分 例如 a|b|c
	     * @return 返回创建文件后的路径 例如 c:/myf/a/b/c
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
	           message = "创建目录操作出错！";
	       }
	        return txts;
	    }

	    
	    /**
	     * 新建文件
	     * @param filePathAndName 文本文件完整绝对路径及文件名
	     * @param fileContent 文本文件内容
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
	            message = "创建文件操作出错";
	        }
	    }


	    /**
	     * 有编码方式的文件创建
	     * @param filePathAndName 文本文件完整绝对路径及文件名
	     * @param fileContent 文本文件内容
	     * @param encoding 编码方式 例如 GBK 或者 UTF-8
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
	            message = "创建文件操作出错";
	        }
	    } 


	    /**
	     * 删除文件
	     * @param filePathAndName 文本文件完整绝对路径及文件名
	     * 文件不存在则返回false
	     * @return Boolean 成功删除返回true遭遇异常返回false
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
	             //message = (filePathAndName+"删除文件操作出错");
	            }
	        }
	        catch (Exception e) {
	            message = e.toString();
	        }
	        return bea;
	    }
	    


	    /**
	     * 删除文件夹
	     * @param folderPath 文件夹完整绝对路径
	     * @return
	     */
	    public static void delFolder(String folderPath) {
	        try {
	            delAllFile(folderPath); //删除完里面所有内容
	            String filePath = folderPath;
	            filePath = filePath.toString();
	            java.io.File myFilePath = new java.io.File(filePath);
	            myFilePath.delete(); //删除空文件夹
	        }
	        catch (Exception e) {
	            message = ("删除文件夹操作出错");
	        }
	    }
	    
	    
	    /**
	     * 删除指定文件夹下所有文件,
	     * @param path 文件夹完整绝对路径,最后无所谓加不加\\或/
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
	                delAllFile(path + tempList[i]);//先删除文件夹里面的文件
	                delFolder(path + tempList[i]);//再删除空文件夹
	                bea = true;
	            }
	        }
	        return bea;
	    }


	    /**
	     * 复制单个文件
	     * @param oldPathFile 准备复制的文件源
	     * @param newPathFile 拷贝到新绝对路径带文件名
	     * @param cover 是否覆盖
	     * @return
	     */
	    public static boolean copyFile(String oldPathFile, String newPathFile,boolean cover) {
	        try {
	            int bytesum = 0;
	            int byteread = 0;
	            File oldfile = new File(oldPathFile);
	            File newfile= new File(newPathFile);

	            if (oldfile.exists()) 
	            { //文件存在时
		            if(newfile.exists())
		            {
		            	if (!cover) {
							return false;
						}
		            	newfile.delete();
		            }
	                InputStream inStream = new FileInputStream(oldPathFile); //读入原文件
	                FileOutputStream fs = new FileOutputStream(newPathFile);
	                byte[] buffer = new byte[1444];
	                while((byteread = inStream.read(buffer)) != -1){
	                    bytesum += byteread; //字节数 文件大小
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
	            message = ("复制单个文件操作出错");
	            return false;
	        }
	    }
	    

	    /**
	     * 复制整个文件夹的内容,如果要文件已经存在，则跳过
	     * @param oldPath 准备拷贝的目录，最后都无所谓加不加"/"
	     * @param newPath 指定绝对路径的新目录
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
	            new File(newPath).mkdirs(); //如果文件夹不存在 则建立新文件夹
	            File a=new File(oldPath);
	            String[] file=a.list();
	            File temp=null;
	            for (int i = 0; i < file.length; i++) 
	            {
	            	temp=new File(oldPath+file[i]);
	                if(temp.isFile())
	                {  //如果目标文件夹已经存在文件，则跳过
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
	                if(temp.isDirectory()){//如果是子文件夹
	                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i],cover);
	                }
	            }
	        }catch (Exception e) {
	            message = "复制整个文件夹内容操作出错";
	        }
	    }

	    /**
	     * 文件改名,如果已有同名文件存在，则不改名并返回
	     * @param oldName 包含全部路径的文件名
	     * @param newName 要修改的文件名,不包含路径
	     * @return
	     */
	    public static void changeFileName(String oldName, String newName) {
	    	//文件原地址
	    	File oldFile = new File(oldName);
	    	//文件新（目标）地址
 
	    
	    	File fnew = new File(oldFile.getParentFile() +File.separator +newName);
	    	if (fnew.exists()) //如果有文件存在，则不变
            {
           	 return;
           	// fnew.delete();
            }
	    	oldFile.renameTo(fnew);
	    	
	     
	    }

	    /**
	     * 移动文件，如果新地址有同名文件，则不移动并返回<br>
	      * 可以创建一级新文件夹<br>
	     * 如果没有文件则返回<br>
	     * 注意：新文件夹后不要加\\<br>
	     * @param oldPath 文件路径
	     * @param newPath 新文件所在的文件夹
	     * @return
	     */
	    public static void moveFile(String oldPath, String newPath,boolean cover) {
	    	//文件原地址
	    	File oldFile = new File(oldPath);
	    	moveFile(oldPath, newPath, oldFile.getName(),cover);
	    }
	    
	    /**
	     * 移动文件，如果新地址有同名文件，则不移动并返回<br>
	     * 可以创建一级新文件夹<br>
	     * 如果没有文件则返回<br>
	     * 注意：新文件夹后不要加\\<br>
	     * @param oldPath 文件路径
	     * @param newPath 新文件所在的文件夹
	     * @param newName 新文件的文件名
	     * @param cover 是否覆盖
	     * @return
	     */
	    public static void moveFile(String oldPath, String newPath, String newName,boolean cover) {
	    	 if (!newPath.endsWith(File.separator)) {  
	    		 newPath = newPath + File.separator;  
		         }  
	    	//文件原地址
	    	File oldFile = new File(oldPath);
	    	//文件新（目标）地址
	    	//new一个新文件夹
	    	File fnewpath = new File(newPath);
	    	if (!oldFile.exists()) {
				return;
			}
	    	//判断文件夹是否存在
	    	if(!fnewpath.exists())
	    	fnewpath.mkdirs();//创建新文件
	    	//将文件移到新文件里
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
	    * 移动指定文件夹内的全部文件，如果目标文件夹下有重名文件，则跳过，同时返回false<br/>
	    * 如果新文件夹不存在，就创建新文件夹，不过似乎只能创建一级文件夹。移动顺利则返回true
	    * @param oldfolderfile 要移动的文件目录,目录都无所谓加不加"/"
	    * @param newfolderfile    目标文件目录
	    * @param prix 在文件前加上的前缀
	    * @param cover 是否覆盖
	    * @throws Exception
	    */
	    public static boolean moveFoldFile(String oldfolderfile,String newfolderfile,String prix,boolean cover) throws Exception 
	    {
	        //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
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
	        File[] files = olddir.listFiles(); // 文件一览
	        if (files == null) return false;
	        File newDir = new File(newfolderfile);// 目标
	        if (!newDir.exists()) 
	        {
	        	newDir.mkdirs();
	        }
	       // 文件移动
	          for (int i = 0; i < files.length; i++) 
	          {
	        	if (files[i].isDirectory()) //如果子文件是文件夹，则递归调用本函数，精彩的用法！！
	        	{
	        		ok=moveFoldFile(files[i].getPath(),newfolderfile + files[i].getName(),prix,cover);
	        				  // 成功，删除原文件
	        		if(ok)
	        		{
	        			files[i].delete();
	        		}
	        		continue;
	        	}
	            File fnew = new File(newfolderfile +prix+ files[i].getName());
	       // 目标文件夹下存在的话，不变
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
	     * 移动目录，要移动的文件目录,目录都无所谓加不加"/"
	     * 这个和moveFoldFile方法貌似没啥区别一样，不过会把原文件夹删除
	     * @param oldPath
	     * @param newPath 没有会创建一个文件夹，但是好像只能创建一级文件夹
	      * @param cover 是否覆盖
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
	     * 判断文件是否存在，给的是绝对路径
	     * @param fileName
	     * @return
	     */
	    public static boolean isFileExist(String fileName)
	    {
	    	if (fileName == null) {
				return false;
			}
	    	 File file=new File(fileName);
	    	 if (!file.exists()) {//没有文件，则返回空
	    		 return false;
	    	 }
	    	else {
				return true;
			} 
	    }
	    
	    /** 
	     * 删除单个文件 
	     * @param   sPath    被删除文件的文件名 
	     * @return 单个文件删除成功返回true，否则返回false 
	     */  
	    private  static boolean deleteFile(String sPath) 
	    {  
	      boolean flag = false;  
	       File file = new File(sPath);  
	        // 路径为文件且不为空则进行删除  
	         if (file.isFile() && file.exists()) {  
	             file.delete();  
	             flag = true;  
	         }  
	         return flag;  
	     }  
	    
	    /** 
	     * 删除目录（文件夹）以及目录下的文件 
	     * @param   sPath 被删除目录的文件路径，最后无所谓加不加"/"
	     * @return  目录删除成功返回true，否则返回false 
	     */  
	    private static boolean deleteDirectory(String sPath) {  
	        //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	        if (!sPath.endsWith(File.separator)) {  
	            sPath = sPath + File.separator;  
	         }  
	         File dirFile = new File(sPath);  
	         //如果dir对应的文件不存在，或者不是一个目录，则退出  
	         if (!dirFile.exists() || !dirFile.isDirectory()) {  
	             return false;  
	         }  
	       boolean flag = true;  
	         //删除文件夹下的所有文件(包括子目录)  
	         File[] files = dirFile.listFiles();  
	         for (int i = 0; i < files.length; i++) {  
	             //删除子文件  
	             if (files[i].isFile()) {  
	                 flag = deleteFile(files[i].getAbsolutePath());  
	                 if (!flag) break;  
	             } //删除子目录  
	             else {  
	                 flag = deleteDirectory(files[i].getAbsolutePath());  
	                 if (!flag) break;  
	             }  
	         }  
	         if (!flag) return false;  
	         //删除当前目录  
	         if (dirFile.delete()) {  
	             return true;  
	         } else {  
	             return false;  
	         }  
	     }     
	    /** 
	     *  根据路径删除指定的目录或文件，无论存在与否 
	     *@param sPath  要删除的目录或文件 
	     *@return 删除成功返回 true，否则返回 false。 
	     */  
	    public static boolean DeleteFolder(String sPath) {  
	      boolean flag = false;  
	       File file = new File(sPath);  
	        // 判断目录或文件是否存在  
	         if (!file.exists()) {  // 不存在返回 false  
	             return flag;  
	         } else {  
	             // 判断是否为文件  
	             if (file.isFile()) {  // 为文件时调用删除文件方法  
	                 return deleteFile(sPath);  
	             } else {  // 为目录时调用删除目录方法  
	                return deleteDirectory(sPath);  
	             }  
	         }  
	     }  
}
