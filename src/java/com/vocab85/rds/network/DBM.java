/*
 * Author: jianqing
 * Date: Jan 30, 2021
 * Description: This document is created for connecting database,

 */
package com.vocab85.rds.network;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.vocab85.rds.controller.User;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jianqing
 */
public class DBM implements AutoCloseable
{

    private java.sql.Connection dbConn;

    public DBM(String dbName, String host, String dbUsername, String dbPassword, boolean useSSL) throws SQLException, ClassNotFoundException
    {
        //this.dbName = dbName;
        this.establishConnection(dbName, host, dbUsername, dbPassword, useSSL);
        //stablishConnection(dbName, dbName, dbUsername, dbPassword, true);); 
    }

    public int removeItemFromList(int listid, String oldContent) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("DELETE FROM listitems WHERE value=? AND listid=?");
        ps.setString(1, oldContent);
        ps.setInt(2, listid);
        return ps.executeUpdate();
    }

    public boolean isListBelongToUser(int userId, int listId) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM userlists WHERE listid=? AND userid=?");
        ps.setInt(1, listId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public int deleteList(int listId) throws SQLException
    {
        Statement s = dbConn.createStatement();
        s.executeUpdate("DELETE FROM userlists WHERE listid=" + listId);
        return s.executeUpdate("DELETE FROM listitems WHERE listid=" + listId);
    }

    public int updateList(int listId, String oldOption, String newOption) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE listitems SET value=? WHERE listid=? AND value=?");
        ps.setString(1, newOption);
        ps.setInt(2, listId);
        ps.setString(3, oldOption);
        return ps.executeUpdate();
    }

    public String getUserTokenByWebToken(String webtoken) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT token FROM users WHERE webtoken=?");
        ResultSet rs;
        ps.setString(1, webtoken);
        rs = ps.executeQuery();
        if (rs.next())
        {
            return rs.getString("token");
        } else
        {
            return null;
        }
    }

    public int updateWebtokenByToken(String webtoken, String usertoken) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET webtoken=? WHERE token=?");
        JSONObject json = JSONUtil.createObj();
        ps.setString(1, webtoken);
        ps.setString(2, usertoken);
        return ps.executeUpdate();
    }

    public User login(String username, String password) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
        //JSONObject json = JSONUtil.createObj();
        String newToken = RandomUtil.randomString(10);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        User user;
        if (rs.next())
        {
            int id = rs.getInt("id");
            updateToken(id, newToken);
            user = new User();
            user.setUserId(id);
            user.setToken(newToken);
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
//            json.putOnce("code", 200);
//            json.putOnce("msg", "用户登录成功");
//            json.putOnce("token", newToken);
//            json.putOnce("userid", id);
        } else
        {
            user = null;
            //json.putOnce("code", 401);
            //json.putOnce("msg", "用户名或密码错误！");
            //json.putOnce("token", JSONNull.NULL);
            //json.putOnce("userid", JSONNull.NULL);
        }
        return user;
    }

    /**
     * Get the user id by user token.
     *
     * @param token The token to be verified.
     * @return The id of the user wants to verify. Otherwise, if the token dne,
     * return -1.
     * @throws SQLException
     */
    public int getIdByToken(String token) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT id FROM users WHERE token=?");
        ResultSet rs;
        ps.setString(1, token);
        rs = ps.executeQuery();
        if (rs.next())
        {
            return rs.getInt("id");
        }
        return -1;
    }

    public int updateToken(int id, String token) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET token=? WHERE id=?");
        ps.setString(1, token);
        ps.setInt(2, id);
        return ps.executeUpdate();
    }

    public JSONArray getUserGroups(int userid) throws SQLException
    {
        JSONObject group = JSONUtil.createObj();
        JSONArray groups = JSONUtil.createArray();
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM userlists WHERE userid=? ORDER BY listname ASC");
        ps.setInt(1, userid);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
        {
            group.putOnce("listid", rs.getInt("listid"));
            group.putOnce("userid", rs.getInt("userid"));
            group.putOnce("listname", rs.getString("listname"));
            groups.add(group);
            group = JSONUtil.createObj();
        }
        return groups;
    }

    public JSONArray getGroupItems(int groupId) throws SQLException
    {
        JSONArray items = JSONUtil.createArray();

        //JSONObject item = JSONUtil.createObj();
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM listitems WHERE listid=? ORDER BY value ASC");
        ps.setInt(1, groupId);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
        {
            items.add(rs.getString("value"));
        }
        return items;
    }

    public int addGroup(int userId, int groupId, String groupName) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO userlists VALUES(?,?,?)");
        ps.setInt(1, userId);
        ps.setInt(2, groupId);
        ps.setString(3, groupName);
        return ps.executeUpdate();
    }

    public int[] addItems(int groupId, java.util.List<Object> items) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO listitems VALUES(?,?)");
        for (Object item : items)
        {
            ps.setInt(1, groupId);
            ps.setString(2, item.toString());
            ps.addBatch();
        }
        return ps.executeBatch();
    }

    public int addIntoList(int groupId, String item) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO listitems VALUES(?,?)");
        ps.setInt(1, groupId);
        ps.setString(2, item);
        ps.addBatch();
        return ps.executeUpdate();
    }

    public int clearToken(String token) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET token=? WHERE token=?");
        ps.setNull(1, java.sql.Types.VARCHAR);
        ps.setString(2, token);
        return ps.executeUpdate();
    }

    public boolean isEmailExists(String email) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE email=?");
        ps.setString(1, email);
        //ps.setString(2, username);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
 public boolean isUsernameExists(String username) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE username=?");
        ps.setString(1, username);
        //ps.setString(2, username);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public int registerUser(User user) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO users VALUES(?,?,?,?,?,?)");
        ps.setInt(1, user.getUserId());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPassword());
        ps.setNull(4, java.sql.Types.VARCHAR);
        ps.setString(5, user.getEmail());
        ps.setBoolean(6, user.isActive());
        return ps.executeUpdate();
    }

    public int findUserIdByEmail(String email)throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT id FROM users WHERE email=?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if(rs.next())
        {
            return rs.getInt(1);
        }else
        {
            return -1;
        }
    }
    
    public int addBash(int userId, String bash) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO userbash VALUES(?,?)");
        ps.setInt(1, userId);
        ps.setString(2, bash);
        return ps.executeUpdate();
    }

    public int updateUserStatus(boolean s, int id) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET open=? WHERE id=?");
        ps.setBoolean(1, s);
        ps.setInt(2, id);
        return ps.executeUpdate();
    }

    public int getUserIdFromVerifyBash(String bash) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT userid FROM userbash WHERE bash=?");
        ResultSet rs;
        //ps.setInt(1, userId);
        ps.setString(1, bash);

        rs = ps.executeQuery();
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return -1;
    }

    public int updateUserPassword(int userid, String newPassword) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET password=? WHERE id=?");
        ps.setString(1, newPassword);
        ps.setInt(2, userid);
        return ps.executeUpdate();
    }
    public int deleteFromUserBash(String bash, int userId) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("DELETE FROM userbash WHERE bash=? OR userid=?");
        //ps.setInt(1, userId);
        ps.setString(1, bash);
        ps.setInt(2, userId);
        return ps.executeUpdate();
    }
    public int deleteAllItemsFromList(int listId) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("DELETE FROM listitems WHERE listid=?");
        ps.setInt(1, listId);
        return ps.executeUpdate();
    }

    public static DBM getDefaultInstance() throws SQLException, ClassNotFoundException
    {
        Setting s = new Setting("db.setting");
        return new DBM("decider", s.getStr("address"), s.getStr("user"),s.getStr("pass"), false);
    }

    public void establishConnection(String dbName, String host, String dbUsername, String dbPassword, boolean useSSL) throws SQLException, ClassNotFoundException
    {
        //NO this.dbConn = dbConn;
        String connectionURL = "jdbc:mysql://" + host + "/" + dbName;
        this.dbConn = null;
        //Find the driver and make connection;

        Class.forName("com.mysql.cj.jdbc.Driver"); //URL for new version jdbc connector.
        Properties properties = new Properties(); //connection system property
        properties.setProperty("user", dbUsername);
        properties.setProperty("password", dbPassword);
        properties.setProperty("useSSL", Boolean.toString(useSSL));//set this true if domain suppotes SSL
        //"-u root -p mysql1 -useSSL false"
        this.dbConn = DriverManager.getConnection(connectionURL, properties);
    }
 
    public static void main(String[] args)
    {
        try
        {
            getDefaultInstance();
        } catch (SQLException | ClassNotFoundException ex)
        {
            Logger.getLogger(DBM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() throws SQLException
    {
        this.dbConn.close();
    }
}
