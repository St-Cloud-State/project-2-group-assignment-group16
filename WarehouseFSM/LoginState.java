import java.util.*;

public final class LoginState extends WarehouseState {
  private static LoginState instance;
  private static Context context;

  // Singleton pattern
  private LoginState() {}
  public static LoginState instance() {
    if (instance == null) instance = new LoginState();
    return instance;
  }

  public static void setContext(Context ctx) { context = ctx; }

  @Override
  public void run() {
    while (true) {
      System.out.println("\n=== Login Menu ===");
      System.out.println("1) Client");
      System.out.println("2) Clerk");
      System.out.println("3) Manager");
      System.out.println("0) Exit");
      int choice = context.getInt("Choose option");
      switch (choice) {
        case 1: enterClientMode();  return;
        case 2: enterClerkMode();   return;
        case 3: enterManagerMode(); return;
        case 0: context.changeState(Context.EXIT_APP); return;
        default: System.out.println("Invalid selection. Try again.");
      }
    }
  }

  private void enterClientMode() {
    String id = context.getToken("Enter Client ID");
    if (context.getWarehouse().findClientById(id) == null) {
      System.out.println("No such client.");
      return;
    }
    context.setClientId(id);
    context.setEntryRole(Context.TO_CLIENT);
    context.changeState(Context.TO_CLIENT);
  }

  private void enterClerkMode() {
    context.setEntryRole(Context.TO_CLERK);
    context.changeState(Context.TO_CLERK);
  }

  private void enterManagerMode() {
    context.setEntryRole(Context.TO_MANAGER);
    context.changeState(Context.TO_MANAGER);
  }
}