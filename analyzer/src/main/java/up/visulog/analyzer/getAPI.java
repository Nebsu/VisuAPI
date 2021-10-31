package up.visulog.analyzer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class getAPI {
    private String Token;
    private int Project;
    public String Adresse;

    public getAPI(int project, String adresse) {
        this.Project = project;
        if(adresse == null) {
            this.Adresse = "https://gitlab.com";
        }
        else {
            this.Adresse = adresse;
            while(!testAdresse()) {
                System.out.println("Veuillez réinserer l'adresse ou appuyez sur entrée pour utiliser l'adresse par defaut : https://gitlab.com");
                //implementer le scanner pour lire l'url
                // if(sacnner == null) {
                //     this.Adresse = "https://gitlab.com";
                //     break;
                // }
            }
        }
    }
    
    public getAPI(int project,String token,String adresse) {
        this(project,adresse);
        this.Token = token;
        while(!testToken()) {
            //cimplementer le scanner pour lire le token
            // if(scanner == null) {
            //     this.Token = null;
            //     break;
            // }
        }
    }

    public static void main(String[] args) throws IOException {
        getAPI a = new getAPI(3389,"8ax_oKvn8CMzvyPmxUD1","https://gaufre.informatique.univ-paris-diderot.fr");
        getAPI b = new getAPI(278964,"efzccqe","https://gitlab.com");
        //URL url = new URL("https://gaufre.informatique.univ-paris-diderot.fr/api/v4/groups/1711/members/?private_token=8ax_oKvn8CMzvyPmxUD1");
        // URL url = new URL("https://gitlab.com/api/v4/projects/278964");
        // InputStream is = url.openConnection().getInputStream();
        // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        // String line = null;
        // while ((line = reader.readLine()) != null) {
        //     System.out.println(line);
        // }
        // reader.close();
    }

    public boolean testAdresse() {
        try {
            URL url = new URL(this.Adresse + "/api/v4/projects");
            InputStream is = url.openConnection().getInputStream();
        }
        catch (Exception e) {
            if(e.toString().equals("java.net.UnknownHostException: gaufre.informatique.univ-paris-diderot.fr")) { // modifier le string de l'execption pour garder suelemnt la partie "java.net.UnknownHostException:"
                System.out.println("Impossible de récupérer la requête.");
                System.out.println("Vérifiez vos paramètres de connexion.");
            }
            else {
                System.out.println("L'adresse donné ne permet pas d'acceder à l'api de gitlab.");
            }
            return false;
        }
        return true;
    }

    public Object[] countcommit() { //A FAIRE
        String s = this.Adresse + "/api/v4/projects/" + this.Project +"/repository/commits";
        if(this.Token != null) {
            s = s + "?private_token=" + this.Token;
        }
        else {

        }
        Object[] result = new Object[2];
        return result;
    }

    public boolean testToken() {
        try {
            URL url = new URL(this.Adresse + "?token=" + this.Token);
            InputStream is = url.openConnection().getInputStream();
        }
        catch (Exception e) {
            System.out.println("Clé invalide, Veuillez réinserer une clé valide ou appuyez sur entrée pour ne pas utiliser de clé.");
            return false;
        }
        return true;
    }

}
