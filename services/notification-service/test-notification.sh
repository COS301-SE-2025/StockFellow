#!/bin/bash

BASE_URL="http://localhost:4050/api/notifications"
USER_ID="test-user-123"

echo "=== Testing Notification Service ==="
echo

# Test 1: Get service info
echo "1. Getting service information..."
curl -s -X GET "${BASE_URL}" | jq '.'
echo -e "\n"

# Test 2: Send a notification
echo "2. Sending a test notification..."
NOTIFICATION_RESPONSE=$(curl -s -X POST "${BASE_URL}/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'${USER_ID}'",
    "groupId": "group-456",
    "type": "WELCOME",
    "title": "Welcome to StockFellow!",
    "message": "Thank you for joining our platform. Start exploring investment groups today!",
    "channel": "IN_APP",
    "priority": "NORMAL",
    "metadata": {
      "source": "registration",
      "campaign": "welcome-series"
    }
  }')

echo $NOTIFICATION_RESPONSE | jq '.'
NOTIFICATION_ID=$(echo $NOTIFICATION_RESPONSE | jq -r '.notificationId')
echo -e "\n"

# Test 3: Send another notification with different channel
echo "3. Sending an EMAIL notification..."
curl -s -X POST "${BASE_URL}/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'${USER_ID}'",
    "type": "PAYMENT_DUE",
    "title": "Payment Due Reminder",
    "message": "Your monthly contribution of $100 is due in 3 days.",
    "channel": "EMAIL",
    "priority": "HIGH",
    "metadata": {
      "amount": 100,
      "dueDate": "2025-08-01"
    }
  }' | jq '.'
echo -e "\n"

# Test 4: Send SMS notification
echo "4. Sending an SMS notification..."
curl -s -X POST "${BASE_URL}/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'${USER_ID}'",
    "type": "REMINDER",
    "title": "Meeting Reminder",
    "message": "Group meeting starts in 1 hour. Join via the app.",
    "channel": "SMS",
    "priority": "URGENT"
  }' | jq '.'
echo -e "\n"

# Wait a bit for processing
echo "5. Waiting 5 seconds for notifications to be processed..."
sleep 5
echo

# Test 5: Get user notifications
echo "6. Getting user notifications..."
curl -s -X GET "${BASE_URL}/user" \
  -H "X-User-Id: ${USER_ID}" \
  -H "X-User-Name: TestUser" | jq '.'
echo -e "\n"

# Test 6: Get unread notifications
echo "7. Getting unread notifications..."
curl -s -X GET "${BASE_URL}/user/unread" \
  -H "X-User-Id: ${USER_ID}" \
  -H "X-User-Name: TestUser" | jq '.'
echo -e "\n"

# Test 7: Get unread count
echo "8. Getting unread count..."
curl -s -X GET "${BASE_URL}/user/count" \
  -H "X-User-Id: ${USER_ID}" \
  -H "X-User-Name: TestUser" | jq '.'
echo -e "\n"

# Test 8: Mark notification as read (if we got a notification ID)
if [ "$NOTIFICATION_ID" != "null" ] && [ -n "$NOTIFICATION_ID" ]; then
    echo "9. Marking notification as read: ${NOTIFICATION_ID}"
    curl -s -X PUT "${BASE_URL}/${NOTIFICATION_ID}/read" \
      -H "X-User-Id: ${USER_ID}" \
      -H "X-User-Name: TestUser" | jq '.'
    echo -e "\n"
fi

# Test 9: Get notification by ID
if [ "$NOTIFICATION_ID" != "null" ] && [ -n "$NOTIFICATION_ID" ]; then
    echo "10. Getting specific notification: ${NOTIFICATION_ID}"
    curl -s -X GET "${BASE_URL}/${NOTIFICATION_ID}" \
      -H "X-User-Id: ${USER_ID}" \
      -H "X-User-Name: TestUser" | jq '.'
    echo -e "\n"
fi

# Test 10: Send bulk notifications
echo "11. Sending bulk notifications..."
curl -s -X POST "${BASE_URL}/bulk" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admin-user" \
  -d '{
    "userIds": ["user-001", "user-002", "user-003"],
    "type": "SYSTEM_UPDATE",
    "title": "System Maintenance",
    "message": "The system will be under maintenance on Sunday from 2-4 AM.",
    "channel": "IN_APP",
    "priority": "NORMAL",
    "metadata": {
      "maintenanceWindow": "2025-08-03 02:00-04:00"
    }
  }' | jq '.'
echo -e "\n"

# Test 11: Mark all as read
echo "12. Marking all notifications as read..."
curl -s -X PUT "${BASE_URL}/user/read-all" \
  -H "X-User-Id: ${USER_ID}" \
  -H "X-User-Name: TestUser" | jq '.'
echo -e "\n"

echo "=== Test completed! ==="