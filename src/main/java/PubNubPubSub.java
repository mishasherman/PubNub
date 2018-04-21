import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.PNTimeResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAddChannelResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAllChannelsResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsDeleteGroupResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsRemoveChannelResult;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;

import java.util.ArrayList;
import java.util.Arrays;

public class PubNubPubSub {
    private PubNub pubNub;
    private Exception pubNubException;
    private ArrayList<String> users;
    private ArrayList<String> history;
    private ArrayList<String> channels;
    private long timeNow;

    PubNubPubSub(PNConfiguration pnConfiguration) {
        pubNub = new PubNub(pnConfiguration);
        pubNub.addListener(new SubscribeCallbackWithReconnect());
    }

    public String GetUUID() {
        return pubNub.getConfiguration().getUuid();
    }

    public Long GetTime() throws Exception {
        timeNow = 0;
        pubNubException = null;
        pubNub.time().async(new PNCallback<PNTimeResult>() {
            @Override
            public void onResponse(PNTimeResult result, PNStatus status) {
                if (!status.isError()) {
                    timeNow = result.getTimetoken();
                } else {
                    System.out.println("Failed to get current timestamp");
                    pubNubException = status.getErrorData().getThrowable();
                }
            }
        });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
        return timeNow;
    }

    public void Subscribe(String[] channels) {
        System.out.printf("User - %s, will attempt to subscribe on channels - %s\n", GetUUID(), new ArrayList<>(Arrays.asList(channels)));
        pubNub.subscribe()
                .channels(Arrays.asList(channels)) // subscribe to channels
                .withPresence()
                .execute();
    }

    public void Unsubscribe(String[] channels) {
        System.out.printf("User - %s, will attempt to unsubscribe from channels - %s\n", GetUUID(), new ArrayList<>(Arrays.asList(channels)));
        pubNub.unsubscribe()
                .channels(Arrays.asList(channels)) // unsubscribe from channels
                .execute();
    }

    public void AddChannelGroup(String cgName, String[] chNames) throws Exception{
        pubNubException = null;
        pubNub.addChannelsToChannelGroup()
                .channelGroup(cgName)
                .channels(Arrays.asList(chNames))
                .async(new PNCallback<PNChannelGroupsAddChannelResult>() {
                    @Override
                    public void onResponse(PNChannelGroupsAddChannelResult result, PNStatus status) {
                        if(!status.isError()) {
                            System.out.printf("Created: channel group - %s, channels -%s\n", cgName, Arrays.asList(chNames));
                        } else {
                            System.out.println("Failed to create channel group " + cgName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
    }

    public void RemoveChannelFromChannelGroup(String cgName, String[] chNames) throws Exception{
        pubNubException = null;
        pubNub.removeChannelsFromChannelGroup()
                .channelGroup(cgName)
                .channels(Arrays.asList(chNames))
                .async(new PNCallback<PNChannelGroupsRemoveChannelResult>() {
                    @Override
                    public void onResponse(PNChannelGroupsRemoveChannelResult result, PNStatus status) {
                        if(!status.isError()) {
                            System.out.printf("Removed: channels - %s, from channel group - %s\n", Arrays.asList(chNames), cgName);
                        } else {
                            System.out.printf("Failed to remove channels - %s, from channel group - %s", Arrays.asList(chNames), cgName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
    }

    public void RemoveChannelGroup(String cgName) throws Exception{
        pubNubException = null;
        pubNub.deleteChannelGroup()
                .channelGroup("family")
                .async(new PNCallback<PNChannelGroupsDeleteGroupResult>() {
                    @Override
                    public void onResponse(PNChannelGroupsDeleteGroupResult result, PNStatus status) {
                        if(!status.isError()) {
                            System.out.printf("Removed: channel group - %s\n", cgName);
                        } else {
                            System.out.printf("Failed to remove channel group - %s", cgName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
    }

    public ArrayList<String> GetChannelsBelongToChannelGroup(String cgName) throws Exception{
        channels = new ArrayList<>();
        pubNubException = null;
        pubNub.listChannelsForChannelGroup()
                .channelGroup(cgName)
                .async(new PNCallback<PNChannelGroupsAllChannelsResult>() {
                    @Override
                    public void onResponse(PNChannelGroupsAllChannelsResult result, PNStatus status) {
                        if(!status.isError()) {
                            channels = new ArrayList<>(result.getChannels());
                        } else {
                            System.out.println("Failed to create channel group " + cgName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
        System.out.printf("Channel group %s include channels %s\n", cgName, channels);
        return channels;
    }

    public ArrayList<String> GetChannelHistoryStartingAt(String chName, Long timeStart) throws Exception {
        history = new ArrayList<>();
        pubNubException = null;
        pubNub.history()
                .channel(chName) // where to fetch history from
                .includeTimetoken(true)
                .async(new PNCallback<PNHistoryResult>() {
                    @Override
                    public void onResponse(PNHistoryResult result, PNStatus status) {
                        if (!status.isError()) {
                            for (PNHistoryItemResult pnHistoryItemResult: result.getMessages()) {
                                if (pnHistoryItemResult.getTimetoken() >= timeStart) {
                                    String msg = pnHistoryItemResult.getEntry().getAsString();
                                    history.add(msg);
                                }

                            }
                        } else {
                            System.out.printf("Failed to get channel %s history\n" + chName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;

        System.out.printf("Channel - %s, history - %s, starts - %s, by - %s\n", chName, history, timeStart, GetUUID());
        return history;
    }

    public ArrayList<String> GetUsersSubscribedToChannel(String chName) throws Exception {
        users = new ArrayList<>();
        pubNubException = null;
        pubNub.hereNow()
                .channels(Arrays.asList(chName))
                .includeUUIDs(true)
                .async(new PNCallback<PNHereNowResult>() {
                    @Override
                    public void onResponse(PNHereNowResult result, PNStatus status) {
                        if (!status.isError()) {
                            for (PNHereNowChannelData channelData : result.getChannels().values()) {
                                for (PNHereNowOccupantData occupant : channelData.getOccupants()) {
                                    users.add(occupant.getUuid());
                                }
                            }
                        } else {
                            System.out.printf("Failed to get channel %s subscribers", chName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;

        System.out.printf("Channel - %s, subscribers found - %s, by - %s\n", chName, users, GetUUID());
        return users;
    }

    public void Publish(String chName, String[] messages) throws Exception{
        pubNubException = null;
        pubNub.publish()
                .message(Arrays.asList(messages))
                .channel(chName)
                .shouldStore(true)
                .ttl(0)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (!status.isError()) {
                            System.out.printf("Published messages - %s to channel - %s, by - %s\n", Arrays.asList(messages), chName, GetUUID());

                        } else {
                            System.out.printf("Failed to publish on channel %s", chName);
                            pubNubException = status.getErrorData().getThrowable();
                        }
                    }
                });
        Thread.sleep(1000);
        if (pubNubException != null) throw pubNubException;
    }

}
