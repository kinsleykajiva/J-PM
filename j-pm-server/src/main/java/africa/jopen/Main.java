
package africa.jopen;


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
        
        /*
        AppProcess appProcess = new AppProcess();
        appProcess.setName("jar-demo-1.0-SNAPSHOT");
        appProcess.runApp(app,"");
        
       
        
        int attempts = 0;
        int maxAttempts = 5;
        System.out.println("now here -" + appProcess.getPid());
        Long pid = appProcess.getPid();
        
        while (pid == null && attempts < maxAttempts) {
            System.out.println("now here - " + pid);
            attempts++;
            try {
                TimeUnit.SECONDS.sleep(1); // Delay for 1 second
                System.out.println("now here -" + appProcess.getPid());
             
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pid = appProcess.getPid();
        }
        
        if (pid != 0) {
            System.out.println("Process started with PID: " + pid);
            try {
                TimeUnit.SECONDS.sleep(5); // Delay for 1 second
                System.out.println("fff now here -" + appProcess.stop());
                System.out.println("xxx here -" + checkIfProcessExists(pid));
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to retrieve process ID after " + maxAttempts + " attempts.");
        }*/
    
        
        // tasklist /fi "PID eq 13056" /fo list
        //sc.exe stop <service_name> force
        /*Thread.ofVirtual().start(() -> {
            AppProcess appProcess = new AppProcess();
            appProcess.setName("jar-demo-1.0-SNAPSHOT");
            appProcess.runApp(app,"");
            
            System.out.println("now here");
        });*/
        
        /*
        Thread.ofVirtual().start(() -> {
        AppProcess appProcess1 = new AppProcess();
        appProcess1.setName("js-test");
        appProcess1.runApp(app1,"");
            appProcess1= null;
        });*/
        
     //   System.out.println( getPIDRAMUsage("13504"));;
//        getPIDRAMUsage("10");
        
        
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
               .register("/app", new AppsRunnerServiceRoute())
               .register("/greet", new GreetService())
               .get("/simple-greet", (req, res) -> res.send("Hello World!"));
    }
}