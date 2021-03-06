package server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.accounts.AccountManager;
import server.api.*;
import server.matches.Match;
import server.matches.MatchManager;
import server.notifications.NotificationManager;
import spark.Request;
import spark.Response;
import spark.Spark;
import static spark.Spark.secure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MicroServer {

    private final Logger log = LoggerFactory.getLogger(MicroServer.class);

    MicroServer(int serverPort) {
        configureServer(serverPort);
        serveStaticPages();
        processRestfulAPIrequests();

    }

    private void configureServer(int serverPort) {
        Spark.port(serverPort);
        String keystoreFile = System.getenv("KEYSTORE_FILE");
        String keystorePassword = System.getenv("KEYSTORE_PASSWORD");
        if (keystoreFile != null && keystorePassword != null) {
            secure(keystoreFile, keystorePassword, null, null);
        }
    }


    private void serveStaticPages() {
        String path = "/public/";
        Spark.staticFileLocation(path);
        Spark.get("/", (req, res) -> { res.redirect("index.html"); return null; });
    }

    private void processRestfulAPIrequests() {
        Spark.get("/api/echo", this::echoHTTPrequest);
        Spark.post("/api/register", this::handleRegisterRequest);
        Spark.post("/api/login", this::handleLoginRequest);
        Spark.post("/api/notifications", this::handleNotificationsRequest);
        Spark.post("/api/message", this::handleMessageRequest);
        Spark.post("/api/move", this::handleMoveRequest);
        Spark.post("/api/inviteAnswer", this::handleInviteResponses);
        Spark.post("/api/ViewCurrentGames", this::handleViewCurrentGamesResponse);
        Spark.post("/api/matches" , this::handleMatchResponse);
        Spark.post("/api/deregister", this::handleDeregisterRequest);
        Spark.post("/api/markRead" , this::handleMarkReadResponse);
    }

    private Object handleMarkReadResponse(Request request, Response response){
        response.type("application/json");
        Gson gson = new GsonBuilder().create();
        MarkReadRequest markReadRequest = gson.fromJson(request.body(), MarkReadRequest.class);
        if(!markReadRequest.verify()){
            response.status(401);
            return "{\"message\": \"Authentication Error\"}";
        }
        return gson.toJson(NotificationManager.markRead(markReadRequest.getAccountId(), markReadRequest.id, markReadRequest.delete));
    }


    private Object handleDeregisterRequest(Request request, Response response) {
        response.type("application/json");
        Gson gson = new GsonBuilder().create();

        DeregisterRequest deregisterRequest = gson.fromJson(request.body(), DeregisterRequest.class);
        if(!deregisterRequest.verify()){
            response.status(401);
            return "false";
        }
        return gson.toJson(AccountManager.deleteAccount(deregisterRequest.getAccountId()));
    }

    private Object handleMatchResponse(Request request, Response response){
        response.type("application/json");
        Gson gson = new GsonBuilder().registerTypeAdapter(Match.class, new Match.MatchSerializer()).create();

       MatchRequest matchRequest = gson.fromJson(request.body(), MatchRequest.class);
        if(!matchRequest.verify()){
            response.status(401);
            return "{\"message\": \"Authentication Error\"}";
        }
        return gson.toJson(MatchManager.getMatchById(matchRequest.matchID, matchRequest.getAccountId()));
    }

    private Object handleMoveRequest(Request request, Response response) {
        response.type("application/json");
        Gson gson = new GsonBuilder().create();
        MoveRequest moveRequest = gson.fromJson(request.body(), MoveRequest.class);
        if(!moveRequest.verify()){
            response.status(401);
            MoveResponse messageResponse = new MoveResponse();
            messageResponse.success = false;
            messageResponse.message = "Authentication Error";
            return gson.toJson(messageResponse);
        }
        return gson.toJson(MatchManager.makeMove(moveRequest));
    }

    private Object handleInviteResponses(Request request, Response response) {
        response.type("application/json");
        Gson gson = new GsonBuilder().registerTypeAdapter(Match.class, new Match.MatchSerializer()).create();

        InviteAnswer answer = gson.fromJson(request.body(), InviteAnswer.class);
        if(!answer.verify()){
            response.status(401);
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.success = false;
            messageResponse.errorMessage = "Authentication Error";
            return gson.toJson(messageResponse);
        }
        return gson.toJson(MatchManager.createMatchFromInvite(answer));
    }

    private String handleViewCurrentGamesResponse(Request request, Response response){
        response.type("application/json");
        response.header("Access-Control-Allow-Origin", "*");
        Gson gson = new GsonBuilder().create();
        MatchesRequest matchesRequest = gson.fromJson(request.body(), MatchesRequest.class);
        if(!matchesRequest.verify()){
            response.status(401);
            return "{\"message\": \"Authentication Error\"}";
        }
        return gson.toJson(MatchManager.getMatchesByUserId(matchesRequest.userID, matchesRequest.finishedGames));
    }

    private String handleMessageRequest(Request request, Response response) {
        response.type("application/json");
        Gson gson = new GsonBuilder().create();
        MessageRequest messageRequest = gson.fromJson(request.body(), MessageRequest.class);
        MessageResponse messageResponse;
        if(!messageRequest.verify()){
            response.status(401);
            messageResponse = new MessageResponse();
            messageResponse.success = false;
            messageResponse.errorMessage = "Authentication Error";
        }
        else{
            messageResponse = NotificationManager.sendMessage(messageRequest);
        }
        return gson.toJson(messageResponse);
    }


    private String handleNotificationsRequest(Request request, Response response) {
        response.type("application/json");
        response.header("Access-Control-Allow-Origin", "*");
        Gson gson = new GsonBuilder().create();
        NotificationsRequest notificationsRequest = gson.fromJson(request.body(), NotificationsRequest.class);
        if(!notificationsRequest.verify()){
            response.status(401);
            return "{\"message\": \"Authentication Error\"}";
        }
        return gson.toJson(NotificationManager.getRecentOrUnreadNotifications(notificationsRequest.getAccountId()));
    }

    private String echoHTTPrequest(Request request, Response response) {
        response.type("application/json");
        response.header("Access-Control-Allow-Origin", "*");
        return HTTPrequestToJson(request);
    }

    private String handleRegisterRequest(Request request, Response response){
        response.type("application/json");
        Gson gson = new GsonBuilder().create();
        RegistrationRequest registrationRequest= gson.fromJson(request.body(), RegistrationRequest.class);
        return gson.toJson(AccountManager.registerUser(registrationRequest));
    }

    private String handleLoginRequest(Request request, Response response){
        response.type("application/json");
        response.header("Access-Control-Allow-Origin", "*");
        Gson gson = new GsonBuilder().create();
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        return gson.toJson(AccountManager.loginUser(loginRequest));
    }

    private String HTTPrequestToJson(Request request) {
        return "{\n"
                + "\"attributes\":\"" + request.attributes() + "\",\n"
                + "\"body\":\"" + request.body() + "\",\n"
                + "\"contentLength\":\"" + request.contentLength() + "\",\n"
                + "\"contentType\":\"" + request.contentType() + "\",\n"
                + "\"contextPath\":\"" + request.contextPath() + "\",\n"
                + "\"cookies\":\"" + request.cookies() + "\",\n"
                + "\"headers\":\"" + request.headers() + "\",\n"
                + "\"host\":\"" + request.host() + "\",\n"
                + "\"ip\":\"" + request.ip() + "\",\n"
                + "\"params\":\"" + request.params() + "\",\n"
                + "\"pathInfo\":\"" + request.pathInfo() + "\",\n"
                + "\"serverPort\":\"" + request.port() + "\",\n"
                + "\"protocol\":\"" + request.protocol() + "\",\n"
                + "\"queryParams\":\"" + request.queryParams() + "\",\n"
                + "\"requestMethod\":\"" + request.requestMethod() + "\",\n"
                + "\"scheme\":\"" + request.scheme() + "\",\n"
                + "\"servletPath\":\"" + request.servletPath() + "\",\n"
                + "\"session\":\"" + request.session() + "\",\n"
                + "\"uri()\":\"" + request.uri() + "\",\n"
                + "\"url()\":\"" + request.url() + "\",\n"
                + "\"userAgent\":\"" + request.userAgent() + "\"\n"
                + "}";
    }

    public static void main(String[] args){
        MicroServer myServer = new MicroServer(8055);

    }

}