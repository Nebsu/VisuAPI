package up.visulog.analyzer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommitParUtilisateur {

    public String toHTML(Map<String, Integer> map){
        String s = "<html><body>";
        for(var item : map.entrySet()){
            s += "<li>"+item.getKey()+": "+item.getValue()+"</li>";
        }
        return s + "</body></html>";
    }
}
