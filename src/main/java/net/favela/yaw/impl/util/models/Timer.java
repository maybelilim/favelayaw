package net.favela.yaw.impl.util.models;

public class Timer {

    private long lastMs = System.currentTimeMillis();

    public boolean passedMs(long ms) {
        return System.currentTimeMillis() - lastMs >= ms;
    }

    public void reset() {
        this.lastMs = System.currentTimeMillis();
    }
}