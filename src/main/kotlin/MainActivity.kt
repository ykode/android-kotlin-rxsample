package com.ykode.research.RxKotlinSample

import android.app.Activity
import android.app.Fragment

import android.os.Bundle
import android.content.Context

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View

import android.widget.EditText
import android.widget.Button

import android.graphics.Color

import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription

import com.jakewharton.rxbinding.widget.*

import kotlinx.android.synthetic.fragment_main.*

import kotlin.text.Regex

var View.enabled: Boolean
  get() = this.isEnabled()
  set(value) = this.setEnabled(value)

val Fragment.ctx:Context?
  get() = this.activity

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState:Bundle?) {
    super<Activity>.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (null == savedInstanceState) {
      fragmentManager.beginTransaction()
        .add(R.id.container, MainFragment())
        .commit()
    }
  }
}

abstract class ReactiveFragment : Fragment() {
  private var _compoSub = CompositeSubscription()
  private val compoSub: CompositeSubscription
    get() {
      if (_compoSub.isUnsubscribed()) {
        _compoSub = CompositeSubscription()
      }
      return _compoSub
    }

  protected final fun manageSub(s: Subscription) = compoSub.add(s)
  
  override fun onDestroyView() {
    compoSub.unsubscribe()
    super<Fragment>.onDestroyView()
  }
}

internal class MainFragment : ReactiveFragment() {

  override fun onCreateView(inflater: LayoutInflater?,
    container: ViewGroup?, savedInstanceState: Bundle?): View? = 
    inflater?.inflate(R.layout.fragment_main, container, false) 

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    val emailPattern = Regex("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+
                             "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})${'$'}")
                             
  
    val userNameValid = edtUserName.textChanges().map { t -> t.length > 4 }
    val emailValid = edtEmail.textChanges().map { t -> emailPattern in t }

    manageSub(emailValid.distinctUntilChanged()
                  .map{ b -> if (b) Color.WHITE else Color.RED }
                  .subscribe{ color -> edtEmail.setTextColor(color) })

    manageSub(userNameValid.distinctUntilChanged()
                  .map{ b -> if (b) Color.WHITE else Color.RED }
                  .subscribe{ color -> edtUserName.setTextColor(color) })

    val registerEnabled = Observable.combineLatest(userNameValid, emailValid, {a, b -> a && b})

    manageSub(registerEnabled.distinctUntilChanged()
                  .subscribe{ enabled -> buttonRegister.enabled = enabled })
    }
}
