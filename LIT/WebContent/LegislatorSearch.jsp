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
	<h1><u>Legislator Name Search Page</u></h1>

	<%
	// get user selected Legislator Name from Drop Down List
	int leg = Integer.parseInt(request.getParameter("legislator"));
	
	// get info from "Legislators" table in database.
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.2:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	PreparedStatement pst = connection.prepareStatement
			("SELECT Name, PassedBills, FailedBills FROM Legislators WHERE Id = ?");
	pst.setInt(1, leg);
	ResultSet rs = pst.executeQuery();
	
	// Assign local variables to values from "Legislators" table in DB
	rs.next();
	
	String DbLegName = rs.getString("Name");
	String[] ConcenCatNameArray = new String[3];
	int[] concenCatArray = new int[3];
	
	int passedBills = rs.getInt("PassedBills");
	int failedBills = rs.getInt("FailedBills");
	double passFailRate = passedBills / failedBills;
	
	// get info from "Concentrations" table in DB
	PreparedStatement pst1 = connection.prepareStatement
			("SELECT Name FROM Concentrations WHERE Id = ?");
	pst1.setInt(1, leg);
	ResultSet rs1 = pst.executeQuery();
	
	// Set local array to legislators top three areas of 
	// concentration
	int i = 0;
	while(rs1.next())
	{
		concenCatArray[i] = rs.getInt("Name");
		
		// take category code from "Concentrations" table and 
		// use the "Categories" table to decode code while
		// setting return strings in array
		PreparedStatement pst2 = connection.prepareStatement
				("SELECT Category FROM Categories WHERE Id = ?");
		pst2.setInt(1, concenCatArray[i]);
		ResultSet rs2 = pst.executeQuery();
		
		ConcenCatNameArray[i] = rs.getString("Category");
		
		i++;
	}
	
	
	out.print("<br/><br/>");

	%>
	</div>
</body>
</html>