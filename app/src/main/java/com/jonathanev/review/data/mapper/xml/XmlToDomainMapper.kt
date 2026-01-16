package com.jonathanev.review.data.mapper.xml

import com.jonathanev.review.data.model.xml.ColorRangeXmlDto
import com.jonathanev.review.data.model.xml.GuideXmlDto
import com.jonathanev.review.data.model.xml.QAItemXmlDto
import com.jonathanev.review.data.model.xml.QuestionContentXmlDto
import com.jonathanev.review.data.model.xml.QuestionItemXmlDto
import com.jonathanev.review.data.model.xml.ResponseXmlDto
import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.ResponseDomain

fun QAItemXmlDto.toDomain(): QAItemDomain {
    return QAItemDomain(this.question.toDomain(), this.answer.toDomain())
}

fun ResponseXmlDto.toDomain(): ResponseDomain {
    return when (this) {
        ResponseXmlDto.Empty -> ResponseDomain.Empty
        is ResponseXmlDto.Filled -> ResponseDomain.Filled(this.item.toDomain())
    }
}

fun QuestionItemXmlDto.toDomain(): QuestionItemDomain {
    return QuestionItemDomain(this.content.map { it.toDomain() })
}

fun QuestionContentXmlDto.toDomain(): QuestionContentDomain {
    return when (this) {
        is QuestionContentXmlDto.Image -> QuestionContentDomain.Image(this.uri, this.nameFile)
        QuestionContentXmlDto.None -> QuestionContentDomain.None
        is QuestionContentXmlDto.Text -> QuestionContentDomain.Text(
            this.text,
            this.colorRangeXmlDto.map { it.toDomain() })
    }
}

fun QuestionContentXmlDto.Image.toDomain(): QuestionContentDomain.Image {
    return QuestionContentDomain.Image(this.uri, this.nameFile)
}

fun ColorRangeXmlDto.toDomain(): ColorRangeDomain {
    return ColorRangeDomain(this.start, this.end, this.color)
}

fun GuideXmlDto.toDomain(): GuideDomainModel =
    GuideDomainModel(
        version = this.version.toGuideVersion(),
        nameGuide = this.nameGuide,
        description = this.description
    )

fun String.toGuideVersion(): GuideVersion {
    return when(this){
        "1.0" -> GuideVersion.V1
        "2.0" -> GuideVersion.V2
        else -> GuideVersion.V2
    }
}