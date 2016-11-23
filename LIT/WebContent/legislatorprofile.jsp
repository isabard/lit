<%@page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Legislator Profile</title>
</head>
<body>
	<% 
	// get parameter
	int leg = Integer.parseInt(request.getParameter("legislator"));
	
	//connect to db
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
			+ "user=root&password=ojmayonnaise");
	
	// get legislator's info
	PreparedStatement pst = connection.prepareStatement("SELECT Name, PassedBills, FailedBills FROM Legislators WHERE Id = ?");
	pst.setInt(1, leg);
	ResultSet rs = pst.executeQuery();
	while (rs.next()) {
		out.println(rs.getString("Name") + "<br/>");
		int passed = rs.getInt("PassedBills");
		int failed = rs.getInt("FailedBills");
		int avg = 100 * (passed / (passed + failed));
		out.println("Success rate: " + avg + "%<br/>");
	}
	// get bills by legislator
	pst = connection.prepareStatement("Select Bill FROM Sponsors WHERE Sponsor = ?");
	pst.setInt(1, leg);
	rs = pst.executeQuery();
	out.println("Bills sponsored:");
	while (rs.next()) {
		String billno = Integer.toString(rs.getInt("Bill"));
		out.print("<br/>");
		out.print("<form action=\"billprofile.jsp\" method=\"post\">" +
				  "<button type=\"submit\" name=\"billno\" value=\"" + billno +
				  "\" class=\"btn-link\">Bill " + billno +"</button>" +
				  "</form>");
	}
	%>
</body>
</html>