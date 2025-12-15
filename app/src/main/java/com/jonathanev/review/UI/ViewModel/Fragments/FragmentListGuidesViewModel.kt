package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.GuideResult
import com.jonathanev.review.Domain.ChangeGuidePathBuildFileUseCase
import com.jonathanev.review.Domain.GetGuidePosicionUseCase
import com.jonathanev.review.Domain.LoadGuidesUseCase
import com.jonathanev.review.Domain.SetMainPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val changeGuidePathBuildFileUseCase: ChangeGuidePathBuildFileUseCase
) : ViewModel() {

    private var cachedGuides: List<GuideModel> = emptyList()
    private val _guides = MutableLiveData<List<GuideModel>>()
    val guides: LiveData<List<GuideModel>> = _guides

    fun getAllGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
        _guides.postValue(cachedGuides)
    }

    fun getGuideSelected(position: Int): GuideResult {
        return getGuidePosicionUseCase(position, cachedGuides)
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
        getAllGuides()
    }

    fun changeFilePath(nameGuide: String) {
        changeGuidePathBuildFileUseCase.invoke(nameGuide)
        getAllGuides()
    }
}
