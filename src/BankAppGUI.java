import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BankAppGUI extends JFrame {
    private final NasabahDAO dao = new NasabahDAO();

    private JTextField tfNoRek, tfNama, tfSaldo;
    private JButton btnSimpan, btnUpdate, btnHapus, btnRefresh, btnReset;
    private JLabel lblStatus;
    private JTable tableNasabah;
    private DefaultTableModel tableModel;

    private int selectedId = -1;

    public BankAppGUI() {
        setTitle("Bank Mini CRUD - Java Thread & JDBC (Laragon)");
        setSize(720, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel Form Input (NORTH)
        JPanel panelForm = new JPanel(new GridLayout(3, 2, 8, 8));
        panelForm.setBorder(BorderFactory.createTitledBorder("Data Nasabah"));

        tfNoRek = new JTextField();
        tfNama = new JTextField();
        tfSaldo = new JTextField();

        panelForm.add(new JLabel("No. Rekening:"));
        panelForm.add(tfNoRek);
        panelForm.add(new JLabel("Nama Nasabah:"));
        panelForm.add(tfNama);
        panelForm.add(new JLabel("Saldo (Rp):"));
        panelForm.add(tfSaldo);

        // Panel Tombol Aksi
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnSimpan = new JButton("Simpan");
        btnUpdate = new JButton("Update");
        btnHapus = new JButton("Hapus");
        btnRefresh = new JButton("Refresh");
        btnReset = new JButton("Reset Form");

        panelTombol.add(btnSimpan);
        panelTombol.add(btnUpdate);
        panelTombol.add(btnHapus);
        panelTombol.add(btnRefresh);
        panelTombol.add(btnReset);

        JPanel panelAtas = new JPanel(new BorderLayout(5, 5));
        panelAtas.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelAtas.add(panelForm, BorderLayout.CENTER);
        panelAtas.add(panelTombol, BorderLayout.SOUTH);

        // 2. Tabel Data Nasabah (CENTER)
        tableModel = new DefaultTableModel(new String[]{"ID", "No. Rekening", "Nama Nasabah", "Saldo (Rp)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableNasabah = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableNasabah);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Nasabah (MySQL Database)"));

        // 3. Status Bar (SOUTH)
        lblStatus = new JLabel("Status: Ready");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblStatus.setForeground(new Color(0, 102, 204));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 12, 10, 12));

        add(panelAtas, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        // Listener klik baris tabel untuk mengisi form (Update / Delete)
        tableNasabah.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableNasabah.getSelectedRow() != -1) {
                int row = tableNasabah.getSelectedRow();
                selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                tfNoRek.setText(tableModel.getValueAt(row, 1).toString());
                tfNama.setText(tableModel.getValueAt(row, 2).toString());
                tfSaldo.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        // Event Handler Tombol
        btnSimpan.addActionListener(e -> handleSimpan());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnHapus.addActionListener(e -> handleHapus());
        btnRefresh.addActionListener(e -> loadDataAsync("Loading data..."));
        btnReset.addActionListener(e -> resetForm());

        // Muat data awal saat jendela dibuka
        loadDataAsync("Loading initial data...");
    }

    private void setControlsEnabled(boolean enabled) {
        btnSimpan.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnHapus.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
    }

    private void resetForm() {
        selectedId = -1;
        tfNoRek.setText("");
        tfNama.setText("");
        tfSaldo.setText("");
        tableNasabah.clearSelection();
    }

    private void updateTable(List<Nasabah> list) {
        tableModel.setRowCount(0);
        for (Nasabah n : list) {
            tableModel.addRow(new Object[]{n.getId(), n.getNoRekening(), n.getNama(), n.getSaldo()});
        }
    }

    // READ: DB on Thread -> invokeLater -> GUI
    private void loadDataAsync(String loadingText) {
        setControlsEnabled(false);
        lblStatus.setText(loadingText);

        new Thread(() -> {
            try {
                Thread.sleep(600); // Simulasi latensi DB agar indikator thread terlihat jelas
                List<Nasabah> data = dao.findAll();

                SwingUtilities.invokeLater(() -> {
                    updateTable(data);
                    lblStatus.setText("Status: Data dimuat (" + data.size() + " nasabah).");
                    setControlsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Gagal terhubung ke database!");
                    setControlsEnabled(true);
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // CREATE: Click Handler -> Disable Button -> Show "Saving..." -> Thread -> invokeLater -> GUI
    private void handleSimpan() {
        String noRek = tfNoRek.getText().trim();
        String nama = tfNama.getText().trim();
        String saldoStr = tfSaldo.getText().trim();

        if (noRek.isEmpty() || nama.isEmpty() || saldoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua kolom wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double saldo;
        try {
            saldo = Double.parseDouble(saldoStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Saldo harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        lblStatus.setText("Saving...");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulasi delay 1 detik agar status "Saving..." bisa di-screenshot
                dao.create(new Nasabah(noRek, nama, saldo));
                List<Nasabah> updatedList = dao.findAll();

                SwingUtilities.invokeLater(() -> {
                    updateTable(updatedList);
                    resetForm();
                    lblStatus.setText("Status: Data berhasil disimpan!");
                    setControlsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Gagal menyimpan data!");
                    setControlsEnabled(true);
                    JOptionPane.showMessageDialog(this, "Error Simpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // UPDATE: Click Handler -> Disable Button -> Show "Updating..." -> Thread -> invokeLater -> GUI
    private void handleUpdate() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris nasabah pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String noRek = tfNoRek.getText().trim();
        String nama = tfNama.getText().trim();
        double saldo;
        try {
            saldo = Double.parseDouble(tfSaldo.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Saldo harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int targetId = selectedId;
        setControlsEnabled(false);
        lblStatus.setText("Updating...");

        new Thread(() -> {
            try {
                Thread.sleep(800);
                dao.update(new Nasabah(targetId, noRek, nama, saldo));
                List<Nasabah> updatedList = dao.findAll();

                SwingUtilities.invokeLater(() -> {
                    updateTable(updatedList);
                    resetForm();
                    lblStatus.setText("Status: Data berhasil diperbarui!");
                    setControlsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Gagal memperbarui data!");
                    setControlsEnabled(true);
                    JOptionPane.showMessageDialog(this, "Error Update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // DELETE: Click Handler -> Disable Button -> Show "Deleting..." -> Thread -> invokeLater -> GUI
    private void handleHapus() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris nasabah yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int targetId = selectedId;
        String targetRek = tfNoRek.getText().trim();
        setControlsEnabled(false);
        lblStatus.setText("Deleting...");

        new Thread(() -> {
            try {
                Thread.sleep(800);
                dao.delete(targetId, targetRek);
                List<Nasabah> updatedList = dao.findAll();

                SwingUtilities.invokeLater(() -> {
                    updateTable(updatedList);
                    resetForm();
                    lblStatus.setText("Status: Data berhasil dihapus!");
                    setControlsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Gagal menghapus data!");
                    setControlsEnabled(true);
                    JOptionPane.showMessageDialog(this, "Error Hapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}