package com.palepeak.closet_tracker

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.*
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private lateinit var saveData: SaveData
    private lateinit var categories: RecyclerView
    private lateinit var today: RecyclerView
    private lateinit var imageViewPreview: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var bitmap: Bitmap
    private var filename = ""
    private var searchResults = ArrayList<ClothesItem>()
    private var inAddItem = false
    private var catIdGlobal = 0
    private var inAddCat = false

    //click listener for when category delete is clicked
    private val categoryDeleteListener = View.OnClickListener {
        //get category id from tag
        val id = it.tag as Int
        //show a confirmation dialog
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

    //handler function for deleting a category
    private fun handleCategoryDelete(id: Int) {
        //get index of category in saveData from id
        val index = saveData.savedCategories.indexOfFirst { it.id == id }

        //search for all actively worn items belonging to that category
        val list = ArrayList<Int>()
        for (i in saveData.activeItems.indices) {
            if (saveData.activeItems[i].categoryId == id) {
                list.add(i)
            }
        }
        //sort and reverse to be in descending indexes for easy remove
        list.sort()
        list.reverse()
        //remove from back
        for (i in list) {
            saveData.activeItems.removeAt(i)
            today.adapter?.notifyDataSetChanged()
        }

        //clear search text since search result might contain items from category
        searchEditText.text.clear()
        //delete image from files
        for (item in saveData.savedCategories[index].items) {
            val delete = File(item.photoPath)
            if (delete.exists()) {
                (delete.delete())
            }
        }
        //remove category from save data
        saveData.savedCategories.removeAt(index)
        //notify the entry has been removed
        categories.adapter?.notifyDataSetChanged()
    }

    //click listener for when a item delete is clicked
    private val itemDeleteListener = View.OnClickListener {
        //get item id from tag
        val id = it.tag as ItemId
        //show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Do you really want to delete this item?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes
            ) { _, _ ->
                handleItemDelete(id)
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    //handler function for deleting an item
    private fun handleItemDelete(id: ItemId) {
        //remove item from save data, notify adpater of data change
        for (cat in saveData.savedCategories) {
            if (cat.id == id.catId) {
                var index = 0
                for (item in cat.items) {
                    if (item.id == id.itemId) {
                        break
                    }
                    index++
                }
                //delete image from files
                val delete = File(cat.items[index].photoPath)
                if (delete.exists()) {
                    (delete.delete())
                }
                cat.items.removeAt(index)
                categories.adapter?.notifyDataSetChanged()
            }
        }

        //search for item in actively wearing items
        var index = 0
        var found = false
        for (item in saveData.activeItems) {
            if (item.categoryId == id.catId && item.id == id.itemId) {
                found = true
                break
            }
            index++
        }
        //if found, remove from wearing items
        if (found) {
            saveData.activeItems.removeAt(index)
            today.adapter?.notifyDataSetChanged()
        }

        //clear search text since it may contain item
        searchEditText.text.clear()
    }

    //click listener for when an item wear button is clicked
    private val itemWearListener = View.OnClickListener {
        //get item id from tag
        val itemId = it.tag as ItemId
        lateinit var item: ClothesItem
        //get item from save data that matched id
        for (cata in saveData.savedCategories) {
            if (cata.id == itemId.catId) {
                for (i in cata.items) {
                    if (i.id == itemId.itemId) item = i
                }
            }
        }

        //check for duplicates
        var add = true
        for (i in saveData.activeItems) {
            if (i.id == item.id && i.categoryId == item.categoryId) add = false
        }
        //add to active items and notify adapter
        if (add) {
            saveData.activeItems.add(item)
            today.adapter?.notifyDataSetChanged()
        }
    }

    //click listener for when the add category button is clicked
    private val addCategoryListener = View.OnClickListener {
        inAddCat = true
        //get a valid category id
        var validCategoryID = 1
        val usedIDs = HashSet<Int>()
        for (cata in saveData.savedCategories) {
            usedIDs.add(cata.id)
        }
        while(true) {
            if (usedIDs.contains(validCategoryID)) validCategoryID++
            else break
        }

        //show add category dialog box
        val holder = findViewById<View>(R.id.addHolder)
        holder.visibility = View.VISIBLE
        holder.findViewById<TextView>(R.id.addTitle).setText(R.string.add_category)
        holder.findViewById<View>(R.id.photoPreview).visibility = View.GONE
        holder.findViewById<View>(R.id.takePhoto).visibility = View.GONE
        holder.findViewById<EditText>(R.id.wears).setHint(R.string.default_wears_before_wash)
        holder.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            //add cancelled, revert to default state
            inAddCat = false
            hideKeyboard(this)
            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
        holder.findViewById<Button>(R.id.doneAdding).setOnClickListener {
            inAddCat = false
            //adding, hide keyboard
            hideKeyboard(this)
            val name = holder.findViewById<EditText>(R.id.name).text.toString()
            val wears = holder.findViewById<EditText>(R.id.wears).text.toString()
            if (!(name.isEmpty() || wears.isEmpty() || wears.toIntOrNull() == null || wears.toInt() < 1)) {
                //if fields are valid, create category and add to saveData
                val cat = ClothesCategory(
                    validCategoryID,
                    wears.toInt(),
                    name,
                    ArrayList(),
                    false
                )
                saveData.savedCategories.add(cat)
                //notify adapter of new category
                categories.adapter?.notifyDataSetChanged()
            } else {
                //if fields are not valid
                var error = ""
                //generate appropriate error message
                when {
                    name.isEmpty() -> error = "Your category couldn't be added because the name was missing"
                    wears.isEmpty() -> error = "Your category couldn't be added because the desired wears was missing"
                    wears.toIntOrNull() == null -> error = "Your category couldn't be added because the desired wears wasn't a number"
                    wears.toInt() < 1 -> error = "Invalid max wears"
                }
                //and show dialog
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(error)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok,null)
                    .show()
            }
            //revert to default state on both success and failure
            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
    }

    //click lister for when the add item button is clicked
    private val addItemListener = View.OnClickListener {
        //get category id from click
        val catId = it.tag as Int
        catIdGlobal = catId
        addItemHandler(catId)
    }

    //wrapper function for add item
    private fun addItemHandler(catId: Int) {
        inAddItem = true
        //get category from save data with id
        var category = saveData.savedCategories[0]
        for (cat in saveData.savedCategories) {
            if (cat.id == catId) {
                category = cat
                break
            }
        }
        //get valid id for item
        var validItemId = 1
        val usedIDs = HashSet<Int>()
        for (item in category.items) {
            usedIDs.add(item.id)
        }
        while(true) {
            if (usedIDs.contains(validItemId)) validItemId++
            else break
        }

        val holder = findViewById<View>(R.id.addHolder)
        //  the final filename will be stored in filenameAbs
        //  the final image will be automatically added to imageViewPreview
        imageViewPreview = holder.findViewById(R.id.photoPreview)

        //entry point for take picture intent
        holder.findViewById<View>(R.id.takePhoto).visibility = View.VISIBLE
        holder.findViewById<View>(R.id.takePhoto).setOnClickListener {
            dispatchTakePictureIntent()
        }

        //make create item dialog visible
        holder.visibility = View.VISIBLE
        holder.findViewById<TextView>(R.id.addTitle).setText(R.string.add_item)
        imageViewPreview.visibility = View.VISIBLE
        imageViewPreview.setImageResource(R.drawable.camera)
        holder.findViewById<EditText>(R.id.wears).hint = "Default for Catagory is " + category.desiredWorn + "    "
        holder.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            inAddItem = false
            hideKeyboard(this)
            //add cancelled, revert to default state
            imageViewPreview.setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }

        holder.findViewById<Button>(R.id.doneAdding).setOnClickListener {
            inAddItem = false
            //adding complete, hide keyboard
            hideKeyboard(this)

            //save image to file
            val filenameAbs = createImageFile("$catId-$validItemId").absolutePath
            try {
                val out = FileOutputStream(filenameAbs)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            val name = holder.findViewById<EditText>(R.id.name).text.toString()
            val wears = holder.findViewById<EditText>(R.id.wears).text.toString()
            if (!(name.isEmpty() || wears.isEmpty() || wears.toIntOrNull() == null || wears.toInt() < 1)) {
                //if entries are valid, create item and add to savedata
                val item = ClothesItem(
                    validItemId,
                    category.id,
                    0,
                    wears.toInt(),
                    name,
                    filenameAbs,
                    false
                )
                category.items.add(item)
                //notify adapter of new item
                categories.adapter?.notifyDataSetChanged()
            } else {
                //if entries are invalid, generate and show appropriate error message
                var error = ""
                when {
                    name.isEmpty() -> error = "Your item couldn't be added because the name was missing"
                    wears.isEmpty() -> error = "Your item couldn't be added because the desired wears was missing"
                    wears.toIntOrNull() == null -> error = "Your item couldn't be added because the desired wears wasn't a number"
                    wears.toInt() < 1 -> error = "Invalid max wears"
                }
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(error)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok,null)
                    .show()
            }

            //revert dialog to default state regardless of success of failure
            holder.findViewById<ImageView>(R.id.photoPreview).setImageResource(R.drawable.camera)
            holder.findViewById<EditText>(R.id.wears).setText("")
            holder.findViewById<EditText>(R.id.name).setText("")
            holder.visibility = View.GONE
        }
    }

    //click listener for when an item is removed from the actively worn
    private val removeActiveItem = View.OnClickListener {
        //get id
        val itemId = it.tag as ItemId
        var index = 0
        //find item and remove, notify adapter
        for (i in saveData.activeItems) {
            if (i.id == itemId.itemId && i.categoryId == i.categoryId) {
                saveData.activeItems.removeAt(index)
                today.adapter?.notifyDataSetChanged()
                break
            }
            index++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get saved data
        if (File(filesDir.absolutePath+"/data").exists()) {
            try {
                val fis2: FileInputStream = openFileInput("data")
                val is2 = ObjectInputStream(fis2)
                saveData = is2.readObject() as SaveData
                is2.close()
                fis2.close()
                //create backup
                val fos: FileOutputStream =
                    applicationContext.openFileOutput("data_backup", Context.MODE_PRIVATE)
                val os = ObjectOutputStream(fos)
                os.writeObject(saveData)
                os.flush()
                os.close()
                fos.close()
            } catch (e: Exception) {
                //if file doesn't exist, create initial value
                if (e is FileNotFoundException) saveData = SaveData(ArrayList(), ArrayList())
                else {
                    //data corrupted, load backup
                    val fis2: FileInputStream = openFileInput("data_backup")
                    val is2 = ObjectInputStream(fis2)
                    saveData = is2.readObject() as SaveData
                    is2.close()
                    fis2.close()
                    //notify user that backup had to be used
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Unable to lead data. Backup loade")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        } else {
            //if the file doesnt exist, then it must be a new user
            //show welcome fragment
            val fragmentManager = supportFragmentManager
            val fragment = OnboardingFragment()
            fragment.show(fragmentManager, "")
            //create default saveData instance
            saveData = SaveData(ArrayList(), ArrayList())
        }

        //handling for main clothing list recyclerview
        val adapter = CategoryAdapter(this, saveData.savedCategories, categoryDeleteListener, itemWearListener, itemDeleteListener, addCategoryListener, addItemListener)
        categories = findViewById(R.id.categoryList)
        val linearLayoutManager =  LinearLayoutManager(this)
        categories.layoutManager = linearLayoutManager
        categories.adapter = adapter

        //handling for todays outfit preview recyclerview
        val adapter2 = PreviewAdapter(this, saveData.activeItems, removeActiveItem)
        today = findViewById(R.id.outfitPreview)
        val linearLayoutManager2 =  LinearLayoutManager(this)
        linearLayoutManager2.orientation = RecyclerView.HORIZONTAL
        today.layoutManager = linearLayoutManager2
        today.adapter = adapter2

        //handling for clear day button
        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            saveData.activeItems.clear()
            today.adapter?.notifyDataSetChanged()
        }

        //handling for end day button
        findViewById<Button>(R.id.endButton).setOnClickListener {
            if (saveData.activeItems.size != 0) {
                //for every item worn today...
                for (i in saveData.activeItems.indices) {
                    //find them in save data and...
                    for (cata in saveData.savedCategories.filter { it.id == saveData.activeItems[i].categoryId }) {
                        for (item in cata.items.filter { it.id == saveData.activeItems[i].id }) {
                            //update their worn data
                            if (item.wash) item.worn = 0
                            else item.worn += 1
                        }
                    }
                }
                //clear active items and search text, notify adapters that data has been updated
                saveData.activeItems.clear()
                searchEditText.text.clear()
                today.adapter?.notifyDataSetChanged()
                categories.adapter?.notifyDataSetChanged()
            }
        }

        //handling for icon for toggling edit mode
        findViewById<View>(R.id.editImage).setOnClickListener {
            (categories.adapter as CategoryAdapter).toggleMode()
            if ((categories.adapter as CategoryAdapter).editMode) {
                //clear data to be default
                (application as ApplicationBase).changedCategories.clear()
                (application as ApplicationBase).changedItems.clear()
                findViewById<ImageView>(R.id.editImage).setColorFilter(R.color.red)
            } else {
                findViewById<ImageView>(R.id.editImage).colorFilter = null
                //get changed categories and save changes
                (application as ApplicationBase).changedCategories.let {
                    for (category in saveData.savedCategories.filter {cat -> it.containsKey(cat.id)}) {
                        category.name = it[category.id]?.name ?: category.name
                        category.desiredWorn = it[category.id]?.desiredWorn ?: category.desiredWorn
                    }
                    it.clear()
                }

                //get changed items and save changes
                (application as ApplicationBase).changedItems.let {
                    for (category in saveData.savedCategories) {
                        for (item in category.items.filter {item -> it.containsKey(category.id.toString() + "-" + item.id)}) {
                            item.name = it[category.id.toString() + "-" + item.id]?.name ?: item.name
                            item.maxWorn = it[category.id.toString() + "-" + item.id]?.maxWorn ?: item.maxWorn
                        }
                    }
                    it.clear()
                }
            }
        }

        //handling for search box
        searchEditText = findViewById(R.id.searchText)
        val searchResult = findViewById<RecyclerView>(R.id.searchResult)
        val adapter3 = SearchAdapter(this, searchResults, itemWearListener)
        val linearLayoutManager3 =  LinearLayoutManager(this)
        findViewById<ImageView>(R.id.clearInputButton).setOnClickListener {
            searchEditText.text.clear()
        }
        //setting up adapter
        linearLayoutManager3.orientation = RecyclerView.HORIZONTAL
        searchResult.layoutManager = linearLayoutManager3
        searchResult.adapter = adapter3
        //when texts change
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                //hide search result if search term is empty
                if (s.isEmpty()) {
                    searchResult.visibility = View.GONE
                    return
                }
                searchResult.visibility = View.VISIBLE
                val searchTerm = s.toString()
                searchResults.clear()
                //search for items with matching names
                for (cat in saveData.savedCategories){
                    for (item in cat.items) {
                        if (item.name.contains(searchTerm, true)) searchResults.add(item)
                    }
                }
                //notify adapter of new data
                searchResult.adapter?.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onStop() {
        //save data to file
        val fos: FileOutputStream = applicationContext.openFileOutput("data", Context.MODE_PRIVATE)
        val os = ObjectOutputStream(fos)
        os.writeObject(saveData)
        os.flush()
        os.close()
        fos.close()
        super.onStop()
    }

    //handle restoring add item/category on app killed
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("IN_ADD_ITEM", inAddItem)
        outState.putBoolean("IN_ADD_CAT", inAddCat)
        outState.putInt("CAT_ID", catIdGlobal)
        outState.putString("FILENAME", filename)
        super.onSaveInstanceState(outState)
    }

    //handle restoring add item/category on app killed
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.getBoolean("IN_ADD_ITEM"))
            addItemHandler(savedInstanceState.getInt("CAT_ID"))
        if (savedInstanceState.getBoolean("IN_ADD_CAT"))
            addCategoryListener.onClick(View(this))
        savedInstanceState.getString("FILENAME")?.let {filename = it}
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() {
        //if add view is visible(active) hide them on back pressed
        if (findViewById<View>(R.id.addHolder).visibility == View.VISIBLE) {
            inAddItem = false
            inAddCat = false
            findViewById<View>(R.id.addHolder).visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    //hide keyboard function for add item/category dialog
    private fun hideKeyboard(activity: Activity) {
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

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile("tmp")
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.palepeak.closet_tracker",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    /*
    //create take picture intent to get picture from camera
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

     */

    //when the data is returned
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //get bitmap
            val imageBitmap = BitmapFactory.decodeFile(filename)
            //calculate dimensions for square aspect ratio
            val minLength = min(imageBitmap.width, imageBitmap.height)
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
            //crop bitmap
            val croppedBitmap = Bitmap.createBitmap(imageBitmap, x, y, croppedWidth, croppedHeight)

            //get rotation data of image
            val exif = ExifInterface(filename)
            //rotate accordingly
            val angle = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val matrix = Matrix()
            matrix.postRotate(angle)
            val rotatedBitmap = when (angle) {
                0f -> croppedBitmap
                else -> Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true)
            }

            //scale down the cropped bitmap to be 100dp
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
            bitmap = Bitmap.createScaledBitmap(rotatedBitmap, px.toInt(), px.toInt(), false)

            //delete tmp file used to get exif data
            val file = File(filename)
            file.delete()

            //save bitmap to the imageview
            imageViewPreview.setImageBitmap(bitmap)
            //add item will get bitmap from the global bitmap variable
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //get unique image file name
    @Throws(IOException::class)
    private fun createImageFile(file: String): File {
        // Create an image file name
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            file, /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            filename = absolutePath
        }
    }

    //companion object for request ID
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
