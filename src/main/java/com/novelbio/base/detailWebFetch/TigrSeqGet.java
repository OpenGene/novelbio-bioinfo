package com.novelbio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;

import com.novelbio.base.dataOperate.WebFetch;


/**
 * 给定网址，获得Tigr序列
 * @author Administrator
 *
 */
public class TigrSeqGet 
{
	/**
	 * 获取Tigr序列
	 */
	WebFetch TIGRSeq;
	
	
	
	BufferedReader SeqReader;
	
	
	public TigrSeqGet()
	{
	
	}

	
	/**
	 * 给定网址，获得序列
	 * @param seqUrl
	 * @return
	 * @throws IOException 
	 */
	public String getSeq(String seqUrl) throws IOException
	{
		TIGRSeq=new WebFetch();
		TIGRSeq.GetUrl(seqUrl);
		SeqReader = TIGRSeq.GetFetch();
	    String seq="";
	    String content="";
	  
        while ((content=SeqReader.readLine())!= null)//读取该网页
        {
            if (content.contains("Protein<br>"))
            {
                for (int i = 0; i < 2; i++)
                {
                	SeqReader.readLine();
                }
                seq= "";
                while ((content=SeqReader.readLine())!= null)
                {
                	content.trim();
                    if (!content.contains("</pre>"))
                    {
                    	seq= seq +  content;
                    }
                    else
                    {
                       return seq;
                    }
                }
            }
        }

		return null;
		
	}
	
	public void close()
	{
		TIGRSeq.closeall();
	}
	
	
	
}
