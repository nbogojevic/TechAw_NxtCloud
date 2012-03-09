package nb.driverobot;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetWinnerServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setHeader("Cache-Control", "no-cache");
    new JSON().append("winner", BallotContainer.get(getServletContext()).getWinner()).marshall(resp);
  }
}