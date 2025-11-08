import java.util.*;

public final class ClerkMenuState extends WarehouseState {
  private static ClerkMenuState instance;
  private static Context context;

  // --- Menu command constants ---
  private static final int EXIT = 0;
  private static final int ADD_CLIENT = 1;
  private static final int SHOW_PRODUCTS = 2;
  private static final int SHOW_CLIENTS = 3;
  private static final int SHOW_CLIENTS_WITH_BALANCE = 4;
  private static final int RECORD_PAYMENT = 5;
  private static final int BECOME_CLIENT = 6;
  private static final int HELP = 9;

  private ClerkMenuState() {}
  public static ClerkMenuState instance() {
    if (instance == null) instance = new ClerkMenuState();
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
  private void help() {
    System.out.println();
    System.out.println("Clerk commands:");
    System.out.println(EXIT + "  Logout");
    System.out.println(ADD_CLIENT + "  Add client");
    System.out.println(SHOW_PRODUCTS + "  Show products (qty & price)");
    System.out.println(SHOW_CLIENTS + "  Show all clients");
    System.out.println(SHOW_CLIENTS_WITH_BALANCE + "  Show clients with outstanding balance");
    System.out.println(RECORD_PAYMENT + "  Record payment from client");
    System.out.println(BECOME_CLIENT + "  Become a client (enter ClientID)");
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
    // Print the Clerk menu immediately upon entering this state
    help();

    int command;
    while ((command = getCommand()) != EXIT) {
      switch (command) {
        case ADD_CLIENT:                addClient();                         break;
        case SHOW_PRODUCTS:             showAllProducts();                   break;
        case SHOW_CLIENTS:              showAllClients();                    break;
        case SHOW_CLIENTS_WITH_BALANCE: showClientsWithOutstandingBalance(); break;
        case RECORD_PAYMENT:            recordPayment();                     break;
        case BECOME_CLIENT:             becomeClient();                      break;
        case HELP:                      help();                              break;
        default:                        System.out.println("Invalid.");
      }
    }
    logout();
  }

  @Override public void run() { process(); }

  // ---------- Actions ----------
  private void addClient() {
    String name = getToken("Client name");
    String address = getToken("Client address");
    try {
      Client c = context.getWarehouse().addClient(name, address);
      if (c != null) {
        System.out.printf("Client added: %s  %-20s  balance: $%.2f%n",
            c.getId(), c.getName(), c.getBalance());
      } else {
        System.out.println("Unable to add client.");
      }
    } catch (Exception e) {
      System.out.println("Error adding client: " + e.getMessage());
    }
  }

  private void showAllProducts() {
    // Use your Warehouse reporter to keep output consistent
    context.getWarehouse().printAllProducts();
  }

  private void showAllClients() {
    context.getWarehouse().printAllClients();
  }

  private void showClientsWithOutstandingBalance() {
    Iterator it = context.getWarehouse().getClients();
    boolean any = false;
    while (it != null && it.hasNext()) {
      Client c = (Client) it.next();
      if (c.getBalance() > 0.0) {
        if (!any) {
          System.out.println("Clients with outstanding balance:");
          any = true;
        }
        System.out.printf("%-4s %-20s balance: $%.2f%n", c.getId(), c.getName(), c.getBalance());
      }
    }
    if (!any) System.out.println("No clients with outstanding balance.");
  }

  private void recordPayment() {
    String clientId = getToken("Client ID");
    Client c = context.getWarehouse().findClientById(clientId);
    if (c == null) { System.out.println("No such client"); return; }

    double amount = getDouble("Payment amount (e.g., 19.95)");
    if (amount <= 0) { System.out.println("Amount must be positive."); return; }

    boolean ok = context.getWarehouse().recordPayment(clientId, amount);
    if (ok) {
      System.out.printf("Payment recorded, new balance for %s: $%.2f%n",
          c.getId(), c.getBalance());
    } else {
      System.out.println("Payment failed.");
    }
  }

  private void becomeClient() {
    String id = getToken("Enter Client ID");
    Client c = context.getWarehouse().findClientById(id);
    if (c == null) {
      System.out.println("No such client");
      return;
    }
    context.setClientId(id);
    context.changeState(Context.TO_CLIENT);
  }

  private void logout() {
    int entry = context.getEntryRole();
    // If manager temporarily became clerk, go back to manager; else to login.
    if (entry == Context.TO_MANAGER) context.changeState(Context.TO_MANAGER);
    else                             context.changeState(Context.TO_LOGIN);
  }
}
