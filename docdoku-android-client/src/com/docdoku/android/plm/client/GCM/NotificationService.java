/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client.GCM;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.docdoku.android.plm.client.Session;
import com.docdoku.android.plm.client.connection.ConnectionActivity;
import com.docdoku.android.plm.client.documents.Document;
import com.docdoku.android.plm.client.documents.DocumentActivity;
import com.docdoku.android.plm.network.tasks.HTTPGetTask;
import com.docdoku.android.plm.network.tasks.HTTPResultTask;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@code Service} called when a user clicks on a {@code Notification}. Attempts to start a {@link DocumentActivity}
 * for the document whose Id was indicated in the {@code Notification}.
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class NotificationService extends Service {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.GCM.NotificationService";

    /**
     * Called when this {@code Service} is started.
     * <p>
     * Loads the {@link com.docdoku.android.plm.client.Session} information in order to be able to send a request to the server.
     * <p>
     * Extracts the document data from the {@code Extra}s in the {@code Intent} to start an {@link HTTPGetTask} to fetch
     * the information about the document from the server. This {@code NotificationService} is set as {@link com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener}
     * for the {@code HTTPGetTask}.
     *
     * @param intent the {@code PendingIntent} created in the {@link com.docdoku.android.plm.client.GCM.GCMIntentService}
     * @param i
     * @param j
     * @return
     * @see Service
     */
    @Override
    public int onStartCommand(Intent intent, int i, int j) {
        Log.i(LOG_TAG, "Click on notification detected. Starting NotificationService.");
        Bundle bundle = intent.getExtras();
        String docRef = bundle.getString("docReference");
        String workspaceId = bundle.getString("workspaceId");

        try {
            Session session = Session.getSession(this);
            session.setCurrentWorkspace(this, workspaceId);
            HTTPGetTask task = new HTTPGetTask(new HTTPTaskDoneListener() {
                @Override
                public void onDone(HTTPResultTask result) {
                    Log.i(LOG_TAG, "Downloaded document that caused a notification");
                    try {
                        JSONObject documentJson = new JSONObject(result.getResultContent());
                        Document document = new Document(documentJson.getString("id"));
                        document.updateFromJSON(documentJson, getResources());
                        Intent documentIntent = new Intent(NotificationService.this, DocumentActivity.class);
                        documentIntent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document);
                        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, documentIntent, 0);
                        Intent intent = new Intent(NotificationService.this, ConnectionActivity.class);
                        intent.putExtra(ConnectionActivity.INTENT_KEY_PENDING_INTENT, pendingIntent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        stopSelf();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            task.execute("/api/workspaces/" + workspaceId + "/documents/" + docRef);
        }
        catch (Session.SessionLoadException e) {
            Log.e(LOG_TAG, "Failed to load session to start application from notification");
        }
        return START_NOT_STICKY;
    }

    /**
     * Unused method. Useful if this {@code Service} was bound to an {@code Activity}, which it shouldn't be.
     *
     * @param intent
     * @return
     * @see Service
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    /**
//     * Handles the result of the {@link HttpGetTask}.
//     * <p>
//     * If the Http request was successful, the result is a {@code JSONObject} representing the {@code Document}.
//     * A {@code PendingIntent} is created to start the {@link DocumentActivity} for this {@link Document} and it is
//     * put inside of another {@code Intent} which starts the {@link com.docdoku.android.plm.client.connection.ConnectionActivity}.
//     *
//     * @param result
//     */
}
