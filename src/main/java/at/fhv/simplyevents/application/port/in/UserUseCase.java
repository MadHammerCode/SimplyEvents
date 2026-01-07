package at.fhv.simplyevents.application.port.in;

public interface UserUseCase {
    UserProfileResult getUserProfile(String email);
    UserProfileResult updateUserProfile(String email, UpdateUserProfileCommand command);

    record UpdateUserProfileCommand(String firstName, String lastName, String phone, String address) {}
    record UserProfileResult(String firstName, String lastName, String email, String phone, String address, String role) {}
}
