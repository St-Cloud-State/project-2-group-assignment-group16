import java.util.List;
import java.util.Scanner;

public class UserInterface {
  private static final int EXIT              = 0;
  private static final int ADD_CLIENT        = 1;
  private static final int ADD_PRODUCT       = 2;
  private static final int ADD_TO_WISHLIST   = 3;
  private static final int SHOW_CLIENTS      = 4;
  private static final int SHOW_PRODUCTS     = 5;
  private static final int SHOW_WISHLIST     = 6;
  private static final int SAVE              = 7;
  private static final int RETRIEVE          = 8;
  private static final int HELP              = 9;
  private static final int PLACE_ORDER       = 10;
  private static final int RECEIVE_SHIPMENT  = 11;
  private static final int RECORD_PAYMENT    = 12;
  private static final int PRINT_INVOICES    = 13;
  private static final int PRINT_WAITLIST    = 14;

  private static UserInterface ui;
  private final Warehouse warehouse = Warehouse.instance();
  private final Scanner in = new Scanner(System.in);

  public static UserInterface instance() {
    if (ui == null) ui = new UserInterface();
    return ui;
  }

  private UserInterface() {}

  private void help() {
    System.out.println("Enter a number between 0 and 14 as explained below:");
    System.out.println(EXIT + "  Exit");
    System.out.println(ADD_CLIENT + "  Add client");
    System.out.println(ADD_PRODUCT + "  Add product");
    System.out.println(ADD_TO_WISHLIST + "  Add item to a client's wishlist");
    System.out.println(SHOW_CLIENTS + "  Show all clients");
    System.out.println(SHOW_PRODUCTS + "  Show all products");
    System.out.println(SHOW_WISHLIST + "  Show a client's wishlist");
    System.out.println(SAVE + "  Save");
    System.out.println(RETRIEVE + "  Retrieve");
    System.out.println(HELP + "  Help");
    System.out.println(PLACE_ORDER + "  Place order from client's wishlist");
    System.out.println(RECEIVE_SHIPMENT + "  Receive product shipment (fills waitlist first)");
    System.out.println(RECORD_PAYMENT + "  Record payment from client");
    System.out.println(PRINT_INVOICES + "  Print a client's invoices");
    System.out.println(PRINT_WAITLIST + "  Print a product's waitlist");
  }

  // --- Scanner I/O helpers (replace your old Keyboard.*)
  private String getToken(String prompt) {
    System.out.print(prompt + ": ");
    String s = in.nextLine();
    return s == null ? "" : s.trim();
  }
  private int getNumber(String prompt) {
    while (true) {
      System.out.print(prompt + ": ");
      String s = in.nextLine();
      try { return Integer.parseInt(s.trim()); } catch (Exception e) { System.out.println("Enter an integer."); }
    }
  }
  private double getDouble(String prompt) {
    while (true) {
      System.out.print(prompt + ": ");
      String s = in.nextLine();
      try { return Double.parseDouble(s.trim()); } catch (Exception e) { System.out.println("Enter a number."); }
    }
  }
  private boolean yesOrNo(String prompt) {
    while (true) {
      System.out.print(prompt + " (y/n): ");
      String s = in.nextLine().trim().toLowerCase();
      if (s.equals("y") || s.equals("yes")) return true;
      if (s.equals("n") || s.equals("no")) return false;
    }
  }

  private void addClient() {
    String name = getToken("Client name");
    String addr = getToken("Client address");
    Client c = warehouse.addClient(name, addr);
    System.out.println("Added: " + c);
  }
  private void addProduct() {
    String id = getToken("Product ID (leave blank to auto-assign)");
    if (id.isBlank()) id = null;
    String name = getToken("Product name");
    double price = getDouble("Unit price");
    int qty = getNumber("Initial qty");
    Product p = (id == null) ? warehouse.addProduct(name, price, qty)
                             : warehouse.addProduct(id, name, price, qty);
    System.out.println("Added: " + p);
  }
  private void addWishlistItem() {
    String cid = getToken("Client ID");
    String pid = getToken("Product ID");
    int qty = getNumber("Qty");
    WishlistItem wi = warehouse.addToWishlist(cid, pid, qty);
    System.out.println(wi != null ? "Wishlist updated." : "Failed to add to wishlist.");
  }

  private void showClients() { warehouse.printAllClients(); }
  private void showProducts() { warehouse.printAllProducts(); }
  private void showWishlist() {
    String cid = getToken("Client ID");
    warehouse.printWishlist(cid);
  }

  private void placeOrder() {
    if (yesOrNo("Show clients first?")) showClients();
    String cid = getToken("Client ID");
    Warehouse.Invoice inv = warehouse.placeOrderFromWishlist(cid);
    if (inv == null) System.out.println("No items shipped (wishlist empty or all backordered).");
    else { System.out.println("Created invoice:"); System.out.print(inv.toString()); }
  }
  private void receiveShipment() {
    if (yesOrNo("Show products first?")) showProducts();
    String pid = getToken("Product ID");
    int qty = getNumber("Qty received");
    List<Warehouse.Invoice> invs = warehouse.receiveShipment(pid, qty);
    if (invs.isEmpty()) System.out.println("No invoices generated; stock updated if product exists.");
    else {
      System.out.println("Generated invoices:");
      for (Warehouse.Invoice inv : invs) {
        System.out.printf("(Client: %s)%n", inv.getClientId());
        System.out.print(inv.toString());
      }
    }
  }
  private void recordPayment() {
    if (yesOrNo("Show clients first?")) showClients();
    String cid = getToken("Client ID");
    double amt = getDouble("Payment amount");
    boolean ok = warehouse.recordPayment(cid, amt);
    System.out.println(ok ? "Payment recorded." : "Payment failed.");
  }
  private void printInvoices() {
    if (yesOrNo("Show clients first?")) showClients();
    String cid = getToken("Client ID");
    warehouse.printInvoices(cid);
  }
  private void printWaitlist() {
    if (yesOrNo("Show products first?")) showProducts();
    String pid = getToken("Product ID");
    warehouse.printWaitlist(pid);
  }

  private void save() {
    boolean ok = Warehouse.save();
    System.out.println(ok ? "Saved." : "Save failed.");
  }
  private void retrieve() {
    Warehouse w = Warehouse.retrieve();
    System.out.println(w != null ? "Retrieved." : "Retrieve failed.");
  }

  public void process() {
    help();
    int command;
    do {
      command = getNumber("Enter command");
      switch (command) {
        case EXIT: break;
        case ADD_CLIENT: addClient(); break;
        case ADD_PRODUCT: addProduct(); break;
        case ADD_TO_WISHLIST: addWishlistItem(); break;
        case SHOW_CLIENTS: showClients(); break;
        case SHOW_PRODUCTS: showProducts(); break;
        case SHOW_WISHLIST: showWishlist(); break;
        case SAVE: save(); break;
        case RETRIEVE: retrieve(); break;
        case HELP: help(); break;

        case PLACE_ORDER: placeOrder(); break;
        case RECEIVE_SHIPMENT: receiveShipment(); break;
        case RECORD_PAYMENT: recordPayment(); break;
        case PRINT_INVOICES: printInvoices(); break;
        case PRINT_WAITLIST: printWaitlist(); break;

        default: System.out.println("Invalid command"); break;
      }
    } while (command != EXIT);
    System.out.println("Goodbye.");
  }

  public static void main(String[] args) { UserInterface.instance().process(); }
}
