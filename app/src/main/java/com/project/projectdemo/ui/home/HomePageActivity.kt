package com.project.projectdemo.ui.home

import android.Manifest.permission.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.project.projectdemo.R
import com.project.projectdemo.data.localdatabase.AppDatabase
import com.project.projectdemo.data.preferences.PreferenceProvider
import com.project.projectdemo.data.repositories.UserRepository
import com.project.projectdemo.databinding.ActivityHomePageBinding
import com.project.projectdemo.ui.auth.ViewModelAuth
import com.project.projectdemo.ui.auth.ViewModelFactoryAuth
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.projectdemo.data.entities.User
import java.io.File
import java.io.FileOutputStream

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var viewModel: ViewModelAuth
    private lateinit var prefs: PreferenceProvider
    private lateinit var bmp: Bitmap
    private lateinit var scaledbmp: Bitmap
    private var PERMISSION_CODE = 101

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home_page)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_page)
        prefs = PreferenceProvider(this)
        val dao = AppDatabase.getInstance(application)!!.getUserDao()
        val repository = UserRepository(dao,prefs)
        val factoryAuth = ViewModelFactoryAuth(repository)
        viewModel = ViewModelProvider(this, factoryAuth)[ViewModelAuth::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        displayUser()

        if (checkPermissions()) {
            Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

     private fun displayUser(){
         val intent = intent
         val userEmail = intent.getStringExtra("User email")
        if (userEmail != null) {
            viewModel.getUserData(userEmail)
        }
         viewModel.user.observe(this) {
             viewModel.userData(it)
         }
    }

    fun onClickSignOut(view: View) {
//        prefs.saveUserLogInState(false)
//        intent = Intent(this,LoginSignupActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun onClickGeneratePDF(view: View) {
        bmp = BitmapFactory.decodeResource(resources, R.drawable.full_logo)
        if(viewModel.user.value != null) {
            scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)
            viewModel.user.value?.let {
                generatePDF(it, scaledbmp, applicationContext)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun generatePDF(user: User, scaledbmp: Bitmap, contextw: Context) {
        var pageHeight = 1120
        var pageWidth = 792

        var pdfDocument = PdfDocument()

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        var paint: Paint = Paint()
        var title: Paint = Paint()

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        var myPageInfo: PdfDocument.PageInfo? =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // below line is used for setting
        // start page for our PDF file.
        var myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)

        // creating a variable for canvas
        // from our page of PDF.
        var canvas: Canvas = myPage.canvas

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.
        canvas.drawBitmap(scaledbmp, 56F, 40F, paint)

        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.textSize = 15F

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this , R.color.colorBlack))

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.

        title.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        title.color = ContextCompat.getColor(this, R.color.colorBlack)
        title.textSize = 15F

        // below line is used for setting
        // our text to center of PDF.
        title.textAlign = Paint.Align.CENTER
        canvas.drawText("Name: " + user.name, 396F, 560F, title)
        canvas.drawText("Age: " + user.surname, 400F, 600F, title)
        //canvas.drawText("This is sample document which we have created.", 396F, 560F, title)

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage)

        // below line is used to set the name of
        // our PDF file and its path.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) , user.name + ".pdf")

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(FileOutputStream(file))

            // on below line we are displaying a toast message as PDF file generated..
            Toast.makeText(this, "PDF file generated..", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // below line is used
            // to handle error
            e.printStackTrace()

            // on below line we are displaying a toast message as fail to generate PDF
            Toast.makeText(this, "Fail to generate PDF file..", Toast.LENGTH_SHORT)
                .show()
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissions(): Boolean {
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )
        val manage = ContextCompat.checkSelfPermission(
            applicationContext,
            MANAGE_EXTERNAL_STORAGE
        )
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && manage == PackageManager.PERMISSION_GRANTED
                && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE), PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}