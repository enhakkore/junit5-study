package com.practice.junit5study.domain;

import com.practice.junit5study.study.StudyStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Study {

    @Id @GeneratedValue
    private Long Id;
    private String name;
    private Long ownerId;
    private Long limit;
    private StudyStatus status = StudyStatus.DRAFT;
    private LocalDateTime openedDateTime;

    public Study() {}

    public Study(Long limit, String name){
        this.limit = limit;
        this.name = name;
    }

    public Study(Long limit){
        if(limit <= 0) {
            throw new IllegalArgumentException("limit은 0보다 커야 한다.");
        }
        this.limit = limit;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getOwnerId() { return ownerId;}

    public void open() {
        this.openedDateTime = LocalDateTime.now();
        this.status = StudyStatus.OPENED;
    }
}
