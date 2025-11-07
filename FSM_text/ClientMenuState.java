import java.util.*;

public final class ClientMenuState extends WarehouseState { 
  private static ClientMenuState instance;
  private static Context context; 

  // --- Menu command constants ---
  private static final int EXIT = 0;
  private static final int SHOW_DETAILS = 1;
  private static final int SHOW_PRODUCTS = 2;
  private static final int SHOW_TRANSACTIONS = 3;
  private static final int ADD_WISHLIST = 4;
  private static final int SHOW_WISHLIST = 5;
  private static final int PLACE_ORDER = 6;
  private static final int HELP = 9;

  private ClientMenuState() {}

  public static ClientMenuState instance() {
    if (instance == null) instance = new ClientMenuState();
    return instance;
  }
  public static void setContext(Context ctx) { context = ctx; }


  private String getToken(String prompt) { 
    return context.getToken(prompt); 
  }

  private int getNumber(String prompt) { 
    return context.getInt(prompt); 
  }

  // ---------- Menu + loop ----------
  public void help() {
    System.out.println("\nClient commands:");
    System.out.println(EXIT + "  Exit/Logout");
    System.out.println(SHOW_DETAILS + "  Show client details");
    System.out.println(SHOW_PRODUCTS + "  Show products (price)");
    System.out.println(SHOW_TRANSACTIONS + "  Show client transactions");
    System.out.println(ADD_WISHLIST + "  Add item to wishlist");
    System.out.println(SHOW_WISHLIST + "  Display wishlist");
    System.out.println(PLACE_ORDER + "  Place an order");
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
    help();
    int command;
    while ((command = getCommand()) != EXIT) {
      switch (command) {
        case SHOW_DETAILS:       showClientDetails();      break;
        case SHOW_PRODUCTS:      listProducts();           break;
        case SHOW_TRANSACTIONS:  listClientTransactions(); break;
        case ADD_WISHLIST:       addToWishlist();          break;
        case SHOW_WISHLIST:      displayWishlist();        break;
        case PLACE_ORDER:        placeOrder();             break;
        case HELP:               help();                   break;
        default:                 System.out.println("Invalid."); 
      }
    }
    logout();
  }

  @Override public void run() { process(); }

  // ---------- Actions ----------
  private void showClientDetails() {
    String cid = context.getClientId();
    Client c = context.getWarehouse().findClientById(cid);
    if (c == null) { 
      System.out.println("Client not found."); 
      return; 
    }
    System.out.printf("%s  %-20s  %-20s  balance: $%.2f%n",
        c.getId(), c.getName(), c.getAddress(), c.getBalance());
  }

  private void listProducts() {
    Iterator it = context.getWarehouse().getProducts();
    while (it.hasNext()) {
      Product p = (Product) it.next();
      System.out.printf("%s  %-20s  $%.2f%n", p.getId(), p.getName(), p.getUnitPrice());
    }
  }

  private void listClientTransactions() {
    context.getWarehouse().printInvoices(context.getClientId());
  }

  private void addToWishlist() {
    String pid = getToken("Product ID");
    int qty = getNumber("Quantity");
    try {
      WishlistItem item = context.getWarehouse()
          .addToWishlist(context.getClientId(), pid, qty);
      if (item != null) {
        System.out.printf("Added to wishlist: %s x %d%n", pid, item.getQty());
      } else {
        System.out.println("Unable to add to wishlist (check product/quantity).");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private void displayWishlist() {
    Iterator it = context.getWarehouse().getWishlistItems(context.getClientId());
    if (it == null || !it.hasNext()) {
      System.out.println("No wishlist for this client.");
      return;
    }
    while (it.hasNext()) {
      WishlistItem wi = (WishlistItem) it.next();
      System.out.printf("%s x %d (added %s)%n",
          wi.getProductId(), wi.getQty(), wi.getTime());
    }
  }

  private void placeOrder() {
    String cid = context.getClientId();
    Warehouse.Invoice inv = context.getWarehouse().placeOrderFromWishlist(cid);
    if (inv == null) {
      System.out.println("No order placed (wishlist empty or no stock).");
      return;
    }
    System.out.println("Order placed:");
    System.out.print(inv.toString()); 
  }

  private void logout() {
    // Route based on how this client session started
    int entry = context.getEntryRole();
    if (entry == Context.TO_CLERK) {
      context.changeState(Context.TO_CLERK);
    } else {
      context.changeState(Context.TO_LOGIN);
    }
  }
}