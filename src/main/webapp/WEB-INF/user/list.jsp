<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>test</title>
    <style type="text/css">
        table{border-collapse: collapse; border: none;}
        th,td{border: 1px solid #92c649;}
    </style>
</head>
<body>
<div><c:url value="/" var="indexUrl"/><a href="${indexUrl}">首页</a></div>
<div><a href="add">添加</a></div>
<table>
    <tr><th>编号</th><th>用户名</th><th>密码</th><th>帐号是否过期</th><th>帐号是否锁定</th><th>凭证是否过期</th><th>是否启用</th><th>操作</th></tr>
    <c:forEach items="${page.content}" var="item">
        <tr><td>${item.id}</td><td>${item.username}</td><td>${item.password}</td>
            <td><c:choose><c:when test="${item.accountNonExpired}">否</c:when><c:otherwise>是</c:otherwise></c:choose></td>
            <td><c:choose><c:when test="${item.accountNonLocked}">否</c:when><c:otherwise>是</c:otherwise></c:choose></td>
            <td><c:choose><c:when test="${item.credentialsNonExpired}">否</c:when><c:otherwise>是</c:otherwise></c:choose></td>
            <td><c:choose><c:when test="${item.enabled}">是</c:when><c:otherwise>否</c:otherwise></c:choose></td>
            <td><a href="update?id=${item.id}">修改</a>&nbsp;&nbsp;<a href="delete?id=${item.id}">删除</a></td></tr>
    </c:forEach>
</table>
<div>
    <c:if test="${page.hasPrevious()}"><a href="list?page=${page.number-1}&size=${page.size}">前一页</a></c:if>
    <c:if test="${page.hasNext()}"><a href="list?page=${page.number+1}&size=${page.size}">后一页</a></c:if>
    ${page.number}/${page.totalPages},共${page.totalElements}条记录</div>
</body>
</html>
