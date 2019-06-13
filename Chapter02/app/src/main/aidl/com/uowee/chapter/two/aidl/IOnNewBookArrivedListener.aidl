// IOnNewBookArrivedListener.aidl
package com.uowee.chapter.two.aidl;

// Declare any non-default types here with import statements
import com.uowee.chapter.two.aidl.Book;
interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
