package up.visulog.analyzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class test extends getAPI {

    public test(String project,String token,String adresse) {
        super(project,token,adresse);
    }

    public void fonction() throws IOException, ParseException {
        this.request("projects/3389/issues", null);
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader("request.json"));
        JSONArray jsonObject =  (JSONArray) object;
        System.out.println(jsonObject.get(1));
        //System.out.println(elementOfID("id",j));
    }

    public Object elementOfID(String element, JSONObject json) {
        Object name = json.get(element);
        return name;
    }

        // JSONParser jsonP = new JSONParser();
        // try {
        //     JSONObject jsonO = (JSONObject) jsonP.parse(new FileReader("C:/Users/theau/OneDrive/Cours/2021/PP/visulog/analyzer/src/main/java/up/visulog/analyzer/test.json"));
        //     Object name = jsonO.get("compare_timeout");
        //     System.out.println("Name :" + name);
        // } catch (FileNotFoundException e) {
        //     e.printStackTrace();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }



}