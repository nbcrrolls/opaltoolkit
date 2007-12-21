/* Useless class does not work!!!!!    */

import java.sql.*;
import java.io.*;


public class Main {
    
    static PrintStream out = System.out;
    
    
    public static void main(String []argv) {
        String queryGetDirty = "select job_id from job_status where start_time ~ '^[0-9][0-9]'";
        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection( "jdbc:postgresql://localhost/app_db", "app_user", "app_user");
            our.println("Connection to the database established");
            Statement sql = conn.createStatement("job_id");
            ResultSet rs = sql.executeQuery(queryGetDirty);
            FileOutputStream file = new FileOutputStream("temp.txt");
            PrintStream fileout = new PrintStream(file);
            for (; rs.next(); ) {
                String job_id = rs.getString("job_id");
                out.println("Modifying the element: " + job_id );
                String updateQuery = "UPDATE job_status " +
                		"SET start_time = to_char( to_timestamp(start_time, 'YYYY/MM/DD HH24:MI:SS') , 'Mon FMDD, YYYY FMHH:MI:SS AM')" +
                		"WHERE job_id = '" + job_id + "';";
                Statement sql = conn.createStatement("update");
                ResultSet rs = sql.executeUpdate(updateQuery);
                out.println("Updated the line!");
                
            }


            return true;
        } catch (Exception e) {
            out.println("Cougth an exception");
            e.printStackTrace();
            error = e.getMessage();
            return false;
        }

    }
}



