package up.visulog.analyzer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.text.Normalizer;
import java.util.Random;
import java.awt.Desktop;

public class CommitsParUtilisateur extends getAPI {

    private Map<String, Object> result;

    // Constructeur :
    public CommitsParUtilisateur(String project, String token, String adresse) 
    throws IOException {
        super(project, token, adresse);
        this.result = this.recupererCommits();
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

    // Fonction auxiliaire pour afficher le nombre de commits par utilisateur dans le terminal
    private static void afficher(Map<String, Object> m) {
        for (var item : m.entrySet()) {
            System.out.println(item.getKey() + " : " + item.getValue()+" commit(s)");
        }
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
            nbCommits = 0;
            // Request API :
            try {
                // On utilise la méthode de getAPI.java :
                request("projects/"+String.valueOf(this.Project)+
                "/repository/commits", "all=true&per_page=1000&page="+String.valueOf(page));
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
        } while (nbCommits>=1000);
        // 1000 => limite arbitraire pour le nombre de commit maximum dans le fichier json
        // s'il y a plus de 1000 commits dans le projet, on répète le programme tant qu'on a pas examiné tous les commits
        // affichage du nombre de commit par utilisateur
        System.out.println();
        afficher(map);
        return map; 
    }

    ///////////////////////////TEMPORAIRE///////////////////////////////////////
    public void creerCss(String s) throws IOException {
        File f = new File("pie.css");
        FileOutputStream fos = new FileOutputStream("pie.css");
        if(f.exists()) {
            f.delete();
        }
        fos.write(s.getBytes());
        fos.flush();
        fos.close();
    }

    public void creer(String s) throws IOException {
        File f = new File("example.html");
        FileOutputStream fos = new FileOutputStream("example.html");
        if(f.exists()) {
            f.delete();
        }
        fos.write(s.getBytes());
        fos.flush();
        fos.close();
    }

    public void ouvrirPage() throws IOException {
        File f = new File("example.html");
        if(!Desktop.isDesktopSupported()){
            System.out.println("Desktop n'est pas prise en charge");
            return;
        }
        Desktop d = Desktop.getDesktop();
        d.open(f);
    }
    //////////////////////////////////////////////////////////////////////////////

    //Creation du code html
    public String toHTML(){
        String s = "<html><body><h1>Nombre de Commits par Utilisateur</h1>";
        for(var item : result.entrySet()){
            s += "<li>"+item.getKey()+": "+item.getValue()+"</li>";
        }
        return s + "</body></html>";
    }

    public double[] getCommitsPercentile(){
        double[] res = new double[result.size()];
        int total = 0;
        int i = 0;
        for(var item : result.entrySet()){
            total += (int)item.getValue();
        }
        for(var item : result.entrySet()){
            res[i] = (double)(int)item.getValue()*100/total;
            i=i+1;
        }      
        return res;
    }

    public String colorGenerator(){
        Random obj = new Random();
        int rand_num = obj.nextInt(0xffffff + 1);
        String colorCode = String.format("#%06x", rand_num);
        return colorCode;
    }

    public String createHTMLChart() {
        StringBuilder html = new StringBuilder("<html><link rel=\"stylesheet\" href=\"pie.css\"><body><h1>Commit Proportion Pie Chart</h1><div id=\"my-pie-chart-container\"><div id=\"my-pie-chart\"></div><div id=\"legenda\">");
        int i = 0;
        for (var item : result.entrySet()) {
            i = i+1;
            String divId = "color-" + i;
            html.append("<div class=\"entry\">").append("<div id=\"").append(divId).append("\" class=\"entry-color\"></div>").append("<div class=\"entry-text\">").append(item.getKey()+" ").append(item.getValue()).append("</div></div>");
        }
        html.append("</div></div></body></html>");
        return html.toString();
    }

    public String cssGenerator(){
        StringBuilder css = new StringBuilder("body {background-color: white;display: flex;justify-content: center;align-items: center;flex-direction: column;}#my-pie-chart-container {display: flex;align-items: center;}  #my-pie-chart {background: conic-gradient(");
        double[]tab = getCommitsPercentile();
        String[]colorMem = new String[tab.length];
        int mem = 0;
        int percentile = 0;
        for(int j = 0; j < colorMem.length; j++){
            colorMem[j] = colorGenerator();
        }
        for(int i = 0; i < tab.length; i++){
            percentile += tab[i];
            css.append(colorMem[i] + " " + mem + "%" + " " + percentile + "%");
            if(i != tab.length - 1){
                css.append(", ");
            }
            mem = percentile;
        }
        css.append(");border-radius: 50%;width: 150px;height: 150px;}#legenda {margin-left: 20px;background-color: white;padding: 5px;}.entry {display: flex;align-items: center;}.entry-color {height: 10px;width: 10px;}.entry-text {margin-left: 5px;}");
        int i = 0;
        for(int j = 0; j < colorMem.length; j++){
            i = i+1;
            css.append("#color-"+i).append("{background-color:").append(colorMem[j]+ ";}");
        }
        return css.toString();
    }

    public void afficherGraphique() throws IOException {
        this.creerCss(this.cssGenerator());
        this.creer(this.createHTMLChart());
        this.ouvrirPage();
    }

    // Tests :
    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur("3389", "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        p.afficherGraphique();
        CommitsParUtilisateur p2 = new CommitsParUtilisateur("3390", null, 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        p2.afficherGraphique();
        // CommitsParUtilisateur p3 = new CommitsParUtilisateur("2335175", null, null);
        // p3.recupererCommits();
    }

}