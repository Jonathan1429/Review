package com.jonathanev.review.Fragments.ViewHolders

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.Data.Model.PreviewQuestion
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.databinding.ListPreviewQuestionsBinding
import javax.inject.Inject

class ListPreviewQuestionsViewHolder @Inject constructor(
    private val binding: ListPreviewQuestionsBinding,
    private val clickedPlay: () -> Unit,
    private val clickedEdit: () -> Unit,
): RecyclerView.ViewHolder(binding.root) {
    fun bind(previewQuestion: PreviewQuestion) {
        Log.i("Inicio", "Inicio")
        when(previewQuestion.question){
            is QuestionContent.Image -> Log.i("Image", "Image")
            QuestionContent.None -> Log.i("None", "None")
            is QuestionContent.Text -> {
                binding.previewQuestion.text = previewQuestion.question.text
                binding.noImages.text = previewQuestion.noImages
            }
        }

        binding.iconPlay.setOnClickListener{
            clickedPlay()
        }

        binding.iconEdit.setOnClickListener{
            clickedEdit()
        }
    }
}