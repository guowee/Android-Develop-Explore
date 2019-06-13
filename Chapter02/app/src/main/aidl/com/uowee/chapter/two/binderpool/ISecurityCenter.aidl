package com.uowee.chapter.two.binderpool;

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}