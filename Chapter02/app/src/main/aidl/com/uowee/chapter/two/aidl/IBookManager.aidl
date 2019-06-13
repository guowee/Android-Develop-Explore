// IBookManager.aidl
package com.uowee.chapter.two.aidl;

// Declare any non-default types here with import statements

import com.uowee.chapter.two.aidl.Book;
import com.uowee.chapter.two.aidl.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(in IOnNewBookArrivedListener listener);
    void unregisterListener(in IOnNewBookArrivedListener listener);
}
