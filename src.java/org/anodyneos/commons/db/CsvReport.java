package org.anodyneos.commons.db;

import java.sql.*;
import java.util.Properties;
import java.io.*;
import org.anodyneos.commons.text.CsvWriter;

public class CsvReport {

    public static final String JDBC_USER = "jdbcUser";
    public static final String JDBC_PASSWORD = "jdbcPassword";
    public static final String JDBC_URL = "jdbcURL";
    public static final String JDBC_DRIVER = "jdbcDriver";
    public static final String JDBC_PROPERTIES = "jdbcProperties";
    public static final String IN = "in";
    public static final String OUT = "out";
    public static final String QUIET = "quiet";
    public static final String HELP = "help";

    private static java.text.SimpleDateFormat isof;
    static {
        isof = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isof.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
    }

    private static final String shortHelp = "Try 'CsvReport --help' for more information.";
    private static final String usageHelp =
              "CsvReport Executes an SQL query and Generates CSV output.\n"
            + "\n"
            + "Usage: CsvReport [OPTION]...\n"
            + "\n"
            + "JDBC Properties can be provided by command line options, a\n"
            + "properties file, or system properties, in that order of priority\n"
            + "if specified more than once.\n"
            + "\n"
            + "Options:\n"
            + "  -P <file>                          props file to use for jdbc\n"
            + "  -u, --jdbcUser <username>          db user name\n"
            + "  -p, --jdbcPassword <password>      db password\n"
            + "  -U, --jdbcUrl <url>                db url\n"
            + "  -d, --jdbcDriver <class>           db driver class name\n"
            + "  -i, --in <file>                    file containing SQL query; stdin\n"
            + "                                     will be used if -i not specified\n"
            + "  -o, --out <file>                   file for CSV output; stdout\n"
            + "                                     will be used if -o not specified\n"
            + "  -q, --quiet                        suppress extra output"
            ;

    private String jdbcUser = null;
    private String jdbcPassword = null;
    private String jdbcURL = null;
    private String jdbcDriver = null;
    private String outPath = null;
    private String inPath = null;
    private boolean quiet = false;

    public static void main(String[] argv) throws SQLException, ClassNotFoundException,
    InstantiationException, IllegalAccessException, IOException {
        try {
            CsvReport report = new CsvReport();
            if (!report.readParms(argv)) {
                return;
            }
            report.go();
        } catch (ReportException e) {
            System.err.println(e.getMessage());
            System.err.println(shortHelp);
        }
    }

    private CsvReport() {
    }

    private boolean readParms(String[] argv) throws ReportException {
        String jdbcProperties = null;

        jdbcUser = System.getProperty(JDBC_USER);
        jdbcPassword = System.getProperty(JDBC_PASSWORD);
        jdbcURL = System.getProperty(JDBC_URL);
        jdbcDriver = System.getProperty(JDBC_DRIVER);

        String which = null;
        for (int i = 0; i < argv.length; i++) {
            String param = argv[i];
            if (param.equals("--" + HELP)) {
                System.err.println(usageHelp);
                return false;
            } else if (param.equals("--" + JDBC_USER) || param.equals("-u")) {
                which = JDBC_USER;
            } else if (param.equals("--" + JDBC_PASSWORD) || param.equals("-p")) {
                which = JDBC_PASSWORD;
            } else if (param.equals("--" + JDBC_URL) || param.equals("-U")) {
                which = JDBC_URL;
            } else if (param.equals("--" + JDBC_DRIVER) || param.equals("-d")) {
                which = JDBC_DRIVER;
            } else if (param.equals("-P")) {
                which = JDBC_PROPERTIES;
            } else if (param.equals("--" + IN) || param.equals("-i")) {
                which = IN;
            } else if (param.equals("--" + OUT) || param.equals("-o")) {
                which = OUT;
            } else if (param.equals("--" + QUIET) || param.equals("-q")) {
                quiet = true;
            } else if (null == which) {
                throw new ReportException("invalid parameter '" + param + "'");
            } else {
                if (JDBC_USER == which)          { jdbcUser = param;
                } else if (JDBC_PASSWORD == which)      { jdbcPassword = param;
                } else if (JDBC_URL == which)           { jdbcURL = param;
                } else if (JDBC_DRIVER == which)        { jdbcDriver = param;
                } else if (JDBC_PROPERTIES == which)    { jdbcProperties = param;
                } else if (IN == which)                 { inPath = param;
                } else if (OUT == which)                { outPath = param;
                } else {
                    throw new Error("bug in command line parameter parsing");
                }
                which = null;
            }
        }

        if (null != jdbcProperties) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(jdbcProperties));
                if (null == jdbcUser) { jdbcUser = props.getProperty(JDBC_USER); }
                if (null == jdbcPassword) { jdbcPassword = props.getProperty(JDBC_PASSWORD); }
                if (null == jdbcURL) { jdbcURL = props.getProperty(JDBC_URL); }
                if (null == jdbcDriver) { jdbcDriver = props.getProperty(JDBC_DRIVER); }
            } catch (Exception e) {
                throw new ReportException("cannot load properties file '" + jdbcProperties + "'\n" + e.getMessage());
            }
        }

        if (null == jdbcUser || null == jdbcPassword || null == jdbcURL || null == jdbcDriver) {
            throw new ReportException("jdbcUser, jdbcPassword, jdbcURL, and jdbcDriver must be provided");
        }

        return true;
    }

    private void go() throws ReportException {
        try {
            Class.forName(jdbcDriver).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ReportException("JDBC driver '" + jdbcDriver + "' cannot be found; check CLASSPATH");
        } catch (InstantiationException e) {
            throw new ReportException("JDBC driver '" + jdbcDriver + "' cannot be instantiated; check CLASSPATH");
        } catch (IllegalAccessException e) {
            throw new ReportException("JDBC driver '" + jdbcDriver + "' cannot be instantiated; check CLASSPATH");
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
        } catch (SQLException e) {
            throw new ReportException("Unable to open db connection to '" + jdbcURL + "'\n" + e.getMessage());
        }
        try {
            StringBuffer query = new StringBuffer();
            if (inPath == null) {
                if (!quiet) {
                    System.err.println("Enter your query, terminate with EOF: ");
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String str = null;
                try {
                    while ((str = in.readLine()) != null) {
                        query.append(str).append('\n');
                    }
                } catch (IOException e) {
                    throw new ReportException("cannot read from stdin\n" + e.getMessage());
                }
                if (!quiet) {
                    System.err.println("Thank you.");
                }
            } else {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new FileReader(inPath));
                    String str = null;
                    while ((str = in.readLine()) != null) {
                        query.append(str).append('\n');
                    }
                } catch (FileNotFoundException e) {
                    throw new ReportException("cannot find input file '" + inPath + "'");
                } catch (IOException e) {
                    throw new ReportException("cannot read input file '" + inPath + "'\n" + e.getMessage());
                } finally {
                    try { in.close(); } catch (IOException e) { }
                }
            }
            runQuery(con, query.toString());
        } finally {
            try { con.close(); } catch (SQLException e) { }
        }
    }

    private void runQuery(Connection con, String query) throws ReportException {
        CsvWriter out = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (outPath == null) {
                out = new CsvWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
            } else {
                File outFile = new File(outPath);
                if (outFile.exists()) {
                    outFile.delete();
                }
                out = new CsvWriter(new BufferedWriter(new FileWriter(outPath)));
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int column_count = md.getColumnCount();
            for (int i = 1; i <= column_count; i++) {
                out.writeField(md.getColumnName(i));
            }
            out.endRecord();
            Object o = null;
            while (rs.next()) {
                for (int i = 1; i <= column_count; i++) {
                    if ((o = rs.getObject(i)) != null) {
                        int colType = md.getColumnType(i);
                        String colClass = md.getColumnClassName(i).toUpperCase();
                        if (o instanceof Number) {
                            out.writeField((Number) o);
                        } else if (colType == Types.DATE || colType == Types.TIMESTAMP || (colClass.indexOf("TIMESTAMP") != -1)) {
                            Timestamp ts = rs.getTimestamp(i);
                            if (null != ts) {
                                //out.writeField(isof.format(rs.getTimestamp(i)));
                                out.writeField((rs.getTimestamp(i)).toString());
                            } else {
                                out.endField();
                            }
                        } else {
                            out.writeField(o.toString());
                        }
                    } else {
                        out.endField();
                    }
                }
                out.endRecord();
            }
        } catch (SQLException e) {
            throw new ReportException("An SQLException has occured\n" + e.getMessage());
        } catch (IOException e) {
            throw new ReportException("An IOException has occured\n" + e.getMessage());
        } finally {
            if (null != stmt) {
                try { stmt.close(); } catch (SQLException e) { }
            }
            if (null != rs) {
                try { rs.close(); } catch (SQLException e) { }
            }
            if (null != out) {
                try { out.close(); } catch (IOException e) { }
            }
        }
    }

    private class ReportException extends Exception {
        ReportException(String message) {
            super(message);
        }
    }

}
