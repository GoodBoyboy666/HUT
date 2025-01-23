package top.goodboyboy.hut.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.BioUtil
import top.goodboyboy.hut.databinding.ActivityBioBinding

class BioActivity : AppCompatActivity() {
    private lateinit var binding:ActivityBioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityBioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@BioActivity, "验证错误： $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val intent=Intent(this@BioActivity,MainActivityPage::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@BioActivity, "验证失败！", Toast.LENGTH_SHORT).show()
            }
        })

        val bio=BioUtil()
        val bioStatus=bio.checkBiometricSupport(this)

        if(bioStatus.status){
            bio.startAuthentication(biometricPrompt)
        }else{
            Toast.makeText(this,bioStatus.reason?:"未知错误",Toast.LENGTH_SHORT).show()
        }

        binding.bioButton.setOnClickListener{
            if(bioStatus.status){
                bio.startAuthentication(biometricPrompt)
            }else{
                Toast.makeText(this,bioStatus.reason?:"未知错误",Toast.LENGTH_SHORT).show()
            }
        }

        binding.logout.setOnClickListener{
            val internalStorageDir = this.filesDir
            KbFunction.clearDirectory(internalStorageDir)
            Toast.makeText(this, "注销完成！", Toast.LENGTH_LONG).show()
            ActivityCompat.finishAffinity(this)
        }

    }
}