package up.visulog.analyzer;

import java.util.*;

import org.json.simple.parser.ParseException;

// TODO A retirer quand on aura modifPagz
import java.io.*;

public class NombreModificationFichierPlugin extends getAPI {
    private String Chemin;
    private String Branche;

    // L'adresse c'est sans api/v4 et sans l'id projet
    public NombreModificationFichierPlugin(int project, String token, String adresse, String Chemin, String Branche) {
        super(project, token, adresse);
        this.Chemin = Chemin;
        this.Branche = Branche;
    }

    public Map<String, Object> NombreModif() {
        Map<String, Object> result = new HashMap<String, Object>();
        int acc = 0;
        ArrayList<String[]> L = new ArrayList<String[]>();
        String date = dateCreationBranche();
        String idLastCommit = idLastCommit();
        String idCommit = idLastCommit;
        String idParent = "";
        while (!date.equals(dateCommit(idCommit))) {
            idParent = idCommitParent(idCommit);
            if (idParent.equals("[]")) { // Voir ce que l'API renvoie quand le commit n'a pas de parents
                break;
            }
            // if (/* Commit avec 2 parents */) {// Commits a deux parents
            // // TODO : Add dans la liste L le fait que y'ait fusion
            // break;
            // }
            if (fichierModif(idCommit, idParent)) {
                acc++;
                String[] commits = new String[2];
                commits[0] = idCommit;
                commits[1] = idParent;
                L.add(commits);
            }
            idCommit = idParent;
        }
        result.put("Nombre", acc);
        result.put("Commits", L);
        return result;
    }

    private String idCommitParent(String idCommit) throws IOException {
        try {
            String request = "projects/" + super.Project + "/repository/commits/" + idCommit;
            super.request(request, null);
            return testJson.getIdParent(null);
            // TODO
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String idLastCommit() {
        // TODO
        return "";
    }

    private String dateCreationBranche() {
        try {
            String request = "projects/" + super.Project + "/repository/branches/" + Branche;
            super.request(request,null);
            System.out.println(testJson.getDateBranche(null));
            return testJson.getDateBranche(null);
            // TODO
           } catch (Exception e) {
               e.printStackTrace();
           }
            return "";
        }

    public String dateCommit(String idCommit) {
        // TODO
        return "";
    }

    // public boolean fichierModif(String idCommit, String idParent) {
    // String s = adresse +
    // // TODO
    // return true;
    // }

    public static void main(String[] args) throws IOException { // org.json.simple.parser.ParseException
        NombreModificationFichierPlugin test = new NombreModificationFichierPlugin(3389, "8ax_oKvn8CMzvyPmxUD1",
                "https://gaufre.informatique.univ-paris-diderot.fr", "aaaaa", "ObtentionAPI");
        // test.idCommitParent("bb4ebe43744ab74969679426eebd214362f88635");
        test.dateCreationBranche();
        // testJson.api();
        // testJson.getAdress("analyzer/src/main/java/up/visulog/analyzer/AnalyzerResult.java");
    }
}
