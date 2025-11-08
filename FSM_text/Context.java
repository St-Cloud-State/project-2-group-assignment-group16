import java.io.Serializable;
import java.util.Scanner;

public final class Context implements Serializable {
  private static final long serialVersionUID = 1L;

  // --- Public constants expected by many template states (keep these) ---
  public static final int TO_CLIENT  = 1;
  public static final int TO_CLERK   = 2;
  public static final int TO_MANAGER = 3;
  public static final int EXIT_APP   = 9;

  // Internal ids
  public enum StateId { LOGIN, CLIENT, CLERK, MANAGER }

  // Singleton
  private static volatile Context INSTANCE;
  public static Context instance() {
    if (INSTANCE == null) {
      synchronized (Context.class) {
        if (INSTANCE == null) INSTANCE = new Context();
      }
    }
    return INSTANCE;
  }

  // IO + loop
  private final Scanner in = new Scanner(System.in);
  private transient boolean running = false;

  // Domain root (adjust if your Warehouse is not a singleton)
  private Warehouse warehouse = Warehouse.instance();

  // Session memory required by spec
  private String clientId;                 // current client session
  private StateId entryRole = StateId.LOGIN; // where the session began

  // FSM
  private State current;
  private StateId currentId;

  private Context() {
    // If your LoginState provides a static setContext, this line enables it without editing the state:
    try { LoginState.setContext(this); } catch (Throwable ignore) {}
  }

  // ===== Loop =====
  public void start() {
    running = true;
    setState(StateId.LOGIN);
    while (running && current != null) current.run(this);
  }

  public void stop() { running = false; }

  // ===== Minimal input helpers commonly used in template states =====
  public int getInt(String prompt) {
    for (;;) {
      System.out.print(prompt + ": ");
      String s = in.nextLine().trim();
      try { return Integer.parseInt(s); }
      catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
    }
  }

  public double getDouble(String prompt) {
    for (;;) {
      System.out.print(prompt + ": ");
      String s = in.nextLine().trim();
      try { return Double.parseDouble(s); }
      catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
    }
  }

  public String getLine(String prompt) {
    System.out.print(prompt + ": ");
    return in.nextLine().trim();
  }

  // ===== Compatibility: numeric state changes expected by older states =====
  public void changeState(int code) {
    if (code == EXIT_APP) {
      stop();
      return;
    }
    switch (code) {
      case TO_CLIENT:
        setState(StateId.CLIENT);
        break;
      case TO_CLERK:
        setState(StateId.CLERK);
        break;
      case TO_MANAGER:
        setState(StateId.MANAGER);
        break;
      default:
        System.out.println("[WARN] Unknown state code: " + code);
        break;
    }
  }

  // Logout with “return to where we started” behavior
  public void logout() {
    if (currentId == StateId.CLIENT) {
      if (entryRole == StateId.CLERK) {
        setState(StateId.CLERK);
      } else {
        setState(StateId.LOGIN);
      }
      return;
    }
    if (currentId == StateId.CLERK) {
      if (entryRole == StateId.MANAGER) {
        setState(StateId.MANAGER);
      } else {
        setState(StateId.LOGIN);
      }
      return;
    }
    setState(StateId.LOGIN);
    entryRole = StateId.LOGIN;
    clientId = null;
  }

  // ===== State plumbing =====
  private void setState(StateId id) {
    if (current != null) {
      try { current.onExit(this); } catch (Exception ignore) {}
    }
    currentId = id;

    // Replace switch-expression with classic if/else
    if (id == StateId.LOGIN) {
      current = LoginState.instance();
    } else if (id == StateId.CLIENT) {
      current = ClientMenuState.instance();
    } else if (id == StateId.CLERK) {
      current = ClerkMenuState.instance();
    } else { // MANAGER
      current = ManagerMenuState.instance();
    }

    try { current.onEnter(this); } catch (Exception ignore) {}
  }

  // ===== Session + domain accessors used by states =====
  public void setClientId(String id) { this.clientId = id; }
  public String getClientId() { return this.clientId; }

  /** Accepts Context.TO_CLIENT / TO_CLERK / TO_MANAGER (what many templates pass). */
  public void setEntryRole(int code) {
    if (code == TO_CLERK) {
      this.entryRole = StateId.CLERK;
    } else if (code == TO_MANAGER) {
      this.entryRole = StateId.MANAGER;
    } else {
      this.entryRole = StateId.LOGIN; // client path begins at login
    }
  }
  public StateId getEntryRole() { return entryRole; }

  public Warehouse getWarehouse() { return warehouse; }

  // ===== Logging (used by WarehouseState default hooks) =====
  public void logEnter(State s) { System.out.println("-> ENTER " + s.getName()); }
  public void logExit(State s)  { System.out.println("<- EXIT  " + s.getName()); }
}
