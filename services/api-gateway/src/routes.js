const ROUTES = [
    {
        url: '/api/user*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 10
        },
        proxy: {
            target: "http://user-service:4000",
            changeOrigin: true,
            pathRewrite: {},
            // Add CORS headers
            onProxyRes: function(proxyRes, req, res) {
                proxyRes.headers['Access-Control-Allow-Origin'] = '*';
                proxyRes.headers['Access-Control-Allow-Methods'] = 'GET,POST,PUT,DELETE,OPTIONS';
                proxyRes.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization';
            }
        }
    },
    {
        url: '/api/group*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 10
        },
        proxy: {
            target: "http://group-service:4040",
            changeOrigin: true,
            pathRewrite: {},
        }
    },
    {
        url: '/api/transaction*',
        auth: true,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 10
        },
        proxy: {
            target: "http://transaction-service:4080", //Unkown endpoint currently 
            changeOrigin: true,
            pathRewrite: {},
        }
    },
    {
        url: '/api*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 10
        },
        proxy: {
            target: "http://user-service:4000",
            changeOrigin: true,
            pathRewrite: {},
        }
    }
]

exports.ROUTES = ROUTES;