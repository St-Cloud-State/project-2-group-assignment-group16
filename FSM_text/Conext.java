import java.io.Serializable;
import java.util.Arrays;

/**
 * Central controller for the Warehouse FSM.
 * - Manages current state and transitions via an FSM matrix (state x event -> next state).
 * - Remembers entryRole and currentClientId to satisfy assignment requirements.
 * - Drives the loop from main() by repeatedly invoking current.run(this).
 *
 * Replace references to Warehouse.instance()
 */
public final class Context implements Serializable {

  // ====== State & Event IDs ==================================================

  public enum StateId {
    LOGIN, CLIENT, CLERK, MANAGER
  }

  public enum EventId {
    // Login choices / role switches
    SELECT_CLIENT, SELECT_CLERK, SELECT_MANAGER,
    // Validations / flow control
    VALIDATE_CLIENT_OK, INVALID_CLIENT,
    // Cross-role actions
    BECOME_CLIENT, BECOME_CLERK,
    // Core actions (menu items) — listed for completeness (matrix may ignore)
    ADD_CLIENT, ADD_PRODUCT, RECEIVE_SHIPMENT, SHOW_WAITLIST,
    LIST_PRODUCTS, LIST_CLIENTS, LIST_BALANCES, RECORD_PAYMENT,
    SHOW_CLIENT_DETAILS, SHOW_TRANSACTIONS, ADD_WISHLIST, SHOW_WISHLIST, PLACE_ORDER,
    // Session control
    LOGOUT, EXIT
  }

  // ====== Singleton ==========================================================

  private static volatile Context INSTANCE;

  public static Context instance() {
    if (INSTANCE == null) {
      synchronized (Context.class) {
        if (INSTANCE == null) {
          INSTANCE = new Context();
        }
      }
    }
    return INSTANCE;
  }

  // ====== Fields =============================================================

  private transient boolean running = false;

  // Domain root.
  private Warehouse warehouse = Warehouse.instance(); // TODO: change if needed

  private State current;           // current state object
  private StateId currentId;       // current state's id
  private StateId entryRole;       // remembers how the session began (LOGIN/CLERK/MANAGER)
  private String currentClientId;  // client "session" id (set when we become a client)

  // FSM matrix: next[state][event] = nextState
  private final StateId[][] next;

  // ====== Construction / Matrix Setup =======================================

  private Context() {
    next = new StateId[StateId.values().length][EventId.values().length];
    for (StateId[] row : next) Arrays.fill(row, null);
    initMatrix();
    // initial state is chosen by start() to LOGIN
  }

  /**
   * Define the deterministic transitions. business logic lives in states.
   * Logout is handled by resolveLogoutTarget() for the "return to where we started" rule.
   */
  private void initMatrix() {
    // From LOGIN
    setNext(StateId.LOGIN, EventId.SELECT_CLIENT,  StateId.CLIENT);
    setNext(StateId.LOGIN, EventId.SELECT_CLERK,   StateId.CLERK);
    setNext(StateId.LOGIN, EventId.SELECT_MANAGER, StateId.MANAGER);

    // From CLIENT
    // (Menu actions are handled inside ClientMenuState; matrix only routes structural transitions.)
    // LOGOUT handled dynamically by resolveLogoutTarget()

    // From CLERK
    setNext(StateId.CLERK, EventId.BECOME_CLIENT, StateId.CLIENT);
    // LOGOUT handled dynamically

    // From MANAGER
    setNext(StateId.MANAGER, EventId.BECOME_CLERK, StateId.CLERK);
    // LOGOUT handled dynamically
  }

  private void setNext(StateId from, EventId e, StateId to) {
    next[from.ordinal()][e.ordinal()] = to;
  }

  private StateId lookup(StateId from, EventId e) {
    return next[from.ordinal()][e.ordinal()];
  }

  // ====== Public Lifecycle ===================================================

  /**
   * Entry point used by Main.main(). Sets initial state and runs until stop() or EXIT.
   */
  public void start() {
    running = true;
    setState(StateId.LOGIN); // Starting state per assignment
    while (running && current != null) {
      current.run(this); // States call ctx.transition(...) or ctx.stop() as needed
    }
  }

  /** stop the main loop (e.g., on EXIT). */
  public void stop() {
    running = false;
  }

  // ====== Transition API =====================================================

  /**
   * Request a transition by event. States should call this to move the FSM.
   * Handles LOGOUT specially to return to the correct state based on entryRole.
   */
  public void transition(EventId e) {
    if (e == EventId.EXIT) {
      stop();
      return;
    }
    if (e == EventId.LOGOUT) {
      StateId target = resolveLogoutTarget();
      setState(target);
      return;
    }
    StateId target = lookup(currentId, e);
    if (target == null) {
      System.out.println("[WARN] No transition defined from " + currentId + " on event " + e);
      return;
    }
    setState(target);
  }

  /**
   * Switch to a specific state by id. Calls onExit() of previous and onEnter() of next.
   */
  public void setState(StateId id) {
    if (id == null) return;
    if (current != null) {
      try { current.onExit(this); } catch (Exception ignore) {}
    }

    currentId = id;
    current = switch (id) {
      case LOGIN   -> LoginState.instance();
      case CLIENT  -> ClientMenuState.instance();
      case CLERK   -> ClerkMenuState.instance();
      case MANAGER -> ManagerMenuState.instance();
    };

    try { current.onEnter(this); } catch (Exception ignore) {}
  }

  /**
   * For LOGOUT behavior:
   * - From CLIENT: back to CLERK if entryRole==CLERK, else LOGIN.
   * - From CLERK: back to MANAGER if entryRole==MANAGER, else LOGIN.
   * - From MANAGER: back to LOGIN.
   * - From LOGIN: stay LOGIN (or EXIT via explicit event).
   */
  private StateId resolveLogoutTarget() {
    if (currentId == StateId.CLIENT) {
      return (entryRole == StateId.CLERK) ? StateId.CLERK : StateId.LOGIN;
    }
    if (currentId == StateId.CLERK) {
      return (entryRole == StateId.MANAGER) ? StateId.MANAGER : StateId.LOGIN;
    }
    if (currentId == StateId.MANAGER) {
      return StateId.LOGIN;
    }
    return StateId.LOGIN;
  }

  // ====== Session Memory Helpers ============================================

  /** Record the "role we started from" if it's not already set. */
  public void markEntryRoleIfUnset(StateId role) {
    if (this.entryRole == null || this.entryRole == StateId.LOGIN) {
      this.entryRole = role;
    }
  }

  /** Reset entry role to LOGIN (e.g., when fully exiting to login flow). */
  public void resetEntryRole() {
    this.entryRole = StateId.LOGIN;
  }

  public StateId getEntryRole() {
    return entryRole;
  }

  public void setEntryRole(StateId role) {
    this.entryRole = role;
  }

  public String getCurrentClientId() {
    return currentClientId;
  }

  public void setCurrentClientId(String clientId) {
    this.currentClientId = clientId;
  }

  public StateId getCurrentId() {
    return currentId;
  }

  public Warehouse getWarehouse() {
    return warehouse;
  }

  // ====== Convenience Logging ================================================

  public void logEnter(State s) {
    System.out.println("-> ENTER " + s.getName());
  }

  public void logExit(State s) {
    System.out.println("<- EXIT  " + s.getName());
  }
}
