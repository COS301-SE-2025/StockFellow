const ROUTES = [
    {
        url: '/user',
        auth: false,
        creditCheck: false,
        rateLimit: {
            windowMs: 15 * 60 * 1000,
            max: 5
        },
        proxy: {
            target: "https://www.google.com",
            changeOrigin: true,
            pathRewrite: {
                [`^/user`]: '',
            },
        }
    },
    {
        url: '/transaction',
        auth: true,
        creditCheck: true,
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
        url: '/group',
        auth: true,
        creditCheck: true,
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