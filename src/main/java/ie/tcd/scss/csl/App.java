package ie.tcd.scss.csl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.atlassian.clover.api.CloverException;
import org.apache.commons.cli.*;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        Options options = new Options();

        // add options
        options.addOption("f", true, "database path");
        options.addOption("o", true, "XML path");
        options.addOption("output_db", true, "output database path");

        // create a parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);

            // retrieve the values of the options
            String dbPath = cmd.getOptionValue("f");
            String outputPath = cmd.getOptionValue("o");
            String outputDBPath = cmd.getOptionValue("output_db");
            if (dbPath == null) {
                System.out.println("Please specify the path to the Clover database");
                return;
            }
            // use the values of the options
            System.out.println("databse path: " + dbPath);
            if (outputPath == null) {
                try {
                    CloverDatabaseReader cloverDatabaseReader = new CloverDatabaseReader(dbPath);
                    cloverDatabaseReader.generateReport();
                } catch (CloverException | ParserConfigurationException | TransformerException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("XML path: " + outputPath);
                System.out.println("Output DB path: " + outputDBPath);
                try {
                    CloverDatabaseReader cloverDatabaseReader = new CloverDatabaseReader(dbPath, outputPath,
                            outputDBPath);

                    cloverDatabaseReader.generateReport();
                } catch (CloverException | ParserConfigurationException | TransformerException e) {
                    e.printStackTrace();
                }
            }

        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
        }
    }
}
