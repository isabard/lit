<%@ page import="java.util.Date" %>
<%@ page language="java" 
contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 
Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" 
content="text/html; charset=UTF-8">
<title>Home Page</title>
</head>
<body>
<h4>Example...</h4>

<strong>The current time is </strong>
<% out.print(new Date()); %><br><br>

<strong>The user agent is </strong>
<%=request.getHeader("User-Agent") %><br><br>

<%response.addCookie(new Cookie("Test", "Value")); %>

<strong>The user init param value is </strong>
<%=config.getInitParameter("User") %><br><br>

<strong>The user context param value is </strong>
<%=application.getInitParameter("User") %><br><br>

<strong>The user session ID is </strong>
<%=session.getId() %><br><br>

<% pageContext.setAttribute("Test", "Test Value"); %>
<strong>The pagecontext attribute is </strong>
{Name="Test","Value="
<%=pageContext.getAttribute("Test") %>}<br><br>

<strong>The generated servlet name is </strong>
<%=page.getClass().getName() %>

</body>
</html>