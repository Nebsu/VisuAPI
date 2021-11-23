package up.visulog.analyzer;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.text.Normalizer;

public class CommitsParUtilisateur extends getAPI {

    // Constructeur :
    public CommitsParUtilisateur(int project, String token, String adresse) {
        super(project, token, adresse);
    }

    // Fonction auxiliaire qui va normaliser une chaine de caractères, c'est-à-dire
    // qu'elle d'abord mettre toutes les lettres de la chaine en majuscule
    // et qui va ensuite enlever ces accents (crée pour les noms propres)
    private static String normalize(String source) {
        String res = "";
        for (int i=0; i<source.length(); i++) {
            res += String.valueOf(Character.toUpperCase(source.charAt(i)));
        }
		return Normalizer.normalize(res, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}

    // Fonction auxiliaire qui va diviser en deux mots, les chaines de caractères contenant un espace
    // exemple : pour source = "Chapeau Melon", divide renvoie un tableau de String contenant deux éléments:
    // "Chapeau" et "Melon"
    // Si la source ne contient pas d'espace (donc un seul mot) ou si elle contient plusieurs espaces (donc plus de deux mots), 
    // alors on revoit null, car la source doit obligatoirement contenir un seul espace et exactement deux mots.
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

    // Fonction auxiliaire qui va inversers les deux mots d'une chaines de caractères contenant exactement un espace
    // exemple : pour source = "Chapeau Melon", alors inverserMots renvoie la chaine : "Melon Chapeau".
    // Si la source ne contient pas d'espace (donc un seul mot) ou si elle contient plusieurs espaces (donc plus de deux mots), 
    // alors on revoit null, car la source doit obligatoirement contenir un seul espace et exactement deux mots.
    private static String inverserMots(String source) {
        String m1 = "", m2 = "";
        int acc = 0;
        for (int i=0; i<source.length(); i++) {
            char c = source.charAt(i);
            if (c==' ') break;
            m1 += String.valueOf(c);
            acc++;
        }
        for (int i=acc+1; i<source.length(); i++) {
            char c = source.charAt(i);
            m2 += String.valueOf(c);
        }
        return (m2+" "+m1);
    }

    // Fonction principale qui renvoie et affiche le nombre de commits par utilisateur
    public Map<String, Object> recupererCommits() throws IOException {
        // Renvoie une HashMap qui est un fait un array de Integer avec comme clé (index) le nom de l'utilisateur
        // Par exemple map[THEAU NICOLAS] = 12 (12 est le nombre commits)
        int page = 1; // accumulateur de pages
        int nbCommits = 0; // accumulateur de commits par page
        Map<String, Object> map = new HashMap<String, Object>(); // map principale
        Set<String> users = new HashSet<String>(); // liste des auteurs des commits
        do {
            // Request API :
            try {
                // On utilise la méthode de getAPI.java :
                request("projects/"+String.valueOf(this.Project)+
                "/repository/commits", "all=true&per_page=50000&page="+String.valueOf(page));
            } catch (Exception e) {
                System.out.println("Erreur commits");
                return null;
            }
            page++;
            // Lecture du fichier JSON
            JSONParser jsParser = new JSONParser();
            try {
                JSONArray commitArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
                // Examination de chaque commit :
                for (Object commit : commitArray) {
                    JSONObject temp = (JSONObject) commit; // commit courant
                    String name = temp.get("author_name").toString(); // récupération du nom de l'auteur du commit
                    name = CommitsParUtilisateur.normalize(name); // normalisation du nom
                    if (users.contains(name) || users.contains(inverserMots(name))) { // cas où l'auteur est connu
                        // Cas où le nom est inversé (bug gitlab de seb) :
                        if (users.contains(inverserMots(name))) {
                            name = inverserMots(name);
                        }
                        // Accumulation de commits en fonction du nom de l'auteur :
                        Integer acc = (Integer) map.get(name);
                        acc += Integer.valueOf(1);
                        // MAJ de l'accumulateur :
                        map.put(name, acc); 
                    } else {
                        // Cas où on détecte un nouvel utilisateur
                        users.add(name);
                        // On ajoute l'utilisateur, ainsi que son premier commit :
                        map.put(name, Integer.valueOf(1)); 
                    }
                    nbCommits++;
                }
            } catch (ParseException e) {
                System.out.println("Erreur ParseException");
                return null;
            }
        // 50000 => limite arbitraire pour le nombre de commit maximum dans le fichier json
        // s'il y a plus de 50000 commits dans le projet, on répète le programme tant qu'on a pas examiné tous les commits
        // affichage du nombre de commit par utilisateur
        } while (nbCommits>=50000);
        return map; 
    }

    // Tests :
    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        p.recupererCommits();
    }

}