package ie.tcd.scss.csl;

import java.sql.*;

public class SQLiteHelper {
    private String url;

    public SQLiteHelper(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    Connection conn = null;

    public boolean connect() {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        try {
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean disconnect() {
        try {
            conn.commit();
            conn.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getRecordId(String file_name) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int id = -1;

        try {
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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return id;
    }

    public void insertTestRecord(int test_id, String qualifiedname) {
        PreparedStatement pstmt = null;

        try {
            // Create and execute the query
            String sql = "INSERT INTO test(id, qualified_name) VALUES(?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, test_id);
            pstmt.setString(2, qualifiedname);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void insertCoverageRecord(int file_id, int line_number, int test_id) {
        PreparedStatement pstmt = null;
        try {
            // Create and execute the query
            String sql = "INSERT INTO test_coverage(file_id, line_no, test_id) VALUES(?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, file_id);
            pstmt.setInt(2, line_number);
            pstmt.setInt(3, test_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteAllTestRecords() {
        Statement stmt = null;

        try {
            // Create and execute the query
            String sql = "DELETE FROM test";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public void deleteAllCoverageRecords() {
        Statement stmt = null;

        try {

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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
