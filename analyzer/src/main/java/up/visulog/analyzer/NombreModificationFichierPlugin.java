package up.visulog.analyzer;

import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


// TODO A retirer quand on aura modifPagz
import java.io.*;

public class NombreModificationFichierPlugin extends getAPI {
    private String Chemin;
    private String Branche;
    private LinkedList<String> CommitsParcourus = new LinkedList<String>();

    // L'adresse c'est sans api/v4 et sans l'id projet
    public NombreModificationFichierPlugin(String project, String token, String adresse, String Chemin,
            String Branche,boolean tests) {
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
    }

    
   
    
    public Map<String, Object> NombreModif(String idCommitDepart, LinkedList<String> parcourus) throws IOException {
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
                if (idParent.contains(",")) {// Commits a deux parents
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

    private String idCommitParent(String idCommit) throws IOException { // Complété
        try {
            String request = "projects/" + this.Project + "/repository/commits/" + idCommit;
            super.request(request, null);
            return lectureJson("parent");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String idLastCommit() { // Complété
        try {
            String request = "projects/" + super.Project + "/repository/branches/" + Branche;
            super.request(request, null);
            return lectureJson("commit");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String lectureJson(String s) {
        JSONParser jsonP = new JSONParser();
        try {
            if (s.equals("diff")) {
                JSONArray differences = (JSONArray) jsonP.parse(new FileReader("request.json"));
                for (Object diff : differences) {
                    JSONObject temp = (JSONObject) diff;
                    String newP = temp.get("new_path").toString();
                    String oldP = temp.get("old_path").toString();
                    if (newP.equals(this.Chemin)) {
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

    public boolean fichierModif(String idCommit) {
        try {
            String request = "projects/" + super.Project + "/repository/commits/" + idCommit + "/diff";
            super.request(request, null);
            String diff = lectureJson("diff"); 
            if (!diff.equals("false")){
                this.Chemin = diff;
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean testBranche(String Branche) {
        try {
            String request = "projects/" + super.Project + "/repository/branches/" + Branche ;
            super.request(request,null);
        } catch (Exception e) {
            System.out.println("Branche non existante");
            return false;
        }
        return true;
    }
    
    public boolean testChemin(String chemin) {
        try {
            String request = "projects/" + super.Project + "/repository/files/" + chemin;
            super.request(request,"ref="+this.Branche);
        } catch (Exception e) {
            System.out.println("Fichier non existant");
            return false;
        }
        return true;
    }

    public static String corrigeHTML(String S) {
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

    public LinkedList<String> getCommitsParcourus() {
        return CommitsParcourus;
    }

}
