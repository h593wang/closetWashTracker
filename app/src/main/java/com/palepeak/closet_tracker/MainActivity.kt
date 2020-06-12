package com.palepeak.closet_tracker

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class MainActivity : AppCompatActivity() {

    lateinit var saveData: SaveData
    lateinit var categories: RecyclerView
    lateinit var today: RecyclerView
    lateinit var imageViewPreview: ImageView
    lateinit var filename: String
     var catagoryIdTmp = 0
     var itemIdTmp = 0

    val categoryDeleteListener = View.OnClickListener {
        val id = it.tag as Int
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Do you really want to delete this category?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes
            ) { _, _ ->
                handleCategoryDelete(id)
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    fun handleCategoryDelete(id: Int) {
        var index = 0
        for (cat in saveData.savedCategories){
            if (cat.id == id) {
                break
            }
            index++
        }

        val list = ArrayList<Int>()
        for (i in saveData.activeItems.indices) {
            if (saveData.activeItems[i].categoryId == id) {
                list.add(i)
            }
        }
        list.sort()
        list.reverse()
        for (i in list) {
            saveData.activeItems.removeAt(i)
            (today.adapter as ItemAdapter).notifyDataChange(i)
        }

        saveData.savedCategories.removeAt(index)
        (categories.adapter as CategoryAdapter).notifyDataChange(index)
    }

    val itemDeleteListener = View.OnClickListener {
        val Id = it.tag as ItemId
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Do you really want to delete this item?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes
            ) { _, _ ->
                handleItemDelete(Id)
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    fun handleItemDelete(id: ItemId) {
        for (cat in saveData.savedCategories) {
            if (cat.id == id.catId) {
                var index = 0
                for (item in cat.items) {
                    if (item.id == id.itemId) {
                        break
                    }
                    index++
                }
                cat.items.removeAt(index)
                (categories.adapter as CategoryAdapter).notifyDataChange(-1)
            }
        }

        var index = 0
        var found = false
        for (item in saveData.activeItems) {
            if (item.categoryId == id.catId && item.id == id.itemId) {
                found = true
                break
            }
            index++
        }
        if (found) {
            saveData.activeItems.removeAt(index)
            (today.adapter as ItemAdapter).notifyDataChange(index)
        }
    }

    val itemWearListener = View.OnClickListener {
        val itemId = it.tag as ItemId
        lateinit var item: ClothesItem
        for (cata in saveData.savedCategories) {
            if (cata.id == itemId.catId) {
                for (i in cata.items) {
                    if (i.id == itemId.itemId) item = i
                }
            }
        }

        var add = true
        for (i in saveData.activeItems) {
            if (i.id == item.id && i.categoryId == item.categoryId) add = false
        }
        if (add) {
            saveData.activeItems.add(item)
            (today.adapter as ItemAdapter).notifyDataChange(-1)
        }
    }

    val addCategoryListener = View.OnClickListener {
        var validCategoryID = 1
        var usedIDs = HashSet<Int>()
        for (cata in saveData.savedCategories) {
            usedIDs.add(cata.id)
        }
        while(true) {
            if (usedIDs.contains(validCategoryID)) validCategoryID++
            else break
        }

        val holder = findViewById<View>(R.id.addHolder)
        holder.visibility = View.VISIBLE
        holder.findViewById<TextView>(R.id.addTitle).text = "Add Category"
        holder.findViewById<View>(R.id.photoPreview).visibility = View.GONE
        holder.findViewById<View>(R.id.takePhoto).visibility = View.GONE
        holder.findViewById<EditText>(R.id.wears).hint = "Default wears before wash"
        holder.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            hideKeyboard(this)
            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
        holder.findViewById<Button>(R.id.doneAdding).setOnClickListener {
            hideKeyboard(this)
            val name = holder.findViewById<EditText>(R.id.name).text.toString()
            val wears = holder.findViewById<EditText>(R.id.wears).text.toString()
            if (!(name.isNullOrEmpty() || wears.isNullOrEmpty() || wears.toIntOrNull() == null)) {
                val cat = ClothesCategory(
                    validCategoryID,
                    wears.toInt(),
                    name,
                    ArrayList()
                )
                saveData.savedCategories.add(cat)
                (categories.adapter as CategoryAdapter).notifyDataChange(-1)
            }

            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }

    }

    val addItemListener = View.OnClickListener {
        //get catagory from click
        val catId = it.tag
        var catagory = saveData.savedCategories[0]
        lateinit var itemList: ArrayList<ClothesItem>
        for (cata in saveData.savedCategories) {
            if (cata.id == catId) {
                catagory = cata
                itemList = cata.items
                break
            }
        }
        //get valid id for item
        var validItemId = 1
        var usedIDs = HashSet<Int>()
        for (item in itemList) {
            usedIDs.add(item.id)
        }
        while(true) {
            if (usedIDs.contains(validItemId)) validItemId++
            else break
        }

        filename = catagory.id.toString() + "-" + validItemId
        val holder = findViewById<View>(R.id.addHolder)
        holder.visibility = View.VISIBLE
        holder.findViewById<TextView>(R.id.addTitle).text = "Add Item"
        imageViewPreview = holder.findViewById(R.id.photoPreview)
        imageViewPreview.visibility = View.VISIBLE
        holder.findViewById<View>(R.id.takePhoto).visibility = View.VISIBLE
        holder.findViewById<View>(R.id.takePhoto).setOnClickListener {
            dispatchTakePictureIntent()
        }
        holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
        catagoryIdTmp = catagory.id
        itemIdTmp = validItemId
        holder.findViewById<EditText>(R.id.wears).hint = "Default for Catagory is " + catagory.desiredWorn + "    "
        holder.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            hideKeyboard(this)
            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
        holder.findViewById<Button>(R.id.doneAdding).setOnClickListener {
            hideKeyboard(this)
            val name = holder.findViewById<EditText>(R.id.name).text.toString()
            val wears = holder.findViewById<EditText>(R.id.wears).text.toString()
            if (!(name.isNullOrEmpty() || wears.isNullOrEmpty() || wears.toIntOrNull() == null)) {
                val item = ClothesItem(
                    validItemId,
                    catagory.id,
                    0,
                    wears.toInt(),
                    name,
                    filename
                )
                catagory.items.add(0, item)
                (categories.adapter as CategoryAdapter).notifyDataChange(-1)
            }

            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
    }

    val removeActiveItem = View.OnClickListener {
        val itemId = it.tag as ItemId
        var index = 0
        for (i in saveData.activeItems) {
            if (i.id == itemId.itemId && i.categoryId == i.categoryId) {
                saveData.activeItems.removeAt(index)
                (today.adapter as ItemAdapter).notifyDataChange(index)
                break
            }
            index++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val fis2: FileInputStream = openFileInput("data")
            val is2 = ObjectInputStream(fis2)
            saveData = is2.readObject() as SaveData
            is2.close()
            fis2.close()

        } catch (e: Exception) {
            if (e is FileNotFoundException) saveData = SaveData(ArrayList(), ArrayList())
        }

        val adapter = CategoryAdapter(this, saveData.savedCategories, categoryDeleteListener, itemWearListener, itemDeleteListener, addCategoryListener, addItemListener)
        categories = findViewById(R.id.categoryList)
        val linearLayoutManager =  LinearLayoutManager(this)
        categories.layoutManager = linearLayoutManager
        categories.adapter = adapter

        val adapter2 = ItemAdapter(this, saveData.activeItems, removeActiveItem)
        today = findViewById(R.id.outfitPreview)
        val linearLayoutManager2 =  LinearLayoutManager(this)
        linearLayoutManager2.orientation = RecyclerView.HORIZONTAL
        today.layoutManager = linearLayoutManager2
        today.adapter = adapter2

        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            saveData.activeItems.clear()
            (today.adapter as ItemAdapter).notifyDataChange(-1)
        }

        findViewById<Button>(R.id.endButton).setOnClickListener {
            val washData = (today.adapter as ItemAdapter).washItems
            for (i in saveData.activeItems.indices) {
                lateinit var curItem: ClothesItem
                for (cata in saveData.savedCategories) {
                    if (cata.id == saveData.activeItems[i].categoryId) {
                        for (item in cata.items) {
                            if (item.id == saveData.activeItems[i].id) curItem = item
                        }
                    }
                }

                if (washData[i]) curItem.worn = 0
                else  curItem.worn += 1
            }

            saveData.activeItems.clear()
            (today.adapter as ItemAdapter).notifyDataChange(-1)
            (categories.adapter as CategoryAdapter).notifyDataChange(-1)

        }

        findViewById<View>(R.id.editImage).setOnClickListener {
            (categories.adapter as CategoryAdapter).toggleMode()
            if ((categories.adapter as CategoryAdapter).deleteMode) {
                findViewById<ImageView>(R.id.editImage).setColorFilter(R.color.red)
            } else {
                findViewById<ImageView>(R.id.editImage).colorFilter = null
            }
        }

    }

    override fun onStop() {
        val fos: FileOutputStream = getApplicationContext().openFileOutput("data", Context.MODE_PRIVATE)
        val os = ObjectOutputStream(fos)
        os.writeObject(saveData)
        os.flush()
        os.close()
        fos.close()
        super.onStop()
    }

    override fun onBackPressed() {
        if (findViewById<View>(R.id.addHolder).visibility == View.VISIBLE) {
            findViewById<View>(R.id.addHolder).visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val minLength = Math.min(imageBitmap.width, imageBitmap.height)
            var x = 0
            var y = 0
            var croppedHeight = imageBitmap.height
            var croppedWidth = imageBitmap.width
            if (croppedHeight > minLength) {
                y = (croppedHeight - minLength)/2
                croppedHeight = minLength
            } else {
                x = (croppedWidth - minLength)/2
                croppedWidth = minLength
            }
            val croppedBitmap = Bitmap.createBitmap(imageBitmap, x, y, croppedWidth, croppedHeight)
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, px.toInt(), px.toInt(), false)
            createImageFile(filename)
            try {
                val out = FileOutputStream(filename)
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            } catch (e: java.lang.Exception) {
                e.printStackTrace();
            }
            Picasso.Builder(this).executor(Executors.newSingleThreadExecutor()).build().load("file://"+filename).into(imageViewPreview)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(file: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            file, /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            filename = absolutePath
        }
    }
}
