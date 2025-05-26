const ROUTES = [
    {
        url: '/api/user*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "http://user-service:4000",
            changeOrigin: true,
            pathRewrite: {
                [`^/api`]: '', // Removes /api prefix
            },
        }
    },
    {
        url: '/api/group*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "http://user-service:4040",
            changeOrigin: true,
            pathRewrite: {
                [`^/api`]: '',
            },
        }
    },
    {
        url: '/api/transaction*',
        auth: true,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "http://transaction-service:4080", //Unkown endpoint currently 
            changeOrigin: true,
            pathRewrite: {
                [`^/api`]: '',
            },
        }
    },
    {
        url: '/api*',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "http://user-service:4000",
            changeOrigin: true,
            pathRewrite: {
                [`^/api`]: '', 
            },
        }
    }
]

exports.ROUTES = ROUTES;