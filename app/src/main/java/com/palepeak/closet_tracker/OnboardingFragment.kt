package com.palepeak.closet_tracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class OnboardingFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_intro, container, false)
        //initialize adapter
        val vp = v.findViewById<ViewPager>(R.id.viewPager)
        vp.adapter = IntroPagerAdapter(inflater, View.OnClickListener { dismiss() })
        return v
    }

    //set background to be rounded grey rectangle
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rectangle_grey)
        return dialog
    }

    //view pager adapter class
    private class IntroPagerAdapter(val inflater: LayoutInflater, val dismiss: View.OnClickListener): PagerAdapter() {
        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view == o
        }

        //4 intro slides total
        override fun getCount(): Int {
            return 4
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = inflater.inflate(R.layout.intro_image_view, container, false)
            v.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.round_camera_24)
            container.addView(v)
            v.requestFocus()
            //last page should have dismiss button
            if (position != 3) {
                v.findViewById<View>(R.id.dismiss).visibility = View.GONE
            } else {
                v.findViewById<View>(R.id.dismiss).visibility = View.VISIBLE
                v.findViewById<View>(R.id.dismiss).setOnClickListener(dismiss)
            }
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            (container as ViewPager).removeView(obj as View)
        }
    }
}