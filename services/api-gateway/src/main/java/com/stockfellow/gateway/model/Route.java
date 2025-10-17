package com.stockfellow.gateway.model;

public class Route {
    private String url;
    private boolean auth;
    private RateLimit rateLimit;
    private Proxy proxy;

    public Route() {}

    public Route(String url, boolean auth, RateLimit rateLimit, Proxy proxy) {
        this.url = url;
        this.auth = auth;
        this.rateLimit = rateLimit;
        this.proxy = proxy;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public boolean isAuth() { return auth; }
    public void setAuth(boolean auth) { this.auth = auth; }
    
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }

    public static class RateLimit {
        private long windowMs;
        private int max;

        public RateLimit() {}

        public RateLimit(long windowMs, int max){
            this.windowMs = windowMs;
            this.max = max;
        }

        public long getWindowMs() { return windowMs; }
        public void setWindowMs(long windowMs) { this.windowMs = windowMs; }
        
        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
    }

    public static class Proxy {
        private String target;
        private boolean changeOrigin;
        
        public Proxy() {}
        
        public Proxy(String target, boolean changeOrigin) {
            this.target = target;
            this.changeOrigin = changeOrigin;
        }
        
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        
        public boolean isChangeOrigin() { return changeOrigin; }
        public void setChangeOrigin(boolean changeOrigin) { this.changeOrigin = changeOrigin; }
    }

}
