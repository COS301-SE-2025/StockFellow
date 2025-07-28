package com.stockfellow.notificationservice.integration.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSendNotificationSuccess() throws Exception {
        String notificationJson = "{\"userId\":\"user123\",\"type\":\"GROUP_INVITE\",\"title\":\"Group Invite\",\"message\":\"Join our group\",\"channel\":\"EMAIL\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Notification sent successfully"))
                .andExpect(jsonPath("$.notificationId").exists());
    }

    @Test
    public void testGetUserNotifications() throws Exception {
        // First send a notification
        String notificationJson = "{\"userId\":\"user123\",\"type\":\"GROUP_INVITE\",\"title\":\"Group Invite\",\"message\":\"Join our group\",\"channel\":\"IN_APP\",\"priority\":\"NORMAL\"}";
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson));

        mockMvc.perform(get("/api/notifications/user")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    public void testMarkNotificationAsRead() throws Exception {
        // Send a notification first
        String notificationJson = "{\"userId\":\"user123\",\"type\":\"GROUP_INVITE\",\"title\":\"Group Invite\",\"message\":\"Join our group\",\"channel\":\"IN_APP\",\"priority\":\"NORMAL\"}";
        String response = mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andReturn().getResponse().getContentAsString();
        String notificationId = response.split("\"notificationId\":\"")[1].split("\"")[0];

        mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification marked as read"))
                .andExpect(jsonPath("$.notificationId").value(notificationId));
    }

    @Test
    public void testGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications/user/count")
                .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").isNumber())
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    public void testSendNotificationInvalidInput() throws Exception {
        String invalidJson = "{\"userId\":\"\",\"type\":\"GROUP_INVITE\",\"title\":\"Group Invite\",\"message\":\"Join our group\",\"channel\":\"EMAIL\"}";

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User ID is required"));
    }
}