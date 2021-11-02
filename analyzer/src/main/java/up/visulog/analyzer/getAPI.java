package up.visulog.analyzer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class getAPI {
    private String Token;
    private int Project;
    public String Adresse;
    // API : https://docs.gitlab.com/ee/api/api_resources.html
    
    public getAPI(int project,String token,String adresse) {
        this.Adresse = adresse;
        while(!testAdresse()) {
            this.Adresse = scan();
        }
        this.Token = token;
        while(!testToken()) {
            this.Token = scan();
        }
        this.Project = project;
        while(!testProject()) {
            try {
                this.Project = Integer.parseInt(scan());
            }
            catch (Exception e) {
                System.out.println("Veuillez entré des chiffres");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        getAPI a = new getAPI(3389,"a8ax_oKvn8CMzvyPmxUD1","https://gaufre.informatique.univ-paris-diderot.fr");
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
            if(e.toString().substring(0,31).equals("java.net.MalformedURLException:")) {
                System.out.println("Adresse invalide, veuillez inserer un URL du type: https://gitlab.com ou appuyer sur entré pour utiliser https://gitlab.com");
            }
            else {
                System.out.println("Erreur de connexion verifier votre connexion internet et réinserer votre URL");
            }
            return false;
        }
        return true;
    }

    public boolean testToken() {
        try {
            URL url = new URL(this.Adresse + "/api/v4/projects?private_token=" + this.Token);
            InputStream is = url.openConnection().getInputStream();
        }
        catch (Exception e) {
            System.out.println("Clé invalide, Veuillez réinserer une clé valide ou appuyez sur entrée pour ne pas utiliser de clé.");
            return false;
        }
        return true;
    }

    public boolean testProject() {
        try {
            URL url = new URL(this.Adresse + "/api/v4/projects/" + this.Project + "?private_token=" + this.Token);
            InputStream is = url.openConnection().getInputStream();
        }
        catch (Exception e) {
            System.out.println("ID de projet invalide veuillez réinserer l'ID.");
            return false;
        }
        return true;
    }

    public String scan() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

}
