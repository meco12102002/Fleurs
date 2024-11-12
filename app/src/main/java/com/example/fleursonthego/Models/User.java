package com.example.fleursonthego.Models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String email;
    private String phoneNumber;
    private String userType; // New field for user type (e.g., "customer" or "admin")
    private String profilePicture; // New field for profile picture URL
    private String password; // New field for password (consider hashing in production)
    private String cartId; // New field for a unique cart ID if necessary
    private Address shippingAddress;
    private List<String> orderHistory;
    private List<String> wishlist;
    private Cart cart; // Cart object
    private List<String> messages;
    private String fcmToken;

    // Default constructor for Firebase
    public User() {
        // Initialize lists to avoid NullPointerExceptions
        this.orderHistory = new ArrayList<>();
        this.wishlist = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    // Constructor with parameters
    public User(String name, String email, String phoneNumber, String userType, String profilePicture,
                String password, String cartId, Address shippingAddress, List<String> orderHistory,
                List<String> wishlist, Cart cart, List<String> messages) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.profilePicture = profilePicture;
        this.password = password;
        this.cartId = cartId;
        this.shippingAddress = shippingAddress;
        this.orderHistory = orderHistory != null ? orderHistory : new ArrayList<>();
        this.wishlist = wishlist != null ? wishlist : new ArrayList<>();
        this.cart = cart;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<String> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<String> orderHistory) {
        this.orderHistory = orderHistory != null ? orderHistory : new ArrayList<>();
    }

    public List<String> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<String> wishlist) {
        this.wishlist = wishlist != null ? wishlist : new ArrayList<>();
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {

    }

    public void setOrders(ArrayList<Object> objects) {
    }

    // Address class
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        // Default constructor for Firebase
        public Address() {
        }

        public Address(String street, String city, String state, String postalCode, String country) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

        // Getters and Setters for Address
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
