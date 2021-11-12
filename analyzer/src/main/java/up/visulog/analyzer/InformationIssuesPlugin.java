package up.visulog.analyzer;

import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class InformationIssuesPlugin extends getAPI {

    public InformationIssuesPlugin(String project, String token, String adresse) {
        super(project, token, adresse);

    }

     public LinkedList<String> getTitle(String information) throws IOException, ParseException {
            requestIssue("/issues");
            return readAndTabJsonfile(information);
    }
    public LinkedList<String> readAndTabJsonfile(String valeurArecuperer) throws IOException, ParseException {
        LinkedList<String> list = new LinkedList<String>();
        JSONParser jsParser = new JSONParser();
        JSONArray userArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
        for (Object user : userArray) {
            JSONObject temp = (JSONObject) user;
            list.add(temp.get(valeurArecuperer).toString());
        }
        return list;
    }

    public void requestIssue(String donnees) {
        try {
            request("projects/" + String.valueOf(this.Project) + donnees, null);
        } catch (Exception e) {
            System.out.println("Erreur dans la récupération de la donnée suivante:" + donnees);
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        InformationIssuesPlugin p = new InformationIssuesPlugin("3389","MkixHCSQPPj_Pwku9TQ1","https://gaufre.informatique.univ-paris-diderot.fr");
        LinkedList<String> list = p.getTitle("title");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

}

