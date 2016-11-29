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
	<%
	// get parameter
	int billno = Integer.parseInt(request.getParameter("billno"));
	if (request.getParameter("billno") == null)
		out.print("No param");
	else {
	//connect to db
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
				+ "user=root&password=ojmayonnaise");
	
	// get bill
	PreparedStatement pst = connection.prepareStatement("SELECT * FROM Bills WHERE Id = ?");
	pst.setInt(1, billno);
	ResultSet rs = pst.executeQuery();
	while (rs.next()) {
		out.print("Title: " + rs.getString("Title") + "<br/>" +
				  "Committee: " + rs.getString("Committee") + "<br/>" +
				  "Start date: " + rs.getDate("StartDate") + "<br/>" +
				  "Last active date: " + rs.getDate("LastActiveDate") + "<br/>" +
				  "Bill's current status: " + rs.getString("Status") + "<br/>" +
				  "Link to bill text: " + rs.getString("Link") + "<br/>");
	}
	
	// get categories
	pst = connection.prepareStatement("SELECT c.Name FROM HasCategory AS h INNER JOIN Categories AS c ON h.Category = c.Id WHERE h.Bill = ?");
	pst.setInt(1, billno);
	rs = pst.executeQuery();
	out.print("Categories: ");
	while (rs.next()) {
		out.print(rs.getString("Name") + " ");
	}
	
	out.print("<br/><br/>");
	
	// get word cloud
	pst = connection.prepareStatement("SELECT Word FROM BillWordClouds WHERE Bill = ?");
	pst.setInt(1, billno);
	rs = pst.executeQuery();
	out.print("Word cloud: ");
	while (rs.next()) {
		out.print(rs.getString("Word") + " ");
	}
	connection.close();
	}
	%>
</body>
</html>