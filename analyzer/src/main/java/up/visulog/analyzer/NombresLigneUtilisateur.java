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

/****************************************************************
UTILISATION :

- Creation d'un Object NombresLigneUtilisateur avec en argument(ID projet, Token si existant sinon null, Adresse de l'herbegement du projet, False pour tout les commits ou True pour les commits de la branche principale).
- Appel de la fonction getNombresLigneUtilisateu() qui renvoi une Map<String, Object>:
    - Avec une clé "users" un liste de tout les utilisateurs qui on commit dans le projet.
    - Des clés qui seront les nom d'utilisateur qui va renvoyer un tableau d'int avec t[0] = addition et t[1] = deletions.
    - Avec une clé "total" qui renvoi un tableau d'int avec t[0] = nombre de lignes ajouté et t[1] = nombre de ligne supprimé.
- Affiche permet d'afficher la la map en entier dans le terminal.

****************************************************************/

public class NombresLigneUtilisateur extends getAPI {
    private boolean principal;

    public NombresLigneUtilisateur(String project, String token, String adresse,boolean principal) {
        super(project, token, adresse);
        this.principal = principal;
    }
    
    public Map<String, Object> getNombresLigneUtilisateur() throws IOException, ParseException {
        Map<String, Object> res = new HashMap<String, Object>();
        Set<String> users = new HashSet<String>();
        int page = 1;
        int size = 0;
        int[] total = new int[2];
        do {
            request("projects/" + this.Project + "/repository/commits", principal? "with_stats=true&per_page=750&page=" + page : "all=true&with_stats=true&per_page=750&page=" + page);
            JSONParser jsonP = new JSONParser();
            JSONArray commits = (JSONArray) jsonP.parse(new FileReader("request.json"));
            if(commits.size() == 0) {
                break;
            }
            size += commits.size();
            for (Object commit : commits) {
                JSONObject objectTemp1 = (JSONObject) commit;
                JSONObject objectTemp2 = (JSONObject) objectTemp1.get("stats");
                JSONArray objectTemp3 = (JSONArray) objectTemp1.get("parent_ids");
                String mail = (String) objectTemp1.get("author_email");
                int tailleParent = (int) objectTemp3.size();
                if(tailleParent == 1) {
                    Object temp2 = objectTemp2.get("additions");
                    Object temp3 = objectTemp2.get("deletions");
                    String t = objectTemp1.get("author_name").toString();
                    int[] ajouter = new int[2];
                    if(users.contains(t)) {
                        int[] temporaire = ((int[]) res.get(t));
                        ajouter[0] = temporaire[0] + ((Long) temp2).intValue();
                        ajouter[1] = temporaire[1] + ((Long) temp3).intValue();
                    }
                    else {
                        users.add(t);
                        res.put(t + "mail", mail);                  
                        ajouter[0] = ((Long) temp2).intValue();
                        ajouter[1] = ((Long) temp3).intValue();
                    }
                    res.put(t, ajouter);
                    total[0] += ((Long) temp2).intValue();
                    total[1] += ((Long) temp3).intValue();
                }
            }
            page++;
        } while(size%750 == 0);
        res.put("users",users);
        res.put("total",total);
        return res;
    }

    public void affiche(Map<String, Object> map) throws IOException, ParseException {
        Set<String> user = (Set<String>) map.get("users");
        for (String string : user) {
            int[] tab =(int[]) map.get(string);
            System.out.println(string + "     additions : " + tab[0] + "    deletions : " + tab[1]);
            System.out.println(this.mailToImg(map.get(string + "mail").toString()));
        }
        int[] total = (int[]) map.get("total");
        int res = total[0] - total[1];
        System.out.println("Nombres de lignes du projet : " + res);
    }

    public String afficheHTML(Map<String, Object> map) throws IOException, ParseException {
        Set<String> user = (Set<String>) map.get("users");
        StringBuilder html = new StringBuilder("<html><meta charset='utf-8'/><link rel='stylesheet' type='text/css' href='test.css'><body>");
         // ACCUEIL AVEC TITRE
         html.append("<div class='title'><h1> Statistiques du projet : X </h1> <br> via Gitlab <div class='img'><img src='https://about.gitlab.com/images/press/logo/png/gitlab-icon-rgb.png' width='50' height='50'></div></div>");
        
         //PLUGIN
        html.append("<div class='statEdit'><h2>Statistiques d'edition par membres</h2> <ul> <div class='statEditMembres'>");
        for (String string : user) {
            int[] tab =(int[]) map.get(string);
            //System.out.println(string + "     additions : " + tab[0] + "    deletions : " + tab[1]);
            html.append("<div class='editMembres'><li><div class='infoMembres'><img src="+ mailToImg((map.get(string+"mail")).toString()) +">")
            .append("<div class='name'>")
            .append(string).append("</div></div>") // Nom du membre
            .append("<div class='commit'> + : ").append(tab[0]).append("</div>") //Nombre de lignes ajoutées
            .append("<div class='commit'> - : ").append(tab[1]).append("</div>") // Nombre de lignes supprimées
            .append("<div class='commit'> = : ").append(tab[0]-tab[1]).append("</div>") // Nombre de lignes ajoutées - supprimées
            .append("</li><br></div>");
        }
        html.append("</div>");
        int[] total = (int[]) map.get("total");
        int res = total[0] - total[1];
        html.append("<p>"+res+"<p>");
        html.append("</div>");
        System.out.println("Nombres de lignes du projet : " + res);
        return html.toString();
    }


    public static void main(String[] args) throws IOException, ParseException {
        NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("3389", "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr",true);
        //NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("278964","glpat-v5gGaWWxz_uXdK4MkY8K",null);
        
        CreatePage c = new CreatePage();
        c.creer(n2.afficheHTML(n2.getNombresLigneUtilisateur()));
        c.ouvrirPage();
        
    }
    
}
