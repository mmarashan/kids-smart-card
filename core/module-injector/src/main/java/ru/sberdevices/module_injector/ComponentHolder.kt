package ru.sberdevices.module_injector

interface ComponentHolder<C : BaseAPI, D : BaseDependencies> {
    fun init(dependencies: D)
    fun get(): C
    fun clear()
}

interface BaseDependencies

interface BaseAPI