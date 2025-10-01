package main;

/**
 * Created by chenglin on 2017-5-5.
 */

public interface OnLogListener<T> {
	void onFinished(T t);
}
