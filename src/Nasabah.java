public class Nasabah {
    private int id;
    private String noRekening;
    private String nama;
    private double saldo;

    public Nasabah(String noRekening, String nama, double saldo) {
        this.noRekening = noRekening;
        this.nama = nama;
        this.saldo = saldo;
    }

    public Nasabah(int id, String noRekening, String nama, double saldo) {
        this.id = id;
        this.noRekening = noRekening;
        this.nama = nama;
        this.saldo = saldo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoRekening() { return noRekening; }
    public void setNoRekening(String noRekening) { this.noRekening = noRekening; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}