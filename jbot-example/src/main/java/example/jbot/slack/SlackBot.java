package example.jbot.slack;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.common.JBot;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;



/**
 * A simple Slack Bot. You can create multiple bots by just
 * extending {@link Bot} class like this one. Though it is
 * recommended to create only bot per jbot instance.
 *
 * @author ramswaroop
 * @version 1.0.0, 05/06/2016
 */
@JBot
@Profile("slack")
public class SlackBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);

    /**
     * Slack token from application.properties file. You can get your slack token
     * next <a href="https://my.slack.com/services/new/bot">creating a new bot</a>.
     */
    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    /**
     * Invoked when the bot receives a direct mention (@botname: message)
     * or a direct message. NOTE: These two event types are added by jbot
     * to make your task easier, Slack doesn't have any direct way to
     * determine these type of events.
     *
     * @param session
     * @param event
     */
    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) {
        reply(session, event, "Hi, I am " + slackService.getCurrentUser().getName());
    }

    /**
     * Invoked when bot receives an event of type message with text satisfying
     * the pattern {@code ([a-z ]{2})(\d+)([a-z ]{2})}. For example,
     * messages like "ab12xy" or "ab2bc" etc will invoke this method.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.MESSAGE, pattern = "^([a-z ]{2})(\\d+)([a-z ]{2})$")
    public void onReceiveMessage(WebSocketSession session, Event event, Matcher matcher) {
        reply(session, event, "First group: " + matcher.group(0) + "\n" +
                "Second group: " + matcher.group(1) + "\n" +
                "Third group: " + matcher.group(2) + "\n" +
                "Fourth group: " + matcher.group(3));
    }

    /**
     * Invoked when an item is pinned in the channel.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.PIN_ADDED)
    public void onPinAdded(WebSocketSession session, Event event) {
        reply(session, event, "Thanks for the pin! You can find all pinned items under channel details.");
    }

    /**
     * Invoked when bot receives an event of type file shared.
     * NOTE: You can't reply to this event as slack doesn't send
     * a channel id for this event type. You can learn more about
     * <a href="https://api.slack.com/events/file_shared">file_shared</a>
     * event from Slack's Api documentation.
     *  @param session
     * @param event
     */
    @Controller(events = EventType.FILE_SHARED)
    public void onFileShared(WebSocketSession session, Event event) {
        logger.info("File shared: {}", event);
    }


    /**
     * Conversation feature of JBot. This method is the starting point of the conversation (as it
     * calls {@link Bot#startConversation(Event, String)} within it. You can chain methods which will be invoked
     * one after the other leading to a conversation. You can chain methods with {@link Controller#next()} by
     * specifying the method name to chain with.
     *
     * @param session
     * @param event
     */
    @Controller(pattern = "(setup meeting)", next = "confirmTiming")
    public void setupMeeting(WebSocketSession session, Event event) {
        startConversation(event, "confirmTiming");   // start conversation
        reply(session, event, "Cool! At what time (ex. 15:30) do you want me to set up the meeting?");
    }

    /**
     * This method will be invoked after {@link SlackBot#setupMeeting(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    @Controller(next = "askTimeForMeeting")
    public void confirmTiming(WebSocketSession session, Event event) {
        reply(session, event, "Your meeting is set at " + event.getText() +
                ". Would you like to repeat it tomorrow?");
        nextConversation(event);    // jump to next question in conversation
    }

    /**
     * This method will be invoked after {@link SlackBot#confirmTiming(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    @Controller(next = "askWhetherToRepeat")
    public void askTimeForMeeting(WebSocketSession session, Event event) {
        if (event.getText().contains("yes")) {
            reply(session, event, "Okay. Would you like me to set a reminder for you?");
            nextConversation(event);    // jump to next question in conversation  
        } else {
            reply(session, event, "No problem. You can always schedule one with 'setup meeting' command.");
            stopConversation(event);    // stop conversation only if user says no
        }
    }
    /**
     * This method will be invoked when JBot has to share the file to the user. This can be shared directly to the user or coded into the application.
     *
     * @param session
     * @param event
     */
    public void postFileToSlack(WebSocketSession session, Event event) throws IOException {
        System.out.println("Enter the location of your file");
        Scanner in = new Scanner(System.in);
        String input=in.nextLine();
        File file = new File(input);
        okhttp3.Response response = new Meteoroid.Builder()
            .token(slackToken)
            .channels(event.getChannelId())
            .uploadFile(file)
            .build()
            .post();
        reply(session, event, "File Shared.");
        response.close();
    }

    /**
     * This method will be invoked when JBot has to share the excel to the user. This can be shared directly to the user or coded into the application.
     * User can also ask Jbot to read the contents of the excel file using (row,column) parameters.
     *
     * @param session
     * @param event
     */
  public void postExcelToSlack(WebSocketSession session, Event event) throws IOException {
        System.out.println("Enter the location of the excel file");
        Scanner in = new Scanner(System.in);
        String input=in.nextLine();
        File file = new File(input);
        okhttp3.Response response = new Meteoroid.Builder()
            .token(slackToken)
            .channels(event.getChannelId())
            .uploadFile(file)
            .build()
            .post();
        reply(session, event, "excel file received!");
        String result;
        if(event.getText().contains(",")){
            String x="";
            String y="";
            int i;
            int flagg=0;
            for(i=0; i<event.getText().length(); i++)
            {
                if(event.getText().charAt(i)>='0' && event.getText().charAt(i)<='9' && flagg==0)
                {
                    x+=event.getText().charAt(i);
                }
                else if(event.getText().charAt(i)==',')
                    flagg++;
                else if(event.getText().charAt(i)>='0' && event.getText().charAt(i)<='9' && flagg==1)
                {
                    y+=event.getText().charAt(i);
                }
            }
            reply(session, event, x + " , " + y);
            try
            {
                Workbook book=Workbook.getWorkbook(new File(input));
                Sheet sheet=book.getSheet(0);
                Cell cell1=sheet.getCell(Integer.parseInt(y),Integer.parseInt(x));
                result=cell1.getContents();
                book.close();
            }catch(Exception e)
            {
                System.out.println(e);
                result="none";
            }
            reply(session, event, "the element in the " + x + "line " + y + "column is: "+result);
        }
        response.close();
    }

    /**
     * This method will be invoked after {@link SlackBot#askTimeForMeeting(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    @Controller
    public void askWhetherToRepeat(WebSocketSession session, Event event) {
        if (event.getText().contains("yes")) {
            reply(session, event, "Great! I will remind you tomorrow before the meeting.");
        } else {
            reply(session, event, "Okay, don't forget to attend the meeting tomorrow :)");
        }
        stopConversation(event);    // stop conversation
    }
}