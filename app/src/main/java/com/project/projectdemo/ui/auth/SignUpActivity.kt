package com.project.projectdemo.ui.auth

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.project.projectdemo.R
import com.project.projectdemo.data.localdatabase.AppDatabase
import com.project.projectdemo.data.repositories.UserRepository
import com.project.projectdemo.databinding.SignupActivityBinding
import com.project.projectdemo.ui.home.HomePageActivity
import com.project.projectdemo.util.toast
import androidx.lifecycle.viewModelScope
import com.project.projectdemo.data.preferences.PreferenceProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: SignupActivityBinding
    private lateinit var viewModel: ViewModelAuth
    private lateinit var prefs: PreferenceProvider
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.signup_activity)
        prefs = PreferenceProvider(this)
        val dao = AppDatabase.getInstance(application)!!.getUserDao()
        val repository = UserRepository(dao,prefs)
        val factoryAuth = ViewModelFactoryAuth(repository)
        viewModel = ViewModelProvider(this, factoryAuth).get(ViewModelAuth::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        if (checkPermissions()) {
            Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissions(): Boolean {
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val manage = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && manage == PackageManager.PERMISSION_GRANTED
                && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
            ), PERMISSION_CODE
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

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        //called when user presses ALLOW or DENY from Permission Request Popup
//        when(requestCode){
//            PERMISSION_CODE -> {
//                if (grantResults.size > 0 && grantResults[0] ==
//                    PackageManager.PERMISSION_GRANTED){
//                    //permission from popup was granted
//                    dispatchTakePictureIntent()
//                } else{
//                    //permission from popup was denied
//                    toast("Permission denied")
//                }
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                viewModel.profilePicture.value = viewModel.encodeImage(imageBitmap)
            }
        }
    }

    private fun dispatchTakePictureIntent() {

    }

    fun onClickListener(view: View) {
        val rbg = findViewById<RadioGroup>(R.id.radioGroup1)
        val selected = rbg.checkedRadioButtonId
        val gender: RadioButton = findViewById<View>(selected) as RadioButton
        viewModel.radioChecked = gender.text.toString()
    }

    fun onClickSignUp(view: View){
        viewModel.viewModelScope.launch {
            if (viewModel.onClickRegister()&& viewModel.isFormValid) {
                prefs.saveUserEmail(viewModel.returnUsername())
                switchToHomepage()
            }
            else if(viewModel.isFormValid){
               toast("Account with this email already exists")
            }
            else{
                toast("Please fill all details!")
            }
        }
    }

    fun onClickTakeProfilePicture(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewModel.viewModelScope.launch {
                dispatchTakePictureIntent()
            }
        } else {
            toast("Sorry you're version android is not support, Min Android 6.0 (Marsmallow)")
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun onClickSignIn(view: View){
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun switchToHomepage(){
        intent = Intent(this@SignUpActivity, HomePageActivity::class.java)
        intent.putExtra("User email", prefs.getUserEmail())
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}