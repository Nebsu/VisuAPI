package up.visulog.analyzer;

import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;

public class NombreModificationFichierPlugin extends getAPI {
    private String Chemin;
    private String Branche;
    private LinkedList<String> CommitsParcourus = new LinkedList<String>();

    public NombreModificationFichierPlugin(String project, String token, String adresse, String Chemin,
            String Branche,boolean tests) throws IOException {
        // Pour pouvoir utiliser le plugin il faut instancier le la classe avec : L'ID
        // du projet,Un token existant,L'adresse du site (/!\ attention du site pas du
        // projet),
        // le chemin du fichier depuis votre dossier de dépot et pour finir la branche
        // GIT d'où vous voulez partir (le tout dans cet ordre)
        // J'ai aussi ajouté un boolean que vous devez initialiser à true
        // Il y a un exemple dans le main
        super(project, token, adresse);
        while (tests &&( Branche == null || Branche.equals("") || !testBranche(corrigeHTML(Branche))))  {
            System.out.println("Veuillez réinsérer une Branche");
            Scanner scanner = new Scanner(System.in);
            Branche = scanner.next();
        }
        this.Branche = corrigeHTML(Branche);
        while (tests && (Chemin == null || !testChemin(corrigeHTML(Chemin))))  {
            System.out.println("Veuillez réinsérer un Chemin");
            Scanner scanner = new Scanner(System.in);
            Chemin = scanner.next();
        }
        this.Chemin = corrigeHTML(Chemin);
        System.out.println("file ="+Chemin);
    }

    
   
    
    public Map<String, Object> NombreModif(String idCommitDepart, LinkedList<String> parcourus) throws IOException { // Renvoie : Le nombre de fois que le fichier a été modifié
        if (parcourus != null) {
            this.CommitsParcourus = parcourus;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        int acc = 0;
        ArrayList<String[]> L = new ArrayList<String[]>();
        String idCommit;
        if (idCommitDepart == null) {
            idCommit = idLastCommit();
        } else {
            idCommit = idCommitDepart;
        }
        String idParent = "";
        while (true) {
            idParent = idCommitParent(idCommit);
            if (!CommitsParcourus.contains(idCommit)) {
                if (idParent.equals("")) {
                    break;
                }
                if (idParent.contains(",")) { // Commits a deux parents
                    String[] tabparent = idParent.split(",");
                    NombreModificationFichierPlugin temp = new NombreModificationFichierPlugin(this.Project, this.Token, this.Adresse, Chemin,"",false);
                    Map<String, Object> temporary = temp.NombreModif(tabparent[1], this.CommitsParcourus);
                    int nbrNouveau = (int) temporary.get("Nombre");
                    if (nbrNouveau != 0) {
                        acc += nbrNouveau;
                        temporary.remove("Nombre");
                        result.putAll(temporary);
                    }
                    CommitsParcourus = temp.CommitsParcourus;
                    idParent = tabparent[0];

                }
                if (fichierModif(idCommit)) {
                    acc++;
                    String[] commits = new String[2];
                    commits[0] = idCommit;
                    commits[1] = idParent;
                    L.add(commits);
                }
                CommitsParcourus.add(idCommit);
            } 
            else {
                break;
            }
            idCommit = idParent;
        }
        if (!L.isEmpty()) {
            String t = "Commits";
            int accumulator = 0;
            while (true) {
                if (accumulator != 0) {
                    t = t.substring(0, 7) + accumulator;
                }
                if (!result.containsKey(t)) {
                    break;
                }
                accumulator++;
            }
            result.put(t, L);
        }
        result.put("Nombre", acc);
        return result;
    }

    private String idCommitParent(String idCommit) throws IOException { // Renvoie l'ID du commit parent d'un commit donné
        try {
            String request = "projects/" + this.Project + "/repository/commits/" + idCommit;
            super.request(request, null);
            return lectureJson("parent");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String idLastCommit() { // Renvoie l'ID du commit le plus récent de la branche
        try {
            String request = "projects/" + super.Project + "/repository/branches/" + Branche;
            super.request(request, null);
            return lectureJson("commit");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String lectureJson(String s) { // Traite un fichier JSON pour y récupérer les information voulues
        JSONParser jsonP = new JSONParser();
        try {
            if (s.equals("diff")) {
                JSONArray differences = (JSONArray) jsonP.parse(new FileReader("request.json"));
                for (Object diff : differences) {
                    JSONObject temp = (JSONObject) diff;
                    String newP = temp.get("new_path").toString();
                    String oldP = temp.get("old_path").toString();
                    if (corrigeHTML(newP).equals(this.Chemin)) {
                        return oldP;
                    }
                }
                return "false";
            } else {
                JSONObject jsonO = (JSONObject) jsonP.parse(new FileReader("request.json"));
                if (s.equals("parent")) {
                    JSONArray array = (JSONArray) jsonO.get("parent_ids");
                    String parent = "";
                    for (int i = 0; i < array.size(); i++) {
                        parent += array.get(i);
                        if (i != array.size() - 1) {
                            parent += ",";
                        }
                    }
                    return parent;
                } else if (s.equals("commit")) {
                    JSONObject commit = (JSONObject) jsonO.get("commit");
                    Object id = commit.get("id");
                    return id.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean fichierModif(String idCommit) { // Renvoie si le fichier à été modifié entre 2 commits
        try {
            String request = "projects/" + super.Project + "/repository/commits/" + idCommit + "/diff";
            super.request(request, null);
            String diff = lectureJson("diff"); 
            if (!diff.equals("false")){
                this.Chemin = corrigeHTML(diff);
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void affiche(Map<String, Object> m) {
        Set<String> s = m.keySet();
        for (String key : s) {
            System.out.print("Clé : " + key + " Valeur : ");
            if (m.get(key) instanceof ArrayList) {
                ArrayList<String[]> ss = (ArrayList<String[]>) m.get(key);
                for (String[] tab : ss) {
                    for (int i = 0; i < tab.length; i++) {
                        if (i == 0) {
                            System.out.print("Commit ID :" + tab[i] + " ");
                        } else {
                            System.out.println("Parent ID :" + tab[i]);
                        }
                    }
                }
            } else {
                System.out.println(m.get(key).toString());
            }
        }
    }

    private boolean testBranche(String Branche) {// Vérifie que la branche existe bel et bien sur le projet
        try {
            String request = "projects/" + super.Project + "/repository/branches/" + Branche ;
            super.request(request,null);
        } catch (Exception e) {
            System.out.println("Branche non existante");
            return false;
        }
        return true;
    }
    
    public boolean testChemin(String chemin) { // Vérifie que le fichier existe bel et bien dans le répertoire
        try {
            String request = "projects/" + super.Project + "/repository/files/" + chemin;
            super.request(request,"ref="+this.Branche);
        } catch (Exception e) {
            System.out.println("Fichier non existant");
            return false;
        }
        return true;
    }

    public static String corrigeHTML(String S) { // Met les liens passés en paramètre au bon format
        String res = "";
        for (int i = 0; i < S.length(); i++) {
            if (S.charAt(i) == '/') {
                res += "%2F";
            } else {
                res += S.charAt(i);
            }
        }
        return res;
    }

    public static String CreateHtmlPage(Map<String, Object> CommitsMap) {    // convert hashmap to an HTML page
        int NombredeCommits = (int) CommitsMap.get("Nombre");
        String html = " <!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<title>Table V03</title>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<link rel=\"icon\" type=\"image/png\" href=\"images/icons/favicon.ico\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"bootstrap.min.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"font-awesome.min.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"animate.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"select2.min.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"perfect-scrollbar.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"util.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1 class=\"limiter2\" >Number of Commits : " + NombredeCommits + "</h1>\n" +

                "<div class=\"limiter\">\n" +
                "<div class=\"container-table100\">\n" +
                "<div class=\"wrap-table100\">\n" +
                "<div class=\"table100 ver5 m-b-110\">";
        String tablebegining="<table data-vertable=\"ver5\">\n" +
                "<thead>\n" +
                "<tr class=\"row100 head \">\n"+
                "<th class=\"column100 column1\" data-column=\"column1\">Commit Index</th>";
        tablebegining=tablebegining+  "<th class=\"column100 column2\" data-column=\"column2\"> Commit's ID	</th>";
        tablebegining=tablebegining+  "<th class=\"column100 column3\" data-column=\"column3\"> Commit's Parent ID </th>";
        tablebegining=tablebegining+"</tr>\n"+
                "</thead>\n"+
                "<tbody>\n";
        ArrayList<String[]> IdCouples = null;
        html=html+tablebegining;
        int index=0;
        System.out.println(CommitsMap.size());
        for (int i=0; i<CommitsMap.size()-1; i++) {
            String c = "Commits";
            if (i != 0) {  
                c+=i;
            }
            IdCouples = (ArrayList<String[]>) CommitsMap.get(c);
            System.out.println(IdCouples.size());
            for (int j=0; j<IdCouples.size(); j++ ) {
                String[] temp = IdCouples.get(j);
                index=index+1 ;
                if (index<10) {
                    html = html +"<tr class=\"row100\">\n"+
                            "<td class=\"column100 column1\" data-column=\"column1\">"+ "0"+index +"</td>\n"+
                            "<td class=\"column100 column2\" data-column=\"column2\">"+ temp[0] +"</td>\n"+
                            "<td class=\"column100 column3\" data-column=\"column3\">"+temp[1] +"</td>\n"+
                            "</tr>";
                }else {
                    html = html +"<tr class=\"row100\">\n"+
                            "<td class=\"column100 column1\" data-column=\"column1\">"+ index +"</td>\n"+
                            "<td class=\"column100 column2\" data-column=\"column2\">"+ temp[0] +"</td>\n"+
                            "<td class=\"column100 column3\" data-column=\"column3\">"+temp[1] +"</td>\n"+
                            "</tr>";
                }
            }
        }
        html=html+"</tbody>\n"+
                "</table>\n"+
                "</div>\n"+
                "</div>\n"+
                "</div>\n"+
                "</div>\n"+
                "<script src=\"jquery-3.2.1.min.js\"></script>\n"+
                "<script src=\"popper.js\"></script>\n"+
                "<script src=\"bootstrap.min.js\"></script>\n"+
                "<script src=\"select2.min.js\"></script>\n"+
                "<script src=\"main.js\"></script>\n"+
                "</body>\n"+
                "</html>\n";
        return html;
    }


}
