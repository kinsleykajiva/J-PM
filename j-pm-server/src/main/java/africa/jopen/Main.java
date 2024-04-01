
package africa.jopen;


import africa.jopen.process.AppProcess;
import africa.jopen.utils.XLogger;
import africa.jopen.utils.XSystemUtils;
import io.helidon.logging.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;




/**
 * The application main class.
 */
public class Main {


    /**
     * Cannot be instantiated.
     */
    private Main() {
    }


    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        // load logging configuration
        LogConfig.configureRuntime();
        XSystemUtils.checkForSDKs();
        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);


        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .build()
                .start();


        System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");
        
        String app  ="C:\\Users\\Kinsl\\IdeaProjects\\jar-demo\\target\\jar-demo-1.0-SNAPSHOT.jar";
        
        String app1  ="C:\\Users\\Kinsl\\IdeaProjects\\jar-demo\\target\\test-app.js";
        Thread.ofVirtual().start(() -> {
        AppProcess appProcess = new AppProcess();
        appProcess.setName("jar-demo-1.0-SNAPSHOT");
        appProcess.runApp(app,"");
            
            appProcess=null;
        });
        Thread.ofVirtual().start(() -> {
        AppProcess appProcess1 = new AppProcess();
        appProcess1.setName("js-test");
        appProcess1.runApp(app1,"");
            appProcess1= null;
        });
        
        
        /*for (int i = 0; i < 10; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    XLogger xLogger = new XLogger("test-app");
                    xLogger.log("This is a test log message");
                    xLogger.logError("This is a test log message");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            System.out.println("done " + i);
        }*/

    }


    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing
               .register("/greet", new GreetService())
               .get("/simple-greet", (req, res) -> res.send("Hello World!")); 
    }
}