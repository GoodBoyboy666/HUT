package top.goodboyboy.hut.Util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt

class BioUtil {
    fun checkBiometricSupport(context: Context): BioStatus {
        val biometricManager=BiometricManager.from(context)
        when(biometricManager.canAuthenticate(BIOMETRIC_STRONG)){
            BiometricManager.BIOMETRIC_SUCCESS->{
                return BioStatus(true,null)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{
                return BioStatus(false,"设备不支持生物验证")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE->{
                return BioStatus(false,"生物验证硬件当前不可用")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{
                return BioStatus(false,"未注册生物验证")
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN->{
                return BioStatus(false,"生物验证状态未知")
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                return BioStatus(false,"设备安全需要升级")            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                return BioStatus(false,"不支持生物验证")            }
        }
        return BioStatus(false,"其他原因，不支持生物验证")
    }

    fun startAuthentication(biometricPrompt:BiometricPrompt){
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("请验证您的身份")
            .setSubtitle("使用指纹完成身份验证")
            .setNegativeButtonText("取消验证")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    class BioStatus(
        val status:Boolean,
        val reason:String?
    )
}