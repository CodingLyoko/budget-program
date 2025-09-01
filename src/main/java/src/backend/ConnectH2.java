package src.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.tinylog.Logger;

import src.shared.Constants;

public class ConnectH2 {

    public static final String JDBC_URL = "jdbc:h2:file:./" + Constants.APP_FILENAME;

    private ConnectH2() {
    }

    public static void executeSqlScripts() throws SQLException, IOException {

        Logger.info("About to execute SQL scripts...");

        String[] sqlScriptFilenames = getSqlScriptFilenames();

        try (Connection connection = DriverManager.getConnection(JDBC_URL);
                Statement statement = connection.createStatement();) {
            Logger.info("DB Connected!");

            // Holds the content of the current SQL script
            StringBuilder sqlScriptContents = new StringBuilder();

            for (String sqlFilename : sqlScriptFilenames) {
                Logger.info("Entered first 'do' statement for Filenames...");

                Logger.info("SQL Filename: " + sqlFilename);

                if (sqlFilename != null) {

                    Logger.info("SQL File Name: " + sqlFilename);

                    // Gets the filepath for the current SQL script
                    String sqlScriptFilepath = "/sql/" + sqlFilename;

                    // Empties the StringBuilder so it can read a fresh SQL script
                    sqlScriptContents.setLength(0);

                    // Instantiate variables to read each of the SQL scripts
                    InputStream sqlScriptStream = ConnectH2.class.getResourceAsStream(sqlScriptFilepath);
                    BufferedReader sqlScriptReader = new BufferedReader(new InputStreamReader(sqlScriptStream));

                    String lineToRead;
                    do {

                        // Get the next unread line of text in the SQL script file
                        lineToRead = sqlScriptReader.readLine();

                        // If there is text on the read line, store it
                        if (lineToRead != null) {
                            sqlScriptContents.append(lineToRead);
                            sqlScriptContents.append(System.getProperty("line.separator"));
                        }
                    } while (lineToRead != null);

                    // Add the contents of the SQL file to the SQL statement
                    statement.addBatch(sqlScriptContents.toString());
                    Logger.info("SQL Statement Contents: " + sqlScriptContents.toString());

                    sqlScriptStream.close();
                    sqlScriptReader.close();
                }
            }

            // Execute the content of all the SQL scripts
            statement.executeBatch();
        }

        Logger.info("SQL Scripts Executed!");
    }

    private static String[] getSqlScriptFilenames() {

        Set<String> result = new HashSet<>(); // Using Set to avoid duplicates in case it is a subdirectory

        Class<?> clazz = ConnectH2.class;
        String path = "sql/"; // Used to designate the directory with the SQL scripts

        URL dirURL = clazz.getResource(path);

        // In case of a jar file, we can't actually find a directory.
        // Have to assume the same jar as clazz.
        if (dirURL == null) {

            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }
        // A JAR path
        if (dirURL.getProtocol().equals("jar")) {

            getJarSqlScriptFilenames(dirURL, path, result);

            // Compiling and running program directly
        } else {
            try {

                File sqlDirectory = new File(ConnectH2.class.getResource("/sql").toURI());
                File[] listOfSqlFiles = sqlDirectory.listFiles();

                for (File sqlScript : listOfSqlFiles) {
                    result.add(sqlScript.getName());
                }
            } catch (URISyntaxException urie) {
                urie.printStackTrace();
                Logger.error("Could not get filenames for SQL scripts.", urie.getMessage());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Gets the filenames of the SQL scripts when the program is being executed
     * within a JAR file.
     * 
     * This is a helper function for getSqlScriptFilenames().
     * 
     * @param dirURL - the directory where the program resources are located
     * @param path   - the directory where the SQL scripts are located
     * @param result - the variable holding the filenames
     */
    private static void getJarSqlScriptFilenames(URL dirURL, String path, Set<String> result) {
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip out only the JAR
        // file
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { // filter according to the path

                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }

                    result.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Could not get SQL Filenames from JAR file.", e.getMessage());
        }
    }
}
