import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion_DB {
    private static final String HOST="bdbsjb7v8o8wa0pot4lt-mysql.services.clever-cloud.com";
    private static final String DB="bdbsjb7v8o8wa0pot4lt";
    private static final String USER="uspitlplqxwpi1ft";
    private static final String PORT="3306";
    private static final String PASSWORD="vDVAQEeEPO9pvXHzs0ih";
    private static final String URL = "jdbc:mysql://"+HOST+":"+PORT+"/"+DB; //DIRECCIONAMIENTO DE LA BASE DE DATOS PARA CONEXION EN LA NUBE



    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
