package com.enablex.demoenablex.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.utilities.FileDownloadModel;

import java.util.ArrayList;
import java.util.List;

public class DownloadFileAdapter extends RecyclerView.Adapter<DownloadFileAdapter.FileDownloadViewHolder> {

    DownloadFileClickListener downloadFileClickListener;
    ArrayList<FileDownloadModel> fileDownloadList;
    Context context;

    public DownloadFileAdapter(Context context, List<FileDownloadModel> fileDownloadList, DownloadFileClickListener downloadFileClickListener) {
        this.downloadFileClickListener = downloadFileClickListener;
        this.fileDownloadList = (ArrayList<FileDownloadModel>) fileDownloadList;
        this.context = context;
    }

    @NonNull
    @Override
    public FileDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_file_item_view, null);
        return new FileDownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileDownloadViewHolder holder, int position) {
        holder.userTV.setText(checkNullValue(fileDownloadList.get(position).getName()));
    }

    @Override
    public int getItemCount() {
        return fileDownloadList.size();
    }

    public class FileDownloadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView downloadIV;
        TextView userTV;
        RelativeLayout file_list_item;

        public FileDownloadViewHolder(View itemView) {
            super(itemView);
            file_list_item = (RelativeLayout) itemView.findViewById(R.id.file_list_item);
            downloadIV = (ImageView) itemView.findViewById(R.id.downloadIV);
            userTV = (TextView) itemView.findViewById(R.id.userTV);
            file_list_item.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.file_list_item:
                    downloadFileClickListener.onClickFile(getLayoutPosition());
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

    public interface DownloadFileClickListener {
        void onClickFile(int position);
    }
}
