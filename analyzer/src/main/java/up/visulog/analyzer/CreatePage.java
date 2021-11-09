package up.visulog.analyzer;
import java.io.*;
import java.awt.Desktop;

public class CreatePage {

    public void creer(String s) throws IOException {
        File f = new File("example.html");
        FileOutputStream fos = new FileOutputStream("example.html");
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

}
