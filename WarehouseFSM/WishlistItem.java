import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** One entry inside a client's Wishlist for a specific product. */
public class WishlistItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String productId;
  private int qty;
  private final Date timeAdded; // when first added

  public WishlistItem(String productId, int qty, Date timeAdded) {
    if (productId == null || productId.trim().isEmpty()) {
      throw new IllegalArgumentException("productId is required");
    }
    if (qty <= 0) {
      throw new IllegalArgumentException("qty must be > 0");
    }
    this.productId = productId;
    this.qty = qty;
    this.timeAdded = (timeAdded == null) ? new Date() : new Date(timeAdded.getTime());
  }

  public String getProductId() { return productId; }
  public int getQty() { return qty; }

  /** Set a positive quantity; use Wishlist.updateQty(..., 0) to remove the item. */
  public void setQty(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
    this.qty = qty;
  }

  /** Defensive copy since Date is mutable. */
  public Date getTime() { return new Date(timeAdded.getTime()); }

  @Override
  public String toString() {
    return "WishlistItem{productId='" + productId + "', qty=" + qty + ", timeAdded=" + timeAdded + "}";
  }

  /** Equality based on productId (unique per item within a Wishlist). */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WishlistItem)) return false;
    WishlistItem that = (WishlistItem) o;
    return Objects.equals(productId, that.productId);
  }

  @Override
  public int hashCode() { return Objects.hash(productId); }
}
