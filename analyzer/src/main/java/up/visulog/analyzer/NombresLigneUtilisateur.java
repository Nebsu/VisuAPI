package up.visulog.analyzer;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NombresLigneUtilisateur extends getAPI {

    public NombresLigneUtilisateur(String project, String token, String adresse) {
        super(project, token, adresse);
    }
    
    public Map<String, Object> getNombresLigneUtilisateur() throws IOException, ParseException {
        Map<String, Object> res = new HashMap<String, Object>();
        Set<String> users = new HashSet<String>();
        int page = 1;
        int size = 0;
        do {
            request("projects/" + this.Project + "/repository/commits", "with_stats=true&per_page=750&page=" + page);
            JSONParser jsonP = new JSONParser();
            JSONArray commits = (JSONArray) jsonP.parse(new FileReader("request.json"));
            if(commits.size() == 0) {
                break;
            }
            size += commits.size();
            for (Object commit : commits) {
                JSONObject temp = (JSONObject) commit;
                JSONObject erv = (JSONObject) temp.get("stats");
                Object temp2 = erv.get("additions");
                Object temp3 = erv.get("deletions");
                String t = temp.get("author_name").toString();
                if(users.contains(t)) {
                    int[] temporaire = ((int[]) res.get(t));
                    int[] ajouter = new int[2];
                    ajouter[0] = temporaire[0] + ((Long) temp2).intValue();
                    ajouter[1] = temporaire[1] + ((Long) temp3).intValue();
                    res.put(t, ajouter);
                }
                else {
                    users.add(t);
                    int[] ajouter = new int[2];
                    ajouter[0] = ((Long) temp2).intValue();
                    ajouter[1] = ((Long) temp3).intValue();
                    res.put(t, ajouter);
                }
            }
            page++;
            System.out.println(size);
        } while(size%750 == 0);
        res.put("users",users);
        return res;
    }

    public static void affiche(Map<String, Object> map) {
        Set<String> user = (Set<String>) map.get("users");
        for (String string : user) {
            int[] tab =(int[]) map.get(string);
            System.out.println(string + "     additions : " + tab[0] + "    deletions : " + tab[1]);
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        //NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("3389","8ax_oKvn8CMzvyPmxUD1","https://gaufre.informatique.univ-paris-diderot.fr");
        NombresLigneUtilisateur n2 = new NombresLigneUtilisateur("278964","glpat-v5gGaWWxz_uXdK4MkY8K",null);
        affiche(n2.getNombresLigneUtilisateur());
    }
    
}
