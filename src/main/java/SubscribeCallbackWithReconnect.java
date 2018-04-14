import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

public class SubscribeCallbackWithReconnect extends SubscribeCallback{
    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
            pubnub.reconnect();
        } else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
            pubnub.reconnect();
        }
    }

    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        if (message.getMessage() != null) {
            System.out.printf("Received message - %s on channel - %s by - %s\n", message.getMessage().toString(), message.getChannel(), pubnub.getConfiguration().getUuid());
        }
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
        if (presence.getEvent().equals("join")) {
            System.out.printf("User %s subscribed to channel - %s\n", pubnub.getConfiguration().getUuid(), presence.getChannel());
        }
    }
}
