package com.hfdevs.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfdevs.myapplication.adapters.FilesAdapter;
import com.hfdevs.myapplication.databinding.ActivityMainBinding;
import com.hfdevs.myapplication.interfaces.OnFileItemClickListener;
import com.hfdevs.myapplication.utils.Constants;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnFileItemClickListener {

    ActivityMainBinding binding;
    FilesAdapter adapter;
    ArrayList<FTPFile> ftpFileArrayList = new ArrayList<>();
    FTPSClient mFtpClient;
    Stack<String> pathStack = new Stack<>();
    public static final int PICK_FILE = 10;

    /* Hardcoding for testing */
    String host = "files.000webhost.com";
    String username = "faizanje";
    String password = "gonawazgo1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
        initFTPClient();
    }


    private void init() {
        adapter = new FilesAdapter(this, ftpFileArrayList);
        adapter.setOnFileItemClickListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        updateActionBar();
    }

    private void setListeners() {
        binding.btnAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });
    }

    private void initFTPClient() {
        Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int port = 21;
                try {
                    mFtpClient = new FTPSClient();
                    mFtpClient.setConnectTimeout(10 * 1000);
                    mFtpClient.connect(InetAddress.getByName(host));
                    boolean status = mFtpClient.login(username, password);
                    Log.d(Constants.TAG, "isFTPConnected: " + status);
                    if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                        mFtpClient.setFileType(FTP.ASCII_FILE_TYPE);
                        mFtpClient.enterLocalPassiveMode();

                    } else {
                        Log.d(Constants.TAG, "FTPReply.isPositiveCompletion: false");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(Constants.TAG, "onCreate: Exception" + e.getMessage());
                }
                return true;
            }
        }).subscribeOn(Schedulers.io())
                // report or post the result to main thread.
                .observeOn(AndroidSchedulers.mainThread())
                // execute this RxJava
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Throwable {
                        listDirectory(getPathFromPathStack());
                    }
                });
    }


    @Override
    public void onFileClicked(int position, FTPFile ftpFile) {
    }

    @Override
    public void onDirectoryClicked(int position, FTPFile ftpFile) {
        Log.d(Constants.TAG, "onDirectoryClicked: ");
        pathStack.push(ftpFile.getName());
        listDirectory(getPathFromPathStack());
    }

    private void listDirectory(String path) {

        Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                FTPFile[] ftpFiles = mFtpClient.listFiles(path);
                ftpFileArrayList.clear();
                ftpFileArrayList.addAll((Arrays.asList(ftpFiles)));
                sortFiles();
                return true;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Throwable {
                        updateActionBar();
                        updatePathTextView();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void sortFiles() {
        Collections.sort(ftpFileArrayList, new Comparator<FTPFile>() {
            @Override
            public int compare(FTPFile o1, FTPFile o2) {
                if (o1.isDirectory() && !o2.isDirectory()) {
                    // Directory
                    return -1;
                } else if (!o1.isDirectory() && o2.isDirectory()) {
                    //File
                    return 1;
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
    }

    private void updateActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pathStack.isEmpty() ? "/" : pathStack.peek());
            getSupportActionBar().setDisplayHomeAsUpEnabled(!getPathFromPathStack().equals("/"));
        }
    }

    public void updatePathTextView() {
        binding.tvPath.setText(getPathFromPathStack());
    }

    public String getPathFromPathStack() {
        StringBuilder path = new StringBuilder("/");
        for (String s : pathStack) {
            path.append(s);
            path.append("/");
        }
        return path.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                pathStack.pop();
                listDirectory(getPathFromPathStack());
                updateActionBar();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        
        startActivityForResult(intent, PICK_FILE);
    }
}