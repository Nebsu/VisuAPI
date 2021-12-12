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

    public static void CreateHtmlPage(Map<String, Object> CommitsMap) {    // convert hashmap to an HTML page
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

        for (int i=0; i<CommitsMap.size()-1; i++) {
            IdCouples = (ArrayList<String[]>) CommitsMap.get("Commit"+i);
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

        //  String tableEnding= "</table>\n";
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
