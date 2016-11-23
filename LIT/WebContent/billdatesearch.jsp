<%@page import = "java.sql.Date" %>
<%@page import = "java.text.SimpleDateFormat" %>
<%@page import = "java.sql.*" %>
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
			
			// get list of bills from DB
			SimpleDateFormat sqlDate = new SimpleDateFormat("dd/MM/yyyy");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
			+ "user=root&password=ojmayonnaise");
			PreparedStatement pst = connection.prepareStatement("SELECT Id FROM Bills WHERE StartDate BETWEEN ? AND ?");
			pst.setDate(1, new Date(sqlDate.parse(start).getTime()));
			pst.setDate(2, new Date(sqlDate.parse(end).getTime()));
			ResultSet rs = pst.executeQuery();
			
			// print list of bills
			while(rs.next()) {
				String billno = Integer.toString(rs.getInt("Id"));
				out.print("<br/>");
				out.print("<form action=\"billprofile.jsp\" method=\"post\">" +
				  "<button type=\"submit\" name=\"billno\" value=\"" + billno +
				  "\" class=\"btn-link\">Bill " + billno +"</button>" +
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
<style>
.btn-link{
  border:none;
  outline:none;
  background:none;
  cursor:pointer;
  color:#0000EE;
  padding:0;
  text-decoration:underline;
  font-family:inherit;
  font-size:inherit;
}
</style>
</html>