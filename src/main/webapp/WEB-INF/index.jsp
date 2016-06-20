<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
</head>
<body>
首页&nbsp;&nbsp;<a href="${pageContext.servletContext.contextPath}/user/list">用户列表</a>&nbsp;&nbsp;
<form style="display: inline" action="${pageContext.servletContext.contextPath}/logout" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/><input type="submit" value="退出"/></form>
</body>
</html>
