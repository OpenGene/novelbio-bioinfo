package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * java����ϵͳ����
 * @author zong0jie
 *
 */
public class cmdOperate2 {
	  /** 
     * ��������ִ�н����Ϣ�� 
     *  
     * @param command 
     *            Ҫִ�е����� 
     * @return ��һ��Ϊ��׼��Ϣ���ڶ���Ϊ������Ϣ 
     * @throws Throwable 
     *             String[] 
     */  
    public static String[] exec(String command) throws Throwable {
        Process process = null;  
        Runtime runtime = Runtime.getRuntime();  
  
        // Linux,Unix  
        process = runtime.exec(command);  
  
        // �洢���ؽ������һ��Ϊ��׼��Ϣ���ڶ���Ϊ������Ϣ  
        String result[] = new String[2];  
        ReadThread inputReadThread = new ReadThread(process.getInputStream());  
        ReadThread errReadThread = new ReadThread(process.getErrorStream());  
        inputReadThread.start();  
        errReadThread.start();  
  
        //ȷ����׼�������������ʱ������緵��ִ�н��  
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
     * ��׼�����������ȡ�߳� 
     */  
    private static class ReadThread extends Thread {
        private InputStream is;  
  
        private ArrayList result = new ArrayList();  
  
        public boolean flag;// ���Ƿ��ȡ���  
  
        public ReadThread(InputStream is) {  
            this.is = is;  
        }  
  
        /**
         * ��ȡ����ִ�к������Ϣ�����û���򷵻ؿ�""�ַ���  
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
  
                flag = true;// ���Ѷ���  
            } catch (Exception e) {  
                e.printStackTrace();  
            }
        }
    }
  
}

