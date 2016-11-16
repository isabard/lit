<%@ page import= "java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Admin Login</title>
</head>
<body>
	<div align="center">
	<h1><u>Login</u></h1>
	
	<% 
	// get parametersss
	String username = request.getParameter("username");
	String password = request.getParameter("password");
	
	// Get data from DB
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.4:3306/litdb?" 
	+ "user=root&password=ojmayonnaise");
	
	PreparedStatement pst = connection.prepareStatement
	%>
</body>
</html>