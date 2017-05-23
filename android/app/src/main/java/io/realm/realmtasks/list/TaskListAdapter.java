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

package io.realm.realmtasks.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.UUID;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.realmtasks.R;
import io.realm.realmtasks.model.TaskList;

public class TaskListAdapter extends CommonAdapter<TaskList> implements TouchHelperAdapter {

    private final Realm realm;
    private final String tresorId;

    public TaskListAdapter(Realm realm, Context context, OrderedRealmCollection<TaskList> items, String tresorId) {
        super(context, items);
        this.realm = realm;
        this.tresorId = tresorId;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        final TaskList taskList = getItem(position);
        itemViewHolder.getText().setText(taskList.getText());
        itemViewHolder.setBadgeVisible(true);
        final long badgeCount = taskList.getItems().where().equalTo(TaskList.FIELD_COMPLETED, false).count();
        itemViewHolder.setBadgeCount((int) badgeCount);
        itemViewHolder.setCompleted(taskList.isCompleted());
    }

    @Override
    public void onItemAdded() {
        realm.executeTransaction(realm1 -> {
            final TaskList taskList = new TaskList();
            taskList.setId(UUID.randomUUID().toString());
            taskList.setTresorId(tresorId);
            taskList.setText("");
            getData().add(0, taskList);
        });
    }

    @Override
    public void onItemMoved(final int fromPosition, final int toPosition) {
        realm.executeTransaction(realm1 -> moveItems(fromPosition, toPosition));
    }

    @Override
    public void onItemCompleted(final int position) {
        final TaskList taskList = getItem(position);
        final int count = (int) getData().where().equalTo(TaskList.FIELD_COMPLETED, false).count();
        realm.executeTransaction(realm1 -> {
            if (!taskList.isCompleted()) {
                if (taskList.isCompletable()) {
                    taskList.setCompleted(true);
                    moveItems(position, count - 1);
                } else {
                    Toast.makeText(context, R.string.no_item, Toast.LENGTH_SHORT).show();
                }
            } else {
                taskList.setCompleted(false);
                moveItems(position, count);
            }
        });
    }

    @Override
    public void onItemDismissed(final int position) {
        realm.executeTransaction(realm1 -> {
            final TaskList taskList = getData().get(position);
            taskList.getItems().deleteAllFromRealm();
            taskList.deleteFromRealm();
        });
    }

    @Override
    public void onItemReverted() {
        if (getData().size() == 0) {
            return;
        }
        realm.executeTransaction(realm1 -> {
            final TaskList taskList = getData().get(0);
            taskList.getItems().deleteAllFromRealm();
            taskList.deleteFromRealm();
        });
    }

    @Override
    public int generatedRowColor(int row) {
        return ItemViewHolder.ColorHelper.getColor(ItemViewHolder.ColorHelper.listColors, row, getItemCount());
    }

    @Override
    public void onItemChanged(final ItemViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (position >= 0) realm.executeTransaction(realm1 -> {
            TaskList taskList = getItem(position);
            taskList.setText(viewHolder.getText().getText().toString());
        });
    }
}
