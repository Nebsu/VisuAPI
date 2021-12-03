package up.visulog.analyzer;

import java.io.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.text.Normalizer;
import java.awt.Desktop;

public class CommitHistory extends getAPI {

    private ArrayList<Commit> commitsList;

    // Constructeur :
    public CommitHistory(String project, String token, String adresse) 
    throws IOException {
        super(project, token, adresse);
        this.commitsList = this.commitHistory();
    }

    // Fonction auxiliaire qui va normaliser une chaine de caractères, c'est-à-dire
    // qu'elle va enlever ses accents :
    private static String normalize(String source) {
        source.replace("A©", "é");
		return source;
	}

    // Fonction auxiliaire qui affiche l'historique des commits d'un projet (du plus récent au plus ancien) :
    // le tout dans le terminal :
    public static void displayCommits(ArrayList<Commit> commits) {
        for (Commit com : commits) {
            System.out.println(com);
        }
    }

    // Fonction principale qui renvoie l'historique des commits d'un projet Gitlab :
    public ArrayList<Commit> commitHistory() throws IOException {
        // Renvoie une liste chainée de Commit
        // On crée l'objet Commit dans Commit.java
        // Chaque commit contient plusieurs informations (tel que son titre, son auteur, ...)
        int nbCommits = 0; // accumulateur de commits par page
        int page = 0; // accumulateur de pages
        ArrayList<Commit> commits = new ArrayList<Commit>(); // initialisation (liste vide)
        do {
            nbCommits = 0;
            // Request API :
            // On récupère dans un premier temps les commits dans l'ordre chronologique (du plus ancien au plus récent) :
            try {
                // On utilise la méthode de getAPI.java :
                request("projects/"+String.valueOf(this.Project)+
                "/repository/commits", "all=true&per_page=1000&page="+String.valueOf(page));
            } catch (Exception e) {
                System.out.println("Erreur commits");
                return null;
            }
            // Lecture du fichier JSON
            JSONParser jsParser = new JSONParser();
            try {
                JSONArray commitArray = (JSONArray) jsParser.parse(new FileReader("request.json"));
                // Examination de chaque commit :
                for (Object commit : commitArray) {
                    JSONObject temp = (JSONObject) commit; // commit courant
                    String title = temp.get("title").toString(); // récupération du titre/message du commit
                    String date = temp.get("committed_date").toString(); // récupération de la date du commit
                    String author = temp.get("author_name").toString(); // récupération du nom de l'auteur du commit
                    author = normalize(author); // normalisation du nom
                    title = normalize(title); // normalisation du titre
                    date = date.substring(0, 10); // simplification de la date
                    Commit com = new Commit(title, date, author); // On construit un nouveau Commit
                    commits.add(com); // on l'ajoute à la liste chainée
                    nbCommits++;
                }
            } catch (ParseException e) {
                System.out.println("Erreur ParseException");
                return null;
            }
        } while (nbCommits>=1000);
        // 1000 => limite arbitraire pour le nombre de commit maximum dans le fichier json
        // s'il y a plus de 1000 commits dans le projet, on répète le programme tant qu'on a pas examiné tous les commits
        // Une fois tous les commits récuprés dans notre liste chainée, il suffit d'inverser l'ordre des éléments de la liste,
        // pour pouvoir obtenir la liste des commits dans l'ordre chronologique inversé (du plus récent au plus ancien).
        // Pour cela, on utlise la méthode reverse de la classe Collections (Classe mère des listes dans l'api java) :
        Collections.reverse(commits);
        return commits; 
    }

    public String toHTML(){
        StringBuilder html = new StringBuilder();
        html.append("<html><link rel='stylesheet' type='text/css' href='test.css'><body><h1>Historique des commits</h1>");
        int n = 1;
        for(Commit commit : commitsList){
            html.append("<ul>").append(n++ + ". ").append(commit).append("</ul>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    ////////////////////////////////////////////CREER PAGE////////////////////////////////////////////////
    public void creer(String s) throws IOException {
        File f = new File("historique.html");
        FileOutputStream fos = new FileOutputStream("historique.html");
        fos.write(s.getBytes());
        fos.flush();
        fos.close();
    }

    public void ouvrirPage() throws IOException {
        File f = new File("historique.html");
        if(!Desktop.isDesktopSupported()){
            System.out.println("Desktop n'est pas prise en charge");
            return;
        }
        Desktop d = Desktop.getDesktop();
        d.open(f);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests :
    public static void main(String[] args) throws IOException {
        CommitHistory cm1 = new CommitHistory("3389", "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        cm1.commitsList = cm1.commitHistory();
        String html = cm1.toHTML();
        cm1.creer(html);
        cm1.ouvrirPage();
        // CommitsParUtilisateur p2 = new CommitsParUtilisateur("3390", null, 
        // "https://gaufre.informatique.univ-paris-diderot.fr");
        // CommitsParUtilisateur p3 = new CommitsParUtilisateur("2335175", null, null);
    }

}