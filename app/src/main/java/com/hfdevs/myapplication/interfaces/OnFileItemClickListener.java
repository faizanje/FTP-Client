package com.hfdevs.myapplication.interfaces;

import org.apache.commons.net.ftp.FTPFile;

public interface OnFileItemClickListener {
    void onFileClicked(int position, FTPFile ftpFile);
    void onDirectoryClicked(int position, FTPFile ftpFile);
}
