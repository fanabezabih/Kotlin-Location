package com.bigminds.map_kotlin.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigminds.map_kotlin.model.Contact
import com.bigminds.map_kotlin.screens.ContactsRepository


import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    val contacts = MutableLiveData<List<Contact>>()
    val repo = ContactsRepository()
    val contactLiveData = MutableLiveData<Contact?>()

    fun getContacts() {
        repo.getContacts().observeForever { contactList ->
            contacts.value = contactList
        }
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            repo.saveContact(contact)
        }
    }


    fun getContactById(contactId: Int) {
        repo.getContactById(contactId).observeForever { contact ->
            contactLiveData.value = contact
        }
    }

    fun updateContactPhoto(contactId: Int, photoUri: String) {
        viewModelScope.launch {
            repo.updateContactPhoto(contactId, photoUri)

            getContactById(contactId)
        }
    }

}