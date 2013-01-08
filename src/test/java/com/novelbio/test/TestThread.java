package com.novelbio.test;

 public class TestThread extends Thread    
 {    
     public static int n = 0;    
    
     static synchronized void inc()    
     {    
           
         n++;    
         System.out.println("当前活动线程和N分别为:"+Thread.currentThread().getName()+"_"+n);  
     }    
     public void run()    
     {    
         for (int i = 0; i < 10; i++)    
             try   
             {    
                 inc();   
                 sleep(3);  // 为了使运行结果更随机，延迟3毫秒    
                     
             }    
             catch (Exception e)    
             {    
             }                                          
     }    
     public static void main(String[] args) throws Exception    
     {    
        
         Thread threads[] = new Thread[100];    
         for (int i = 0; i < threads.length; i++)  // 建立100个线程    
             threads[i] = new TestThread();    
         for (int i = 0; i < threads.length; i++){// 运行刚才建立的100个线程    
             threads[i].start();  
             System.out.println("线程"+i+"start了");  
             threads[i].join();//关键点  
         }  
               
         System.out.println("n=" + TestThread.n);    
         }  
       
 }    
