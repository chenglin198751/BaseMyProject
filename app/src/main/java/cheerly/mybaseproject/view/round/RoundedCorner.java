package cheerly.mybaseproject.view.round;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
    RoundedCorner.TOP_LEFT, RoundedCorner.TOP_RIGHT,
    RoundedCorner.BOTTOM_LEFT, RoundedCorner.BOTTOM_RIGHT
})
public @interface RoundedCorner {
  int TOP_LEFT = 0;
  int TOP_RIGHT = 1;
  int BOTTOM_RIGHT = 2;
  int BOTTOM_LEFT = 3;
}
