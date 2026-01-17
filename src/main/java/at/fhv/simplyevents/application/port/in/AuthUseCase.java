package at.fhv.simplyevents.application.port.in;

import at.fhv.simplyevents.domain.model.User;

public interface AuthUseCase {
    User registerCustomer(AuthUseCase.RegisterCustomerCommand cmd);
    User registerVendor(AuthUseCase.RegisterVendorCommand cmd);
    User login(AuthUseCase.LoginCommand cmd);

    record RegisterCustomerCommand(String firstName, String lastName, String email, String password) {}
    record RegisterVendorCommand(String firstName, String lastName, String email, String password, String companyId, String contactInfo) {}
    record LoginCommand(String email, String password) {}
}
