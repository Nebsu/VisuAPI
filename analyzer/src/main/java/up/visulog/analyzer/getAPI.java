package up.visulog.analyzer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class getAPI {

    public static void main(String[] args) throws IOException {
        URL url = new URL("https://gaufre.informatique.univ-paris-diderot.fr/api/v4/projects/?private_token=8ax_oKvn8CMzvyPmxUD1");
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }

}
