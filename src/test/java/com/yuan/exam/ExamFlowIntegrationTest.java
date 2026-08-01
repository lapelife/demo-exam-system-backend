package com.yuan.exam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExamFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void studentCanLoginStartAndAdminCannotTake() throws Exception {
        String studentToken = login("student", "123456");
        assertThat(studentToken).isNotBlank();

        MvcResult exams = mockMvc.perform(get("/api/exams")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode examList = objectMapper.readTree(exams.getResponse().getContentAsString()).path("data").path("list");
        assertThat(examList.isArray()).isTrue();
        assertThat(examList.size()).isGreaterThan(0);
        long examId = examList.get(0).path("id").asLong();

        // 学生题目列表不应包含答案
        MvcResult qs = mockMvc.perform(get("/api/exams/" + examId + "/questions")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode q0 = objectMapper.readTree(qs.getResponse().getContentAsString()).path("data").get(0);
        assertThat(q0.path("answer").isNull() || q0.path("answer").asText().isEmpty()).isTrue();

        String adminToken = login("admin", "123456");
        mockMvc.perform(post("/api/take/start/" + examId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .toString();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }
}
