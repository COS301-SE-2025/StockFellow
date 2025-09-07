// package com.stockfellow.notificationservice.integration;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.stockfellow.notificationservice.model.Notification;
// import com.stockfellow.notificationservice.repository.NotificationRepository;
// import com.stockfellow.notificationservice.service.GroupServiceClient;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.jms.core.JmsTemplate;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.verify;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// @Transactional
// public class NotificationControllerIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private NotificationRepository notificationRepository;

//     @MockBean
//     private JmsTemplate jmsTemplate;

//     @MockBean
//     private GroupServiceClient groupServiceClient;

//     private static final String TEST_USER_ID = "user123";
//     private static final String TEST_GROUP_ID = "group456";

//     @BeforeEach
//     public void setup() {
//         // Clear repository before each test
//         notificationRepository.deleteAll();
//     }

//     @Test
//     public void testGetServiceInfo() throws Exception {
//         mockMvc.perform(get("/api/notifications"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.service").value("Notification Service"))
//                 .andExpect(jsonPath("$.version").value("1.0.0"))
//                 .andExpect(jsonPath("$.endpoints").isArray());
//     }

//     @Test
//     public void testSendNotificationSuccess() throws Exception {
//         Map<String, Object> notificationRequest = new HashMap<>();
//         notificationRequest.put("userId", TEST_USER_ID);
//         notificationRequest.put("groupId", TEST_GROUP_ID);
//         notificationRequest.put("type", "GROUP_INVITE");
//         notificationRequest.put("title", "Group Invite");
//         notificationRequest.put("message", "Join our group");
//         notificationRequest.put("channel", "EMAIL");
//         notificationRequest.put("priority", "HIGH");

//         String requestJson = objectMapper.writeValueAsString(notificationRequest);

//         mockMvc.perform(post("/api/notifications/send")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.message").value("Notification sent successfully"))
//                 .andExpect(jsonPath("$.notificationId").exists());

//         // Verify notification was saved to database
//         List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(TEST_USER_ID);
//         assertEquals(1, notifications.size());
//         assertEquals("GROUP_INVITE", notifications.get(0).getType());

//         // Verify JMS message was sent
//         verify(jmsTemplate).convertAndSend(eq("notification.queue"), any(Map.class));
//     }

//     @Test
//     public void testSendNotificationWithInvalidUserId() throws Exception {
//         Map<String, Object> notificationRequest = new HashMap<>();
//         notificationRequest.put("userId", ""); // Empty user ID
//         notificationRequest.put("type", "GROUP_INVITE");
//         notificationRequest.put("title", "Group Invite");
//         notificationRequest.put("message", "Join our group");
//         notificationRequest.put("channel", "EMAIL");

//         String requestJson = objectMapper.writeValueAsString(notificationRequest);

//         mockMvc.perform(post("/api/notifications/send")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.error").value("User ID is required"));
//     }

//     @Test
//     public void testSendNotificationWithInvalidChannel() throws Exception {
//         Map<String, Object> notificationRequest = new HashMap<>();
//         notificationRequest.put("userId", TEST_USER_ID);
//         notificationRequest.put("type", "GROUP_INVITE");
//         notificationRequest.put("title", "Group Invite");
//         notificationRequest.put("message", "Join our group");
//         notificationRequest.put("channel", "INVALID_CHANNEL");

//         String requestJson = objectMapper.writeValueAsString(notificationRequest);

//         mockMvc.perform(post("/api/notifications/send")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.error").value("Invalid channel. Must be: EMAIL, SMS, PUSH, or IN_APP"));
//     }

//     @Test
//     public void testGetUserNotifications() throws Exception {
//         // Create test notifications in database
//         createTestNotification("notif1", TEST_USER_ID, "GROUP_INVITE", "Test Title 1", false);
//         createTestNotification("notif2", TEST_USER_ID, "PAYMENT_DUE", "Test Title 2", true);

//         mockMvc.perform(get("/api/notifications/user")
//                 .header("X-User-Id", TEST_USER_ID))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.notifications").isArray())
//                 .andExpect(jsonPath("$.notifications.length()").value(2))
//                 .andExpect(jsonPath("$.count").value(2));
//     }

//     @Test
//     public void testGetUserNotificationsWithoutUserId() throws Exception {
//         mockMvc.perform(get("/api/notifications/user"))
//                 .andExpect(status().isUnauthorized())
//                 .andExpect(jsonPath("$.error").value("User ID not found in request"));
//     }

//     @Test
//     public void testMarkNotificationAsRead() throws Exception {
//         // Create test notification
//         Notification notification = createTestNotification("notif123", TEST_USER_ID, "GROUP_INVITE", "Test Title", false);

//         mockMvc.perform(put("/api/notifications/" + notification.getNotificationId() + "/read")
//                 .header("X-User-Id", TEST_USER_ID))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Notification marked as read"))
//                 .andExpect(jsonPath("$.notificationId").value(notification.getNotificationId()));

//         // Verify notification was marked as read
//         Optional<Notification> updatedNotification = notificationRepository.findByNotificationId(notification.getNotificationId());
//         assertTrue(updatedNotification.isPresent());
//         assertTrue(updatedNotification.get().getReadStatus());
//     }

//     @Test
//     public void testGetUnreadCount() throws Exception {
//         // Create test notifications
//         createTestNotification("notif1", TEST_USER_ID, "GROUP_INVITE", "Unread 1", false);
//         createTestNotification("notif2", TEST_USER_ID, "GROUP_INVITE", "Unread 2", false);
//         createTestNotification("notif3", TEST_USER_ID, "PAYMENT_DUE", "Read", true);

//         mockMvc.perform(get("/api/notifications/user/count")
//                 .header("X-User-Id", TEST_USER_ID))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.unreadCount").value(2))
//                 .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
//     }

//     // Helper method to create test notifications
//     private Notification createTestNotification(String notificationId, String userId, String type, String title, boolean isRead) {
//         Notification notification = new Notification(
//                 notificationId, userId, TEST_GROUP_ID, type, title, 
//                 "Test message", "IN_APP", "NORMAL"
//         );
//         notification.setReadStatus(isRead);
//         notification.setCreatedAt(LocalDateTime.now());
//         notification.setStatus("SENT");
//         return notificationRepository.save(notification);
//     }
// }