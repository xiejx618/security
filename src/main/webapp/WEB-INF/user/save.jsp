<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
    <script src="${pageContext.servletContext.contextPath}/static/jquery/jquery.min.js" type="text/javascript"></script>
</head>
<body>
<form action="save" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <c:if test="${action=='update'}"><div>用户ID:${user.id}<input type="hidden" value="${user.id}" name="id"></div></c:if>
    <div>用户名:<input type="text" value="${user.username}" name="username"/></div>
    <div>密码:<input type="text" value="${user.password}" name="password"/></div>
    <div>是否启用:<select name="enabled">
        <option value="true" <c:if test="${user.enabled}">selected="selected"</c:if>>是</option>
        <option value="false" <c:if test="${!user.enabled}">selected="selected"</c:if>>否</option>
    </select></div>
    <div><input type="submit" value="保存"/></div>
</form>
</body>
</html>
