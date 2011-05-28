package com.novelbio.web;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

public class HelloWorld extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter aa = new AnnotationMethodHandlerAdapter();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>");
		out.println("This is my first Servlet");
		out.println("</title></head><body>");
		out.println("<h1>Hello,Worldaaa</h1>");
		out.println("</body></html>");
		System.out.println("fes");
	}

}