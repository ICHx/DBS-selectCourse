import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Main {
    /**
     *
     */
    private static final String MIME_JSON = "application/json";
    public static int DEBUG = 1; // if set as 0, will not auto-rollback
    public static final int MAXCREDIT = 21; 

    public static void main(String[] args) {
        // Maven dependency help: http://hk.uwenku.com/question/p-ezcjtqat-va.html

        // Model
        SqlModel dbModel;
        if (args.length > 0) {
            File f = new File(args[args.length - 1]);
            dbModel = new SqlModel(f);
        } else {
            dbModel = new SqlModel();

        }

        // Control
        // Starts the webapp on localhost and the port defined by the PORT
        // environment variable when present, otherwise on 8080.
        final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        port(PORT);
        System.out.println("Listening on :" + PORT);

        staticFiles.location("/public");

        // debug info
        get("/isdebug", (request, response) -> {
            return DEBUG;
        });

        // ! List all courses
        get("/courses", (request, response) -> {

            // https://github.com/sparktutorials/BlogService_SparkExample/blob/47e4076564a1d09d02431ecf09f8e27f719b66d1/src/main/java/me/tomassetti/AbstractRequestHandler.java
            response.status(200);
            response.type(MIME_JSON);
            System.out.println("get /courses");
            return dataToJson(dbModel.listCourses());

        });

        // ! List my courses
        get("/myCourses", (request, response) -> {

            // https://github.com/sparktutorials/BlogService_SparkExample/blob/47e4076564a1d09d02431ecf09f8e27f719b66d1/src/main/java/me/tomassetti/AbstractRequestHandler.java

            System.out.println(request.url());
            response.status(200);
            response.type(MIME_JSON);
            String netID = request.queryParams("User");
            System.out.println("get /myCourses for " + netID);
            String json1 = dataToJson(dbModel.listMyCourses(netID));
            return json1;

        });

        // ! List my previous courses
        get("/myHistoryCourses", (request, response) -> {

            // https://github.com/sparktutorials/BlogService_SparkExample/blob/47e4076564a1d09d02431ecf09f8e27f719b66d1/src/main/java/me/tomassetti/AbstractRequestHandler.java
            response.status(200);
            response.type(MIME_JSON);
            String netID = request.queryParams("User");
            System.out.println("get /myHistoryCourses for " + netID);
            String json1 = dataToJson(dbModel.listMyHistory(netID));
            return json1;

        });

        // ! Authenticate
        // matches "GET /hello/foo" and "GET /hello/bar"
        // request.params(":netID") is 'foo' or 'bar'
        get("/hello/:netID", (request, response) -> {
            return "Hello: " + request.params(":netID");
        });

        get("/login/:netID", (request, response) -> {
            // to login as some user, password mechanisms are not implemented
            // if required to, they are hashed as a combination of user and password

            boolean result = dbModel.loginAs(request.params(":netID"));
            System.out.println(request.params(":netID") + " tried login and " + result);
            return result;
        });

        // ! add & drop handler

        get("/add/:course", (request, response) -> {
            String status = ""; // json

            // todo
            System.out.println(request.url());
            String netID = request.queryParams("User");
            String cId = request.params("course");
            System.out.println("request /add course " + cId + " for " + netID);

            // sql portion, response in json
            status = dataToJson(dbModel.addCourse(netID, cId));

            return status;
        });

        get("/drop/:course", (request, response) -> {
            String status = "";
            // todo
            System.out.println(request.url());
            String netID = request.queryParams("User");
            String cId = request.params("course");
            System.out.println("request /drop course " + cId + " for " + netID);
            status = dataToJson(dbModel.dropCourse(netID, cId));
            return status;
        });

        // ! Search: not implemented
        // ? using select * from ... likes ('keyword',...)
        get("/search", (request, response) -> {

            System.out.println(request.url());
            String keyString = request.queryParams("keyword");
            System.out.println("request /search course " + request.params("keyword") + " for " + keyString);
            return false;
        });

        // ! Matches meaningless queries
        get("/login", (request, response) -> {
            return false;
        });

        get("/login/", (request, response) -> {
            return false;
        });

    } // end of main

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            System.err.println("IOException from a StringWriter?");
            e.printStackTrace();
            return null;
        }
    }

}
