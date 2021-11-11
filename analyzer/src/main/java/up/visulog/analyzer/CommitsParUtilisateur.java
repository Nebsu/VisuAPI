package up.visulog.analyzer;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.text.Normalizer;

public class CommitsParUtilisateur extends getAPI {

    public CommitsParUtilisateur(int project, String token, String adresse) {
        super(project, token, adresse);
    }

    private LinkedList<String> recupererMembres() throws IOException, ParseException {
        try {   
            request("projects/"+String.valueOf(this.Project)+
            "/members/all", null);
        } catch (Exception e) {
            System.out.println("Erreur dans la récupération des membres");
        }
        LinkedList<String> members = new LinkedList<String>();
        JSONParser jsParser = new JSONParser();
        JSONArray userArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
        for (Object user : userArray) {
            JSONObject temp = (JSONObject) user;
            members.add(temp.get("name").toString());
        }
        return members;
    }

    private static String removeAccent(String source) {
		return Normalizer.normalize(source, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}

    public int[] recupererCommits() throws IOException {
        // LinkedList<String> users = recupererMembres();
        int acc = 1; // accumulateur de pages
        int acc2 = 0; // accumulateur de commits par page
        boolean b = true;
        LinkedList<String> users = new LinkedList<String>();
        int[] nbCommits;
        // LinkedList<String> detectedUsers = new LinkedList<String>();
        do {
            try {
                request("projects/"+String.valueOf(this.Project)+
                "/repository/commits", "all=true&per_page=50000&page="+String.valueOf(acc));
            } catch (Exception e) {
                System.out.println("Erreur commits");
                return null;
            }
            acc++;
            JSONParser jsParser = new JSONParser();
            try {
                JSONArray commitArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
                for (Object user : commitArray) {
                    JSONObject temp = (JSONObject) user;
                    String name = temp.get("author_name").toString();
                    String newName = "";
                    for (int i=0; i<name.length(); i++) {
                        newName += String.valueOf(Character.toUpperCase(name.charAt(i)));
                    }
                    newName = CommitsParUtilisateur.removeAccent(newName);
                    if (!users.contains(newName)) {
                        users.add(newName);
                    }
                    acc2++;
                }
                nbCommits = new int[users.size()];
                for (Object user : commitArray) {
                    JSONObject temp = (JSONObject) user;
                    String name = temp.get("author_name").toString();
                    String newName = "";
                    for (int i=0; i<name.length(); i++) {
                        newName += String.valueOf(Character.toUpperCase(name.charAt(i)));
                    }
                    newName = CommitsParUtilisateur.removeAccent(newName);
                    if (users.contains(newName)) {
                        nbCommits[users.indexOf(newName)]++;
                    }
                }
                if (acc2<50000) b = false;
            } catch (ParseException e) {
                System.out.println("Erreur ParseException");
                return null;
            }
        } while (b);
        for (String user : users) {
            System.out.println(user+" : "+nbCommits[users.indexOf(user)]+" commits");
        }
        return nbCommits;
        // commits?ref_name=master&all=true&per_page=100&page=x
    }

    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        // CommitsParUtilisateur p2 = new CommitsParUtilisateur(278964, null, "https://gitlab.com");
        p.recupererCommits();
        // p2.recupererCommits();
    }

}