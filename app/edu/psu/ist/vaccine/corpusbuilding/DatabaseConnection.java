package edu.psu.ist.vaccine.corpusbuilding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection c;
//    private static String url = "jdbc:postgresql://zeus.geog.psu.edu:5432";
//    private static String db = "twitter_geotxt_4";
//    private static String username = "morteza";
//    private static String password = "new";
   private static String url = "jdbc:postgresql://localhost:5432";
    //private static String url = "jdbc:postgresql://oldtrent.geog.psu.edu:5435";
    //private static String db = "newcorpustestc";
    private static String db = "NewCorpusTestC";

    private static String username = "postgres";
     private static String password = "123456";
   // private static String username = "geotxt";
   // private static String password = "j48sb&#";
    public static final String TWEET_TABLE_NAME = "tweets_final";
    public static final String GC_TABLE_NAME = "GCResults";

    static void connect() throws SQLException {

        c = DriverManager.getConnection(url + "/" + db, username, password);
        c.setAutoCommit(false);

    }

    static void close() throws SQLException {

        c.close();

    }
}
