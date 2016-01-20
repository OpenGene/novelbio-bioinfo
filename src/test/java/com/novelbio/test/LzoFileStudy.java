package com.novelbio.test;

//import com.hadoop.compression.lzo.LzopCodec;   
/**  
 * 读写Lzo文件  
 */  
public class LzoFileStudy {
//     
//  private static Configuration getDefaultConf(){
//	  HdfsBaseHolderHadoop2 hdfsBase = new HdfsBaseHolderHadoop2();
//	  return hdfsBase.getConf();
//  }   
//     
//  /**  
//   * 写入数据到lzo文件  
//   *   
//   * @param destLzoFilePath  
//   * @param conf  
//   * @param datas  
//   */  
//  public static void write2LzoFile(String destLzoFilePath,Configuration conf,byte[] datas){   
//    LzopCodec lzo=null;   
//    OutputStream out=null;   
//       
//    try {   
//      lzo=new LzopCodec();   
//      lzo.setConf(conf);   
//      out=lzo.createOutputStream(new FileOutputStream(destLzoFilePath));   
//      out.write(datas);   
//    } catch (FileNotFoundException e) {   
//      // TODO Auto-generated catch block   
//      e.printStackTrace();   
//    } catch (IOException e) {   
//      // TODO Auto-generated catch block   
//      e.printStackTrace();   
//    }finally{   
//      try {   
//        if(out!=null){   
//          out.close();   
//        }   
//      } catch (IOException e) {   
//        // TODO Auto-generated catch block   
//        e.printStackTrace();   
//      }   
//    }   
//       
//  }   
//     
//  /**  
//   * 从lzo文件中读取数据  
//   *   
//   * @param lzoFilePath  
//   * @param conf  
//   * @return  
//   */  
//  public static List<String> readLzoFile(String lzoFilePath,Configuration conf){   
//    LzopCodec lzo=null;   
//    InputStream is=null;   
//    InputStreamReader isr=null;   
//    BufferedReader reader=null;   
//    List<String> result=null;   
//    String line=null;   
//       
//    try {   
//      lzo=new LzopCodec();   
//      lzo.setConf(conf);   
//      is=lzo.createInputStream(new FileInputStream(lzoFilePath));   
//      isr=new InputStreamReader(is);   
//      reader=new BufferedReader(isr);   
//      result=new ArrayList<String>();   
//      while((line=reader.readLine())!=null){   
//        result.add(line);   
//      }   
//         
//    } catch (FileNotFoundException e) {   
//      // TODO Auto-generated catch block   
//      e.printStackTrace();   
//    } catch (IOException e) {   
//      // TODO Auto-generated catch block   
//      e.printStackTrace();   
//    }finally{   
//      try {   
//        if(reader!=null){   
//          reader.close();   
//        }   
//        if(isr!=null){   
//          isr.close();   
//        }   
//        if(is!=null){   
//          is.close();   
//        }   
//      } catch (IOException e) {   
//        // TODO Auto-generated catch block   
//        e.printStackTrace();   
//      }   
//    }   
//    return result;   
//  }   
//     
//  /**  
//   * @param args  
//   */  
//  public static void main(String[] args) {   
//    // 生成数据   
//    String dataSource="abcdefghijklmnopqrstuvwxyz0123456789～！＠＃￥％……＆＊（）——＋\r";   
//    dataSource=dataSource.concat(dataSource);   
//    dataSource=dataSource.concat(dataSource);   
//    dataSource=dataSource.concat(dataSource);   
//       
//    String lzoFilePath="./data/test.lzo";   
//    // 写入到lzo文件   
//    write2LzoFile(lzoFilePath,getDefaultConf(),dataSource.getBytes());   
//    StringBuilder sb=new StringBuilder();   
//    // 读取lzo文件   
//    List<String> lines=readLzoFile(lzoFilePath,getDefaultConf());   
//    for(String line:lines){   
//      sb.append(line);    
//      sb.append("\r");   
//    }   
//    // 数据是否一致   
//    if(sb.toString().equals(dataSource)){   
//      System.out.println(sb.toString());   
//    }else{   
//      System.err.println("Error line:"+sb.toString());   
//    }   
//       
//  }   
//  
}  