package up.visulog.analyzer;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

public abstract class getAPI {
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
            url.openConnection().getInputStream();
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
            url.openConnection().getInputStream();
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
            url.openConnection().getInputStream();
        }
        catch (Exception e) {
            System.out.println("ID de projet invalide veuillez réinserer l'ID.");
            return false;
        }
        return true;
    }

    public String scan() {
        return new Scanner(System.in).nextLine();
    }

    protected void request(String uri, String args) throws IOException {
        String s = "";
        if(this.Token != null) {
            uri += "?private_token=" + this.Token;
            if(args != null) {
                uri += "&" + args;
            }
        }
        else if(args != null) {
            uri += "?" + args;
        }
        URL url = new URL(this.Adresse + "/api/v4/" + uri);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        FileOutputStream output = new FileOutputStream("request.json");
        while ((line = reader.readLine()) != null) {
            s = s + line;
        }
        output.write(s.getBytes());
        output.flush();
        output.close();
        reader.close();
    }

}
