package com.novelbio.test;

 public class TestThread extends Thread    
 {    
     public static int n = 0;    
    
     static synchronized void inc()    
     {    
           
         n++;    
         System.out.println("��ǰ��̺߳�N�ֱ�Ϊ:"+Thread.currentThread().getName()+"_"+n);  
     }    
     public void run()    
     {    
         for (int i = 0; i < 10; i++)    
             try   
             {    
                 inc();   
                 sleep(3);  // Ϊ��ʹ���н����������ӳ�3����    
                     
             }    
             catch (Exception e)    
             {    
             }                                          
     }    
     public static void main(String[] args) throws Exception    
     {    
        
         Thread threads[] = new Thread[100];    
         for (int i = 0; i < threads.length; i++)  // ����100���߳�    
             threads[i] = new TestThread();    
         for (int i = 0; i < threads.length; i++){// ���иղŽ�����100���߳�    
             threads[i].start();  
             System.out.println("�߳�"+i+"start��");  
             threads[i].join();//�ؼ���  
         }  
               
         System.out.println("n=" + TestThread.n);    
         }  
       
 }    