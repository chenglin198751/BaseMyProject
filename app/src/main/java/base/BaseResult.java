package base;

/**
 * Created by chenglin on 2017-12-29.
 */
public class BaseResult {
    private int code = -1;
    private String desc = "";

    public boolean isOk() {
        return code == 0;
    }

    public String getMessage() {
        return desc;
    }
}
