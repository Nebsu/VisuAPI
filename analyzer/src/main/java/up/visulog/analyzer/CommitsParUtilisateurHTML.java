package up.visulog.analyzer;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.io.*;
import java.awt.Desktop;
import java.util.Arrays;
import java.util.LinkedList;

public class CommitsParUtilisateurHTML{
    private Map<String, Object> result;

    public CommitsParUtilisateurHTML(Map<String, Object> r){
        this.result = r;
    }

    public Map<String, Object> getResult() {
        return result;
    }


    ///////////////////////////TEMPORAIRE///////////////////////////////////////
    public void creerCss(String s) throws IOException {
        File f = new File("pie.css");
        FileOutputStream fos = new FileOutputStream("pie.css");
        if(f.exists()) {
            f.delete();
        }
        fos.write(s.getBytes());
        fos.flush();
        fos.close();
    }

    public void creer(String s) throws IOException {
        File f = new File("example.html");
        FileOutputStream fos = new FileOutputStream("example.html");
        if(f.exists()) {
            f.delete();
        }
        fos.write(s.getBytes());
        fos.flush();
        fos.close();
    }

    public void ouvrirPage() throws IOException {
        File f = new File("example.html");
        if(!Desktop.isDesktopSupported()){
            System.out.println("Desktop n'est pas prise en charge");
            return;
        }
        Desktop d = Desktop.getDesktop();
        d.open(f);
    }
    //////////////////////////////////////////////////////////////////////////////

    //Creation du code html
    public String toHTML(){
        String s = "<html><body><h1>Nombre de Commits par Utilisateur</h1>";
        for(var item : result.entrySet()){
            s += "<li>"+item.getKey()+": "+item.getValue()+"</li>";
        }
        return s + "</body></html>";
    }

    public double[] getCommitsPercentile(){
        double[] res = new double[result.size()];
        int total = 0;
        int i = 0;
        for(var item : result.entrySet()){
            total += (int)item.getValue();
        }
        for(var item : result.entrySet()){
            res[i] = (double)(int)item.getValue()*100/total;
            i=i+1;
        }      
        return res;
    }

    //TODO : Fonction Creation page css + html pie chart

    public String colorGenerator(){
        Random obj = new Random();
        int rand_num = obj.nextInt(0xffffff + 1);
        String colorCode = String.format("#%06x", rand_num);
        return colorCode;
    }

    public String createHTMLChart() {
        StringBuilder html = new StringBuilder("<html><link rel=\"stylesheet\" href=\"pie.css\"><body><h1>Commit Proportion Pie Chart</h1><div id=\"my-pie-chart-container\"><div id=\"my-pie-chart\"></div><div id=\"legenda\">");
        int i = 0;
        for (var item : result.entrySet()) {
            i = i+1;
            String divId = "color-" + i;
            html.append("<div class=\"entry\">").append("<div id=\"").append(divId).append("\" class=\"entry-color\"></div>").append("<div class=\"entry-text\">").append(item.getKey()).append("</div></div>");
        }
        html.append("</div></div></body></html>");
        return html.toString();
    }

    public String cssGenerator(){
        StringBuilder css = new StringBuilder("body {background-color: white;display: flex;justify-content: center;align-items: center;flex-direction: column;}#my-pie-chart-container {display: flex;align-items: center;}  #my-pie-chart {background: conic-gradient(");
        double[]tab = getCommitsPercentile();
        String[]colorMem = new String[tab.length];
        int mem = 0;
        int percentile = 0;
        for(int j = 0; j < colorMem.length; j++){
            colorMem[j] = colorGenerator();
        }
        for(int i = 0; i < tab.length; i++){
            percentile += tab[i];
            css.append(colorMem[i] + " " + mem + "%" + " " + percentile + "%");
            if(i != tab.length - 1){
                css.append(", ");
            }
            mem = percentile;
        }
        css.append(");border-radius: 50%;width: 150px;height: 150px;}#legenda {margin-left: 20px;background-color: white;padding: 5px;}.entry {display: flex;align-items: center;}.entry-color {height: 10px;width: 10px;}.entry-text {margin-left: 5px;}");
        int i = 0;
        for(int j = 0; j < colorMem.length; j++){
            i = i+1;
            css.append("#color-"+i).append("{background-color:").append(colorMem[j]+ ";}");
        }
        return css.toString();
    }

    public static void main(String[] args) throws IOException {
        CommitsParUtilisateur p = new CommitsParUtilisateur(3389, "bVqyB1SzLYKnSi6u1cdM", 
        "https://gaufre.informatique.univ-paris-diderot.fr");
        CommitsParUtilisateurHTML c = new CommitsParUtilisateurHTML(p.recupererCommits());
        c.creerCss(c.cssGenerator());
        c.creer(c.createHTMLChart());
        c.ouvrirPage();
    }
}
