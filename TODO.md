# Authentication Server
## REST API
* Login POST /api/v1/login
  * username
  * password
* Register POST /api/v1/register
  * username
  * password
* RefreshToken POST /api/v1/refresh [self]
  * refreshToken
* List GET /api/v1/user [admin]
* Update POST /api/v1/user/{id} [admin]
  * username
  * enabled
  * verified
  * roles
* Delete user DELETE /api/v1/user/{1} [admin]
* Set password POST /api/v1/user/{1}/password [self]

## Tables
### User
* ID
* Username/email
* Password (secure)
* Created
* Updated
* Verified
* Enabled

### Tokens
* ID
* Token (access or refresh)
* Type
* Expires
* Created
* Updated

### Roles
* ID
* Name
* Created
* Updated

### User_Role
* User_id
* Role_id

## Pages
* Login
  * Accepts a forward to URL for sending user to the app requesting authentication (optional)
  * Admin forwards to user list if no forward supplied
  * User forwards to set password page if no forward supplied
* Register 
  * Forwards to login on success
* Set password [self]
* User details (view, update, delete) [admin]
* User list (all, by role, unverified, disabled) [admin]

## Notes
* Users can register themselves
* To create first admin, register a user and add admin role via SQL command.
* Disabled or unverified users display error message after login
* Display messages at top
  * Errors stay until dismissed
  * Info messages show for 30 seconds
* How to do MFA?
