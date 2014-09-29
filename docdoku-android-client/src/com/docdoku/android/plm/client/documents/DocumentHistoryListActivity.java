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

package com.docdoku.android.plm.client.documents;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.tasks.HTTPGetTask;
import com.docdoku.android.plm.network.tasks.HTTPResultTask;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * {@code Activity} displaying the list of documents recently viewed by the user.
 * <p>
 * Since only the recently viewed documents' Id is stored in the device's permanent memory, the data for each document
 * has to be loaded from the server. {@link DocumentLoaderByDocument}s are used to handle the asynchronous loading
 * of these documents.
 * <p>
 * While the documents are loading, the {@code ListView} rows display an indeterminate {@code ProgressBar}. Once a
 * {@link Document} is downloaded, its row is updated to show its information. If the download of a document fails (for
 * example, if the document was deleted from the server after the user viewed it), then an error row is shown.
 * <p>
 * Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class DocumentHistoryListActivity extends DocumentListActivity implements LoaderManager.LoaderCallbacks<Document> {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentHistoryListActivity";

    /**
     * Called when the {@code Activity} is created.
     * <p>Removes the {@code View} indicating that a loading was taking place. Loads the {@link com.docdoku.android.plm.client.NavigationHistory}
     * from the {@code SharedPreferences} to obtain the number of documents in history and their id.
     * <br>Initializes the {@code ArrayList} of
     * documents to the size of the history with null values. The {@link DocumentAdapter} will show a {@code ProgressBar}s for
     * these rows while the content remains {@code null}.
     * <br>Starts a {@link DocumentLoaderByDocument} for each {@link Document}.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see DocumentListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removeLoadingView();

        Log.i(LOG_TAG, "navigation history size: " + navigationHistory.getSize());
        documentArray = new ArrayList<>();
        documentAdapter = new DocumentAdapter(documentArray, this);
        documentListView.setAdapter(documentAdapter);

        Iterator<String> iterator = navigationHistory.getKeyIterator();
        int i = 0;
        while (iterator.hasNext()) {
            Bundle bundle = new Bundle();
            bundle.putString("partKey", iterator.next());
            bundle.putString("workspace", getCurrentWorkspace());
            documentArray.add(null);
            getSupportLoaderManager().initLoader(i, bundle, this);
            i++;
        }
        //TODO Currently, the documents are loaded in the order from the least recently viewed one to the most recently viewed one, which is absurd
        Log.i(LOG_TAG, "Document history list size : " + documentArray.size());
    }

    /**
     * Starts a {@link DocumentLoaderByDocument} to load a {@link Document}.
     *
     * @param id
     * @param bundle the {@code Bundle} containing the id and the workspace of the document to be downloaded.
     * @return
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<Document> onCreateLoader(int id, Bundle bundle) {
        Log.i(LOG_TAG, "Querying information for part in history at position " + id + " with reference " + bundle.getString("partKey"));
        return new DocumentLoaderByDocument(this, bundle.getString("partKey"), bundle.getString("workspace"));
    }

    /**
     * Handles the result of the {@link DocumentLoaderByDocument}.
     * <p>
     * The id of the {@code Loader} is used to determine its position in the {@code ListView}, which is where it is added in
     * the {@code ArrayList} of documents, replacing the null value. The {@link DocumentAdapter} is then notified that the
     * data has changed.
     *
     * @param loader
     * @param document
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoadFinished(Loader<Document> loader, Document document) {
        try {
            Log.i(LOG_TAG, "Received information for part in history at position " + loader.getId() + " with reference " + document.getIdentification());
            documentArray.set(loader.getId(), document);
            documentAdapter.notifyDataSetChanged();
        }
        catch (NullPointerException e) {
            Log.i(LOG_TAG, "Load of a document in history failed");
        }
    }

    /**
     * @param loader
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoaderReset(Loader<Document> loader) {
    }

    /**
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.menuRecentlyViewedDocuments;
    }

    /**
     * {@code Loader} that makes a request to the server to obtain the information about a specific document.
     */
    private static class DocumentLoaderByDocument extends Loader<Document> {

        private final String      elementId;
        private final String      workspace;
        private       HTTPGetTask asyncTask;

        public DocumentLoaderByDocument(Context context, String elementId, String workspace) {
            super(context);
            this.elementId = elementId;
            this.workspace = workspace;
            Log.d(LOG_TAG, elementId +" " + workspace);
        }

        /**
         * Start an {@link HTTPGetTask} to load the information about a document.
         *
         * @see Loader
         */
        @Override
        protected void onStartLoading() {
            createTask("api/workspaces/" + workspace + "/documents/" + elementId);
        }

        /**
         * @see Loader
         */
        @Override
        protected void onForceLoad() {
        }

        /**
         * @see Loader
         */
        @Override
        protected void onStopLoading() {
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
        }

        /**
         * @see Loader
         */
        @Override
        protected void onAbandon() {
        }

        /**
         * @see Loader
         */
        @Override
        protected void onReset() {
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
            createTask("api/workspaces/" + workspace + "/documents/" + elementId);
        }

        private void createTask(String exec) {
            asyncTask = new HTTPGetTask(new HTTPTaskDoneListener() {
                /**
                 * Handles the result of the {@link HTTPGetTask}. The result is read to create a new instance of
                 * {@link Document} which is passed to the {@code LoaderManager.LoaderCallbacks} using {@code deliverResult()}.
                 *
                 * @param result the {@code JSONObject} representing the {@link Document}.
                 * @see com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener
                 */
                @Override
                public void onDone(HTTPResultTask result) {
                    Document document;
                    try {
                        JSONObject documentJSON = new JSONObject(result.getResultContent());
                        document = new Document(documentJSON.getString("id"));
                        document.updateFromJSON(documentJSON, getContext().getResources());
                    }
                    catch (JSONException e) {
                        Log.e(LOG_TAG, "Error handling json object of a document");
                        Log.i(LOG_TAG, "Error message: " + e.getMessage());
                        document = new Document(elementId);
                    }
                    deliverResult(document);
                }
            });
            asyncTask.execute(exec);
        }
    }
}