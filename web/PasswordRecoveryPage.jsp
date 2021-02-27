<%-- 
    Document   : PasswordRecoveryPage
    Created on : Feb 1, 2021, 7:57:10 PM
    Author     : jianqing
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Password Recovery</title>
    </head>
    <body>
        <h1>密码恢复</h1>
        <c:if test="${pass}">
            <form method="POST" action="PasswordRecoveryPage" accept-charset="utf-8" id="sbmt">
                请输入新密码：<input type="password" name="password" id="password1" size="20">
                再次输入新密码：<input type="password" id="password2" size="20">
                <input type="hidden" value="${param.bash}" name="bash">
                <button type="button" onclick="submit()">提交</button>
            </form>
            <script>
                function submit()
                {
                    var pw1 = document.getElementById("password1").value;
                    var pw2 = document.getElementById("password2").value;
                    if(pw1 === "")
                    {
                        alert("密码1为空，请检查输入")
                    }else if(pw2 === "")
                    {
                        alert("密码2为空，请检查输入")
                    }
                    else if(pw1 === pw2)
                    {
                        document.getElementById("sbmt").submit()
                    }else
                    {
                        alert("两次密码不一致，请重新输入！")
                    }
                }
            </script>
        </c:if>
        <c:if test="${!pass}">
            ${msg}
        </c:if>
    </body>
</html>
