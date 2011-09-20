package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

class StreamGobbler extends Thread   
{
    InputStream is;   
    String type;   
      
    StreamGobbler(InputStream is, String type)   
    {   
        this.is = is;   
        this.type = type;   
    }   
      
    public void run()   
    {   
        try  
        {   
            InputStreamReader isr = new InputStreamReader(is);   
            BufferedReader br = new BufferedReader(isr);   
            String line=null;   
            while ( (line = br.readLine()) != null)   
                System.out.println(type + ">" + line);      
            } catch (IOException ioe)   
              {   
                ioe.printStackTrace();     
              }   
    }   
}

public class CMDcallback
{
    public static void setCMD(String command)
    {
    	File aaFile = new File(pathname);
    	try {
    		Runtime rt = Runtime.getRuntime();
        	System.out.println(command);
        	Process proc = rt.exec(command);
                // any error message?
                StreamGobbler errorGobbler = new
                    StreamGobbler(proc.getErrorStream(), "ERROR");            
                
                // any output?
                StreamGobbler outputGobbler = new
                    StreamGobbler(proc.getInputStream(), "OUTPUT");
                   
                // kick them off
                errorGobbler.start();
                outputGobbler.start();
                // any error???
                int exitVal;
    				exitVal = proc.waitFor();
    				  System.out.println("ExitValue: " + exitVal);       
    	} catch (Exception e) {
			// TODO: handle exception
		}
    } 
}


