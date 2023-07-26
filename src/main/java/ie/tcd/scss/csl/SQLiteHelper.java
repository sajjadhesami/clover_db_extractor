package ie.tcd.scss.csl;

import java.sql.*;

public class SQLiteHelper {
    private String url;

    public SQLiteHelper(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    public int getRecordId(String file_name) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int id = -1;

        try {
            // Connect to the database
            conn = DriverManager.getConnection(url);

            // Create and execute the query
            String sql = "SELECT id FROM file WHERE name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, file_name);
            rs = pstmt.executeQuery();

            // Get the result
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return id;
    }

    public void insertRecord(int file_id, int line_number, String test_name) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Connect to the database
            conn = DriverManager.getConnection(url);

            // Create and execute the query
            String sql = "INSERT INTO test_coverage(file_id, line_no, qualified_name) VALUES(?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, file_id);
            pstmt.setInt(2, line_number);
            pstmt.setString(3, test_name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void deleteAllRecords() {
        Connection conn = null;
        Statement stmt = null;

        try {
            // Connect to the database
            conn = DriverManager.getConnection(url);

            // Create and execute the query
            String sql = "DELETE FROM test_coverage";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
