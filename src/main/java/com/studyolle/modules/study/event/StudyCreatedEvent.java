package com.studyolle.modules.study.event;

import com.studyolle.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
//@RequiredArgsConstructor
public class StudyCreatedEvent {

//    private final Study study;
    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }

}
