import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDBFetcher {
    private static final Logger LOGGER = Logger.getLogger(PDBFetcher.class.getName());
    /**
     * Fetches PDB data from the RCSB PDB website.
     *
     * @param pdbId the PDB ID of the protein structure to fetch
     * @return the PDB data as a string
     */
    public static String fetchPDBData(String pdbId) {
        StringBuilder pdbData = new StringBuilder();
        try {
            URL url = new URL("https://files.rcsb.org/view/" + pdbId + ".pdb");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 seconds timeout
            conn.setReadTimeout(5000); // 5 seconds timeout

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    pdbData.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching PDB data", e);
        }
        return pdbData.toString();
    }
}
