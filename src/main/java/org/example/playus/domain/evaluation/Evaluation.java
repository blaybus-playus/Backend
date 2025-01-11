package org.example.playus.domain.evaluation;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "evaluation")
public class Evaluation {
    private String term;  // 평가 기간

    private List<PersonalEvaluation> personalEvaluation; // 개인 평가

    @Builder
    public Evaluation(String term, List<PersonalEvaluation> personalEvaluation) {
        this.term = term;
        this.personalEvaluation = personalEvaluation != null ? personalEvaluation : new ArrayList<>();
    }
}
