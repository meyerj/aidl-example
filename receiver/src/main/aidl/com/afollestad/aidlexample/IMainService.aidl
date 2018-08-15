package com.afollestad.aidlexample;
import com.afollestad.aidlexample.IListener;
import com.afollestad.aidlexample.MainObject;

interface IMainService {
    MainObject[] listFiles(String path);
    void registerCallbacks(IListener listener);

    void exit();
}
