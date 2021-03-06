package com.j2ee.course_management.controller;

import java.util.Map;

import com.j2ee.course_management.exception.BadRequestException;
import com.j2ee.course_management.exception.ForbiddenException;
import com.j2ee.course_management.exception.NotFoundException;
import com.j2ee.course_management.model.User;
import com.j2ee.course_management.service.command.UserCommand;
import com.j2ee.course_management.service.query.UserQuery;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserQuery userQuery;

	@Autowired
	private UserCommand userCommand;

	@ApiOperation(value="Add new user account")
	@RequestMapping(
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<User> createUser(@RequestBody User user) {
		User newUser = this.userCommand.createUser(user);
		return new ResponseEntity<>(newUser, HttpStatus.CREATED);
	}

	@ApiOperation(value="Get all/some users")
	@RequestMapping(
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<Page<User>> getUsers(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size) {
		Map<String, Integer> pageAttributes = PageValidator.validatePageAndSize(page, size);
		page = pageAttributes.get("page");
		size = pageAttributes.get("size");

		Page<User> users = this.userQuery.findAll(page, size);
		if (page > users.getTotalPages()) {
			throw BadRequestException.create("Bad Request: Page number does not exist");
		}
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@ApiOperation(value="Find user by username")
	@RequestMapping(
			value = "/userId",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<User> getUser(@PathVariable("userId") Long userId) {
		User user = this.userQuery.findById(userId);
		if (user == null) {
			throw NotFoundException.create("Not Found: User with id, {0} does not exist", userId);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@ApiOperation(value="Find user by email")
	@RequestMapping(
			value = "/email",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<User> getUser(@PathVariable("email") String email) {
		User user = this.userQuery.findByEmail(email);
		if (user == null) {
			throw NotFoundException.create("Not Found: User with email, {0} does not exist", email);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@ApiOperation(value="Update an existing user's account")
	@RequestMapping(
			value = "/userId",
			method = RequestMethod.PUT,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable("userId") Long userId) {
		if (userId == user.getId()) {
			User updatedUser = this.userCommand.updateUser(user);
			return new ResponseEntity<>(updatedUser, HttpStatus.OK);
		}
		throw ForbiddenException.create("Forbidden: User id used in model does not match that on the path");
	}

}
