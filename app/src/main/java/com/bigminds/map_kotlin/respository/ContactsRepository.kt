package com.bigminds.map_kotlin.respository

import androidx.lifecycle.LiveData
import com.bigminds.map_kotlin.ContactsApp
import com.bigminds.map_kotlin.database.ContactsDatabase
import com.bigminds.map_kotlin.model.Contact


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository {
    private val database = ContactsDatabase.getDatabase(ContactsApp.appContext)
    private val dao = database.contactsDao()

    suspend fun saveContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            database.contactsDao().insertContact(contact)
        }
    }
    suspend fun updateContactPhoto(contactId: Int, photoUri: String) {
        withContext(Dispatchers.IO) {
            database.contactsDao().updateContactPhoto(contactId, photoUri)
        }
    }

    fun getContacts(): LiveData<List<Contact>> = database.contactsDao().getAllContacts()

    fun getContactById(contactId:Int): LiveData<Contact> = database.contactsDao().getContactById(contactId)

}
