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

    // fonction qui va d'abord mettre toutes les lettres d'une chaine de caractères en majuscule
    // et qui va ensuite enlever les accents
    private static String normalize(String source) {
        String res = "";
        for (int i=0; i<source.length(); i++) {
            res += String.valueOf(Character.toUpperCase(source.charAt(i)));
        }
		return Normalizer.normalize(res, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}

    // fonction qui va diviser en deux mots, les chaines de caractères contenant un espace
    // exemple : pour source = "Chapeau Melon", divide renvoie un tableau de String contenant deux éléments:
    // "Chapeau" et "Melon"
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

    // fonction qui va trouver des similarités entre un mot et un des éléments d'une liste de String
    // exemple : similarty renvoie true si : n = "Michael Jordan" et si list contient une chaine comme "Jordan Michael"
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

    // fonction qui récupère tous les membres inscrits sur le projet gitlab
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

    // fonction qui renvoie et affiche le nombre de commits par utilisateur
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
                // Première lecture du fichier JSON, pour examiner l'auteur de chaque commit
                for (Object user : commitArray) {
                    JSONObject temp = (JSONObject) user;
                    String name = temp.get("author_name").toString();
                    name = CommitsParUtilisateur.normalize(name);
                    if (!users.contains(name)) {
                        // on ajoute seulement les utilisateurs qu'on a pas déjà ajouté
                        users.add(name);
                    }
                    acc2++;
                }
                // on sait désormais le nombre total d'utilisateurs, donc la taille du tableau final
                nbCommits = new int[users.size()];
                // Deuxieme lecture du tableau pour calculer le nombre de commit par auteur
                for (Object user : commitArray) {
                    JSONObject temp = (JSONObject) user;
                    String name = temp.get("author_name").toString();
                    name = CommitsParUtilisateur.normalize(name);
                    if (users.contains(name)) {
                        nbCommits[users.indexOf(name)]++; // accumulation de commit en fonction du nom de l'auteur
                    }
                }
            } catch (ParseException e) {
                System.out.println("Erreur ParseException");
                return null;
            }
        } while (acc2>=50000); // 50000 => limite arbitraire pour le nombre de commit maximum dans le fichier json
        // s'il y a plus de 50000 commits dans le projet, on répète le programme tant qu'on a pas examiné tous les commits
        try {
            // Determination des membres du projet qui sont inscrits, MAIS qui n'ont pas fait de commit
            LinkedList<String> getOtherUsers = recupererMembres();
            for (String user : getOtherUsers) {
                if (!users.contains(user) && !CommitsParUtilisateur.similarity(users, user)) {
                    users.add(user);
                }
            }
            int indexOtherUsers = 0;
            // affichage du nombre de commit par utilisateur
            for (String user : users) {
                System.out.println(user+" : "+nbCommits[users.indexOf(user)]+" commit(s)");
                indexOtherUsers++;
                if (indexOtherUsers==nbCommits.length) break;
            }
            // affichage des membres qui n'ont pas fait de commit, s'il y en a :
            for (int i=indexOtherUsers; i<users.size(); i++) {
                System.out.println(users.get(i)+" : 0 commit");
            }
        } catch (ParseException e) {
            System.out.println("Erreur ParseException");
            return null;
        }
        return nbCommits; 
        // on revoit un tableau de int (pour le moment)
    }

    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        // CommitsParUtilisateur p2 = new CommitsParUtilisateur(278964, null, "https://gitlab.com");
        p.recupererCommits();
        // p2.recupererCommits();
    }

}