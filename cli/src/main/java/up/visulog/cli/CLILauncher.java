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
        // var config = makeConfigFromCommandLineArgs(args);
        // if (config.isPresent()) {
        //     var analyzer = new Analyzer(config.get());
        //     var results = analyzer.computeResults();
        //     CreatePage c = new CreatePage();
        //     c.creer(results.toHTML());
        //     c.ouvrirPage();
        // } else
        //     displayHelpAndExit();
        test(args);
    }

    static Optional<Configuration> makeConfigFromCommandLineArgs(String[] args) {
        var gitPath = FileSystems.getDefault().getPath(".");
        var plugins = new HashMap<String, PluginConfig>();
        for (var arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.split("=");
                if (parts.length != 2)
                    return Optional.empty();
                else {
                    String pName = parts[0];
                    String pValue = parts[1];
                    switch (pName) {
                        case "--addPlugin":
                            // TODO: parse argument and make an instance of PluginConfig

                            // Let's just trivially do this, before the TODO is fixed:

                            if (pValue.equals("countCommits"))
                                plugins.put("countCommits", new PluginConfig() {
                                });

                            break;
                        case "LignesUtilisateurs":
                            // TODO (load options from a file)
                            break;
                        case "--justSaveConfigFile":
                            // TODO (save command line options to a file instead of running the analysis)
                            break;
                        default:
                            return Optional.empty();
                    }
                }
            } else {
                gitPath = FileSystems.getDefault().getPath(arg);
            }
        }
        return Optional.of(new Configuration(gitPath, plugins));
    }

    public static void test(String[] args) throws IOException, ParseException, URISyntaxException {
        // TODO : une map avec tout les clef = args
        Map<String, String> arguments = new HashMap<String, String>();
        for (var s : args) {
            if (s.toUpperCase().equals("WIKI")) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI ("https://gaufre.informatique.univ-paris-diderot.fr/groupe-1/visulog/wikis/home"));
                }
                // TODO créer page avec code qui redirect vers wiki
                return;
            }
            String[] spl = s.split("=");
            String s2=null;
            if(spl.length > 1) {
                s2 = spl[1];
            }
            arguments.put(spl[0],s2);
        }
        if (arguments.get("Plugin") == null) {
            displayHelpAndExit();
            return;// ERREUR
        }
        // TODO à modifier ptet
        CreatePage c = new CreatePage();
        String id = arguments.get("ID");
        String adr = arguments.get("Adresse");
        String token = arguments.get("Token");
        switch (arguments.get("Plugin")) {
            default: displayHelpAndExit();
            case "LignesUtilisateurs":
                if (arguments.size() > 5) {
                    displayHelpAndExit();
                }
                // TODO Faire en sorte de renvoyer une erreur si y'a trop d'args

                boolean all = false;
                if (arguments.get("all") != null && arguments.get("All").equals("true")) {
                    all = true;
                }
                NombresLigneUtilisateur NLU = new NombresLigneUtilisateur(id, token, adr, all);
                Map<String, Object> res = NLU.getNombresLigneUtilisateur();
                c.creer("drftgyhu");
                break;
            case "ModificationsFichier":
                if (arguments.size() > 5) {
                    displayHelpAndExit();
                }
                String file = arguments.get("File");
                String branch = arguments.get("Branch");
                NombreModificationFichierPlugin NMF = new NombreModificationFichierPlugin(id, adr, token , file, branch,
                        true);
                NMF.NombreModif(null, null);
                c.creer(NMF.toString());
                c.ouvrirPage();
                break;
            case "InformationIssues":
                // c.creer(NLM.toString());
                // c.ouvrirPage();
                 break;
            case "CommitsUtilisateurs":
                CommitsParUtilisateur CPU = new CommitsParUtilisateur(id,token,adr);
                CPU.afficherGraphique();
                // c.creer(CPU.toString());
                // c.ouvrirPage();
                 break;
            case "NombreLignesUtilisateur":
                // c.creer(NLM.toString());
                // c.ouvrirPage();
                break;
            case "HistoriqueCommit" :
                
                break;
        }
        // if (c /*TODO test cas ou fichier n'est pas vide */) {
        // c.ouvrir();
        // }
        // else {

        // }
        return;
    }

    private static void displayHelpAndExit() {
        System.out.println(ANSI_RED +"Mauvaise commande, veuillez consulter le wiki pour voir la liste des commandes disponibles et leurs utilisations");
        System.out.println("Vous pouvez appeler la commande \" .\\gradlew run --args='wiki' \"" + ANSI_RESET + " /!\\ Attention cela requiert une connexion à un compte gaufre");
        // TODO: print the list of options and their syntax
        System.exit(0);
    }
}