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
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.realm.OrderedRealmCollection;
import io.realm.realmtasks.R;
import io.realm.realmtasks.TaskListActivity;
import io.realm.realmtasks.model.User;

public class InvitationListAdapter extends CommonAdapter<User> {

    public InvitationListAdapter(Context context, OrderedRealmCollection<User> items) {
        super(context, items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row2, parent, false);
        return new ItemViewHolder(rowItem, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        final User user = getItem(position);
        String userName = user.getUserName();
        String realmUserId = user.getRealmUserId();

        itemViewHolder.getText().setText(userName);
        itemViewHolder.setBadgeVisible(false);
        itemViewHolder.delete.setVisibility(View.GONE);
        itemViewHolder.itemView.setOnClickListener(v -> {
            final Intent intent = new Intent(context, TaskListActivity.class);
            intent.putExtra(TaskListActivity.EXTRA_USER_ID, realmUserId);
            intent.putExtra(TaskListActivity.EXTRA_USER_NAME, userName);
            context.startActivity(intent);
        });
    }

}
