package com.koperek.bin;

public class UpstreamRejectedException extends RuntimeException {
    private final int upstreamStatus;

    public UpstreamRejectedException(int upstreamStatus, Throwable cause) {
        super("Upstream rejected the request with status " + upstreamStatus, cause);
        this.upstreamStatus = upstreamStatus;
    }

    public int upstreamStatus() {
        return upstreamStatus;
    }
}