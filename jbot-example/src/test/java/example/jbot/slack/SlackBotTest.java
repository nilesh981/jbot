package example.jbot.slack;


import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.facebook.models.Callback;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author ramswaroop
 * @version 20/06/2016
 */
@SpringBootTest
@ActiveProfiles("slack")
@RunWith(MockitoJUnitRunner.class)
public class SlackBotTest extends SlackBot{

    @Mock
    private WebSocketSession session;

    @Mock
    private SlackService slackService;

    @InjectMocks
    private TestBot bot;

    @Rule
    public OutputCapture capture = new OutputCapture();

    @Before
    public void init() {
        // set user
        User user = new User();
        user.setName("SlackBot");
        user.setId("UEADGH12S");
        // set rtm
        when(slackService.getImChannelIds()).thenReturn(Arrays.asList("D1E79BACV", "C0NDSV5B8"));
        when(slackService.getCurrentUser()).thenReturn(user);
        when(slackService.getWebSocketUrl()).thenReturn("");
    }

    @Test
    public void When_DirectMention_Then_InvokeOnDirectMention() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"<@UEADGH12S>: Hello\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Hi, I am"));
    }

    @Test
    public void When_DirectMessage_Then_InvokeOnDirectMessage() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"channel\": \"D1E79BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"Hello\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("this is a direct message"));
    }

    @Test
    public void When_MessageWithPattern_Then_InvokeOnReceiveMessageWithPattern() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"as12sd\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("First group: as12sd"));
        assertThat(capture.toString(), containsString("Second group: as"));
        assertThat(capture.toString(), containsString("Third group: 12"));
        assertThat(capture.toString(), containsString("Fourth group: sd"));
    }

    @Test
    public void When_DirectMessageWithPattern_Then_InvokeOnDirectMessage() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"channel\": \"D1E79BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"as12sd\"}"); // this matches the pattern but it's a direct message instead of message
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("this is a direct message"));
    }

    @Test
    public void When_MessageWithPatternAndPatternFlags_Then_InvokeOnReceiveMessageWithPatternAndPatternFlags() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
            "\"ts\": \"1358878749.000002\"," +
            "\"channel\": \"A1E78BACV\"," +
            "\"user\": \"U023BECGF\"," +
            "\"text\": \"HEY\"}"); // this matches the pattern with CASE_INSENSITIVE pattern flag on
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("hey case insensitive"));
    }


    @Test
    public void When_DirectMessage_Then_InvokeOnPinAdded() {
        TextMessage textMessage = new TextMessage("{\"type\":\"pin_added\"," +
                "\"user\":\"U0MCAEX8A\",\"channel_id\":\"C0NDSV5B8\"," +
                "\"item\":{\"type\":\"message\",\"channel\":\"C0NDSV5B8\"," +
                "\"message\":{\"type\":\"message\",\"user\":\"U1G0EBU2G\"," +
                "\"text\":\"Hi, I am nexagebot\",\"ts\":\"1471213096.000004\"," +
                "\"permalink\":\"https://aol.slack.com/archives/house-hunting/p1471213096000004\"," +
                "\"pinned_to\":[\"C0NDSV5B8\"]}," +
                "\"created\":1471213126}," +
                "\"item_user\":\"U1G0EBU2G\"," +
                "\"event_ts\":\"1471213126.954738\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Thanks for the pin"));
    }

    @Test
    public void When_DirectMessage_Then_InvokeOnFileShared() {
        TextMessage textMessage = new TextMessage("{\"type\":\"file_shared\"," +
                "\"file_id\":\"F219AF6VD\"," +
                "\"user_id\":\"U0MCAEX8A\"," +
                "\"file\":{\"id\":\"F219AF6VD\"}," +
                "\"event_ts\":\"1471213812.962298\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("File shared"));
    }

    @Test
    public void When_UserJoinsChannel_Then_InvokeOnMemberJoinedChannel() {
        TextMessage textMessage = new TextMessage("{\"type\": \"member_joined_channel\"," +
                "\"ts\": \"1348878749.000302\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Welcome to the channel!"));
    }

    @Test
    public void When_UserLeavesChannel_Then_InvokeOnMemberLeftChannel() {
        TextMessage textMessage = new TextMessage("{\"type\": \"member_left_channel\"," +
                "\"ts\": \"1348878749.000302\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Someone just left."));
    }

    /**
     * Unit tests for conversation feature.
     */
    
    @Test
    public void When_ConversationPattern_Then_StartConversation() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1158878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"setup meeting\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("At what time (ex. 15:30) do you want me to set up the meeting?"));

        textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1258878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"12:50\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Would you like to repeat it tomorrow?"));

        textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"yes\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Would you like me to set a reminder for you"));

        textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1458878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"yes\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("I will remind you tomorrow before the meeting"));
    }

    @Test
    public void Given_InConversation_When_AnswerNo_Then_StopConversation() {
        TextMessage textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1368878749.000602\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"setup meeting\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("At what time (ex. 15:30) do you want me to set up the meeting?"));

        textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1348878749.000302\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"12:40\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("Would you like to repeat it tomorrow?"));

        textMessage = new TextMessage("{\"type\": \"message\"," +
                "\"ts\": \"1358878749.000002\"," +
                "\"channel\": \"A1E78BACV\"," +
                "\"user\": \"U023BECGF\"," +
                "\"text\": \"no\"}");
        bot.handleTextMessage(session, textMessage);
        assertThat(capture.toString(), containsString("You can always schedule one with 'setup meeting' command"));
    }

    /**
     * This test will allow the user to specify location of excel which will be shared by bot and read the data from excel.
     * The user will provide location for test.xls and read the 2nd row and 1st column of excel data.
     * This test will compare whether the data is the same between the two.
     *
     * @param session
     * @param event
     */
    @Test
    @Controller(events = { EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE })
    public void When_ExcelShared_Slack(WebSocketSession session, Event event)
    {
        if (event.getText().contains("file")) {
            try {
                postExcelToSlack(session,event);
                TextMessage text= new TextMessage("{\"type\": \"message\"," +
                    "\"ts\": \"1368878749.000602\"," +
                    "\"channel\": \"A1E78BACV\"," +
                    "\"user\": \"U023BECGF\"," +
                    "\"text\": \"C:\\test.xls\"}");
                bot.handleTextMessage(session, text);
                assertThat(capture.toString(),containsString("Excel File Shared."));
                TextMessage text1= new TextMessage("{\"type\": \"message\"," +
                    "\"ts\": \"1368878749.000602\"," +
                    "\"channel\": \"A1E78BACV\"," +
                    "\"user\": \"U023BECGF\"," +
                    "\"text\": \"Excel 2,1\"}");
                bot.handleTextMessage(session, text1);
                assertThat(capture.toString(),containsString("300"));
            } catch (Exception e) {
                ;
            }
        } else {
            reply(session, event, "Hi, I am " + slackService.getCurrentUser().getName());
        }
    }

    /**
     * This test will allow the user to specify location of file which will be shared by bot.
     * The user will provide location for test.pdf and once the file is located, it will be shared by the bot.
     * The test will verify whether the file has been shared using the onFileShared controller.
     *
     * @param session
     * @param event
     */

    @Test
    @Controller(events = { EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE })
    public void When_FileShared_Slack(WebSocketSession session, Event event)
    {
        if (event.getText().contains("file")) {
            try {
                postFileToSlack(session,event);
                TextMessage text= new TextMessage("{\"type\": \"message\"," +
                    "\"ts\": \"1368878749.000602\"," +
                    "\"channel\": \"A1E78BACV\"," +
                    "\"user\": \"U023BECGF\"," +
                    "\"text\": \"C:\\test.pdf\"}");
                bot.handleTextMessage(session, text);
                assertThat(capture.toString(),containsString("File Shared."));
            } catch (Exception e) {
                ;
            }
        } else {
            reply(session, event, "Hi, I am " + slackService.getCurrentUser().getName());
        }
    }



    /**
     * Slack Bot for unit tests.
     */
    public static class TestBot extends SlackBot {

        private Callback capture;

        @Override
        public String getSlackToken() {
            return "slackToken";
        }

        @Override
        public Bot getSlackBot() {
            return this;
        }

        @Controller(events = EventType.DIRECT_MENTION)
        public void onDirectMention(WebSocketSession session, Event event) {
            System.out.println("Hi, I am SlackBot");
        }

        @Controller(events = EventType.DIRECT_MESSAGE)
        public void onDirectMessage(WebSocketSession session, Event event) {
            System.out.println("Hi, this is a direct message.");
        }

        @Controller(events = EventType.MESSAGE, pattern = "^([a-z ]{2})(\\d+)([a-z ]{2})$")
        public void onReceiveMessageWithPattern(WebSocketSession session, Event event, Matcher matcher) {
            System.out.println("First group: " + matcher.group(0) + "\n" +
                    "Second group: " + matcher.group(1) + "\n" +
                    "Third group: " + matcher.group(2) + "\n" +
                    "Fourth group: " + matcher.group(3));
        }

        @Controller(events = EventType.MESSAGE, pattern = "hey", patternFlags = Pattern.CASE_INSENSITIVE)
        public void onReceiveMessageWithPatternAndPatternFlags(WebSocketSession session, Event event) {
            System.out.println("hey case insensitive!");
        }

        @Controller(events = EventType.PIN_ADDED)
        public void onPinAdded(WebSocketSession session, Event event) {
            System.out.println("Thanks for the pin! You can find all pinned items under channel details.");
        }

        @Controller(events = EventType.FILE_SHARED)
        public void onFileShared(WebSocketSession session, Event event) {
            System.out.println("File shared.");
        }

        @Controller(events = EventType.MEMBER_JOINED_CHANNEL)
        public void onMemberJoinedChannel(WebSocketSession session, Event event) {
            System.out.println("Welcome to the channel!");
        }

        @Controller(events = EventType.MEMBER_LEFT_CHANNEL)
        public void onMemberLeftChannel(WebSocketSession session, Event event) {
            System.out.println("Someone just left.");
        }

        /**
         * Conversation feature of JBot.
         */

        @Controller(pattern = "(setup meeting)", next = "confirmTiming")
        public void setupMeeting(WebSocketSession session, Event event) {
            startConversation(event, "confirmTiming");   // start conversation
            System.out.println("Cool! At what time (ex. 15:30) do you want me to set up the meeting?");
        }

        @Controller(next = "askTimeForMeeting")
        public void confirmTiming(WebSocketSession session, Event event) {
            System.out.println("Your meeting is set at " + event.getText() +
                    ". Would you like to repeat it tomorrow?");
            nextConversation(event);    // jump to next question in conversation
        }

        @Controller(next = "askWhetherToRepeat")
        public void askTimeForMeeting(WebSocketSession session, Event event) {
            if (event.getText().contains("yes")) {
                System.out.println("Okay. Would you like me to set a reminder for you?");
                nextConversation(event);    // jump to next question in conversation
            } else {
                System.out.println("No problem. You can always schedule one with 'setup meeting' command.");
                stopConversation(event);    // stop conversation only if user says no
            }
        }

        @Controller
        public void askWhetherToRepeat(WebSocketSession session, Event event) {
            if (event.getText().contains("yes")) {
                System.out.println("Great! I will remind you tomorrow before the meeting.");
            } else {
                System.out.println("Okay, don't forget to attend the meeting tomorrow :)");
            }
            stopConversation(event);    // stop conversation
        }



    }
}
