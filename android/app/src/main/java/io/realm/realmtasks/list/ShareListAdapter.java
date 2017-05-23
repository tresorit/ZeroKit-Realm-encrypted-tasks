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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tresorit.zerokit.call.Action;

import io.realm.OrderedRealmCollection;
import io.realm.realmtasks.R;
import io.realm.realmtasks.model.User;

public class ShareListAdapter extends CommonAdapter<User> {

    private final Action<User> deleteUser;

    public ShareListAdapter(Context context, OrderedRealmCollection<User> items, Action<User> deleteUser) {
        super(context, items);
        this.deleteUser = deleteUser;
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
        itemViewHolder.getText().setText(user.getUserName());
        itemViewHolder.setBadgeVisible(false);

        itemViewHolder.delete.setOnClickListener(v -> deleteUser.call(user));
    }

}
