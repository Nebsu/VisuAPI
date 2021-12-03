package up.visulog.analyzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class InformationIssuesPlugin extends getAPI {
    private HashMap<String, LinkedList<String>> data = new HashMap<String, LinkedList<String>>();
    public InformationIssuesPlugin(String project, String token, String adresse) {
        super(project, token, adresse);

    }



    public void readAndTabJsonfile(String valeurArecuperer) throws IOException, ParseException {

        JSONParser jsParser = new JSONParser();
        JSONArray issueArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
        for (Object issue : issueArray) {
            JSONObject temp = (JSONObject) issue;
            data.get(valeurArecuperer).add(temp.get(valeurArecuperer).toString());
        }
    }

    public void requestIssue() throws IOException, ParseException {
        int page = 1;
        int size = 0;
        int nbrIssues = nbrIssues();
        initElement();
        while (size%100 == 0) {
            try {
                request("projects/" + String.valueOf(this.Project) + "/issues", "per_page=100&page=" + (page));
                addList();
                JSONParser jsonP = new JSONParser();
                JSONArray issues = (JSONArray) jsonP.parse(new FileReader("request.json"));
                size += issues.size();
            } catch (Exception e) {
                System.out.println("Erreur dans la récupération de la donnée suivante:");
            }
            page++;
        }
    }

    public int nbrIssues() throws IOException, ParseException {
        request("projects/" + String.valueOf(this.Project) + "/issues_statistics", null);
        JSONParser jsonP = new JSONParser();
        JSONObject object = (JSONObject) jsonP.parse(new FileReader("request.json"));
        JSONObject satistics = (JSONObject) object.get("statistics");
        JSONObject counts = (JSONObject) satistics.get("counts");
        Long a = (Long) counts.get("all");

        return Math.toIntExact(a);
    }

    public void initElement() throws IOException, ParseException {
        String[] element = {"iid", "title", "state", "created_at", "updated_at", "assignees", "author","description"};
        for (int i = 0; i < element.length; i++) {
            data.put(element[i], new LinkedList<String>());
        }
    }
        public void addList() throws IOException, ParseException {
            String[] element = {"iid", "title", "state", "created_at", "updated_at", "assignees", "author"};
            for (int i = 0; i < element.length; i++) {
                readAndTabJsonfile(element[i]);
            }
        }

    public static void main(String[] args) throws IOException, ParseException {
        InformationIssuesPlugin p = new InformationIssuesPlugin("10582521", "", "https://gitlab.com");
        p.requestIssue();
        for (int i = 0; i < p.data.get("iid").size(); i++) {
           System.out.println(p.data.get("iid").get(i));
       }

    }
}


