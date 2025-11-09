import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client implements Serializable {
  private static final long serialVersionUID = 1L;

  private String id;        // assigned by ClientIdServer/ClientList
  private String name;
  private String address;

  private Wishlist wishlist = new Wishlist(); // your existing class

  // Accounting + invoices (Invoices live as Warehouse.Invoice)
  private double balance = 0.0;
  private final List<Warehouse.Invoice> invoices = new ArrayList<>();

  public Client(String name, String address) {
    this.id = ClientIdServer.instance().nextId();
    this.name = name;
    this.address = address;
    this.wishlist = new Wishlist();
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getName() { return name; }
  public String getAddress() { return address; }
  public void setName(String n) { this.name = n; }
  public void setAddress(String a) { this.address = a; }

  public Wishlist getWishlist() { return wishlist; }

  public double getBalance() { return balance; }
  public void debit(double amt) { balance += amt; }   // invoice raises balance
  public void credit(double amt) { balance -= amt; }  // payment lowers balance

  public void addInvoice(Warehouse.Invoice inv) { invoices.add(inv); }
  public List<Warehouse.Invoice> getInvoices() {
    return Collections.unmodifiableList(invoices);
  }

  @Override public String toString() {
    return String.format("%s  %-20s  balance: $%.2f", id, name, balance);
  }
}
