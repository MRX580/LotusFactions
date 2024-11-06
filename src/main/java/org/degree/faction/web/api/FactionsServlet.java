package org.degree.faction.web.api;

import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FactionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JSONObject json = new JSONObject();
        json.put("status", "online");
        json.put("players", 5);  // Пример, можно изменить на реальное значение
        json.put("maxPlayers", 100);

        resp.getWriter().write(json.toString());
    }
}
