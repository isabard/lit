<%@page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bill Profile</title>
</head>
<body>
	<h1><u>Bill Profile Page</u></h1>
	<%
	// Make sure there is a billno to acquire
	if (request.getParameter("billno") == null)
		out.print("No bill number was passed!");
	else {
		// if so, get it
		int billno = Integer.parseInt(request.getParameter("billno"));
		
		//connect to db
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
					+ "user=root&password=ojmayonnaise");
		
		// get bill attributes
		PreparedStatement pst = connection.prepareStatement("SELECT Title, Committee, StartDate, LastActiveDate, Status FROM Bills WHERE Id = ?");
		pst.setInt(1, billno);
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
			out.print("<b>Bill number:</b> " + billno + "<br>" +
					  "<b>Title:</b> " + rs.getString("Title") + "<br/>" +
					  "<b>Committee:</b> " + rs.getString("Committee") + "<br/>" +
					  "<b>Start date:</b> " + rs.getDate("StartDate") + "<br/>" +
					  "<b>Last active date:</b> " + rs.getDate("LastActiveDate") + "<br/>" +
					  "<b>Bill's current status:</b> " + rs.getString("Status") + "<br/>");
		}
		
		// get categories
		pst = connection.prepareStatement("SELECT c.Name FROM HasCategory AS h INNER JOIN Categories AS c ON h.Category = c.Id WHERE h.Bill = ?");
		pst.setInt(1, billno);
		rs = pst.executeQuery();
		out.print("<b>Categories:</b> ");
		boolean first = true;
		while(rs.next())
		{
			if (!first) {
				out.print(", ");
			}
			else {
				first = false;
			}
			out.print(rs.getString("Name"));
		}
		
		out.print("<br/>");
		
		// get word cloud
		pst = connection.prepareStatement("SELECT Word FROM BillWordClouds WHERE Bill = ?");
		pst.setInt(1, billno);
		rs = pst.executeQuery();
		first = true;
		out.print("<b>Word cloud:</b> ");
		while(rs.next())
		{
			if (!first) {
				out.print(", ");
			}
			else {
				first = false;
			}
			out.print(rs.getString("Word"));
		}
	// close connection
	connection.close();
	}
	%>
</body>
</html>