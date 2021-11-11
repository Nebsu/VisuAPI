package up.visulog.analyzer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CommitsParUtilisateurHTML{
    private int[] nbCommits;
    private LinkedList<String> members;

    public CommitsParUtilisateurHTML(int[]n, LinkedList<String> m){
        this.nbCommits = n;
        this.members = m;
    }

    //Creation du code html
    public String toHTML(){
        String s = "<html><body>";
        for(int i = 0; i < nbCommits.length; i++){
            s += "<li>"+members.get(i)+": "+nbCommits[i]+"</li>";
        }
        return s + "</body></html>";
    }

    public static void main(String[] args) {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        CommitsParUtilisateurHTML c = new CommitsParUtilisateurHTML(p.recupererCommits(), p.recupererMembres());
        c.toHTML();
    }
}
