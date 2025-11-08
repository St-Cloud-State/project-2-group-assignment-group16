import java.io.Serializable;

/**
 * Common contract for all FSM states (Login, Client, Clerk, Manager).
 * Keep implementations lightweight; do all transition requests via Context.
 *
 * Library-style notes:
 * - Use singleton pattern for each concrete state (instance()).
 * - Context drives the loop by calling onEnter -> run -> onExit.
 */
public interface State extends Serializable {

  /**
   * Invoked right after the Context switches into this state.
   * Do any lightweight setup or contextual logging here.
   */
  void onEnter(Context ctx);

  /**
   * The main body for this state:
   * - Print the menu
   * - Read user input
   * - Call domain methods (Warehouse, Client*, Product*)
   * - Request transitions via ctx.transition(EventId)
   *
   * This method should return only after either:
   * - A valid transition event has been emitted, OR
   * - The state decides to stay and re-display its menu (loop internally)
   */
  void run(Context ctx);

  /**
   * Invoked right before the Context leaves this state.
   * Use for cleanup if needed 
   */
  void onExit(Context ctx);

  /**
   * Friendly name for logging/debugging.
   */
  String getName();
}
