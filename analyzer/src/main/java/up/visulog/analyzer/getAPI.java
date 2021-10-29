package up.visulog.analyzer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class getAPI {
    private String Token;
    private String project;
    
    public getAPI(String Token) {
        this.Token = Token;
    }

    public getAPI() {

    }

    public static void main(String[] args) throws IOException {
        getAPI a = new getAPI("8ax_oKvn8CMzvyPmxUD1");
        System.out.println(a.connexion());
        // URL url = new URL("https://gaufre.informatique.univ-paris-diderot.fr/api/v4/groups/1711/members/?private_token=8ax_oKvn8CMzvyPmxUD1");
        // InputStream is = url.openConnection().getInputStream();
        // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        // String line = null;
        // while ((line = reader.readLine()) != null) {
        //     System.out.println(line);
        // }
        // reader.close();
    }

    public Map countcommit() { //A FAIRE
        String s = "https://gaufre.informatique.univ-paris-diderot.fr/api/v4/projects/" + this.project +"/repository/commits";
        if(this.Token != null) {
            s = s + "?private_token=" + this.Token;
        }
        else {

        }
        return new HashMap<>();
    }

    public boolean connexion() {
        try {
            String s = "https://gaufre.informatique.univ-paris-diderot.fr/api/v4/projects";
            if(this.Token != null) {
                s = s + "?private_token=" + this.Token;
            }
            URL url = new URL(s);
            InputStream is = url.openConnection().getInputStream();
        }
        catch (Exception e) {
            if(e.toString().equals("java.net.UnknownHostException: gaufre.informatique.univ-paris-diderot.fr")) {
                System.out.println("Impossible de récupérer la requête.");
                System.out.println("Vérifiez vos paramètres de connexion.");
            }
            else {
                System.out.println("Clé invalide");
            }
            return false;
        }
        return true;
    }

}
