import java.util.*;

public final class ManagerMenuState extends WarehouseState {
  private static ManagerMenuState instance;
  private static Context context;

  // --- Menu command constants ---
  private static final int EXIT = 0;
  private static final int ADD_PRODUCT = 1;
  private static final int SHOW_WAITLIST = 2;
  private static final int RECEIVE_SHIPMENT = 3;
  private static final int BECOME_CLERK = 4;
  private static final int HELP = 9;

  private ManagerMenuState() {}
  public static ManagerMenuState instance() {
    if (instance == null) instance = new ManagerMenuState();
    return instance;
  }
  public static void setContext(Context ctx) { context = ctx; }

  private String getToken(String prompt) { return context.getToken(prompt); }
  private int getNumber(String prompt)   { return context.getInt(prompt);   }
  private double getDouble(String prompt) {
    while (true) {
      try { return Double.parseDouble(getToken(prompt)); }
      catch (NumberFormatException e) { System.out.println("Enter a number."); }
    }
  }

  // ---------- Menu + loop ----------
  public void help() {
    System.out.println("\nManager commands:");
    System.out.println(EXIT + "  Logout");
    System.out.println(ADD_PRODUCT + "  Add product");
    System.out.println(SHOW_WAITLIST + "  Display waitlist for a product");
    System.out.println(RECEIVE_SHIPMENT + "  Receive a shipment");
    System.out.println(BECOME_CLERK + "  Become a clerk");
    System.out.println(HELP + "  Help");
  }

  private int getCommand() {
    while (true) {
      try {
        int cmd = Integer.parseInt(getToken("Enter command (9 for help)"));
        if (cmd >= 0 && cmd <= 9) return cmd;
      } catch (NumberFormatException ignore) {}
      System.out.println("Enter a number from the menu.");
    }
  }

  public void process() {
    help(); // show the Manager menu immediately on entry
    int command;
    while ((command = getCommand()) != EXIT) {
      switch (command) {
        case ADD_PRODUCT:
          addProduct();
          break;
        case SHOW_WAITLIST:
          displayWaitlistForProduct();
          break;
        case RECEIVE_SHIPMENT:
          receiveShipment();
          break;
        case BECOME_CLERK:
          becomeClerk();
          return; // IMPORTANT: stop Manager loop so Clerk can take over immediately
        case HELP:
          help();
          break;
        default:
          System.out.println("Invalid.");
      }
    }
    // EXIT selected -> logout to Login (per assignment)
    logout();
  }

  @Override public void run() { process(); }

  // ---------- Actions ----------
  private void addProduct() {
    // Your spec demo requires P1..P5 to be creatable explicitly, so accept ID (blank = auto).
    String id = getToken("Product ID (leave blank to auto-assign)");
    if (id != null && id.isBlank()) id = null;
    String name = getToken("Name/Description");
    double price = getDouble("Unit price");
    int qty = getNumber("Initial quantity");

    try {
      Product p = (id == null)
          ? context.getWarehouse().addProduct(name, price, qty)
          : context.getWarehouse().addProduct(id, name, price, qty);
      if (p != null) {
        System.out.printf("Product added: %s  %-20s  price:$%.2f  qty:%d%n",
            p.getId(), p.getName(), p.getUnitPrice(), p.getOnHandQty());
      } else {
        System.out.println("Unable to add product (duplicate id?).");
      }
    } catch (Exception e) {
      System.out.println("Error adding product: " + e.getMessage());
    }
  }

  private void displayWaitlistForProduct() {
    String pid = getToken("Product ID");
    if (context.getWarehouse().findProductById(pid) == null) {
      System.out.println("No such product.");
      return;
    }
    // Use Warehouse reporter to avoid exposing internals
    context.getWarehouse().printWaitlist(pid);
  }

  private void receiveShipment() {
    String pid = getToken("Product ID");
    int qty = getNumber("Quantity received");

    try {
      List<Warehouse.Invoice> invs = context.getWarehouse().receiveShipment(pid, qty);
      System.out.println("Shipment processed for " + pid + " (qty " + qty + ").");
      if (invs == null || invs.isEmpty()) {
        System.out.println("No invoices generated; stock updated if product exists.");
        return;
      }
      System.out.println("Generated invoices:");
      for (Warehouse.Invoice inv : invs) {
        System.out.printf("(Client: %s)%n", inv.getClientId());
        System.out.print(inv.toString());
      }
    } catch (Exception e) {
      System.out.println("Error receiving shipment: " + e.getMessage());
    }
  }

  private void becomeClerk() {
    // Do not modify entryRole; Context already knows we started as Manager
    context.changeState(Context.TO_CLERK);
  }

  private void logout() {
    // Per spec, Manager logout returns to Login
    context.changeState(Context.TO_LOGIN);
  }
}
