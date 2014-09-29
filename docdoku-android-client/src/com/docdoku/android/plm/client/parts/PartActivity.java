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

package com.docdoku.android.plm.client.parts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.docdoku.PLMModel;
import com.docdoku.android.plm.client.Element;
import com.docdoku.android.plm.client.ElementActivity;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.gdx.GDXActivity;
import com.docdoku.android.plm.network.HTTPDownloadTask;
import com.docdoku.android.plm.network.HTTPResultTask;
import com.docdoku.android.plm.network.listeners.HTTPTaskDoneListener;

import java.io.File;

/**
 * <code>Activity</code> presenting the details for a {@link Part} in the form of an <code>ExpandableListView</code>
 * <p>Layout file: {@link /res/layout/activity_element.xml activity_element}
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class PartActivity extends ElementActivity {
    /**
     * Key for the <code>Parcelable Intent Extra</code> which is the <code>Part</code> represented in this <code>Activity</code>
     */
    public static final  String          PART_EXTRA                     = "part";
    private static final String          LOG_TAG                        = "com.docdoku.android.plm.client.parts.PartActivity";
    private static final int             NUM_PAGES                      = 6;
    private static final int             NUM_GENERAL_INFORMATION_FIELDS = 10;
    private static final int             NUM_REVISION_FIELDS            = 4;
    public static        Array<PLMModel> plmmodels                      = new Array<>();
    private Part           part;
    private ProgressDialog fileDownloadProgressDialog;
    ;

    /**
     * Called on this <code>Activity</code>'s creation.
     * Extracts the <code>Part</code> from the <code>Intent</code> and then sets the <code>Adapter</code> for the
     * <code>ExpandableListView</code>. The first group (general information) of the <code>ExpandableListView</code> is
     * expanded here.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element);

        Intent intent = getIntent();
        part = (Part) intent.getSerializableExtra(PART_EXTRA);

        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.addHeaderView(createHeaderView());
        expandableListView.setAdapter(new PartDetailsExpandableListAdapter());
        expandableListView.expandGroup(0);
    }

    private View createHeaderView() {
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null);
        TextView documentReference = (TextView) header.findViewById(R.id.documentIdentification);
        documentReference.setText(part.getKey());

        ToggleButton notifyIteration = (ToggleButton) header.findViewById(R.id.notifyIteration);
        notifyIteration.setVisibility(View.INVISIBLE);
        ToggleButton notifyStateChange = (ToggleButton) header.findViewById(R.id.notifyStateChange);
        notifyStateChange.setVisibility(View.INVISIBLE);

        checkInOutButton = (Button) header.findViewById(R.id.checkInOutButton);
        if (part.getCheckOutUserLogin() != null) {
            if (getCurrentUserLogin().equals(part.getCheckOutUserLogin())) {
                setElementCheckedOutByCurrentUser();
            }
            else {
                checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_other_user_light, 0, 0);
                checkInOutButton.setClickable(false);
                checkInOutButton.setText(R.string.locked);
            }
        }
        else {
            setElementCheckedIn();
        }
        return header;
    }

    /* TODO before refactor Part / PartActivity / Element / ElementActivity / Document / DocumentActivity
          this code is Duplicated from ElementActivity */
    protected View createFileRowView(final Part part) {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_dowloadable_file, null);
        TextView fileNameField = (TextView) rowView.findViewById(R.id.fileName);
        fileNameField.setText(part.getCADFileName());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileDownloadProgressDialog = new ProgressDialog(PartActivity.this);
                fileDownloadProgressDialog.setTitle(R.string.loadingFile);
                fileDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                fileDownloadProgressDialog.setIndeterminate(true);

                fileDownloadProgressDialog.show();

                final String dest = getExternalCacheDir() + "/" + part.getCADFileUrl().replaceAll(part.getCADFileName(), "");
                HTTPDownloadTask task = new HTTPDownloadTask(new HTTPTaskDoneListener() {
                    @Override
                    public void onDone(HTTPResultTask result) {
                        fileDownloadProgressDialog.dismiss();
                        if (result.isSucceed()) {
                            Toast.makeText(PartActivity.this, getResources().getString(R.string.downloadSuccessToPath, getExternalCacheDir() + part.getCADFileUrl()), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);

                            if (part.getCADFileName().endsWith(".obj")) {
                                PLMModel plmmodel = new PLMModel(part.getKey(),
                                        "partiterationID",
                                        new Matrix4(),
                                        part.getCADFileName(),
                                        dest);
                                plmmodels.clear();
                                plmmodels.add(plmmodel);


                                startActivity(new Intent(PartActivity.this, GDXActivity.class));
                            }
                            else {
                                File file = new File(dest + part.getCADFileName());

                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                String type = mime.getMimeTypeFromExtension(ext);

                                intent.setDataAndType(Uri.fromFile(file), type);

                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.chooseHowToOpenFile)));
                            }

                        }
                        else {
                            Toast.makeText(PartActivity.this, R.string.fileDownloadFail, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                task.execute("files/" + part.getCADFileUrl(), dest, part.getCADFileName());
            }
        });
        return rowView;
    }

    /**
     * There is no {@code Button} in the menu leading directly to this activity, so 0 is returned.
     *
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return 0;
    }

    /**
     * @return
     * @see com.docdoku.android.plm.client.ElementActivity#getElement()
     */
    @Override
    protected Element getElement() {
        return part;
    }

    private View createComponentRowView(int quantity, String name) {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_component, null);
        ((TextView) rowView.findViewById(R.id.componentQuantity)).setText("x" + quantity);
        ((TextView) rowView.findViewById(R.id.componentName)).setText(name);
        return rowView;
    }

    /**
     * {@code Adapter} for the {@code ExpandableListView}
     * Group 0: Header with tile of part and the check in/out {@code Button}
     * Group 1: General information about the part
     * Group 2: CAD file
     * Group 3: Components
     * Group 4: Linked documents
     * Group 5: Information about the last iteration
     * Group 6: Attributes
     */
    private class PartDetailsExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return NUM_PAGES;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i) {
                case 0:
                    return NUM_GENERAL_INFORMATION_FIELDS;
                case 1:
                    return 1;
                case 2:
                    return Math.max(part.getNumComponents(), 1);
                case 3:
                    return Math.max(part.getNumberOfLinkedDocuments(), 1);
                case 4:
                    return NUM_REVISION_FIELDS;
                case 5:
                    return Math.max(part.getNumberOfAttributes(), 1);
            }
            return 0;
        }

        @Override
        public Object getGroup(int i) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getChild(int i, int i2) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            ViewGroup pageView;
            pageView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_detail_header, null);
            if (b) {
                ((ImageView) pageView.findViewById(R.id.collapse_expand_group)).setImageResource(R.drawable.group_collapse_light);
            }
            TextView title = (TextView) pageView.findViewById(R.id.page_title);
            switch (i) {
                case 0:
                    title.setText(R.string.partGeneralInformation);
                    break;
                case 1:
                    title.setText(R.string.partCADFile);
                    break;
                case 2:
                    title.setText(R.string.partAssembly);
                    break;
                case 3:
                    title.setText(R.string.partLinks);
                    break;
                case 4:
                    title.setText(R.string.partIteration);
                    break;
                case 5:
                    title.setText(R.string.partAttributes);
                    break;
            }
            return pageView;
        }

        @Override
        public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
            View rowView = null;
            switch (i) {
                case 0://Part general information
                    String[] fieldNames = getResources().getStringArray(R.array.partGeneralInformationFieldNames);
                    String[] fieldValues = part.getGeneralInformationValues(PartActivity.this);
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 1://CAD file
                    try {
//                        rowView = createFileRowView(part.getCADFileName(), part.getCADFileUrl());
                        rowView = createFileRowView(part);
                    }
                    catch (NullPointerException e) {
                        return createNoContentFoundRowView(R.string.partNoCADFile);
                    }
                    break;
                case 2: //Components
                    try {
                        Part.Component component = part.getComponent(i2);
                        rowView = createComponentRowView(component.getAmount(), component.getNumber());
                    }
                    catch (NullPointerException e) {
                        return createNoContentFoundRowView(R.string.partNoComponents);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        return createNoContentFoundRowView(R.string.partNoComponents);
                    }
                    break;
                case 3: //Linked documents
                    try {
                        String linkedDocument = part.getLinkedDocument(i2);
                        rowView = createLinkedDocumentRowView(linkedDocument);
                    }
                    catch (NullPointerException e) {
                        return createNoContentFoundRowView(R.string.partNoLinkedDocuments);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        return createNoContentFoundRowView(R.string.partNoLinkedDocuments);
                    }
                    break;
                case 4: //Last iteration
                    fieldNames = getResources().getStringArray(R.array.iterationFieldNames);
                    fieldValues = part.getLastIteration();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 5: //Attributes
                    try {
                        Element.Attribute attribute = part.getAttribute(i2);
                        rowView = createNameValuePairRowView(attribute.getName(), attribute.getValue());
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        rowView = createNoContentFoundRowView(R.string.partNoAttributes);
                    }
                    catch (NullPointerException e) {
                        rowView = createNoContentFoundRowView(R.string.partNoAttributes);
                    }
                    break;
            }
            return rowView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            switch (i) {
                case 1: //CAD file
                    if (part.getCADFileUrl() != null && part.getCADFileUrl().length() > 0) {
                        Log.i(LOG_TAG, "CAD url: " + part.getCADFileUrl());
                        return true;
                    }
                    break;
                case 3: //Linked documents
                    if (part.getNumberOfLinkedDocuments() > 0) {
                        return true;
                    }
                    break;
            }
            return false;
        }
    }
}