package com.okraku.android.heartrate.watch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.okraku.android.heartrate.watch.sensor.HeartRateSensorEventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Data layer service for smartwatches. Receives and sends messages.
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
     * Log tag.
     */
    private static final String LOG_TAG = DataLayerService.class.getName();

    /**
     * ID of the notification channel.
     */
    private static final String CHANNEL_ID = "heartrate_channel";

    /**
     * Called when a peer has connected.
     *
     * @param node A peer
     */
    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);
        Log.d(LOG_TAG, String.format("Peer connected: id=%s name=%s", node.getId(), node.getDisplayName()));
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
        Log.d(LOG_TAG, String.format("Received message: path=%s, data=%s", messageEvent.getPath(), new String(messageEvent.getData())));

        if (MESSAGE_PATH_WARNING.equals(messageEvent.getPath())) {
            Log.d(LOG_TAG, String.format("Received warning from smartphone %s - heart rate is too high (now: %d)!", messageEvent.getSourceNodeId(), HeartRateSensorEventListener.lastHeartRate));
            createNotification();
        }
    }

    /**
     * Creates a notification to inform the user that his heart rate is too high.
     */
    private void createNotification() {
        createNotificationChannel();

        int notificationId = 001;
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_description))
                        .setContentIntent(viewPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Creates a notification channel, if needed.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Sends a message to all connected nodes.
     *
     * @param context An Android context
     * @param heartRate The heart rate to send
     */
    public static void sendBroadcastMessage(final Context context, final int heartRate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Collection<String> nodeIDs;
                try {
                    nodeIDs = getNodes(context);
                }
                catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to get connected nodes", e);
                    return;
                }

                sendMessageInternal(context, nodeIDs, heartRate);
            }
        }).start();
    }

    private static void sendMessageInternal(final Context context, final Collection<String> nodeIDs, final int heartRate) {
        final String path = MESSAGE_PATH_HEART_RATE;
        final String data = String.valueOf(heartRate);

        if (nodeIDs.isEmpty()) {
            Log.d(LOG_TAG, "No nodes found - cannot send heart rate");
            return;
        }

        for (final String nodeID : nodeIDs) {
            Log.d(LOG_TAG, String.format("Sending heart rate %d to node id %s", HeartRateSensorEventListener.lastHeartRate, nodeID));
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

    private static Collection<String> getNodes(Context context) throws ExecutionException, InterruptedException {
        HashSet<String> results = new HashSet<>();
        List<Node> nodes = Tasks.await(Wearable.getNodeClient(context).getConnectedNodes());

        for (Node node : nodes) {
            if (node.isNearby()) {
                results.add(node.getId());
            }
        }
        return results;
    }
}
