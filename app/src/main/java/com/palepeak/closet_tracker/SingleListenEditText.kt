package com.palepeak.closet_tracker

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet

//edit text that can only have a max of one edit listener
//  needed to prevent jank with recyclerview view recycling
class SingleListenEditText : androidx.appcompat.widget.AppCompatEditText {
    private var mListeners: TextWatcher? = null

    constructor(ctx: Context?) : super(ctx)
    constructor(ctx: Context?, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context?, attrs: AttributeSet?, defStyle: Int) : super(ctx, attrs, defStyle)

    fun addSingleTextChangedListener(watcher: TextWatcher) {
        //only one listener is allowed so remove old listener if it exists
        removeSingleTextChangedListener()
        mListeners = watcher
        super.addTextChangedListener(watcher)
    }

    fun removeSingleTextChangedListener() {
        if (mListeners == null) return
        super.removeTextChangedListener(mListeners)
        mListeners = null
    }
}