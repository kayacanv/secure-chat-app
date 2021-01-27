package com.example.messengerapp

import java.security.PrivateKey
import java.security.PublicKey

class Singleton {
  companion object {
    lateinit var publicKey : PublicKey
    lateinit var privateKey : PrivateKey
    private val ourInstance = Singleton()
    fun getInstance(): Singleton {
      return ourInstance
    }
  }
  lateinit var currentUser: UserModel
}