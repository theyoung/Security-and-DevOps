package com.example.demo.controllers;


import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.security.WebSecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

    @MockBean
    CartRepository cartRepository;

    @MockBean
    ItemRepository itemRepository;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    UserDetailsService userDetailsService;
    PasswordEncoder bCryptPasswordEncoder;


    @Test
    public void createUser() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("abcd");
        createUserRequest.setPassword("1234567");
        createUserRequest.setConfirmPassword("1234567");

        ObjectMapper mapper = new ObjectMapper();
        String requestString = mapper.writeValueAsString(createUserRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/create").contentType(MediaType.APPLICATION_JSON).content(requestString))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getUserById() throws Exception {
        User user = new User();
        user.setUsername("abcd");
        user.setCart(new Cart());

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/id/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.username").value("abcd"))
                .andDo(print());
    }

    @Test
    public void getUserByName() throws Exception {
        User user = new User();
        user.setUsername("abcd");
        user.setCart(new Cart());

        given(userRepository.findByUsername("abcd")).willReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/abcd"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.username").value("abcd"))
                .andDo(print());
    }
}