package com.mkvsk.warehousewizard.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mkvsk.warehousewizard.core.User;
import com.mkvsk.warehousewizard.ui.local.DatabaseClient;
import com.mkvsk.warehousewizard.ui.repository.UserRepository;

import java.util.List;

public class UserViewModel extends ViewModel {
    private UserRepository repository;
    private MutableLiveData<String> login;
    private MutableLiveData<String> password;
    private MutableLiveData<Boolean> isAuthMode;

    public UserViewModel() {
        repository = new UserRepository(DatabaseClient.getInstance().context);
    }

    public List<User> getAllUsers() {
        return repository.getAllUsers().getValue();
    }

    // Auth and register
    public LiveData<String> getLogin() {
        return login;
    }

    public void setLogin(String value) {
        login.setValue(value);
    }

    public LiveData<String> getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password.setValue(value);
    }

    public LiveData<Boolean> getIsAuthMode() {
        return isAuthMode;
    }

    public void setIsAuthMode(boolean value) {
        isAuthMode.setValue(value);
    }

    // DB queries


    private final MutableLiveData<User> userByEmail = new MutableLiveData<>();

    public LiveData<User> getUserByEmail(String email) {
        return userByEmail;
    }

    public User fetchUserByEmail(String email) {
        return repository.getByEmail(email);
    }

//    public void setUserByEmail(LiveData<User> userByEmail) {
//        this.userByEmail.setValue();
//    }

    private final MutableLiveData<User> userByPhoneNumber = new MutableLiveData<>();

    public LiveData<User> getUserByPhoneNumber(String phoneNumber) {
        return userByPhoneNumber;
    }

    public void fetchUserByPhoneNumber(String phoneNumber) {
        userByPhoneNumber.setValue(repository.getByPhoneNumber(phoneNumber));
    }

//    public MutableLiveData

}