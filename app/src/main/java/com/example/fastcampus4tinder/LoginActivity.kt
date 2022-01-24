package com.example.fastcampus4tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.fastcampus4tinder.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private var mBinding: ActivityLoginBinding? = null
    private val binding get() = mBinding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()

        initLoginButton()
        initSignUpButton()
        initEmailAndPasswordEditText()
        initFaceBookLoginButton()

    }



    //로그인 관련 코드
    private fun initLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        handleSuccessLogin()
                    } else {
                        Toast.makeText(this, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    //회원 가입 관련 코드
    private fun initSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "회원 가입에 성공했습니다. 로그인 버튼을 눌러주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "이미 가입한 이메일이거나, 회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()

                    }
                }
        }
    }
    //예외 처리 (이메일 로그인)
    private fun initEmailAndPasswordEditText() {
        val emailEt = binding.emailEditText
        val passwordEt = binding.passwordEditText

        emailEt.addTextChangedListener {
            val enable = emailEt.text.isNotEmpty() && passwordEt.text.isNotEmpty()
            binding.loginButton.isEnabled = enable
            binding.signUpButton.isEnabled = enable
        }
        passwordEt.addTextChangedListener {
            val enable = emailEt.text.isNotEmpty() && passwordEt.text.isNotEmpty()
            binding.loginButton.isEnabled = enable
            binding.signUpButton.isEnabled = enable
        }
    }
    private fun initFaceBookLoginButton() {
        val faceBookLoginButton = binding.facebookLoginButton

        faceBookLoginButton.setPermissions("email", "public_profile")
        faceBookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        if (task.isSuccessful) {
                            handleSuccessLogin()
                        }else {
                            Toast.makeText(this@LoginActivity, "페이스북 로그인이 실패했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancel() {}

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@LoginActivity, "페이스북 로그인이 실패했습니다", Toast.LENGTH_SHORT).show()
            }

        })
    }

    //페이스북 로그인
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, requestCode, data)

    }
    private fun handleSuccessLogin() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = auth.currentUser!!.uid
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        val user= mutableMapOf<String,  Any>()
        user["userId"] = userId
        currentUserDB.updateChildren(user)

        finish()
    }




    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}