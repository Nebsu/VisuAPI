package up.visulog.analyzer;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CommitsParUtilisateurHTML{
    private Map<String, Object> result;

    public CommitsParUtilisateurHTML(Map<String, Object> r){
        this.result = r;
    }

    //Creation du code html
    public String toHTML(){
        String s = "<html><body>";
        for(var item : result.entrySet()){
            s += "<li>"+item.getKey()+": "+item.getValue()+"</li>";
        }
        return s + "</body></html>";
    }

    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        CommitsParUtilisateurHTML c = new CommitsParUtilisateurHTML(p.recupererCommits());
        System.out.println(c.toHTML());
    }
}
