// package com.stockfellow.transactionservice;

// // JUnit 5 imports
// import org.junit.jupiter.api.*;
// import static org.junit.jupiter.api.Assertions.*;

// // Spring Boot Test imports
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.transaction.annotation.Transactional;

// // Spring Test imports for MockMvc
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// // Spring Framework imports
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.web.client.RestTemplate;

// // Mock imports
// import org.springframework.boot.test.mock.mockito.MockBean;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// // Jackson for JSON processing
// import com.fasterxml.jackson.databind.ObjectMapper;

// // AssertJ for better assertions
// import static org.assertj.core.api.Assertions.*;

// // Your application imports
// import com.stockfellow.transactionservice.dto.*;
// import com.stockfellow.transactionservice.model.*;
// import com.stockfellow.transactionservice.repository.*;

// // Java standard library imports
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @TestPropertySource(locations = "classpath:application-test.yml")
// class TransactionIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private UserRepository userRepository;

//     @MockBean(name = "restTemplate")
//     private RestTemplate restTemplate;

//     private static final String X_USER_ID_HEADER = "X-User-Id";
    
//     // Test data holders
//     private static UUID groupOwnerId;
//     private static String ownerEmail;
//     private static UUID groupId;
//     private static List<UUID> memberUserIds = new ArrayList<>();
//     private static List<UUID> payerDetailIds = new ArrayList<>();
//     private String[] memberEmails = {
//         "member1@stockfellow.com",
//         "member2@stockfellow.com",
//         "member3@stockfellow.com",
//         "member4@stockfellow.com"
//     };
//     private String[] memberNames = {
//         "Member One",
//         "Member Two",
//         "Member Three",
//         "Member Four"
//     };

//     @BeforeEach
//     void setUp() {
//         // Clear relevant data before each test
//         userRepository.deleteAll();
//     }

//     @Test
//     @Order(1)
//     @DisplayName("1. Create Group Owner via User Sync")
//     void createGroupOwner() throws Exception {
//         groupOwnerId = UUID.randomUUID();
//         ownerEmail = "group.owner@stockfellow.com";
        
//         SyncUserDto ownerDto = new SyncUserDto();
//         ownerDto.setUserId(groupOwnerId);
//         ownerDto.setEmail("group.owner@stockfellow.com");
//         ownerDto.setFirstName("Group");
//         ownerDto.setLastName("Owner");
//         ownerDto.setPhone("+27123456789");
//         ownerDto.setStatus(User.UserStatus.active);

//         MvcResult result = mockMvc.perform(post("/api/users/sync")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(ownerDto)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userId").value(groupOwnerId.toString()))
//                 .andExpect(jsonPath("$.email").value("group.owner@stockfellow.com"))
//                 .andReturn();

//         // Verify user was created in database
//         Optional<User> savedUser = userRepository.findById(groupOwnerId);
//         assertThat(savedUser).isPresent();
//         assertThat(savedUser.get().getEmail()).isEqualTo("group.owner@stockfellow.com");
        
//         System.out.println("✅ Group owner created: " + groupOwnerId);
//     }


//     @Test
//     @Order(2)
//     @DisplayName("2. Create Group Members via User Sync")
//     void createGroupMembers() throws Exception {

//         List<SyncUserDto> syncDtos = new ArrayList<>();
        
//         for (int i = 0; i < memberEmails.length; i++) {
//             UUID memberId = UUID.randomUUID();
//             memberUserIds.add(memberId);
            
//             SyncUserDto memberDto = new SyncUserDto();
//             memberDto.setUserId(memberId);
//             memberDto.setEmail(memberEmails[i]);
//             String[] names = memberNames[i].split(" ");
//             memberDto.setFirstName(names[0]);
//             memberDto.setLastName(names[1]);
//             memberDto.setPhone("0712345678" + i);
//             memberDto.setStatus(User.UserStatus.active);
            
//             syncDtos.add(memberDto);
//         }

//         // Batch sync users
//         mockMvc.perform(post("/api/users/sync/batch")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(syncDtos)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(4));

//         // Verify all members were created
//         List<User> savedMembers = userRepository.findAllById(memberUserIds);
//         assertThat(savedMembers).hasSize(4);
        
//         System.out.println("✅ Group members created: " + memberUserIds.size());
//     }

//     @Test
//     @Order(3)
//     @DisplayName("3. Add Payment Details for All Users")
//     void addPaymentDetailsForUsers() throws Exception {
//         // Add payment details for group owner
//         addPaymentDetailsForUser(groupOwnerId, ownerEmail, "visa");
        
//         for (int i = 0; i < memberUserIds.size(); i++) {
//             UUID payerId = addPaymentDetailsForUser(
//                 memberUserIds.get(i), 
//                 memberEmails[i],
//                 "visa"
//             );
//             payerDetailIds.add(payerId);
//         }
        
//         System.out.println("✅ Payment details added for all users");
//     }

//     private UUID addPaymentDetailsForUser(UUID userId, String email, String type) throws Exception {
//         // Mock Paystack payment details
//         CreatePayerDetailsDto payerDto = createMockPayerDetails(userId, email, type);
        
//         MvcResult result = mockMvc.perform(post("/api/transaction/payment-methods/payer/initialize")
//                 .header(X_USER_ID_HEADER, userId.toString())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(payerDto)))
//                 .andExpect(status().isCreated())
//                 .andReturn();
                
//         String responseBody = result.getResponse().getContentAsString();
//         PayerDetailsResponseDto response = objectMapper.readValue(responseBody, PayerDetailsResponseDto.class);
        
//         return response.getPayerId();
//     }

//     private CreatePayerDetailsDto createMockPayerDetails(UUID userId, String email, String type) {
//         CreatePayerDetailsDto dto = new CreatePayerDetailsDto();
//         dto.setUserId(groupOwnerId);
//         dto.setEmail(email);
//         dto.setType("visa");
//         // dto.setBank(bankName);
//         // dto.setLast4(last4);
//         // dto.setAuthCode("AUTH_mock_" + UUID.randomUUID().toString().substring(0, 8));
//         // dto.setExpMonth(expMonth);
//         // dto.setExpYear(expYear);
//         // dto.setBin(bin);
        
//         return dto;
//     }

//     @Test
//     @Order(4)
//     @DisplayName("4. Test Complete Payer Details Flow with Paystack Webhook")
//     void testCompletePayerDetailsFlowWithWebhook() throws Exception {
//         // // Use the first member for this test
//         UUID testUserId = memberUserIds.get(0);
//         // String testUserEmail = memberEmails[0];
        
//         // // Step 1: Initialize payer details (creates partial record)
//         // CreatePayerDetailsDto payerDto = createMockPayerDetails(
//         //     testUserId, 
//         //     testUserEmail,
//         //     "visa"
//         // );
        
//         // MvcResult initResult = mockMvc.perform(post("/api/transaction/payment-methods/payer/initialize")
//         //         .header(X_USER_ID_HEADER, testUserId.toString())
//         //         .contentType(MediaType.APPLICATION_JSON)
//         //         .content(objectMapper.writeValueAsString(payerDto)))
//         //         .andExpect(status().isCreated())
//         //         .andExpect(jsonPath("$.authorization_url").exists())
//         //         .andExpect(jsonPath("$.access_code").exists())
//         //         .andExpect(jsonPath("$.reference").exists())
//         //         .andReturn();
        
//         // String initResponseBody = initResult.getResponse().getContentAsString();
//         // PayerDetailsResponseDto initResponse = objectMapper.readValue(initResponseBody, PayerDetailsResponseDto.class);
//         // UUID payerId = initResponse.getPayerId();
        
//         // Verify initial state - should have basic info but missing some Paystack details
//         MvcResult result = mockMvc.perform(get("/api/transaction/payment-methods/payer/user")
//                 .header(X_USER_ID_HEADER, testUserId.toString()))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].payerId").exists())
//                 .andReturn();
                
        
//         // String initResponseBody = result.getResponse().getContentAsString();
//         // PayerDetailsResponseDto initResponse = objectMapper.readValue(initResponseBody, PayerDetailsResponseDto.class);
//         // UUID payerId = initResponse.getPayerId();
        
//         // // Step 2: Simulate Paystack webhook with additional data
//         // PaystackWebhookDto webhookPayload = createMockPaystackWebhook(payerId, testUserId);
        
//         // // Mock the webhook signature validation if you have it
//         // String mockSignature = "mock_signature_" + UUID.randomUUID().toString();
        
//         // MvcResult webhookResult = mockMvc.perform(post("/api/transaction/webhooks/paystack")
//         //         .header("x-paystack-signature", mockSignature)
//         //         .contentType(MediaType.APPLICATION_JSON)
//         //         .content(objectMapper.writeValueAsString(webhookPayload)))
//         //         .andExpect(status().isOk())
//         //         .andReturn();
        
//         // // Step 3: Verify that payer details were updated with webhook data
//         // mockMvc.perform(get("/api/transaction/payment-methods/payer/" + payerId)
//         //         .header(X_USER_ID_HEADER, testUserId.toString()))
//         //         .andExpect(status().isOk())
//         //         .andExpect(jsonPath("$.payerId").value(payerId.toString()))
//         //         .andExpect(jsonPath("$.bank").value("Standard Bank"))
//         //         .andExpect(jsonPath("$.last4").value("1234"))
//         //         // These should now be populated from webhook
//         //         .andExpect(jsonPath("$.paystackCustomerCode").value("CUS_mock_customer"))
//         //         .andExpect(jsonPath("$.paystackAuthorizationCode").value("AUTH_mock_authorization"))
//         //         .andExpect(jsonPath("$.cardType").value("visa"))
//         //         .andExpect(jsonPath("$.accountName").value("Test Account Holder"));
        
//         // // Step 4: Verify that the payer details can now be used for transactions
//         // // (Optional) Try to create a transaction using this payer detail
//         // verifyPayerDetailsCanBeUsedForTransaction(testUserId, payerId);
        
//         // System.out.println("✅ Complete payer details flow tested successfully");
//         // System.out.println("   - Initial creation: " + payerId);
//         // System.out.println("   - Webhook processing: SUCCESS");
//         // System.out.println("   - Final verification: PASSED");
//     }

//     private PaystackWebhookDto createMockPaystackWebhook(UUID payerId, UUID userId) {
//         PaystackWebhookDto webhook = new PaystackWebhookDto();
//         webhook.setEvent("customer.created"); // or whatever event type you use
//         webhook.setData(createMockPaystackData(payerId, userId));
//         return webhook;
//     }

//     private PaystackWebhookDataDto createMockPaystackData(UUID payerId, UUID userId) {
//         PaystackWebhookDataDto data = new PaystackWebhookDataDto();
//         data.setCustomerCode("CUS_mock_customer");
//         data.setAuthorizationCode("AUTH_mock_authorization");
//         data.setCardType("visa");
//         data.setAccountName("Test Account Holder");
//         data.setSignature("SIG_mock_signature");
//         data.setChannel("card");
//         data.setCountryCode("ZA"); // or ZA for South Africa
        
//         // Include reference to link back to your payer details
//         data.setMetadata(Map.of(
//             "payerId", payerId.toString(),
//             "userId", userId.toString(),
//             "custom_field", "integration_test"
//         ));
        
//         return data;
//     }

//     private void verifyPayerDetailsCanBeUsedForTransaction(UUID userId, UUID payerId) throws Exception {
//         // Create a mock transaction to verify the payer details work
//         CreateTransactionDto transactionDto = new CreateTransactionDto();
//         transactionDto.setTransactionId(UUID.randomUUID());
//         transactionDto.setCycleId(UUID.randomUUID()); // Mock cycle
//         transactionDto.setUserId(userId);
//         transactionDto.setPayerId(payerId);
//         transactionDto.setAmount(new BigDecimal("100.00"));
//         transactionDto.setPaystackReference("REF_test_" + System.currentTimeMillis());
        
//         // This should succeed because payer details are now complete
//         mockMvc.perform(post("/api/transaction/validate-payment-method")
//                 .header(X_USER_ID_HEADER, userId.toString())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(transactionDto)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.valid").value(true));
//     }

    
//     // @Test
//     // @Order(4)
//     // @DisplayName("4. Create Group with Cycle Settings")
//     // void createGroupWithCycle() throws Exception {
//     //     // This would call your group service - you'll need to implement based on your group service API
//     //     // For now, mock the group ID
//     //     groupId = UUID.randomUUID();

//     //     CreateGroupDto groupDto = new CreateGroupDto();
//     //     groupDto.setGroupId(groupId);
//     //     groupDto.setCyclePeriod("08/25-09/25");
//     //     groupDto.setRecipientUserId(groupOwnerId);
//     //     groupDto.setContributionAmount(BigDecimal.valueOf(500));
//     //     groupDto.setExpectedTotal(BigDecimal.valueOf(2000));
//     //     groupDto.setCollectionStartDate(LocalDate.now());
//     //     groupDto.setCollectionEndDate(LocalDate.now().plusDays(10));
//     //     groupDto.setPayoutDate(LocalDate.of(2025, 8, 30));

//     //     // Mock call to group service (you'll need to implement actual HTTP call or use TestRestTemplate)
//     //     MvcResult result = mockMvc.perform(post("/api/cycles")
//     //             .header(X_USER_ID_HEADER, groupOwnerId.toString())
//     //             .contentType(MediaType.APPLICATION_JSON)
//     //             .content(objectMapper.writeValueAsString(groupDto)))
//     //             .andExpect(status().isCreated())
//     //             .andExpect(jsonPath("$.groupCycleId").exists()) 
//     //             .andReturn();
        
//     //     System.out.println("✅ Group created: " + groupId);
//     // }

//     // @Test
//     // @Order(5)
//     // @DisplayName("5. Simulate Scheduled Transaction Processing")
//     // void simulateScheduledTransactionProcessing() throws Exception {
//     //     // This would typically be triggered by your scheduler
//     //     // You might need to create a test endpoint that triggers the debit order processing
        
//     //     // Mock scenario: It's contribution day, process debit orders for the group
//     //     processContributionsForGroup(groupId);
        
//     //     System.out.println("✅ Scheduled transactions processed");
//     // }

//     // private void processContributionsForGroup(UUID groupId) throws Exception {
//     //     // Create a mock cycle ID (this would normally come from your group service)
//     //     UUID cycleId = UUID.randomUUID();
        
//     //     // Process contributions for each member
//     //     for (int i = 0; i < memberUserIds.size(); i++) {
//     //         UUID memberId = memberUserIds.get(i);
//     //         UUID payerId = payerDetailIds.get(i); // Get corresponding payer detail
            
//     //         // Create transaction DTO based on your actual structure
//     //         CreateTransactionDto transactionDto = new CreateTransactionDto();
//     //         transactionDto.setCycleId(cycleId);
//     //         transactionDto.setUserId(memberId);
//     //         transactionDto.setPayerId(payerId);
//     //         transactionDto.setAmount(new BigDecimal("500.00"));
//     //         transactionDto.setPaystackReference("REF_" + UUID.randomUUID().toString().substring(0, 8));

//     //         // Process the transaction via your actual endpoint
//     //         mockMvc.perform(post("/api/transaction/process") // Adjust endpoint as needed
//     //                 .header(X_USER_ID_HEADER, memberId.toString())
//     //                 .contentType(MediaType.APPLICATION_JSON)
//     //                 .content(objectMapper.writeValueAsString(transactionDto)))
//     //                 .andExpect(status().isOk())
//     //                 .andExpect(jsonPath("$.userId").value(memberId.toString()))
//     //                 .andExpect(jsonPath("$.amount").value(500.00));
                    
//     //         System.out.println("✅ Transaction created for user: " + memberId + " with payer: " + payerId);
//     //     }
//     // }

//     // @Test
//     // @Order(6)
//     // @DisplayName("6. Verify Transaction History")
//     // void verifyTransactionHistory() throws Exception {
//     //     // Check that transactions were created for all members
//     //     for (UUID memberId : memberUserIds) {
//     //         mockMvc.perform(get("/api/transaction/history")
//     //                 .header(X_USER_ID_HEADER, memberId.toString()))
//     //                 .andExpect(status().isOk())
//     //                 .andExpect(jsonPath("$").isArray())
//     //                 .andExpect(jsonPath("$.length()").isGreaterThan(0));
//     //     }
        
//     //     System.out.println("✅ Transaction history verified");
//     // }

//     // @Test
//     // @Order(7)
//     // @DisplayName("7. Simulate Payout Processing")
//     // void simulatePayoutProcessing() throws Exception {
//     //     // Simulate payout day - one member should receive the collected funds
//     //     UUID payoutRecipient = memberUserIds.get(0); // First member gets payout
        
//     //     // Create payout transaction
//     //     CreatePayoutDto payoutDto = new CreatePayoutDto();
//     //     // payoutDto.setRecipientUserId(payoutRecipient);
//     //     // payoutDto.setGroupId(groupId);
//     //     // payoutDto.setAmount(new BigDecimal("2000.00")); // 4 members × 500 each
//     //     // payoutDto.setDescription("Monthly payout");

//     //     // Process payout
//     //     // mockMvc.perform(post("/api/transaction/payout")
//     //     //         .header(X_USER_ID_HEADER, payoutRecipient.toString())
//     //     //         .contentType(MediaType.APPLICATION_JSON)
//     //     //         .content(objectMapper.writeValueAsString(payoutDto)))
//     //     //         .andExpect(status().isOk());
        
//     //     System.out.println("✅ Payout processed for user: " + payoutRecipient);
//     // }

//     // @Test
//     // @Order(8)
//     // @DisplayName("8. Verify Final Balances and Transaction State")
//     // void verifyFinalState() throws Exception {
//     //     // Verify that all transactions are properly recorded
//     //     // Check balances, transaction statuses, etc.
        
//     //     // Get group summary
//     //     // mockMvc.perform(get("/api/groups/" + groupId + "/summary")
//     //     //         .header(X_USER_ID_HEADER, groupOwnerId.toString()))
//     //     //         .andExpected(status().isOk())
//     //     //         .andExpect(jsonPath("$.totalContributions").value("2000.00"))
//     //     //         .andExpect(jsonPath("$.totalPayouts").value("2000.00"));
        
//     //     System.out.println("✅ Final state verification complete");
//     // }

//     static class PaystackWebhookDto {
//         private String event;
//         private PaystackWebhookDataDto data;
        
//         public PaystackWebhookDto() {}
        
//         public String getEvent() { return event; }
//         public void setEvent(String event) { this.event = event; }
        
//         public PaystackWebhookDataDto getData() { return data; }
//         public void setData(PaystackWebhookDataDto data) { this.data = data; }
//     }

//     static class CreateGroupDto {
//         private UUID groupId;
//         private String cyclePeriod;
//         private UUID recipientUserId;
//         private BigDecimal contributionAmount;
//         private BigDecimal expectedTotal;
//         private LocalDate collectionStartDate;
//         private LocalDate collectionEndDate;
//         private LocalDate payoutDate;

//         // Constructors
//         public CreateGroupDto() {}

//         // Getters and Setters
//         public UUID getGroupId() { return groupId; }
//         public String getCyclePeriod() { return cyclePeriod; }
//         public UUID getRecipientUserId() { return recipientUserId; }
//         public BigDecimal getContributionAmount() { return contributionAmount; }
//         public BigDecimal getExpectedTotal() { return expectedTotal; }
//         public LocalDate getCollectionStartDate() { return collectionStartDate; }
//         public LocalDate getCollectionEndDate() { return collectionEndDate; }
//         public LocalDate getPayoutDate() { return payoutDate; }
        
//         public void setGroupId(UUID groupId) { this.groupId = groupId; }
//         public void setCyclePeriod(String cyclePeriod) { this.cyclePeriod = cyclePeriod; }
//         public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
//         public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
//         public void setExpectedTotal(BigDecimal expectedTotal) { this.expectedTotal = expectedTotal; }
//         public void setCollectionStartDate(LocalDate collectionStartDate) { this.collectionStartDate = collectionStartDate; }
//         public void setCollectionEndDate(LocalDate collectionEndDate) { this.collectionEndDate = collectionEndDate; }
//         public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
//     }
    
//     static class CreateTransactionDto {
//         private UUID transactionId;
//         private UUID cycleId;
//         private UUID userId;
//         private UUID payerId;
//         private String paystackReference;
//         private BigDecimal amount;

//         // Static factory method
//         public CreateTransactionDto(){}

//         // Getters and Setters
//         public UUID getTransactionId() { return transactionId; }
//         public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
        
//         public UUID getCycleId() { return cycleId; }
//         public void setCycleId(UUID cycleId) { this.cycleId = cycleId; }
        
//         public UUID getUserId() { return userId; }
//         public void setUserId(UUID userId) { this.userId = userId; }
        
//         public UUID getPayerId() { return payerId; }
//         public void setPayerId(UUID payerId) { this.payerId = payerId; }
        
//         public String getPaystackReference() { return paystackReference; }
//         public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }
        
//         public BigDecimal getAmount() { return amount; }
//         public void setAmount(BigDecimal amount) { this.amount = amount; }
//     }
    
//     static class CreatePayoutDto {
//         // Add fields based on your payout DTOs
//     }
    
//     static class CreatePayerDetailsDto {
//         private UUID userId;
//         private String type = "visa";
//         private String bank; 
//         private String last4;
//         private String authCode;
//         private String expMonth;
//         private String expYear;
//         private String bin;
//         private String email;

//         public CreatePayerDetailsDto(){}

//         public CreatePayerDetailsDto(UUID userId, String email, String type){
//             this.userId = userId;
//             this.email = email;
//             this.type = type;
//         }

//         public UUID getUserId(){ return userId; }
//         public String getType(){ return type; }
//         public String getBank(){ return bank; }
//         public String getLast4(){ return last4; }
//         public String getAuthCode(){ return authCode; }
//         public String getExpMonth(){ return expMonth; }
//         public String getExpYear(){ return expYear; }
//         public String getBin(){ return bin; }
//         public String getEmail(){ return email; }
        

//         public void setUserId(UUID userId){ this.userId=userId; }
//         public void setType(String type){ this.type=type; }
//         public void setBank(String bank){ this.bank =  bank; }
//         public void setLast4(String last4){ this.last4 =  last4; }
//         public void setAuthCode(String authCode){ this.authCode =  authCode; }
//         public void setExpMonth(String expMonth){ this.expMonth=expMonth; }
//         public void setExpYear(String expYear){ this.expYear=expYear; }
//         public void setBin(String bin){ this.bin=bin; }
//         public void setEmail(String email){ this.email=email; }
//     }

//     static class PaystackWebhookDataDto {
//         private String customerCode;
//         private String authorizationCode;
//         private String cardType;
//         private String accountName;
//         private String signature;
//         private String channel;
//         private String countryCode;
//         private Map<String, String> metadata;
        
//         public PaystackWebhookDataDto() {}
        
//         // Getters and setters
//         public String getCustomerCode() { return customerCode; }
//         public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
        
//         public String getAuthorizationCode() { return authorizationCode; }
//         public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
        
//         public String getCardType() { return cardType; }
//         public void setCardType(String cardType) { this.cardType = cardType; }
        
//         public String getAccountName() { return accountName; }
//         public void setAccountName(String accountName) { this.accountName = accountName; }
        
//         public String getSignature() { return signature; }
//         public void setSignature(String signature) { this.signature = signature; }
        
//         public String getChannel() { return channel; }
//         public void setChannel(String channel) { this.channel = channel; }
        
//         public String getCountryCode() { return countryCode; }
//         public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
//         public Map<String, String> getMetadata() { return metadata; }
//         public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
//     }

//     @AfterAll
//     static void cleanUp() {
//         System.out.println("🧹 Integration test completed");
//         System.out.println("📊 Test Summary:");
//         System.out.println("   - Group Owner: " + groupOwnerId);
//         System.out.println("   - Group ID: " + groupId);
//         System.out.println("   - Members: " + memberUserIds.size());
//         System.out.println("   - Payment Methods: " + payerDetailIds.size());
//     }
// }