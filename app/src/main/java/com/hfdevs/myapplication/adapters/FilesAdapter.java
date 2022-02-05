package com.hfdevs.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.hfdevs.myapplication.R;
import com.hfdevs.myapplication.databinding.LayoutFileItemBinding;
import com.hfdevs.myapplication.interfaces.OnFileItemClickListener;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    Context context;
    ArrayList<FTPFile> ftpFiles;
    OnFileItemClickListener onFileItemClickListener;

    public FilesAdapter(Context context, ArrayList<FTPFile> ftpFiles) {
        this.context = context;
        this.ftpFiles = ftpFiles;
    }

    public void setOnFileItemClickListener(OnFileItemClickListener onFileItemClickListener) {
        this.onFileItemClickListener = onFileItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutFileItemBinding binding = LayoutFileItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(FilesAdapter.ViewHolder holder, int position) {

        FTPFile selectedFile = ftpFiles.get(position);
        holder.binding.fileNameTextView.setText(selectedFile.getName());

        if (selectedFile.isDirectory()) {
            holder.binding.iconView.setImageResource(R.drawable.ic_baseline_folder_24);
        } else {
            holder.binding.iconView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFile.isDirectory()) {
                    if (onFileItemClickListener != null)
                        onFileItemClickListener.onDirectoryClicked(position, selectedFile);
                } else {
                    if (onFileItemClickListener != null)
                        onFileItemClickListener.onFileClicked(position, selectedFile);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenu().add("DELETE");
                popupMenu.getMenu().add("MOVE");
                popupMenu.getMenu().add("RENAME");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("DELETE")) {
                            Toast.makeText(context.getApplicationContext(), "Delete", Toast.LENGTH_SHORT).show();
                        }
                        if (item.getTitle().equals("MOVE")) {
                            Toast.makeText(context.getApplicationContext(), "MOVED ", Toast.LENGTH_SHORT).show();

                        }
                        if (item.getTitle().equals("RENAME")) {
                            Toast.makeText(context.getApplicationContext(), "RENAME ", Toast.LENGTH_SHORT).show();

                        }
                        return true;
                    }
                });

                popupMenu.show();
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return ftpFiles.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        LayoutFileItemBinding binding;

        public ViewHolder(LayoutFileItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
