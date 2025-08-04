package com.example.homescreen

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PageAdapter
    private var imageList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageList = List(140) {
            "https://picsum.photos/200/200?random=${Random.nextInt()}"
        }.toMutableList() // 210 images = 3 pages

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        PagerSnapHelper().attachToRecyclerView(recyclerView)

        adapter = PageAdapter(imageList.chunked(70)) // 70 items per page
        recyclerView.adapter = adapter

        val toolbar = findViewById<Toolbar>(R.id.topToolbar)

        setActionBar(toolbar)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_reload -> {
                imageList = List(140) {index ->
                    "https://picsum.photos/200/200?random=${Random.nextInt()}"
                }.toMutableList()

                adapter.updateData(imageList.chunked(70));
                Toast.makeText(this, "Reload clicked", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_add -> {
                val newImageUrl = "https://picsum.photos/200/200?random=${Random.nextInt()}"

                imageList.add(newImageUrl)
                adapter.updateData(imageList.chunked(70))
                Toast.makeText(this, "Add clicked", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
}


class PageAdapter(private var pages: List<List<String>>) : RecyclerView.Adapter<PageAdapter.PageViewHolder>() {
    private var toolbarHeight: Int = 0

    class PageViewHolder(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val rv = RecyclerView(parent.context)
        rv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        rv.layoutManager = GridLayoutManager(parent.context, 7) // 10 rows = 10 items per column
        rv.addItemDecoration(SpaceItemDecoration(2))
        return PageViewHolder(rv)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val gridAdapter = GridAdapter(pages[position])
        gridAdapter.setToolbarHeight(toolbarHeight);
        holder.recyclerView.adapter = gridAdapter
    }

    override fun getItemCount() = pages.size



    fun updateData(newImages: List<List<String>>) {
        pages = newImages
        notifyDataSetChanged()
    }

}

class GridAdapter(private val images: List<String>) : RecyclerView.Adapter<GridAdapter.ImageViewHolder>() {

    private var toolbarHeight: Int = 0

    class ImageViewHolder(
        private val container: FrameLayout,
        val imageView: ImageView,
        val progressBar: ProgressBar
    ) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val context = parent.context

        val screenWidth = Utils.getScreenWidth(context)
        val screenHeight = Utils.getScreenHeight(context)

        val totalSpacingHeight = 2.dp * (10 - 1)
        val totalSpacingWidth = 2.dp * (7 - 1)
        val toolbarHeight = context.getActionBarHeight()


        val itemWidth = ( screenWidth - totalSpacingWidth) / 7     // 👈 7 cột
        val itemHeight = (Utils.getUsableScreenHeight(context as Activity) - totalSpacingHeight - toolbarHeight) / 10 // 👈 10 dòng
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(itemWidth, itemHeight)
        }
        val imageView = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(itemWidth, itemHeight)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val progressBar = ProgressBar(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                20.dp,
                20.dp,
                Gravity.CENTER
            )
            visibility = View.VISIBLE
        }

        container.addView(imageView)
        container.addView(progressBar)


        return ImageViewHolder(container, imageView, progressBar)

    }

    fun setToolbarHeight(toolbarHeight: Int) {
        this.toolbarHeight = toolbarHeight
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageView = holder.imageView  as ImageView
        val progressBar = holder.progressBar as ProgressBar

        progressBar.visibility = View.VISIBLE

        Glide.with(imageView.context)
            .load(images[position])
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CenterCrop(),RoundedCorners(7.dp)).listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                }
            )
            .into(imageView)
    }

    override fun getItemCount() = images.size
}
fun View.getStatusBarHeightCompat(callback: (Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        callback(topInset)
        insets // return original insets
    }
    requestApplyInsets() // ⚠️ quan trọng: để trigger listener
}

class SpaceItemDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
    private val spacePx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, spaceDp.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.set(2.dp
                / 2, 2.dp
                / 2, 2.dp
                / 2, 2.dp
                / 2)
    }
}


val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()