package ru.volgadev.pay_lib

interface PayApi {
    fun pay(itemId: String, price: Int)
}