package com.bridgefy.samples.tic_tac_toe;

import android.util.Log;

import com.bridgefy.samples.tic_tac_toe.entities.Event;
import com.bridgefy.samples.tic_tac_toe.entities.Move;
import com.bridgefy.samples.tic_tac_toe.entities.Player;
import com.bridgefy.samples.tic_tac_toe.entities.RefuseMatch;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.client.Session;
import com.bridgefy.sdk.client.StateListener;
import com.squareup.otto.Bus;

/**
 * @author dekaru on 5/10/17.
 */
class BridgefyListener {

    private static final String TAG = "BridgefyListener";

    private static BridgefyListener instance;

    // This sample app uses the Otto Event Bus to communicate between app components easily.
    // The Otto plugin is not a part of the Bridgefy framework
    private Bus ottoBus;

    private String username;
    private String uuid;


    private BridgefyListener(Bus ottoBus) {
        this.ottoBus = ottoBus;
    }

    static void initialize(String uuid, String username) {
        instance = new BridgefyListener(new Bus());
        instance.setUuid(uuid);
        instance.setUsername(username);
    }

    static void release() {
        instance = null;
    }


    /**
     *      BRIDGEFY LISTENER IMPLEMENTATIONS
     */
    private StateListener stateListener = new StateListener() {
        @Override
        public void onDeviceConnected(Device device, Session session) {
            // we present ourselves to the user
            device.sendMessage(
                    new Player(uuid, username, Player.STATUS_FREE)
                            .toHashMap());
        }

        @Override
        public void onDeviceLost(Device device) {
            // post this event via the Otto plugin so our components can update their views
            ottoBus.post(device);
        }

        @Override
        public void onStarted() {
            Log.i(TAG, "onStarted()");
        }

        @Override
        public void onStartError(String s, int i) {
            Log.e(TAG, s);
        }

        @Override
        public void onStopped() {
            Log.w(TAG, "onStopped()");
        }
    };

    private MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            // identify the type of incoming event
            int eventOrdinal = ((Double) message.getContent().get("event")).intValue();
            Event.EventType eventType = Event.EventType.values()[eventOrdinal];
            switch (eventType) {
                case FIRST_MESSAGE:
                    // recreate the Player object from the incoming message
                    // post the found object to our activities via the Otto plugin
                    ottoBus.post(Player.create(message));
                    break;

                case REFUSE_MATCH:
                    // recreate the RefuseMatch object from the incoming message
                    // post the found object to our activities via the Otto plugin
                    ottoBus.post(RefuseMatch.create(message));
                    break;
            }
        }

        @Override
        public void onBroadcastMessageReceived(Message message) {
            // build a TicTacToe Move object from our incoming Bridgefy Message
            Move move = Move.create(message);

            // TODO save this move so we can go back to it later

            // post this event via the Otto plugin so our components can update their views
            ottoBus.post(move);

            // if we're available to play or if it's a move from our current match ...
            if (MatchActivity.getMatchId() == null || !MatchActivity.getMatchId().equals(move.getMatchId())) {
                // ... post the incoming object to our active component via the Otto plugin
                // TODO create a notification for the incoming move

//            } else {
//                // ... if it isn't, it means that it's an invitation, but we're already playing
//                // TODO maybe create a shorthand method: Bridgefy.sendMessage(String receiver, HashMap content)
//                Bridgefy.sendMessage(Bridgefy.createMessage(
//                        message.getSenderId(),
//                        RefuseMatch.create(move.getMatchId(), true)));
            }
        }
    };


    /**
     *      GETTERS
     */

    static Bus getOttoBus() {
        return instance.ottoBus;
    }

    static StateListener getStateListener() {
        return instance.stateListener;
    }

    static MessageListener getMessageListener() {
        return instance.messageListener;
    }

    static String getUuid() {
        return instance.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}