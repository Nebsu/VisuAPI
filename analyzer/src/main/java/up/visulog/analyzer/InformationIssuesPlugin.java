package up.visulog.analyzer;

import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class InformationIssuesPlugin extends getAPI {

    public InformationIssuesPlugin(String project, String token, String adresse) {
        super(project, token, adresse);

    }

     public LinkedList<String> getData(String information) throws IOException, ParseException {
            requestIssue("/issues");
            return readAndTabJsonfile(information);
    }
    public LinkedList<String> readAndTabJsonfile(String valeurArecuperer) throws IOException, ParseException {
        LinkedList<String> list = new LinkedList<String>();
        JSONParser jsParser = new JSONParser();
        JSONArray issueArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
        for (Object issue : issueArray) {
            JSONObject temp = (JSONObject) issue;
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
    public HashMap<String,LinkedList<String>> issuesInfo() throws IOException, ParseException {
        HashMap<String,LinkedList<String>> data = new HashMap<String,LinkedList<String>>();
        String [] element = {"iid","title","state","created_at","updated_at","assignees","author"};
        for (int i = 0; i <element.length ; i++) {
            data.put(element[i],getData(element[i]));
            //System.out.println("ça marche" + element[i]);
        }
        return data;
    }

    public static void main(String[] args) throws IOException, ParseException {
        InformationIssuesPlugin p = new InformationIssuesPlugin("3389","65pJShP_Dd8MZPiTGvxC","https://gaufre.informatique.univ-paris-diderot.fr");
        HashMap<String,LinkedList<String>> h = p.issuesInfo();
        for (int i = 0; i <h.get("iid").size() ; i++) {
            System.out.println(h.get("iid").get(i));
            }
        }
    }


