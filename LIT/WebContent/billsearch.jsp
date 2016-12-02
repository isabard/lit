<%@ page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Legislator Search Page</title>
</head>
<body>
	<div align="center">
	<h1><u>Bill Search Page</u></h1>

	<%
	// get user entered bill search input
	int billnum = Integer.parseInt(request.getParameter("billno"));
	
	// get info from "Bills" table in database.
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	PreparedStatement pst = connection.prepareStatement
			("SELECT Title, Committee, StartDate, LastActiveDate, Status, Link FROM Bills WHERE Id = ?");
	pst.setInt(1, billnum);
	ResultSet rs = pst.executeQuery();
	
	// Assign local variables to values from "Bills" table in DBb
	rs.next();
	
	String Title = rs.getString("Title");
	String Committee = rs.getString("Committee");
	String StartDate = rs.getString("StartDate");
	String LastActDate = rs.getString("LastActiveDate");
	String Status = rs.getString("String");
	String Link = rs.getString("Link");

	// Output the info to screen
	out.print("Title: " + Title);
	out.print("Committee: " + Committee);
	out.print("Start Date: " + StartDate);
	out.print("Last Active Date: " + LastActDate);
	out.print("Status: " + Status);
	out.print("<form name=\"submitForm\" method=\"post\" action=\"billprofile.jsp\">" +
	    	"<input type=\"hidden\" name=\"billnum\" value=\"" + billnum + "\">" +
	"<A HREF=\"javascript:document.submitForm.submit()\">Bill " + billnum + "</A>" +
	"</form>");
	
	%>
	</div>
</body>
</html>
