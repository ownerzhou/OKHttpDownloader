package com.owner.downloader.rxjava;

import android.util.AndroidRuntimeException;

import com.owner.downloader.rxjava.handler.CurHandler;
import com.owner.downloader.rxjava.handler.MainHandler;
import com.owner.downloader.rxjava.handler.SubHandler;
import com.owner.downloader.rxjava.handler.IHandler;
import com.owner.downloader.rxjava.observe.IObserved;
import com.owner.downloader.rxjava.observe.IObserver;
import com.owner.downloader.rxjava.observe.ISender;

public class RxBase {
    public static final int CURRENT = 0;
    public static final int MAIN = 1;
    public static final int SUB = 2;
    private IObserved observed;
    private IObserver observerRef;
    private IHandler inputHandler, outputHandler;

    private RxBase(IObserved observed) {
        this.observed = observed;
    }

    public static RxBase create(IObserved observed) {
        return new RxBase(observed);
    }

    public void observer(IObserver observer) {
        if (observer != null) {
            this.observerRef = observer;
        }
        execute();
    }

    public RxBase executeOn(int threadType) {
        switch (threadType) {
            case SUB:
                inputHandler = new SubHandler();
                break;
            case MAIN:
                inputHandler = new MainHandler();
                break;
            default:
                inputHandler = new CurHandler();
                break;
        }
        return this;
    }

    public RxBase observerOn(int threadType) {
        switch (threadType) {
            case SUB:
                outputHandler = new SubHandler();
                break;
            case MAIN:
                outputHandler = new MainHandler();
                break;
            default:
                outputHandler = new CurHandler();
                break;
        }
        return this;
    }

    private void execute() {
        if (observed == null) {
            throw new AndroidRuntimeException("invalid action!");
        }
        if (outputHandler == null) {
            outputHandler = new CurHandler();
        }
        outputHandler.init();
        final ISender sender = createSender();
        if (inputHandler == null) {
            observed.observed(sender);
        } else {
            inputHandler.init();
            inputHandler.submit(new Runnable() {
                @Override
                public void run() {
                    observed.observed(sender);
                }
            });
        }
    }

    private ISender createSender() {
        return new ISender() {
            @Override
            public void sendEvent(final String tag, final Object data) {
                if (observerRef != null) {
                    outputHandler.submit(new Runnable() {
                        @Override
                        public void run() {
                            observerRef.observer(tag, data);
                        }
                    });
                }
            }
        };
    }
}
