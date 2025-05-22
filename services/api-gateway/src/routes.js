const ROUTES = [
    {
        url: '/auth',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 100
        },
        proxy: {
            target: "http://localhost:5000",
            changeOrigin: true,
            pathRewrite: {
                [`^/auth`]: '/auth',
            },
        }
    },
    {
        url: '/user',
        auth: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "http://localhost:5000",
            changeOrigin: true,
            pathRewrite: {
                [`^/user`]: '',
            },
        }
    },
    {
        url: '/transaction',
        auth: true,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "https://www.github.com",
            changeOrigin: true,
            pathRewrite: {
                [`^/transaction`]: '',
            },
        }
    },
    {
        url: '/api*',
        auth: true,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "https://www.youtube.com/",
            changeOrigin: true,
            pathRewrite: {
                [`^/group`]: '',
            },
        }
    }
]

// TODO: Should add a rate limiter for API(already have) and one spcifically for the auth services like keycloak
//

exports.ROUTES = ROUTES;