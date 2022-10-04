package com.example.musicplayer1

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var playIB: ImageButton
    private lateinit var metadataRetriever: MediaMetadataRetriever
    private lateinit var toolbar : MaterialToolbar
    private lateinit var imageView : ImageView
    private lateinit var seekbar: SeekBar
    private lateinit var loadButton : ExtendedFloatingActionButton
    private lateinit var nameTextView : TextView
    private lateinit var artistTextView : TextView
    private lateinit var extraInfoTextView : TextView
    private var isMusicLoaded : Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private val mimeTypes = arrayOf("audio/mpeg")


    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: SeekBar?,
            progress: Int,
            fromUser: Boolean
        ) {
            if (fromUser) {
                mediaPlayer?.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private val openMusicLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
                .also {
                    isMusicLoaded = true
                    seekbar.max = it.duration
                    it.start()
                    playIB.setImageResource(R.drawable.ic_pause_filled)
                    lifecycleScope.launch {
                        while (it.isPlaying) {
                            seekbar.progress = it.currentPosition
                            delay(1000)
                        }
                    }
                    if (uri != null) {
                        setDetails(imageView, uri)
                    }
                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Getting all the views
        assignViews()

        //Setting the greeting in Toolbar
        greetThroughToolbarTitle(toolbar)

        //To make the thumbnail image rounded
        imageView.clipToOutline = true

        //Adding Event Listeners
        seekbar.setOnSeekBarChangeListener(seekBarListener)
        loadButton.setOnClickListener {
            openMusicLauncher.launch(mimeTypes)

        }
        playIB.setOnClickListener {
            if(isMusicLoaded) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    playIB.setImageResource(R.drawable.ic_play_filled)
                } else {
                    mediaPlayer?.start()
                    playIB.setImageResource(R.drawable.ic_pause_filled)
                }
            }
        }
    }

    private fun assignViews() {
        loadButton = findViewById(R.id.extendedFloatingActionButton)
        toolbar = findViewById(R.id.materialToolbar)
        playIB = findViewById(R.id.playPauseButton)
        imageView = findViewById(R.id.imageView)
        seekbar = findViewById(R.id.seekBar)
        nameTextView = findViewById(R.id.NameTextView)
        artistTextView = findViewById(R.id.ArtistNameTextView)
        extraInfoTextView = findViewById(R.id.ExtraInfoTextView)
    }

    private fun setDetails(imageView: ImageView?, uri: Uri) {
        metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(applicationContext, uri)
        val art = metadataRetriever.embeddedPicture
        val songImage = art?.let { BitmapFactory.decodeByteArray(art, 0, it.size) }
        if(songImage != null)   imageView?.setImageBitmap(songImage)
        nameTextView.text = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        artistTextView.text = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        extraInfoTextView.text = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    }

    private fun greetThroughToolbarTitle(toolbar: MaterialToolbar?) {
        val time = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
        val hr = Integer.parseInt(time.toString())
        if(hr < 12)     toolbar?.title = "Good Morning"
        else if(hr < 16) toolbar?.title = "Good Afternoon"
        else            toolbar?.title = "Good evening"
    }
    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        playIB.setImageResource(R.drawable.ic_play_filled)
    }
}