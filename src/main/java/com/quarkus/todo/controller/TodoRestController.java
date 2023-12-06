package com.quarkus.todo.controller;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.quarkus.todo.dto.TodoDTO;
import com.quarkus.todo.service.TodoService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;

@Path("todo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoRestController {

	@Inject
	TodoService service;

	@Inject
	Validator validator;

	@GET
	@Operation(summary = "List To Do, description = Returns a list of Todo.class")
	@APIResponse(responseCode = "200", description = "to-do list", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TodoDTO.class, type = SchemaType.ARRAY)) })
	@PermitAll
	public Response listAllTask() {
		List<TodoDTO> todoDTOS = service.list();
		todoDTOS.stream()
				.map(TodoDTO::getName)
				.map(String::toLowerCase)
				.forEach(name -> name.equals(""));
		return Response.status(Response.Status.OK).entity(todoDTOS).build();
	}

	@POST
	@Operation(summary = "Add a task", description = "Add a task")
	@APIResponse(responseCode = "201", description = "task", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TodoDTO.class)) })
	public Response addTask(TodoDTO todo) {
		Set<ConstraintViolation<TodoDTO>> errors = validator.validate(todo);
		Random random = new Random();
		double randomNumber = random.nextDouble(100) + 1;
		if (errors.isEmpty()) {
			todo.setName(generateRandom());
			todo.setRandomNumber(randomNumber);
			service.insert(todo);
		} else {
			throw new NotFoundException(errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")).toString());
		}
		return Response.status(Response.Status.CREATED).build();
	}

	private String generateRandom() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 7;
		Random random = new Random();

		return random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}


	@DELETE
	@Path("/{id}")
	@Operation(summary = "Delete a task", description = "Delete a task")
	@APIResponse(responseCode = "202", description = "task", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TodoDTO.class)) })
	public Response deleteTask(@PathParam("id") Long id) {
		service.delete(id);
		return Response.status(Response.Status.ACCEPTED).build();
	}

	@PUT
	@Path("/{id}/{status}")
	@Operation(summary = "Edit status", description = "Edit a task based on ID")
	@APIResponse(responseCode = "200", description = "task", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TodoDTO.class)) })
	public Response updateTask(@PathParam("id") Long id, @PathParam("status") String status) {
		service.updateStatus(id, status);
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/fibonacci/{fibNumber}")
	public String cpuIntensiveOperation(@PathParam("fibNumber") int fibNumber) {
		long result = calculateFibonacci(fibNumber);
		return "Result: " + result;
	}

	private long calculateFibonacci(int n) {
		if (n <= 1) {
			return n;
		} else {
			return calculateFibonacci(n - 1) + calculateFibonacci(n - 2);
		}
	}
}
