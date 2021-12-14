package up.visulog.analyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
    
    public Map<String, Object> getNombresLigneUtilisateur() throws IOException, ParseException, InterruptedException {
        Map<String, Object> res = new HashMap<String, Object>();
        Set<String> users = new HashSet<String>();
        int[] total = new int[2];
        boolean requestIsNotEmpty = true;
        int nbr = 1;
        int taille = 0;
        while (requestIsNotEmpty) {
            LinkedList<Thread> tl = new LinkedList<Thread>();
            for (int i = 1 + (nbr - 1) * 100; i <= nbr * 100; i++) {
                Thread t = new Thread("" + i) {
                    public void run() {
                        try {
                            request2("projects/" + Project + "/repository/commits", principal? "with_stats=true&per_page=500&page=" : "all=true&with_stats=true&per_page=500&page=", getName());
                        } catch (IOException e) {
                            try {
                                Thread.sleep(1000);
                                request2("projects/" + Project + "/repository/commits", principal? "with_stats=true&per_page=500&page=" : "all=true&with_stats=true&per_page=500&page=", getName());
                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                        //System.out.println(getName());
                    }
                };
                t.start();
                tl.add(t);
            }
            Thread.sleep(10000);
            boolean b = true;
            System.out.println("test thread" + tl.size());
            while(b) {
                b = false;
                Thread.sleep(2000);
                System.out.println("yo");
                for (Thread t : tl) {
                    File f = new File(".gen/request" + t.getName() + ".json");
                    System.out.println(".gen/request" + t.getName() + ".json");
                    if(!f.exists()) {
                        b = true;
                    }
                }
            }
            JSONParser jsonP = new JSONParser();
            System.out.println(".gen/request" + nbr * 100 + ".json");
            try {
                JSONArray commits = (JSONArray) jsonP.parse(new FileReader(".gen/request" + nbr * 100 + ".json"));
                if(commits.size() == 0) {
                    requestIsNotEmpty = false;
                }
            }
            catch (Exception e) {
                Thread.sleep(10000);
                JSONArray commits = (JSONArray) jsonP.parse(new FileReader(".gen/request" + nbr * 100 + ".json"));
                if(commits.size() == 0) {
                    requestIsNotEmpty = false;
                }
            }
            nbr++;
        }
            System.out.println("finish");
            System.out.println(taille);
            for (int i = 1; i < nbr * 100; i++) {
                JSONParser jsonP = new JSONParser();
                JSONArray commits = (JSONArray) jsonP.parse(new FileReader(".gen/request" + i + ".json"));
                if(commits.size() == 0) {
                    break;
                }
                for (Object commit : commits) {
                    JSONObject objectTemp1 = (JSONObject) commit;
                    JSONObject objectTemp2 = (JSONObject) objectTemp1.get("stats");
                    JSONArray objectTemp3 = (JSONArray) objectTemp1.get("parent_ids");
                    String mail = (String) objectTemp1.get("committer_email");
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
            }
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
        
         // NOMBRE DE LIGNES EDITEES AU TOTAL
        int[] total = (int[]) map.get("total");
        int res = total[0] - total[1];
        html.append("<div class='statEdit'><h2>Statistiques d'edition globales</h2><ul> <div class='statEditMembres'>");
        html.append("<div class='editMembres'><li><div class='infoMembres'><img src='https://secure.gravatar.com/avatar/3add01f9be15323a4875cb4cde08bbb3?s'>")
            .append("<div class='name'>")
            .append("<strong>Projet</strong>").append("</div></div><div class='allCommits'>") // Nom du membre
            .append("<div class='commit'><div class='plus'> + </div><strong> ").append(total[0]).append("</strong></div>") //Nombre de lignes ajoutées
            .append("<div class='commit'><div class='moins'> - </div><strong> ").append(total[1]).append("</strong></div>") // Nombre de lignes supprimées
            .append("<div class='commit'><div class='egal'> = </div><strong> ").append(total[0]-total[1]).append("</strong></div>") // Nombre de lignes ajoutées - supprimées
            .append("</div></li><br></div>");
        html.append("</div></ul>");
         //PLUGIN
        html.append("<h2>Statistiques d'edition par membres</h2> <ul> <div class='statEditMembres'>");
        for (String string : user) {
            int[] tab =(int[]) map.get(string);
            //System.out.println(string + "     additions : " + tab[0] + "    deletions : " + tab[1]);
            //html.append("<div class='editMembres'><li><div class='infoMembres'><img src="+ mailToImg((map.get(string+"mail")).toString()) +">")
            html.append("<div class='editMembres'><li><div class='infoMembres'><img src=https://secure.gravatar.com/avatar/3add01f9be15323a4875cb4cde08bbb3?s>")
            .append("<div class='name'>")
            .append(string).append("</div></div><div class='allCommits'>") // Nom du membre
            .append("<div class='commit'><div class='plus'> + </div> ").append(tab[0]).append("</div>") //Nombre de lignes ajoutées
            .append("<div class='commit'><div class='moins'> - </div> ").append(tab[1]).append("</div>") // Nombre de lignes supprimées
            .append("<div class='commit'><div class='egal'> = </div> ").append(tab[0]-tab[1]).append("</div>") // Nombre de lignes ajoutées - supprimées
            .append("</div></li><br></div>");
        }
        html.append("</div></ul>").append("</div>");
        return html.toString();
    }

/**************
 * 
 * REMETTRE LA LIGNE 174
 * 
***/
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("278964", null, null,true);
        // NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("278964","glpat-v5gGaWWxz_uXdK4MkY8K",null);
        CreatePage c = new CreatePage();
        c.creer(n2.afficheHTML(n2.getNombresLigneUtilisateur()));
        c.ouvrirPage();
        Thread.sleep(2000);
        Process p = Runtime.getRuntime().exec("clear.bat");
    }
    
}
