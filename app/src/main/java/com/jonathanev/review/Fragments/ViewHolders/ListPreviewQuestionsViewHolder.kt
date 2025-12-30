package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.data.Model.PreviewQuestion
import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ListPreviewQuestionsBinding
import javax.inject.Inject

class ListPreviewQuestionsViewHolder @Inject constructor(
    private val binding: ListPreviewQuestionsBinding,
    private val clickedPlay: (Int) -> Unit,
    private val clickedEdit: (Int) -> Unit,
): RecyclerView.ViewHolder(binding.root) {
    fun bind(previewQuestion: PreviewQuestion) {
        when(previewQuestion.question){
            is QuestionContent.Image -> {
                binding.previewQuestion.text = binding.previewQuestion.context.getString(R.string.withoutPreviewQuestion)
                binding.noImages.text = previewQuestion.noImages
            }
            QuestionContent.None -> {
                binding.previewQuestion.text = binding.previewQuestion.context.getString(R.string.withoutPreviewQuestion)
                binding.noImages.text = previewQuestion.noImages
            }
            is QuestionContent.Text -> {
                binding.previewQuestion.text = previewQuestion.question.text
                binding.noImages.text = previewQuestion.noImages
            }
        }

        binding.iconPlay.setOnClickListener{
            clickedPlay(layoutPosition)
        }

        binding.iconEdit.setOnClickListener{
            clickedEdit(layoutPosition)
        }
    }
}