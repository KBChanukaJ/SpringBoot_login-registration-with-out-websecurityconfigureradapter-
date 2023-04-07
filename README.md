# SpringBoot_login-registration-with-out-websecurityconfigureradapter-
# Create the database
# Enter Data to the Role Table (model -> erole)
# Create the RestAPIs controllers
The authentication controller provides APIs for register and login actions.

# `/api/auth/signup`

Check existing username/email

Create new User (specifying role)

Save the user to database using `UserRepository`

# `/api/auth/signin`

Authenticate { username, pasword }
Update SecurityContext using Authentication object

Generate JWT

Get the `UserDetails` from the authentication object

The response contains JWT and `UserDetails` data
