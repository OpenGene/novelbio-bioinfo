package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * java调用系统命令
 * @author zong0jie
 *
 */
public class cmdOperate2 {
	  /** 
     * 返回命令执行结果信息串 
     *  
     * @param command 
     *            要执行的命令 
     * @return 第一个为标准信息，第二个为错误信息 
     * @throws Throwable 
     *             String[] 
     */  
    public static String[] exec(String command) throws Throwable {
        Process process = null;  
        Runtime runtime = Runtime.getRuntime();  
  
        // Linux,Unix  
        process = runtime.exec(command);  
  
        // 存储返回结果，第一个为标准信息，第二个为错误信息  
        String result[] = new String[2];  
        ReadThread inputReadThread = new ReadThread(process.getInputStream());  
        ReadThread errReadThread = new ReadThread(process.getErrorStream());  
        inputReadThread.start();  
        errReadThread.start();  
  
        //确保标准与错误流都读完时才向外界返回执行结果  
        while (true) {  
            if (inputReadThread.flag && errReadThread.flag) {  
                break;  
            } else {  
                Thread.sleep(1000);  
            }  
        }  
        result[0] = inputReadThread.getResult();  
        result[1] = errReadThread.getResult();  
        return result;  
    }  
  
    public static void main(String args[]) throws Throwable {
        if (args.length == 0) {  
            System.out.println("Useage: \r\n    java JavaExcCommand <command>");  
            return;  
        }
        String[] result = exec(args[0]);  
        System.out.println("error info:---------------\r\n" + result[1]);  
        System.out.println("std info:-----------------\r\n" + result[0]);  
    }
  
    /* 
     * 标准流与错误流读取线程 
     */  
    private static class ReadThread extends Thread {
        private InputStream is;  
  
        private ArrayList result = new ArrayList();  
  
        public boolean flag;// 流是否读取完毕  
  
        public ReadThread(InputStream is) {  
            this.is = is;  
        }  
  
        /**
         * 获取命令执行后输出信息，如果没有则返回空""字符串  
         * @return
         */
        protected String getResult() {
            byte[] byteArr = new byte[result.size()];  
            for (int i = 0; i < result.size(); i++) {  
                byteArr[i] = ((Byte) result.get(i)).byteValue();  
            }
            return new String(byteArr);  
        }
  
        public void run() {  
            try {
                int readInt = is.read();  
                while (readInt != -1) {  
                    result.add(Byte.valueOf(String.valueOf((byte) readInt)));  
                    readInt = is.read();  
                }
  
                flag = true;// 流已读完  
            } catch (Exception e) {  
                e.printStackTrace();  
            }
        }
    }
  
}

