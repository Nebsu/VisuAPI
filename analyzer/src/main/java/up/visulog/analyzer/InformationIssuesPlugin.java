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
    // L'attribut est une hashmap ou chaque clé représente un élément de la requête, exemple : Title, Author, description, ... etc.
    //Et la valeur est une LinkedList de String qui liste toutes les données contenues dans chaque élément.
    private HashMap<String, LinkedList<String>> data = new HashMap<String, LinkedList<String>>();

    public InformationIssuesPlugin(String project, String token, String adresse) {
        super(project, token, adresse);

    }

    // Cette fonction lis le fichier request.json en extrait chaque object mentionné dans le tableau de la fonction initElement.
    //Et elle l'ajoute dans l'attribut data.
    public void readAndTabJsonfile(String valeurArecuperer) throws IOException, ParseException {
        try {

            JSONParser jsParser = new JSONParser();
            JSONArray issueArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
            for (Object issue : issueArray) {
                JSONObject temp = (JSONObject) issue;
                // System.out.println(valeurArecuperer);
                if (valeurArecuperer.equals("author")) {
                    JSONObject author = (JSONObject) temp.get("author");
                    String name = (String) author.get("name");
                    data.get("author").add(name);
                } else {
                    if (temp.get(valeurArecuperer)!=null)
                    data.get(valeurArecuperer).add(temp.get(valeurArecuperer).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Cette fonction lance une requête et fait appelle à la fonction addList() entre chaque page.
    public void requestIssue() throws IOException, ParseException {
        int page = 1;
        int size = 0;
        int nbrIssues = nbrIssues();
        initElement();
        while (size % 100 == 0) {
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
        System.out.println(size);
    }
    //Cette fonction permet d'obtenir le nombre de tickets.
    public int nbrIssues() throws IOException, ParseException {
        try {
            request("projects/" + String.valueOf(this.Project) + "/issues_statistics", null);
            JSONParser jsonP = new JSONParser();
            JSONObject object = (JSONObject) jsonP.parse(new FileReader("request.json"));
            JSONObject satistics = (JSONObject) object.get("statistics");
            JSONObject counts = (JSONObject) satistics.get("counts");
            Long a = (Long) counts.get("all");
            return Math.toIntExact(a);
        } catch (Exception e) {
            return -1;
        }
    }
    //Cette fonction permet d'initialiser la HashMap data.
    public void initElement() throws IOException, ParseException {
        String[] element = {"iid", "title", "state", "created_at", "updated_at", "assignees", "author","description"};
        for (int i = 0; i < element.length; i++) {
            data.put(element[i], new LinkedList<String>());
        }
    }
    //Cette fonction fait appelle à readAndTabJsonfile pour chaque élément désiré.
    public void addList() throws IOException, ParseException {
        String[] element = {"iid", "title", "state", "created_at", "updated_at", "assignees", "author","description"};
        for (int i = 0; i < element.length; i++) {
            readAndTabJsonfile(element[i]);
        }
    }

    public HashMap<String, LinkedList<String>> getData() {
        return data;
    }

    public static void main(String[] args) throws IOException, ParseException {
        InformationIssuesPlugin p = new InformationIssuesPlugin("3389", "8ax_oKvn8CMzvyPmxUD1", "https://gaufre.informatique.univ-paris-diderot.fr");
        p.requestIssue();
        HashMap<String, LinkedList<String>> data = p.getData();

    }
}


