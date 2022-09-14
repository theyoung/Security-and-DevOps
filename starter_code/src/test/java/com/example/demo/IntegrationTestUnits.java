package com.example.demo;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.requests.CreateUserRequest;

import com.example.demo.model.requests.ModifyCartRequest;
import com.example.demo.model.requests.UserLoginRequest;
import com.example.demo.security.SecurityConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTestUnits {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private long id;
	private String token;
	private HttpHeaders headers = new HttpHeaders();

	private final String username = "member1";
	private final String password = "1234567";

	@BeforeAll
	void registerUser() throws JsonProcessingException {
		System.out.println("[Start] Register User to test for everyone in this class");
		//given
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername(username);
		request.setPassword(password);
		request.setConfirmPassword(password);

		//when
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:"+port+"/api/user/create", HttpMethod.POST, new HttpEntity<>(request), User.class);
		User user = response.getBody();

		//then
		Assertions.assertEquals(request.getUsername(), user.getUsername());
		this.id = user.getId();
		System.out.println("[End] Register User to test for everyone in this class");

		//given
		UserLoginRequest userLoginRequest = new UserLoginRequest();
		userLoginRequest.setUsername(user.getUsername());
		userLoginRequest.setPassword(request.getPassword());

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(userLoginRequest);

		//when
		//Retrieve Token from eCommerce
		ResponseEntity<Object> header = restTemplate.exchange("http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(json), Object.class);
		String token = header.getHeaders().get("Authorization").get(0);

		//then
		Assertions.assertTrue(token.startsWith(SecurityConstants.TOKEN_PREFIX));
		this.token = token;

		//update header
		this.headers.add("Authorization",this.token);
	}

	@Test
	void registerNewUser(){
		//given
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("member2");
		request.setPassword("1234567");
		request.setConfirmPassword("1234567");

		//when
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:"+port+"/api/user/create", HttpMethod.POST, new HttpEntity<>(request), User.class);
		User user = response.getBody();

		//result
		Assertions.assertEquals(request.getUsername(), user.getUsername());
	}

	@Test
	void registerNewUserWithShortPassword(){
		//given
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("member3");
		request.setPassword("123");
		request.setConfirmPassword("123");

		//when
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:"+port+"/api/user/create", HttpMethod.POST, new HttpEntity<>(request), User.class);

		Assertions.assertTrue(response.getStatusCode().isError());
	}

	@Test
	void getUserByIdWithoutToken(){
		//given
		long testId = this.id;

		//when
		ResponseEntity<User> response = restTemplate.getForEntity("http://localhost:"+port+"/api/user/id/"+testId, User.class);

		//result
		Assertions.assertTrue(response.getStatusCode().isError());
	}

	@Test
	void getUserByIdWithToken(){

		//given
		long testId = this.id;

		//when
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:"+port+"/api/user/id/"+testId,HttpMethod.GET,new HttpEntity<>(this.headers), User.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertEquals(testId, response.getBody().getId());
	}

	@Test
	void getUserByNameWithToken(){
		//given
		String name = this.username;

		//when
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:"+port+"/api/user/"+name,HttpMethod.GET,new HttpEntity<>(this.headers), User.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertEquals(name, response.getBody().getUsername());
	}

	@Test
	void getAllItemsWithToken(){
		//given

		//when
		ResponseEntity<List> response = restTemplate.exchange("http://localhost:"+port+"/api/item",HttpMethod.GET,new HttpEntity<>(this.headers), List.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertTrue(0 < response.getBody().size());
	}

	@Test
	void getItemByIdWithToken(){
		//given

		//when
		ResponseEntity<Item> response = restTemplate.exchange("http://localhost:"+port+"/api/item/1",HttpMethod.GET,new HttpEntity<>(this.headers), Item.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertNotNull(response.getBody());
	}

	@Test
	void getItemByNameWithToken(){
		//given
		String itemName = "Square Widget";

		//when
		ResponseEntity<List> response = restTemplate.exchange("http://localhost:"+port+"/api/item/name/"+itemName,HttpMethod.GET,new HttpEntity<>(this.headers), List.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertNotNull(response.getBody());
	}

	@Test
	void addToCartWithToken(){
		//given
		ModifyCartRequest cartRequest = new ModifyCartRequest();
		cartRequest.setUsername(this.username);
		cartRequest.setItemId(1);
		cartRequest.setQuantity(2);

		//when
		ResponseEntity<Cart> response = restTemplate.exchange("http://localhost:"+port+"/api/cart/addToCart",HttpMethod.POST,new HttpEntity<>(cartRequest,this.headers), Cart.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertNotNull(response.getBody());
		Assertions.assertEquals(2, response.getBody().getItems().size());
	}

	@Test
	void removeCartWithToken(){
		//given
		ModifyCartRequest cartRequest = new ModifyCartRequest();
		cartRequest.setUsername(this.username);
		cartRequest.setItemId(1);
		cartRequest.setQuantity(2);

		//when
		ResponseEntity<Cart> response = restTemplate.exchange("http://localhost:"+port+"/api/cart/removeFromCart",HttpMethod.POST,new HttpEntity<>(cartRequest,this.headers), Cart.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertNotNull(response.getBody());
		Assertions.assertEquals(0, response.getBody().getItems().size());
	}

	@Test
	void orderSubmitNoCartWithToken(){

		//given
		String name = "nobody";

		//when
		ResponseEntity<UserOrder> response = restTemplate.exchange("http://localhost:"+port+"/api/order/submit/"+name,HttpMethod.POST,new HttpEntity<>(this.headers), UserOrder.class);

		//result
		Assertions.assertTrue(response.getStatusCode().isError());

	}

	@Test
	void orderSubmitWithToken(){
		this.addToCartWithToken();

		//given
		String name = this.username;

		//when
		ResponseEntity<UserOrder> response = restTemplate.exchange("http://localhost:"+port+"/api/order/submit/"+name,HttpMethod.POST,new HttpEntity<>(this.headers), UserOrder.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertNotNull(response.getBody());
		Assertions.assertEquals(2,response.getBody().getItems().size());

		orderHistoryOfOrderWithToken();
	}


	void orderHistoryOfOrderWithToken(){
		//given
		String name = this.username;

		//when
		ResponseEntity<List> response = restTemplate.exchange("http://localhost:"+port+"/api/order/history/"+name,HttpMethod.GET,new HttpEntity<>(this.headers), List.class);

		//result
		Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assertions.assertTrue(0 < response.getBody().size());
	}



}
