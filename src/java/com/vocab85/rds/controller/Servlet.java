/*
 * Author: jianqing
 * Date: Jan 30, 2021
 * Description: This document is created for
 */
package com.vocab85.rds.controller;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailException;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vocab85.rds.network.AliOSS;
import com.vocab85.rds.network.DBM;
import com.vocab85.rds.network.TestBaiduTranslate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jianqing
 */
@WebServlet(name = "Servlet", urlPatterns =
{
    "/Login", "/GetUserGroups", "/CreateGroup", "/GetGroupItem", "/ChangeList", "/UpdateGroup", "/DeleteGroup", "/ChatBot",
    "/Logout", "/Register", "/Verify", "/ForgetPassword", "/PasswordRecovery", "/PasswordRecoveryPage",
    "/GetLanguage", "/Translate"

}, loadOnStartup = 1)
public class Servlet extends HttpServlet
{

    private String baseUrl = "https://decider.85vocab.com";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Servlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String path = request.getServletPath();
        switch (path)
        {
            case "/Login":
                processLogin(request, response);
                break;
            case "/GetUserGroups":
                processGetUserGroups(request, response);
                break;
            case "/GetGroupItem":
                processGetGroupItem(request, response);
                break;
            case "/CreateGroup":
                //processCreateGroup(request, response);
                JSONObject json = JSONUtil.createObj();
                methodNotAllowed(json);
                writeResponse(json, response);
                break;
            case "/ChangeList"://
            //processChangeList(request, response);
            case "/RequestWebToken":
                //abandoned
                //processWebToken(request, response);
                processRequest(request, response);
                break;
            case "/UpdateGroup":
                processUpdateGroup(request, response);
                break;
            case "/DeleteGroup":
                processDeleteGroup(request, response);
                break;
            case "/ChatBot":
                processChatBot(request, response);
                break;
            case "/Logout":
                processLogout(request, response);
                break;
            case "/Register":
                processRegister(request, response);
                break;
            case "/Verify":
                processVerify(request, response);
                break;
            case "/PasswordRecovery":
                processPasswordRecovery(request, response);
                break;
            case "/PasswordRecoveryPage":
                processPasswordRecoveryPageGET(request, response);
                break;

            default:
                processRequest(request, response);
                break;
        }
    }

    protected void processGetLanguagePOST(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String ua = req.getHeader("X-Requested-By");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        try (PrintWriter writer = resp.getWriter())
        {
            if (ua.equals("SimpleMusicPlayer"))
            {
                JSONObject jsonr = JSONUtil.parseObj(ServletUtil.getBody(req));
                String q = jsonr.getStr("q");
                writer.write(TestBaiduTranslate.getSourceLanguageJSONStr(q));
            } else
            {
                JSONObject jsonr = JSONUtil.createObj();
                jsonr.putOnce("error_code", 401);
                jsonr.putOnce("error_msg", "暂时不支持该客户端！");
                writer.write(jsonr.toStringPretty());
            }
        }
    }

    protected void processPasswordRecoveryPageGET(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.getSession().setAttribute("pass", true);
        req.getRequestDispatcher(req.getServletPath() + ".jsp").forward(req, resp);
    }

    protected void processPasswordRecoveryPagePOST(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        HttpSession session = req.getSession();
        String password = req.getParameter("password");
        String bash = req.getParameter("bash");
        if (StrUtil.isAllNotBlank(password, bash))
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int userid = db.getUserIdFromVerifyBash(bash);
                if (userid > 0)
                {
                    int rows = db.updateUserPassword(userid, password);
                    db.deleteFromUserBash(bash, userid);
                    session.setAttribute("msg", "<div style='color:green'>密码更改成功，现在可以使用你的新密码登录啦！</div>");
                } else
                {
                    session.setAttribute("msg", "该用户不存在");
                }

            } catch (SQLException | ClassNotFoundException sqle)
            {
                session.setAttribute("msg", "sql服务器错误，请联系support@jianqinggao.com解决");
            }
        } else
        {
            session.setAttribute("msg", "有attribute留空，请重新输入。");
        }
        session.setAttribute("pass", false);

        req.getRequestDispatcher(req.getServletPath() + ".jsp").forward(req, resp);
    }

    protected void processPasswordRecovery(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //HttpSession session = req.getSession();
        JSONObject json = JSONUtil.createObj();
        int id;
        String email = req.getParameter("email");
        if (StrUtil.isEmpty(email))
        {
            putCodeMsg(json, 400, "电子邮件地址不能为空！");
        } else
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                id = db.findUserIdByEmail(email);
                if (id > 0)
                {
                    String bash = RandomUtil.randomString(50);
                    db.addBash(id, bash);
                    MailUtil.sendText(email, "森森选择器密码恢复", "您好"
                            + "\n您最近申请了一个密码恢复\n"
                            + "请访问下面的链接完成密码恢复\n"
                            + baseUrl + "/PasswordRecoveryPage?bash=" + bash
                            + "\n 如果不是您本人所为, 请忽略这封邮件并向support@jianqinggao.com反馈情况");
                    successMsg(json);
                } else
                {
                    putCodeMsg(json, 400, "经查，该邮箱未注册。");
                }
            } catch (SQLException | ClassNotFoundException e)
            {
                dbError(json);
                e.printStackTrace();
            } catch (MailException me)
            {
                mailError(json);
            }
        }
        writeResponse(json, resp);
    }

    protected void processVerify(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String bash = req.getParameter("bash");
        HttpSession s = req.getSession();
        if (StrUtil.isBlank(bash))
        {
            s.setAttribute("msg", "缺失bash码，请您复制完整链接！");
        } else
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int id = db.getUserIdFromVerifyBash(bash);
                if (id > 0)
                {
                    db.updateUserStatus(true, id);
                    db.deleteFromUserBash(bash, id);
                    s.setAttribute("msg", "账号激活成功，您可以继续使用啦！");
                } else
                {
                    s.setAttribute("msg", "bash码错误，请您复制完整链接或该链接已失效！");
                }
            } catch (Exception e)
            {
                s.setAttribute("msg", "数据库出错了，呜呜呜");
                e.printStackTrace();
            }
        }
        req.getRequestDispatcher(req.getServletPath() + ".jsp").forward(req, resp);
    }

    protected void processLogout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        HttpSession session = req.getSession();
        JSONObject json = JSONUtil.createObj();
        String userToken = getUserToken(req);
        User user = getUser(session);
        if (user != null)
        {
            userToken = user.getToken();
        }

        try (DBM db = DBM.getDefaultInstance())
        {
            db.clearToken(userToken);
            successMsg(json);
        } catch (Exception e)
        {
            dbError(json);
        }
        writeResponse(json, resp);
        session.invalidate();
    }

    protected void processRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        JSONObject reqJSON, json;
        json = JSONUtil.createObj();
        String data = readRequestBody(req);
        //System.out.println(data);
        if (StrUtil.isEmpty(data))
        {
            putCodeMsg(json, 400, "请求body为空！");
        } else
        {
            try
            {
                reqJSON = JSONUtil.parseObj(data);
                //System.out.println(reqJSON);
                User user = new User();
                user.setUserId(RandomUtil.randomInt(1000));
                user.setPassword(reqJSON.getStr("password"));
                user.setUsername(reqJSON.getStr("username"));
                user.setToken(null);
                user.setEmail(reqJSON.getStr("email"));
                user.setActive(false);
                //System.out.println(user);
                if (user.isReady())
                {
                    //connect to db only if ready
                    try (DBM db = DBM.getDefaultInstance())
                    {
                        if (db.isEmailExists(user.getEmail()))
                        {
                            putCodeMsg(json, HttpStatus.HTTP_BAD_REQUEST, "该邮箱已被注册，请换一个邮箱注册");
                        } else if (db.isUsernameExists(data))
                        {
                            putCodeMsg(json, HttpStatus.HTTP_BAD_REQUEST, "该用户名已被注册，请换一个用户名注册");
                        } else
                        {
                            successMsg(json);
                            String bash = RandomUtil.randomString(60);
                            db.registerUser(user);
                            db.addBash(user.getUserId(), bash);
                            MailUtil.sendText(user.getEmail(), "森森选择器注册确认", "您最近注册了森森选择器的账号。为保证安全，请访问下面的链接完成验证\n"
                                    + baseUrl + "/Verify?bash=" + bash);

                        }

                    } catch (SQLException | ClassNotFoundException sqle)
                    {
                        dbError(json);
                        sqle.printStackTrace();
                        AliOSS.logError(sqle);
                    } catch (cn.hutool.extra.mail.MailException me)
                    {
                        mailError(json);
                        me.printStackTrace();
                    }
                } else
                {
                    putCodeMsg(json, 400, "用户信息有空白，请检查后重新提交");
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                putCodeMsg(json, 400, "上传数据格式有误！");
            }
        }

        System.out.println("JSON. " + json);
        writeResponse(json, resp);

    }

    protected void processChatBot(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //String usertoken = getUserToken(req);
        JSONObject json = JSONUtil.createObj();
        javax.servlet.http.HttpSession session = req.getSession();

        String message = req.getParameter("message");
        User user = getUser(session);

        //用户验证
        if (user == null)
        {
            putCodeMsg(json, 401, "您未登录，请先登录，并允许cookie使用！");
        } else if (StrUtil.isEmpty(message))
        {
            putCodeMsg(json, 400, "请勿发送空信息");
        } else
        {
            //找session id
            String chatId = (String) session.getAttribute("chatid");
            System.out.println(chatId);
            if (chatId == null)
            {
                chatId = RandomUtil.randomString(8);
                session.setAttribute("chatid", chatId);
            }
            JSONObject chat = ChatBot.chat(message, chatId);
            String msg = chat.getStr("msg");
            if (msg.equals("ok"))
            {
                putCodeMsg(json, HttpStatus.HTTP_OK, chat.getJSONObject("data").getStr("answer"));
            } else
            {
                putCodeMsg(json, HttpStatus.HTTP_INTERNAL_ERROR, msg);
            }
        }
        writeResponse(json, resp);
    }

    protected void processDeleteGroup(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        HttpSession s = req.getSession();
        String token = getUserToken(req);
        JSONObject json = JSONUtil.createObj();
        User user = getUser(s);
        if (StrUtil.isEmpty(token) && user == null)
        {
            emptyTokenError(json);
        } else
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int id;
                if (user == null)
                {
                    id = db.getIdByToken(token);
                } else
                {
                    id = user.getUserId();
                }

                int listId = Integer.parseInt(req.getParameter("listid"));
                if (id < 0)
                {
                    //token invalid
                    unauthorizedError(json);
                } else if (db.isListBelongToUser(id, listId))
                {
                    //token valid, and list belongs to user
                    int rows = db.deleteList(listId);
                    db.deleteAllItemsFromList(listId);
                    if (rows > 0)
                    {
                        successMsg(json);
                    } else
                    {
                        putCodeMsg(json, 400, "您要删除的列表不存在！");
                    }
                }

            } catch (NumberFormatException nfe)
            {
                putCodeMsg(json, 400, "请为listid输入一个正整数");
            } catch (SQLException | ClassNotFoundException sqle)
            {
                dbError(json);
            }

        }
        writeResponse(json, resp);
    }

    protected void processUpdateGroup(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        HttpSession s = req.getSession();
        String token = getUserToken(req);
        JSONObject json = JSONUtil.createObj();
        User user = getUser(s);
        if (StrUtil.isNotBlank(token) || user != null)
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int userId;
                if (user == null)
                {
                    userId = db.getIdByToken(token);
                } else
                {
                    userId = user.getUserId();
                }

                int id = Integer.parseInt(req.getParameter("listid"));
                String old = req.getParameter("old");
                String newMsg = req.getParameter("new");
                if (userId > 0)
                {
                    if (db.isListBelongToUser(userId, id))
                    {
                        if (StrUtil.isBlank(old))
                        {
                            //old and new all blank
                            if (StrUtil.isBlank(newMsg))
                            {
                                putCodeMsg(json, 400, "选项文本不能留空！");
                            } else
                            {
                                //insert new
                                db.addIntoList(id, newMsg);
                                successMsg(json);
                            }

                        } else if (StrUtil.isBlank(newMsg))
                        {
                            db.removeItemFromList(id, old);
                            successMsg(json);
                        } else
                        {
                            db.updateList(id, old, newMsg);
                            successMsg(json);
                        }
                    } else
                    {
                        putCodeMsg(json, 401, "该列表并不属于你，请使用属于你的列表。");
                    }

                } else
                {
                    unauthorizedError(json);
                }
            } catch (SQLException | ClassNotFoundException e)
            {
                dbError(json);
            } catch (NumberFormatException nfe)
            {
                putCodeMsg(json, 400, "请输入整数集合id哦！");
            }
        } else
        {
            emptyTokenError(json);
        }
        writeResponse(json, resp);
    }

    protected void processWebToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String usertoken = getUserToken(request);
        JSONObject json = JSONUtil.createObj();
        if (StrUtil.isNotBlank(usertoken))
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int rows = db.updateWebtokenByToken(usertoken, usertoken);
                if (rows == 1)
                {
                    successMsg(json);
                } else
                {
                    unauthorizedError(json);
                }
            } catch (SQLException | ClassNotFoundException e)
            {
                dbError(json);
            }
        } else
        {
            emptyTokenError(json);
        }
        writeResponse(json, response);
    }

    protected void processChangeList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String webtoken = getUserToken(request);
        String groupIdStr = request.getHeader("X-token");
        HttpSession s = request.getSession();
        User user = getUser(s);
        if (StrUtil.isNotBlank(webtoken) || user != null)
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int grourId = Integer.parseInt(groupIdStr);
                //String usertoken = db.getUserTokenByWebToken(webtoken);
                JSONArray groupItems = db.getGroupItems(grourId);
                request.getSession().setAttribute("items", groupItems);
                request.getRequestDispatcher("/WEB-INF" + request.getServletPath() + ".jsp");

            } catch (SQLException | ClassNotFoundException e)
            {
                response.sendError(500, "Internal Database Error");
            } catch (NumberFormatException nfe)
            {
                response.sendError(403, "Invalid group ID");
            }
        } else
        {
            response.sendError(401, "Request Not authorized");
        }
    }

    protected void processCreateGroup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession s = request.getSession();
        //String name = request.getParameter("name");
        int groupId = RandomUtil.randomInt(1, 9999);
        //read the request body
        String data = readRequestBody(request);
        String token = getUserToken(request);
        JSONObject json = JSONUtil.createObj();
        User user = getUser(s);
        if (StrUtil.isAllNotBlank(token, data))
        {
            JSONObject jsonr = JSONUtil.parseObj(data);
            String name = jsonr.getStr("name");
            JSONArray options = jsonr.getJSONArray("items");
            if (StrUtil.isNotBlank(name) || user != null)
            {
                try (DBM db = DBM.getDefaultInstance())
                {
                    int userId;
                    if (user == null)
                    {
                        userId = db.getIdByToken(token);
                    } else
                    {
                        userId = user.getUserId();
                    }

                    if (userId > 0)
                    {
                        if (hasDuplicatedElements(options))
                        {
                            putCodeMsg(json, 403, "");
                        } else
                        {
                            db.addGroup(userId, groupId, name);
                            db.addItems(groupId, options);
                            successMsg(json);
                        }
                    } else
                    {
                        unauthorizedError(json);
                    }
                } catch (SQLException | ClassNotFoundException e)
                {
                    dbError(json);
                }
            } else
            {
                putCodeMsg(json, 403, "选项组名不能为空！");
            }
        }
        writeResponse(json, response);
    }

    protected void processLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        JSONObject json = JSONUtil.createObj();
        HttpSession session = request.getSession();
        if (StrUtil.isAllNotEmpty(username, password))
        {
            try (DBM m = DBM.getDefaultInstance())
            {
                User user = m.login(username, password);
                if (user != null)
                {
                    session.setAttribute("user", user);
                    putCodeMsg(json, 200, "用户登录成功！");
                    json.putOpt("token", user.getToken());
                    json.putOpt("userid", user.getUserId());
                } else
                {
                    putCodeMsg(json, 401, "用户名或密码错误！");
                }
            } catch (Exception e)
            {
                AliOSS.logError(e);
                json = JSONUtil.createObj();
                json.putOnce("code", 500);
                json.putOnce("msg", "远程服务器数据库出错了");
            }
        } else
        {
            json = JSONUtil.createObj();
            json.putOnce("code", 402);
            json.putOnce("msg", "用户名或密码空白");
        }

        //response.setStatus(json.getInt("code"));
        writeResponse(json, response);
    }

    /**
     * Allow user to get the groups.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processGetUserGroups(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        User user = getUser(session);
        String token = getUserToken(request);
        JSONObject returnObj = JSONUtil.createObj();
        if (StrUtil.isNotBlank(token) || user != null)
        {
            try (DBM db = DBM.getDefaultInstance())
            {
                int userid;
                if (user == null)
                {
                    userid = db.getIdByToken(token);
                } else
                {
                    userid = user.getUserId();
                }

                if (userid > 0)
                {
                    //user is valid
                    JSONArray groups = db.getUserGroups(userid);
                    returnObj.putOnce("code", 200);
                    returnObj.putOnce("msg", "查询成功！");
                    returnObj.putOnce("groups", groups);
                } else
                {
                    //user token is invalid
                    unauthorizedError(returnObj);
                    //response.setStatus(401);
                }
            } catch (Exception e)
            {
                //server error
                dbError(returnObj);
                //response.setStatus(500);
            }
        } else
        {
            //empty token
            emptyTokenError(returnObj);
            //response.setStatus(401);
        }
        //return the response
        writeResponse(returnObj, response);
    }

    /**
     * Process the get group item request.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processGetGroupItem(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        JSONObject json = JSONUtil.createObj();
        String token = getUserToken(request);
        String groupIdStr = request.getParameter("id");
        User user = getUser(session);
        if (StrUtil.isNotBlank(token) || user != null)
        {
            //not blank
            if (NumberUtil.isInteger(groupIdStr))
            {
                //is valid id
                try (DBM dbm = DBM.getDefaultInstance())
                {
                    //authorize user
                    int userId;
                    if (user == null)
                    {
                        userId = dbm.getIdByToken(token);
                    } else
                    {
                        userId = user.getUserId();
                    }

                    if (userId > 0)
                    {
                        //user id valid
                        json.putOpt("items", dbm.getGroupItems(Integer.parseInt(groupIdStr)));
                        successMsg(json);
                    } else
                    {
                        //user token invalid
                        unauthorizedError(json);
                        response.setStatus(401);
                    }
                } catch (SQLException | ClassNotFoundException e)
                {
                    //internal db error
                    dbError(json);
                    response.setStatus(500);
                } catch (NumberFormatException nfe)
                {
                    //group id invalid
                    putCodeMsg(json, 403, "请输入一个正整数的组id");
                    response.setStatus(403);
                }

            } else
            {
                //group id invalid
                putCodeMsg(json, 403, "请输入一个正整数的组id");
                response.setStatus(403);
            }
        } else
        {
            //empty token
            emptyTokenError(json);
            response.setStatus(401);
        }
        writeResponse(json, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        //processRequest(request, response);
        String p = request.getServletPath();
        switch (p)
        {
            case "/CreateGroup":
                processCreateGroup(request, response);
                break;
            case "/Register":
                processRegister(request, response);
                break;
            case "/PasswordRecoveryPage":
                processPasswordRecoveryPagePOST(request, response);
                break;
            case "/GetLanguage":
                processGetLanguagePOST(request, response);
                break;
            default:
                JSONObject json = JSONUtil.createObj();
                response.setStatus(405);
                methodNotAllowed(json);
                writeResponse(json, response);
                break;
        }

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

    /**
     * // * Verify the request by its x-token header. // * If the user
     * verification is invalid, then return an error and directly write it in
     * response. // * Warning: This will directly modify the http response. //
     *
     *
     * @return -1 if the token is invalid. Otherwise return the user id. //
     */
//    private int verifyUserByToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
//    {
//        String token = request.getHeader("X-token");
//        if(StrUtil.isBlank(token))
//        {
//            //empty verify
//            emptyTokenError(json);
//        }
//    }
    private void listNotBelongError(JSONObject json)
    {
        putCodeMsg(json, 401, "这个组并不属于你哦");
    }

    private static User getUser(HttpSession session)
    {
        return (User) session.getAttribute("user");
    }

    public static String readRequestBody(HttpServletRequest request) throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null)
        {
            buffer.append(line);
            buffer.append(System.lineSeparator());
        }
        return buffer.toString();
    }

    protected static void mailError(JSONObject json)
    {
        putCodeMsg(json, HttpStatus.HTTP_BAD_REQUEST, "邮箱地址不存在，请检查重新输入您的有效邮箱！");
    }

    /**
     * Get the auth token from user. The "X-token" header usually.
     *
     * @param request
     * @return
     */
    protected static String getUserToken(HttpServletRequest request)
    {
        return request.getHeader("X-token");
    }

    //protected static String getUserToken()
    private static void methodNotAllowed(JSONObject json)
    {
        putCodeMsg(json, 405, "该请求方法暂不支持");
    }

    private static void successMsg(JSONObject json)
    {
        putCodeMsg(json, 200, "查询成功！");
    }

    private static void putCodeMsg(JSONObject json, int code, String msg)
    {
        json.putOpt("code", code);
        json.putOpt("msg", msg);
    }

    private void dbError(JSONObject json)
    {
        putCodeMsg(json, 500, "服务器内部数据库出现问题，请稍后重试！");
    }

    private void unauthorizedError(JSONObject json)
    {
        putCodeMsg(json, 401, "口令不存在或已经过期，无法验证用户！");
    }

    private void emptyTokenError(JSONObject json)
    {
        putCodeMsg(json, 401, "X-token 请求头不得为空，未找到合适验证的session, 无法验证用户！");
    }

    private void writeResponse(JSONObject json, HttpServletResponse response) throws IOException
    {
        //set token
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(json.getInt("code"));
        //write response
        try (PrintWriter pw = response.getWriter())
        {
            pw.write(json.toString());
            pw.flush();
        }
    }

    public static boolean hasDuplicatedElements(List<Object> a)
    {
        int count = 0;
        for (int j = 0; j < a.size(); j++)
        {
            for (int k = j + 1; k < a.size(); k++)
            {
                if (a.get(j).toString().equalsIgnoreCase(a.get(k).toString()))
                {
                    count++;
                }
            }
            if (count == 1)
            {
                return true;
            }
            count = 0;
        }
        return false;
    }

    public static void main(String[] args)
    {
        ArrayList<Object> l = new ArrayList<>();
        l.add("A");
        l.add("B");
        l.add("B");

        System.out.println(hasDuplicatedElements(l));
    }
}
