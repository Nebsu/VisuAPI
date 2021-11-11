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

    private static String normalize(String source) {
        String res = "";
        for (int i=0; i<source.length(); i++) {
            res += String.valueOf(Character.toUpperCase(source.charAt(i)));
        }
		return Normalizer.normalize(res, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}

    private static String[] divide(String source) {
        String n1 = "", n2 = "";
        int k = 0;
        for (int i=0; i<source.length(); i++) {
            char c = source.charAt(i);
            if (c==' ') break;
            n1 += String.valueOf(c);
            k++;
        }
        for (int i=k+1; i<source.length(); i++) {
            char c = source.charAt(i);
            n2 += String.valueOf(c);
        }
        String[] res = {n1, n2};
        return res;
    }

    private static boolean similarity(LinkedList<String> list, String n) {
        n = CommitsParUtilisateur.normalize(n);
        String[] words = CommitsParUtilisateur.divide(n);
        for (String name : list) {
            name = CommitsParUtilisateur.normalize(name);
            String[] words2 = CommitsParUtilisateur.divide(name);
            if (name.equals(words[0]) || name.equals(words[1]) || name.equals(n) ||
                words[0].equals(words2[0]) || words[0].equals(words2[1]) || 
                words[1].equals(words2[0]) || words[1].equals(words2[1])) return true;
        }
        return false;
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

    public int[] recupererCommits() throws IOException {
        int acc = 1; // accumulateur de pages
        int acc2 = 0; // accumulateur de commits par page
        LinkedList<String> users = new LinkedList<String>(); // liste des utilisateurs qui ont commit
        int[] nbCommits; // nombre de commits par utilisateurs (chaque utilisateur a son index)
        do {
            // request api :
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
                    name = CommitsParUtilisateur.normalize(name);
                    if (!users.contains(name)) {
                        users.add(name);
                    }
                    acc2++;
                }
                nbCommits = new int[users.size()];
                for (Object user : commitArray) {
                    JSONObject temp = (JSONObject) user;
                    String name = temp.get("author_name").toString();
                    name = CommitsParUtilisateur.normalize(name);
                    if (users.contains(name)) {
                        nbCommits[users.indexOf(name)]++;
                    }
                }
            } catch (ParseException e) {
                System.out.println("Erreur ParseException");
                return null;
            }
        } while (acc2>=50000);
        try {
            LinkedList<String> getOtherUsers = recupererMembres();
            for (String user : getOtherUsers) {
                if (!users.contains(user) && !CommitsParUtilisateur.similarity(users, user)) {
                    users.add(user);
                }
            }
            int indexOtherUsers = 0;
            for (String user : users) {
                System.out.println(user+" : "+nbCommits[users.indexOf(user)]+" commit(s)");
                indexOtherUsers++;
                if (indexOtherUsers==nbCommits.length) break;
            }
            for (int i=indexOtherUsers; i<users.size(); i++) {
                System.out.println(users.get(i)+" : 0 commit");
            }
        } catch (ParseException e) {
            System.out.println("Erreur ParseException");
            return null;
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