package com.sysgears.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.model.User;
import com.sysgears.user.model.UserAuth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserMutationTest {
    @Autowired
    GraphQLTestTemplate template;
    @Autowired
    UserDBInitializer userDBInitializer;

    @AfterEach
    void initUser() {
        userDBInitializer.onApplicationStartedEvent(mock(ApplicationStartedEvent.class));
    }

    @Test
    void addUser() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode input = mapper.createObjectNode();
        ObjectNode node = mapper.createObjectNode();
        ObjectNode profile = mapper.createObjectNode();

        node.put("username", "user");
        node.put("password", "supersecret");
        node.put("role", "USER");
        node.put("isActive", true);
        node.put("email", "user@sysgears.com");

        profile.put("firstName", "Edward");
        profile.put("lastName", "Fillmore");
        node.set("profile", profile);

        input.set("input", node);
        GraphQLResponse response = template.perform("mutation/add-user.graphql", input);

        assertTrue(response.isOk());
        UserPayload payload = response.get("$.data.addUser", UserPayload.class);
        User createdUser = payload.getUser();

        assertEquals("user", createdUser.getUsername());
        assertEquals("USER", createdUser.getRole());
        assertTrue(createdUser.getIsActive());
        assertEquals("user@sysgears.com", createdUser.getEmail());
        assertEquals("Edward Fillmore", createdUser.getProfile().getFullName());
        assertNull(createdUser.getAuth());
    }

    @Test
    void editUser() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode input = mapper.createObjectNode();
        ObjectNode node = mapper.createObjectNode();
        ObjectNode profile = mapper.createObjectNode();
        ObjectNode auth = mapper.createObjectNode();

        node.put("id", 1);
        node.put("username", "admin");
        node.put("password", "supersecret");
        node.put("role", "ADMIN");
        node.put("isActive", true);
        node.put("email", "admin@sysgears.com");

        profile.put("firstName", "John");
        profile.put("lastName", "Sinna");
        node.set("profile", profile);

        ObjectNode facebook = mapper.createObjectNode();
        facebook.put("fbId", "fb_id");
        facebook.put("displayName", "some");
        ObjectNode google = mapper.createObjectNode();
        google.put("googleId", "g_id");
        google.put("displayName", "google");
        ObjectNode github = mapper.createObjectNode();
        github.put("ghId", "gh_id");
        github.put("displayName", "github");
        ObjectNode certificate = mapper.createObjectNode();
        certificate.put("serial", "some_unique_id");
        ObjectNode linkedin = mapper.createObjectNode();
        linkedin.put("lnId", "ln_id");
        linkedin.put("displayName", "LinkedIn");
        auth.set("facebook", facebook);
        auth.set("google", google);
        auth.set("github", github);
        auth.set("certificate", certificate);
        auth.set("linkedin", linkedin);
        node.set("auth", auth);

        input.set("input", node);
        GraphQLResponse response = template.perform("mutation/edit-user.graphql", input);

        assertTrue(response.isOk());
        UserPayload payload = response.get("$.data.editUser", UserPayload.class);
        User createdUser = payload.getUser();

        assertEquals(1, createdUser.getId());
        assertEquals("admin", createdUser.getUsername());
        assertEquals("ADMIN", createdUser.getRole());
        assertTrue(createdUser.getIsActive());
        assertEquals("admin@sysgears.com", createdUser.getEmail());
        assertEquals("John Sinna", createdUser.getProfile().getFullName());

        UserAuth userAuth = createdUser.getAuth();
        assertNotNull(userAuth);
        assertEquals("some_unique_id", userAuth.getCertificate().getSerial());
        assertEquals("fb_id", userAuth.getFacebook().getFbId());
        assertEquals("some", userAuth.getFacebook().getDisplayName());
        assertEquals("g_id", userAuth.getGoogle().getGoogleId());
        assertEquals("google", userAuth.getGoogle().getDisplayName());
        assertEquals("gh_id", userAuth.getGithub().getGhId());
        assertEquals("github", userAuth.getGithub().getDisplayName());
        assertEquals("ln_id", userAuth.getLinkedin().getLnId());
        assertEquals("LinkedIn", userAuth.getLinkedin().getDisplayName());
    }

    @Test
    void deleteUser() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("id", 1);

        GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

        UserPayload payload = response.get("$.data.deleteUser", UserPayload.class);
        User deletedUser = payload.getUser();

        assertEquals(1, deletedUser.getId());
    }
}
