import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

public class Product implements Serializable {
  private static final long serialVersionUID = 1L;

  private String id;          // assigned by ProductCatalog if null/blank
  private String name;
  private double unitPrice;
  private int onHandQty;

  // Product-side waitlist (FIFO) of WaitList entries
  private final Deque<WaitList> waitlist = new ArrayDeque<>();

  public Product(String id, String name, double unitPrice, int onHandQty) {
    this.id = id; this.name = name; this.unitPrice = unitPrice; this.onHandQty = onHandQty;
  }
  public Product(String name, double unitPrice, int onHandQty) {
    this(null, name, unitPrice, onHandQty);
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public double getUnitPrice() { return unitPrice; }
  public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
  public int getOnHandQty() { return onHandQty; }
  public void setOnHandQty(int onHandQty) { this.onHandQty = onHandQty; }

  public void enqueueWaitList(String clientId, int qty) {
    if (qty > 0) waitlist.addLast(new WaitList(clientId, qty));
  }
  public Deque<WaitList> getWaitlist() { return waitlist; }

  @Override
  public String toString() {
    return String.format("%s  %-20s  price:$%.2f  qty:%d", id, name, unitPrice, onHandQty);
  }
}
