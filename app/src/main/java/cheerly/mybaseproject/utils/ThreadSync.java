package cheerly.mybaseproject.utils;

/**
 * 用notify 和 wait做线程同步
 */
public class ThreadSync {

    private static final String TAG = "ThreadSync";
    private boolean mIsPaused;
    private boolean mIsExit;

    public ThreadSync() {
        mIsPaused = false;
    }

    public synchronized boolean isPaused() {
        return mIsPaused;
    }

    public synchronized void pause() {
        mIsPaused = true;
    }

    public synchronized void resume() {
        mIsPaused = false;
        notifyAll();
    }

    public synchronized void callWait() {
        while (mIsPaused) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void callWait(long millis) {
        while (mIsPaused) {
            try {
                this.wait(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mIsPaused = false;
            }
        }
    }

    public synchronized boolean isExit() {
        return mIsExit;
    }

    public synchronized void exit() {
        mIsPaused = false;
        mIsExit = true;
        notifyAll();
    }
}
