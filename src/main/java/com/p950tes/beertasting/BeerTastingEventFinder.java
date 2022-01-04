package com.p950tes.beertasting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BeerTastingEventFinder {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	private final boolean htmlOutput;
	private final boolean nofilter;
	
	public BeerTastingEventFinder(boolean htmlOutput, boolean nofilter) {
		this.htmlOutput = htmlOutput;
		this.nofilter = nofilter;
	}
	
	public void execute() throws Exception {
		
		JsonNode eventResponse = fetchEventData();
		List<Event> events = parseEventResponse(eventResponse);
		List<Event> availableEvents = findAvailableEvents(events);
		
		if (! availableEvents.isEmpty()) {
			printEvents(availableEvents);
		}	
	}
	
	private JsonNode fetchEventData() throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://taysta.se/api/events/upcoming?filter[product_id]=140"))
				.POST(BodyPublishers.ofString("{\"paginate\":true,\"per_page\":20}"))
				.header("user-agent", "insomnia/2021.7.2")
				.header("content-type", "application/json")
				.header("accept", "*/*")
				.build();
		
		HttpClient client = HttpClient.newBuilder().build();
		
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		
		if (response.statusCode() >= 300) {
			throw new IllegalStateException("Retrieval failed: " + response.statusCode() + ": " + response.body());
		}
		
		String body = response.body();
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(body);
	}
	
	private List<Event> parseEventResponse(JsonNode rootNode) {
		
		List<Event> eventList = new ArrayList<>();
		JsonNode eventListNode = rootNode.get("data");

		if (rootNode.get("total").asInt() > eventListNode.size()) {
			System.out.println("WARNING: taysta indicates that there are more events than they returned to us"
				+ "Check manually here: https://taysta.se/provningar/olprovning/uppsala");
		}
		
		for (int i = 0; i < eventListNode.size(); i++) {
			Event event = parseEvent(eventListNode.get(i));
			eventList.add(event);
		}
		return eventList;
	}
	
	private Event parseEvent(JsonNode eventNode) {
		
		Event event = new Event();
		event.setName(eventNode.get("event_name").asText());
		event.setTime(parseDateTime(eventNode.get("start").asText()));
		event.setCreated(parseDateTime(eventNode.get("created_at").asText()));
		event.setUpdated(parseDateTime(eventNode.get("updated_at").asText()));
		event.setFull(eventNode.get("show_as_fullybooked").asBoolean());
		event.setCancelled(eventNode.get("show_as_cancelled").asBoolean());
		return event;
	}
	
	private static LocalDateTime parseDateTime(String dateString) {
		return LocalDateTime.parse(dateString, DATE_FORMAT);
	}

	private List<Event> findAvailableEvents(List<Event> events) {
		if (nofilter) {
			return events;
		}
		List<Event> availableEvents = events.stream()
			.filter(Event::isAvailable)
			.filter(event -> ! event.getName().startsWith("Ost "))
			.toList();
		return availableEvents;
	}

	private void printEvents(List<Event> events) {
		String newline = htmlOutput ? "<br />" : "";
		System.out.println("Beertasting events found! " + newline);
		if (htmlOutput) {
			System.out.println("Url: <a href=\"https://taysta.se/provningar/olprovning/uppsala\">https://taysta.se/provningar/olprovning/uppsala</a><br/><br/>");
		} else {
			System.out.println("Url: https://taysta.se/provningar/olprovning/uppsala\n");
		}
		for (Event event : events) {
			System.out.println(event.getName() + newline);
			System.out.println("Time: " + event.getTime() + newline);
			System.out.println("Created: " + event.getCreated() + newline);
			System.out.println("Updated: " + event.getUpdated() + newline);
			System.out.println(newline);
		}
		System.out.println(newline);
	}
	
	public static void main(String[] argsArray) throws Exception {
		Set<String> args = Set.of(argsArray);
		
		var eventFinder = new BeerTastingEventFinder(args.contains("--html"), args.contains("--nofilter"));
		eventFinder.execute();
	}
}
