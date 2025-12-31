package com.jonathanev.review.Fragments.ViewHolders

import androidx.recyclerview.widget.RecyclerView
import com.jonathanev.review.presentation.model.PreviewQuestionUi
import com.jonathanev.review.R
import com.jonathanev.review.databinding.ListPreviewQuestionsBinding
import com.jonathanev.review.presentation.model.QuestionContentUi
import javax.inject.Inject

class ListPreviewQuestionsViewHolder @Inject constructor(
    private val binding: ListPreviewQuestionsBinding,
    private val clickedPlay: (Int) -> Unit,
    private val clickedEdit: (Int) -> Unit,
): RecyclerView.ViewHolder(binding.root) {
    fun bind(previewQuestionUi: PreviewQuestionUi) {
        when(previewQuestionUi.question){
            is QuestionContentUi.Image -> {
                binding.previewQuestion.text = binding.previewQuestion.context.getString(R.string.withoutPreviewQuestion)
                binding.noImages.text = previewQuestionUi.noImages
            }
            QuestionContentUi.None -> {
                binding.previewQuestion.text = binding.previewQuestion.context.getString(R.string.withoutPreviewQuestion)
                binding.noImages.text = previewQuestionUi.noImages
            }
            is QuestionContentUi.Text -> {
                binding.previewQuestion.text = previewQuestionUi.question.text
                binding.noImages.text = previewQuestionUi.noImages
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