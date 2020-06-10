package com.practice.junit5study.domain;

public class Member {
    Long id;
    String email;

    public Member() {}

    public Member(Long id, String email){
        this.id = id;
        this.email = email;
    }

    public Long getId() { return id;}
    public String getEmail() { return email;}
}
