package com.novelbio.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class TestJung {
	
	public static void main(String [] args) throws Exception
	{
	    String amazon = "http://www.amazon.com";
	    String yahoo = "http://www.yahoo.com";
	    String ebay = "http://www.ebay.com";
	    String novelbio = "http://www.novelbio.com";
		Graph<String, Integer> g = new DirectedSparseGraph<String, Integer>();
        g.addVertex(amazon);
        g.addVertex(yahoo);
        g.addVertex(ebay);
        g.addVertex(novelbio);
		g.addEdge(10,yahoo, amazon);
        g.addEdge(12,yahoo, ebay);
        g.addEdge(5,ebay, novelbio);
        g.addEdge(8,amazon, novelbio);
		Collection<Integer> ls = g.findEdgeSet(yahoo, amazon);
		System.out.println("aaa");
		
		
	}
	
	
	
	
}
