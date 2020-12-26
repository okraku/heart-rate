package com.okraku.android.heartrate.phone;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data layer service for smartphones. Receives and sends messages.
 */
public class DataLayerService extends WearableListenerService {
    /**
     * This path is used by smartwatches to send the current heart rate.
     */
    private static final String MESSAGE_PATH_HEART_RATE = "/heart_rate";

    /**
     * This path is used by smartphones to send warnings, if the heart rate is too high.
     */
    private static final String MESSAGE_PATH_WARNING = "/heart_rate_warning";

    /**
     * log tag.
     */
    private static final String LOG_TAG = DataLayerService.class.getName();

    /**
     * This listener will be notified when the heart rate has changed. Usually implemented by
     * activities.
     */
    private static OnHeartRateChangeListener onChangeListener;

    /**
     * The current heart rate.
     */
    private static int currentValue = 0;

    /**
     * Determines if a warning has been sent to the smartwatch already.
     */
    private static boolean warned = false;

    /**
     * Called when a peer has connected.
     *
     * @param node A peer
     */
    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);
        Log.d(LOG_TAG, String.format("Peer connected: id=%s, name=%s", node.getId(), node.getDisplayName()));
    }

    /**
     * Called when a peer has disconnected.
     *
     * @param node A peer
     */
    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
        Log.d(LOG_TAG, String.format("Peer disconnected: id=%s, name=%s", node.getId(), node.getDisplayName()));
    }

    /**
     * Called when a message has been received.
     *
     * @param messageEvent The message event
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String data = new String(messageEvent.getData());

        Log.d(LOG_TAG, String.format("Received message: sourceNodeId=%s, path=%s, data=%s", messageEvent.getSourceNodeId(), messageEvent.getPath(), data));

        if (MESSAGE_PATH_HEART_RATE.equals(messageEvent.getPath())) {
            /*
             * A new heart rate has been received from the smartwatch.
             */

            // Store value
            currentValue = Integer.parseInt(new String(messageEvent.getData()));

            // Notify the GUI to update the TextView
            if (onChangeListener != null) {
                onChangeListener.onHeartRateChanged(currentValue);
            }

            // Example for bidirectional communication:
            // If the received heart rate is higher than 100, send a warning to the smartwatch.
            if(!warned && currentValue > 100) {
                sendMessage(getApplicationContext(), MESSAGE_PATH_WARNING, "");
                warned = true;
            }
        }
        else {
            /*
             * Unknown message path.
             */
            Log.e(LOG_TAG, "Ignoring message to path " + messageEvent.getPath());
        }
    }

    /**
     * Sends a message to the smartwatch.
     *
     * @param context An Android context
     * @param path A path
     * @param data A message
     */
    public static void sendMessage(final Context context, final String path, final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (final String nodeID : getNodes(context)) {
                    Task<Integer> sendTask = Wearable.getMessageClient(context).sendMessage(nodeID, path, data.getBytes());

                    sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer heartRate) {
                            Log.d(LOG_TAG, String.format("Successfully sent message: target=%s, path=%s, data=%s", nodeID, path, data));
                        }
                    });

                    sendTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(LOG_TAG, String.format("Failed to send message: target=%s, path=%s, data=%s", nodeID, path, data), e);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Returns all connected nearby nodes.
     *
     * @param context An Android context
     * @return A set of nodes
     */
    private static Set<String> getNodes(Context context) {
        HashSet<String> results = new HashSet<>();
        List<Node> nodes;

        try {
            nodes = Tasks.await(Wearable.getNodeClient(context).getConnectedNodes());

            for (Node node : nodes) {
                if (node.isNearby()) {
                    results.add(node.getId());
                }
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get nodes", e);
        }

        return results;
    }

    /**
     * Interface which will be notified when the heart rate has changed.
     */
    public interface OnHeartRateChangeListener {
        /**
         * Called when the heart rate has changed.
         *
         * @param value The new heart rate
         */
        void onHeartRateChanged(int value);
    }

    /**
     * Sets a listener that will be notified when the heart rate has changed.
     *
     * @param onChangeListener Listener to set
     */
    public static void setCallBack(OnHeartRateChangeListener onChangeListener) {
        DataLayerService.onChangeListener = onChangeListener;

        if (onChangeListener != null) {
            onChangeListener.onHeartRateChanged(currentValue);
        }
    }
}
