package up.visulog.cli;


import up.visulog.analyzer.*;
import up.visulog.analyzer.Analyzer;
import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.analyzer.CreatePage;
import up.visulog.analyzer.getAPI;
import up.visulog.analyzer.NombresLigneUtilisateur;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.awt.Desktop;

import org.json.simple.parser.ParseException;

public class CLILauncher {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
        commandToFunction(args);
    }

    public static void commandToFunction(String[] args) throws IOException, ParseException, URISyntaxException { // Fonction qui lit la ligne passée en commande et appelle la fonction correspondante si les critères requis sont remplis.
        Map<String, String> arguments = new HashMap<String, String>();
        for (var s : args) {
            if (s.toUpperCase().equals("WIKI")) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI ("https://gaufre.informatique.univ-paris-diderot.fr/groupe-1/visulog/wikis/home"));
                }
                return;
            }
            String[] spl = s.split("=");
            String s2=null;
            if(spl.length > 1) {
                s2 = spl[1];
            }
            arguments.put(spl[0].toLowerCase(),s2);
        }
        if (arguments.get("plugin") == null) {
            displayHelpAndExit(false);
            return;
        }
        CreatePage c = new CreatePage();
        String id = arguments.get("id");
        String adr = arguments.get("adresse");
        String token = arguments.get("token");
        switch (arguments.get("plugin")) {
            default: displayHelpAndExit(false);
            case "LignesUtilisateurs": 
                if (arguments.size() > 5) {
                    displayHelpAndExit(true);
                }
                boolean all = false;
                if (arguments.get("all") != null && arguments.get("all").equals("true")) {
                    all = true;
                }
                NombresLigneUtilisateur NLU = new NombresLigneUtilisateur(id, token, adr, all);
                Map<String, Object> res = NLU.getNombresLigneUtilisateur();
                c.creer(NLU.afficheHTML(res));
                c.ouvrirPage();
                break;
            case "ModificationsFichier":
                if (arguments.size() > 6) {
                    displayHelpAndExit(true);
                }
                String file = arguments.get("file");
                String branch = arguments.get("branch");
                NombreModificationFichierPlugin NMF = new NombreModificationFichierPlugin(id, token, adr , file, branch,
                        true);
                c.creer(NombreModificationFichierPlugin.CreateHtmlPage(NMF.NombreModif(null, null)));
                c.ouvrirPage();
                break;
            case "InformationsTickets":
                if (arguments.size() > 4) {
                    displayHelpAndExit(true);
                }
                InformationIssuesPlugin IIP = new InformationIssuesPlugin(id, token, adr);
                IIP.requestIssue();
                c.creer(IIP.toHTML());
                c.ouvrirPage();
                break;
            case "CommitsUtilisateurs":
                if (arguments.size() > 5) {
                    displayHelpAndExit(true);
                }
                boolean graphique = true;
                if (arguments.get("graphique") != null && arguments.get("graphique").equals("false")) {
                    graphique = false;
                }
                CommitsParUtilisateur CPU = new CommitsParUtilisateur(id,token,adr);
                if(graphique) {
                    CPU.afficherGraphique();
                }
                else {
                    c.creer(CPU.toHTML());
                    c.ouvrirPage();
                }
                break;
            case "HistoriqueCommit" :
                if (arguments.size() > 4) {
                    displayHelpAndExit(true);
                }
                CommitHistory CH = new CommitHistory(id, token, adr);
                String html = CH.toHTML();
                c.creer(html);
                c.ouvrirPage();
                break;
        }
        return;
    }

    private static void displayHelpAndExit(boolean arg) { //  Fonction appelée en cas de mauvais appel du programme, elle redirige vers le wiki.
        System.out.print(ANSI_RED);
        if (arg) {
            System.out.println("Nombre d'arguments incorrects, veuillez consulter le wiki pour voir la liste des commandes disponibles et leurs utilisations");
        }
        else {
            System.out.println("Mauvaise commande, veuillez consulter le wiki pour voir la liste des commandes disponibles et leurs utilisations");
        }
        System.out.println("Vous pouvez appeler la commande \" .\\gradlew run --args='wiki' \"" + ANSI_RESET + " /!\\ Attention cela requiert une connexion à un compte gaufre");
        System.exit(0);
    }
}