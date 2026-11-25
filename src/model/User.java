package model;

/**
 * Abstract parent class representing any person interacting with SmartPark Pro.
 */
public abstract class User {

    private final String userId;
    private String name;
    private String phone;

    protected User(String userId, String name, String phone) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public abstract String getRoleDescription();

    @Override
    public String toString() {
        return "[" + userId + "] " + name + " (" + phone + ") - " + getRoleDescription();
    }
}
