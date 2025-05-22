const Keycloak = require('keycloak-connect');
const session = require('express-session');
const path = require('path');
const fs = require('fs');

const setupAuth = (app, routes) => {
    var memoryStore = new session.MemoryStore();
    
    const keycloakConfigPath = path.join(__dirname, 'keycloak.json');
    
    /* Test block for verifying path to keycloak.json
    console.log('Keycloak config path:', keycloakConfigPath);
    console.log('File exists:', fs.existsSync(keycloakConfigPath));
    
    try {
        const configContent = fs.readFileSync(keycloakConfigPath, 'utf8');
        console.log('Config file content is accessible');
    } catch (err) {
        console.error('Error reading config file:', err.message);
    }
    */
    
    // Creates a config using keycloack.js
    // This was done due to errors with docker not being able to find the file - DJR
    const keycloakConfig = JSON.parse(fs.readFileSync(keycloakConfigPath, 'utf8'));
    
    var keycloak = new Keycloak({ 
        store: memoryStore
    }, keycloakConfig);  // Passes the created config diectly

    app.use(session({
        secret:'<RANDOM GENERATED TOKEN>',
        resave: false,
        saveUninitialized: true,
        store: memoryStore
    }));

    app.use(keycloak.middleware());

        //login endpoint 
        app.get('/login', (req, res) => {
            console.log('Login enpoint hit'); // checking that it reaches endpoint
            res.redirect(keycloak.loginUrl());
        });

        //registration endpoint
        app.get('/register', (req, res) => {
            console.log('Register enpoint hit');
            res.redirect(keycloak.registerUrl());
        });

        //logout endpoint
        app.get('/logout', (req, res) => {
            console.log('Logout enpoint hit');
            res.redirect(keycloak.logoutUrl());
        });

    routes.forEach(r => {
        if (r.auth) {
            app.use(r.url, keycloak.protect(), function (req, res, next) {
                //Can add additional logging code here
                next();
            });
        }
    });
}

exports.setupAuth = setupAuth