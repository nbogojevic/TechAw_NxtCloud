package nb.driverobot;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class VoteServlet extends HttpServlet {
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String input = req.getParameter("vote");
    Ballot ballot = BallotContainer.get(getServletContext());
    try {
      ballot.vote(input);
    } catch (IllegalArgumentException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;      
    }
    String leader = ballot.getLeader();
    boolean isLeader = leader != null && leader.equals(input);
    
    new JSON().append("isLeader", isLeader).marshall(resp);
  }
}