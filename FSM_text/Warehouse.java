import java.io.*;
import java.util.*;

/** Central service. Uses ClientList.instance() and ProductCatalog.instance(). */
public class Warehouse implements Serializable {
  private static final long serialVersionUID = 1L;

  // =================== Invoice and InvoiceLine classes ===================
  public static class InvoiceLine implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String productId;
    private final int qty;
    private final double unitPrice;
    public InvoiceLine(String productId, int qty, double unitPrice) {
      this.productId = productId; this.qty = qty; this.unitPrice = unitPrice;
    }
    public String getProductId() { return productId; }
    public int getQty() { return qty; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return qty * unitPrice; }
    @Override public String toString() {
      return String.format("%s x %d @ %.2f = %.2f", productId, qty, unitPrice, getLineTotal());
    }
  }

  public static class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String clientId;
    private final Date created = new Date();
    private final List<InvoiceLine> lines = new ArrayList<>();
    public Invoice(String id, String clientId) { this.id = id; this.clientId = clientId; }
    public String getId() { return id; }
    public String getClientId() { return clientId; }
    public Date getCreated() { return created; }
    public List<InvoiceLine> getLines() { return Collections.unmodifiableList(lines); }
    public void addLine(String productId, int qty, double unitPrice) {
      if (qty > 0) lines.add(new InvoiceLine(productId, qty, unitPrice));
    }
    public double getTotal() { double s=0; for (InvoiceLine l:lines) s+=l.getLineTotal(); return s; }
    @Override public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%s  %tF  $%.2f%n", id, created, getTotal()));
      for (InvoiceLine l : lines) sb.append("  ").append(l.toString()).append('\n');
      return sb.toString();
    }
  }
  // ===================================================================

  private static Warehouse instance;
  public static Warehouse instance() {
    if (instance == null) instance = new Warehouse();
    return instance;
  }

  private final ClientList clientList = ClientList.instance();
  private final ProductCatalog productCatalog = ProductCatalog.instance();

  private int invoiceSeq = 1;
  private String nextInvoiceId() { return "INV" + (invoiceSeq++); }

  private Warehouse() {}

  // ============= Lookups using helpers =============
  public Client findClientById(String clientId) { return clientList.get(clientId); }
  public Product findProductById(String productId) { return productCatalog.get(productId); }

  // ============= Iterators for tests ====================
  public Iterator getClients() { return clientList.getClients(); }
  public Iterator getProducts() { return productCatalog.getProducts(); }
  public Iterator getWishlistItems(String clientId) {
    Client c = findClientById(clientId);
    return (c == null) ? Collections.emptyIterator() : c.getWishlist().getItems();
  }

  // ============= Create ops (both addProduct overloads) =============
  public Client addClient(String name, String address) {
    Client c = new Client(name, address);
    clientList.insertClient(c);
    return c;
  }
  public Product addProduct(String id, String name, double unitPrice, int qty) {
    Product p = new Product(id, name, unitPrice, qty);
    productCatalog.insertProduct(p);
    return p;
  }
  public Product addProduct(String name, double unitPrice, int qty) {
    // id will be auto-assigned by ProductCatalog.insertProduct(...)
    return addProduct(null, name, unitPrice, qty);
  }

  // ============= Wishlist mutation ======================
  /** Returns the created WishlistItem, or null on failure. */
  public WishlistItem addToWishlist(String clientId, String productId, int qty) {
    Client c = findClientById(clientId);
    Product p = findProductById(productId);
    if (c == null || p == null || qty <= 0) return null;
    return c.getWishlist().addOrUpdate(productId, qty);
  }

  // =================== FINAL IMPLEMENTATION CORE ===================

  /** Place order from a client's wishlist.
   * Ships immediately available quantities (creates one Invoice).
   * Shortfalls move to each Product's BackOrder waitlist.
   * Clears the client's wishlist after processing.
   */
  public Invoice placeOrderFromWishlist(String clientId) {
    Client client = findClientById(clientId);
    if (client == null) return null;
    Wishlist wl = client.getWishlist();
    if (wl == null || wl.isEmpty()) return null;

    Invoice inv = new Invoice(nextInvoiceId(), clientId);

    for (Iterator it = wl.getItems(); it.hasNext();) {
      WishlistItem wi = (WishlistItem) it.next();
      String pid = wi.getProductId();
      int wanted = wi.getQty();
      if (wanted <= 0) continue;

      Product p = findProductById(pid);
      if (p == null) continue;

      int shipNow = Math.min(wanted, p.getOnHandQty());
      int WaitList = wanted - shipNow;

      if (shipNow > 0) {
        p.setOnHandQty(p.getOnHandQty() - shipNow);
        inv.addLine(pid, shipNow, p.getUnitPrice());
      }
      if (WaitList > 0) {
        p.enqueueWaitList(clientId, WaitList);
      }
    }

    // clear wishlist after processing
    wl.clear();

    if (!inv.getLines().isEmpty()) {
      client.addInvoice(inv);
      client.debit(inv.getTotal());
      return inv;
    }
    return null; // nothing shipped now
  }

  /** Receive shipment for a product: fill WaitLists FIFO first, generating invoices per client; leftover goes to stock. */
  public List<Invoice> receiveShipment(String productId, int qtyReceived) {
    Product p = findProductById(productId);
    if (p == null || qtyReceived <= 0) return Collections.emptyList();

    int remaining = qtyReceived;
    List<Invoice> generated = new ArrayList<>();
    Deque<WaitList> q = p.getWaitlist();

    while (remaining > 0 && !q.isEmpty()) {
      WaitList bo = q.peekFirst();
      int fulfill = Math.min(remaining, bo.getQty());
      if (fulfill <= 0) break;

      Invoice inv = new Invoice(nextInvoiceId(), bo.getClientId());
      inv.addLine(productId, fulfill, p.getUnitPrice());
      generated.add(inv);

      Client c = findClientById(bo.getClientId());
      if (c != null) {
        c.addInvoice(inv);
        c.debit(inv.getTotal());
      }

      bo.consume(fulfill);
      remaining -= fulfill;
      if (bo.cleared()) q.removeFirst();
    }

    if (remaining > 0) p.setOnHandQty(p.getOnHandQty() + remaining);
    return generated;
  }

  /** Record a payment from a client. */
  public boolean recordPayment(String clientId, double amount) {
    if (amount <= 0) return false;
    Client c = findClientById(clientId);
    if (c == null) return false;
    c.credit(amount);
    return true;
  }

  // ========================= Reports =========================
  public void printAllClients() {
    for (Iterator it = clientList.getClients(); it.hasNext();) {
      Client c = (Client) it.next();
      System.out.printf("%s  %-20s  balance: $%.2f%n", c.getId(), c.getName(), c.getAddress(), c.getBalance());
    }
  }
  public void printAllProducts() {
    for (Iterator it = productCatalog.getProducts(); it.hasNext();) {
      Product p = (Product) it.next();
      System.out.printf("%s  %-20s  price:$%.2f  qty:%d%n",
          p.getId(), p.getName(), p.getUnitPrice(), p.getOnHandQty());
    }
  }
  public void printWishlist(String clientId) {
    Client c = findClientById(clientId);
    if (c == null) { System.out.println("No such client"); return; }
    System.out.println("Wishlist for " + clientId + ":");
    Wishlist wl = c.getWishlist();
    for (Iterator it = wl.getItems(); it.hasNext();) {
      WishlistItem wi = (WishlistItem) it.next();
      System.out.printf("  %s x %d%n", wi.getProductId(), wi.getQty());
    }
  }
  public void printWaitlist(String productId) {
    Product p = findProductById(productId);
    if (p == null) { System.out.println("No such product"); return; }
    System.out.println("Waitlist for " + productId + ":");
    for (WaitList bo : p.getWaitlist()) {
      System.out.printf("  %s x %d%n", bo.getClientId(), bo.getQty());
    }
  }
  public void printInvoices(String clientId) {
    Client c = findClientById(clientId);
    if (c == null) { System.out.println("No such client"); return; }
    System.out.println("Invoices for " + clientId + ":");
    for (Invoice inv : c.getInvoices()) System.out.print(inv.toString());
  }

  // ===================== Persistence (static) =====================
  private static final String DATA_FILE = "WarehouseData.ser";

  public static boolean save() {
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
      out.writeObject(instance());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static Warehouse retrieve() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
      Warehouse loaded = (Warehouse) in.readObject();
      instance = loaded; // reset singleton to loaded instance
      return loaded;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Expose lists if needed by tests/UI
  public ClientList getClientList() { return clientList; }
  public ProductCatalog getProductCatalog() { return productCatalog; }
}
