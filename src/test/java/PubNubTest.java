import com.pubnub.api.PNConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class PubNubTest {
    PNConfiguration pnConfiguration;
    String channelName;
    String channelGroupName;


    @Before
    public void beforeEachTest() {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("demo");
        pnConfiguration.setPublishKey("demo");
        pnConfiguration.setSecure(false);
        pnConfiguration.setUuid(getRandomString("pubsub-",5));

        channelName = getRandomString("channel-", 5);
        channelGroupName = "cg1";
    }

    @Test
    public void testChannelSubscribeUnsubscribe () throws Exception {
        PubNubPubSub pubsub = new PubNubPubSub(pnConfiguration);

        pubsub.AddChannelGroup(channelGroupName, new String[]{channelName});
        Assert.assertTrue(checkChannelBelongToChannelGroup(pubsub, channelGroupName, channelName,true));

        pubsub.Subscribe(new String[]{channelName});
        Assert.assertTrue(checkUserSubscribedToChannel(pubsub, channelName, pubsub.GetUUID(), true));

        pubsub.Unsubscribe(new String[]{channelName});
        Assert.assertFalse(checkUserSubscribedToChannel(pubsub, channelName, pubsub.GetUUID(), false));

        pubsub.RemoveChannelFromChannelGroup(channelGroupName, new String[]{channelName});
        Assert.assertFalse(checkChannelBelongToChannelGroup(pubsub, channelGroupName, channelName,false));

        pubsub.RemoveChannelGroup(channelGroupName);
    }

    @Test
    public void testChannelHistory() throws Exception {
        String[] messages = new String[]{
                getRandomString("msg-", 5),
                getRandomString("msg-", 5),
                getRandomString("msg-", 5)
        };

        PubNubPubSub pubsub = new PubNubPubSub(pnConfiguration);
        pubsub.Subscribe(new String[]{channelName});
        Assert.assertTrue(checkUserSubscribedToChannel(pubsub, channelName, pubsub.GetUUID(), true));

        Long startPublish = pubsub.GetTime();
        Thread.sleep(1000);
        System.out.println("Start publish timestamp - " + startPublish);
        for (String msg : messages) {
            long nextPublish = pubsub.GetTime();
            System.out.println("Before next publish timestamp - " + nextPublish);
            pubsub.Publish(channelName, new String[]{msg});
            Assert.assertTrue(checkHistoryForMessagesStaringAt(pubsub, channelName, new String[]{msg}, nextPublish, true));
            Thread.sleep(1000);
        }

        Assert.assertTrue(checkHistoryForMessagesStaringAt(pubsub, channelName, messages, startPublish, true));

        pubsub.Unsubscribe(new String[]{channelName});
        Assert.assertFalse(checkUserSubscribedToChannel(pubsub, channelName, pubsub.GetUUID(), false));
    }

    private String getRandomString(String prefix, int suffixLen) {
        return prefix + RandomStringUtils.randomAlphanumeric(suffixLen);
    }

    private boolean checkChannelBelongToChannelGroup(PubNubPubSub sub, String cgName, String chName, boolean desired) throws Exception {
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(1000);
            ArrayList<String> channels = sub.GetChannelsBelongToChannelGroup(cgName);
            System.out.printf("Trial %s : check channel group - %s, %s channel - %s\n", i, cgName, (desired? "contain": "not contain"), chName);

            if (channels.contains(chName) == desired) {
                return desired;
            }
        }
        return !desired;
    }

    private boolean checkUserSubscribedToChannel(PubNubPubSub sub, String chName, String uuid, boolean desired) throws Exception {
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(1000);
            ArrayList<String> users = sub.GetUsersSubscribedToChannel(chName);
            System.out.printf("Trial %s : check user - %s, %s, on channel - %s\n", i, uuid, (desired? "subscribed": "not subscribed"), chName);
            if (users.contains(uuid) == desired) {
                return desired;
            }
        }
        return !desired;
    }

    private boolean checkHistoryForMessagesStaringAt(PubNubPubSub sub, String chName, String[] msgs, long startTime, boolean desired) throws  Exception {
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(1000);
            ArrayList<String> history = sub.GetChannelHistoryStartingAt(chName, startTime);
            ArrayList<String> messages = new ArrayList<>(Arrays.asList(msgs));
            System.out.printf("Trial %s : check history - %s, staring at %s, %s messages - %s\n", i, history, startTime, (desired? "contain": "not contain"), messages);
            if (history.containsAll(messages) == desired) {
                return desired;
            }
        }
        return !desired;
    }


}
