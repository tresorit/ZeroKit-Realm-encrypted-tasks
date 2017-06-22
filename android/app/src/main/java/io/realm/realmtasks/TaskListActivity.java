/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.realmtasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.realmtasks.list.ItemViewHolder;
import io.realm.realmtasks.list.TaskListAdapter;
import io.realm.realmtasks.list.TouchHelper;
import io.realm.realmtasks.model.TaskList;
import io.realm.realmtasks.model.TaskListList;
import io.realm.realmtasks.view.RecyclerViewWithEmptyViewSupport;

public class TaskListActivity extends BaseListenerActivity {

    private Realm realm;

    public static final String EXTRA_USER_ID = "extra.user_id";
    public static final String EXTRA_USER_NAME = "extra.user_name";

    private RecyclerViewWithEmptyViewSupport recyclerView;
    private TaskListAdapter adapter;
    private TouchHelper touchHelper;
    private RealmResults<TaskListList> list;
    private boolean logoutAfterClose;

    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list);
        recyclerView = (RecyclerViewWithEmptyViewSupport) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(findViewById(R.id.empty_view));

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_USER_ID)) userId = intent.getStringExtra(EXTRA_USER_ID);
        if (intent.hasExtra(EXTRA_USER_NAME)) getSupportActionBar().setTitle(intent.getStringExtra(EXTRA_USER_NAME));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (touchHelper != null) {
            touchHelper.attachToRecyclerView(null);
        }
        adapter = null;

        realm = Realm.getInstance(UserManager.getSyncConfigurationForTasks(userId));
        list = realm.where(TaskListList.class).findAll();
        list.addChangeListener(this::updateList);
        updateList(list);
    }

    private void updateList(RealmResults<TaskListList> results) {
        if (results.size() > 0) {

            if (adapter != null){
                touchHelper.attachToRecyclerView(null);
                adapter = null;
            }

            // Create Adapter
            adapter = new TaskListAdapter(realm, TaskListActivity.this, results.first().getItems(), results.get(0).getId());
            touchHelper = new TouchHelper(new Callback(), adapter);
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    protected void onStop() {
        list.removeChangeListeners();
        if (adapter != null) {
            touchHelper.attachToRecyclerView(null);
            adapter = null;
        }
        realm.removeAllChangeListeners();
        realm.close();
        realm = null;

        if (logoutAfterClose) {
            /*
             * We need call logout() here since onCreate() of the next Activity is already
             * executed before reaching here.
             */
            UserManager.logoutActiveUser();
            logoutAfterClose = false;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        menu.findItem(R.id.action_share_list).setVisible((TextUtils.isEmpty(userId)));
        menu.findItem(R.id.action_logout).setVisible((TextUtils.isEmpty(userId)));
        menu.findItem(R.id.action_people).setVisible((TextUtils.isEmpty(userId)));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_list:
                Intent intentShare = new Intent(this, ShareListActivity.class);
                intentShare.putExtra(ShareListActivity.EXTRA_TRESOR_ID, list.get(0).getId());
                startActivity(intentShare);
                return true;

            case R.id.action_add:
                if (adapter != null) adapter.onItemAdded();
                return true;

            case R.id.action_people:
                startActivity(new Intent(this, InvitationListActivity.class));
                return true;

            case R.id.action_logout:
                Intent intent = new Intent(TaskListActivity.this, SignInActivity.class);
                intent.setAction(SignInActivity.ACTION_IGNORE_CURRENT_USER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                logoutAfterClose = true;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class Callback implements TouchHelper.Callback {

        @Override
        public void onMoved(RecyclerView recyclerView, ItemViewHolder from, ItemViewHolder to) {
            final int fromPosition = from.getAdapterPosition();
            final int toPosition = to.getAdapterPosition();
            adapter.onItemMoved(fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onCompleted(ItemViewHolder viewHolder) {
            adapter.onItemCompleted(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onDismissed(ItemViewHolder viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            adapter.onItemDismissed(position);
            adapter.notifyItemRemoved(position);
        }

        @Override
        public boolean canDismissed() {
            return false;
        }

        @Override
        public boolean onClicked(ItemViewHolder viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            final TaskList taskList = adapter.getItem(position);
            final String id = taskList.getId();
            final Intent intent = new Intent(TaskListActivity.this, TaskActivity.class);
            intent.putExtra(TaskActivity.EXTRA_LIST_ID, id);
            intent.putExtra(TaskActivity.EXTRA_TRESOR_ID, list.get(0).getId());
            intent.putExtra(TaskActivity.EXTRA_USER_ID, userId);
            TaskListActivity.this.startActivity(intent);
            return true;
        }

        @Override
        public void onChanged(ItemViewHolder viewHolder) {
            adapter.onItemChanged(viewHolder);
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        @Override
        public void onAdded() {
            adapter.onItemAdded();
        }

        @Override
        public void onReverted(boolean shouldUpdateUI) {
            adapter.onItemReverted();
            if (shouldUpdateUI) adapter.notifyDataSetChanged();
        }

        @Override
        public void onExit() {
        }
    }
}
