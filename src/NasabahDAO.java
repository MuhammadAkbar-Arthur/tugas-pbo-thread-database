import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NasabahDAO {

    // CREATE dengan Transaction (simpan ke tabel nasabah + log_transaksi secara atomik)
    public void create(Nasabah nasabah) throws SQLException {
        String sqlNasabah = "INSERT INTO nasabah (no_rekening, nama, saldo) VALUES (?, ?, ?)";
        String sqlLog = "INSERT INTO log_transaksi (no_rekening, aksi, nominal) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Mulai Transaction

            try (PreparedStatement psNasabah = conn.prepareStatement(sqlNasabah);
                 PreparedStatement psLog = conn.prepareStatement(sqlLog)) {

                psNasabah.setString(1, nasabah.getNoRekening());
                psNasabah.setString(2, nasabah.getNama());
                psNasabah.setDouble(3, nasabah.getSaldo());
                psNasabah.executeUpdate();

                psLog.setString(1, nasabah.getNoRekening());
                psLog.setString(2, "CREATE_NASABAH");
                psLog.setDouble(3, nasabah.getSaldo());
                psLog.executeUpdate();
            }

            conn.commit(); // Commit jika kedua query sukses
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback jika terjadi error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // READ menggunakan PreparedStatement dan ResultSet
    public List<Nasabah> findAll() throws SQLException {
        List<Nasabah> daftar = new ArrayList<>();
        String sql = "SELECT id, no_rekening, nama, saldo FROM nasabah ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftar.add(new Nasabah(
                        rs.getInt("id"),
                        rs.getString("no_rekening"),
                        rs.getString("nama"),
                        rs.getDouble("saldo")
                ));
            }
        }
        return daftar;
    }

    // UPDATE dengan Transaction
    public void update(Nasabah nasabah) throws SQLException {
        String sqlUpdate = "UPDATE nasabah SET no_rekening = ?, nama = ?, saldo = ? WHERE id = ?";
        String sqlLog = "INSERT INTO log_transaksi (no_rekening, aksi, nominal) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                 PreparedStatement psLog = conn.prepareStatement(sqlLog)) {

                psUpdate.setString(1, nasabah.getNoRekening());
                psUpdate.setString(2, nasabah.getNama());
                psUpdate.setDouble(3, nasabah.getSaldo());
                psUpdate.setInt(4, nasabah.getId());
                psUpdate.executeUpdate();

                psLog.setString(1, nasabah.getNoRekening());
                psLog.setString(2, "UPDATE_NASABAH");
                psLog.setDouble(3, nasabah.getSaldo());
                psLog.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // DELETE dengan Transaction
    public void delete(int id, String noRekening) throws SQLException {
        String sqlDelete = "DELETE FROM nasabah WHERE id = ?";
        String sqlLog = "INSERT INTO log_transaksi (no_rekening, aksi, nominal) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete);
                 PreparedStatement psLog = conn.prepareStatement(sqlLog)) {

                psDelete.setInt(1, id);
                psDelete.executeUpdate();

                psLog.setString(1, noRekening);
                psLog.setString(2, "DELETE_NASABAH");
                psLog.setDouble(3, 0);
                psLog.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}