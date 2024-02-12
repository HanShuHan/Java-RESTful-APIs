/*****************************************************************
 * File:  CustomIdentityStore.java Course materials (23S) CST 8277
 * 
 * @author Teddy Yap
 * @author Mike Norman
 *
 */
package acmecollege.security;

import static java.util.Collections.emptySet;

import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.security.enterprise.credential.CallerOnlyCredential;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;

import org.glassfish.soteria.WrappingCallerPrincipal;

import acmecollege.entity.SecurityRole;
import acmecollege.entity.SecurityUser;

@ApplicationScoped
@Typed(CustomIdentityStore.class)
public class CustomIdentityStore implements IdentityStore {

	@Inject
	protected CustomIdentityStoreJPAHelper jpaHelper;

	@Inject
	protected Pbkdf2PasswordHash pbAndjPasswordHash;

	@Override
	public CredentialValidationResult validate(Credential credential) {

		CredentialValidationResult result = CredentialValidationResult.NOT_VALIDATED_RESULT;

		if (credential instanceof UsernamePasswordCredential usrnameAndPswdCredential) {
			String callerName = usrnameAndPswdCredential.getCaller();
			SecurityUser user = jpaHelper.findUserByName(callerName);

			if (user != null) {
				/*
				 * pwHash is actually a multifield String with ':' as the field separator:
				 * <algorithm>:<iterations>:<base64(salt)>:<base64(hash)>
				 *
				 * Pbkdf2PasswordHash.Algorithm (String identifier) "PBKDF2WithHmacSHA224" - too
				 * small don't use, "PBKDF2WithHmacSHA256" - default, "PBKDF2WithHmacSHA384" -
				 * meh "PBKDF2WithHmacSHA512" - better security - more CPU: maybe not
				 * watch/phone, tablet Ok
				 *
				 * Pbkdf2PasswordHash.Iterations (integer) 1024 - minimum (too small don't use)
				 * 2048 - default I have seen 20,000 up to 50,000 in production
				 *
				 */
//                try {
				boolean verified = pbAndjPasswordHash.verify(usrnameAndPswdCredential.getPasswordAsString().toCharArray(),
						user.getPwHash());

				if (verified) {
					Set<String> rolesForUser = jpaHelper.findRoleNamesForUser(user);
					result = new CredentialValidationResult(new WrappingCallerPrincipal(user), rolesForUser);
				} else {
					result = CredentialValidationResult.INVALID_RESULT;
				}
//                }
//                catch (Exception e) {
//                    // e.printStackTrace();
//                }
			}
		}
		// Check if the credential was CallerOnlyCredential
		else if (credential instanceof CallerOnlyCredential callerOnlyCredential) {
			String callerName = callerOnlyCredential.getCaller();
			SecurityUser user = jpaHelper.findUserByName(callerName);

			if (user != null) {
				result = new CredentialValidationResult(callerName);
			}
		} else {
			result = CredentialValidationResult.INVALID_RESULT;
		}

		return result;
	}

	protected Set<String> getRolesNamesForSecurityRoles(Set<SecurityRole> roles) {
		Set<String> roleNames = emptySet();

		if (roles != null && !roles.isEmpty()) {
			roleNames = roles.stream().map(s -> s.getRoleName()).collect(Collectors.toSet());
		}

		return roleNames;
	}

}