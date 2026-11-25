package model;

/**
 * Represents a mall customer/vehicle owner registered in the parking system.
 */
public class Customer extends User {

    private String membershipTier;

    public Customer(String userId, String name, String phone, String membershipTier) {
        super(userId, name, phone);
        this.membershipTier = membershipTier;
    }

    public String getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(String membershipTier) {
        this.membershipTier = membershipTier;
    }

    @Override
    public String getRoleDescription() {
        return "Customer (" + membershipTier + ")";
    }
}
