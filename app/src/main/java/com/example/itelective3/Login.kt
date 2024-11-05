package com.example.itelective3


import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivityLoginBinding
import com.example.itelective3.databinding.DialogForgotPasswordBinding
import com.example.itelective3.databinding.DialogLoadingBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



class Login : AppCompatActivity() {

    // Binding object para sa layout sa Login activity
    private lateinit var binding: ActivityLoginBinding
    // Binding object para sa layout sa Forgot Password dialog
    private lateinit var dialogForgotPasswordBinding: DialogForgotPasswordBinding
    // GoogleSignInClient nga mag-manage sa Google sign-in
    private lateinit var googleSignInClient: GoogleSignInClient
    // FirebaseAuth object para sa authentication gamit ang Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    // AuthCredential nga gigamit para sa pag-authenticate sa user
    private lateinit var credential: AuthCredential

    private lateinit var loadingDialog: Dialog
    private lateinit var dialogForgotPassword: Dialog

    // Companion object nga nag-himo sa constant value
    companion object {
        // Request code para sa Google Sign-In
        private const val RC_SIGN_IN = 9001 //Request code para sa Google Sign in
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize ug binding gamit ang layout sa Login
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth


        // Configure ang Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso) //set para mag handle sa sign in with Google using configuration in gso


        // Set up ang mga button listeners
        binding.googlebtn.setOnClickListener { signIn() } //para sa Google Sign in
        binding.forgotPass.setOnClickListener { forgotPassword() } //para sa Forgot Pass
        binding.loginButton.setOnClickListener { btnLogin() } //para sa Email/Password Login
        binding.signUpButton.setOnClickListener { btnSignUp() } //para sa pag register
    }

    //method para sa Forgot password dialog
    private fun forgotPassword() {
        // Nag-inflate sa layout para sa forgot password dialog
        dialogForgotPasswordBinding = DialogForgotPasswordBinding.inflate(layoutInflater)
        // Pag-setup sa AlertDialog using ang layout sa forgot password
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogForgotPasswordBinding.root)
        builder.setCancelable(false) // Dili ma-cancel ang dialog kung i-tap ang gawas

        dialogForgotPassword = builder.create()

        //mga button sa forgot password dialog
        dialogForgotPasswordBinding.btnReset.setOnClickListener {
            // Mo tawag sa resetPassword function ug mo pasa sa email input
            resetPassword(dialogForgotPasswordBinding.email)
        }
        dialogForgotPasswordBinding.btnCancel.setOnClickListener {
            dialogForgotPassword.dismiss()
        }

        dialogForgotPassword.show()
    }

    //method para sa pag reset ug password with email
    private fun resetPassword(input: TextInputEditText) {
        val email = input.text.toString()
        if (email.isEmpty()) {
            input.error = "Please enter an email."
            Toast.makeText(this, "Please enter an email.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input.error = "Please enter a valid email."
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
            return
        }

        //use firebase para sa pag send ug reset password
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogForgotPassword.dismiss()
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("RESET PASSWORD SENT!")
                    .setMessage("Please check your email, including the spam folder.")
                    .setNegativeButton(android.R.string.yes) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    //method for initiate sa google sign in
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        // Mo-start sa activity aron mag login using ang Google
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //method nga mo handle sa result sa google sign in activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // eh check kung ang result is from Google Sign-In request
        if (requestCode == RC_SIGN_IN) {
               // to get the result sa sign in using data from Google Sign-In
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
           // if successful, eh get ang account using sa google sign in
            // ug eh authenticate ni firebase using ang firebaseAuthWithGoogle(account.idToken!!
            // and if error, i-display ang "Google sign-in failed" message using ang Toast
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //method for email/password login
    private fun btnLogin() {
        val email = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (email.isEmpty()) {
            binding.usernameInput.error = "Please enter email."
            return
        }
        if (password.isEmpty()) {
            binding.passwordInput.error = "Please enter a password."
            return
        }

        showLoadingDialog()

        //pag login with the use of firebase authentication
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            dismissLoadingDialog()
            if (it.isSuccessful) {
                goToHome()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method para sa pag-authenticate using ang Google ID token
    private fun firebaseAuthWithGoogle(idToken: String) {
        //get the Google credential using ang idToken nga gi-provide ni Google para eh authenticate sa Firebase
        credential = GoogleAuthProvider.getCredential(idToken, null)
        showLoadingDialog()
        // Pag-sign in sa Firebase using ang credential nga gikan sa Google
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            dismissLoadingDialog()
            goToHome()
        }.addOnFailureListener {
            dismissLoadingDialog()
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    //method to go homepage
    private fun goToHome() {
        startActivity(Intent(this, Homepage::class.java))
        finish()
    }

    //to show loading dialog
    private fun showLoadingDialog() {
        val loadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        loadingDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(loadingBinding.root)
            .create()

        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }


        private fun btnSignUp() {
            startActivity(Intent(this, SignUp::class.java))
        }
}









