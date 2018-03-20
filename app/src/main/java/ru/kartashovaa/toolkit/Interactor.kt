package ru.kartashovaa.toolkit

/**
 * Created by Kartashov A.A. on 12/11/17.
 * This interface describe the common part of any interactor
 * In this case, we don't have any usage of type param P,
 * But this needed to force understanding that interactor is paired to presenter
 */

interface Interactor<P : Presenter<*>>