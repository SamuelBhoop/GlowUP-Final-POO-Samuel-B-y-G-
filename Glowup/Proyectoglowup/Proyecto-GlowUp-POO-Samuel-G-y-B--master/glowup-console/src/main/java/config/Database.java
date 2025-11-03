package config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final Properties props = new Properties();

    static {
        try {
            // 1) classpath (resources)
            InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("db.properties");
            if (in == null) in = Database.class.getResourceAsStream("/db.properties");

            // 2) archivo junto al proceso (working dir)
            if (in == null) {
                Path p = Path.of(System.getProperty("user.dir"), "db.properties");
                if (Files.exists(p)) in = Files.newInputStream(p);
            }

            // 3) archivo de dev: src/main/resources (por si ejecutas desde IDE)
            if (in == null) {
                Path p = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "db.properties");
                if (Files.exists(p)) in = Files.newInputStream(p);
            }

            if (in == null) throw new IllegalStateException("No se encontró db.properties");

            props.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DB] db.properties cargado. url=" + props.getProperty("db.url"));
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando Database", e);
        }
    }

    private Database() {}

    public static Connection getConnection() throws SQLException {
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        Connection c = (user == null && pass == null)
                ? DriverManager.getConnection(url)
                : DriverManager.getConnection(url, user == null ? "" : user, pass == null ? "" : pass);

        c.setAutoCommit(true);
        return c;
    }
}
