package com.project.projectdemo.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.project.projectdemo.data.entities.User
import com.project.projectdemo.data.repositories.UserRepository
import java.text.SimpleDateFormat
import android.util.Base64
import java.util.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.project.projectdemo.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ViewModelAuth(
    private val userRepository: UserRepository
):ViewModel() {
//    val users: LiveData<List<User>> = userRepository.allUsers

   lateinit var user: LiveData<User>

    var radioChecked : String? = null

    val userEmail = MutableLiveData<String>()

    val userPassword = MutableLiveData<String?>()

    val userName = MutableLiveData<String?>()

    val surName = MutableLiveData<String?>()

    val userGender = MutableLiveData<String?>()

    val profilePicture = MutableLiveData<String?>()

    var isFormValid:Boolean = true

   suspend fun onClickRegister(): Boolean {
       if(userName == null || surName == null || userEmail == null || userPassword == null ||  radioChecked.isNullOrBlank())
       {
           isFormValid = false
       }
       else{
           isFormValid = true
           val name = userName.value!!
           val surname = surName.value!!
           val email = userEmail.value!!
           val password = userPassword.value!!
           val gender = radioChecked
           val profilePicture = profilePicture.value
           val date = Calendar.getInstance().time
           val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
           val formattedDate = formatter.format(date)
           return addUser(User(email, name, surname, gender, password, profilePicture, formattedDate))
       }
       return isFormValid
   }
    suspend fun authenticateUser(): Boolean {
        val email = userEmail.value!!
        val password = userPassword.value!!
        Log.i("TAG Auth", "Started Authentication")
        return userRepository.isValidAccount(email, password)
    }
    private suspend fun addUser(user: User) : Boolean{
        return userRepository.insertUser(user)
    }
    fun returnUsername():String{
        return userEmail.value!!
    }

    fun getUserData(username:String){
        user = userRepository.getUserLiveDataByEmail(username)
    }

    fun userData(user: User){
        userName.value = user.name
        surName.value = user.surname
        userEmail.value = user.email
        userPassword.value = user.password
        userGender.value = user.gender
        profilePicture.value = user.profile_picture.toString() // from string to bitmap
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun generatePDF(user: User, scaledbmp: Bitmap, context: Context) {

        try {

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
            title.setColor(ContextCompat.getColor(context, R.color.colorBlack))

            // below line is used to draw text in our PDF file.
            // the first parameter is our text, second parameter
            // is position from start, third parameter is position from top
            // and then we are passing our variable of paint which is title.
            canvas.drawText("Name: " + user.name, 209F, 100F, title)
            canvas.drawText("Surname: " + user.surname, 209F, 80F, title)
            title.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            title.color = ContextCompat.getColor(context, R.color.colorBlack)
            title.textSize = 15F

            // below line is used for setting
            // our text to center of PDF.
            title.textAlign = Paint.Align.CENTER
            canvas.drawText("This is sample document which we have created.", 396F, 560F, title)

            // after adding all attributes to our
            // PDF file we will be finishing our page.
            pdfDocument.finishPage(myPage)

            // below line is used to set the name of
            // our PDF file and its path.
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "GFG.pdf"
            )


            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(FileOutputStream(file))

            // on below line we are displaying a toast message as PDF file generated..
            Toast.makeText(context, "PDF file generated..", Toast.LENGTH_SHORT).show()

        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close()
        }
        catch (e: Exception) {
            // below line is used
            // to handle error
            e.printStackTrace()

            // on below line we are displaying a toast message as fail to generate PDF
            Toast.makeText(context, "Fail to generate PDF file..", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun encodeImage(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}