package up.visulog.analyzer;

import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// TODO A retirer quand on aura modifPagz
import java.io.*;

public class NombreModificationFichierPlugin extends getAPI {
    private String Chemin;
    private String Branche;

    // L'adresse c'est sans api/v4 et sans l'id projet
    public NombreModificationFichierPlugin(String project, String token, String adresse, String Chemin, String Branche) {
        // Pour pouvoir utiliser le plugin il faut instancier le la classe avec : L'ID du projet,Un token existant,L'adresse du site (/!\ attention du site pas du projet),
        // le chemin du fichier depuis votre dossier de dépot et pour finir la branche GIT d'où vous voulez partir (le tout dans cet ordre)
        // Il y a un exemple dans le main
        super(project, token, adresse);
        this.Chemin = Chemin;
        this.Branche = corrigeHTML(Branche);
    }

    public Map<String, Object> NombreModif() throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        int acc = 0;
        ArrayList<String[]> L = new ArrayList<String[]>();
        String idCommit = idLastCommit();
        String idParent = "";
        while (true) {
            idParent = idCommitParent(idCommit);
            if (idParent.equals("")) {
                break;
            }
            if (idParent.contains(",")) {// Commits a deux parents
                // // TODO : Add dans la liste L le fait que y'ait fusion
                String[] tabparent = idParent.split(",");
                idParent = tabparent[0];

            }
            if (fichierModif(idCommit)) {
                acc++;
                System.out.println("Fichier modifié " + acc + " fois");
                String[] commits = new String[2];
                commits[0] = idCommit;
                commits[1] = idParent;
                System.out.println(" ID commit :" + commits[0]);
                System.out.println(" ID parent :" + commits[1]);
                L.add(commits);
            }
            idCommit = idParent;
        }
        result.put("Nombre", acc);
        result.put("Commits", L);
        affiche(result);
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
                    if (oldP.equals(this.Chemin) || newP.equals(this.Chemin)) {
                        return "true";
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public boolean fichierModif(String idCommit) {
        try {
            String request = "projects/" + super.Project + "/repository/commits/" + idCommit + "/diff";
            super.request(request, null);
            return lectureJson("diff").equals("true");

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

    public void CreateHtmlPage() throws IOException {
        Map<String, Object> CommitsMap = NombreModif();
        int NombredeCommits = (int) CommitsMap.get("Nombre");
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "   <head>\n" +
                "      <style>\n" +
                "td {\n" +
                "  text-align: center;\n" +
                "}\n" +
                "table {\nwidth:50% ;\n" +
                "}\n" +
                "  th, td {\n" +
                "            border: 1px solid black;\n" +
                "            height: 25px;\n" +
                "         }\n" +
                "      </style>\n" +
                "   </head>\n" +
                "\n" +
                "   <body>\n" +
                "   \n" +
                "      <h1>Number of Commits " + NombredeCommits + "</h1>\n" +
                "      <h2>" + "</h2>\n" +
                "\n" +
                "      <table style=\"width:100%;text-align:center;border-collapse:collapse;background-color:gold;\">\n" +
                "         <tr style=\"background-color:#00FF00\">\n" +
                "            <th colspan=\"2\">Table of Commits</th>\n" +
                "         </tr>\n" +
                "         <tr style=\"background-color:#00FF00\" >\n" +
                "            <td>Commit's ID </td>\n" +
                "            <td rowspan=\"1\">Commit's Parent ID</td>\n" +
                "         </tr>\n";

        ArrayList<String[]> IdCouples = (ArrayList<String[]>) CommitsMap.get("Commits");

        for (int i = 0; i < IdCouples.size(); i++) {
            String[] temp = IdCouples.get(i);
            html = html + "<tr style=\"background-color:yellowgreen;color:white;\"><td>" + temp[0] + "</td>";
            html = html + "<td>" + temp[1] + "</td></tr>";
        }
        html = html + "    </table>\n" +
                "   </body>\n" +
                "</html>";
        File f = new File("./commits.html");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(html);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String CreateHtmlPageheader(int NombredeCommits) {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "   <head>\n" +
                "      <style>\n" +
                "td {\n" +
                "  text-align: center;\n" +
                "}\n" +
                "table {\nwidth:50% ;\n" +
                "}\n" +
                "  th, td {\n" +
                "            border: 1px solid black;\n" +
                "            height: 25px;\n" +
                "         }\n" +
                "      </style>\n" +
                "   </head>\n" +
                "\n" +
                "   <body>\n" +
                "   \n" +
                "      <h1>Number of Commits " + NombredeCommits + "</h1>\n" +
                "      <h2>" + "</h2>\n" +
                "\n" +
                "      <table style=\"width:100%;text-align:center;border-collapse:collapse;background-color:gold;\">\n" +
                "         <tr style=\"background-color:#00FF00\">\n" +
                "            <th colspan=\"2\">Table of Commits</th>\n" +
                "         </tr>\n" +
                "         <tr style=\"background-color:#00FF00\" >\n" +
                "            <td>Commit's ID </td>\n" +
                "            <td rowspan=\"1\">Commit's Parent ID</td>\n" +
                "         </tr>\n";

        return html;
    }

    public static void CreateHtmlPageNewversion(Map<String, Object> CommitsMap) {// nouvelle version pour creation page html mais avec plusieurs tableaux des ids pour les  commits
// en cours de changer le syntaxe de la fonction
        int NombredeCommits = (int) CommitsMap.get("Nombre");
        String html = CreateHtmlPageheader(NombredeCommits);


        for (Map.Entry<String, Object> entry : CommitsMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String[]> IdCouples = entry.getValue();


            ArrayList<String[]> IdCouples = (ArrayList<String[]>) CommitsMap.get("Commits");

            for (int i = 0; i < IdCouples.size(); i++) {
                String[] temp = IdCouples.get(i);
                html = html + "<tr style=\"background-color:yellowgreen;color:white;\"><td>" + temp[0] + "</td>";
                html = html + "<td>" + temp[1] + "</td></tr>";
            }
        }
        html = html + "    </table>\n" +
                "   </body>\n" +
                "</html>";
        File f = new File("./commits.html");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(html);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
