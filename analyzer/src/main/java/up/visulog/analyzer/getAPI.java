package up.visulog.analyzer;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class getAPI {
    protected String Token;
    protected String Project;
    public String Adresse;

    public getAPI(String project,String token,String adresse) {
        setAdresse(adresse);
        setToken(token);
        setProject(project);
    }

    public void setAdresse(String adresse) {
        if(adresse == null) {
            Adresse = "https://gitlab.com";
        }
        else {
            if (!adresse.substring(0,8).equals("https://")){
                Adresse = "https://"+adresse;
            }
            else {
                Adresse = adresse;
            }
            System.out.println(Adresse);
            if(!testAdresse()) {
                System.out.println(Adresse);
                setAdresse(scan());
            }
        }
    }

    public void setToken(String token) {
        if(token != null) {
            Token = token;
            if(!testToken()) {
                String s = scan();
                if(s == null) {
                    Token = null;
                }
                else if(s.equals("a") || s.equals("A")) {
                    System.out.println("Indiquez votre adresse :");
                    setAdresse(scan());
                    setToken(token);
                }
                else {
                    setToken(s);
                }
            }
        }
    }

    public void setProject(String project) {
        this.Project = project;        
        if(!testProject()) {
            String s = scan();
            if(s == null) {
                setProject(s);
            }
            else if(s.equals("a") || s.equals("A")) {
                System.out.println("Indiquez votre adresse :");
                setAdresse(scan());
                setProject(project);
            }
            else if(s.equals("t") || s.equals("T")) {
                System.out.println("Indiquez votre token :");
                setToken(scan());
                setProject(project);
            }
            else {
                setProject(s);
            }
        }
    }

    public boolean testAdresse() {
        try {
            URL url = new URL(this.Adresse + "/api/v4/projects");
            url.openConnection().getInputStream();
        }
        catch (Exception e) {
            if(e.toString().substring(0,31).equals("java.net.MalformedURLException:")) {
                System.out.println("Adresse invalide.");
                System.out.println("Veuillez inserer un URL du type: https://gitlab.com ou appuyer sur entré pour utiliser https://gitlab.com");
                System.out.println("(Enregistré actuellement : "+ this.Adresse + " ) ");
            }
            else {
                System.out.println("URL invalide ou erreur de connexion.");
                System.out.println("Veuillez réinserer l'URL");
            }
            return false;
        }
        return true;
    }

    public boolean testToken() {
        try {
            if(this.Token == null) {
                return true;
            }
            else {
                URL url = new URL(this.Adresse + "/api/v4/projects?private_token=" + this.Token);
                url.openConnection().getInputStream();
            }
        }
        catch (Exception e) {
            System.out.println();
            System.out.println("Clé invalide. Veuillez :");
            System.out.println("- Réinsérer une clé valide");
            System.out.println("ou");
            System.out.println("-Entrer \"A\" pour changer l'adresse");
            System.out.println("ou");
            System.out.println("-Laisser vide pour ne pas présenter de clé ");
            return false;
        }
        return true;
    }

    public boolean testProject() {
        try {
            if(this.Token == null) {
                URL url = new URL(this.Adresse + "/api/v4/projects/" + this.Project);
                url.openConnection().getInputStream();
            }
            else {
                URL url = new URL(this.Adresse + "/api/v4/projects/" + this.Project + "?private_token=" + this.Token);
                url.openConnection().getInputStream();
            }
        }
        catch (Exception e) {
            System.out.println();
            System.out.println("ID de projet invalide. Veuillez :");
            System.out.println("- Réinsérer un ID valide" + " (Enregistré actuellement : "+ this.Project + " ) ");
            System.out.println("ou");
            System.out.println("- Entrer \"A\" pour changer l'adresse"  + " (Enregistré actuellement : "+ this.Adresse + " ) ");
            System.out.println("ou");
            System.out.println("- Entrer \"T\" pour changer le token");
            return false;
        }
        return true;
    }

    public String scan() {
        return new Scanner(System.in).nextLine();
    }

    public String mailToImg(String mail) throws IOException, ParseException {
        request("avatar","email=" + mail);
        JSONParser jsonP = new JSONParser();
        JSONObject image = (JSONObject) jsonP.parse(new FileReader("request.json"));
        return (String) image.get("avatar_url");
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