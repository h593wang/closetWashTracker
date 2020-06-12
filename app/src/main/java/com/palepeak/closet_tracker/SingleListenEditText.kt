package com.palepeak.closet_tracker

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet


class SingleListenEditText : androidx.appcompat.widget.AppCompatEditText {
    private var mListeners: TextWatcher? = null

    constructor(ctx: Context?) : super(ctx) {}
    constructor(ctx: Context?, attrs: AttributeSet?) : super(ctx, attrs) {}
    constructor(ctx: Context?, attrs: AttributeSet?, defStyle: Int) : super(ctx, attrs, defStyle) {}

    fun addSingleTextChangedListener(watcher: TextWatcher) {
        removeSingleTextChangedListener()
        mListeners = watcher
        super.addTextChangedListener(watcher)
    }

    fun removeSingleTextChangedListener() {
        if (mListeners == null) return
        super.removeTextChangedListener(mListeners)
        mListeners = null
    }

    fun clearTextChangedListeners() {
    }
}