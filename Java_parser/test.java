package com.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.DB.connection.Connect;

public class Add extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        int empid = Integer.parseInt(request.getParameter("empid"));
        try {
            HttpSession session = request.getSession();
            AddLocations(session, request, response);

        } catch (Exception e) {
            e.printStackTrace();
            if (empid == 11) {
                RequestDispatcher ReqDis = request.getRequestDispatcher("Adminerror.jsp");
                ReqDis.forward(request, response);

            } else {
                RequestDispatcher ReqDis = request.getRequestDispatcher("error.jsp");
                ReqDis.forward(request, response);
            }
        }
    }

    private void AddLocations(HttpSession session, HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        int empid = Integer.parseInt(request.getParameter("empid"));

        String truckType = request.getParameter("cap");
        int prority = Integer.parseInt(request.getParameter("priority"));
        int Volume = Integer.parseInt(request.getParameter("volume"));
        int cap = 0;
        Connection con = Connect.getconnection();
        PreparedStatement ps1 = con.prepareStatement("select capacity from truckparam where typeofgood=?  ");
        ps1.setString(1, truckType);
        ResultSet rs1 = ps1.executeQuery();
        while (rs1.next()) {
            cap = rs1.getInt("capacity");
        }
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("long"));

        // String Good=request.getParameter("type");
        String location = org.apache.commons.lang3.StringUtils.capitalize(request.getParameter("location"));

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `mckc`.`autoparam` (`typeofgood`,`capacity`, `Origin`, `lat`, `long`,`Pirority`,`Volume Rating`) VALUES  (?,?,?,?,?,?,?) ");

        ps.setString(1, truckType);
        ps.setInt(2, cap);
        ps.setString(3, location);
        ps.setDouble(4, lat);
        ps.setDouble(5, lon);
        ps.setInt(6, prority);
        ps.setInt(7, Volume);

        ps.executeUpdate();

        RequestDispatcher ReqDis = request.getRequestDispatcher("adminAuto.jsp");
        ReqDis.forward(request, response);

    }

    public static int extractInt(String str) {
        Matcher matcher = Pattern.compile("\\d+").matcher(str);

        if (!matcher.find())
            throw new NumberFormatException("For input string [" + str + "]");

        return Integer.parseInt(matcher.group());
    }
}