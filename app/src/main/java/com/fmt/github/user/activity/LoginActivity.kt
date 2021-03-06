package com.fmt.github.user.activity

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import com.fmt.github.R
import com.fmt.github.base.activity.BaseDataBindVMActivity
import com.fmt.github.base.viewmodel.BaseViewModel
import com.fmt.github.config.Settings
import com.fmt.github.databinding.ActivityLoginBinding
import com.fmt.github.ext.otherwise
import com.fmt.github.ext.yes
import com.fmt.github.home.activity.HomeActivity
import com.fmt.github.user.model.db.User
import com.fmt.github.user.model.AuthorizationRespModel
import com.fmt.github.user.model.UserLoginModel
import com.fmt.github.user.model.UserModel
import com.fmt.github.user.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseDataBindVMActivity<ActivityLoginBinding>() {

    private var mAuthId = 0

    private val mViewModel: UserViewModel by viewModel()

    private val mUserLoginModel: UserLoginModel by lazy { UserLoginModel() }

    override fun getLayoutId(): Int = R.layout.activity_login

    override fun getViewModel(): BaseViewModel = mViewModel

    override fun initView() {
        mSignInBt.setOnClickListener {
            login()
        }
    }

    override fun initData() {
        mDataBind.userLoginModel = mUserLoginModel
    }

    private fun login() {
        val username = mUserLoginModel.username.get().toString()
        val password = mUserLoginModel.password.get().toString()
        username.isNullOrEmpty().yes {
            mUserNameInputLayout.error = getString(R.string.username_not_null)
            mUserNameInputLayout.isErrorEnabled = true
        }.otherwise {
            mUserNameInputLayout.isErrorEnabled = false

            password.isNullOrEmpty().yes {
                mPasswordInputLayout.error = getString(R.string.password_not_null)
                mPasswordInputLayout.isErrorEnabled = true
            }.otherwise {
                mPasswordInputLayout.isErrorEnabled = false
                mSignInBt.visibility = View.GONE
                mProgressBar.visibility = View.VISIBLE
                Settings.Account.userName = username
                Settings.Account.password = password
                createOrGetAuthorization()
            }
        }

    }

    private fun createOrGetAuthorization() {
        mViewModel.createOrGetAuthorization().observe(this, Observer<AuthorizationRespModel> {
            //保存授权后的Token和ID
            Settings.Account.token = it.token
            mAuthId = it.id
            //获取用户信息
            getUserInfo()
        })
    }

    private fun getUserInfo() {
        mViewModel.getUser().observe(this, Observer<UserModel> {
            saveUserInfo(it)
        })
    }

    private fun saveUserInfo(userModel: UserModel) {
        User(mAuthId, userModel.login, userModel.avatar_url).apply {
            mViewModel.saveLocalUser(this@apply)
            go2MainActivity()
        }
    }

    private fun go2MainActivity() {
        Intent(this, HomeActivity::class.java).run {
            startActivity(this)
            finish()
        }
    }

    override fun handleError() {
        mSignInBt.visibility = View.VISIBLE
        mProgressBar.visibility = View.GONE
    }
}