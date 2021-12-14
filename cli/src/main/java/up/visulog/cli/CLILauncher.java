package up.visulog.cli;


import up.visulog.analyzer.*;
import up.visulog.analyzer.Analyzer;
import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.analyzer.CreatePage;
import up.visulog.analyzer.getAPI;
import up.visulog.analyzer.NombresLigneUtilisateur;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.simple.parser.ParseException;

public class CLILauncher {

    public static void main(String[] args) throws IOException, ParseException {
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

    public static void test(String[] args) throws IOException, ParseException {
        // TODO : une map avec tout les clef = args
        Map<String, String> arguments = new HashMap<String, String>();
        for (var s : args) {
            if (s.toUpperCase().equals("WIKI")) {
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
            return; // ERREUR
        }
        // TODO à modifier ptet
        CreatePage c = new CreatePage();
        String id = arguments.get("ID");
        String adr = arguments.get("Adresse");
        String token = arguments.get("Token");
        switch (arguments.get("Plugin")) {
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
                NombreModificationFichierPlugin NLM = new NombreModificationFichierPlugin(id, adr, token , file, branch,
                        true);
                NLM.NombreModif(null, null);
                c.creer(NLM.toString());
                c.ouvrirPage();
                break;
            case "InformationIssues":
                // c.creer(NLM.toString());
                // c.ouvrirPage();
                 break;
            case "NombreCommitsUtilisateur":
                // c.creer(NLM.toString());
                // c.ouvrirPage();
                 break;
            case "NombreLignesUtilisateur":
                // c.creer(NLM.toString());
                // c.ouvrirPage();
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
        System.out.println("Wrong command...");
        // TODO: print the list of options and their syntax
        System.exit(0);
    }
}