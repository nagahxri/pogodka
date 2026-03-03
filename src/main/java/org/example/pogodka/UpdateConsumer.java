package org.example.pogodka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;



@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    Dotenv dotenv = Dotenv.load();
    private static TelegramClient telegramClient;

    //real tg token is not allowed to give here in the project (security risk)
    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient(dotenv.get("TELEGRAM_TOKEN"));
        //added .env here
        //registerBotCommands();
    }



    public class LocationResponse {
        List<Location> locations;
        public void setLocations(List<Location> locations) {
            this.locations = locations;
        }
        public List<Location> getLocations() {
            return locations;
        }

        static class Location {
            String id;
            String name;

            public void setId(String id) {
                this.id = id;
            }
            public String getId() {
                return id;
            }
            public void setName(String name) {
                this.name = name;
            }
            public String getName() {
                return name;
            }
        }
    }
    public class DepartureResponse {
        List<Departure> departures;

        public void setDepartures(List<Departure> departures) {
            this.departures = departures;
        }
        public List<Departure> getDepartures() {
            return departures;
        }

        static class Departure {
            String when;
            int delay;
            Line line;
            String direction;

            public void setDirection(String direction) {
                this.direction = direction;
            }
            public String getDirection() {
                return direction;
            }
            public void setLine(Line line) {
                this.line = line;
            }
            public Line getLine() {
                return line;
            }
            public void setWhen(String when) {
                this.when = when;
            }
            public String getWhen() {
                return when;
            }
            public void setDelay(int delay) {
                this.delay = delay;
            }
            public int getDelay() {
                return delay;
            }
        }

        static class Line {
            String name;
        }
    }

    public void ostanovkaUni(long chatId) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();
        String stopName = "Universität";
        // GET DEPARTURES
        String departuresUrl = dotenv.get("stop_uni_api");
        //added .env here
        HttpRequest depRequest = HttpRequest.newBuilder()
                .uri(URI.create(departuresUrl))
                .GET()
                .build();

        HttpResponse<String> depResponse =
                client.send(depRequest, HttpResponse.BodyHandlers.ofString());
        //System.out.println(depResponse.body());
        DepartureResponse departures =
                gson.fromJson(depResponse.body(), DepartureResponse.class);


        OffsetDateTime now = OffsetDateTime.now();

        for (DepartureResponse.Departure d : departures.departures) {

            OffsetDateTime departureTime = OffsetDateTime.parse(d.when);
            long minutes = Duration.between(now, departureTime).toMinutes();

            if (minutes < 0) continue;

            System.out.println(
                    d.line.name + " → " + d.direction + " — " + minutes + " min"
            );
//            switch (d.line.name) {
//                case "Bus 4": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                case "Bus 6": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                default: sendMessage(update.getMessage().getChatId(), "next hour there is no buses to the university");
//            }

            if ((d.line.name.equals("Bus 4") && !d.direction.equals("Flughafen/Airport")) || d.line.name.equals("Bus 6")) {
                if (d.delay == 0) {
                    sendMessage(chatId, "next departure is " + d.line.name + " in " + minutes + " minutes");
                } else {
                    sendMessage(chatId, "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
                }
            }
            //sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes");

        }
    }
    public void ostanovkaHome(long chatId) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();
        String stopName = "Hauptmann-Hermann-Platz";
        // GET DEPARTURES
        String departuresUrl = dotenv.get("stop_home_api");
        //added .env here
        HttpRequest depRequest = HttpRequest.newBuilder()
                .uri(URI.create(departuresUrl))
                .GET()
                .build();

        HttpResponse<String> depResponse =
                client.send(depRequest, HttpResponse.BodyHandlers.ofString());
        //System.out.println(depResponse.body());
        DepartureResponse departures =
                gson.fromJson(depResponse.body(), DepartureResponse.class);


        OffsetDateTime now = OffsetDateTime.now();

        for (DepartureResponse.Departure d : departures.departures) {

            OffsetDateTime departureTime = OffsetDateTime.parse(d.when);
            long minutes = Duration.between(now, departureTime).toMinutes();

            if (minutes < 0) continue;

            System.out.println(
                    d.line.name + " → " + d.direction + " — " + minutes + " min"
            );
//            switch (d.line.name) {
//                case "Bus 4": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                case "Bus 6": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                default: sendMessage(update.getMessage().getChatId(), "next hour there is no buses to the university");
//            }

            if ((d.line.name.equals("Bus 4") && !d.direction.equals("Flughafen/Airport")) || d.line.name.equals("Bus 6")) {
                if (d.delay == 0) {
                    sendMessage(chatId, "next departure is " + d.line.name + " in " + minutes + " minutes");
                } else {
                    sendMessage(chatId, "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
                }
            }
            //sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes");

        }
    }

    public void weather(long chatId) throws URISyntaxException, IOException, InterruptedException {
        String url = dotenv.get("weather_api");
        //add .env here
        //Gson gson = new Gson();
        //String jsonReq = gson.toJson(city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        //response = (HttpResponse<String>) gson.fromJson(response.body(), City.class);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);

        double tempC = jsonObject
                .getAsJsonObject("current")
                .get("temp_c")
                .getAsDouble();
        String timeLoc = jsonObject
                .getAsJsonObject("location")
                .get("localtime")
                .getAsString();
        double windKmh = jsonObject
                .getAsJsonObject("current")
                .get("wind_kph").
                getAsDouble();
        sendMessage(chatId, "Temperature in Klagenfurt is: " + tempC +
                "; Wind speed is: " + windKmh + "; At local time: " + timeLoc);
    }

    @SneakyThrows
    @Override
    public void consume (Update update){

        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            messageText = messageText.toLowerCase();
            messageText = messageText.strip();
            System.out.println(messageText + " " + chatId);
//            if(messageText.equals("ostanovka")){
//                ostanovka(update);
//            }
//            if(messageText.equals("weather")){
//                weather(update);
//            } else {
//                //sendMessage(chatId, "Hello, u sent " + messageText);
//            }
//            if(messageText.contains("good morning")){
//                sendMessage(chatId, "Good morning, roman! ");
//                weather(chatId);
//                ostanovkaHome(chatId);
//            }
            if (messageText.startsWith("/start")) {
                sendMainMenu(chatId);
                //else sends a message that it doesnt understand a thing and recommends to press /start
            } /*else {
            sendMessage(chatId, "Sorry, i dont understand you, type /start to start");
        }*/

        } else {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
                return;
            }
        }
    }
    private void handleCallbackQuery(CallbackQuery callbackQuery) throws URISyntaxException, IOException, InterruptedException {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        Update update = new Update();
        update.setCallbackQuery(callbackQuery); // Associate callbackQuery with Update
        switch (data) {
            case "HOME" -> {// Ensure update is initialized
                weather(chatId);
                ostanovkaHome(chatId);
            }
            case "UNIVERSITY" -> {
                weather(chatId);
                ostanovkaUni(chatId);
            }
            //case "shure" -> sendListShure(chatId);
            //default -> sendMessage(chatId, "Sorry i dont understand you, type /start to start");
        }
    }
    @SneakyThrows
    public static void sendMainMenu(Long chatId){
        SendMessage message = SendMessage.builder().text("Hello, I am a bot. I can tell you the weather and the next departures of the buses.").chatId(chatId).build();
        //sets the first button of the main menu
        var button1 = InlineKeyboardButton.builder().text("UNIVERCITY TO HOME").callbackData("UNIVERSITY").build();
        //sets the second button of main menu
        var button2 = InlineKeyboardButton.builder().text("HOME TO UNI").callbackData("HOME").build();
        //sets the trird button of main menu
        List<InlineKeyboardRow> buttons = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(buttons);

        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }
    @SneakyThrows
    public static void sendMessage(
            Long chatId,
            String messageText
    ) {
        SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();
        telegramClient.execute(message);
    }



}
