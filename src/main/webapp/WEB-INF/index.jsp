<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
</head>
<body>
首页&nbsp;&nbsp;<c:url value="/user/list" var="userListUrl"/><a href="${userListUrl}">用户列表</a>&nbsp;&nbsp;<c:url value="/logout" var="logoutUrl"/><a href="${logoutUrl}">退出</a>
</body>
</html>
