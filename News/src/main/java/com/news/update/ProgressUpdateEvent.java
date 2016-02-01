package com.news.update;

/**
 * Created by moshuangquan on 11/23/0023.
 */
public class ProgressUpdateEvent extends Event {
    public int progress;

    ProgressUpdateEvent(int progress) {
        this.progress = progress;
    }
}
