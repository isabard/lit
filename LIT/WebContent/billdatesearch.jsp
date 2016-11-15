<%@page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bill Date Search</title>
</head>
<body>
	<div align="center">
	<h1><u>Bill Date Search Results</u></h1>
	
	<!-- Parse Parameters and Show Results -->
	<% 
	// get parameters
	String start = request.getParameter("start");
	String end = request.getParameter("end");
	
		// make sure they are correct dates
		if (start.matches("\\d{2}/\\d{2}/\\d{4}") && end.matches("\\d{2}/\\d{2}/\\d{4}")) {
			out.print("Bills from " + start + " through " + end + ":");
			
			// convert to mysql date format
			start = start.substring(6) + "-" + start.substring(3, 5) + "-" + start.substring(0, 2);
			end = end.substring(6) + "-" + end.substring(3, 5) + "-" + end.substring(0, 2);
			
			// get list of bills from DB
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
			+ "user=root&password=ojmayonnaise");
			PreparedStatement pst = connection.prepareStatement
					("SELECT Id FROM Bills WHERE StartDate BETWEEN '" + start + "' AND '" + end + "'");
			ResultSet rs = pst.executeQuery();
			
			// print list of bills
			while(rs.next()) {
				String billno = Integer.toString(rs.getInt("Id"));
				out.print("<br/><br/>");
				out.print("<form name=\"submitForm\" method=\"post\" action=\"billprofile.jsp\">" +
				    	"<input type=\"hidden\" name=\"billno\" value=\"" + billno + "\">" +
		    	"<A HREF=\"javascript:document.submitForm.submit()\">Bill " + billno + "</A>" +
			"</form>");
			}
		}
		else {
			out.print("Bad input given for a date search.\n"+
					"Use the selector or DD/MM/YYYY.");
		}

	%>
	</div>
</body>
</html>