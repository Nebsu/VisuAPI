package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountCommitsPerAuthorPlugin implements AnalyzerPlugin {
    private final Configuration configuration;
    private Result result;

    public CountCommitsPerAuthorPlugin(Configuration generalConfiguration) {
        this.configuration = generalConfiguration;
    }

    static Result processLog(List<Commit> gitLog) {
        var result = new Result();
        for (var commit : gitLog) {
            var nb = result.commitsPerAuthor.getOrDefault(commit.author, 0);
            result.commitsPerAuthor.put(commit.author, nb + 1);
        }
        return result;
    }

    @Override
    public void run() {
        result = processLog(Commit.parseLogFromCommand(configuration.getGitPath()));
    }

    @Override
    public Result getResult() {
        if (result == null) run();
        return result;
    }

    static class Result implements AnalyzerPlugin.Result {
        private final Map<String, Integer> commitsPerAuthor = new HashMap<>();

        Map<String, Integer> getCommitsPerAuthor() {
            return commitsPerAuthor;
        }

        @Override
        public String getResultAsString() {
            return commitsPerAuthor.toString();
        }

        @Override
        public String getResultAsHtmlDiv() {  
            
            //CHOIX DU PLUGIN

            String plugin = "historique de commits";

            // ACCUEIL AVEC TITRE
            StringBuilder html = new StringBuilder("<div class='title'><h1> Statistiques du projet : X </h1> <br> via Gitlab <div class='img'><img src='https://about.gitlab.com/images/press/logo/png/gitlab-icon-rgb.png' width='50' height='50'></div></div>");
            
            //BAR DE CHARGEMENT
            int pourcentage;
            pourcentage = (int)(Math.random()*100);
            
            html.append("<div class='bar'><div class='percentage has-tip'  style='width: "+pourcentage+"%' data-perc='"+pourcentage+"%'></div></div>");
            html.append("<p>"+pourcentage+"</p>");

            //if(pourcentage <100){
                //html.append("<meta http-equiv='refresh' content='3'>");
            //}
            
            
            // WIKI DU PROJET
            if(plugin == "wiki"){
                html.append("<div class='wiki'> <h2>Wiki du projet</h2>").append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede. Praesent blandit odio eu enim. Pellentesque sed dui ut augue blandit sodales. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Aliquam nibh. Mauris ac mauris sed pede pellentesque fermentum. Maecenas adipiscing ante non diam sodales hendrerit.");
                html.append("</div>");
            }
            // MEMBRES ET NOMBRES DE COMMITS
            if(plugin=="nombre de commits"){
                html.append("<div class='statParMembres'><h2>Statistiques par membres</h2> <ul> <div class='statMembres'>");
                for (var item : commitsPerAuthor.entrySet()) {
                    html.append("<li><div class='name'>").append(item.getKey()).append("</div>") // Nom du membre
                    .append("<div class='commit'> Nombre de commits : ").append(item.getValue()).append("</div>") // Son nombre de commit
                    .append("</li>");
                }
                html.append("</div></ul></div>");
            }
            

            // STATS GLOBAUX
            if(plugin=="stats globaux"){
                html.append("<div class='globalStat'><h2>Statistiques globaux</h2>");
                html.append("<div class='branches'><h3>Nombre de branches</h3><div class='nbDeBranches'>14</div></div> <br> <div class='tickets'><h3>Nombre de tickets</h3><div class='nbDeTickets'>26</div></div>  </div>");
            }
            
            // HISTORIQUE DE COMMITS
            if(plugin=="historique de commits"){
                html.append("<div class='histoCommits'> <h2>Historique de commits</h2> <table>")
                .append("<tr> <td>Membre</td> <td>Contenu du commit</td> <td>Date</td> <tr>");

                for (var item : commitsPerAuthor.entrySet()) {
                    html.append("<tr> <td>").append(item.getKey()).append("</td>") // Auteur du commit
                    .append("<td> Lorem ipsum dolor sit amet, consectetur adipiscing elit.").append("</td>") // Contenu du commit
                    .append("<td> 16:09 </td>")
                    .append("</tr>");
                }
                html.append("</table></div>");
            }
            
            return html.toString();
        }
    }
}
