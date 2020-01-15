package com.enablex.demoenablex.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.utilities.UserListModels;

import java.util.ArrayList;
import java.util.List;


public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    UserItemClickListener userItemClickListener;
    ArrayList<UserListModels> participantList;
    Context context;

    public UserListAdapter(Context context, List<UserListModels> participantList, UserItemClickListener userItemClickListener) {
        this.userItemClickListener=userItemClickListener;
        this.participantList= (ArrayList<UserListModels>) participantList;
        this.context=context;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, null);
        return new UserListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        holder.userTV.setText(checkNullValue(participantList.get(position).getName()));
        if(checkNullValue(participantList.get(position).getRole()).equalsIgnoreCase("moderator")){
            holder.roleIV.setImageResource(R.drawable.moderator_icon);
        }else {
            holder.roleIV.setImageResource(R.drawable.participant_icon);
        }
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    public class UserListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView roleIV;
        ImageView shareFileIV;
        TextView userTV;

        public UserListViewHolder(View itemView) {
            super(itemView);
            roleIV=(ImageView) itemView.findViewById(R.id.roleIV);
            shareFileIV=(ImageView)itemView.findViewById(R.id.shareFileIV);
            userTV=(TextView) itemView.findViewById(R.id.userTV);
            shareFileIV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.shareFileIV:
                    userItemClickListener.onFileClick(getLayoutPosition());
                    break;
            }

        }
    }

    private String checkNullValue(String value) {
        if (value != null) {
            return value;
        }
        return "";
    }

    public interface UserItemClickListener{
        void onFileClick(int position);
    }
}
