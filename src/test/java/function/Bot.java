package function;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.ConfigReader;
import com.mongodb.Block;
import com.mongodb.client.*;
import com.mongodb.MongoClientURI;
import org.bson.Document;
import utilities.WSClient;

import static com.mongodb.client.model.Filters.eq;


import java.time.Duration;

public class Bot {

    ConfigReader config = new ConfigReader();
    WSClient wsClient = new WSClient();

    public void solvePuzzle() throws Exception{
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + config.getConfig("chromeDriverPath"));

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(Long.parseLong(config.getConfig("waitSeconds"))));

        driver.get(config.getConfig("appUrl"));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(config.getConfig("waitSeconds"))));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Long.parseLong(config.getConfig("waitSeconds"))));

        //Enter The Door
        driver.findElement(By.xpath("//img[@src='/static/door.png']")).click();

        //Start Puzzle
        driver.findElement(By.id("start")).click();
        Thread.sleep(2000);

        //Puzzle 1 - Random Access
        int buttonCount = driver.findElements(By.xpath("//button[contains(@id,'c1submitbutton')]")).size();
        for(int i=1;i<=buttonCount;i++) {
            driver.findElement(By.id("c1submitbutton" + i)).click();
            if(driver.findElements(By.xpath("//span/h2[.='A Video Player']")).size() > 0){
                System.out.println("Puzzle 1 Completed");
                break;
            }
        }

        //Puzzle 2 - Video Player
        Thread.sleep(2000);
        driver.switchTo().frame("aVideoPlayer");
        WebElement playButton = driver.findElement(By.xpath("//button[@class='ytp-large-play-button ytp-button']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", playButton);
        playButton.click();

        Thread.sleep(30000);

        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.xpath("//button[@class='ytp-mute-button ytp-button']"))).perform();
        driver.findElement(By.xpath("//button[@class='ytp-mute-button ytp-button']")).click();
        driver.switchTo().defaultContent();
        driver.findElement(By.xpath("//button[.='Proceed']")).click();
        if(driver.findElements(By.xpath("//span/h2[.='Crystal Maze']")).size() > 0){
            System.out.println("Puzzle 2 Completed");
        }

        //Puzzle 3 - Crystal Maze
        Thread.sleep(2000);
        WebElement mazePointer = driver.findElement(By.xpath("//td[contains(@class,'deep-purple')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", mazePointer);
        //WebElement purpleDestination = driver.findElement(By.xpath("//td[@class='x11 y10 deep-purple']"));

        String greenObj = driver.findElement(By.xpath("//td[contains(@class,'green')]")).getAttribute("class");
        int greenObjxPos = Integer.parseInt(greenObj.split(" ")[0].replace("x", ""));
        int greenObjyPos = Integer.parseInt(greenObj.split(" ")[1].replace("y", ""));

        while(driver.findElements(By.xpath("//td[@class='x"+greenObjxPos+" y"+greenObjyPos+" deep-purple']")).size() == 0) {
            String key[] = config.getConfig("crystalMazekey").split("-");
            for(String keyItem : key) {
                String direction = keyItem.substring(0,1);
                int counter = Integer.parseInt(keyItem.substring(1, 2));
                for(int i=1;i<=counter;i++) {
                    if(direction.equalsIgnoreCase("f")){
                        driver.findElement(By.xpath("//i[.='arrow_forward']")).click();
                    }else if(direction.equalsIgnoreCase("d")) {
                        driver.findElement(By.xpath("//i[.='arrow_downward']")).click();
                    }else if(direction.equalsIgnoreCase("u")) {
                        driver.findElement(By.xpath("//i[.='arrow_upward']")).click();
                    }else if(direction.equalsIgnoreCase("b")) {
                        driver.findElement(By.xpath("//i[.='arrow_back']")).click();
                    }
                }
            }
        }

        driver.findElement(By.xpath("//button[.='Submit']")).click();
        Thread.sleep(2000);
        if(driver.findElements(By.xpath("//div[@id='map']")).size() > 0){
            System.out.println("Puzzle 3 Completed");
        }

        //Puzzle 4 - Map
        Thread.sleep(5000);
        WebElement map = driver.findElement(By.xpath("//div[@id='map']"));
        map.click();
        map.sendKeys(Keys.TAB);
        map.sendKeys("i");

        double mapXPos = Double.valueOf(driver.findElement(By.xpath("//*[name()='svg']//*[name()='circle']")).getAttribute("cx"));
        double mapYPos = Double.valueOf(driver.findElement(By.xpath("//*[name()='svg']//*[name()='circle']")).getAttribute("cy"));

        while(!(mapXPos < 120)) {
            map.sendKeys(Keys.LEFT);
            mapXPos = Double.valueOf(driver.findElement(By.xpath("//*[name()='svg']//*[name()='circle']")).getAttribute("cx"));
        }

        while(!(mapYPos < 75)) {
            map.sendKeys(Keys.UP);
            mapYPos = Double.valueOf(driver.findElement(By.xpath("//*[name()='svg']//*[name()='circle']")).getAttribute("cy"));
        }

        driver.findElement(By.xpath("//button[.='Proceed']")).click();
        if(driver.findElements(By.xpath("//span/h2[.='Not a Bot!']")).size() > 0 ){
            System.out.println("Puzzle 4 Completed");
        }

        //Puzzle 5 - Captcha Validation
        Thread.sleep(5000);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementById(\"notABotForm\").submit()");
        System.out.println("Puzzle 5 Completed");

        //Puzzle 6 - Mongo DB Connect
        String challengeCode = driver.findElement(By.id("challenge_code")).getText().trim();
        //String dbServer = driver.findElement(By.xpath("//li[contains(.,'Find response code from mongo DB')]//li[1]")).getText().trim();
        //dbServer = dbServer.replace("DB Server:","").trim();
        String dbName = driver.findElement(By.xpath("//li[contains(.,'Find response code from mongo DB')]//li[2]")).getText().trim();
        dbName = dbName.replace("Database:","").trim();
        String dbUser = driver.findElement(By.xpath("//li[contains(.,'Find response code from mongo DB')]//li[3]")).getText().trim();
        dbUser = dbUser.replace("Username:","").trim();
        String dbPwd = driver.findElement(By.xpath("//li[contains(.,'Find response code from mongo DB')]//li[4]")).getText().trim();
        dbPwd = dbPwd.replace("Password:","").trim();
        String stringURI = "mongodb+srv://"+dbUser+":"+dbPwd+"@cluster0.unfcl.mongodb.net/"+dbName;
        MongoClientURI uri = new MongoClientURI(stringURI);
        MongoClient mongoClient = MongoClients.create(stringURI);
        MongoDatabase database = mongoClient.getDatabase(uri.getDatabase());
        MongoCollection<Document> challenge = database.getCollection("challenge");

        FindIterable<Document> cur = challenge.find(eq("code",challengeCode));

        cur.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                driver.findElement(By.id("mangoMongoResponse")).sendKeys(document.get("response").toString());
                driver.findElement(By.xpath("//button[.='Submit']")).click();
                System.out.println("Puzzle 6 Completed");
            }
        });

        //Puzzle 7 - Socket Gate
        Thread.sleep(2000);
        String socketGateURI = driver.findElement(By.xpath("//span[@id='wsurl']")).getAttribute("innerText");
        String socketGateMessage = driver.findElement(By.xpath("//li[contains(.,'Generate access token by sending message')]//following-sibling::div")).getAttribute("innerText");
        String token = wsClient.connectWSClient(socketGateURI, socketGateMessage);
        driver.findElement(By.xpath("//input[@placeholder='Access Token']")).sendKeys(token);
        driver.findElement(By.xpath("//button[.='Submit']")).click();
        if(driver.findElements(By.xpath("//span/h3[.='Congratulations!! You Found the Treasure']")).size() > 0){
            System.out.println("Treasure Found");
        }

        Thread.sleep(5000);

        driver.quit();
    }


}
