import java.io.Serializable;

/** Minimal base so existing states compile unchanged. */
public abstract class WarehouseState implements State, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public void onEnter(Context ctx) { ctx.logEnter(this); }

  @Override
  public void onExit(Context ctx) { ctx.logExit(this); }

  @Override
  public String getName() { return getClass().getSimpleName(); }

  /** Bridge: Context-driven run(ctx) calls the parameterless run() your states already implement. */
  @Override
  public final void run(Context ctx) { this.run(); }

  /** Your states already implement this. Keep them unchanged. */
  public abstract void run();
}
