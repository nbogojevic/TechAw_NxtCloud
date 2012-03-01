package nb.driverobot;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TallyServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Ballot ballot = BallotContainer.get(getServletContext());
    JSON json = new JSON();
    for (String choice :  ballot.getChoices()) {
      json.append(choice, ballot.getVotesForChoice(choice));
    }
    json.marshall(resp);
  }
}