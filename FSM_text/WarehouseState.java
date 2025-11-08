import java.io.Serializable;

/** Minimal base class so existing states (Login/Client/Clerk/Manager) compile unchanged. */
public abstract class WarehouseState implements State, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public void onEnter(Context ctx) { ctx.logEnter(this); }

  @Override
  public void onExit(Context ctx) { ctx.logExit(this); }

  @Override
  public String getName() { return getClass().getSimpleName(); }
}
