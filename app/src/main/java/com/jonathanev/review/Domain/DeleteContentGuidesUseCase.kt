package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class DeleteContentGuidesUseCase @Inject constructor(){
   operator fun invoke(currentPath : File): Boolean{
       return currentPath.delete()
   }
}