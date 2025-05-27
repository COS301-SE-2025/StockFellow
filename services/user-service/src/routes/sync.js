const express = require('express');
const router = express.Router();
const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

router.post('/', async (req, res) => {
    console.log('=== KEYCLOAK SYNC REQUEST ===');
    console.log('Body:', req.body);
    console.log('Headers:', req.headers);

    try {
        const {
            keycloakId,
            username,
            email,
            firstName,
            lastName,
            emailVerified,
            idNumber,
            phoneNumber
        } = req.body;

        if (!keycloakId || !username || !email || !firstName || !lastName || !idNumber) {
            return res.status(400).json({ error: 'Missing required fields' });
        }

        // Check if user exists to determine whther this is a registration or update
        const existingUser = await readModel.getUser(keycloakId);

        let event;
        let eventType;
        let message;

        if (existingUser){
            //Update
            console.log('Updating existing user:', keycloakId);

            eventType = 'UserUpdated';
            event = await eventStore.appendEvent(eventType, {
                userId: keycloakId,
                username: username || existingUser.username,
                email: email || existingUser.email,
                firstName: firstName || existingUser.firstName,
                lastName: lastName || existingUser.lastName,
                emailVerified: emailVerified !== undefined ? emailVerified : existingUser.emailVerified,
                contactNumber: phoneNumber || existingUser.contactNumber,
                updatedAt: new Date()
            });

            message = 'User updated successfully';
            logger.info(`User ${keycloakId} updated successfully`);

        } else {
            //Create
            console.log('Creating new user:', keycloakId);
            
            eventType = 'UserRegistered';
            event = await eventStore.appendEvent(eventType, {
                userId: keycloakId,
                username: username || '',
                email: email,
                firstName: firstName || '',
                lastName: lastName || '',
                emailVerified: emailVerified || false,
                contactNumber: phoneNumber || '',
                idNumber: idNumber || '', 
                createdAt: new Date(),
                updatedAt: new Date()
            });
        }
        
        message = 'User registered successfully';
        logger.info(`User ${keycloakId} registered successfully`);

        await readModel.rebuildState(keycloakId);

        console.log(`User sync successful for: ${email}`);
        
        res.status(200).json({ 
            success: true,
            message: message,
            userId: keycloakId,
            eventId: event._id,
            eventType: eventType
        });

    } catch (error) {
        console.error('User sync error:', error);
        logger.error(`User sync failed: ${error.message}`);
        res.status(500).json({ 
            success: false,
            error: 'Failed to sync user',
            details: error.message 
        });
    }
});

module.exports = router